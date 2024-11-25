package com.example.myapplication2.tg;

import android.util.Log;

/**
 * @author a.s.korchagin
 */
public class FileLog {

    public static void e(final String message, final Throwable exception) {
        Log.e("test____FileLog", message, exception);
    }

    public static void e(final String message) {
        Log.e("test____FileLog", message);
    }

    public static void e(final Throwable e) {
        Log.e("test____FileLog", e.getMessage(), e);
    }
}
