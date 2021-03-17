package com.android.systemui.power;

import dagger.internal.Factory;

public final class EnhancedEstimatesImpl_Factory implements Factory<EnhancedEstimatesImpl> {
    private static final EnhancedEstimatesImpl_Factory INSTANCE = new EnhancedEstimatesImpl_Factory();

    @Override // javax.inject.Provider
    public EnhancedEstimatesImpl get() {
        return provideInstance();
    }

    public static EnhancedEstimatesImpl provideInstance() {
        return new EnhancedEstimatesImpl();
    }

    public static EnhancedEstimatesImpl_Factory create() {
        return INSTANCE;
    }
}
