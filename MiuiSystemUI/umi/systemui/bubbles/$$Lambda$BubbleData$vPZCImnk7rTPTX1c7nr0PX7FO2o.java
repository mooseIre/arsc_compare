package com.android.systemui.bubbles;

import java.util.function.Function;

/* renamed from: com.android.systemui.bubbles.-$$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o implements Function {
    public static final /* synthetic */ $$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o INSTANCE = new $$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o();

    private /* synthetic */ $$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Long.valueOf(BubbleData.sortKey((Bubble) obj));
    }
}
