package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.SystemProperties;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.WirelessChargingController;

public class MiuiWirelessPowerTile extends QSTileImpl<QSTile.BooleanState> {
    private final Callback mCallback = new Callback();
    private final WirelessChargingController mController = ((WirelessChargingController) Dependency.get(WirelessChargingController.class));
    private boolean mHideTile = this.mContext.getResources().getBoolean(R.bool.config_hideWirelessPowerTile);
    private boolean mListening;

    public int getMetricsCategory() {
        return -1;
    }

    public MiuiWirelessPowerTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mController.addCallback(this.mCallback);
                refreshState();
                return;
            }
            this.mController.removeCallback(this.mCallback);
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("com.miui.securitycenter.action.POWER_SETTINGS");
    }

    public boolean isAvailable() {
        return this.mController.isWirelessChargingSupported() && (!this.mHideTile || !"TW".equals(SystemProperties.get("ro.miui.region", "")));
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        this.mController.setWirelessChargingEnabled(!((QSTile.BooleanState) this.mState).value);
        this.mHost.collapsePanels();
        refreshState();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_wireless_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(R.string.quick_settings_wireless_label);
        boolean isWirelessChargingEnabled = this.mController.isWirelessChargingEnabled();
        booleanState.value = isWirelessChargingEnabled;
        booleanState.state = isWirelessChargingEnabled ? 2 : 1;
        booleanState.icon = QSTileImpl.ResourceIcon.get(booleanState.value ? R.drawable.ic_qs_wireless_chg_enabled : R.drawable.ic_qs_wireless_chg_disabled);
        booleanState.contentDescription = this.mContext.getString(R.string.quick_settings_wireless_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        return this.mContext.getString(R.string.quick_settings_wireless_label);
    }

    private final class Callback implements WirelessChargingController.Callback {
        private Callback() {
        }

        public void onWirelessChargingChanged(boolean z) {
            MiuiWirelessPowerTile.this.refreshState(Boolean.valueOf(z));
        }
    }
}
