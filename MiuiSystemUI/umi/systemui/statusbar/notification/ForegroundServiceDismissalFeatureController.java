package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.util.DeviceConfigProxy;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ForegroundServiceDismissalFeatureController.kt */
public final class ForegroundServiceDismissalFeatureController {
    @NotNull
    private final DeviceConfigProxy proxy;

    public ForegroundServiceDismissalFeatureController(@NotNull DeviceConfigProxy deviceConfigProxy, @NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(deviceConfigProxy, "proxy");
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.proxy = deviceConfigProxy;
    }

    public final boolean isForegroundServiceDismissalEnabled() {
        return ForegroundServiceDismissalFeatureControllerKt.access$isEnabled(this.proxy);
    }
}
