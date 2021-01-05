package com.android.keyguard.fod.policy;

import android.view.ViewTreeObserver;
import com.android.keyguard.fod.MiuiGxzwManager;

/* compiled from: MiuiGxzwPolicy.kt */
final class MiuiGxzwPolicy$start$1 implements ViewTreeObserver.OnWindowFocusChangeListener {
    public static final MiuiGxzwPolicy$start$1 INSTANCE = new MiuiGxzwPolicy$start$1();

    MiuiGxzwPolicy$start$1() {
    }

    public final void onWindowFocusChanged(boolean z) {
        MiuiGxzwManager.getInstance().updateGxzwState();
    }
}
