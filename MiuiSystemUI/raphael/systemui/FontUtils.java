package com.android.systemui;

import android.view.View;
import android.widget.TextView;

public class FontUtils {
    public static void updateFontSize(View view, int i, int i2) {
        View findViewById = view.findViewById(i);
        if (findViewById != null && (findViewById instanceof TextView)) {
            updateFontSize((TextView) findViewById, i2);
        }
    }

    public static void updateFontSize(TextView textView, int i) {
        textView.setTextSize(0, (float) textView.getResources().getDimensionPixelSize(i));
    }
}
