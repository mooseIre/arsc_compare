package com.android.systemui.bubbles;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BubbleData_Factory implements Factory<BubbleData> {
    private final Provider<Context> contextProvider;

    public BubbleData_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public BubbleData get() {
        return provideInstance(this.contextProvider);
    }

    public static BubbleData provideInstance(Provider<Context> provider) {
        return new BubbleData(provider.get());
    }

    public static BubbleData_Factory create(Provider<Context> provider) {
        return new BubbleData_Factory(provider);
    }
}
