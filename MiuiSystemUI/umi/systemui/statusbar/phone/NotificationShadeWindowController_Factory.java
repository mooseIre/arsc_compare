package com.android.systemui.statusbar.phone;

import android.app.IActivityManager;
import android.content.Context;
import android.view.WindowManager;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationShadeWindowController_Factory implements Factory<NotificationShadeWindowController> {
    private final Provider<IActivityManager> activityManagerProvider;
    private final Provider<SysuiColorExtractor> colorExtractorProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<WindowManager> windowManagerProvider;

    public NotificationShadeWindowController_Factory(Provider<Context> provider, Provider<WindowManager> provider2, Provider<IActivityManager> provider3, Provider<DozeParameters> provider4, Provider<StatusBarStateController> provider5, Provider<ConfigurationController> provider6, Provider<KeyguardViewMediator> provider7, Provider<KeyguardBypassController> provider8, Provider<SysuiColorExtractor> provider9, Provider<DumpManager> provider10) {
        this.contextProvider = provider;
        this.windowManagerProvider = provider2;
        this.activityManagerProvider = provider3;
        this.dozeParametersProvider = provider4;
        this.statusBarStateControllerProvider = provider5;
        this.configurationControllerProvider = provider6;
        this.keyguardViewMediatorProvider = provider7;
        this.keyguardBypassControllerProvider = provider8;
        this.colorExtractorProvider = provider9;
        this.dumpManagerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public NotificationShadeWindowController get() {
        return provideInstance(this.contextProvider, this.windowManagerProvider, this.activityManagerProvider, this.dozeParametersProvider, this.statusBarStateControllerProvider, this.configurationControllerProvider, this.keyguardViewMediatorProvider, this.keyguardBypassControllerProvider, this.colorExtractorProvider, this.dumpManagerProvider);
    }

    public static NotificationShadeWindowController provideInstance(Provider<Context> provider, Provider<WindowManager> provider2, Provider<IActivityManager> provider3, Provider<DozeParameters> provider4, Provider<StatusBarStateController> provider5, Provider<ConfigurationController> provider6, Provider<KeyguardViewMediator> provider7, Provider<KeyguardBypassController> provider8, Provider<SysuiColorExtractor> provider9, Provider<DumpManager> provider10) {
        return new NotificationShadeWindowController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static NotificationShadeWindowController_Factory create(Provider<Context> provider, Provider<WindowManager> provider2, Provider<IActivityManager> provider3, Provider<DozeParameters> provider4, Provider<StatusBarStateController> provider5, Provider<ConfigurationController> provider6, Provider<KeyguardViewMediator> provider7, Provider<KeyguardBypassController> provider8, Provider<SysuiColorExtractor> provider9, Provider<DumpManager> provider10) {
        return new NotificationShadeWindowController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
