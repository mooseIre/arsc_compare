package com.android.systemui.qs;

import android.view.View;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$addQSContent$1 implements View.OnScrollChangeListener {
    final /* synthetic */ MiuiQSFragment this$0;

    MiuiQSFragment$addQSContent$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public final void onScrollChange(@Nullable View view, int i, int i2, int i3, int i4) {
        QSAnimator qSAnimator = this.this$0.qsAnimator;
        if (qSAnimator != null) {
            qSAnimator.onQsScrollingChanged();
        }
    }
}
