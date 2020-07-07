package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class ConfigurationChangedEvent extends RecentsEventBus.AnimatedEvent {
    public final boolean fromDeviceOrientationChange;
    public final boolean fromDisplayDensityChange;
    public final boolean fromMultiWindow;

    public ConfigurationChangedEvent(boolean z, boolean z2, boolean z3, boolean z4) {
        this.fromMultiWindow = z;
        this.fromDeviceOrientationChange = z2;
        this.fromDisplayDensityChange = z3;
    }
}
