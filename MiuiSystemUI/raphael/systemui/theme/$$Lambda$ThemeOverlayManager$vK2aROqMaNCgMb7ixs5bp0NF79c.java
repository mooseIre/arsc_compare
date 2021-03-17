package com.android.systemui.theme;

import android.content.om.OverlayInfo;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.theme.-$$Lambda$ThemeOverlayManager$vK2aROqMaNCgMb7ixs5bp0NF79c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ThemeOverlayManager$vK2aROqMaNCgMb7ixs5bp0NF79c implements Predicate {
    public static final /* synthetic */ $$Lambda$ThemeOverlayManager$vK2aROqMaNCgMb7ixs5bp0NF79c INSTANCE = new $$Lambda$ThemeOverlayManager$vK2aROqMaNCgMb7ixs5bp0NF79c();

    private /* synthetic */ $$Lambda$ThemeOverlayManager$vK2aROqMaNCgMb7ixs5bp0NF79c() {
    }

    public final boolean test(Object obj) {
        return ((OverlayInfo) obj).isEnabled();
    }
}
