package com.wayne.currencyexchanger.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CURRENCY")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "SYMBOL")
    var symbol: String = "",
    @ColumnInfo(name = "DESCRIPTION")
    var description: String = ""
)