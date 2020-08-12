package com.android.systemui.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.SmoothRoundDrawable;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Utils {
    public static int argb(float f, float f2, float f3, float f4) {
        return (((int) ((f * 255.0f) + 0.5f)) << 24) | (((int) ((f2 * 255.0f) + 0.5f)) << 16) | (((int) ((f3 * 255.0f) + 0.5f)) << 8) | ((int) ((f4 * 255.0f) + 0.5f));
    }

    public static <T> void safeForeach(List<T> list, Consumer<T> consumer) {
        ArrayList arrayList = new ArrayList(list);
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            Object obj = arrayList.get(size);
            if (obj != null) {
                consumer.accept(obj);
            }
        }
    }

    public static void updateFsgState(Context context, String str, boolean z) {
        Intent intent = new Intent();
        intent.setAction("com.android.systemui.fsgesture");
        intent.putExtra("typeFrom", str);
        intent.putExtra("isEnter", z);
        intent.addFlags(67108864);
        context.sendBroadcast(intent);
    }

    public static int getColorAttr(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int color = obtainStyledAttributes.getColor(0, 0);
        obtainStyledAttributes.recycle();
        return color;
    }

    public static int getColorAccent(Context context) {
        return getColorAttr(context, 16843829);
    }

    public static int getColorError(Context context) {
        return context.getColor(R.color.color_error);
    }

    public static int getDefaultColor(Context context, int i) {
        return context.getResources().getColorStateList(i, context.getTheme()).getDefaultColor();
    }

    public static <T> T[] arrayConcat(T[] tArr, T[] tArr2) {
        if (tArr == null) {
            return tArr2;
        }
        if (tArr2 == null) {
            return tArr;
        }
        T[] copyOf = Arrays.copyOf(tArr, tArr.length + tArr2.length);
        System.arraycopy(tArr2, 0, copyOf, tArr.length, tArr2.length);
        return copyOf;
    }

    public static String getCalendarPkg(Context context) {
        if (!Constants.IS_INTERNATIONAL) {
            return "com.android.calendar";
        }
        if (Util.isAppInstalledForUser(context, "com.xiaomi.calendar", KeyguardUpdateMonitor.getCurrentUser())) {
            return "com.xiaomi.calendar";
        }
        if (Util.isAppInstalledForUser(context, "com.android.calendar", KeyguardUpdateMonitor.getCurrentUser())) {
            return "com.android.calendar";
        }
        return "com.google.android.calendar";
    }

    public static Drawable getSmoothRoundDrawable(Context context, int i) {
        if (context == null || i <= 0) {
            return null;
        }
        if (!(context.getDrawable(i) instanceof SmoothRoundDrawable)) {
            return context.getDrawable(i);
        }
        try {
            Resources resources = context.getResources();
            return Drawable.createFromXml(resources, resources.getLayout(i));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
