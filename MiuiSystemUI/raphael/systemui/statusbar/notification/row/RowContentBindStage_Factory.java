package com.android.systemui.statusbar.notification.row;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class RowContentBindStage_Factory implements Factory<RowContentBindStage> {
    private final Provider<NotificationRowContentBinder> binderProvider;
    private final Provider<NotifInflationErrorManager> errorManagerProvider;
    private final Provider<RowContentBindStageLogger> loggerProvider;

    public RowContentBindStage_Factory(Provider<NotificationRowContentBinder> provider, Provider<NotifInflationErrorManager> provider2, Provider<RowContentBindStageLogger> provider3) {
        this.binderProvider = provider;
        this.errorManagerProvider = provider2;
        this.loggerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public RowContentBindStage get() {
        return provideInstance(this.binderProvider, this.errorManagerProvider, this.loggerProvider);
    }

    public static RowContentBindStage provideInstance(Provider<NotificationRowContentBinder> provider, Provider<NotifInflationErrorManager> provider2, Provider<RowContentBindStageLogger> provider3) {
        return new RowContentBindStage(provider.get(), provider2.get(), provider3.get());
    }

    public static RowContentBindStage_Factory create(Provider<NotificationRowContentBinder> provider, Provider<NotifInflationErrorManager> provider2, Provider<RowContentBindStageLogger> provider3) {
        return new RowContentBindStage_Factory(provider, provider2, provider3);
    }
}
