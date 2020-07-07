package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;

public class LocationTile extends QSTileImpl<QSTile.BooleanState> {
    private final Callback mCallback = new Callback();
    private final LocationController mController = ((LocationController) Dependency.get(LocationController.class));
    private final KeyguardMonitor mKeyguard = ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class));

    public int getMetricsCategory() {
        return 122;
    }

    public LocationTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mController.addCallback(this.mCallback);
            this.mKeyguard.addCallback(this.mCallback);
            return;
        }
        this.mController.removeCallback(this.mCallback);
        this.mKeyguard.removeCallback(this.mCallback);
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
    }

    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.location.gps");
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleClick: from: ");
        sb.append(((QSTile.BooleanState) this.mState).value);
        sb.append(", to: ");
        sb.append(!((QSTile.BooleanState) this.mState).value);
        Log.d(str, sb.toString());
        this.mController.setLocationEnabled(!((QSTile.BooleanState) this.mState).value);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_location_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mController.isLocationEnabled();
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_share_location");
        booleanState.contentDescription = this.mContext.getString(booleanState.value ? R.string.accessibility_quick_settings_location_on : R.string.accessibility_quick_settings_location_off);
        booleanState.label = this.mContext.getString(R.string.quick_settings_location_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(booleanState.value ? R.drawable.ic_signal_location_enable : R.drawable.ic_signal_location_disable), this.mInControlCenter));
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_location_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_location_changed_off);
    }

    private final class Callback implements LocationController.LocationChangeCallback, KeyguardMonitor.Callback {
        public void onLocationActiveChanged(boolean z) {
        }

        public void onLocationStatusChanged(Intent intent) {
        }

        private Callback() {
        }

        public void onLocationSettingsChanged(boolean z) {
            LocationTile.this.refreshState();
        }

        public void onKeyguardShowingChanged() {
            LocationTile.this.refreshState();
        }
    }
}
