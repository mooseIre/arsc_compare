package com.android.systemui;

import android.content.Context;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SizeCompatModeActivityController_Factory implements Factory<SizeCompatModeActivityController> {
    private final Provider<ActivityManagerWrapper> amProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public SizeCompatModeActivityController_Factory(Provider<Context> provider, Provider<ActivityManagerWrapper> provider2, Provider<CommandQueue> provider3) {
        this.contextProvider = provider;
        this.amProvider = provider2;
        this.commandQueueProvider = provider3;
    }

    @Override // javax.inject.Provider
    public SizeCompatModeActivityController get() {
        return provideInstance(this.contextProvider, this.amProvider, this.commandQueueProvider);
    }

    public static SizeCompatModeActivityController provideInstance(Provider<Context> provider, Provider<ActivityManagerWrapper> provider2, Provider<CommandQueue> provider3) {
        return new SizeCompatModeActivityController(provider.get(), provider2.get(), provider3.get());
    }

    public static SizeCompatModeActivityController_Factory create(Provider<Context> provider, Provider<ActivityManagerWrapper> provider2, Provider<CommandQueue> provider3) {
        return new SizeCompatModeActivityController_Factory(provider, provider2, provider3);
    }
}
