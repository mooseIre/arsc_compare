package com.android.systemui;

import android.content.Context;
import android.widget.Toast;

public class SysUIToast {
    public static Toast makeText(Context context, int i, int i2) {
        return makeText(context, (CharSequence) context.getString(i), i2);
    }

    public static Toast makeText(Context context, CharSequence charSequence, int i) {
        return Toast.makeText(context, charSequence, i);
    }
}
