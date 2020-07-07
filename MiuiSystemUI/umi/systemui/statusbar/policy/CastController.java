package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;

public interface CastController extends CallbackController<Callback>, Dumpable {

    public interface Callback {
        void onCastDevicesChanged();
    }
}
