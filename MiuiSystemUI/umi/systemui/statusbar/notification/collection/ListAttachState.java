package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ListAttachState.kt */
public final class ListAttachState {
    public static final Companion Companion = new Companion(null);
    @Nullable
    private NotifFilter excludingFilter;
    @Nullable
    private GroupEntry parent;
    @Nullable
    private NotifPromoter promoter;
    @Nullable
    private NotifSection section;
    private int sectionIndex;

    @NotNull
    public static final ListAttachState create() {
        return Companion.create();
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ListAttachState)) {
            return false;
        }
        ListAttachState listAttachState = (ListAttachState) obj;
        return Intrinsics.areEqual(this.parent, listAttachState.parent) && Intrinsics.areEqual(this.section, listAttachState.section) && this.sectionIndex == listAttachState.sectionIndex && Intrinsics.areEqual(this.excludingFilter, listAttachState.excludingFilter) && Intrinsics.areEqual(this.promoter, listAttachState.promoter);
    }

    public int hashCode() {
        GroupEntry groupEntry = this.parent;
        int i = 0;
        int hashCode = (groupEntry != null ? groupEntry.hashCode() : 0) * 31;
        NotifSection notifSection = this.section;
        int hashCode2 = (((hashCode + (notifSection != null ? notifSection.hashCode() : 0)) * 31) + Integer.hashCode(this.sectionIndex)) * 31;
        NotifFilter notifFilter = this.excludingFilter;
        int hashCode3 = (hashCode2 + (notifFilter != null ? notifFilter.hashCode() : 0)) * 31;
        NotifPromoter notifPromoter = this.promoter;
        if (notifPromoter != null) {
            i = notifPromoter.hashCode();
        }
        return hashCode3 + i;
    }

    @NotNull
    public String toString() {
        return "ListAttachState(parent=" + this.parent + ", section=" + this.section + ", sectionIndex=" + this.sectionIndex + ", excludingFilter=" + this.excludingFilter + ", promoter=" + this.promoter + ")";
    }

    private ListAttachState(GroupEntry groupEntry, NotifSection notifSection, int i, NotifFilter notifFilter, NotifPromoter notifPromoter) {
        this.parent = groupEntry;
        this.section = notifSection;
        this.sectionIndex = i;
        this.excludingFilter = notifFilter;
        this.promoter = notifPromoter;
    }

    public /* synthetic */ ListAttachState(GroupEntry groupEntry, NotifSection notifSection, int i, NotifFilter notifFilter, NotifPromoter notifPromoter, DefaultConstructorMarker defaultConstructorMarker) {
        this(groupEntry, notifSection, i, notifFilter, notifPromoter);
    }

    @Nullable
    public final GroupEntry getParent() {
        return this.parent;
    }

    public final void setParent(@Nullable GroupEntry groupEntry) {
        this.parent = groupEntry;
    }

    @Nullable
    public final NotifSection getSection() {
        return this.section;
    }

    public final void setSection(@Nullable NotifSection notifSection) {
        this.section = notifSection;
    }

    public final int getSectionIndex() {
        return this.sectionIndex;
    }

    public final void setSectionIndex(int i) {
        this.sectionIndex = i;
    }

    @Nullable
    public final NotifFilter getExcludingFilter() {
        return this.excludingFilter;
    }

    public final void setExcludingFilter(@Nullable NotifFilter notifFilter) {
        this.excludingFilter = notifFilter;
    }

    @Nullable
    public final NotifPromoter getPromoter() {
        return this.promoter;
    }

    public final void setPromoter(@Nullable NotifPromoter notifPromoter) {
        this.promoter = notifPromoter;
    }

    public final void clone(@NotNull ListAttachState listAttachState) {
        Intrinsics.checkParameterIsNotNull(listAttachState, "other");
        this.parent = listAttachState.parent;
        this.section = listAttachState.section;
        this.sectionIndex = listAttachState.sectionIndex;
        this.excludingFilter = listAttachState.excludingFilter;
        this.promoter = listAttachState.promoter;
    }

    public final void reset() {
        this.parent = null;
        this.section = null;
        this.sectionIndex = -1;
        this.excludingFilter = null;
        this.promoter = null;
    }

    /* compiled from: ListAttachState.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final ListAttachState create() {
            return new ListAttachState(null, null, -1, null, null, null);
        }
    }
}
