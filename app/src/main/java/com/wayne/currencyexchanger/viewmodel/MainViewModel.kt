package com.wayne.currencyexchanger.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayne.currencyexchanger.repository.APIService
import com.wayne.currencyexchanger.repository.DatabaseManager
import com.wayne.currencyexchanger.repository.entity.CurrencyEntity
import com.wayne.currencyexchanger.repository.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant

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

    val repositoryStatus = APIService.repositoryStatus

    fun retrieveCurrenciesAsync() =
        viewModelScope.launch(Dispatchers.IO) {
            var currencyEntities = DatabaseManager.queryCurrencyEntities()

            if (currencyEntities.isEmpty()) {
                APIService.requestCurrencies()
                currencyEntities = DatabaseManager.queryCurrencyEntities()
            }

            _currencies.tryEmit(currencyEntities)
        }

    fun retrieveHistoryDataAsync() =
        viewModelScope.launch(Dispatchers.IO) {
            var historyData = DatabaseManager.queryHistoryEntity()

            if (historyData == null) {
                APIService.requestLatest()
            } else {
                if (Instant.now().epochSecond - historyData.timestamp.epochSecond > (30 * 60)) {
                    APIService.requestLatest()
                    historyData = DatabaseManager.queryHistoryEntity()
                }

                _historyData.tryEmit(historyData)
            }
        }
}