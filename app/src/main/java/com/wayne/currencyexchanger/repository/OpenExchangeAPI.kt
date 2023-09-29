package com.wayne.currencyexchanger.repository

import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
interface OpenExchangeAPI {
    @GET("currencies.json")
    fun getCurrenciesAsync(): Deferred<Response<ResponseBody>>

    @GET("latest.json")
    fun getLatestAsync(@QueryMap map: Map<String, String>): Deferred<Response<ResponseBody>>
}