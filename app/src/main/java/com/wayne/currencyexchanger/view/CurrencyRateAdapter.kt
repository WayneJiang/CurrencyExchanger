package com.wayne.currencyexchanger.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wayne.currencyexchanger.databinding.ViewCurrencyRateBinding
import java.math.BigDecimal
import java.math.RoundingMode

/*
 * Copyright (c) 2023 GoMore Inc. All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
class CurrencyRateAdapter :
    ListAdapter<Pair<String, Float>, CurrencyRateAdapter.CurrencyRateViewHolder>(
        CurrencyRateDiff()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRateViewHolder {
        return CurrencyRateViewHolder(
            ViewCurrencyRateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CurrencyRateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CurrencyRateViewHolder(private val viewCurrencyRateBinding: ViewCurrencyRateBinding) :
        RecyclerView.ViewHolder(viewCurrencyRateBinding.root) {

        fun bind(currencyPair: Pair<String, Float>) = with(viewCurrencyRateBinding) {
            tvCurrency.text = currencyPair.first

            val rate =
                BigDecimal(currencyPair.second.toDouble())
                    .setScale(1, RoundingMode.HALF_UP)
                    .toDouble()

            tvRate.text = "$rate"
        }
    }

    class CurrencyRateDiff : DiffUtil.ItemCallback<Pair<String, Float>>() {
        override fun areItemsTheSame(
            oldItem: Pair<String, Float>,
            newItem: Pair<String, Float>
        ) =
            (oldItem == newItem)

        override fun areContentsTheSame(
            oldItem: Pair<String, Float>,
            newItem: Pair<String, Float>
        ) =
            (oldItem.first == newItem.first) and
                    (oldItem.second == newItem.second)
    }
}