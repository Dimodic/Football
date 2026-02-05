package com.example.footballstats.core.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateTimeUtils {
    fun parseApiDateTime(value: String?): Instant? {
        if (value.isNullOrBlank()) return null
        val v = value.trim()

        try {
            return Instant.parse(v)
        } catch (_: Throwable) {
            
        }

        try {
            return OffsetDateTime.parse(v, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
        } catch (_: Throwable) {
            
        }

        val offsetFormatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
        )
        for (fmt in offsetFormatters) {
            try {
                return OffsetDateTime.parse(v, fmt).toInstant()
            } catch (_: DateTimeParseException) {
                
            } catch (_: Throwable) {
                
            }
        }

        val localFormatters = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US),
        )
        for (fmt in localFormatters) {
            try {
                val ldt = LocalDateTime.parse(v, fmt)
                return ldt.toInstant(ZoneOffset.UTC)
            } catch (_: DateTimeParseException) {
                
            } catch (_: Throwable) {
                
            }
        }

        return null
    }

    fun parseEpochSeconds(value: Long?): Instant? {
        if (value == null) return null
        
        return if (value > 10_000_000_000L) {
            Instant.ofEpochMilli(value)
        } else {
            Instant.ofEpochSecond(value)
        }
    }

    fun parseEpochSeconds(value: Any?): Instant? {
        return when (value) {
            null -> null
            is Long -> parseEpochSeconds(value)
            is Int -> parseEpochSeconds(value.toLong())
            is Double -> parseEpochSeconds(value.toLong())
            is String -> value.trim().toLongOrNull()?.let { parseEpochSeconds(it) }
            else -> null
        }
    }

    fun formatApiDateTime(instant: Instant, timezoneOffsetHours: Int = 0): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val offset = ZoneOffset.ofHours(timezoneOffsetHours)
        return formatter.format(instant.atOffset(offset).toLocalDateTime())
    }

    fun formatTime(instant: Instant): String {
        val ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        return formatter.format(ldt)
    }

    fun formatDate(instant: Instant): String {
        val ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
        return formatter.format(ldt)
    }

    fun deviceTimeZoneOffsetHours(now: Instant = Instant.now()): Int {
        val offsetSeconds = ZoneId.systemDefault().rules.getOffset(now).totalSeconds
        return offsetSeconds / 3600
    }

    fun chipLabel(instant: Instant, now: Instant = Instant.now()): String {
        val zone = ZoneId.systemDefault()
        val date = instant.atZone(zone).toLocalDate()
        val today = now.atZone(zone).toLocalDate()

        return when {
            date == today -> "Today"
            date == today.plusDays(1) -> "Tomorrow"
            date == today.minusDays(1) -> "Yesterday"
            else -> {
                
                val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.US)
                fmt.format(date)
            }
        }
    }

    fun formatDetailsDateTime(instant: Instant): String {
        val ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy • HH:mm", Locale.US)
        return formatter.format(ldt)
    }
}
