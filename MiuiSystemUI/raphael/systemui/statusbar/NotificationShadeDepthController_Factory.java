package com.android.systemui.statusbar;

import android.app.WallpaperManager;
import android.view.Choreographer;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationShadeDepthController_Factory implements Factory<NotificationShadeDepthController> {
    private final Provider<BiometricUnlockController> biometricUnlockControllerProvider;
    private final Provider<BlurUtils> blurUtilsProvider;
    private final Provider<Choreographer> choreographerProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<WallpaperManager> wallpaperManagerProvider;

    public NotificationShadeDepthController_Factory(Provider<StatusBarStateController> provider, Provider<BlurUtils> provider2, Provider<BiometricUnlockController> provider3, Provider<KeyguardStateController> provider4, Provider<Choreographer> provider5, Provider<WallpaperManager> provider6, Provider<NotificationShadeWindowController> provider7, Provider<DozeParameters> provider8, Provider<DumpManager> provider9) {
        this.statusBarStateControllerProvider = provider;
        this.blurUtilsProvider = provider2;
        this.biometricUnlockControllerProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
        this.choreographerProvider = provider5;
        this.wallpaperManagerProvider = provider6;
        this.notificationShadeWindowControllerProvider = provider7;
        this.dozeParametersProvider = provider8;
        this.dumpManagerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public NotificationShadeDepthController get() {
        return provideInstance(this.statusBarStateControllerProvider, this.blurUtilsProvider, this.biometricUnlockControllerProvider, this.keyguardStateControllerProvider, this.choreographerProvider, this.wallpaperManagerProvider, this.notificationShadeWindowControllerProvider, this.dozeParametersProvider, this.dumpManagerProvider);
    }

    public static NotificationShadeDepthController provideInstance(Provider<StatusBarStateController> provider, Provider<BlurUtils> provider2, Provider<BiometricUnlockController> provider3, Provider<KeyguardStateController> provider4, Provider<Choreographer> provider5, Provider<WallpaperManager> provider6, Provider<NotificationShadeWindowController> provider7, Provider<DozeParameters> provider8, Provider<DumpManager> provider9) {
        return new NotificationShadeDepthController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static NotificationShadeDepthController_Factory create(Provider<StatusBarStateController> provider, Provider<BlurUtils> provider2, Provider<BiometricUnlockController> provider3, Provider<KeyguardStateController> provider4, Provider<Choreographer> provider5, Provider<WallpaperManager> provider6, Provider<NotificationShadeWindowController> provider7, Provider<DozeParameters> provider8, Provider<DumpManager> provider9) {
        return new NotificationShadeDepthController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }
}
