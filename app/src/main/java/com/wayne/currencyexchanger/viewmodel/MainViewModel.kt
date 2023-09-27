package com.wayne.currencyexchanger.viewmodel

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
            val currencyEntities = DatabaseManager.queryCurrencyEntities()

            if (currencyEntities.isEmpty()) {
                APIService.requestCurrencies()
            } else {
                _currencies.tryEmit(currencyEntities)
            }
        }

    fun retrieveHistoryDataAsync(baseCurrency: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val historyData = DatabaseManager.queryHistoryEntity(baseCurrency)

//            if (historyData == null) {
//                APIService.requestLatest(baseCurrency)
//            } else {
//                if (Instant.now().epochSecond - historyData.timestamp.epochSecond > (30 * 60)) {
                    APIService.requestLatest(baseCurrency)
//                }

                _historyData.tryEmit(historyData)
//            }
        }
}