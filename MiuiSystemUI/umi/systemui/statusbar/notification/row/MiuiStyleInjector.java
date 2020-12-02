package com.android.systemui.statusbar.notification.row;

import com.android.systemui.C0022R$style;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;

/* compiled from: MiuiStyleInjector.kt */
public final class MiuiStyleInjector {
    public static final MiuiStyleInjector INSTANCE = new MiuiStyleInjector();

    private MiuiStyleInjector() {
    }

    public final int getHybridNotificationStyle() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return C0022R$style.HybridNotification;
        }
        return C0022R$style.HybridNotificationMiui;
    }
}
