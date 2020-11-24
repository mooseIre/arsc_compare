package com.android.systemui.statusbar.notification.collection.listbuilder.pluggable;

import com.android.systemui.statusbar.notification.collection.ListEntry;

public abstract class NotifSection extends Pluggable<NotifSection> {
    public abstract boolean isInSection(ListEntry listEntry);

    protected NotifSection(String str) {
        super(str);
    }
}
