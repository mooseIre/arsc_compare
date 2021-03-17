package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.ActionClickLogger;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class StatusBarRemoteInputCallback_Factory implements Factory<StatusBarRemoteInputCallback> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<ActionClickLogger> clickLoggerProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<NotificationLockscreenUserManager> notificationLockscreenUserManagerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public StatusBarRemoteInputCallback_Factory(Provider<Context> provider, Provider<NotificationGroupManager> provider2, Provider<NotificationLockscreenUserManager> provider3, Provider<KeyguardStateController> provider4, Provider<StatusBarStateController> provider5, Provider<StatusBarKeyguardViewManager> provider6, Provider<ActivityStarter> provider7, Provider<ShadeController> provider8, Provider<CommandQueue> provider9, Provider<ActionClickLogger> provider10) {
        this.contextProvider = provider;
        this.groupManagerProvider = provider2;
        this.notificationLockscreenUserManagerProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
        this.statusBarStateControllerProvider = provider5;
        this.statusBarKeyguardViewManagerProvider = provider6;
        this.activityStarterProvider = provider7;
        this.shadeControllerProvider = provider8;
        this.commandQueueProvider = provider9;
        this.clickLoggerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public StatusBarRemoteInputCallback get() {
        return provideInstance(this.contextProvider, this.groupManagerProvider, this.notificationLockscreenUserManagerProvider, this.keyguardStateControllerProvider, this.statusBarStateControllerProvider, this.statusBarKeyguardViewManagerProvider, this.activityStarterProvider, this.shadeControllerProvider, this.commandQueueProvider, this.clickLoggerProvider);
    }

    public static StatusBarRemoteInputCallback provideInstance(Provider<Context> provider, Provider<NotificationGroupManager> provider2, Provider<NotificationLockscreenUserManager> provider3, Provider<KeyguardStateController> provider4, Provider<StatusBarStateController> provider5, Provider<StatusBarKeyguardViewManager> provider6, Provider<ActivityStarter> provider7, Provider<ShadeController> provider8, Provider<CommandQueue> provider9, Provider<ActionClickLogger> provider10) {
        return new StatusBarRemoteInputCallback(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static StatusBarRemoteInputCallback_Factory create(Provider<Context> provider, Provider<NotificationGroupManager> provider2, Provider<NotificationLockscreenUserManager> provider3, Provider<KeyguardStateController> provider4, Provider<StatusBarStateController> provider5, Provider<StatusBarKeyguardViewManager> provider6, Provider<ActivityStarter> provider7, Provider<ShadeController> provider8, Provider<CommandQueue> provider9, Provider<ActionClickLogger> provider10) {
        return new StatusBarRemoteInputCallback_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
