package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;

public abstract class ListEntry {
    private final ListAttachState mAttachState = ListAttachState.create();
    int mFirstAddedIteration = -1;
    private final String mKey;
    private final ListAttachState mPreviousAttachState = ListAttachState.create();

    public abstract NotificationEntry getRepresentativeEntry();

    ListEntry(String str) {
        this.mKey = str;
    }

    public String getKey() {
        return this.mKey;
    }

    public GroupEntry getParent() {
        return this.mAttachState.getParent();
    }

    /* access modifiers changed from: package-private */
    public void setParent(GroupEntry groupEntry) {
        this.mAttachState.setParent(groupEntry);
    }

    public int getSection() {
        return this.mAttachState.getSectionIndex();
    }

    public NotifSection getNotifSection() {
        return this.mAttachState.getSection();
    }

    /* access modifiers changed from: package-private */
    public ListAttachState getAttachState() {
        return this.mAttachState;
    }

    /* access modifiers changed from: package-private */
    public ListAttachState getPreviousAttachState() {
        return this.mPreviousAttachState;
    }

    /* access modifiers changed from: package-private */
    public void beginNewAttachState() {
        this.mPreviousAttachState.clone(this.mAttachState);
        this.mAttachState.reset();
    }
}
