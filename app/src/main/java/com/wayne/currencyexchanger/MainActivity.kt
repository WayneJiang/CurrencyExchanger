package com.wayne.currencyexchanger

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.wayne.currencyexchanger.databinding.ActivityMainBinding
import com.wayne.currencyexchanger.repository.APIService
import com.wayne.currencyexchanger.view.CurrencyRateAdapter
import com.wayne.currencyexchanger.view.CurrencySymbolAdapter
import com.wayne.currencyexchanger.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    private val mOnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            p0?.getItemAtPosition(p2)?.let {
                mMainViewModel.retrieveHistoryDataAsync()
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
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

                edAmount.doAfterTextChanged {
                    mMainViewModel.retrieveHistoryDataAsync()
                }
            }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    repositoryStatusChanged()
                }

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

        mMainViewModel.retrieveCurrenciesAsync()
    }

    private suspend fun repositoryStatusChanged() {
        mMainViewModel.repositoryStatus.collect {
            when (it) {
                APIService.CODE_CURRENCIES_RETRIEVED -> {
                    mMainViewModel.retrieveCurrenciesAsync()
                }

                APIService.CODE_LATEST_RATE_RETRIEVED -> {
                    val dateTime =
                        Instant.now()
                            .atZone(ZoneId.systemDefault()).format(
                                DateTimeFormatter.ofPattern(getString(R.string.datetime_format))
                            )

                    mMainViewModel.retrieveCurrenciesAsync()

                    Snackbar.make(
                        mActivityMainBinding.root,
                        getString(R.string.last_update, dateTime),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                APIService.CODE_NETWORK_ERROR -> {
                    Snackbar.make(
                        mActivityMainBinding.root,
                        getString(R.string.network_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                APIService.CODE_UNKNOWN_ERROR -> {
                    Snackbar.make(
                        mActivityMainBinding.root,
                        getString(R.string.unknown_error),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Snackbar.make(
                        mActivityMainBinding.root,
                        getString(R.string.http_error, it),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun loadAvailableCurrency() {
        mMainViewModel.currencies.collect {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                mActivityMainBinding.spinnerCurrency.apply {
                    adapter =
                        CurrencySymbolAdapter(
                            this@MainActivity,
                            it.map { currencyEntity -> currencyEntity.symbol }
                        )

                    onItemSelectedListener = mOnItemSelectedListener

                    setSelection(it.indexOfFirst { currencyEntity -> currencyEntity.symbol == "USD" })
                }

                mMainViewModel.retrieveHistoryDataAsync()
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
                    val dateTime =
                        it.timestamp.atZone(ZoneId.systemDefault()).format(
                            DateTimeFormatter.ofPattern(getString(R.string.datetime_format))
                        )

                    mActivityMainBinding.tvUpdate.text = getString(R.string.last_update, dateTime)

                    mCurrencyRateAdapter.apply {
                        submitList(
                            map { keyValue ->
                                Pair(
                                    keyValue.key,
                                    keyValue.value
                                )
                            }
                        )

                        val amounts =
                            if (mActivityMainBinding.edAmount.text?.isNotEmpty() == true) {
                                mActivityMainBinding.edAmount.text.toString().toFloat()
                            } else {
                                1f
                            }

                        delay(100)

                        currentList.singleOrNull { currencyEntity ->
                            currencyEntity.first == (mActivityMainBinding.spinnerCurrency.selectedItem as String)
                        }?.let { rate ->
                            val exchangedList =
                                currentList
                                    .map { historyData ->
                                        val exchangedValue =
                                            historyData.second * amounts / rate.second

                                        Pair(historyData.first, exchangedValue)
                                    }

                            submitList(exchangedList)
                        }
                    }
                }
            }
        }
    }
}