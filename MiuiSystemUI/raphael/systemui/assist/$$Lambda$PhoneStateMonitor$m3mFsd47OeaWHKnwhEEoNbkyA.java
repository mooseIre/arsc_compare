package com.android.systemui.assist;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.function.Function;

/* renamed from: com.android.systemui.assist.-$$Lambda$PhoneStateMonitor$m-3mFsd47OeaWHKnwhE-EoNbkyA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneStateMonitor$m3mFsd47OeaWHKnwhEEoNbkyA implements Function {
    public static final /* synthetic */ $$Lambda$PhoneStateMonitor$m3mFsd47OeaWHKnwhEEoNbkyA INSTANCE = new $$Lambda$PhoneStateMonitor$m3mFsd47OeaWHKnwhEEoNbkyA();

    private /* synthetic */ $$Lambda$PhoneStateMonitor$m3mFsd47OeaWHKnwhEEoNbkyA() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Boolean.valueOf(((StatusBar) ((Lazy) obj).get()).isBouncerShowing());
    }
}
