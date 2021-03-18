package com.android.systemui.keyguard;

import com.android.systemui.keyguard.WakefulnessLifecycle;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$ASgSeR7gTZT1Q2JGNWCU20EppLY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ASgSeR7gTZT1Q2JGNWCU20EppLY implements Consumer {
    public static final /* synthetic */ $$Lambda$ASgSeR7gTZT1Q2JGNWCU20EppLY INSTANCE = new $$Lambda$ASgSeR7gTZT1Q2JGNWCU20EppLY();

    private /* synthetic */ $$Lambda$ASgSeR7gTZT1Q2JGNWCU20EppLY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WakefulnessLifecycle.Observer) obj).onStartedGoingToSleep();
    }
}
