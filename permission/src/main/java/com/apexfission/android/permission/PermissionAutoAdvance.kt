package com.apexfission.android.permission

import kotlin.math.roundToInt

/** Approximate reading pace for an explicitly supplied page text. */
enum class ReadingPace(val wordsPerMinute: Int) {
    Slow(120),
    Normal(180),
    Fast(230),
}

/**
 * Estimates an auto-advance delay for [text] using whitespace-delimited words at [pace].
 * Includes two seconds for visual orientation and bounds the result to 4–60 seconds.
 * For languages without word separators or content dominated by images, supply an explicit
 * `autoAdvanceDelayMillis` instead. This function never inspects a composable's UI.
 */
fun estimateReadingDelayMillis(text: String, pace: ReadingPace = ReadingPace.Slow): Long {
    val words = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
    val estimated = 2_000L + words * 60_000L / pace.wordsPerMinute
    return estimated.coerceIn(4_000L, 60_000L)
}

internal fun nextAutoAdvancePage(currentPage: Int, pageCount: Int): Int? =
    if (pageCount <= 1) null else (currentPage + 1) % pageCount

internal fun remainingAutoAdvanceMillis(progress: Float, totalMillis: Long): Int =
    ((1f - progress.coerceIn(0f, 1f)) * totalMillis).roundToInt().coerceAtLeast(1)
