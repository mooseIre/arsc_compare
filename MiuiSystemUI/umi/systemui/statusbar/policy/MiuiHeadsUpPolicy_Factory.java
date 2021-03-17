package com.android.systemui.statusbar.policy;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiHeadsUpPolicy_Factory implements Factory<MiuiHeadsUpPolicy> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;

    public MiuiHeadsUpPolicy_Factory(Provider<BroadcastDispatcher> provider, Provider<HeadsUpManagerPhone> provider2) {
        this.broadcastDispatcherProvider = provider;
        this.headsUpManagerPhoneProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiHeadsUpPolicy get() {
        return provideInstance(this.broadcastDispatcherProvider, this.headsUpManagerPhoneProvider);
    }

    public static MiuiHeadsUpPolicy provideInstance(Provider<BroadcastDispatcher> provider, Provider<HeadsUpManagerPhone> provider2) {
        return new MiuiHeadsUpPolicy(provider.get(), provider2.get());
    }

    public static MiuiHeadsUpPolicy_Factory create(Provider<BroadcastDispatcher> provider, Provider<HeadsUpManagerPhone> provider2) {
        return new MiuiHeadsUpPolicy_Factory(provider, provider2);
    }
}
