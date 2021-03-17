package com.android.keyguard.clock;

import com.android.systemui.Dependency;
import java.util.function.Supplier;

/* renamed from: com.android.keyguard.clock.-$$Lambda$ClockOptionsProvider$VCF-r6VBqrtOSuPKYuOzo6kUuyg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ClockOptionsProvider$VCFr6VBqrtOSuPKYuOzo6kUuyg implements Supplier {
    public static final /* synthetic */ $$Lambda$ClockOptionsProvider$VCFr6VBqrtOSuPKYuOzo6kUuyg INSTANCE = new $$Lambda$ClockOptionsProvider$VCFr6VBqrtOSuPKYuOzo6kUuyg();

    private /* synthetic */ $$Lambda$ClockOptionsProvider$VCFr6VBqrtOSuPKYuOzo6kUuyg() {
    }

    @Override // java.util.function.Supplier
    public final Object get() {
        return ((ClockManager) Dependency.get(ClockManager.class)).getClockInfos();
    }
}
