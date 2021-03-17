package com.android.systemui.statusbar.notification.row;

import dagger.internal.Factory;

public final class RowInflaterTask_Factory implements Factory<RowInflaterTask> {
    private static final RowInflaterTask_Factory INSTANCE = new RowInflaterTask_Factory();

    @Override // javax.inject.Provider
    public RowInflaterTask get() {
        return provideInstance();
    }

    public static RowInflaterTask provideInstance() {
        return new RowInflaterTask();
    }

    public static RowInflaterTask_Factory create() {
        return INSTANCE;
    }
}
