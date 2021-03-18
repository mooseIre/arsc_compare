package com.android.systemui.controlcenter.qs.tile;

import android.content.Intent;
import com.android.systemui.plugins.miui.qs.MiuiQSTile;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class PluginTile extends QSTileImpl<QSTile.State> implements QSTile.Callback {
    private boolean mListening;
    private MiuiQSTile mMiuiQsTile;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
    }

    @Override // com.android.systemui.plugins.qs.QSTile.Callback
    public void onAnnouncementRequested(CharSequence charSequence) {
    }

    @Override // com.android.systemui.plugins.qs.QSTile.Callback
    public void onScanStateChanged(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QSTile.Callback
    public void onShowDetail(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QSTile.Callback
    public void onToggleStateChanged(boolean z) {
    }

    public PluginTile(QSHost qSHost, MiuiQSTile miuiQSTile) {
        super(qSHost);
        this.mMiuiQsTile = miuiQSTile;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mMiuiQsTile.isAvailable();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mMiuiQsTile.addCallback(this);
            } else {
                this.mMiuiQsTile.removeCallback(this);
            }
            this.mMiuiQsTile.setListening(z);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile.Callback
    public void onStateChanged(QSTile.State state) {
        refreshState(state);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return this.mMiuiQsTile.getLongClickIntent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        this.mMiuiQsTile.handleClick();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mMiuiQsTile.getState().label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.State state, Object obj) {
        this.mMiuiQsTile.refreshState(null);
        this.mMiuiQsTile.getState().copyTo(state);
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return this.mMiuiQsTile.getMetricsCategory();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        return this.mMiuiQsTile.composeChangeAnnouncement();
    }
}
