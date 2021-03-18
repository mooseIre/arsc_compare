package com.android.systemui.util;

import android.media.AudioManager;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: RingerModeTrackerImpl.kt */
public final /* synthetic */ class RingerModeTrackerImpl$ringerMode$1 extends FunctionReference implements Function0<Integer> {
    RingerModeTrackerImpl$ringerMode$1(AudioManager audioManager) {
        super(0, audioManager);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "getRingerMode";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(AudioManager.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "getRingerMode()I";
    }

    /* Return type fixed from 'int' to match base method */
    @Override // kotlin.jvm.functions.Function0
    public final Integer invoke() {
        return ((AudioManager) this.receiver).getRingerMode();
    }
}
