package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.systemui.media.MediaCarouselScrollHandler;
import com.android.systemui.media.MediaScrollView;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.util.concurrency.DelayableExecutor;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiMediaCarouseScrollHandler.kt */
public final class MiuiMediaCarouselScrollHandler extends MediaCarouselScrollHandler {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiMediaCarouselScrollHandler(@NotNull MediaScrollView mediaScrollView, @NotNull PageIndicator pageIndicator, @NotNull DelayableExecutor delayableExecutor, @NotNull Function0<Unit> function0, @NotNull Function0<Unit> function02, @NotNull FalsingManager falsingManager) {
        super(mediaScrollView, pageIndicator, delayableExecutor, function0, function02, falsingManager);
        Intrinsics.checkParameterIsNotNull(mediaScrollView, "scrollView");
        Intrinsics.checkParameterIsNotNull(pageIndicator, "pageIndicator");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "mainExecutor");
        Intrinsics.checkParameterIsNotNull(function0, "dismissCallback");
        Intrinsics.checkParameterIsNotNull(function02, "translationChangedListener");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
    }

    /* access modifiers changed from: protected */
    public void startScroll(int i, int i2, float f) {
        getScrollView().cancelCurrentScroll();
        int playerWidthPlusPadding = i + (getPlayerWidthPlusPadding() / 3);
        int playerWidthPlusPadding2 = playerWidthPlusPadding - (playerWidthPlusPadding % getPlayerWidthPlusPadding());
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, 0.95f, 0.4f);
        AnimState animState = new AnimState("start");
        animState.add(ViewProperty.SCROLL_X, getScrollView().getScrollX(), new long[0]);
        AnimState animState2 = new AnimState("target");
        animState2.add(ViewProperty.SCROLL_X, playerWidthPlusPadding2, new long[0]);
        Folme.useAt(getScrollView()).state().fromTo(animState, animState2, animConfig);
    }
}
