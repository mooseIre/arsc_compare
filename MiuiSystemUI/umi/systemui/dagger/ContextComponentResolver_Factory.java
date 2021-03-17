package com.android.systemui.dagger;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.RecentsImplementation;
import dagger.internal.Factory;
import java.util.Map;
import javax.inject.Provider;

public final class ContextComponentResolver_Factory implements Factory<ContextComponentResolver> {
    private final Provider<Map<Class<?>, Provider<Activity>>> activityCreatorsProvider;
    private final Provider<Map<Class<?>, Provider<BroadcastReceiver>>> broadcastReceiverCreatorsProvider;
    private final Provider<Map<Class<?>, Provider<RecentsImplementation>>> recentsCreatorsProvider;
    private final Provider<Map<Class<?>, Provider<Service>>> serviceCreatorsProvider;
    private final Provider<Map<Class<?>, Provider<SystemUI>>> systemUICreatorsProvider;

    public ContextComponentResolver_Factory(Provider<Map<Class<?>, Provider<Activity>>> provider, Provider<Map<Class<?>, Provider<Service>>> provider2, Provider<Map<Class<?>, Provider<SystemUI>>> provider3, Provider<Map<Class<?>, Provider<RecentsImplementation>>> provider4, Provider<Map<Class<?>, Provider<BroadcastReceiver>>> provider5) {
        this.activityCreatorsProvider = provider;
        this.serviceCreatorsProvider = provider2;
        this.systemUICreatorsProvider = provider3;
        this.recentsCreatorsProvider = provider4;
        this.broadcastReceiverCreatorsProvider = provider5;
    }

    @Override // javax.inject.Provider
    public ContextComponentResolver get() {
        return provideInstance(this.activityCreatorsProvider, this.serviceCreatorsProvider, this.systemUICreatorsProvider, this.recentsCreatorsProvider, this.broadcastReceiverCreatorsProvider);
    }

    public static ContextComponentResolver provideInstance(Provider<Map<Class<?>, Provider<Activity>>> provider, Provider<Map<Class<?>, Provider<Service>>> provider2, Provider<Map<Class<?>, Provider<SystemUI>>> provider3, Provider<Map<Class<?>, Provider<RecentsImplementation>>> provider4, Provider<Map<Class<?>, Provider<BroadcastReceiver>>> provider5) {
        return new ContextComponentResolver(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static ContextComponentResolver_Factory create(Provider<Map<Class<?>, Provider<Activity>>> provider, Provider<Map<Class<?>, Provider<Service>>> provider2, Provider<Map<Class<?>, Provider<SystemUI>>> provider3, Provider<Map<Class<?>, Provider<RecentsImplementation>>> provider4, Provider<Map<Class<?>, Provider<BroadcastReceiver>>> provider5) {
        return new ContextComponentResolver_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
