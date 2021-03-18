package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: KeyguardNegative1PageInjector.kt */
public final class KeyguardNegative1PageInjector$onFinishInflate$1 extends MiuiKeyguardUpdateMonitorCallback {
    final /* synthetic */ KeyguardNegative1PageInjector this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    KeyguardNegative1PageInjector$onFinishInflate$1(KeyguardNegative1PageInjector keyguardNegative1PageInjector) {
        this.this$0 = keyguardNegative1PageInjector;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onUserUnlocked() {
        MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.this$0.mKeyguardLeftView;
        if (miuiKeyguardMoveLeftViewContainer != null) {
            miuiKeyguardMoveLeftViewContainer.setCustomBackground();
            MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer2 = this.this$0.mKeyguardLeftView;
            if (miuiKeyguardMoveLeftViewContainer2 != null) {
                miuiKeyguardMoveLeftViewContainer2.initLeftView();
                MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer3 = this.this$0.mKeyguardLeftView;
                if (miuiKeyguardMoveLeftViewContainer3 != null) {
                    miuiKeyguardMoveLeftViewContainer3.uploadData();
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
