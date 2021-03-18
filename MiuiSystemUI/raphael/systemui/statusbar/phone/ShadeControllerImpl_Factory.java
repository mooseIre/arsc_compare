package com.android.systemui.statusbar.phone;

import android.view.WindowManager;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ShadeControllerImpl_Factory implements Factory<ShadeControllerImpl> {
    private final Provider<AssistManager> assistManagerLazyProvider;
    private final Provider<BubbleController> bubbleControllerLazyProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBar> statusBarLazyProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<WindowManager> windowManagerProvider;

    public ShadeControllerImpl_Factory(Provider<CommandQueue> provider, Provider<StatusBarStateController> provider2, Provider<NotificationShadeWindowController> provider3, Provider<StatusBarKeyguardViewManager> provider4, Provider<WindowManager> provider5, Provider<StatusBar> provider6, Provider<AssistManager> provider7, Provider<BubbleController> provider8) {
        this.commandQueueProvider = provider;
        this.statusBarStateControllerProvider = provider2;
        this.notificationShadeWindowControllerProvider = provider3;
        this.statusBarKeyguardViewManagerProvider = provider4;
        this.windowManagerProvider = provider5;
        this.statusBarLazyProvider = provider6;
        this.assistManagerLazyProvider = provider7;
        this.bubbleControllerLazyProvider = provider8;
    }

    @Override // javax.inject.Provider
    public ShadeControllerImpl get() {
        return provideInstance(this.commandQueueProvider, this.statusBarStateControllerProvider, this.notificationShadeWindowControllerProvider, this.statusBarKeyguardViewManagerProvider, this.windowManagerProvider, this.statusBarLazyProvider, this.assistManagerLazyProvider, this.bubbleControllerLazyProvider);
    }

    public static ShadeControllerImpl provideInstance(Provider<CommandQueue> provider, Provider<StatusBarStateController> provider2, Provider<NotificationShadeWindowController> provider3, Provider<StatusBarKeyguardViewManager> provider4, Provider<WindowManager> provider5, Provider<StatusBar> provider6, Provider<AssistManager> provider7, Provider<BubbleController> provider8) {
        return new ShadeControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), DoubleCheck.lazy(provider6), DoubleCheck.lazy(provider7), DoubleCheck.lazy(provider8));
    }

    public static ShadeControllerImpl_Factory create(Provider<CommandQueue> provider, Provider<StatusBarStateController> provider2, Provider<NotificationShadeWindowController> provider3, Provider<StatusBarKeyguardViewManager> provider4, Provider<WindowManager> provider5, Provider<StatusBar> provider6, Provider<AssistManager> provider7, Provider<BubbleController> provider8) {
        return new ShadeControllerImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
