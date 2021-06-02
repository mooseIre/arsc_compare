package com.android.keyguard.injector;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.RegionController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardNegative1PageInjector.kt */
public final class KeyguardNegative1PageInjector implements RegionController.Callback {
    private MiuiKeyguardMoveLeftViewContainer mKeyguardLeftView;
    private ImageView mLeftViewBg;
    private MiuiKeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    public KeyguardNegative1PageInjector(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
    }

    public final void onFinishInflate(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "notificationPanelView");
        View findViewById = view.findViewById(C0015R$id.left_view_bg);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "notificationPanelView.fi…ewById(R.id.left_view_bg)");
        this.mLeftViewBg = (ImageView) findViewById;
        this.mKeyguardLeftView = (MiuiKeyguardMoveLeftViewContainer) view.findViewById(C0015R$id.keyguard_left_view);
        this.mUpdateMonitorCallback = new KeyguardNegative1PageInjector$onFinishInflate$1(this);
    }

    @Nullable
    public final MiuiKeyguardMoveLeftViewContainer getLeftView() {
        return this.mKeyguardLeftView;
    }

    @NotNull
    public final ImageView getLeftBackgroundView() {
        ImageView imageView = this.mLeftViewBg;
        if (imageView != null) {
            return imageView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mLeftViewBg");
        throw null;
    }

    public final void onAttachedToWindow() {
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
    }

    public final void onDetachedFromWindow() {
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    @Override // com.android.systemui.statusbar.policy.RegionController.Callback
    public void onRegionChanged(@Nullable String str) {
        MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.mKeyguardLeftView;
        if (miuiKeyguardMoveLeftViewContainer != null) {
            miuiKeyguardMoveLeftViewContainer.initLeftView();
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
