package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import com.android.systemui.plugins.R;

public class NotificationIconDozeHelper extends NotificationDozeHelper {
    public void setColor(int i) {
    }

    public NotificationIconDozeHelper(Context context) {
        new PorterDuffColorFilter(0, PorterDuff.Mode.SRC_ATOP);
        context.getResources().getInteger(R.integer.doze_small_icon_alpha);
    }
}
