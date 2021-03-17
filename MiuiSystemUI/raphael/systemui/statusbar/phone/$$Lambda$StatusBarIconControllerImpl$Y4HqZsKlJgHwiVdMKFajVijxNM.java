package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.phone.StatusBarIconController;
import java.util.function.BiConsumer;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarIconControllerImpl$Y4HqZsKlJgHwiVdMKFajVi-jxNM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$StatusBarIconControllerImpl$Y4HqZsKlJgHwiVdMKFajVijxNM implements BiConsumer {
    public static final /* synthetic */ $$Lambda$StatusBarIconControllerImpl$Y4HqZsKlJgHwiVdMKFajVijxNM INSTANCE = new $$Lambda$StatusBarIconControllerImpl$Y4HqZsKlJgHwiVdMKFajVijxNM();

    private /* synthetic */ $$Lambda$StatusBarIconControllerImpl$Y4HqZsKlJgHwiVdMKFajVijxNM() {
    }

    public final void accept(Object obj, Object obj2) {
        ((StatusBarIconController.IconManager) obj).onDensityOrFontScaleChanged();
    }
}
