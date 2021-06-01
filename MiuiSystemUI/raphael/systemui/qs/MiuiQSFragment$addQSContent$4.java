package com.android.systemui.qs;

import android.database.ContentObserver;
import android.os.Handler;

/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$addQSContent$4 extends ContentObserver {
    final /* synthetic */ MiuiQSFragment this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiQSFragment$addQSContent$4(MiuiQSFragment miuiQSFragment, Handler handler) {
        super(handler);
        this.this$0 = miuiQSFragment;
    }

    public void onChange(boolean z) {
        this.this$0.updateQSDataUsage();
    }
}
