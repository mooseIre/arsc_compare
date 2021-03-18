package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UserInfoControllerImpl_Factory implements Factory<UserInfoControllerImpl> {
    private final Provider<Context> contextProvider;

    public UserInfoControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public UserInfoControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static UserInfoControllerImpl provideInstance(Provider<Context> provider) {
        return new UserInfoControllerImpl(provider.get());
    }

    public static UserInfoControllerImpl_Factory create(Provider<Context> provider) {
        return new UserInfoControllerImpl_Factory(provider);
    }
}
