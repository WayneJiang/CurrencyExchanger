package com.wayne.currencyexchanger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wayne.currencyexchanger.repository.DatabaseManager
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {
    private val _currencies =
        MutableSharedFlow<List<CurrencyEntity>>(
            0,
            1,
            BufferOverflow.DROP_LATEST
        )
    val currencies = _currencies

    private val _historyData =
        MutableSharedFlow<HistoryEntity?>(
            0,
            1,
            BufferOverflow.DROP_LATEST
        )
    val historyData = _historyData

    fun retrieveHistoryDataAsync(baseCurrency: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _historyData.tryEmit(DatabaseManager.queryHistoryEntity(baseCurrency))
        }

    fun retrieveCurrenciesAsync() =
        viewModelScope.launch(Dispatchers.IO) {
            _currencies.tryEmit(DatabaseManager.queryCurrencyEntities())
        }
}