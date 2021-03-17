package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: KeyguardNegative1PageInjector.kt */
public final class KeyguardNegative1PageInjector$onFinishInflate$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ KeyguardNegative1PageInjector this$0;

    KeyguardNegative1PageInjector$onFinishInflate$1(KeyguardNegative1PageInjector keyguardNegative1PageInjector) {
        this.this$0 = keyguardNegative1PageInjector;
    }

    public void onUserUnlocked() {
        MiuiKeyguardMoveLeftViewContainer access$getMKeyguardLeftView$p = this.this$0.mKeyguardLeftView;
        if (access$getMKeyguardLeftView$p != null) {
            access$getMKeyguardLeftView$p.setCustomBackground();
            MiuiKeyguardMoveLeftViewContainer access$getMKeyguardLeftView$p2 = this.this$0.mKeyguardLeftView;
            if (access$getMKeyguardLeftView$p2 != null) {
                access$getMKeyguardLeftView$p2.initLeftView();
                MiuiKeyguardMoveLeftViewContainer access$getMKeyguardLeftView$p3 = this.this$0.mKeyguardLeftView;
                if (access$getMKeyguardLeftView$p3 != null) {
                    access$getMKeyguardLeftView$p3.uploadData();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
