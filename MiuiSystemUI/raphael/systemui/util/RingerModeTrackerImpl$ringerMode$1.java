package com.android.systemui.util;

import android.media.AudioManager;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: RingerModeTrackerImpl.kt */
final /* synthetic */ class RingerModeTrackerImpl$ringerMode$1 extends FunctionReference implements Function0<Integer> {
    RingerModeTrackerImpl$ringerMode$1(AudioManager audioManager) {
        super(0, audioManager);
    }

    public final String getName() {
        return "getRingerMode";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(AudioManager.class);
    }

    public final String getSignature() {
        return "getRingerMode()I";
    }

    public final int invoke() {
        return ((AudioManager) this.receiver).getRingerMode();
    }
}
