package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;

public interface SilentModeObserverController extends CallbackController<SilentModeListener>, Dumpable {

    public interface SilentModeListener {
        void onSilentModeChanged(boolean z);
    }
}
