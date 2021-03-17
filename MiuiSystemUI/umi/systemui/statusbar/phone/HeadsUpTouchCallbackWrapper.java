package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.phone.HeadsUpTouchHelper;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class HeadsUpTouchCallbackWrapper implements HeadsUpTouchHelper.Callback {
    private final HeadsUpTouchHelper.Callback base;
    private final HeadsUpManagerPhone headsUpManagerPhone;
    private final MiuiNotificationPanelViewController panelView;

    public HeadsUpTouchCallbackWrapper(@NotNull MiuiNotificationPanelViewController miuiNotificationPanelViewController, @NotNull HeadsUpManagerPhone headsUpManagerPhone2, @NotNull HeadsUpTouchHelper.Callback callback) {
        Intrinsics.checkParameterIsNotNull(miuiNotificationPanelViewController, "panelView");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone2, "headsUpManagerPhone");
        Intrinsics.checkParameterIsNotNull(callback, "base");
        this.panelView = miuiNotificationPanelViewController;
        this.headsUpManagerPhone = headsUpManagerPhone2;
        this.base = callback;
    }

    @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
    public boolean isExpanded() {
        return this.base.isExpanded() && !(this.headsUpManagerPhone.hasPinnedHeadsUp() && this.panelView.isExpectingSynthesizedDown$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core());
    }

    @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
    @Nullable
    public ExpandableView getChildAtRawPosition(float f, float f2) {
        return this.base.getChildAtRawPosition(f, f2);
    }

    @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
    @NotNull
    public Context getContext() {
        Context context = this.base.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "base.context");
        return context;
    }
}
