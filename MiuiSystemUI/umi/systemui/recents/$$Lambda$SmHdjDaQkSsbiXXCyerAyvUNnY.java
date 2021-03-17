package com.android.systemui.recents;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.recents.-$$Lambda$SmHdjDaQkSsbiXXCyer-AyvUNnY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$SmHdjDaQkSsbiXXCyerAyvUNnY implements Consumer {
    public static final /* synthetic */ $$Lambda$SmHdjDaQkSsbiXXCyerAyvUNnY INSTANCE = new $$Lambda$SmHdjDaQkSsbiXXCyerAyvUNnY();

    private /* synthetic */ $$Lambda$SmHdjDaQkSsbiXXCyerAyvUNnY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Divider) obj).onRecentsDrawn();
    }
}
