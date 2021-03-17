package com.android.systemui.keyguard;

import com.android.systemui.keyguard.ScreenLifecycle;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$DmSZzOb4vxXoGU7unAMsJYIcFwE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DmSZzOb4vxXoGU7unAMsJYIcFwE implements Consumer {
    public static final /* synthetic */ $$Lambda$DmSZzOb4vxXoGU7unAMsJYIcFwE INSTANCE = new $$Lambda$DmSZzOb4vxXoGU7unAMsJYIcFwE();

    private /* synthetic */ $$Lambda$DmSZzOb4vxXoGU7unAMsJYIcFwE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ScreenLifecycle.Observer) obj).onScreenTurningOff();
    }
}
