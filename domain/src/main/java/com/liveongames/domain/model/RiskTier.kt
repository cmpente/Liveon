package com.liveongames.domain.model

/**
 * Coarse risk band used by the data layer and UI.
 * Keep names stable; storage may persist the enum name.
 */
enum class RiskTier {
    LOW,
    MEDIUM,
    HIGH,
    EXTREME
}
