package com.android.systemui.controlcenter.phone.controls;

import dagger.internal.Factory;

public final class ControlsPluginManager_Factory implements Factory<ControlsPluginManager> {
    private static final ControlsPluginManager_Factory INSTANCE = new ControlsPluginManager_Factory();

    public ControlsPluginManager get() {
        return provideInstance();
    }

    public static ControlsPluginManager provideInstance() {
        return new ControlsPluginManager();
    }

    public static ControlsPluginManager_Factory create() {
        return INSTANCE;
    }
}
