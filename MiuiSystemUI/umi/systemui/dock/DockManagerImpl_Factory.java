package com.android.systemui.dock;

import dagger.internal.Factory;

public final class DockManagerImpl_Factory implements Factory<DockManagerImpl> {
    private static final DockManagerImpl_Factory INSTANCE = new DockManagerImpl_Factory();

    @Override // javax.inject.Provider
    public DockManagerImpl get() {
        return provideInstance();
    }

    public static DockManagerImpl provideInstance() {
        return new DockManagerImpl();
    }

    public static DockManagerImpl_Factory create() {
        return INSTANCE;
    }
}
