package com.wayne.currencyexchanger.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.android.material.textview.MaterialTextView
import com.wayne.currencyexchanger.databinding.ViewItemDropdownBinding

/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/25
 */
class CurrencySymbolAdapter(private val context: Context, private val symbols: List<String>) :
    BaseAdapter() {

    override fun getCount() = symbols.size

    override fun getItem(p0: Int) = symbols[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view: View
        val itemViewHolder: ItemViewHolder

        if (p1 == null) {
            val viewBinding = ViewItemDropdownBinding.inflate(LayoutInflater.from(context))

            itemViewHolder = ItemViewHolder()
            itemViewHolder.materialTextView = viewBinding.tvItem

            viewBinding.root.tag = itemViewHolder

            view = viewBinding.root
        } else {
            view = p1
            itemViewHolder = p1.tag as ItemViewHolder
        }

        itemViewHolder.materialTextView.text = symbols[p0]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent)
    }

    private class ItemViewHolder {
        lateinit var materialTextView: MaterialTextView
    }
}