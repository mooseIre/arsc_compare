package com.android.systemui.statusbar.notification.row;

import android.transition.Transition;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChannelEditorListView.kt */
public final class ChannelEditorListView$updateRows$1 implements Transition.TransitionListener {
    final /* synthetic */ ChannelEditorListView this$0;

    public void onTransitionCancel(@Nullable Transition transition) {
    }

    public void onTransitionPause(@Nullable Transition transition) {
    }

    public void onTransitionResume(@Nullable Transition transition) {
    }

    public void onTransitionStart(@Nullable Transition transition) {
    }

    ChannelEditorListView$updateRows$1(ChannelEditorListView channelEditorListView) {
        this.this$0 = channelEditorListView;
    }

    public void onTransitionEnd(@Nullable Transition transition) {
        this.this$0.notifySubtreeAccessibilityStateChangedIfNeeded();
    }
}
