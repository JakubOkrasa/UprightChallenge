package com.example.uprightchallenge.ui;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class IntegerValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return "" + ((int) value);
    }
}
