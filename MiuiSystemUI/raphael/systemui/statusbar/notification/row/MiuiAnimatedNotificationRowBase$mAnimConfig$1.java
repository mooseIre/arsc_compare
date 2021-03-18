package com.android.systemui.statusbar.notification.row;

import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import miuix.animation.listener.TransitionListener;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiAnimatedNotificationRowBase.kt */
public final class MiuiAnimatedNotificationRowBase$mAnimConfig$1 extends TransitionListener {
    final /* synthetic */ MiuiAnimatedNotificationRowBase this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiAnimatedNotificationRowBase$mAnimConfig$1(MiuiAnimatedNotificationRowBase miuiAnimatedNotificationRowBase) {
        this.this$0 = miuiAnimatedNotificationRowBase;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onBegin(@Nullable Object obj) {
        super.onBegin(obj);
        ExpandableViewState viewState = this.this$0.getViewState();
        if (viewState != null) {
            viewState.setAnimatingAddRemove(true);
        }
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onCancel(@Nullable Object obj) {
        super.onCancel(obj);
        ExpandableViewState viewState = this.this$0.getViewState();
        if (viewState != null) {
            viewState.setAnimatingAddRemove(false);
        }
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onComplete(@Nullable Object obj) {
        super.onComplete(obj);
        ExpandableViewState viewState = this.this$0.getViewState();
        if (viewState != null) {
            viewState.setAnimatingAddRemove(false);
        }
    }
}
