package com.android.systemui.statusbar;

import com.android.systemui.statusbar.SysuiStatusBarStateController;
import java.util.function.ToIntFunction;

/* renamed from: com.android.systemui.statusbar.-$$Lambda$StatusBarStateControllerImpl$7y8VOe44iFeEd9HPscwVVB7kUfw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StatusBarStateControllerImpl$7y8VOe44iFeEd9HPscwVVB7kUfw implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$StatusBarStateControllerImpl$7y8VOe44iFeEd9HPscwVVB7kUfw INSTANCE = new $$Lambda$StatusBarStateControllerImpl$7y8VOe44iFeEd9HPscwVVB7kUfw();

    private /* synthetic */ $$Lambda$StatusBarStateControllerImpl$7y8VOe44iFeEd9HPscwVVB7kUfw() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((SysuiStatusBarStateController.RankedListener) obj).mRank;
    }
}
