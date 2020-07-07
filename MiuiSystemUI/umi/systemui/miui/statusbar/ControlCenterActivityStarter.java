package com.android.systemui.miui.statusbar;

import android.content.Context;
import android.content.Intent;

public class ControlCenterActivityStarter {
    protected ControlCenter mControlCenter;

    public ControlCenterActivityStarter(Context context) {
    }

    public void setControlCenter(ControlCenter controlCenter) {
        this.mControlCenter = controlCenter;
    }

    public void postStartActivityDismissingKeyguard(Intent intent) {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            controlCenter.postStartActivityDismissingKeyguard(intent);
        }
    }
}
