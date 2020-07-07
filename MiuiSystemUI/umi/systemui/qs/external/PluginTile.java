package com.android.systemui.qs.external;

import android.content.Intent;
import com.android.systemui.plugins.miui.qs.MiuiQSTile;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class PluginTile extends QSTileImpl<QSTile.State> implements QSTile.Callback {
    private boolean mListening;
    private MiuiQSTile mMiuiQsTile;

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
    }

    public void onAnnouncementRequested(CharSequence charSequence) {
    }

    public void onScanStateChanged(boolean z) {
    }

    public void onShowDetail(boolean z) {
    }

    public void onShowEdit(boolean z) {
    }

    public void onToggleStateChanged(boolean z) {
    }

    public PluginTile(QSTileHost qSTileHost, MiuiQSTile miuiQSTile) {
        super(qSTileHost);
        this.mMiuiQsTile = miuiQSTile;
    }

    public boolean isAvailable() {
        return this.mMiuiQsTile.isAvailable();
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.State newTileState() {
        return new QSTile.State();
    }

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

    public void onStateChanged(QSTile.State state) {
        refreshState(state);
    }

    public Intent getLongClickIntent() {
        return this.mMiuiQsTile.getLongClickIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        this.mMiuiQsTile.handleClick();
    }

    public CharSequence getTileLabel() {
        return this.mMiuiQsTile.getState().label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.State state, Object obj) {
        this.mMiuiQsTile.refreshState((Object) null);
        this.mMiuiQsTile.getState().copyTo(state);
    }

    public int getMetricsCategory() {
        return this.mMiuiQsTile.getMetricsCategory();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        return this.mMiuiQsTile.composeChangeAnnouncement();
    }
}
