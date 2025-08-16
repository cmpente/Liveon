// app/src/main/java/com/liveongames/data/assets/crime/CrimeAssetLoader.kt
package com.liveongames.data.assets.crime

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

class CrimeAssetLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class OutcomeType { SUCCESS, PARTIAL, FAIL, CAUGHT }

    data class CrimeBank(val byKey: Map<String, CrimeAsset>)

    // ----- Raw pack model (matches JSON) -----
    data class CrimesPack(
        val crimes: List<CrimeDef> = emptyList(),
        val defaults: Defaults? = null
    )
    data class Defaults(
        val messageIntervalSec: Int? = null,
        val phaseSplit: PhaseSplit? = null,           // { setup: 0.25, execution: 0.55, climax: 0.20 }
        val cooldowns: Cooldowns? = null              // { onFailSec: 6, onArrestSec: 30 }
    )
    data class PhaseSplit(val setup: Float = 0.25f, val execution: Float = 0.55f, val climax: Float = 0.20f)
    data class Cooldowns(val onFailSec: Int? = null, val onArrestSec: Int? = null)

    data class CrimeDef(
        val type: String,
        val name: String? = null,
        val durationSeconds: Int? = null,
        val paths: List<PathDef> = emptyList()
    )
    data class PathDef(
        val setup: List<String> = emptyList(),
        val execution: List<String> = emptyList(),
        val climax: JsonElement? = null,             // can be array or object { success, partial, fail, caught }
        val ambient: List<String> = emptyList(),
        val outcomes: List<OutcomeWeight> = emptyList()
    )
    data class OutcomeWeight(val type: String, val weight: Int)

    // ----- Normalized asset model used by VM/UI -----
    data class NarrativePath(
        val setup: List<String>,
        val execution: List<String>,
        val climaxLines: List<String>,      // generic lines if climax was an array
        val climaxSuccess: String?,
        val climaxPartial: String?,
        val climaxFail: String?,
        val climaxCaught: String?,
        val ambient: List<String>,
        val outcomes: List<Pair<OutcomeType, Int>>
    ) {
        fun totalLines(): Int = setup.size + execution.size
    }

    data class CrimeAsset(
        val typeKey: String,
        val durationSeconds: Int?,
        val paths: List<NarrativePath>,
        val ambientCadenceMs: Long?,         // pack default message cadence
        val phaseSplit: PhaseSplit?,         // pack default phase split
        val cooldownOnFailMs: Long?,         // pack default cooldowns
        val cooldownOnArrestMs: Long?
    )

    private val gson = Gson()

    fun loadBank(): CrimeBank {
        val files = listOf(
            "street_crimes.json",
            "robbery_crimes.json",
            "heists_and_smuggling_crimes.json",
            "mastermind_crimes.json"
        )
        val byKey = mutableMapOf<String, CrimeAsset>()
        for (file in files) {
            val json = runCatching { context.assets.open(file).bufferedReader().use { it.readText() } }.getOrNull()
                ?: continue

            // Try to parse as pack (with defaults) first; fallback to legacy [CrimeDef] list.
            val pack: CrimesPack = runCatching {
                val type = object : TypeToken<CrimesPack>() {}.type
                gson.fromJson<CrimesPack>(json, type)
            }.getOrElse {
                val arrType = object : TypeToken<List<CrimeDef>>() {}.type
                val list = gson.fromJson<List<CrimeDef>>(json, arrType) ?: emptyList()
                CrimesPack(crimes = list, defaults = null)
            }

            val ambientMs  = pack.defaults?.messageIntervalSec?.let { (it.coerceAtLeast(1)) * 1000L }
            val split      = pack.defaults?.phaseSplit
            val cdFailMs   = pack.defaults?.cooldowns?.onFailSec?.let { it.coerceAtLeast(0) * 1000L }
            val cdArrestMs = pack.defaults?.cooldowns?.onArrestSec?.let { it.coerceAtLeast(0) * 1000L }

            pack.crimes.forEach { c ->
                val paths = c.paths.mapNotNull { p ->
                    val outs = normalizeOutcomes(p.outcomes)
                    val climax = normalizeClimax(p.climax)
                    val np = NarrativePath(
                        setup = p.setup,
                        execution = p.execution,
                        climaxLines = climax.a,
                        climaxSuccess = climax.b,
                        climaxPartial = climax.c,
                        climaxFail = climax.d,
                        climaxCaught = climax.e,
                        ambient = p.ambient,
                        outcomes = outs
                    )
                    if (np.totalLines() == 0) null else np
                }
                if (paths.isNotEmpty()) {
                    byKey[c.type] = CrimeAsset(
                        typeKey = c.type,
                        durationSeconds = c.durationSeconds?.takeIf { it > 0 },
                        paths = paths,
                        ambientCadenceMs = ambientMs,
                        phaseSplit = split,
                        cooldownOnFailMs = cdFailMs,
                        cooldownOnArrestMs = cdArrestMs
                    )
                }
            }
        }
        return CrimeBank(byKey)
    }

    fun pickPath(asset: CrimeAsset, rng: Random): NarrativePath =
        asset.paths[if (asset.paths.size == 1) 0 else rng.nextInt(asset.paths.size)]

    fun pickOutcome(path: NarrativePath): OutcomeType {
        val pool = path.outcomes
        if (pool.isEmpty()) return OutcomeType.SUCCESS
        val total = pool.sumOf { it.second.coerceAtLeast(0) }
        if (total <= 0) return OutcomeType.SUCCESS
        val r = Random.nextInt(total)
        var acc = 0
        for ((ot, w) in pool) {
            acc += w.coerceAtLeast(0)
            if (r < acc) return ot
        }
        return OutcomeType.SUCCESS
    }

    fun finalLine(path: NarrativePath, outcome: OutcomeType): String? = when (outcome) {
        OutcomeType.SUCCESS -> path.climaxSuccess ?: path.climaxLines.firstOrNull()
        OutcomeType.PARTIAL -> path.climaxPartial ?: path.climaxLines.firstOrNull()
        OutcomeType.FAIL    -> path.climaxFail ?: path.climaxLines.firstOrNull()
        OutcomeType.CAUGHT  -> path.climaxCaught ?: path.climaxLines.firstOrNull()
    }

    // -------- helpers --------
    private data class Quint<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

    private fun normalizeOutcomes(list: List<OutcomeWeight>): List<Pair<OutcomeType, Int>> =
        list.mapNotNull { ow ->
            val ot = when (ow.type.lowercase()) {
                "success" -> OutcomeType.SUCCESS
                "partial" -> OutcomeType.PARTIAL
                "fail"    -> OutcomeType.FAIL
                "caught"  -> OutcomeType.CAUGHT
                else      -> null
            } ?: return@mapNotNull null
            ot to ow.weight.coerceAtLeast(0)
        }

    private fun normalizeClimax(el: JsonElement?): Quint<List<String>, String?, String?, String?, String?> {
        if (el == null) return Quint(emptyList(), null, null, null, null)
        return if (el.isJsonArray) {
            val arr = el.asJsonArray
            Quint(arr.mapNotNull { it.asSafeString() }, null, null, null, null)
        } else if (el.isJsonObject) {
            val obj = el.asJsonObject
            Quint(
                emptyList(),
                obj.get("success").asSafeString(),
                obj.get("partial").asSafeString(),
                obj.get("fail").asSafeString(),
                obj.get("caught").asSafeString()
            )
        } else Quint(emptyList(), null, null, null, null)
    }

    private fun JsonElement?.asSafeString(): String? =
        try {
            when (this) {
                null -> null
                is JsonObject -> null
                else -> this.asString?.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) { null }
}