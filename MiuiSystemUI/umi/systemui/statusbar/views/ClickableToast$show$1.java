package com.android.systemui.statusbar.views;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import kotlin.TypeCastException;

/* compiled from: ClickableToast.kt */
final class ClickableToast$show$1 implements Runnable {
    final /* synthetic */ ClickableToast this$0;

    ClickableToast$show$1(ClickableToast clickableToast) {
        this.this$0 = clickableToast;
    }

    public final void run() {
        if (this.this$0.mView != null) {
            View view = this.this$0.mView;
            ViewParent viewParent = null;
            if ((view != null ? view.getParent() : null) instanceof ViewGroup) {
                View view2 = this.this$0.mView;
                if (view2 != null) {
                    viewParent = view2.getParent();
                }
                if (viewParent != null) {
                    ((ViewGroup) viewParent).removeView(this.this$0.mView);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
                }
            }
        }
        WindowManager windowManager = this.this$0.mWindowManager;
        if (windowManager != null) {
            windowManager.addView(this.this$0.mView, this.this$0.mParams);
        }
        Runnable runnable = this.this$0.mCancelRunnable;
        if (runnable != null) {
            ClickableToast.sHandler.postDelayed(runnable, 4000);
        }
    }
}
