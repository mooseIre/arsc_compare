package com.android.systemui.qs.customize;

import android.content.Context;
import android.widget.TextView;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.tileimpl.MiuiQSTileView;

public class MiuiCustomizeTileView extends MiuiQSTileView {
    private boolean mShowAppLabel;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.MiuiQSTileBaseView
    public boolean animationsEnabled() {
        return false;
    }

    public boolean isLongClickable() {
        return false;
    }

    public MiuiCustomizeTileView(Context context, QSIconView qSIconView) {
        super(context, qSIconView);
    }

    public void setShowAppLabel(boolean z) {
        this.mShowAppLabel = z;
        this.mSecondLine.setVisibility(z ? 0 : 8);
        this.mLabel.setSingleLine(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.MiuiQSTileBaseView, com.android.systemui.qs.tileimpl.MiuiQSTileView
    public void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
        this.mSecondLine.setVisibility(this.mShowAppLabel ? 0 : 8);
    }

    public TextView getAppLabel() {
        return this.mSecondLine;
    }
}
