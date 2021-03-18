package com.android.systemui.qs;

import com.android.systemui.statusbar.phone.StatusBar;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.qs.-$$Lambda$4RRpk2g2DG1jxcebU4uq2xyjwbI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$4RRpk2g2DG1jxcebU4uq2xyjwbI implements Consumer {
    public static final /* synthetic */ $$Lambda$4RRpk2g2DG1jxcebU4uq2xyjwbI INSTANCE = new $$Lambda$4RRpk2g2DG1jxcebU4uq2xyjwbI();

    private /* synthetic */ $$Lambda$4RRpk2g2DG1jxcebU4uq2xyjwbI() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((StatusBar) obj).postAnimateCollapsePanels();
    }
}
