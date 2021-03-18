package com.android.keyguard.injector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardViewMediatorInjector.kt */
public final class KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ KeyguardViewMediatorInjector this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1(KeyguardViewMediatorInjector keyguardViewMediatorInjector) {
        this.this$0 = keyguardViewMediatorInjector;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (Intrinsics.areEqual("xiaomi.intent.action.SHOW_SECURE_KEYGUARD", intent.getAction()) && this.this$0.getMStatusBarKeyguardViewManager() != null && this.this$0.getMStatusBarKeyguardViewManager().isShowing()) {
            new Handler(Looper.getMainLooper()).post(new KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1$onReceive$1(this));
        }
    }
}
