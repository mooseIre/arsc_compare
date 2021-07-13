package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RowAnimationUtils.kt */
public final class RowAnimationUtils {
    public static final RowAnimationUtils INSTANCE = new RowAnimationUtils();

    private RowAnimationUtils() {
    }

    public static final /* synthetic */ void access$setTouchAnimatingState(RowAnimationUtils rowAnimationUtils, ExpandableView expandableView, boolean z) {
        rowAnimationUtils.setTouchAnimatingState(expandableView, z);
    }

    @NotNull
    public final String getFolmeTarget(@NotNull ExpandableView expandableView) {
        Intrinsics.checkParameterIsNotNull(expandableView, "row");
        return "ExpandableView_" + expandableView.hashCode();
    }

    public static final void startTouchAnimationIfNeed(@Nullable ExpandableView expandableView, float f) {
        AnimConfig animConfig;
        RowAnimationUtils rowAnimationUtils = INSTANCE;
        if (expandableView != null) {
            boolean z = false;
            if (f >= ((float) 0)) {
                String folmeTarget = rowAnimationUtils.getFolmeTarget(expandableView);
                if (expandableView.isGroupExpansionChanging()) {
                    Folme.useValue(folmeTarget).cancel();
                    if (f != 1.0f) {
                        z = true;
                    }
                    rowAnimationUtils.setTouchAnimatingState(expandableView, z);
                    return;
                }
                RowAnimationUtils$startTouchAnimationIfNeed$listener$1 rowAnimationUtils$startTouchAnimationIfNeed$listener$1 = new RowAnimationUtils$startTouchAnimationIfNeed$listener$1(f, expandableView, "scale", folmeTarget, folmeTarget);
                if (f == 1.0f) {
                    animConfig = new AnimConfig();
                    animConfig.setEase(-2, 0.6f, 0.25f);
                    animConfig.addListeners(rowAnimationUtils$startTouchAnimationIfNeed$listener$1);
                } else {
                    animConfig = new AnimConfig();
                    animConfig.setEase(-2, 0.9f, 0.4f);
                    animConfig.addListeners(rowAnimationUtils$startTouchAnimationIfNeed$listener$1);
                }
                Folme.useValue(folmeTarget).cancel();
                Folme.getValueTarget(folmeTarget).setMinVisibleChange(0.001f, "scale");
                Folme.useValue(folmeTarget).setTo("scale", Float.valueOf(expandableView.getScaleX())).to("scale", Float.valueOf(f), animConfig);
            }
        }
    }

    private final void setTouchAnimatingState(ExpandableView expandableView, boolean z) {
        ExpandableViewState viewState;
        if (expandableView != null && (viewState = expandableView.getViewState()) != null) {
            viewState.setTouchAnimating(z);
        }
    }
}
