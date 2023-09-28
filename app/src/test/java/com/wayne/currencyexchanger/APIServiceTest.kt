package com.wayne.currencyexchanger

import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wayne.currencyexchanger.repository.OpenExchangeAPI
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.json.LatestData
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Okio
import okio.buffer
import okio.source
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.net.HttpURLConnection

/*
 * Copyright (c) 2023 GoMore Inc. All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/27
 */
class APIServiceTest {
    private lateinit var mOpenExchangeAPI: OpenExchangeAPI

    private lateinit var mMockWebServer: MockWebServer

    private lateinit var mMapJsonAdapter: JsonAdapter<Map<String, String>>

    private lateinit var mLatestDataJsonAdapter: JsonAdapter<LatestData>

    @Before
    fun setup() {
        mMockWebServer = MockWebServer()
        mMockWebServer.start()

        mOpenExchangeAPI = Retrofit.Builder()
            .baseUrl(mMockWebServer.url("/"))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(OpenExchangeAPI::class.java)

        val mapType =
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                String::class.java
            )

        mMapJsonAdapter =
            Moshi.Builder()
                .build()
                .adapter(mapType)

        mLatestDataJsonAdapter =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(LatestData::class.java)
    }

    @After
    fun shutdown() {
        mMockWebServer.close()
    }

    @Test
    fun testGetCurrenciesAPISuccess(): Unit = runBlocking {
        javaClass.classLoader?.getResourceAsStream("currencies.json")?.source()?.buffer()?.let {
            val mockResponse =
                MockResponse().apply {
                    setResponseCode(HttpURLConnection.HTTP_OK)
                    setBody(it.readString(Charsets.UTF_8))
                }

            mMockWebServer.enqueue(mockResponse)
        }

        val response = mOpenExchangeAPI.getCurrenciesAsync().await()

        val request = mMockWebServer.takeRequest()

        assertThat(request.path, `is`("/currencies.json"))
        assertThat(response.isSuccessful, `is`(true))

        response.body()?.source()?.let { source ->
            assertThat(source.buffer.size, not(0))
            assertThat(mMapJsonAdapter.fromJson(source), notNullValue())
        }
    }

    @Test
    fun testGetCurrenciesAPIFail(): Unit = runBlocking {
        val mockResponse =
            MockResponse()
                .apply {
                    setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                }

        mMockWebServer.enqueue(mockResponse)

        val response = mOpenExchangeAPI.getCurrenciesAsync().await()

        val request = mMockWebServer.takeRequest()

        assertThat(request.path, `is`("/currencies.json"))
        assertThat(response.code(), `is`(HttpURLConnection.HTTP_FORBIDDEN))
    }

    @Test
    fun testGetLatestAPISuccess(): Unit = runBlocking {
        javaClass.classLoader?.getResourceAsStream("latest.json")?.source()?.buffer()?.let {
            val mockResponse =
                MockResponse().apply {
                    setResponseCode(HttpURLConnection.HTTP_OK)
                    setBody(it.readString(Charsets.UTF_8))
                }

            mMockWebServer.enqueue(mockResponse)
        }

        val response = mOpenExchangeAPI.getLatestAsync(mapOf()).await()

        val request = mMockWebServer.takeRequest()

        assertThat(request.path, `is`("/latest.json"))
        assertThat(response.isSuccessful, `is`(true))

        response.body()?.source()?.let { source ->
            assertThat(source.buffer.size, not(0))
            assertThat(mLatestDataJsonAdapter.fromJson(source), notNullValue())
        }
    }

    @Test
    fun testGetLatestAPIFail(): Unit = runBlocking {
        val mockResponse =
            MockResponse().apply {
                setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            }

        mMockWebServer.enqueue(mockResponse)

        val response = mOpenExchangeAPI.getLatestAsync(mapOf()).await()

        val request = mMockWebServer.takeRequest()

        assertThat(request.path, `is`("/latest.json"))
        assertThat(response.code(), `is`(HttpURLConnection.HTTP_FORBIDDEN))
    }
}