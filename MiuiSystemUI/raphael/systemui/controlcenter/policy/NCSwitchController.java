package com.android.systemui.controlcenter.policy;

import android.content.Context;
import android.view.ViewConfiguration;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.miui.systemui.analytics.SystemUIStat;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NCSwitchController.kt */
public final class NCSwitchController {
    private final Context mContext;

    public NCSwitchController(@NotNull Context context, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull ControlPanelController controlPanelController, @NotNull NotificationShadeWindowController notificationShadeWindowController, @NotNull SystemUIStat systemUIStat) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "mStatusBarStateController");
        Intrinsics.checkParameterIsNotNull(controlPanelController, "mControlPanelController");
        Intrinsics.checkParameterIsNotNull(notificationShadeWindowController, "shadeWindowController");
        Intrinsics.checkParameterIsNotNull(systemUIStat, "systemUIStat");
        this.mContext = context;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(mContext)");
        viewConfiguration.getScaledTouchSlop();
    }
}
