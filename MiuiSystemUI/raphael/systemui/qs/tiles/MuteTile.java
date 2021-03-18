package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.provider.MiuiSettings;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.miui.systemui.util.CommonUtil;

public class MuteTile extends QSTileImpl<QSTile.BooleanState> implements ZenModeController.Callback {
    private final ZenModeController mZenModeController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    public MuteTile(QSHost qSHost, ZenModeController zenModeController) {
        super(qSHost);
        this.mZenModeController = zenModeController;
        zenModeController.observe(getLifecycle(), this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.settings.SOUND_SETTINGS");
        intent.setPackage("com.android.settings");
        intent.setFlags(335544320);
        return intent;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (this.mZenModeController.isRingerModeOn()) {
            Context context = this.mContext;
            CommonUtil.playRingtoneAsync(context, RingtoneManager.getActualDefaultRingtoneUri(context, 2), 5);
        }
        this.mZenModeController.toggleSilent();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
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
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenOrRingerChanged(boolean z, boolean z2) {
        refreshState();
    }
}
