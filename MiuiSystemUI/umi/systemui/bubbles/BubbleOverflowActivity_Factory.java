package com.android.systemui.bubbles;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class BubbleOverflowActivity_Factory implements Factory<BubbleOverflowActivity> {
    private final Provider<BubbleController> controllerProvider;

    public BubbleOverflowActivity_Factory(Provider<BubbleController> provider) {
        this.controllerProvider = provider;
    }

    @Override // javax.inject.Provider
    public BubbleOverflowActivity get() {
        return provideInstance(this.controllerProvider);
    }

    public static BubbleOverflowActivity provideInstance(Provider<BubbleController> provider) {
        return new BubbleOverflowActivity(provider.get());
    }

    public static BubbleOverflowActivity_Factory create(Provider<BubbleController> provider) {
        return new BubbleOverflowActivity_Factory(provider);
    }
}
