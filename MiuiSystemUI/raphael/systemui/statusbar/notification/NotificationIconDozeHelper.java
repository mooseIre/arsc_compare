package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import com.android.systemui.plugins.R;

public class NotificationIconDozeHelper extends NotificationDozeHelper {
    private int mColor = -16777216;
    private PorterDuffColorFilter mImageColorFilter = new PorterDuffColorFilter(0, PorterDuff.Mode.SRC_ATOP);
    private final int mImageDarkAlpha;
    private final int mImageDarkColor = -1;

    public NotificationIconDozeHelper(Context context) {
        this.mImageDarkAlpha = context.getResources().getInteger(R.integer.doze_small_icon_alpha);
    }

    public void setColor(int i) {
        this.mColor = i;
    }
}
