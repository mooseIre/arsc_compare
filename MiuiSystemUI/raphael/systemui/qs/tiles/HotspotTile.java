package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManagerCompat;
import android.util.Log;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;

public class HotspotTile extends QSTileImpl<QSTile.BooleanState> {
    private final HotspotAndDataSaverCallbacks mCallbacks;
    private final DataSaverController mDataSaverController;
    private final QSTile.Icon mEnabledStatic = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_hotspot);
    private final HotspotController mHotspotController;
    private boolean mListening;
    private final QSTile.Icon mWifi4EnabledStatic = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_wifi_4_hotspot);
    private final QSTile.Icon mWifi5EnabledStatic = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_wifi_5_hotspot);
    private final QSTile.Icon mWifi6EnabledStatic = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_wifi_6_hotspot);
    private WifiManager mWifiManager;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedHeightMajor;
    }

    public HotspotTile(QSHost qSHost, HotspotController hotspotController, DataSaverController dataSaverController) {
        super(qSHost);
        HotspotAndDataSaverCallbacks hotspotAndDataSaverCallbacks = new HotspotAndDataSaverCallbacks();
        this.mCallbacks = hotspotAndDataSaverCallbacks;
        this.mHotspotController = hotspotController;
        this.mDataSaverController = dataSaverController;
        hotspotController.observe(this, hotspotAndDataSaverCallbacks);
        this.mDataSaverController.observe(this, this.mCallbacks);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mHotspotController.isHotspotSupported();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                refreshState();
            }
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.TETHER_SETTINGS");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        Object obj;
        boolean z = ((QSTile.BooleanState) this.mState).value;
        if (z || !this.mDataSaverController.isDataSaverEnabled()) {
            if (z) {
                obj = null;
            } else {
                obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
            }
            refreshState(obj);
            this.mHotspotController.setHotspotEnabled(!z);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_hotspot_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z;
        int i;
        int i2 = 1;
        boolean z2 = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        boolean z3 = z2 || this.mHotspotController.isHotspotTransient();
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_config_tethering");
        if (obj instanceof CallbackInfo) {
            CallbackInfo callbackInfo = (CallbackInfo) obj;
            booleanState.value = z2 || callbackInfo.isHotspotEnabled;
            i = callbackInfo.numConnectedDevices;
            z = callbackInfo.isDataSaverEnabled;
        } else {
            booleanState.value = z2 || this.mHotspotController.isHotspotEnabled();
            i = this.mHotspotController.getNumConnectedDevices();
            z = this.mDataSaverController.isDataSaverEnabled();
        }
        booleanState.icon = this.mEnabledStatic;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_hotspot_label);
        booleanState.isTransient = z3;
        booleanState.slash.isSlashed = !booleanState.value && !z3;
        if (booleanState.isTransient) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(17302458);
        } else if (booleanState.value) {
            int softApWifiStandard = WifiManagerCompat.getSoftApWifiStandard(this.mWifiManager);
            if (softApWifiStandard == 6) {
                booleanState.icon = this.mWifi6EnabledStatic;
            } else if (softApWifiStandard == 5) {
                booleanState.icon = this.mWifi5EnabledStatic;
            } else if (softApWifiStandard == 4) {
                booleanState.icon = this.mWifi4EnabledStatic;
            }
        }
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.contentDescription = booleanState.label;
        boolean z4 = booleanState.value || booleanState.isTransient;
        if (z) {
            booleanState.state = 0;
        } else {
            if (z4) {
                i2 = 2;
            }
            booleanState.state = i2;
        }
        String secondaryLabel = getSecondaryLabel(z4, z3, z, i);
        booleanState.secondaryLabel = secondaryLabel;
        booleanState.stateDescription = secondaryLabel;
    }

    private String getSecondaryLabel(boolean z, boolean z2, boolean z3, int i) {
        if (z2) {
            return this.mContext.getString(C0021R$string.quick_settings_hotspot_secondary_label_transient);
        }
        if (z3) {
            return this.mContext.getString(C0021R$string.quick_settings_hotspot_secondary_label_data_saver_enabled);
        }
        if (i <= 0 || !z) {
            return null;
        }
        return this.mContext.getResources().getQuantityString(C0019R$plurals.quick_settings_hotspot_secondary_label_num_devices, i, Integer.valueOf(i));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_hotspot_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_hotspot_changed_off);
    }

    private final class HotspotAndDataSaverCallbacks implements HotspotController.Callback, DataSaverController.Listener {
        CallbackInfo mCallbackInfo;

        private HotspotAndDataSaverCallbacks() {
            this.mCallbackInfo = new CallbackInfo();
        }

        @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
        public void onDataSaverChanged(boolean z) {
            CallbackInfo callbackInfo = this.mCallbackInfo;
            callbackInfo.isDataSaverEnabled = z;
            HotspotTile.this.refreshState(callbackInfo);
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            CallbackInfo callbackInfo = this.mCallbackInfo;
            callbackInfo.isHotspotEnabled = z;
            callbackInfo.numConnectedDevices = i;
            HotspotTile.this.refreshState(callbackInfo);
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotAvailabilityChanged(boolean z) {
            if (!z) {
                Log.d(((QSTileImpl) HotspotTile.this).TAG, "Tile removed. Hotspot no longer available");
                ((QSTileImpl) HotspotTile.this).mHost.removeTile(HotspotTile.this.getTileSpec());
            }
        }
    }

    /* access modifiers changed from: protected */
    public static final class CallbackInfo {
        boolean isDataSaverEnabled;
        boolean isHotspotEnabled;
        int numConnectedDevices;

        protected CallbackInfo() {
        }

        public String toString() {
            return "CallbackInfo[" + "isHotspotEnabled=" + this.isHotspotEnabled + ",numConnectedDevices=" + this.numConnectedDevices + ",isDataSaverEnabled=" + this.isDataSaverEnabled + ']';
        }
    }
}
