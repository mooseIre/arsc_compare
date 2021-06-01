package com.android.systemui.qs;

import android.view.View;

/* access modifiers changed from: package-private */
/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$onLayoutChangeListener$1 implements View.OnLayoutChangeListener {
    final /* synthetic */ MiuiQSFragment this$0;

    MiuiQSFragment$onLayoutChangeListener$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        QSAnimator qSAnimator = this.this$0.qsAnimator;
        if (qSAnimator != null) {
            qSAnimator.onQsScrollingChanged();
        }
    }
}
