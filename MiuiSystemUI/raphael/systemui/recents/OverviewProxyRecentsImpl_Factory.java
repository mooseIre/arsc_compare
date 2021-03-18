package com.android.systemui.recents;

import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

public final class OverviewProxyRecentsImpl_Factory implements Factory<OverviewProxyRecentsImpl> {
    private final Provider<Optional<Divider>> dividerOptionalProvider;
    private final Provider<Optional<Lazy<StatusBar>>> statusBarLazyProvider;

    public OverviewProxyRecentsImpl_Factory(Provider<Optional<Lazy<StatusBar>>> provider, Provider<Optional<Divider>> provider2) {
        this.statusBarLazyProvider = provider;
        this.dividerOptionalProvider = provider2;
    }

    @Override // javax.inject.Provider
    public OverviewProxyRecentsImpl get() {
        return provideInstance(this.statusBarLazyProvider, this.dividerOptionalProvider);
    }

    public static OverviewProxyRecentsImpl provideInstance(Provider<Optional<Lazy<StatusBar>>> provider, Provider<Optional<Divider>> provider2) {
        return new OverviewProxyRecentsImpl(provider.get(), provider2.get());
    }

    public static OverviewProxyRecentsImpl_Factory create(Provider<Optional<Lazy<StatusBar>>> provider, Provider<Optional<Divider>> provider2) {
        return new OverviewProxyRecentsImpl_Factory(provider, provider2);
    }
}
