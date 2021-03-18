package com.android.systemui.media;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: MediaCarouselController.kt */
public final /* synthetic */ class MediaCarouselController$addOrUpdatePlayer$2 extends FunctionReference implements Function0<Unit> {
    MediaCarouselController$addOrUpdatePlayer$2(MediaCarouselController mediaCarouselController) {
        super(0, mediaCarouselController);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "updateCarouselDimensions";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "updateCarouselDimensions()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ((MediaCarouselController) this.receiver).updateCarouselDimensions();
    }
}
