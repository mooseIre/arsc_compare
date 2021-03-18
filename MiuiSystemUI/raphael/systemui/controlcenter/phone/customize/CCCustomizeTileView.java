package com.android.systemui.controlcenter.phone.customize;

import android.content.Context;
import com.android.systemui.controlcenter.qs.tileview.CCQSTileView;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;

public class CCCustomizeTileView extends CCQSTileView {
    public CCCustomizeTileView(Context context, QSIconView qSIconView) {
        super(context, qSIconView);
        setGravity(49);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.qs.tileview.CCQSTileView
    public void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
    }
}
