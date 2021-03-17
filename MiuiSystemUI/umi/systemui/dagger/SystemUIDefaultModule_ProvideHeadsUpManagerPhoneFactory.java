package com.android.systemui.dagger;

import android.content.Context;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory implements Factory<HeadsUpManagerPhone> {
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory(Provider<Context> provider, Provider<StatusBarStateController> provider2, Provider<KeyguardBypassController> provider3, Provider<NotificationGroupManager> provider4, Provider<ConfigurationController> provider5) {
        this.contextProvider = provider;
        this.statusBarStateControllerProvider = provider2;
        this.bypassControllerProvider = provider3;
        this.groupManagerProvider = provider4;
        this.configurationControllerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public HeadsUpManagerPhone get() {
        return provideInstance(this.contextProvider, this.statusBarStateControllerProvider, this.bypassControllerProvider, this.groupManagerProvider, this.configurationControllerProvider);
    }

    public static HeadsUpManagerPhone provideInstance(Provider<Context> provider, Provider<StatusBarStateController> provider2, Provider<KeyguardBypassController> provider3, Provider<NotificationGroupManager> provider4, Provider<ConfigurationController> provider5) {
        return proxyProvideHeadsUpManagerPhone(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory create(Provider<Context> provider, Provider<StatusBarStateController> provider2, Provider<KeyguardBypassController> provider3, Provider<NotificationGroupManager> provider4, Provider<ConfigurationController> provider5) {
        return new SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory(provider, provider2, provider3, provider4, provider5);
    }

    public static HeadsUpManagerPhone proxyProvideHeadsUpManagerPhone(Context context, StatusBarStateController statusBarStateController, KeyguardBypassController keyguardBypassController, NotificationGroupManager notificationGroupManager, ConfigurationController configurationController) {
        HeadsUpManagerPhone provideHeadsUpManagerPhone = SystemUIDefaultModule.provideHeadsUpManagerPhone(context, statusBarStateController, keyguardBypassController, notificationGroupManager, configurationController);
        Preconditions.checkNotNull(provideHeadsUpManagerPhone, "Cannot return null from a non-@Nullable @Provides method");
        return provideHeadsUpManagerPhone;
    }
}
