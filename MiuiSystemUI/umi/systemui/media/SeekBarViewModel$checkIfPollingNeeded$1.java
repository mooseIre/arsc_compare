package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: SeekBarViewModel.kt */
final /* synthetic */ class SeekBarViewModel$checkIfPollingNeeded$1 extends FunctionReference implements Function0<Unit> {
    SeekBarViewModel$checkIfPollingNeeded$1(SeekBarViewModel seekBarViewModel) {
        super(0, seekBarViewModel);
    }

    public final String getName() {
        return "checkPlaybackPosition";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(SeekBarViewModel.class);
    }

    public final String getSignature() {
        return "checkPlaybackPosition()V";
    }

    public final void invoke() {
        ((SeekBarViewModel) this.receiver).checkPlaybackPosition();
    }
}
