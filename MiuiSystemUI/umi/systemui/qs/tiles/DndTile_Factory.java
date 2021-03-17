package com.android.systemui.qs.tiles;

import android.content.SharedPreferences;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.ZenModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DndTile_Factory implements Factory<DndTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<SharedPreferences> sharedPreferencesProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public DndTile_Factory(Provider<QSHost> provider, Provider<ZenModeController> provider2, Provider<ActivityStarter> provider3, Provider<BroadcastDispatcher> provider4, Provider<SharedPreferences> provider5) {
        this.hostProvider = provider;
        this.zenModeControllerProvider = provider2;
        this.activityStarterProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.sharedPreferencesProvider = provider5;
    }

    @Override // javax.inject.Provider
    public DndTile get() {
        return provideInstance(this.hostProvider, this.zenModeControllerProvider, this.activityStarterProvider, this.broadcastDispatcherProvider, this.sharedPreferencesProvider);
    }

    public static DndTile provideInstance(Provider<QSHost> provider, Provider<ZenModeController> provider2, Provider<ActivityStarter> provider3, Provider<BroadcastDispatcher> provider4, Provider<SharedPreferences> provider5) {
        return new DndTile(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static DndTile_Factory create(Provider<QSHost> provider, Provider<ZenModeController> provider2, Provider<ActivityStarter> provider3, Provider<BroadcastDispatcher> provider4, Provider<SharedPreferences> provider5) {
        return new DndTile_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
