package com.android.systemui.statusbar.notification.people;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class PeopleHubViewAdapterImpl_Factory implements Factory<PeopleHubViewAdapterImpl> {
    private final Provider<DataSource<Object>> dataSourceProvider;

    public PeopleHubViewAdapterImpl_Factory(Provider<DataSource<Object>> provider) {
        this.dataSourceProvider = provider;
    }

    @Override // javax.inject.Provider
    public PeopleHubViewAdapterImpl get() {
        return provideInstance(this.dataSourceProvider);
    }

    public static PeopleHubViewAdapterImpl provideInstance(Provider<DataSource<Object>> provider) {
        return new PeopleHubViewAdapterImpl(provider.get());
    }

    public static PeopleHubViewAdapterImpl_Factory create(Provider<DataSource<Object>> provider) {
        return new PeopleHubViewAdapterImpl_Factory(provider);
    }
}
