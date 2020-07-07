package com.android.systemui.miui.volume;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;

class Util {
    public static boolean DEBUG = Log.isLoggable("volume", 3);

    public static float constrain(float f, float f2, float f3) {
        return f < f2 ? f2 : f > f3 ? f3 : f;
    }

    public static int constrain(int i, int i2, int i3) {
        return i < i2 ? i2 : i > i3 ? i3 : i;
    }

    public static String logTag(Class<?> cls) {
        String str = "vol." + cls.getSimpleName();
        return str.length() < 23 ? str : str.substring(0, 23);
    }

    public static final void setVisOrGone(View view, boolean z) {
        if (view != null) {
            int i = 0;
            if (view.getVisibility() != (z ? 0 : 8)) {
                if (!z) {
                    i = 8;
                }
                view.setVisibility(i);
            }
        }
    }

    public static final void setVisOrInvis(View view, boolean z) {
        if (view != null) {
            int i = 0;
            if (view.getVisibility() != (z ? 0 : 4)) {
                if (!z) {
                    i = 4;
                }
                view.setVisibility(i);
            }
        }
    }

    public static final void setVisOrInvis(Drawable drawable, boolean z) {
        drawable.setAlpha(z ? 255 : 0);
    }

    public static void reparentChildren(ViewGroup viewGroup, ViewGroup viewGroup2) {
        ArrayList<View> arrayList = new ArrayList<>();
        HashMap hashMap = new HashMap();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            arrayList.add(childAt);
            hashMap.put(childAt, childAt.getLayoutParams());
        }
        for (View view : arrayList) {
            ((ViewGroup) view.getParent()).removeView(view);
            viewGroup2.addView(view, (ViewGroup.LayoutParams) hashMap.get(view));
        }
    }

    public static void setLastTotalCountDownTime(Context context, int i) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("miui_last_count_down_time", i).apply();
    }

    public static int getLastTotalCountDownTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("miui_last_count_down_time", 0);
    }
}
