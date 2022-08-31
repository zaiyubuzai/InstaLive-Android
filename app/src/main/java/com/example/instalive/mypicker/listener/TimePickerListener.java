package com.example.instalive.mypicker.listener;

import android.view.View;

import java.util.Date;

public interface TimePickerListener {

    void onTimeChanged(Date date);
    void onTimeConfirm(Date date, View view);
}