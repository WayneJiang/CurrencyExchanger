package com.wayne.currencyexchanger.repository.typeconverter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

class InstantTypeConverter {
    @TypeConverter
    fun revertToData(seconds: Long): Instant = Instant.ofEpochSecond(seconds)

    @TypeConverter
    fun convertToStore(instant: Instant): Long = instant.epochSecond
}