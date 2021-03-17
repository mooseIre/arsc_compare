package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.systemui.media.MediaHost;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiKeyguardMediaController_Factory implements Factory<MiuiKeyguardMediaController> {
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<MediaHost> mediaHostProvider;
    private final Provider<NotificationLockscreenUserManager> notifLockscreenUserManagerProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;

    public MiuiKeyguardMediaController_Factory(Provider<MediaHost> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4) {
        this.mediaHostProvider = provider;
        this.bypassControllerProvider = provider2;
        this.statusBarStateControllerProvider = provider3;
        this.notifLockscreenUserManagerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public MiuiKeyguardMediaController get() {
        return provideInstance(this.mediaHostProvider, this.bypassControllerProvider, this.statusBarStateControllerProvider, this.notifLockscreenUserManagerProvider);
    }

    public static MiuiKeyguardMediaController provideInstance(Provider<MediaHost> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4) {
        return new MiuiKeyguardMediaController(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static MiuiKeyguardMediaController_Factory create(Provider<MediaHost> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4) {
        return new MiuiKeyguardMediaController_Factory(provider, provider2, provider3, provider4);
    }
}
