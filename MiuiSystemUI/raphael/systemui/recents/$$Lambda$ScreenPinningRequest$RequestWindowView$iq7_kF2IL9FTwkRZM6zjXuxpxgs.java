package com.android.systemui.recents;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.function.Function;

/* renamed from: com.android.systemui.recents.-$$Lambda$ScreenPinningRequest$RequestWindowView$iq7_kF2IL9FTwkRZM6zjXuxpxgs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ScreenPinningRequest$RequestWindowView$iq7_kF2IL9FTwkRZM6zjXuxpxgs implements Function {
    public static final /* synthetic */ $$Lambda$ScreenPinningRequest$RequestWindowView$iq7_kF2IL9FTwkRZM6zjXuxpxgs INSTANCE = new $$Lambda$ScreenPinningRequest$RequestWindowView$iq7_kF2IL9FTwkRZM6zjXuxpxgs();

    private /* synthetic */ $$Lambda$ScreenPinningRequest$RequestWindowView$iq7_kF2IL9FTwkRZM6zjXuxpxgs() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((StatusBar) ((Lazy) obj).get()).getNavigationBarView();
    }
}
