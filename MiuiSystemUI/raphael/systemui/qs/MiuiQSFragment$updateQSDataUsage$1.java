package com.android.systemui.qs;

import android.app.MiuiStatusBarManager;
import android.content.Context;

/* access modifiers changed from: package-private */
/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$updateQSDataUsage$1 implements Runnable {
    final /* synthetic */ MiuiQSFragment this$0;

    MiuiQSFragment$updateQSDataUsage$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public final void run() {
        Context context = this.this$0.getContext();
        if (context != null) {
            this.this$0.getQsContainer().updateQSDataUsage(MiuiStatusBarManager.isShowFlowInfoForUser(context, -2));
        }
    }
}
