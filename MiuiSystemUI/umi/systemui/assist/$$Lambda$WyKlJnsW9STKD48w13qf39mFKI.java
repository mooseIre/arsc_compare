package com.android.systemui.assist;

import android.os.SystemClock;
import androidx.slice.Clock;

/* renamed from: com.android.systemui.assist.-$$Lambda$WyKlJnsW9STKD48w13qf39m-FKI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WyKlJnsW9STKD48w13qf39mFKI implements Clock {
    public static final /* synthetic */ $$Lambda$WyKlJnsW9STKD48w13qf39mFKI INSTANCE = new $$Lambda$WyKlJnsW9STKD48w13qf39mFKI();

    private /* synthetic */ $$Lambda$WyKlJnsW9STKD48w13qf39mFKI() {
    }

    @Override // androidx.slice.Clock
    public final long currentTimeMillis() {
        return SystemClock.uptimeMillis();
    }
}
