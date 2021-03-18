package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationIconObserver_Factory implements Factory<NotificationIconObserver> {
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public NotificationIconObserver_Factory(Provider<Context> provider, Provider<Handler> provider2) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationIconObserver get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider);
    }

    public static NotificationIconObserver provideInstance(Provider<Context> provider, Provider<Handler> provider2) {
        return new NotificationIconObserver(provider.get(), provider2.get());
    }

    public static NotificationIconObserver_Factory create(Provider<Context> provider, Provider<Handler> provider2) {
        return new NotificationIconObserver_Factory(provider, provider2);
    }
}
