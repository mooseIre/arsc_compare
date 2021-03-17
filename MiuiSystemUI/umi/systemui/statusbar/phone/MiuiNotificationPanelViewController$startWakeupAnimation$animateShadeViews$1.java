package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.statusbar.notification.stack.MediaHeaderView;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import java.util.List;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$1 extends Lambda implements Function1<View, Boolean> {
    final /* synthetic */ List $visibleNotifications;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$1(List list) {
        super(1);
        this.$visibleNotifications = list;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(View view) {
        return Boolean.valueOf(invoke(view));
    }

    public final boolean invoke(View view) {
        return (view instanceof MediaHeaderView) || (view instanceof ZenModeView) || (CollectionsKt___CollectionsKt.contains(this.$visibleNotifications, view));
    }
}
