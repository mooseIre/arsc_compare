package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.statusbar.notification.stack.MediaHeaderView;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import java.util.List;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$onStartedWakingUp$animateShadeViews$1 extends Lambda implements Function1<View, Boolean> {
    final /* synthetic */ List $visibleNotifications;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$onStartedWakingUp$animateShadeViews$1(List list) {
        super(1);
        this.$visibleNotifications = list;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Boolean.valueOf(invoke((View) obj));
    }

    public final boolean invoke(View view) {
        return (view instanceof MediaHeaderView) || (view instanceof ZenModeView) || CollectionsKt___CollectionsKt.contains(this.$visibleNotifications, view);
    }
}
