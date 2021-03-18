package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.StatusBarWindowCallback;
import com.miui.systemui.util.CommonUtil;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationShadePolicy.kt */
public final class MiuiNotificationShadePolicy implements StatusBarWindowCallback {
    private final Context context;
    private final ControlPanelController controlPanelController;
    private final Handler handler;
    private final HeadsUpManagerPhone headsUpManagerPhone;
    private boolean mShouldDisableHomeFsg;
    private final NotificationShadeWindowController notificationShadeWindowController;

    public MiuiNotificationShadePolicy(@NotNull Context context2, @NotNull Handler handler2, @NotNull HeadsUpManagerPhone headsUpManagerPhone2, @NotNull NotificationShadeWindowController notificationShadeWindowController2, @NotNull ControlPanelController controlPanelController2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(handler2, "handler");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone2, "headsUpManagerPhone");
        Intrinsics.checkParameterIsNotNull(notificationShadeWindowController2, "notificationShadeWindowController");
        Intrinsics.checkParameterIsNotNull(controlPanelController2, "controlPanelController");
        this.context = context2;
        this.handler = handler2;
        this.headsUpManagerPhone = headsUpManagerPhone2;
        this.notificationShadeWindowController = notificationShadeWindowController2;
        this.controlPanelController = controlPanelController2;
    }

    public final void start() {
        this.notificationShadeWindowController.registerCallback(this);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarWindowCallback
    public void onStateChanged(boolean z, boolean z2, boolean z3) {
        boolean z4 = (this.notificationShadeWindowController.getPanelExpanded() || !this.controlPanelController.isCCFullyCollapsed()) && !this.headsUpManagerPhone.hasPinnedHeadsUp() && !z && CommonUtil.isFullScreenGestureEnabled();
        if (this.mShouldDisableHomeFsg != z4) {
            this.handler.post(new MiuiNotificationShadePolicyKt$sam$java_lang_Runnable$0(new MiuiNotificationShadePolicy$onStateChanged$1(this)));
        }
        this.mShouldDisableHomeFsg = z4;
    }

    /* access modifiers changed from: private */
    public final void handleUpdateFsgState() {
        CommonUtil.updateFsgState(this.context, "typefrom_status_bar_expansion", this.mShouldDisableHomeFsg);
    }

    public final void notifyFsgChanged(boolean z) {
        this.mShouldDisableHomeFsg = z;
    }
}
