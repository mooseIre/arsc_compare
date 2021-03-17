package com.android.systemui.keyguard;

import com.android.systemui.keyguard.WakefulnessLifecycle;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$TPhVA13qrDBGFKbgQpRNBPBvAqI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TPhVA13qrDBGFKbgQpRNBPBvAqI implements Consumer {
    public static final /* synthetic */ $$Lambda$TPhVA13qrDBGFKbgQpRNBPBvAqI INSTANCE = new $$Lambda$TPhVA13qrDBGFKbgQpRNBPBvAqI();

    private /* synthetic */ $$Lambda$TPhVA13qrDBGFKbgQpRNBPBvAqI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WakefulnessLifecycle.Observer) obj).onStartedWakingUp();
    }
}
