package com.wayne.currencyexchanger

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.wayne.currencyexchanger.repository.DatabaseAbstract
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.isEmptyOrNullString
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
class DataBaseTest {
    private lateinit var mDatabaseAbstract: DatabaseAbstract

    @Before
    fun setup() {
        mDatabaseAbstract =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                DatabaseAbstract::class.java
            ).build()
    }

    @After
    fun shutdown() {
        mDatabaseAbstract.close()
    }

    /**
     * Test currencies data insert/update
     */
    @Test
    fun testInsertCurrencyAndUpdate(): Unit = runTest {
        val currencyEntityDao = mDatabaseAbstract.getCurrencyEntityDao()

        val currencyEntity = CurrencyEntity("TWD", "Taiwan Dollar")

        currencyEntityDao.insert(currencyEntity)

        currencyEntityDao.query().forEach {
            assertThat(it, notNullValue())
            assertThat(it.symbol, `is`("TWD"))
            assertThat(it.description, `is`("Taiwan Dollar"))
        }

        val updatedCurrencyEntity = CurrencyEntity("TWD", "New Taiwan Dollar")

        currencyEntityDao.insert(updatedCurrencyEntity)

        currencyEntityDao.query().forEach {
            assertThat(it, notNullValue())
            assertThat(it.symbol, `is`("TWD"))
            assertThat(it.description, `is`("New Taiwan Dollar"))
        }
    }

    /**
     * Test history data insert/update
     */
    @Test
    fun testInsertLatestAndUpdate(): Unit = runTest {
        val historyEntityDao = mDatabaseAbstract.getHistoryEntityDao()

        val currencyRateMap = mapOf(Pair("TWD", 32f))

        val historyEntity = HistoryEntity("USD", Instant.now(), currencyRateMap.toString())

        historyEntityDao.insert(historyEntity)

        val mapType =
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Float::class.javaObjectType
            )

        val mapJsonAdapter =
            Moshi.Builder()
                .build()
                .adapter<Map<String, Float>>(mapType)
                .lenient()

        historyEntityDao.query()?.let {
            assertThat(it, notNullValue())
            assertThat(it.baseCurrency, `is`("USD"))
            assertThat(it.timestamp.epochSecond, greaterThan(0L))
            assertThat(it.currencyRateMap, not(isEmptyOrNullString()))

            mapJsonAdapter.fromJson(it.currencyRateMap)?.let { map ->
                assertThat(map, instanceOf(Map::class.java))
            }
        }

        val updatedCurrencyRateMap = mapOf(Pair("TWD", 32f))
        val updatedHistoryEntity =
            HistoryEntity("USD", Instant.now(), updatedCurrencyRateMap.toString())

        historyEntityDao.insert(updatedHistoryEntity)

        historyEntityDao.query()?.let {
            assertThat(it, notNullValue())
            assertThat(it.baseCurrency, `is`("USD"))
            assertThat(it.timestamp.epochSecond, greaterThan(0))
            assertThat(it.currencyRateMap, not(isEmptyOrNullString()))

            mapJsonAdapter.fromJson(it.currencyRateMap)?.let { map ->
                assertThat(map, instanceOf(Map::class.java))
            }
        }
    }
}