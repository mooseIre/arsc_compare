package com.android.systemui;

public interface SysUiServiceProvider {
    <T> T getComponent(Class<T> cls);
}
