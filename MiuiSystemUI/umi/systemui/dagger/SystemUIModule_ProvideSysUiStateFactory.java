package com.android.systemui.dagger;

import com.android.systemui.model.SysUiState;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class SystemUIModule_ProvideSysUiStateFactory implements Factory<SysUiState> {
    private static final SystemUIModule_ProvideSysUiStateFactory INSTANCE = new SystemUIModule_ProvideSysUiStateFactory();

    @Override // javax.inject.Provider
    public SysUiState get() {
        return provideInstance();
    }

    public static SysUiState provideInstance() {
        return proxyProvideSysUiState();
    }

    public static SystemUIModule_ProvideSysUiStateFactory create() {
        return INSTANCE;
    }

    public static SysUiState proxyProvideSysUiState() {
        SysUiState provideSysUiState = SystemUIModule.provideSysUiState();
        Preconditions.checkNotNull(provideSysUiState, "Cannot return null from a non-@Nullable @Provides method");
        return provideSysUiState;
    }
}
