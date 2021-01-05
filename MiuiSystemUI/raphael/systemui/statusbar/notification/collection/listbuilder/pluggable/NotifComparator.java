package com.android.systemui.statusbar.notification.collection.listbuilder.pluggable;

import com.android.systemui.statusbar.notification.collection.ListEntry;
import java.util.Comparator;

public abstract class NotifComparator extends Pluggable<NotifComparator> implements Comparator<ListEntry> {
    public abstract int compare(ListEntry listEntry, ListEntry listEntry2);
}
