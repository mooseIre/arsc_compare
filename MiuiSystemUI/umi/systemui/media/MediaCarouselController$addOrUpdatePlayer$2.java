package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: MediaCarouselController.kt */
final /* synthetic */ class MediaCarouselController$addOrUpdatePlayer$2 extends FunctionReference implements Function0<Unit> {
    MediaCarouselController$addOrUpdatePlayer$2(MediaCarouselController mediaCarouselController) {
        super(0, mediaCarouselController);
    }

    public final String getName() {
        return "updateCarouselDimensions";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
    }

    public final String getSignature() {
        return "updateCarouselDimensions()V";
    }

    public final void invoke() {
        ((MediaCarouselController) this.receiver).updateCarouselDimensions();
    }
}
