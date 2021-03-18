package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import com.android.systemui.media.MediaViewController;
import com.android.systemui.media.SeekBarViewModel;
import com.android.systemui.plugins.ActivityStarter;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class MiuiMediaControlPanel_Factory implements Factory<MiuiMediaControlPanel> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<MiuiMediaTransferManager> mediaTransferManagerProvider;
    private final Provider<MediaViewController> mediaViewControllerProvider;
    private final Provider<SeekBarViewModel> seekBarViewModelProvider;

    public MiuiMediaControlPanel_Factory(Provider<Context> provider, Provider<Executor> provider2, Provider<ActivityStarter> provider3, Provider<MediaViewController> provider4, Provider<SeekBarViewModel> provider5, Provider<MiuiMediaTransferManager> provider6) {
        this.contextProvider = provider;
        this.backgroundExecutorProvider = provider2;
        this.activityStarterProvider = provider3;
        this.mediaViewControllerProvider = provider4;
        this.seekBarViewModelProvider = provider5;
        this.mediaTransferManagerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public MiuiMediaControlPanel get() {
        return provideInstance(this.contextProvider, this.backgroundExecutorProvider, this.activityStarterProvider, this.mediaViewControllerProvider, this.seekBarViewModelProvider, this.mediaTransferManagerProvider);
    }

    public static MiuiMediaControlPanel provideInstance(Provider<Context> provider, Provider<Executor> provider2, Provider<ActivityStarter> provider3, Provider<MediaViewController> provider4, Provider<SeekBarViewModel> provider5, Provider<MiuiMediaTransferManager> provider6) {
        return new MiuiMediaControlPanel(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static MiuiMediaControlPanel_Factory create(Provider<Context> provider, Provider<Executor> provider2, Provider<ActivityStarter> provider3, Provider<MediaViewController> provider4, Provider<SeekBarViewModel> provider5, Provider<MiuiMediaTransferManager> provider6) {
        return new MiuiMediaControlPanel_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
