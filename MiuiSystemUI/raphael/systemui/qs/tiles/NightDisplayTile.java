package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.NightDisplayListener;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.LocationController;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public class NightDisplayTile extends QSTileImpl<QSTile.BooleanState> implements NightDisplayListener.Callback {
    private boolean mIsListening;
    private NightDisplayListener mListener = new NightDisplayListener(this.mContext, new Handler(Looper.myLooper()));
    private final LocationController mLocationController;
    private final ColorDisplayManager mManager = ((ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class));

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 491;
    }

    public NightDisplayTile(QSHost qSHost, LocationController locationController) {
        super(qSHost);
        this.mLocationController = locationController;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return ColorDisplayManager.isNightDisplayAvailable(this.mContext);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if ("1".equals(Settings.Global.getString(this.mContext.getContentResolver(), "night_display_forced_auto_mode_available")) && this.mManager.getNightDisplayAutoModeRaw() == -1) {
            this.mManager.setNightDisplayAutoMode(1);
            Log.i("NightDisplayTile", "Enrolled in forced night display auto mode");
        }
        this.mManager.setNightDisplayActivated(!((QSTile.BooleanState) this.mState).value);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        if (this.mIsListening) {
            this.mListener.setCallback((NightDisplayListener.Callback) null);
        }
        NightDisplayListener nightDisplayListener = new NightDisplayListener(this.mContext, i, new Handler(Looper.myLooper()));
        this.mListener = nightDisplayListener;
        if (this.mIsListening) {
            nightDisplayListener.setCallback(this);
        }
        super.handleUserSwitch(i);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        CharSequence charSequence;
        booleanState.value = this.mManager.isNightDisplayActivated();
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_night_display_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(17302824);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.state = booleanState.value ? 2 : 1;
        String secondaryLabel = getSecondaryLabel(booleanState.value);
        booleanState.secondaryLabel = secondaryLabel;
        if (TextUtils.isEmpty(secondaryLabel)) {
            charSequence = booleanState.label;
        } else {
            charSequence = TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
        }
        booleanState.contentDescription = charSequence;
    }

    private String getSecondaryLabel(boolean z) {
        LocalTime localTime;
        int i;
        int nightDisplayAutoMode = this.mManager.getNightDisplayAutoMode();
        if (nightDisplayAutoMode == 1) {
            if (z) {
                localTime = this.mManager.getNightDisplayCustomEndTime();
                i = C0021R$string.quick_settings_secondary_label_until;
            } else {
                localTime = this.mManager.getNightDisplayCustomStartTime();
                i = C0021R$string.quick_settings_night_secondary_label_on_at;
            }
            Calendar instance = Calendar.getInstance();
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this.mContext);
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            instance.setTimeZone(timeFormat.getTimeZone());
            instance.set(11, localTime.getHour());
            instance.set(12, localTime.getMinute());
            instance.set(13, 0);
            instance.set(14, 0);
            return this.mContext.getString(i, timeFormat.format(instance.getTime()));
        } else if (nightDisplayAutoMode != 2 || !this.mLocationController.isLocationEnabled()) {
            return null;
        } else {
            if (z) {
                return this.mContext.getString(C0021R$string.quick_settings_night_secondary_label_until_sunrise);
            }
            return this.mContext.getString(C0021R$string.quick_settings_night_secondary_label_on_at_sunset);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).addTaggedData(1311, Integer.valueOf(this.mManager.getNightDisplayAutoModeRaw()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.NIGHT_DISPLAY_SETTINGS");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mIsListening = z;
        if (z) {
            this.mListener.setCallback(this);
            refreshState();
            return;
        }
        this.mListener.setCallback((NightDisplayListener.Callback) null);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_night_display_label);
    }

    public void onActivated(boolean z) {
        refreshState();
    }
}
