package com.android.systemui.util.io;

import dagger.internal.Factory;

public final class Files_Factory implements Factory<Files> {
    private static final Files_Factory INSTANCE = new Files_Factory();

    @Override // javax.inject.Provider
    public Files get() {
        return provideInstance();
    }

    public static Files provideInstance() {
        return new Files();
    }

    public static Files_Factory create() {
        return INSTANCE;
    }
}
