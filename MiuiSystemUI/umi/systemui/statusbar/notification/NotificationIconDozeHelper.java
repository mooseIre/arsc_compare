package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.C0013R$integer;

public class NotificationIconDozeHelper extends NotificationDozeHelper {
    public void setColor(int i) {
    }

    public NotificationIconDozeHelper(Context context) {
        context.getResources().getInteger(C0013R$integer.doze_small_icon_alpha);
    }
}
