package com.android.systemui.statusbar.notification.collection;

import dagger.internal.Factory;

public final class NotifViewBarn_Factory implements Factory<NotifViewBarn> {
    private static final NotifViewBarn_Factory INSTANCE = new NotifViewBarn_Factory();

    @Override // javax.inject.Provider
    public NotifViewBarn get() {
        return provideInstance();
    }

    public static NotifViewBarn provideInstance() {
        return new NotifViewBarn();
    }

    public static NotifViewBarn_Factory create() {
        return INSTANCE;
    }
}
