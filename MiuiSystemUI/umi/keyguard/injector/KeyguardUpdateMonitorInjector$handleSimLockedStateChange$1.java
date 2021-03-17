package com.android.keyguard.injector;

import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardUpdateMonitorInjector.kt */
final class KeyguardUpdateMonitorInjector$handleSimLockedStateChange$1 extends Lambda implements Function1<MiuiKeyguardUpdateMonitorCallback, Unit> {
    final /* synthetic */ boolean $isSimLocked;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    KeyguardUpdateMonitorInjector$handleSimLockedStateChange$1(boolean z) {
        super(1);
        this.$isSimLocked = z;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke((MiuiKeyguardUpdateMonitorCallback) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback) {
        Intrinsics.checkParameterIsNotNull(miuiKeyguardUpdateMonitorCallback, "callback");
        miuiKeyguardUpdateMonitorCallback.onSimLockedStateChanged(this.$isSimLocked);
    }
}
