package com.android.systemui.dagger;

import dagger.internal.Factory;

public final class SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory implements Factory<Boolean> {
    private static final SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory INSTANCE = new SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory();

    @Override // javax.inject.Provider
    public Boolean get() {
        return provideInstance();
    }

    public static Boolean provideInstance() {
        proxyProvideAllowNotificationLongPress();
        return Boolean.TRUE;
    }

    public static SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory create() {
        return INSTANCE;
    }

    public static boolean proxyProvideAllowNotificationLongPress() {
        return SystemUIDefaultModule.provideAllowNotificationLongPress();
    }
}
