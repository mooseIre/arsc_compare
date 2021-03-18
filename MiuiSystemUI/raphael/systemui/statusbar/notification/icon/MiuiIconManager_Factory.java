package com.android.systemui.statusbar.notification.icon;

import android.content.pm.LauncherApps;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiIconManager_Factory implements Factory<MiuiIconManager> {
    private final Provider<IconBuilder> iconBuilderProvider;
    private final Provider<LauncherApps> launcherAppsProvider;
    private final Provider<CommonNotifCollection> notifCollectionProvider;

    public MiuiIconManager_Factory(Provider<CommonNotifCollection> provider, Provider<LauncherApps> provider2, Provider<IconBuilder> provider3) {
        this.notifCollectionProvider = provider;
        this.launcherAppsProvider = provider2;
        this.iconBuilderProvider = provider3;
    }

    @Override // javax.inject.Provider
    public MiuiIconManager get() {
        return provideInstance(this.notifCollectionProvider, this.launcherAppsProvider, this.iconBuilderProvider);
    }

    public static MiuiIconManager provideInstance(Provider<CommonNotifCollection> provider, Provider<LauncherApps> provider2, Provider<IconBuilder> provider3) {
        return new MiuiIconManager(provider.get(), provider2.get(), provider3.get());
    }

    public static MiuiIconManager_Factory create(Provider<CommonNotifCollection> provider, Provider<LauncherApps> provider2, Provider<IconBuilder> provider3) {
        return new MiuiIconManager_Factory(provider, provider2, provider3);
    }
}
