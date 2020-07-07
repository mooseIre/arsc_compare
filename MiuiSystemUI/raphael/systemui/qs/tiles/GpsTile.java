package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;

public class GpsTile extends QSTileImpl<QSTile.BooleanState> {
    private int mCurrentUserId = ActivityManager.getCurrentUser();
    private final ContentObserver mLocationAllowedObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            GpsTile.this.refreshState();
        }
    };
    private final ContentResolver mResolver = this.mContext.getContentResolver();

    public int getMetricsCategory() {
        return -1;
    }

    public GpsTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mResolver.registerContentObserver(Settings.Secure.getUriFor("location_providers_allowed"), false, this.mLocationAllowedObserver, -1);
        } else {
            this.mResolver.unregisterContentObserver(this.mLocationAllowedObserver);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
        this.mCurrentUserId = i;
    }

    public Intent getLongClickIntent() {
        return longClickGPSIntent();
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
        TilesHelper.setLocationProviderEnabledForUser(this.mContext, "gps", !((QSTile.BooleanState) this.mState).value, this.mCurrentUserId);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(Constants.SUPPORT_DUAL_GPS ? R.string.quick_settings_dual_location_label : R.string.quick_settings_gps_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        booleanState.value = TilesHelper.isLocationProviderEnabledForUser(this.mContext, "gps", this.mCurrentUserId);
        if (booleanState.value) {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_location_on);
        } else {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_location_off);
        }
        booleanState.label = this.mContext.getString(Constants.SUPPORT_DUAL_GPS ? R.string.quick_settings_dual_location_label : R.string.quick_settings_gps_label);
        if (booleanState.value) {
            i = Constants.SUPPORT_DUAL_GPS ? R.drawable.ic_qs_dual_location_enabled : R.drawable.ic_signal_location_enable;
        } else {
            i = Constants.SUPPORT_DUAL_GPS ? R.drawable.ic_qs_dual_location_disabled : R.drawable.ic_signal_location_disable;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(i), this.mInControlCenter));
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private Intent longClickGPSIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$LocationSettingsActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }
}
