package com.wayne.currencyexchanger

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.wayne.currencyexchanger.databinding.ActivityMainBinding
import com.wayne.currencyexchanger.view.CurrencyRateAdapter
import com.wayne.currencyexchanger.view.CurrencySymbolAdapter
import com.wayne.currencyexchanger.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
 * Copyright (c) 2023 GoMore Inc. All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
class MainActivity : AppCompatActivity() {
    private lateinit var mActivityMainBinding: ActivityMainBinding

    private val mMainViewModel by viewModels<MainViewModel>()

    private val mCurrencyRateAdapter by lazy {
        CurrencyRateAdapter()
    }

    private val mGridManager by lazy {
        GridLayoutManager(
            this@MainActivity,
            3,
            GridLayoutManager.VERTICAL,
            false
        )
    }

    private val mItemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val position = parent.getChildAdapterPosition(view)

            val space = 20

            outRect.left = 20
            outRect.right = 20

            outRect.top =
                if (position < 3) {
                    0
                } else {
                    space
                }
            outRect.bottom = space
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivityMainBinding =
            ActivityMainBinding.inflate(layoutInflater).apply {
                setContentView(root)

                viewRecycler.apply {
                    layoutManager = mGridManager

                    addItemDecoration(mItemDecoration)

                    adapter = mCurrencyRateAdapter
                }
            }

        lifecycleScope.launch(Dispatchers.IO) {
//            APIService.requestLatest("USD")
//            APIService.requestCurrencies()
        }

        mMainViewModel.retrieveCurrenciesAsync()
        mMainViewModel.retrieveHistoryDataAsync("USD")
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    loadPersistData()
                }

                launch {
                    loadAvailableCurrency()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        window.apply {
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            setDecorFitsSystemWindows(false)

            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT

            insetsController?.apply {
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                hide(WindowInsetsCompat.Type.navigationBars())

                setSystemBarsAppearance(
                    (WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS),
                    (WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
                )
            }
        }
    }

    private suspend fun loadAvailableCurrency() {
        mMainViewModel.currencies.collect {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                mActivityMainBinding.spinnerCurrency.adapter =
                    CurrencySymbolAdapter(
                        this,
                        it.map { currencyEntity -> currencyEntity.symbol }
                    )
            }
        }
    }

    private suspend fun loadPersistData() {
        mMainViewModel.historyData.collect {
            it?.apply {
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

                mapJsonAdapter.fromJson(it.currencyRateMap)?.apply {
                    mCurrencyRateAdapter.submitList(
                        map { keyValue ->
                            Pair(
                                keyValue.key,
                                keyValue.value
                            )
                        }
                    )
                }
            }
        }
    }
}