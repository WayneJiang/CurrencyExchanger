package com.wayne.currencyexchanger.repository

import android.net.Uri
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wayne.currencyexchanger.repository.DatabaseManager.insert
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity
import com.wayne.currencyexchanger.repository.json.LatestData
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.net.UnknownHostException
import java.time.Instant
import java.util.concurrent.TimeUnit

/*
 * Copyright (c) 2023 GoMore Inc. All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
object APIService {
    private val mOkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            retryOnConnectionFailure(true)
            connectTimeout(300, TimeUnit.SECONDS)
            writeTimeout(300, TimeUnit.SECONDS)
            readTimeout(300, TimeUnit.SECONDS)
            addInterceptor(
                HttpLoggingInterceptor(HttpLogger())
                    .apply {
                        level =
                            HttpLoggingInterceptor.Level.BODY
                    })
        }.build()
    }

    private class HttpLogger : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            val maxLength = 2000
            var start = 0
            var end = maxLength
            val logLength = message.length
            for (index in 0 until 100) {
                if (logLength > end) {
                    Log.d(
                        "Wayne",
                        Uri.decode(message.substring(start, end))
                    )
                    start = end
                    end += maxLength
                } else {
                    Log.d(
                        "WayneOP",
                        Uri.decode(message.substring(start, logLength))
                    )
                    break
                }
            }
        }
    }

    private val mAPIInterface by lazy {
        Retrofit.Builder()
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl("https://openexchangerates.org/api/")
            .client(mOkHttpClient)
            .build()
            .create(OpenExchangeAPI::class.java)
    }

    private const val APP_ID = "78c06c5b327f4bc9931232c1b924804d"

    suspend fun requestCurrencies() {
        try {
            mAPIInterface.getCurrenciesAsync().await().apply {
                if (isSuccessful) {
                    val response = (body() as ResponseBody).string()

                    val mapType =
                        Types.newParameterizedType(
                            Map::class.java,
                            String::class.java,
                            String::class.java
                        )

                    val mapJsonAdapter =
                        Moshi.Builder()
                            .build()
                            .adapter<Map<String, String>>(mapType)

                    mapJsonAdapter.fromJson(response)?.forEach { (key, value) ->
                        CurrencyEntity(key, value).insert()
                    }
                } else {
                }
            }
        } catch (throwable: Throwable) {
            Log.d("Wayne", Log.getStackTraceString(throwable))

            val code =
                if (throwable is UnknownHostException) {
                    -9998
                } else {
                    -9999
                }
        }
    }

    suspend fun requestLatest(baseCurrency: String) {
        val queryMap = mutableMapOf<String, String>()
        queryMap["app_id"] = APP_ID
        queryMap["base"] = baseCurrency

        try {
            mAPIInterface.getLatestAsync(queryMap).await().apply {
                if (isSuccessful) {
                    val response = (body() as ResponseBody).string()

                    val latestDataJsonAdapter =
                        Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                            .adapter(LatestData::class.java)

                    latestDataJsonAdapter.fromJson(response)?.apply {
                        HistoryEntity(
                            baseCurrency,
                            Instant.now().epochSecond,
                            rates.toString()
                        ).insert()
                    }
                } else {
                }
            }
        } catch (throwable: Throwable) {
            Log.d("Wayne", Log.getStackTraceString(throwable))

            val code =
                if (throwable is UnknownHostException) {
                    -9998
                } else {
                    -9999
                }
        }
    }
}