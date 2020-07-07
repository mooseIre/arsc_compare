package com.android.keyguard.fod;

import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;

/* renamed from: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE implements Runnable {
    public static final /* synthetic */ $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE INSTANCE = new $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE();

    private /* synthetic */ $$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE() {
    }

    public final void run() {
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
    }
}
