package com.android.systemui.qs;

import com.android.systemui.qs.QSPanel;
import java.util.function.Function;

/* renamed from: com.android.systemui.qs.-$$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ implements Function {
    public static final /* synthetic */ $$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ INSTANCE = new $$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ();

    private /* synthetic */ $$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((QSPanel.TileRecord) obj).tile.getTileSpec();
    }
}
