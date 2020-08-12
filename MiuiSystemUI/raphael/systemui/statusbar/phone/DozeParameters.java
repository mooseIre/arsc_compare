package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.SystemProperties;
import android.util.MathUtils;
import com.android.internal.hardware.AmbientDisplayConfigurationCompat;
import com.android.systemui.plugins.R;

public class DozeParameters {
    private final AmbientDisplayConfigurationCompat mAmbientDisplayConfigurationCompat = new AmbientDisplayConfigurationCompat(this.mContext);
    private final Context mContext;

    public DozeParameters(Context context) {
        this.mContext = context;
    }

    public int getPulseInDuration(boolean z) {
        if (z) {
            return getInt("doze.pulse.duration.in.pickup", R.integer.doze_pulse_duration_in_pickup);
        }
        return getInt("doze.pulse.duration.in", R.integer.doze_pulse_duration_in);
    }

    public int getPulseVisibleDuration() {
        return getInt("doze.pulse.duration.visible", R.integer.doze_pulse_duration_visible);
    }

    public int getPulseOutDuration() {
        return getInt("doze.pulse.duration.out", R.integer.doze_pulse_duration_out);
    }

    public boolean getAlwaysOn() {
        return this.mAmbientDisplayConfigurationCompat.alwaysOnEnabled(-2);
    }

    private int getInt(String str, int i) {
        return MathUtils.constrain(SystemProperties.getInt(str, this.mContext.getResources().getInteger(i)), 0, 60000);
    }

    public int getPulseVisibleDurationExtended() {
        return getPulseVisibleDuration() * 2;
    }
}
