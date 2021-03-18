package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.dagger.ContextComponentHelper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class RecentsModule_ProvideRecentsImplFactory implements Factory<RecentsImplementation> {
    private final Provider<ContextComponentHelper> componentHelperProvider;
    private final Provider<Context> contextProvider;

    public RecentsModule_ProvideRecentsImplFactory(Provider<Context> provider, Provider<ContextComponentHelper> provider2) {
        this.contextProvider = provider;
        this.componentHelperProvider = provider2;
    }

    @Override // javax.inject.Provider
    public RecentsImplementation get() {
        return provideInstance(this.contextProvider, this.componentHelperProvider);
    }

    public static RecentsImplementation provideInstance(Provider<Context> provider, Provider<ContextComponentHelper> provider2) {
        return proxyProvideRecentsImpl(provider.get(), provider2.get());
    }

    public static RecentsModule_ProvideRecentsImplFactory create(Provider<Context> provider, Provider<ContextComponentHelper> provider2) {
        return new RecentsModule_ProvideRecentsImplFactory(provider, provider2);
    }

    public static RecentsImplementation proxyProvideRecentsImpl(Context context, ContextComponentHelper contextComponentHelper) {
        RecentsImplementation provideRecentsImpl = RecentsModule.provideRecentsImpl(context, contextComponentHelper);
        Preconditions.checkNotNull(provideRecentsImpl, "Cannot return null from a non-@Nullable @Provides method");
        return provideRecentsImpl;
    }
}
