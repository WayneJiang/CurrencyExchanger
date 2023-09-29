package com.wayne.currencyexchanger.repository.json

import com.squareup.moshi.Json

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
data class LatestData(
    @Json(name = "disclaimer")
    val disclaimer: String = "",
    @Json(name = "license")
    val license: String = "",
    @Json(name = "timestamp")
    val timestamp: Int = -9999,
    @Json(name = "base")
    val base: String = "",
    @Json(name = "rates")
    val rates: Map<String, Float> = mapOf()
)
