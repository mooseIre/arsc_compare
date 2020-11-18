package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.HotspotController;
import miui.securityspace.CrossUserUtils;

public class HotspotTile extends QSTileImpl<QSTile.AirplaneBooleanState> {
    static final Intent TETHER_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$TetherSettingsActivity"));
    private final GlobalSetting mAirplaneMode = new GlobalSetting(this.mContext, this.mHandler, "airplane_mode_on") {
        /* access modifiers changed from: protected */
        public void handleValueChanged(int i) {
            HotspotTile.this.refreshState();
        }
    };
    private final Callback mCallback = new Callback();
    private final HotspotController mController = ((HotspotController) Dependency.get(HotspotController.class));
    private boolean mListening;

    public int getMetricsCategory() {
        return 120;
    }

    public HotspotTile(QSHost qSHost) {
        super(qSHost);
    }

    public boolean isAvailable() {
        return this.mController.isHotspotSupported() && CrossUserUtils.getCurrentUserId() == 0;
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.AirplaneBooleanState newTileState() {
        return new QSTile.AirplaneBooleanState();
    }

    public void handleSetListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mController.addCallback(this.mCallback);
                refreshState();
            } else {
                this.mController.removeCallback(this.mCallback);
            }
            this.mAirplaneMode.setListening(z);
        }
    }

    public Intent getLongClickIntent() {
        return new Intent(TETHER_SETTINGS);
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        boolean z = ((QSTile.AirplaneBooleanState) this.mState).value;
        if ((z || this.mAirplaneMode.getValue() == 0) && this.mController.isHotspotReady()) {
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("handleClick: from: mState.value: ");
            sb.append(((QSTile.AirplaneBooleanState) this.mState).value);
            sb.append(", to: ");
            sb.append(!((QSTile.AirplaneBooleanState) this.mState).value);
            Log.d(str, sb.toString());
            this.mController.setHotspotEnabled(!z);
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_hotspot_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.AirplaneBooleanState airplaneBooleanState, Object obj) {
        checkIfRestrictionEnforcedByAdminOnly(airplaneBooleanState, "no_config_tethering");
        int i = 0;
        airplaneBooleanState.isTransient = false;
        airplaneBooleanState.value = this.mController.isHotspotEnabled();
        airplaneBooleanState.withAnimation = this.mController.isHotspotTransient() && !airplaneBooleanState.value;
        airplaneBooleanState.label = this.mContext.getString(R.string.quick_settings_hotspot_label);
        airplaneBooleanState.isAirplaneMode = this.mAirplaneMode.getValue() != 0;
        if (airplaneBooleanState.value) {
            airplaneBooleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_hotspot_enabled), this.mInControlCenter));
        } else {
            airplaneBooleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_hotspot_disabled), this.mInControlCenter));
        }
        if (!airplaneBooleanState.isAirplaneMode) {
            i = airplaneBooleanState.value ? 2 : 1;
        }
        airplaneBooleanState.state = i;
        StringBuilder sb = new StringBuilder();
        sb.append(airplaneBooleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(airplaneBooleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        airplaneBooleanState.contentDescription = sb.toString();
        airplaneBooleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.AirplaneBooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_hotspot_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_hotspot_changed_off);
    }

    private final class Callback implements HotspotController.Callback {
        private Callback() {
        }

        public void onHotspotChanged(boolean z) {
            HotspotTile.this.refreshState(Boolean.valueOf(z));
        }

        public void onHotspotAvailabilityChanged(boolean z) {
            if (!z) {
                Log.d(HotspotTile.this.TAG, "Tile removed. Hotspot no longer available");
                HotspotTile.this.mHost.removeTile(HotspotTile.this.getTileSpec());
            }
        }
    }
}
