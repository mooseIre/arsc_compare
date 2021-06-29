package com.android.systemui.qs;

import android.view.View;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiQSFragment.kt */
final class MiuiQSFragment$onViewCreated$1 implements View.OnLayoutChangeListener {
    final /* synthetic */ MiuiQSFragment this$0;

    MiuiQSFragment$onViewCreated$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public final void onLayoutChange(@Nullable View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if ((i6 - i8 != i2 - i4) && !FoldManager.Companion.isShowingUnimportant()) {
            MiuiQSFragment miuiQSFragment = this.this$0;
            miuiQSFragment.setQsExpansion(miuiQSFragment.lastQSExpansion, this.this$0.lastQSExpansion);
        }
    }
}
