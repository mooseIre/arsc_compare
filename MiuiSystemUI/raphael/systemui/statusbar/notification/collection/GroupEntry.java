package com.android.systemui.statusbar.notification.collection;

import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupEntry extends ListEntry {
    public static final GroupEntry ROOT_ENTRY = new GroupEntry("<root>");
    private final List<NotificationEntry> mChildren;
    private NotificationEntry mSummary;
    private final List<NotificationEntry> mUnmodifiableChildren;
    private int mUntruncatedChildCount;

    @VisibleForTesting
    public GroupEntry(String str) {
        super(str);
        ArrayList arrayList = new ArrayList();
        this.mChildren = arrayList;
        this.mUnmodifiableChildren = Collections.unmodifiableList(arrayList);
    }

    @Override // com.android.systemui.statusbar.notification.collection.ListEntry
    public NotificationEntry getRepresentativeEntry() {
        return this.mSummary;
    }

    public NotificationEntry getSummary() {
        return this.mSummary;
    }

    public List<NotificationEntry> getChildren() {
        return this.mUnmodifiableChildren;
    }

    @VisibleForTesting
    public void setSummary(NotificationEntry notificationEntry) {
        this.mSummary = notificationEntry;
    }

    public void setUntruncatedChildCount(int i) {
        this.mUntruncatedChildCount = i;
    }

    public int getUntruncatedChildCount() {
        return this.mUntruncatedChildCount;
    }

    /* access modifiers changed from: package-private */
    public void clearChildren() {
        this.mChildren.clear();
    }

    /* access modifiers changed from: package-private */
    public void addChild(NotificationEntry notificationEntry) {
        this.mChildren.add(notificationEntry);
    }

    /* access modifiers changed from: package-private */
    public void sortChildren(Comparator<? super NotificationEntry> comparator) {
        this.mChildren.sort(comparator);
    }

    /* access modifiers changed from: package-private */
    public List<NotificationEntry> getRawChildren() {
        return this.mChildren;
    }
}
