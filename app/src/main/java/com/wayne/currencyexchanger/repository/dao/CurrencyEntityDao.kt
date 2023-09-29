package com.wayne.currencyexchanger.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
@Dao
interface CurrencyEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currencyEntity: CurrencyEntity)

    @Query("SELECT * FROM CURRENCY")
    fun query(): List<CurrencyEntity>
}