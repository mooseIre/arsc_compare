package com.android.systemui.dump;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemUIAuxiliaryDumpService_Factory implements Factory<SystemUIAuxiliaryDumpService> {
    private final Provider<DumpHandler> dumpHandlerProvider;

    public SystemUIAuxiliaryDumpService_Factory(Provider<DumpHandler> provider) {
        this.dumpHandlerProvider = provider;
    }

    @Override // javax.inject.Provider
    public SystemUIAuxiliaryDumpService get() {
        return provideInstance(this.dumpHandlerProvider);
    }

    public static SystemUIAuxiliaryDumpService provideInstance(Provider<DumpHandler> provider) {
        return new SystemUIAuxiliaryDumpService(provider.get());
    }

    public static SystemUIAuxiliaryDumpService_Factory create(Provider<DumpHandler> provider) {
        return new SystemUIAuxiliaryDumpService_Factory(provider);
    }
}
