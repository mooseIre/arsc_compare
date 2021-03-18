package com.android.systemui.tuner;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.util.AttributeSet;
import androidx.preference.SwitchPreference;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.tuner.TunerService;

public class TunerSwitch extends SwitchPreference implements TunerService.Tunable {
    private final int mAction;
    private final boolean mDefault;

    public TunerSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.TunerSwitch);
        this.mDefault = obtainStyledAttributes.getBoolean(R$styleable.TunerSwitch_defValue, false);
        this.mAction = obtainStyledAttributes.getInt(R$styleable.TunerSwitch_metricsAction, -1);
    }

    @Override // androidx.preference.Preference
    public void onAttached() {
        super.onAttached();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, getKey().split(","));
    }

    @Override // androidx.preference.Preference
    public void onDetached() {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        super.onDetached();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        setChecked(TunerService.parseIntegerSwitch(str2, this.mDefault));
    }

    /* access modifiers changed from: protected */
    @Override // androidx.preference.TwoStatePreference, androidx.preference.Preference
    public void onClick() {
        super.onClick();
        if (this.mAction != -1) {
            MetricsLogger.action(getContext(), this.mAction, isChecked());
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.preference.Preference
    public boolean persistBoolean(boolean z) {
        for (String str : getKey().split(",")) {
            Settings.Secure.putString(getContext().getContentResolver(), str, z ? "1" : "0");
        }
        return true;
    }
}
