package com.android.systemui.media;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class MediaDataFilter_Factory implements Factory<MediaDataFilter> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<MediaDataCombineLatest> dataSourceProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<NotificationLockscreenUserManager> lockscreenUserManagerProvider;
    private final Provider<MediaDataManager> mediaDataManagerProvider;
    private final Provider<MediaResumeListener> mediaResumeListenerProvider;

    public MediaDataFilter_Factory(Provider<MediaDataCombineLatest> provider, Provider<BroadcastDispatcher> provider2, Provider<MediaResumeListener> provider3, Provider<MediaDataManager> provider4, Provider<NotificationLockscreenUserManager> provider5, Provider<Executor> provider6, Provider<NotificationEntryManager> provider7) {
        this.dataSourceProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.mediaResumeListenerProvider = provider3;
        this.mediaDataManagerProvider = provider4;
        this.lockscreenUserManagerProvider = provider5;
        this.executorProvider = provider6;
        this.entryManagerProvider = provider7;
    }

    @Override // javax.inject.Provider
    public MediaDataFilter get() {
        return provideInstance(this.dataSourceProvider, this.broadcastDispatcherProvider, this.mediaResumeListenerProvider, this.mediaDataManagerProvider, this.lockscreenUserManagerProvider, this.executorProvider, this.entryManagerProvider);
    }

    public static MediaDataFilter provideInstance(Provider<MediaDataCombineLatest> provider, Provider<BroadcastDispatcher> provider2, Provider<MediaResumeListener> provider3, Provider<MediaDataManager> provider4, Provider<NotificationLockscreenUserManager> provider5, Provider<Executor> provider6, Provider<NotificationEntryManager> provider7) {
        return new MediaDataFilter(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get());
    }

    public static MediaDataFilter_Factory create(Provider<MediaDataCombineLatest> provider, Provider<BroadcastDispatcher> provider2, Provider<MediaResumeListener> provider3, Provider<MediaDataManager> provider4, Provider<NotificationLockscreenUserManager> provider5, Provider<Executor> provider6, Provider<NotificationEntryManager> provider7) {
        return new MediaDataFilter_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
