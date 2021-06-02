package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;

public class LocationTile extends QSTileImpl<QSTile.BooleanState> {
    private final Callback mCallback;
    private final LocationController mController;
    private final KeyguardStateController mKeyguard;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedWidthMajor;
    }

    public LocationTile(QSHost qSHost, LocationController locationController, KeyguardStateController keyguardStateController, ActivityStarter activityStarter) {
        super(qSHost);
        Callback callback = new Callback();
        this.mCallback = callback;
        this.mController = locationController;
        this.mKeyguard = keyguardStateController;
        locationController.observe(this, callback);
        this.mKeyguard.observe(this, this.mCallback);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.location.gps");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
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

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_location_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mController.isLocationEnabled();
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_share_location");
        if (!booleanState.disabledByPolicy) {
            checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_config_location");
        }
        booleanState.contentDescription = this.mContext.getString(booleanState.value ? C0021R$string.accessibility_quick_settings_location_on : C0021R$string.accessibility_quick_settings_location_off);
        booleanState.icon = QSTileImpl.ResourceIcon.get(booleanState.value ? C0013R$drawable.ic_signal_location_enable : C0013R$drawable.ic_signal_location_disable);
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_location_label);
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_location_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_location_changed_off);
    }

    private final class Callback implements LocationController.LocationChangeCallback, KeyguardStateController.Callback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.LocationController.LocationChangeCallback
        public void onLocationSettingsChanged(boolean z) {
            LocationTile.this.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            LocationTile.this.refreshState();
        }
    }
}
