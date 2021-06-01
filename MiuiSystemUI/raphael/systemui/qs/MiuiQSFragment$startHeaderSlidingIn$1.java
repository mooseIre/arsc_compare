package com.android.systemui.qs;

import android.view.View;
import android.view.ViewTreeObserver;
import com.android.systemui.Interpolators;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$startHeaderSlidingIn$1 implements ViewTreeObserver.OnPreDrawListener {
    final /* synthetic */ MiuiQSFragment this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiQSFragment$startHeaderSlidingIn$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public boolean onPreDraw() {
        View view = this.this$0.getView();
        if (view != null) {
            view.getViewTreeObserver().removeOnPreDrawListener(this);
            View view2 = this.this$0.getView();
            if (view2 != null) {
                view2.animate().translationY(0.0f).setStartDelay(this.this$0.delay).setDuration((long) 448).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(this.this$0.animateHeaderSlidingInListener).start();
                return true;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
