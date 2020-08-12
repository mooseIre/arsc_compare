package com.android.systemui.miui.volume.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.android.systemui.miui.volume.R$styleable;

class ExpandCollapseStateHelper {
    private boolean mExpanded = false;
    private OnExpandStateUpdatedListener mListener;
    private Transition mTransitionCollapse;
    private Transition mTransitionExpand;
    private ViewGroup mTransitionRoot;

    public interface OnExpandStateUpdatedListener {
        void onExpandStateUpdated(boolean z);
    }

    public ExpandCollapseStateHelper(ViewGroup viewGroup, OnExpandStateUpdatedListener onExpandStateUpdatedListener, AttributeSet attributeSet, int i) {
        this.mTransitionRoot = viewGroup;
        Context context = viewGroup.getContext();
        this.mListener = onExpandStateUpdatedListener;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ExpandCollapseLayout, i, 0);
        this.mTransitionExpand = getTransition(context, obtainStyledAttributes, R$styleable.ExpandCollapseLayout_expandingTransition, new AutoTransition());
        this.mTransitionCollapse = getTransition(context, obtainStyledAttributes, R$styleable.ExpandCollapseLayout_collapsingTransition, new AutoTransition());
        obtainStyledAttributes.recycle();
    }

    private static Transition getTransition(Context context, TypedArray typedArray, int i, Transition transition) {
        int resourceId = typedArray.getResourceId(i, -1);
        return resourceId > 0 ? TransitionInflater.from(context).inflateTransition(resourceId) : transition;
    }

    public final void updateExpanded(boolean z, boolean z2) {
        this.mExpanded = z;
        if (z2) {
            beginDelayedTransition();
        }
        OnExpandStateUpdatedListener onExpandStateUpdatedListener = this.mListener;
        if (onExpandStateUpdatedListener != null) {
            onExpandStateUpdatedListener.onExpandStateUpdated(z);
        }
    }

    public void beginDelayedTransition() {
        TransitionManager.endTransitions(this.mTransitionRoot);
        TransitionManager.beginDelayedTransition(this.mTransitionRoot, this.mExpanded ? this.mTransitionExpand : this.mTransitionCollapse);
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }
}
