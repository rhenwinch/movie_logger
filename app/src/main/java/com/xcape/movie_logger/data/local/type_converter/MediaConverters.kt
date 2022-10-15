package com.xcape.movie_logger.data.local.type_converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.xcape.movie_logger.domain.model.media.MediaInfo
import com.xcape.movie_logger.domain.utils.Functions.parseFromJson
import com.xcape.movie_logger.domain.utils.Functions.parseToJson
import java.util.*

@ProvidedTypeConverter
class MediaConverters {
    @TypeConverter
    fun toMedia(mediaJson: String?): MediaInfo? {
        if(mediaJson == null)
            return null

        return parseFromJson<MediaInfo>(mediaJson)
    }

    @TypeConverter
    fun toStringMedia(mediaInfoParsedJson: MediaInfo?): String? {
        if(mediaInfoParsedJson == null)
            return null

        return parseToJson(mediaInfoParsedJson)
    }

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}