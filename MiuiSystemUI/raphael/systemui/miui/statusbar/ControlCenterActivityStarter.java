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

    public void startActivity(Intent intent) {
        startActivityDismissKeyguard(intent);
    }

    public void startActivityDismissKeyguard(Intent intent) {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            controlCenter.startActivityDismissingKeyguard(intent);
        }
    }

    public void postStartActivityDismissingKeyguard(Intent intent) {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            controlCenter.postStartActivityDismissingKeyguard(intent);
        }
    }
}
