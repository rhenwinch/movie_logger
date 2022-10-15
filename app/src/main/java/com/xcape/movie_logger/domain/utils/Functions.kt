package com.xcape.movie_logger.domain.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xcape.movie_logger.domain.model.media.Cast
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.round

object Functions {
    fun getCurrentTimestamp(): Long {
        val currentLocalDateTime: LocalDateTime = LocalDateTime.now()
        val zoneId: ZoneId = ZoneId.of(Constants.TIMEZONE)
        return currentLocalDateTime.atZone(zoneId).toEpochSecond()
    }

    fun needsUpdate(
        currentUnixTimestamp: Long,
        pastTimestamp: Long,
        updateInterval: Int? = null
    ): Boolean {
        // Check if 'UPDATE_TIMER' of hours has passed by since the pastTimestamp
        return currentUnixTimestamp - pastTimestamp > (updateInterval ?: Constants.UPDATE_TIMER)
    }

    // Convert DP to Pixel
    val Int.px: Int get() = round(this * Resources.getSystem().displayMetrics.density).toInt()

    // Convert Pixel to DP
    val Int.dp: Int get() = round(this / Resources.getSystem().displayMetrics.density).toInt()

    // Convert byte array to bitmap; vise versa
    fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // Sentence case
    fun String.sentencecase(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase(Locale.ROOT)
            else
                it.toString()
        }
    }

    //convert a data class to a map
    fun <T> T.serializeToMap(): Map<String, Any> {
        return convert()
    }

    //convert a map to a data class
    inline fun <reified T> Map<String, Any>.toDataClass(): T {
        return convert()
    }

    //convert an object of type I to type O
    inline fun <I, reified O> I.convert(): O {
        val gson = Gson()
        val json = gson.toJson(this)
        return gson.fromJson(json, object : TypeToken<O>() {}.type)
    }

    inline fun <reified T> parseFromJson(jsonString: String?): T {
        val gson = Gson()
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(jsonString, type)
    }

    inline fun <reified T> parseToJson(objectItem: T?): String? {
        val gson = Gson()
        val type = object : TypeToken<T>() {}.type
        return gson.toJson(objectItem, type)
    }

    // Async/Parallel map
    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        map { async { f(it) } }.awaitAll()
    }

    fun parseDate(date: String?): String? {
        if(date == null)
            return date

        return try {
            val dateArray = date.split("-")
            val month = when (dateArray[1].toInt()) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sept"
                10 -> "Oct"
                11 -> "Nov"
                else -> "Dec"
            }
            // Concatenate back the dates
            month + " " + dateArray[2] + ", " + dateArray[0]
        } catch (e: Exception) {
            date
        }
    }

    fun parseDuration(duration: String?): String? {
        var newDuration = duration?.replace("PT", "")
        if(duration?.contains("H") == true) {
            val indexOfH = newDuration?.indexOf("H")

            // Insert a space after 'h' character
            if (indexOfH != null) {
                newDuration = StringBuilder(newDuration)
                    .insert(indexOfH + 1, " ")
                    .toString()
            }
        }

        return newDuration?.lowercase()
    }

    fun parseCast(stars: List<Cast>?): String? {
        if (stars != null) {
            return stars.asSequence()
                .map(Cast::name)
                .joinToString(", ")
        }
        return null
    }

    // https://stackoverflow.com/a/33158859 - Check instance type of a generic function
    class Generic<T : Any>(val klass: Class<T>) {
        companion object {
            inline operator fun <reified T : Any>invoke() = Generic(T::class.java)
        }

        fun checkType(t: Any): Boolean {
            return when {
                klass.isAssignableFrom(t.javaClass) -> true
                else -> false
            }
        }
    }
}