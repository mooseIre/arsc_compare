package com.android.systemui.plugins;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.View;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.TimeZone;

@ProvidesInterface(action = ClockPlugin.ACTION, version = 5)
public interface ClockPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_CLOCK";
    public static final int VERSION = 5;

    default View getBigClockView() {
        return null;
    }

    String getName();

    int getPreferredY(int i);

    Bitmap getPreview(int i, int i2);

    Bitmap getThumbnail();

    String getTitle();

    View getView();

    void onDestroyView();

    default void onTimeTick() {
    }

    default void onTimeZoneChanged(TimeZone timeZone) {
    }

    default void setColorPalette(boolean z, int[] iArr) {
    }

    default void setDarkAmount(float f) {
    }

    void setStyle(Paint.Style style);

    void setTextColor(int i);

    default boolean shouldShowStatusArea() {
        return true;
    }
}
