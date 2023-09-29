package com.wayne.currencyexchanger.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wayne.currencyexchanger.repository.dao.CurrencyEntityDao
import com.wayne.currencyexchanger.repository.dao.HistoryEntityDao
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
@Database(
    entities = [CurrencyEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DatabaseAbstract : RoomDatabase() {
    abstract fun getCurrencyEntityDao(): CurrencyEntityDao

    abstract fun getHistoryEntityDao(): HistoryEntityDao
}