package com.android.systemui.recents;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Function;

/* renamed from: com.android.systemui.recents.-$$Lambda$OverviewProxyService$1$jWyXSUssf3YIGp2Ozuegdbo3RQM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OverviewProxyService$1$jWyXSUssf3YIGp2Ozuegdbo3RQM implements Function {
    public static final /* synthetic */ $$Lambda$OverviewProxyService$1$jWyXSUssf3YIGp2Ozuegdbo3RQM INSTANCE = new $$Lambda$OverviewProxyService$1$jWyXSUssf3YIGp2Ozuegdbo3RQM();

    private /* synthetic */ $$Lambda$OverviewProxyService$1$jWyXSUssf3YIGp2Ozuegdbo3RQM() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((Divider) obj).getView().getNonMinimizedSplitScreenSecondaryBounds();
    }
}
