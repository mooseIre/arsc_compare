package com.android.systemui.tuner;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import androidx.preference.DropDownPreference;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;

public class BatteryPreference extends DropDownPreference implements TunerService.Tunable {
    private final String mBattery;
    private boolean mBatteryEnabled;
    private ArraySet<String> mBlacklist;
    private boolean mHasPercentage;
    private boolean mHasSetValue;

    public BatteryPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBattery = context.getString(17041383);
        setEntryValues(new CharSequence[]{"percent", "default", "disabled"});
    }

    @Override // androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        boolean z = false;
        if (Settings.System.getInt(getContext().getContentResolver(), "status_bar_show_battery_percent", 0) != 0) {
            z = true;
        }
        this.mHasPercentage = z;
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
    }

    @Override // androidx.preference.Preference
    public void onDetached() {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        super.onDetached();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(getContext(), str2);
            this.mBlacklist = iconBlacklist;
            this.mBatteryEnabled = !iconBlacklist.contains(this.mBattery);
        }
        if (!this.mHasSetValue) {
            this.mHasSetValue = true;
            if (this.mBatteryEnabled && this.mHasPercentage) {
                setValue("percent");
            } else if (this.mBatteryEnabled) {
                setValue("default");
            } else {
                setValue("disabled");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.preference.Preference
    public boolean persistString(String str) {
        boolean equals = "percent".equals(str);
        MetricsLogger.action(getContext(), 237, equals);
        Settings.System.putInt(getContext().getContentResolver(), "status_bar_show_battery_percent", equals ? 1 : 0);
        if ("disabled".equals(str)) {
            this.mBlacklist.add(this.mBattery);
        } else {
            this.mBlacklist.remove(this.mBattery);
        }
        ((TunerService) Dependency.get(TunerService.class)).setValue("icon_blacklist", TextUtils.join(",", this.mBlacklist));
        return true;
    }
}
