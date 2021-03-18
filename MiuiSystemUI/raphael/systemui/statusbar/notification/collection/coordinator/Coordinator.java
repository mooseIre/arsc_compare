package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;

public interface Coordinator {
    void attach(NotifPipeline notifPipeline);

    default NotifSection getSection() {
        return null;
    }
}
