package com.android.systemui.statusbar.phone;

import android.util.Pair;
import java.util.function.ToIntFunction;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$NearestTouchFrame$NP6mvtRuXVTLLChUNbbl4JUIMyU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NearestTouchFrame$NP6mvtRuXVTLLChUNbbl4JUIMyU implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$NearestTouchFrame$NP6mvtRuXVTLLChUNbbl4JUIMyU INSTANCE = new $$Lambda$NearestTouchFrame$NP6mvtRuXVTLLChUNbbl4JUIMyU();

    private /* synthetic */ $$Lambda$NearestTouchFrame$NP6mvtRuXVTLLChUNbbl4JUIMyU() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((Integer) ((Pair) obj).first).intValue();
    }
}
