package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DynamicChildBindController_Factory implements Factory<DynamicChildBindController> {
    private final Provider<RowContentBindStage> stageProvider;

    public DynamicChildBindController_Factory(Provider<RowContentBindStage> provider) {
        this.stageProvider = provider;
    }

    @Override // javax.inject.Provider
    public DynamicChildBindController get() {
        return provideInstance(this.stageProvider);
    }

    public static DynamicChildBindController provideInstance(Provider<RowContentBindStage> provider) {
        return new DynamicChildBindController(provider.get());
    }

    public static DynamicChildBindController_Factory create(Provider<RowContentBindStage> provider) {
        return new DynamicChildBindController_Factory(provider);
    }
}
