package com.android.systemui.keyguard;

import com.android.systemui.keyguard.ScreenLifecycle;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$w9PiqN50NESCg48fJRhE_dJBSdc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$w9PiqN50NESCg48fJRhE_dJBSdc implements Consumer {
    public static final /* synthetic */ $$Lambda$w9PiqN50NESCg48fJRhE_dJBSdc INSTANCE = new $$Lambda$w9PiqN50NESCg48fJRhE_dJBSdc();

    private /* synthetic */ $$Lambda$w9PiqN50NESCg48fJRhE_dJBSdc() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ScreenLifecycle.Observer) obj).onScreenTurningOn();
    }
}
