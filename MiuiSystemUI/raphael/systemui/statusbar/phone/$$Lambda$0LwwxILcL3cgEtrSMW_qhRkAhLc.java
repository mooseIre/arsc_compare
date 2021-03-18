package com.android.systemui.statusbar.phone;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc implements Consumer {
    public static final /* synthetic */ $$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc INSTANCE = new $$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc();

    private /* synthetic */ $$Lambda$0LwwxILcL3cgEtrSMW_qhRkAhLc() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Divider) obj).onAppTransitionFinished();
    }
}
