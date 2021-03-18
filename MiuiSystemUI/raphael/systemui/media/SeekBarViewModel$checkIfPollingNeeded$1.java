package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: SeekBarViewModel.kt */
public final /* synthetic */ class SeekBarViewModel$checkIfPollingNeeded$1 extends FunctionReference implements Function0<Unit> {
    SeekBarViewModel$checkIfPollingNeeded$1(SeekBarViewModel seekBarViewModel) {
        super(0, seekBarViewModel);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "checkPlaybackPosition";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(SeekBarViewModel.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "checkPlaybackPosition()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ((SeekBarViewModel) this.receiver).checkPlaybackPosition();
    }
}
