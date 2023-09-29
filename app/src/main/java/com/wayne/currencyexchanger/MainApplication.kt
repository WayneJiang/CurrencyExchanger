package com.wayne.currencyexchanger

import android.app.Application
import com.wayne.currencyexchanger.repository.DatabaseManager

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        DatabaseManager.setup(this)
    }
}