package com.liveongames.data.assets.crime

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlin.math.floor
import kotlin.math.max
import kotlin.random.Random

/**
 * Crime asset loader (decoupled from ViewModel).
 *
 * Supports both JSON styles you have:
 * 1) climax: [ "line1", "line2" ]                 (array form)
 * 2) climax: { "success": "...", "fail": "...", "caught": "..." }  (object form)
 *
 * Outcome supports: SUCCESS, PARTIAL, FAIL, CAUGHT.
 */
class CrimeAssetLoader(private val context: Context) {

    private val gson = Gson()

    // ---- Raw DTOs from JSON ----
    data class OutcomeWeight(val type: String, val weight: Int)
    data class CrimePathRaw(
        val setup: List<String> = emptyList(),
        val execution: List<String> = emptyList(),
        val climax: JsonElement? = null,                 // array or object
        val outcomes: List<OutcomeWeight> = emptyList()
    )
    data class CrimeDef(
        val type: String,
        val name: String? = null,
        val durationSeconds: Int? = null,
        val paths: List<CrimePathRaw> = emptyList()
    )
    data class CrimesPack(val crimes: List<CrimeDef> = emptyList())

    // ---- Normalized runtime models ----
    enum class OutcomeType { SUCCESS, PARTIAL, FAIL, CAUGHT }
    enum class Phase { SETUP, EXECUTION, CLIMAX }

    data class NarrativePath(
        val setup: List<String>,
        val execution: List<String>,
        val climaxLines: List<String>,         // neutral climax lines (array-form)
        val climaxSuccess: String?,            // object-form fields (optional)
        val climaxFail: String?,
        val climaxCaught: String?,
        val outcomes: List<Pair<OutcomeType, Int>>
    ) {
        fun totalLines(): Int = setup.size + execution.size
    }

    data class CrimeAsset(
        /** String key of the crime type, must match enum name (e.g., "PICKPOCKETING"). */
        val typeKey: String,
        val durationSeconds: Int?,             // may be null; VM falls back by tier
        val paths: List<NarrativePath>
    )

    data class CrimeBank(val byKey: Map<String, CrimeAsset>)

    /** Load all crime files and merge to a bank. Non-suspending for easy use in VMs. */
    fun loadBank(
        files: List<String> = listOf(
            "street_crimes.json",
            "robbery_crimes.json",
            "heists_and_smuggling_crimes.json",
            "mastermind_crimes.json"
        )
    ): CrimeBank {
        val byKey = LinkedHashMap<String, CrimeAsset>()
        val packType = object : TypeToken<CrimesPack>() {}.type

        for (file in files) {
            runCatching {
                context.assets.open(file).use { input ->
                    InputStreamReader(input).use { reader ->
                        val root = JsonParser.parseReader(reader)
                        val pack = gson.fromJson<CrimesPack>(root, packType) ?: CrimesPack()

                        pack.crimes.forEach { c ->
                            if (c.type.isBlank() || c.paths.isEmpty()) return@forEach

                            val paths = c.paths.mapNotNull { p ->
                                val outs = normalizeOutcomes(p.outcomes)
                                val (climaxLines, cSucc, cFail, cCaught) = normalizeClimax(p.climax)
                                val np = NarrativePath(
                                    setup = p.setup,
                                    execution = p.execution,
                                    climaxLines = climaxLines,
                                    climaxSuccess = cSucc,
                                    climaxFail = cFail,
                                    climaxCaught = cCaught,
                                    outcomes = outs
                                )
                                if (np.totalLines() == 0) null else np
                            }

                            if (paths.isNotEmpty()) {
                                byKey[c.type] = CrimeAsset(
                                    typeKey = c.type,
                                    durationSeconds = c.durationSeconds?.takeIf { it > 0 },
                                    paths = paths
                                )
                            }
                        }
                    }
                }
            }.onFailure { it.printStackTrace() }
        }
        return CrimeBank(byKey = byKey)
    }

    /** Random path selection. */
    fun pickPath(asset: CrimeAsset, rng: Random = Random.Default): NarrativePath =
        asset.paths.getOrElse(rng.nextInt(asset.paths.size)) { asset.paths.first() }

