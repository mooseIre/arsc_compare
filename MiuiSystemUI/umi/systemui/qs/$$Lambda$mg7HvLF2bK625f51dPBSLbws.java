package com.android.systemui.qs;

import com.android.systemui.statusbar.phone.StatusBar;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.qs.-$$Lambda$mg7HvLF2bK-625f51dPB--SLbws  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$mg7HvLF2bK625f51dPBSLbws implements Consumer {
    public static final /* synthetic */ $$Lambda$mg7HvLF2bK625f51dPBSLbws INSTANCE = new $$Lambda$mg7HvLF2bK625f51dPBSLbws();

    private /* synthetic */ $$Lambda$mg7HvLF2bK625f51dPBSLbws() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((StatusBar) obj).postAnimateForceCollapsePanels();
    }
}
