package com.android.keyguard.fod;

import com.android.systemui.Dependency;
import com.miui.systemui.util.HapticFeedBackImpl;

/* renamed from: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$pU93HFtcJ915rU4l2vSbn9uDT5U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$pU93HFtcJ915rU4l2vSbn9uDT5U implements Runnable {
    public static final /* synthetic */ $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$pU93HFtcJ915rU4l2vSbn9uDT5U INSTANCE = new $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$pU93HFtcJ915rU4l2vSbn9uDT5U();

    private /* synthetic */ $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$pU93HFtcJ915rU4l2vSbn9uDT5U() {
    }

    public final void run() {
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
    }
}
