package com.android.systemui.media;

import com.android.systemui.util.concurrency.RepeatableExecutor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SeekBarViewModel_Factory implements Factory<SeekBarViewModel> {
    private final Provider<RepeatableExecutor> bgExecutorProvider;

    public SeekBarViewModel_Factory(Provider<RepeatableExecutor> provider) {
        this.bgExecutorProvider = provider;
    }

    @Override // javax.inject.Provider
    public SeekBarViewModel get() {
        return provideInstance(this.bgExecutorProvider);
    }

    public static SeekBarViewModel provideInstance(Provider<RepeatableExecutor> provider) {
        return new SeekBarViewModel(provider.get());
    }

    public static SeekBarViewModel_Factory create(Provider<RepeatableExecutor> provider) {
        return new SeekBarViewModel_Factory(provider);
    }
}
