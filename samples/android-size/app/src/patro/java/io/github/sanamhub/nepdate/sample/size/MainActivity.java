package io.github.sanamhub.nepdate.sample.size;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import io.github.sanamhub.nepdate.Lang;
import io.github.sanamhub.nepdate.NepaliDate;
import io.github.sanamhub.nepdate.patro.CalendarInfo;
import io.github.sanamhub.nepdate.patro.Patro;

/** The patro flavor: conversion, formatting and a patro lookup, so R8 keeps both artifacts. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NepaliDate date = NepaliDate.ofGregorian(2024, 7, 30);
        CalendarInfo info = Patro.info(date);
        String tithi = info.getTithi() == null ? "" : info.getTithi().getNepali();
        TextView view = new TextView(this);
        view.setText(date.toLongString(false, true, true, Lang.NEPALI) + " " + tithi);
        setContentView(view);
    }
}
