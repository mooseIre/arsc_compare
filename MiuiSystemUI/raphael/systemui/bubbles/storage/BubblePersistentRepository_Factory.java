package com.android.systemui.bubbles.storage;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BubblePersistentRepository_Factory implements Factory<BubblePersistentRepository> {
    private final Provider<Context> contextProvider;

    public BubblePersistentRepository_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public BubblePersistentRepository get() {
        return provideInstance(this.contextProvider);
    }

    public static BubblePersistentRepository provideInstance(Provider<Context> provider) {
        return new BubblePersistentRepository(provider.get());
    }

    public static BubblePersistentRepository_Factory create(Provider<Context> provider) {
        return new BubblePersistentRepository_Factory(provider);
    }
}
