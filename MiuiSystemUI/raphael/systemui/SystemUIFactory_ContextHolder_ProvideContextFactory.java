package com.android.systemui;

import android.content.Context;
import com.android.systemui.SystemUIFactory;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class SystemUIFactory_ContextHolder_ProvideContextFactory implements Factory<Context> {
    private final SystemUIFactory.ContextHolder module;

    public SystemUIFactory_ContextHolder_ProvideContextFactory(SystemUIFactory.ContextHolder contextHolder) {
        this.module = contextHolder;
    }

    @Override // javax.inject.Provider
    public Context get() {
        return provideInstance(this.module);
    }

    public static Context provideInstance(SystemUIFactory.ContextHolder contextHolder) {
        return proxyProvideContext(contextHolder);
    }

    public static SystemUIFactory_ContextHolder_ProvideContextFactory create(SystemUIFactory.ContextHolder contextHolder) {
        return new SystemUIFactory_ContextHolder_ProvideContextFactory(contextHolder);
    }

    public static Context proxyProvideContext(SystemUIFactory.ContextHolder contextHolder) {
        Context provideContext = contextHolder.provideContext();
        Preconditions.checkNotNull(provideContext, "Cannot return null from a non-@Nullable @Provides method");
        return provideContext;
    }
}
