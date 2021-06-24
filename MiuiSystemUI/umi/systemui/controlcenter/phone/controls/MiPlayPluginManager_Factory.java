package com.android.systemui.controlcenter.phone.controls;

import dagger.internal.Factory;

public final class MiPlayPluginManager_Factory implements Factory<MiPlayPluginManager> {
    private static final MiPlayPluginManager_Factory INSTANCE = new MiPlayPluginManager_Factory();

    @Override // javax.inject.Provider
    public MiPlayPluginManager get() {
        return provideInstance();
    }

    public static MiPlayPluginManager provideInstance() {
        return new MiPlayPluginManager();
    }

    public static MiPlayPluginManager_Factory create() {
        return INSTANCE;
    }
}
