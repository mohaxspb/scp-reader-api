package ru.kuchanov.scpreaderapi.utils

/**
 * converts given millis to minutes:seconds format
 */
fun millisToMinutesAndSeconds(duration: Long): Pair<Long, Long> {
    val minutes = duration / 1000 / 60
    val seconds = duration / 1000 % 60
    return minutes to seconds
}