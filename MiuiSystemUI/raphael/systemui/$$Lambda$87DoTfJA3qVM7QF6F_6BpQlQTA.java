package com.android.systemui;

import android.os.SystemClock;
import java.util.function.Supplier;

/* renamed from: com.android.systemui.-$$Lambda$87Do-TfJA3qVM7QF6F_6BpQlQTA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA implements Supplier {
    public static final /* synthetic */ $$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA INSTANCE = new $$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA();

    private /* synthetic */ $$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA() {
    }

    @Override // java.util.function.Supplier
    public final Object get() {
        return Long.valueOf(SystemClock.elapsedRealtime());
    }
}
