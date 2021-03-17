package com.android.systemui.statusbar.phone;

import java.util.function.BiConsumer;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$aKsp0zdf_wKFZXD1TonJ2cFEsN4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$aKsp0zdf_wKFZXD1TonJ2cFEsN4 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$aKsp0zdf_wKFZXD1TonJ2cFEsN4 INSTANCE = new $$Lambda$aKsp0zdf_wKFZXD1TonJ2cFEsN4();

    private /* synthetic */ $$Lambda$aKsp0zdf_wKFZXD1TonJ2cFEsN4() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((NotificationPanelView) obj).setPanelAlphaInternal(((Float) obj2).floatValue());
    }
}
