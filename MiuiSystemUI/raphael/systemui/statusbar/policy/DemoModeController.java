package com.android.systemui.statusbar.policy;

import android.os.Bundle;

public interface DemoModeController extends CallbackController<DemoModeCallback> {

    public interface DemoModeCallback {
        void onDemoModeChanged(String str, Bundle bundle);
    }
}
