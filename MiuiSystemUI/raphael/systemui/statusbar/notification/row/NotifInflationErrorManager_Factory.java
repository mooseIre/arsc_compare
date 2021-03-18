package com.android.systemui.statusbar.notification.row;

import dagger.internal.Factory;

public final class NotifInflationErrorManager_Factory implements Factory<NotifInflationErrorManager> {
    private static final NotifInflationErrorManager_Factory INSTANCE = new NotifInflationErrorManager_Factory();

    @Override // javax.inject.Provider
    public NotifInflationErrorManager get() {
        return provideInstance();
    }

    public static NotifInflationErrorManager provideInstance() {
        return new NotifInflationErrorManager();
    }

    public static NotifInflationErrorManager_Factory create() {
        return INSTANCE;
    }
}
