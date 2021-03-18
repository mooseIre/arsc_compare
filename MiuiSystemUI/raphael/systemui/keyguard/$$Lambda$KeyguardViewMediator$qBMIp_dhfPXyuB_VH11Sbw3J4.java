package com.android.systemui.keyguard;

import com.android.systemui.Dependency;
import com.miui.systemui.util.HapticFeedBackImpl;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$qBMIp_d-hf-PXyuB_VH11Sbw3J4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KeyguardViewMediator$qBMIp_dhfPXyuB_VH11Sbw3J4 implements Runnable {
    public static final /* synthetic */ $$Lambda$KeyguardViewMediator$qBMIp_dhfPXyuB_VH11Sbw3J4 INSTANCE = new $$Lambda$KeyguardViewMediator$qBMIp_dhfPXyuB_VH11Sbw3J4();

    private /* synthetic */ $$Lambda$KeyguardViewMediator$qBMIp_dhfPXyuB_VH11Sbw3J4() {
    }

    public final void run() {
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extLongHapticFeedback(169, false, 0);
    }
}
