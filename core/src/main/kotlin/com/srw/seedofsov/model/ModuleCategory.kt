package com.srw.seedofsov.model

/**
 * Defines the main categories of ship modules.
 */
enum class ModuleCategory(val displayName: String) {
    ENGINE("ENGINES"),
    NAVIGATION("NAV"),
    WEAPON_PRIMARY("PRI-WPN"),
    WEAPON_SECONDARY("SEC-WPN"),
    POWER_CORE("POWER"),
    SHIELD("SHIELDS"),
    BAY("BAY"),
    POINT_DEFENSE("PDC"),
    UTILITY("UTIL")
}
