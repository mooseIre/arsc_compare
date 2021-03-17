package com.android.systemui.statusbar.phone;

import com.android.systemui.shared.system.ActivityManagerWrapper;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$Zm3Yj0EQnVWvu_ZksQ-OsrTwJ3k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Zm3Yj0EQnVWvu_ZksQOsrTwJ3k implements Function {
    public static final /* synthetic */ $$Lambda$Zm3Yj0EQnVWvu_ZksQOsrTwJ3k INSTANCE = new $$Lambda$Zm3Yj0EQnVWvu_ZksQOsrTwJ3k();

    private /* synthetic */ $$Lambda$Zm3Yj0EQnVWvu_ZksQOsrTwJ3k() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((ActivityManagerWrapper) obj).getRunningTask();
    }
}
