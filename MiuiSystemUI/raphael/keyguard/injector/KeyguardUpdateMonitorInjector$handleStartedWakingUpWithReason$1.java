package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardUpdateMonitorInjector.kt */
final class KeyguardUpdateMonitorInjector$handleStartedWakingUpWithReason$1 extends Lambda implements Function1<MiuiKeyguardUpdateMonitorCallback, Unit> {
    final /* synthetic */ String $reason;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    KeyguardUpdateMonitorInjector$handleStartedWakingUpWithReason$1(String str) {
        super(1);
        this.$reason = str;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke((MiuiKeyguardUpdateMonitorCallback) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback) {
        Intrinsics.checkParameterIsNotNull(miuiKeyguardUpdateMonitorCallback, "callback");
        miuiKeyguardUpdateMonitorCallback.onStartedWakingUpWithReason(this.$reason);
    }
}
