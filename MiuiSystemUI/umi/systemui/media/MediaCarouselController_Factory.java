package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaCarouselController_Factory implements Factory<MediaCarouselController> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DelayableExecutor> executorProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<MiuiMediaControlPanel> mediaControlPanelFactoryProvider;
    private final Provider<MediaHostStatesManager> mediaHostStatesManagerProvider;
    private final Provider<MediaDataFilter> mediaManagerProvider;
    private final Provider<VisualStabilityManager> visualStabilityManagerProvider;

    public MediaCarouselController_Factory(Provider<Context> provider, Provider<MiuiMediaControlPanel> provider2, Provider<VisualStabilityManager> provider3, Provider<MediaHostStatesManager> provider4, Provider<ActivityStarter> provider5, Provider<DelayableExecutor> provider6, Provider<MediaDataFilter> provider7, Provider<ConfigurationController> provider8, Provider<FalsingManager> provider9) {
        this.contextProvider = provider;
        this.mediaControlPanelFactoryProvider = provider2;
        this.visualStabilityManagerProvider = provider3;
        this.mediaHostStatesManagerProvider = provider4;
        this.activityStarterProvider = provider5;
        this.executorProvider = provider6;
        this.mediaManagerProvider = provider7;
        this.configurationControllerProvider = provider8;
        this.falsingManagerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public MediaCarouselController get() {
        return provideInstance(this.contextProvider, this.mediaControlPanelFactoryProvider, this.visualStabilityManagerProvider, this.mediaHostStatesManagerProvider, this.activityStarterProvider, this.executorProvider, this.mediaManagerProvider, this.configurationControllerProvider, this.falsingManagerProvider);
    }

    public static MediaCarouselController provideInstance(Provider<Context> provider, Provider<MiuiMediaControlPanel> provider2, Provider<VisualStabilityManager> provider3, Provider<MediaHostStatesManager> provider4, Provider<ActivityStarter> provider5, Provider<DelayableExecutor> provider6, Provider<MediaDataFilter> provider7, Provider<ConfigurationController> provider8, Provider<FalsingManager> provider9) {
        return new MediaCarouselController(provider.get(), provider2, provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get());
    }

    public static MediaCarouselController_Factory create(Provider<Context> provider, Provider<MiuiMediaControlPanel> provider2, Provider<VisualStabilityManager> provider3, Provider<MediaHostStatesManager> provider4, Provider<ActivityStarter> provider5, Provider<DelayableExecutor> provider6, Provider<MediaDataFilter> provider7, Provider<ConfigurationController> provider8, Provider<FalsingManager> provider9) {
        return new MediaCarouselController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }
}
