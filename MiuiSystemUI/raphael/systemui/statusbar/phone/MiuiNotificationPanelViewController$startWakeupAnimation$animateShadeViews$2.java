package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2 extends Lambda implements Function1<View, Boolean> {
    public static final MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2 INSTANCE = new MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2();

    MiuiNotificationPanelViewController$startWakeupAnimation$animateShadeViews$2() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(View view) {
        return Boolean.valueOf(invoke(view));
    }

    public final boolean invoke(View view) {
        ExpandableViewState viewState;
        return (view instanceof ExpandableView) && (viewState = ((ExpandableView) view).getViewState()) != null && !viewState.hidden;
    }
}
