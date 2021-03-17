package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AccessibilityManagerWrapper_Factory implements Factory<AccessibilityManagerWrapper> {
    private final Provider<Context> contextProvider;

    public AccessibilityManagerWrapper_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AccessibilityManagerWrapper get() {
        return provideInstance(this.contextProvider);
    }

    public static AccessibilityManagerWrapper provideInstance(Provider<Context> provider) {
        return new AccessibilityManagerWrapper(provider.get());
    }

    public static AccessibilityManagerWrapper_Factory create(Provider<Context> provider) {
        return new AccessibilityManagerWrapper_Factory(provider);
    }
}
