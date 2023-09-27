package com.wayne.currencyexchanger.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.wayne.currencyexchanger.repository.typeconverter.InstantTypeConverter
import java.time.Instant

@Entity(tableName = "HISTORY")
@TypeConverters(InstantTypeConverter::class)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "BASE_CURRENCY")
    var baseCurrency: String = "",
    @ColumnInfo(name = "TIMESTAMP")
    var timestamp: Instant = Instant.now(),
    @ColumnInfo(name = "CURRENCY_RATE")
    var currencyRateMap: String = ""
)