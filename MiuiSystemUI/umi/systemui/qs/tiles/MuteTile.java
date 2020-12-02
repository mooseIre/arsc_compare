package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.provider.MiuiSettings;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ZenModeController;

public class MuteTile extends QSTileImpl<QSTile.BooleanState> implements ZenModeController.Callback {
    private final ZenModeController mZenModeController;

    public int getMetricsCategory() {
        return -1;
    }

    public MuteTile(QSHost qSHost, ZenModeController zenModeController) {
        super(qSHost);
        this.mZenModeController = zenModeController;
        zenModeController.observe(getLifecycle(), this);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$SoundSettingsActivity"));
        intent.setFlags(335544320);
        return intent;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        this.mZenModeController.toggleSilent();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_mute_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        boolean z2 = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        MiuiSettings.SilenceMode.getZenMode(this.mContext);
        if (z2 || this.mZenModeController.isRingerModeOn()) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_mute_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_mute_on);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_mute_off);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public void onZenOrRingerChanged(boolean z, boolean z2) {
        refreshState();
    }
}
