package com.liveongames.liveon.model

import com.liveongames.data.db.entity.TermStateEntity

data class TermState(
    val weekOfTerm: Int,
    val progress: Float
)

fun TermStateEntity.toModel(): TermState {
    return TermState(
        weekOfTerm = this.weekIndex,
        progress = when (this.coursePhase) {
            "EARLY" -> 0.0f
            "MID" -> 0.5f
            "LATE" -> 1.0f
            else -> 0.0f
        }
    )
}