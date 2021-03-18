package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardBypassController_Factory implements Factory<KeyguardBypassController> {
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<NotificationLockscreenUserManager> lockscreenUserManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<TunerService> tunerServiceProvider;

    public KeyguardBypassController_Factory(Provider<Context> provider, Provider<TunerService> provider2, Provider<StatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<KeyguardStateController> provider5, Provider<DumpManager> provider6) {
        this.contextProvider = provider;
        this.tunerServiceProvider = provider2;
        this.statusBarStateControllerProvider = provider3;
        this.lockscreenUserManagerProvider = provider4;
        this.keyguardStateControllerProvider = provider5;
        this.dumpManagerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public KeyguardBypassController get() {
        return provideInstance(this.contextProvider, this.tunerServiceProvider, this.statusBarStateControllerProvider, this.lockscreenUserManagerProvider, this.keyguardStateControllerProvider, this.dumpManagerProvider);
    }

    public static KeyguardBypassController provideInstance(Provider<Context> provider, Provider<TunerService> provider2, Provider<StatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<KeyguardStateController> provider5, Provider<DumpManager> provider6) {
        return new KeyguardBypassController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static KeyguardBypassController_Factory create(Provider<Context> provider, Provider<TunerService> provider2, Provider<StatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<KeyguardStateController> provider5, Provider<DumpManager> provider6) {
        return new KeyguardBypassController_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
