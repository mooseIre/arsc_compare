package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;

public abstract class CustomInterpolatorTransformation extends ViewTransformationHelper.CustomTransformation {
    private final int mViewType;

    /* access modifiers changed from: protected */
    public abstract boolean hasCustomTransformation();

    public CustomInterpolatorTransformation(int i) {
        this.mViewType = i;
    }

    public boolean transformTo(TransformState transformState, TransformableView transformableView, float f) {
        TransformState currentState;
        if (!hasCustomTransformation() || (currentState = transformableView.getCurrentState(this.mViewType)) == null) {
            return false;
        }
        CrossFadeHelper.fadeOut(transformState.getTransformedView(), f);
        transformState.transformViewFullyTo(currentState, this, f);
        currentState.recycle();
        return true;
    }

    public boolean transformFrom(TransformState transformState, TransformableView transformableView, float f) {
        TransformState currentState;
        if (!hasCustomTransformation() || (currentState = transformableView.getCurrentState(this.mViewType)) == null) {
            return false;
        }
        CrossFadeHelper.fadeIn(transformState.getTransformedView(), f, true);
        transformState.transformViewFullyFrom(currentState, this, f);
        currentState.recycle();
        return true;
    }
}
