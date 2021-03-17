package com.android.systemui.statusbar.notification.row;

import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import dagger.internal.Factory;

public final class PriorityOnboardingDialogController_Builder_Factory implements Factory<PriorityOnboardingDialogController.Builder> {
    private static final PriorityOnboardingDialogController_Builder_Factory INSTANCE = new PriorityOnboardingDialogController_Builder_Factory();

    @Override // javax.inject.Provider
    public PriorityOnboardingDialogController.Builder get() {
        return provideInstance();
    }

    public static PriorityOnboardingDialogController.Builder provideInstance() {
        return new PriorityOnboardingDialogController.Builder();
    }

    public static PriorityOnboardingDialogController_Builder_Factory create() {
        return INSTANCE;
    }
}
