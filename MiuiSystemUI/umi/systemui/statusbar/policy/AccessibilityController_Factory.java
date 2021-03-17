package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AccessibilityController_Factory implements Factory<AccessibilityController> {
    private final Provider<Context> contextProvider;

    public AccessibilityController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AccessibilityController get() {
        return provideInstance(this.contextProvider);
    }

    public static AccessibilityController provideInstance(Provider<Context> provider) {
        return new AccessibilityController(provider.get());
    }

    public static AccessibilityController_Factory create(Provider<Context> provider) {
        return new AccessibilityController_Factory(provider);
    }
}
