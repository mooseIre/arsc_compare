package com.android.systemui.media;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaDataCombineLatest_Factory implements Factory<MediaDataCombineLatest> {
    private final Provider<MediaDataManager> dataSourceProvider;
    private final Provider<MediaDeviceManager> deviceSourceProvider;

    public MediaDataCombineLatest_Factory(Provider<MediaDataManager> provider, Provider<MediaDeviceManager> provider2) {
        this.dataSourceProvider = provider;
        this.deviceSourceProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MediaDataCombineLatest get() {
        return provideInstance(this.dataSourceProvider, this.deviceSourceProvider);
    }

    public static MediaDataCombineLatest provideInstance(Provider<MediaDataManager> provider, Provider<MediaDeviceManager> provider2) {
        return new MediaDataCombineLatest(provider.get(), provider2.get());
    }

    public static MediaDataCombineLatest_Factory create(Provider<MediaDataManager> provider, Provider<MediaDeviceManager> provider2) {
        return new MediaDataCombineLatest_Factory(provider, provider2);
    }
}
