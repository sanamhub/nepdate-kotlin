package io.github.sanamhub.nepdate.sample.size;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import io.github.sanamhub.nepdate.Lang;
import io.github.sanamhub.nepdate.NepaliDate;

/** The core flavor: conversion and formatting, so R8 keeps the nepdate core. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NepaliDate date = NepaliDate.ofGregorian(2024, 7, 30);
        TextView view = new TextView(this);
        view.setText(date.toLongString(false, true, true, Lang.NEPALI));
        setContentView(view);
    }
}