    /** Phase calculation with a 25/55/20 split. */
    fun phaseFor(elapsedSec: Int, durationSec: Int): Phase {
        val total = max(1, durationSec)
        val e = elapsedSec.coerceIn(0, total)
        val setupEnd = (0.25f * total)
        val execEnd = (0.80f * total) // 0.25 + 0.55
        return when {
            e < setupEnd -> Phase.SETUP
            e < execEnd  -> Phase.EXECUTION
            else         -> Phase.CLIMAX
        }
    }

    /** Rotating status line for current phase; climax text is kept for the final reveal. */
    fun messageFor(
        elapsedSec: Int,
        durationSec: Int,
        path: NarrativePath,
        messageIntervalSec: Int = 3
    ): String {
        val phase = phaseFor(elapsedSec, durationSec)
        val list = when (phase) {
            Phase.SETUP     -> path.setup
            Phase.EXECUTION -> path.execution
            Phase.CLIMAX    -> if (path.execution.isNotEmpty()) path.execution else path.setup
        }
        if (list.isEmpty()) return ""
        val idx = floor(elapsedSec / messageIntervalSec.toFloat()).toInt().mod(list.size)
        return list[idx]
    }

    /** Weighted outcome at completion. */
    fun pickOutcome(path: NarrativePath, rng: Random = Random.Default): OutcomeType {
        val total = path.outcomes.sumOf { it.second }.coerceAtLeast(1)
        var roll = rng.nextInt(total)
        for ((type, weight) in path.outcomes) {
            if (roll < weight) return type
            roll -= weight
        }
        return path.outcomes.last().first
    }

    /** Final reveal line for the outcome, preferring explicit success/fail/caught; otherwise neutral climaxLines. */
    fun finalLine(path: NarrativePath, outcome: OutcomeType): String? = when (outcome) {
        OutcomeType.SUCCESS -> path.climaxSuccess ?: path.climaxLines.firstOrNull()
        OutcomeType.PARTIAL -> path.climaxLines.firstOrNull()
        OutcomeType.FAIL    -> path.climaxFail ?: path.climaxLines.firstOrNull()
        OutcomeType.CAUGHT  -> path.climaxCaught ?: path.climaxLines.firstOrNull()
    }

    // ---- helpers ----
    private fun normalizeOutcomes(raw: List<OutcomeWeight>): List<Pair<OutcomeType, Int>> {
        if (raw.isEmpty()) return listOf(OutcomeType.SUCCESS to 100)
        val mapped = raw.mapNotNull { ow ->
            val t = when (ow.type.uppercase()) {
                "SUCCESS" -> OutcomeType.SUCCESS
                "PARTIAL" -> OutcomeType.PARTIAL
                "FAIL"    -> OutcomeType.FAIL
                "CAUGHT"  -> OutcomeType.CAUGHT
                else -> null
            } ?: return@mapNotNull null
            t to ow.weight.coerceAtLeast(0)
        }
        return mapped.takeIf { it.isNotEmpty() } ?: listOf(OutcomeType.SUCCESS to 100)
    }

    /** Supports array-based and object-based climax. */
    private fun normalizeClimax(el: JsonElement?): Quad<List<String>, String?, String?, String?> {
        if (el == null) return Quad(emptyList(), null, null, null)
        return when {
            el.isJsonArray -> {
                val arr = el.asJsonArray
                Quad(arr.mapNotNull { it.asSafeString() }, null, null, null)
            }
            el.isJsonObject -> {
                val obj = el.asJsonObject
                Quad(
                    emptyList(),
                    obj.get("success").asSafeString(),
                    obj.get("fail").asSafeString(),
                    obj.get("caught").asSafeString()
                )
            }
            else -> Quad(emptyList(), null, null, null)
        }
    }

    private fun JsonElement?.asSafeString(): String? = try {
        if (this == null || !this.isJsonPrimitive) null else this.asString
    } catch (_: Exception) { null }

    private data class Quad<A,B,C,D>(val a: A, val b: B, val c: C, val d: D)
}
