package com.android.systemui.tuner;

import dagger.internal.Factory;

public final class TunerActivity_Factory implements Factory<TunerActivity> {
    private static final TunerActivity_Factory INSTANCE = new TunerActivity_Factory();

    @Override // javax.inject.Provider
    public TunerActivity get() {
        return provideInstance();
    }

    public static TunerActivity provideInstance() {
        return new TunerActivity();
    }

    public static TunerActivity_Factory create() {
        return INSTANCE;
    }
}
