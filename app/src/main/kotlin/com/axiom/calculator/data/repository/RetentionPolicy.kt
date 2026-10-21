package com.axiom.calculator.data.repository

/** History auto-deletion retention options (LIB-002). Default is THIRTY_DAYS per SECTION 12. */
enum class RetentionPolicy(val storageKey: String, val displayName: String, val durationMillis: Long?) {
    ONE_DAY("1d", "1 day", 24L * 60 * 60 * 1000),
    SEVEN_DAYS("7d", "7 days", 7L * 24 * 60 * 60 * 1000),
    THIRTY_DAYS("30d", "30 days", 30L * 24 * 60 * 60 * 1000),
    NINETY_DAYS("90d", "90 days", 90L * 24 * 60 * 60 * 1000),
    NEVER("never", "Never", null);

    companion object {
        val DEFAULT = THIRTY_DAYS
        fun fromStorageKey(key: String?): RetentionPolicy = entries.find { it.storageKey == key } ?: DEFAULT
    }
}
