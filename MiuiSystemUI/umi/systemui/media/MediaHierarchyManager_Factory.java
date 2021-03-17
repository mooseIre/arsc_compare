package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaHierarchyManager_Factory implements Factory<MediaHierarchyManager> {
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<MediaCarouselController> mediaCarouselControllerProvider;
    private final Provider<NotificationLockscreenUserManager> notifLockscreenUserManagerProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public MediaHierarchyManager_Factory(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<KeyguardStateController> provider3, Provider<KeyguardBypassController> provider4, Provider<MediaCarouselController> provider5, Provider<NotificationLockscreenUserManager> provider6, Provider<WakefulnessLifecycle> provider7) {
        this.contextProvider = provider;
        this.statusBarStateControllerProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
        this.bypassControllerProvider = provider4;
        this.mediaCarouselControllerProvider = provider5;
        this.notifLockscreenUserManagerProvider = provider6;
        this.wakefulnessLifecycleProvider = provider7;
    }

    @Override // javax.inject.Provider
    public MediaHierarchyManager get() {
        return provideInstance(this.contextProvider, this.statusBarStateControllerProvider, this.keyguardStateControllerProvider, this.bypassControllerProvider, this.mediaCarouselControllerProvider, this.notifLockscreenUserManagerProvider, this.wakefulnessLifecycleProvider);
    }

    public static MediaHierarchyManager provideInstance(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<KeyguardStateController> provider3, Provider<KeyguardBypassController> provider4, Provider<MediaCarouselController> provider5, Provider<NotificationLockscreenUserManager> provider6, Provider<WakefulnessLifecycle> provider7) {
        return new MediaHierarchyManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get());
    }

    public static MediaHierarchyManager_Factory create(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<KeyguardStateController> provider3, Provider<KeyguardBypassController> provider4, Provider<MediaCarouselController> provider5, Provider<NotificationLockscreenUserManager> provider6, Provider<WakefulnessLifecycle> provider7) {
        return new MediaHierarchyManager_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
