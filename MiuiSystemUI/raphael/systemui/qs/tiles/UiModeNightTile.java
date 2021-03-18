package com.android.systemui.qs.tiles;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Switch;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.LocationController;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UiModeNightTile extends QSTileImpl<QSTile.BooleanState> implements ConfigurationController.ConfigurationListener, BatteryController.BatteryStateChangeCallback {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
    private final BatteryController mBatteryController;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(17302825);
    private final LocationController mLocationController;
    private final UiModeManager mUiModeManager;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 1706;
    }

    public UiModeNightTile(QSHost qSHost, ConfigurationController configurationController, BatteryController batteryController, LocationController locationController) {
        super(qSHost);
        this.mBatteryController = batteryController;
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService(UiModeManager.class);
        this.mLocationController = locationController;
        configurationController.observe(getLifecycle(), this);
        batteryController.observe(getLifecycle(), this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        refreshState();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (((QSTile.BooleanState) getState()).state != 0) {
            boolean z = !((QSTile.BooleanState) this.mState).value;
            this.mUiModeManager.setNightModeActivated(z);
            refreshState(Boolean.valueOf(z));
        }
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        CharSequence charSequence;
        LocalTime localTime;
        int i;
        int i2;
        int nightMode = this.mUiModeManager.getNightMode();
        boolean isPowerSave = this.mBatteryController.isPowerSave();
        int i3 = 1;
        boolean z = (this.mContext.getResources().getConfiguration().uiMode & 48) == 32;
        if (isPowerSave) {
            booleanState.secondaryLabel = this.mContext.getResources().getString(C0021R$string.quick_settings_dark_mode_secondary_label_battery_saver);
        } else if (nightMode == 0 && this.mLocationController.isLocationEnabled()) {
            Resources resources = this.mContext.getResources();
            if (z) {
                i2 = C0021R$string.quick_settings_dark_mode_secondary_label_until_sunrise;
            } else {
                i2 = C0021R$string.quick_settings_dark_mode_secondary_label_on_at_sunset;
            }
            booleanState.secondaryLabel = resources.getString(i2);
        } else if (nightMode == 3) {
            boolean is24HourFormat = DateFormat.is24HourFormat(this.mContext);
            if (z) {
                localTime = this.mUiModeManager.getCustomNightModeEnd();
            } else {
                localTime = this.mUiModeManager.getCustomNightModeStart();
            }
            Resources resources2 = this.mContext.getResources();
            if (z) {
                i = C0021R$string.quick_settings_dark_mode_secondary_label_until;
            } else {
                i = C0021R$string.quick_settings_dark_mode_secondary_label_on_at;
            }
            Object[] objArr = new Object[1];
            objArr[0] = is24HourFormat ? localTime.toString() : formatter.format(localTime);
            booleanState.secondaryLabel = resources2.getString(i, objArr);
        } else {
            booleanState.secondaryLabel = null;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_ui_mode_night_label);
        booleanState.icon = this.mIcon;
        if (TextUtils.isEmpty(booleanState.secondaryLabel)) {
            charSequence = booleanState.label;
        } else {
            charSequence = TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
        }
        booleanState.contentDescription = charSequence;
        if (isPowerSave) {
            booleanState.state = 0;
        } else {
            if (booleanState.value) {
                i3 = 2;
            }
            booleanState.state = i3;
        }
        booleanState.showRippleEffect = false;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.DARK_THEME_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return ((QSTile.BooleanState) getState()).label;
    }
}
