package com.android.systemui.keyguard;

import com.android.systemui.keyguard.ScreenLifecycle;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$n4aPxVrHdTzFo5NE6H_ILivOadQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$n4aPxVrHdTzFo5NE6H_ILivOadQ implements Consumer {
    public static final /* synthetic */ $$Lambda$n4aPxVrHdTzFo5NE6H_ILivOadQ INSTANCE = new $$Lambda$n4aPxVrHdTzFo5NE6H_ILivOadQ();

    private /* synthetic */ $$Lambda$n4aPxVrHdTzFo5NE6H_ILivOadQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ScreenLifecycle.Observer) obj).onScreenTurnedOn();
    }
}
