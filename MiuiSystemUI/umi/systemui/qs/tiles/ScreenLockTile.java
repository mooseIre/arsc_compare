package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class ScreenLockTile extends QSTileImpl<QSTile.BooleanState> {
    public Intent getLongClickIntent() {
        return null;
    }

    public int getMetricsCategory() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void handleLongClick() {
    }

    public void handleSetListening(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
    }

    public boolean isAvailable() {
        return true;
    }

    public ScreenLockTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        ((PowerManager) this.mContext.getSystemService("power")).goToSleep(SystemClock.uptimeMillis());
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_screenlock_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = false;
        booleanState.state = 1;
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_screenlock);
        booleanState.label = this.mHost.getContext().getString(C0021R$string.quick_settings_screenlock_label);
        booleanState.contentDescription = this.mContext.getString(C0021R$string.quick_settings_screenlock_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }
}
