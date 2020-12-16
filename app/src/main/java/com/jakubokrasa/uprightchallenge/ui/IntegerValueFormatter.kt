package com.jakubokrasa.uprightchallenge.ui

import com.github.mikephil.charting.formatter.ValueFormatter


class IntegerValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float) = "" + value.toInt()
}