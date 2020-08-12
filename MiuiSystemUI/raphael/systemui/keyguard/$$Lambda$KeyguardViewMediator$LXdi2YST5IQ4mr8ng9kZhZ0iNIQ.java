package com.android.systemui.keyguard;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.xiaomi.stat.d.i;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$LXdi2YST5IQ4mr8ng9kZhZ0iNIQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KeyguardViewMediator$LXdi2YST5IQ4mr8ng9kZhZ0iNIQ implements Runnable {
    public static final /* synthetic */ $$Lambda$KeyguardViewMediator$LXdi2YST5IQ4mr8ng9kZhZ0iNIQ INSTANCE = new $$Lambda$KeyguardViewMediator$LXdi2YST5IQ4mr8ng9kZhZ0iNIQ();

    private /* synthetic */ $$Lambda$KeyguardViewMediator$LXdi2YST5IQ4mr8ng9kZhZ0iNIQ() {
    }

    public final void run() {
        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setUserActivityTime(i.a);
    }
}
