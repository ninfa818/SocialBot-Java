package com.hostcart.socialbot.utils;

import android.app.Activity;

import com.hostcart.socialbot.R;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtil {
    public static void showDoesNotFireAppSnackbar(Activity context) {
        String text = String.format(context.getString(R.string.does_not_have_fireapp), context.getString(R.string.app_name));
        Snackbar.make(context.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
    }
}
