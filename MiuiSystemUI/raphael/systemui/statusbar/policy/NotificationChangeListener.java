package com.android.systemui.statusbar.policy;

public interface NotificationChangeListener {
    void onAdd(String str);

    void onClearAll();

    void onDelete(String str);

    void onUpdate(String str);
}
