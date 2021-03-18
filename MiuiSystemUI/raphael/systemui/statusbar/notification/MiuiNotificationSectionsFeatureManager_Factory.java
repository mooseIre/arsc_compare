package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.util.DeviceConfigProxy;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiNotificationSectionsFeatureManager_Factory implements Factory<MiuiNotificationSectionsFeatureManager> {
    private final Provider<Context> mContextProvider;
    private final Provider<DeviceConfigProxy> mProxyProvider;

    public MiuiNotificationSectionsFeatureManager_Factory(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        this.mProxyProvider = provider;
        this.mContextProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiNotificationSectionsFeatureManager get() {
        return provideInstance(this.mProxyProvider, this.mContextProvider);
    }

    public static MiuiNotificationSectionsFeatureManager provideInstance(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        return new MiuiNotificationSectionsFeatureManager(provider.get(), provider2.get());
    }

    public static MiuiNotificationSectionsFeatureManager_Factory create(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        return new MiuiNotificationSectionsFeatureManager_Factory(provider, provider2);
    }
}
