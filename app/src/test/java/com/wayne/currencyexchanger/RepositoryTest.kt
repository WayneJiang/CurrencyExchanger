package com.wayne.currencyexchanger

import app.cash.turbine.test
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity
import com.wayne.currencyexchanger.viewmodel.MainViewModel
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.time.Instant

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
class RepositoryTest {
    private lateinit var mMainViewModel: MainViewModel

    @Before
    fun setup() {
        mMainViewModel = MainViewModel()
    }

    /**
     * Test for currencies data changed will tell UI
     */
    @Test
    fun testCurrenciesFlow() = runTest {
        mMainViewModel.currencies.test {
            mMainViewModel.currencies.tryEmit(
                listOf(
                    CurrencyEntity("TWD", "New Taiwan Dollar"),
                    CurrencyEntity("USD", "US Dollar")
                )
            )

            val item = expectMostRecentItem()

            assertThat(item.size, `is`(2))
            assertThat(item[0].symbol, `is`("TWD"))
            assertThat(item[0].description, `is`("New Taiwan Dollar"))
            assertThat(item[1].symbol, `is`("USD"))
            assertThat(item[1].description, `is`("US Dollar"))
        }
    }

    /**
     * Test for history data changed will tell UI
     */
    @Test
    fun testHistoryDataFlow() = runTest {
        mMainViewModel.historyData.test {
            mMainViewModel.historyData.tryEmit(
                HistoryEntity("USD", Instant.now(), mapOf(Pair("TWD", 32f)).toString())
            )

            val item = expectMostRecentItem()

            assertThat(item, notNullValue())

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

            item?.let { historyEntity ->
                assertThat(historyEntity.baseCurrency, `is`("USD"))

                mapJsonAdapter.fromJson(item.currencyRateMap)?.let { rateMap ->
                    rateMap.forEach { (key, value) ->
                        assertThat(key, `is`("TWD"))
                        assertThat(value, `is`(32f))
                    }
                }
            }
        }
    }
}