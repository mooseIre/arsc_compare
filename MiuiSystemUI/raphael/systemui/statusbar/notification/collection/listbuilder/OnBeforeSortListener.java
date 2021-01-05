package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.statusbar.notification.collection.ListEntry;
import java.util.List;

public interface OnBeforeSortListener {
    void onBeforeSort(List<ListEntry> list);
}
