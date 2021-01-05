package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.statusbar.notification.collection.ListEntry;
import java.util.List;

public interface OnBeforeTransformGroupsListener {
    void onBeforeTransformGroups(List<ListEntry> list);
}
