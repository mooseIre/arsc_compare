package com.android.systemui.plugins;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.View;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.TimeZone;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_CLOCK", version = 5)
public interface ClockPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_CLOCK";
    public static final int VERSION = 5;

    View getBigClockView() {
        return null;
    }

    String getName();

    int getPreferredY(int i);

    Bitmap getPreview(int i, int i2);

    Bitmap getThumbnail();

    String getTitle();

    View getView();

    void onDestroyView();

    void onTimeTick() {
    }

    void onTimeZoneChanged(TimeZone timeZone) {
    }

    void setColorPalette(boolean z, int[] iArr) {
    }

    void setDarkAmount(float f) {
    }

    void setStyle(Paint.Style style);

    void setTextColor(int i);

    boolean shouldShowStatusArea() {
        return true;
    }
}
