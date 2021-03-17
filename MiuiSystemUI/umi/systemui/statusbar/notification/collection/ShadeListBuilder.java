package com.android.systemui.statusbar.notification.collection;

import android.util.ArrayMap;
import android.util.Pair;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.notification.collection.listbuilder.OnBeforeFinalizeFilterListener;
import com.android.systemui.statusbar.notification.collection.listbuilder.OnBeforeRenderListListener;
import com.android.systemui.statusbar.notification.collection.listbuilder.OnBeforeSortListener;
import com.android.systemui.statusbar.notification.collection.listbuilder.OnBeforeTransformGroupsListener;
import com.android.systemui.statusbar.notification.collection.listbuilder.PipelineState;
import com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifComparator;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.Pluggable;
import com.android.systemui.statusbar.notification.collection.notifcollection.CollectionReadyForBuildListener;
import com.android.systemui.util.Assert;
import com.android.systemui.util.time.SystemClock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class ShadeListBuilder implements Dumpable {
    private static final Comparator<NotificationEntry> sChildComparator = $$Lambda$ShadeListBuilder$c6onOLMSwF5woQjUCc8sv1YwJM.INSTANCE;
    private static final NotifSection sDefaultSection = new NotifSection("DefaultSection") {
        /* class com.android.systemui.statusbar.notification.collection.ShadeListBuilder.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection
        public boolean isInSection(ListEntry listEntry) {
            return true;
        }
    };
    private Collection<NotificationEntry> mAllEntries = Collections.emptyList();
    private final Map<String, GroupEntry> mGroups = new ArrayMap();
    private final NotificationInteractionTracker mInteractionTracker;
    private int mIterationCount = 0;
    private final ShadeListBuilderLogger mLogger;
    private List<ListEntry> mNewNotifList = new ArrayList();
    private final List<NotifComparator> mNotifComparators = new ArrayList();
    private final List<NotifFilter> mNotifFinalizeFilters = new ArrayList();
    private List<ListEntry> mNotifList = new ArrayList();
    private final List<NotifFilter> mNotifPreGroupFilters = new ArrayList();
    private final List<NotifPromoter> mNotifPromoters = new ArrayList();
    private final List<NotifSection> mNotifSections = new ArrayList();
    private final List<OnBeforeFinalizeFilterListener> mOnBeforeFinalizeFilterListeners = new ArrayList();
    private final List<OnBeforeRenderListListener> mOnBeforeRenderListListeners = new ArrayList();
    private final List<OnBeforeSortListener> mOnBeforeSortListeners = new ArrayList();
    private final List<OnBeforeTransformGroupsListener> mOnBeforeTransformGroupsListeners = new ArrayList();
    private OnRenderListListener mOnRenderListListener;
    private final PipelineState mPipelineState = new PipelineState();
    private List<ListEntry> mReadOnlyNewNotifList = Collections.unmodifiableList(this.mNewNotifList);
    private List<ListEntry> mReadOnlyNotifList = Collections.unmodifiableList(this.mNotifList);
    private final CollectionReadyForBuildListener mReadyForBuildListener = new CollectionReadyForBuildListener() {
        /* class com.android.systemui.statusbar.notification.collection.ShadeListBuilder.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.CollectionReadyForBuildListener
        public void onBuildList(Collection<NotificationEntry> collection) {
            Assert.isMainThread();
            ShadeListBuilder.this.mPipelineState.requireIsBefore(1);
            ShadeListBuilder.this.mLogger.logOnBuildList();
            ShadeListBuilder.this.mAllEntries = collection;
            ShadeListBuilder.this.buildList();
        }
    };
    private final SystemClock mSystemClock;
    private final Comparator<ListEntry> mTopLevelComparator = new Comparator() {
        /* class com.android.systemui.statusbar.notification.collection.$$Lambda$ShadeListBuilder$j4Y9Xdxb2bsigQJC_JntCQwmx4 */

        @Override // java.util.Comparator
        public final int compare(Object obj, Object obj2) {
            return ShadeListBuilder.this.lambda$new$2$ShadeListBuilder((ListEntry) obj, (ListEntry) obj2);
        }
    };

    public interface OnRenderListListener {
        void onRenderList(List<ListEntry> list);
    }

    public ShadeListBuilder(SystemClock systemClock, ShadeListBuilderLogger shadeListBuilderLogger, DumpManager dumpManager, NotificationInteractionTracker notificationInteractionTracker) {
        Assert.isMainThread();
        this.mSystemClock = systemClock;
        this.mLogger = shadeListBuilderLogger;
        this.mInteractionTracker = notificationInteractionTracker;
        dumpManager.registerDumpable("NotifListBuilderImpl", this);
    }

    public void attach(NotifCollection notifCollection) {
        Assert.isMainThread();
        notifCollection.setBuildListener(this.mReadyForBuildListener);
    }

    public void setOnRenderListListener(OnRenderListListener onRenderListListener) {
        Assert.isMainThread();
        this.mPipelineState.requireState(0);
        this.mOnRenderListListener = onRenderListListener;
    }

    /* access modifiers changed from: package-private */
    public void addOnBeforeFinalizeFilterListener(OnBeforeFinalizeFilterListener onBeforeFinalizeFilterListener) {
        Assert.isMainThread();
        this.mPipelineState.requireState(0);
        this.mOnBeforeFinalizeFilterListeners.add(onBeforeFinalizeFilterListener);
    }

    /* access modifiers changed from: package-private */
    public void addPreGroupFilter(NotifFilter notifFilter) {
        Assert.isMainThread();
        this.mPipelineState.requireState(0);
        this.mNotifPreGroupFilters.add(notifFilter);
        notifFilter.setInvalidationListener(new Pluggable.PluggableListener() {
            /* class com.android.systemui.statusbar.notification.collection.$$Lambda$ShadeListBuilder$nY0ibCyaSPniz4LEX1W2bWrRcs */

            @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.Pluggable.PluggableListener
            public final void onPluggableInvalidated(Object obj) {
                ShadeListBuilder.this.onPreGroupFilterInvalidated((NotifFilter) obj);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void addFinalizeFilter(NotifFilter notifFilter) {
        Assert.isMainThread();
        this.mPipelineState.requireState(0);
        this.mNotifFinalizeFilters.add(notifFilter);
        notifFilter.setInvalidationListener(new Pluggable.PluggableListener() {
            /* class com.android.systemui.statusbar.notification.collection.$$Lambda$ShadeListBuilder$xeAx9GATmY7ZgJZ0F6oEQlc0G_0 */

            @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.Pluggable.PluggableListener
            public final void onPluggableInvalidated(Object obj) {
                ShadeListBuilder.this.onFinalizeFilterInvalidated((NotifFilter) obj);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void addPromoter(NotifPromoter notifPromoter) {
        Assert.isMainThread();
        this.mPipelineState.requireState(0);
        this.mNotifPromoters.add(notifPromoter);
        notifPromoter.setInvalidationListener(new Pluggable.PluggableListener() {
            /* class com.android.systemui.statusbar.notification.collection.$$Lambda$ShadeListBuilder$WhP4dzR4yYnVTR1LdzWTnz4ov9k */

            @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.Pluggable.PluggableListener
            public final void onPluggableInvalidated(Object obj) {
                ShadeListBuilder.this.onPromoterInvalidated((NotifPromoter) obj);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void setSections(List<NotifSection> list) {
        Assert.isMainThread();
        this.mPipelineState.requireState(0);
        this.mNotifSections.clear();
        for (NotifSection notifSection : list) {
            this.mNotifSections.add(notifSection);
            notifSection.setInvalidationListener(new Pluggable.PluggableListener() {
                /* class com.android.systemui.statusbar.notification.collection.$$Lambda$ShadeListBuilder$bhojRXQ6IzMsuyeOmu4rRbLGws */

                @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.Pluggable.PluggableListener
                public final void onPluggableInvalidated(Object obj) {
                    ShadeListBuilder.this.onNotifSectionInvalidated((NotifSection) obj);
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public List<ListEntry> getShadeList() {
        Assert.isMainThread();
        return this.mReadOnlyNotifList;
    }

    /* access modifiers changed from: private */
    public void onPreGroupFilterInvalidated(NotifFilter notifFilter) {
        Assert.isMainThread();
        this.mLogger.logPreGroupFilterInvalidated(notifFilter.getName(), this.mPipelineState.getState());
        rebuildListIfBefore(3);
    }

    /* access modifiers changed from: private */
    public void onPromoterInvalidated(NotifPromoter notifPromoter) {
        Assert.isMainThread();
        this.mLogger.logPromoterInvalidated(notifPromoter.getName(), this.mPipelineState.getState());
        rebuildListIfBefore(5);
    }

    /* access modifiers changed from: private */
    public void onNotifSectionInvalidated(NotifSection notifSection) {
        Assert.isMainThread();
        this.mLogger.logNotifSectionInvalidated(notifSection.getName(), this.mPipelineState.getState());
        rebuildListIfBefore(6);
    }

    /* access modifiers changed from: private */
    public void onFinalizeFilterInvalidated(NotifFilter notifFilter) {
        Assert.isMainThread();
        this.mLogger.logFinalizeFilterInvalidated(notifFilter.getName(), this.mPipelineState.getState());
        rebuildListIfBefore(7);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void buildList() {
        this.mPipelineState.requireIsBefore(1);
        this.mPipelineState.setState(1);
        this.mPipelineState.incrementTo(2);
        resetNotifs();
        this.mPipelineState.incrementTo(3);
        filterNotifs(this.mAllEntries, this.mNotifList, this.mNotifPreGroupFilters);
        this.mPipelineState.incrementTo(4);
        groupNotifs(this.mNotifList, this.mNewNotifList);
        applyNewNotifList();
        pruneIncompleteGroups(this.mNotifList);
        dispatchOnBeforeTransformGroups(this.mReadOnlyNotifList);
        this.mPipelineState.incrementTo(5);
        promoteNotifs(this.mNotifList);
        pruneIncompleteGroups(this.mNotifList);
        dispatchOnBeforeSort(this.mReadOnlyNotifList);
        this.mPipelineState.incrementTo(6);
        sortList();
        dispatchOnBeforeFinalizeFilter(this.mReadOnlyNotifList);
        this.mPipelineState.incrementTo(7);
        filterNotifs(this.mNotifList, this.mNewNotifList, this.mNotifFinalizeFilters);
        applyNewNotifList();
        pruneIncompleteGroups(this.mNotifList);
        this.mPipelineState.incrementTo(8);
        logChanges();
        freeEmptyGroups();
        dispatchOnBeforeRenderList(this.mReadOnlyNotifList);
        OnRenderListListener onRenderListListener = this.mOnRenderListListener;
        if (onRenderListListener != null) {
            onRenderListListener.onRenderList(this.mReadOnlyNotifList);
        }
        this.mLogger.logEndBuildList(this.mIterationCount, this.mReadOnlyNotifList.size(), countChildren(this.mReadOnlyNotifList));
        if (this.mIterationCount % 10 == 0) {
            this.mLogger.logFinalList(this.mNotifList);
        }
        this.mPipelineState.setState(0);
        this.mIterationCount++;
    }

    private void applyNewNotifList() {
        this.mNotifList.clear();
        List<ListEntry> list = this.mNotifList;
        this.mNotifList = this.mNewNotifList;
        this.mNewNotifList = list;
        List<ListEntry> list2 = this.mReadOnlyNotifList;
        this.mReadOnlyNotifList = this.mReadOnlyNewNotifList;
        this.mReadOnlyNewNotifList = list2;
    }

    private void resetNotifs() {
        for (GroupEntry groupEntry : this.mGroups.values()) {
            groupEntry.beginNewAttachState();
            groupEntry.clearChildren();
            groupEntry.setSummary(null);
        }
        for (NotificationEntry notificationEntry : this.mAllEntries) {
            notificationEntry.beginNewAttachState();
            if (notificationEntry.mFirstAddedIteration == -1) {
                notificationEntry.mFirstAddedIteration = this.mIterationCount;
            }
        }
        this.mNotifList.clear();
    }

    private void filterNotifs(Collection<? extends ListEntry> collection, List<ListEntry> list, List<NotifFilter> list2) {
        long uptimeMillis = this.mSystemClock.uptimeMillis();
        for (ListEntry listEntry : collection) {
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                NotificationEntry representativeEntry = groupEntry.getRepresentativeEntry();
                if (applyFilters(representativeEntry, uptimeMillis, list2)) {
                    groupEntry.setSummary(null);
                    annulAddition(representativeEntry);
                }
                List<NotificationEntry> rawChildren = groupEntry.getRawChildren();
                for (int size = rawChildren.size() - 1; size >= 0; size--) {
                    NotificationEntry notificationEntry = rawChildren.get(size);
                    if (applyFilters(notificationEntry, uptimeMillis, list2)) {
                        rawChildren.remove(notificationEntry);
                        annulAddition(notificationEntry);
                    }
                }
                list.add(groupEntry);
            } else if (applyFilters((NotificationEntry) listEntry, uptimeMillis, list2)) {
                annulAddition(listEntry);
            } else {
                list.add(listEntry);
            }
        }
    }

    private void groupNotifs(List<ListEntry> list, List<ListEntry> list2) {
        Iterator<ListEntry> it = list.iterator();
        while (it.hasNext()) {
            NotificationEntry notificationEntry = (NotificationEntry) it.next();
            if (notificationEntry.getSbn().isGroup()) {
                String groupKey = notificationEntry.getSbn().getGroupKey();
                GroupEntry groupEntry = this.mGroups.get(groupKey);
                if (groupEntry == null) {
                    groupEntry = new GroupEntry(groupKey);
                    groupEntry.mFirstAddedIteration = this.mIterationCount;
                    this.mGroups.put(groupKey, groupEntry);
                }
                if (groupEntry.getParent() == null) {
                    groupEntry.setParent(GroupEntry.ROOT_ENTRY);
                    list2.add(groupEntry);
                }
                notificationEntry.setParent(groupEntry);
                if (notificationEntry.getSbn().getNotification().isGroupSummary()) {
                    NotificationEntry summary = groupEntry.getSummary();
                    if (summary == null) {
                        groupEntry.setSummary(notificationEntry);
                    } else {
                        this.mLogger.logDuplicateSummary(this.mIterationCount, groupEntry.getKey(), summary.getKey(), notificationEntry.getKey());
                        if (notificationEntry.getSbn().getPostTime() > summary.getSbn().getPostTime()) {
                            groupEntry.setSummary(notificationEntry);
                            annulAddition(summary, list2);
                        } else {
                            annulAddition(notificationEntry, list2);
                        }
                    }
                } else {
                    groupEntry.addChild(notificationEntry);
                }
            } else {
                String key = notificationEntry.getKey();
                if (this.mGroups.containsKey(key)) {
                    this.mLogger.logDuplicateTopLevelKey(this.mIterationCount, key);
                } else {
                    notificationEntry.setParent(GroupEntry.ROOT_ENTRY);
                    list2.add(notificationEntry);
                }
            }
        }
    }

    private void promoteNotifs(List<ListEntry> list) {
        for (int i = 0; i < list.size(); i++) {
            ListEntry listEntry = list.get(i);
            if (listEntry instanceof GroupEntry) {
                ((GroupEntry) listEntry).getRawChildren().removeIf(new Predicate(list) {
                    /* class com.android.systemui.statusbar.notification.collection.$$Lambda$ShadeListBuilder$ePmhZ1cn_RHisgrq179QhMPgfM */
                    public final /* synthetic */ List f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return ShadeListBuilder.this.lambda$promoteNotifs$0$ShadeListBuilder(this.f$1, (NotificationEntry) obj);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$promoteNotifs$0 */
    public /* synthetic */ boolean lambda$promoteNotifs$0$ShadeListBuilder(List list, NotificationEntry notificationEntry) {
        boolean applyTopLevelPromoters = applyTopLevelPromoters(notificationEntry);
        if (applyTopLevelPromoters) {
            notificationEntry.setParent(GroupEntry.ROOT_ENTRY);
            list.add(notificationEntry);
        }
        return applyTopLevelPromoters;
    }

    private void pruneIncompleteGroups(List<ListEntry> list) {
        int i = 0;
        while (i < list.size()) {
            ListEntry listEntry = list.get(i);
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                List<NotificationEntry> rawChildren = groupEntry.getRawChildren();
                if (groupEntry.getSummary() != null && rawChildren.size() == 0) {
                    list.remove(i);
                    i--;
                    NotificationEntry summary = groupEntry.getSummary();
                    summary.setParent(GroupEntry.ROOT_ENTRY);
                    list.add(summary);
                    groupEntry.setSummary(null);
                    annulAddition(groupEntry, list);
                } else if (groupEntry.getSummary() == null || rawChildren.size() < 2) {
                    list.remove(i);
                    i--;
                    if (groupEntry.getSummary() != null) {
                        ListEntry summary2 = groupEntry.getSummary();
                        groupEntry.setSummary(null);
                        annulAddition(summary2, list);
                    }
                    for (int i2 = 0; i2 < rawChildren.size(); i2++) {
                        NotificationEntry notificationEntry = rawChildren.get(i2);
                        notificationEntry.setParent(GroupEntry.ROOT_ENTRY);
                        list.add(notificationEntry);
                    }
                    rawChildren.clear();
                    annulAddition(groupEntry, list);
                }
            }
            i++;
        }
    }

    private void annulAddition(ListEntry listEntry, List<ListEntry> list) {
        if (listEntry.getParent() == null || listEntry.mFirstAddedIteration == -1) {
            throw new IllegalStateException("Cannot nullify addition of " + listEntry.getKey() + ": no such addition. (" + listEntry.getParent() + " " + listEntry.mFirstAddedIteration + ")");
        } else if (listEntry.getParent() != GroupEntry.ROOT_ENTRY || !list.contains(listEntry)) {
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                if (groupEntry.getSummary() != null) {
                    throw new IllegalStateException("Cannot nullify group " + groupEntry.getKey() + ": summary is not null");
                } else if (!groupEntry.getChildren().isEmpty()) {
                    throw new IllegalStateException("Cannot nullify group " + groupEntry.getKey() + ": still has children");
                }
            } else if ((listEntry instanceof NotificationEntry) && (listEntry == listEntry.getParent().getSummary() || listEntry.getParent().getChildren().contains(listEntry))) {
                throw new IllegalStateException("Cannot nullify addition of child " + listEntry.getKey() + ": it's still attached to its parent.");
            }
            annulAddition(listEntry);
        } else {
            throw new IllegalStateException("Cannot nullify addition of " + listEntry.getKey() + ": it's still in the shade list.");
        }
    }

    private void annulAddition(ListEntry listEntry) {
        listEntry.setParent(null);
        listEntry.getAttachState().setSectionIndex(-1);
        listEntry.getAttachState().setSection(null);
        listEntry.getAttachState().setPromoter(null);
        if (listEntry.mFirstAddedIteration == this.mIterationCount) {
            listEntry.mFirstAddedIteration = -1;
        }
    }

    private void sortList() {
        for (ListEntry listEntry : this.mNotifList) {
            Pair<NotifSection, Integer> applySections = applySections(listEntry);
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                for (NotificationEntry notificationEntry : groupEntry.getChildren()) {
                    notificationEntry.getAttachState().setSection((NotifSection) applySections.first);
                    notificationEntry.getAttachState().setSectionIndex(((Integer) applySections.second).intValue());
                }
                groupEntry.sortChildren(sChildComparator);
            }
        }
        this.mNotifList.sort(this.mTopLevelComparator);
    }

    private void freeEmptyGroups() {
        this.mGroups.values().removeIf($$Lambda$ShadeListBuilder$pkfdgVYB9WxpGP4Dl92u_QCynaw.INSTANCE);
    }

    static /* synthetic */ boolean lambda$freeEmptyGroups$1(GroupEntry groupEntry) {
        return groupEntry.getSummary() == null && groupEntry.getChildren().isEmpty();
    }

    private void logChanges() {
        for (NotificationEntry notificationEntry : this.mAllEntries) {
            logAttachStateChanges(notificationEntry);
        }
        for (GroupEntry groupEntry : this.mGroups.values()) {
            logAttachStateChanges(groupEntry);
        }
    }

    private void logAttachStateChanges(ListEntry listEntry) {
        ListAttachState attachState = listEntry.getAttachState();
        ListAttachState previousAttachState = listEntry.getPreviousAttachState();
        if (!Objects.equals(attachState, previousAttachState)) {
            this.mLogger.logEntryAttachStateChanged(this.mIterationCount, listEntry.getKey(), previousAttachState.getParent(), attachState.getParent());
            if (attachState.getParent() != previousAttachState.getParent()) {
                this.mLogger.logParentChanged(this.mIterationCount, previousAttachState.getParent(), attachState.getParent());
            }
            if (attachState.getExcludingFilter() != previousAttachState.getExcludingFilter()) {
                this.mLogger.logFilterChanged(this.mIterationCount, previousAttachState.getExcludingFilter(), attachState.getExcludingFilter());
            }
            boolean z = attachState.getParent() == null && previousAttachState.getParent() != null;
            if (!z && attachState.getPromoter() != previousAttachState.getPromoter()) {
                this.mLogger.logPromoterChanged(this.mIterationCount, previousAttachState.getPromoter(), attachState.getPromoter());
            }
            if (!z && attachState.getSection() != previousAttachState.getSection()) {
                this.mLogger.logSectionChanged(this.mIterationCount, previousAttachState.getSection(), previousAttachState.getSectionIndex(), attachState.getSection(), attachState.getSectionIndex());
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ int lambda$new$2$ShadeListBuilder(ListEntry listEntry, ListEntry listEntry2) {
        int compare = Integer.compare(listEntry.getSection(), listEntry2.getSection());
        if (compare == 0) {
            int i = 0;
            while (i < this.mNotifComparators.size() && (compare = this.mNotifComparators.get(i).compare(listEntry, listEntry2)) == 0) {
                i++;
            }
        }
        NotificationEntry representativeEntry = listEntry.getRepresentativeEntry();
        NotificationEntry representativeEntry2 = listEntry2.getRepresentativeEntry();
        if (compare == 0) {
            compare = representativeEntry.getRanking().getRank() - representativeEntry2.getRanking().getRank();
        }
        return compare == 0 ? Long.compare(representativeEntry2.getSbn().getNotification().when, representativeEntry.getSbn().getNotification().when) : compare;
    }

    static /* synthetic */ int lambda$static$3(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
        int rank = notificationEntry.getRanking().getRank() - notificationEntry2.getRanking().getRank();
        return rank == 0 ? Long.compare(notificationEntry2.getSbn().getNotification().when, notificationEntry.getSbn().getNotification().when) : rank;
    }

    private boolean applyFilters(NotificationEntry notificationEntry, long j, List<NotifFilter> list) {
        NotifFilter findRejectingFilter = findRejectingFilter(notificationEntry, j, list);
        notificationEntry.getAttachState().setExcludingFilter(findRejectingFilter);
        if (findRejectingFilter != null) {
            notificationEntry.resetInitializationTime();
        }
        return findRejectingFilter != null;
    }

    private static NotifFilter findRejectingFilter(NotificationEntry notificationEntry, long j, List<NotifFilter> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            NotifFilter notifFilter = list.get(i);
            if (notifFilter.shouldFilterOut(notificationEntry, j)) {
                return notifFilter;
            }
        }
        return null;
    }

    private boolean applyTopLevelPromoters(NotificationEntry notificationEntry) {
        NotifPromoter findPromoter = findPromoter(notificationEntry);
        notificationEntry.getAttachState().setPromoter(findPromoter);
        return findPromoter != null;
    }

    private NotifPromoter findPromoter(NotificationEntry notificationEntry) {
        for (int i = 0; i < this.mNotifPromoters.size(); i++) {
            NotifPromoter notifPromoter = this.mNotifPromoters.get(i);
            if (notifPromoter.shouldPromoteToTopLevel(notificationEntry)) {
                return notifPromoter;
            }
        }
        return null;
    }

    private Pair<NotifSection, Integer> applySections(ListEntry listEntry) {
        Pair<NotifSection, Integer> findSection = findSection(listEntry);
        listEntry.getAttachState().setSection((NotifSection) findSection.first);
        listEntry.getAttachState().setSectionIndex(((Integer) findSection.second).intValue());
        return findSection;
    }

    private Pair<NotifSection, Integer> findSection(ListEntry listEntry) {
        for (int i = 0; i < this.mNotifSections.size(); i++) {
            NotifSection notifSection = this.mNotifSections.get(i);
            if (notifSection.isInSection(listEntry)) {
                return new Pair<>(notifSection, Integer.valueOf(i));
            }
        }
        return new Pair<>(sDefaultSection, Integer.valueOf(this.mNotifSections.size()));
    }

    private void rebuildListIfBefore(int i) {
        this.mPipelineState.requireIsBefore(i);
        if (this.mPipelineState.is(0)) {
            buildList();
        }
    }

    private static int countChildren(List<ListEntry> list) {
        int i = 0;
        for (int i2 = 0; i2 < list.size(); i2++) {
            ListEntry listEntry = list.get(i2);
            if (listEntry instanceof GroupEntry) {
                i += ((GroupEntry) listEntry).getChildren().size();
            }
        }
        return i;
    }

    private void dispatchOnBeforeTransformGroups(List<ListEntry> list) {
        for (int i = 0; i < this.mOnBeforeTransformGroupsListeners.size(); i++) {
            this.mOnBeforeTransformGroupsListeners.get(i).onBeforeTransformGroups(list);
        }
    }

    private void dispatchOnBeforeSort(List<ListEntry> list) {
        for (int i = 0; i < this.mOnBeforeSortListeners.size(); i++) {
            this.mOnBeforeSortListeners.get(i).onBeforeSort(list);
        }
    }

    private void dispatchOnBeforeFinalizeFilter(List<ListEntry> list) {
        for (int i = 0; i < this.mOnBeforeFinalizeFilterListeners.size(); i++) {
            this.mOnBeforeFinalizeFilterListeners.get(i).onBeforeFinalizeFilter(list);
        }
    }

    private void dispatchOnBeforeRenderList(List<ListEntry> list) {
        for (int i = 0; i < this.mOnBeforeRenderListListeners.size(); i++) {
            this.mOnBeforeRenderListListeners.get(i).onBeforeRenderList(list);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("\tNotifListBuilderImpl shade notifications:");
        if (getShadeList().size() == 0) {
            printWriter.println("\t\t None");
        }
        printWriter.println(ListDumper.dumpTree(getShadeList(), this.mInteractionTracker, true, "\t\t"));
    }
}
