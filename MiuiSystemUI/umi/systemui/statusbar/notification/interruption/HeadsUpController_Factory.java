package com.android.systemui.statusbar.notification.interruption;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class HeadsUpController_Factory implements Factory<HeadsUpController> {
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<HeadsUpViewBinder> headsUpViewBinderProvider;
    private final Provider<NotificationInterruptStateProvider> notificationInterruptStateProvider;
    private final Provider<NotificationListener> notificationListenerProvider;
    private final Provider<NotificationRemoteInputManager> remoteInputManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<VisualStabilityManager> visualStabilityManagerProvider;

    public HeadsUpController_Factory(Provider<HeadsUpViewBinder> provider, Provider<NotificationInterruptStateProvider> provider2, Provider<HeadsUpManager> provider3, Provider<NotificationRemoteInputManager> provider4, Provider<StatusBarStateController> provider5, Provider<VisualStabilityManager> provider6, Provider<NotificationListener> provider7) {
        this.headsUpViewBinderProvider = provider;
        this.notificationInterruptStateProvider = provider2;
        this.headsUpManagerProvider = provider3;
        this.remoteInputManagerProvider = provider4;
        this.statusBarStateControllerProvider = provider5;
        this.visualStabilityManagerProvider = provider6;
        this.notificationListenerProvider = provider7;
    }

    @Override // javax.inject.Provider
    public HeadsUpController get() {
        return provideInstance(this.headsUpViewBinderProvider, this.notificationInterruptStateProvider, this.headsUpManagerProvider, this.remoteInputManagerProvider, this.statusBarStateControllerProvider, this.visualStabilityManagerProvider, this.notificationListenerProvider);
    }

    public static HeadsUpController provideInstance(Provider<HeadsUpViewBinder> provider, Provider<NotificationInterruptStateProvider> provider2, Provider<HeadsUpManager> provider3, Provider<NotificationRemoteInputManager> provider4, Provider<StatusBarStateController> provider5, Provider<VisualStabilityManager> provider6, Provider<NotificationListener> provider7) {
        return new HeadsUpController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get());
    }

    public static HeadsUpController_Factory create(Provider<HeadsUpViewBinder> provider, Provider<NotificationInterruptStateProvider> provider2, Provider<HeadsUpManager> provider3, Provider<NotificationRemoteInputManager> provider4, Provider<StatusBarStateController> provider5, Provider<VisualStabilityManager> provider6, Provider<NotificationListener> provider7) {
        return new HeadsUpController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
