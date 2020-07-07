package com.android.systemui.assist.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;

public class DisplayUtils {
    public static int convertDpToPx(float f, Context context) {
        Display display = context.getDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        return (int) Math.ceil((double) (f * displayMetrics.density));
    }

    public static int getWidth(Context context) {
        Display display = context.getDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int rotation = display.getRotation();
        if (rotation == 0 || rotation == 2) {
            return displayMetrics.widthPixels;
        }
        return displayMetrics.heightPixels;
    }

    public static int getHeight(Context context) {
        Display display = context.getDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int rotation = display.getRotation();
        if (rotation == 0 || rotation == 2) {
            return displayMetrics.heightPixels;
        }
        return displayMetrics.widthPixels;
    }

    public static int getCornerRadiusBottom(Context context) {
        int identifier = context.getResources().getIdentifier("rounded_corner_radius_bottom", "dimen", "android");
        int dimensionPixelSize = identifier > 0 ? context.getResources().getDimensionPixelSize(identifier) : 0;
        return dimensionPixelSize == 0 ? getCornerRadiusDefault(context) : dimensionPixelSize;
    }

    public static int getCornerRadiusTop(Context context) {
        int identifier = context.getResources().getIdentifier("rounded_corner_radius_top", "dimen", "android");
        int dimensionPixelSize = identifier > 0 ? context.getResources().getDimensionPixelSize(identifier) : 0;
        return dimensionPixelSize == 0 ? getCornerRadiusDefault(context) : dimensionPixelSize;
    }

    private static int getCornerRadiusDefault(Context context) {
        int identifier = context.getResources().getIdentifier("rounded_corner_radius", "dimen", "android");
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }
}
