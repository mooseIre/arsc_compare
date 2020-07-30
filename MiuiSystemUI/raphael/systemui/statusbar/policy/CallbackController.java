package com.android.systemui.statusbar.policy;

public interface CallbackController<T> {
    void addCallback(T t);

    void removeCallback(T t);
}
