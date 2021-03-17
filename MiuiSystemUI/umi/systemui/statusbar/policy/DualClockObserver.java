package com.android.systemui.statusbar.policy;

import java.util.ArrayList;

public class DualClockObserver {
    private ArrayList<Callback> mCallbacks = new ArrayList<>();
    private boolean mShowDualClock = false;

    public interface Callback {
        void onDualShowClockChanged(boolean z);
    }

    public void setShowDualClock(boolean z) {
        if (this.mShowDualClock != z) {
            this.mShowDualClock = z;
            fireShowDualClockChanged();
        }
    }

    public void addCallback(Callback callback) {
        if (!this.mCallbacks.contains(callback)) {
            this.mCallbacks.add(callback);
            callback.onDualShowClockChanged(this.mShowDualClock);
        }
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public void fireShowDualClockChanged() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onDualShowClockChanged(this.mShowDualClock);
        }
    }
}
