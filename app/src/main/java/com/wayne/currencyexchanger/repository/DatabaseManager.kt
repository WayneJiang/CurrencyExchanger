package com.wayne.currencyexchanger.repository

import android.content.Context
import androidx.room.Room
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
object DatabaseManager {
    private lateinit var mDatabaseAbstract: DatabaseAbstract

    fun setup(context: Context) {
        mDatabaseAbstract =
            Room.databaseBuilder(context, DatabaseAbstract::class.java, "Database")
                .build()
    }

    fun CurrencyEntity.insert() = mDatabaseAbstract.getCurrencyEntityDao().insert(this)

    fun queryCurrencyEntities() = mDatabaseAbstract.getCurrencyEntityDao().query()

    fun HistoryEntity.insert() = mDatabaseAbstract.getHistoryEntityDao().insert(this)

    fun queryHistoryEntity() = mDatabaseAbstract.getHistoryEntityDao().query()
}