package com.android.systemui.bubbles;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0016R$integer;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleLogger;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BubbleData {
    private static final Comparator<Bubble> BUBBLES_BY_SORT_KEY_DESCENDING = Comparator.comparing($$Lambda$BubbleData$vPZCImnk7rTPTX1c7nr0PX7FO2o.INSTANCE).reversed();
    private final List<Bubble> mBubbles;
    private BubbleController.PendingIntentCanceledListener mCancelledListener;
    private final Context mContext;
    private boolean mExpanded;
    private Listener mListener;
    private BubbleLogger mLogger = new BubbleLoggerImpl();
    private final int mMaxBubbles;
    private int mMaxOverflowBubbles;
    private final List<Bubble> mOverflowBubbles;
    private final HashMap<String, Bubble> mPendingBubbles;
    private Bubble mSelectedBubble;
    private boolean mShowingOverflow;
    private Update mStateChange;
    private HashMap<String, String> mSuppressedGroupKeys = new HashMap<>();
    private BubbleController.NotificationSuppressionChangedListener mSuppressionListener;
    private TimeSource mTimeSource = $$Lambda$0E0fwzH9SS6aB9lL5npMzupI4Q.INSTANCE;

    /* access modifiers changed from: package-private */
    public interface Listener {
        void applyUpdate(Update update);
    }

    /* access modifiers changed from: package-private */
    public interface TimeSource {
        long currentTimeMillis();
    }

    /* access modifiers changed from: package-private */
    public static final class Update {
        Bubble addedBubble;
        final List<Bubble> bubbles;
        boolean expanded;
        boolean expandedChanged;
        boolean orderChanged;
        final List<Pair<Bubble, Integer>> removedBubbles;
        Bubble selectedBubble;
        boolean selectionChanged;
        Bubble updatedBubble;

        private Update(List<Bubble> list, List<Bubble> list2) {
            this.removedBubbles = new ArrayList();
            this.bubbles = Collections.unmodifiableList(list);
            Collections.unmodifiableList(list2);
        }

        /* access modifiers changed from: package-private */
        public boolean anythingChanged() {
            return this.expandedChanged || this.selectionChanged || this.addedBubble != null || this.updatedBubble != null || !this.removedBubbles.isEmpty() || this.orderChanged;
        }

        /* access modifiers changed from: package-private */
        public void bubbleRemoved(Bubble bubble, int i) {
            this.removedBubbles.add(new Pair<>(bubble, Integer.valueOf(i)));
        }
    }

    public BubbleData(Context context) {
        this.mContext = context;
        this.mBubbles = new ArrayList();
        this.mOverflowBubbles = new ArrayList();
        this.mPendingBubbles = new HashMap<>();
        this.mStateChange = new Update(this.mBubbles, this.mOverflowBubbles);
        this.mMaxBubbles = this.mContext.getResources().getInteger(C0016R$integer.bubbles_max_rendered);
        this.mMaxOverflowBubbles = this.mContext.getResources().getInteger(C0016R$integer.bubbles_max_overflow);
    }

    public void setSuppressionChangedListener(BubbleController.NotificationSuppressionChangedListener notificationSuppressionChangedListener) {
        this.mSuppressionListener = notificationSuppressionChangedListener;
    }

    public void setPendingIntentCancelledListener(BubbleController.PendingIntentCanceledListener pendingIntentCanceledListener) {
        this.mCancelledListener = pendingIntentCanceledListener;
    }

    public boolean hasBubbles() {
        return !this.mBubbles.isEmpty();
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public boolean hasAnyBubbleWithKey(String str) {
        return hasBubbleInStackWithKey(str) || hasOverflowBubbleWithKey(str);
    }

    public boolean hasBubbleInStackWithKey(String str) {
        return getBubbleInStackWithKey(str) != null;
    }

    public boolean hasOverflowBubbleWithKey(String str) {
        return getOverflowBubbleWithKey(str) != null;
    }

    public Bubble getSelectedBubble() {
        return this.mSelectedBubble;
    }

    public void setExpanded(boolean z) {
        setExpandedInternal(z);
        dispatchPendingChanges();
    }

    public void setSelectedBubble(Bubble bubble) {
        setSelectedBubbleInternal(bubble);
        dispatchPendingChanges();
    }

    /* access modifiers changed from: package-private */
    public void setShowingOverflow(boolean z) {
        this.mShowingOverflow = z;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.bubbles.Bubble getOrCreateBubble(com.android.systemui.statusbar.notification.collection.NotificationEntry r4, com.android.systemui.bubbles.Bubble r5) {
        /*
            r3 = this;
            if (r4 == 0) goto L_0x0007
            java.lang.String r0 = r4.getKey()
            goto L_0x000b
        L_0x0007:
            java.lang.String r0 = r5.getKey()
        L_0x000b:
            com.android.systemui.bubbles.Bubble r1 = r3.getBubbleInStackWithKey(r0)
            if (r1 != 0) goto L_0x003a
            com.android.systemui.bubbles.Bubble r1 = r3.getOverflowBubbleWithKey(r0)
            if (r1 == 0) goto L_0x001d
            java.util.List<com.android.systemui.bubbles.Bubble> r5 = r3.mOverflowBubbles
            r5.remove(r1)
            goto L_0x003a
        L_0x001d:
            java.util.HashMap<java.lang.String, com.android.systemui.bubbles.Bubble> r1 = r3.mPendingBubbles
            boolean r1 = r1.containsKey(r0)
            if (r1 == 0) goto L_0x002e
            java.util.HashMap<java.lang.String, com.android.systemui.bubbles.Bubble> r5 = r3.mPendingBubbles
            java.lang.Object r5 = r5.get(r0)
            com.android.systemui.bubbles.Bubble r5 = (com.android.systemui.bubbles.Bubble) r5
            goto L_0x003b
        L_0x002e:
            if (r4 == 0) goto L_0x003b
            com.android.systemui.bubbles.Bubble r5 = new com.android.systemui.bubbles.Bubble
            com.android.systemui.bubbles.BubbleController$NotificationSuppressionChangedListener r1 = r3.mSuppressionListener
            com.android.systemui.bubbles.BubbleController$PendingIntentCanceledListener r2 = r3.mCancelledListener
            r5.<init>(r4, r1, r2)
            goto L_0x003b
        L_0x003a:
            r5 = r1
        L_0x003b:
            if (r4 == 0) goto L_0x0040
            r5.setEntry(r4)
        L_0x0040:
            java.util.HashMap<java.lang.String, com.android.systemui.bubbles.Bubble> r3 = r3.mPendingBubbles
            r3.put(r0, r5)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.BubbleData.getOrCreateBubble(com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.bubbles.Bubble):com.android.systemui.bubbles.Bubble");
    }

    /* access modifiers changed from: package-private */
    public void notificationEntryUpdated(Bubble bubble, boolean z, boolean z2) {
        this.mPendingBubbles.remove(bubble.getKey());
        Bubble bubbleInStackWithKey = getBubbleInStackWithKey(bubble.getKey());
        boolean z3 = z | (!bubble.isVisuallyInterruptive());
        if (bubbleInStackWithKey == null) {
            bubble.setSuppressFlyout(z3);
            doAdd(bubble);
            trim();
        } else {
            bubble.setSuppressFlyout(z3);
            doUpdate(bubble);
        }
        boolean z4 = false;
        if (bubble.shouldAutoExpand()) {
            bubble.setShouldAutoExpand(false);
            setSelectedBubbleInternal(bubble);
            if (!this.mExpanded) {
                setExpandedInternal(true);
            }
        }
        boolean z5 = this.mExpanded && this.mSelectedBubble == bubble;
        if (z5 || !z2 || !bubble.showInShade()) {
            z4 = true;
        }
        bubble.setSuppressNotification(z4);
        bubble.setShowDot(!z5);
        dispatchPendingChanges();
    }

    public void dismissBubbleWithKey(String str, int i) {
        doRemove(str, i);
        dispatchPendingChanges();
    }

    /* access modifiers changed from: package-private */
    public void addSummaryToSuppress(String str, String str2) {
        this.mSuppressedGroupKeys.put(str, str2);
    }

    /* access modifiers changed from: package-private */
    public String getSummaryKey(String str) {
        return this.mSuppressedGroupKeys.get(str);
    }

    /* access modifiers changed from: package-private */
    public void removeSuppressedSummary(String str) {
        this.mSuppressedGroupKeys.remove(str);
    }

    /* access modifiers changed from: package-private */
    public boolean isSummarySuppressed(String str) {
        return this.mSuppressedGroupKeys.containsKey(str);
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Bubble> getBubblesInGroup(String str, NotificationEntryManager notificationEntryManager) {
        ArrayList<Bubble> arrayList = new ArrayList<>();
        if (str == null) {
            return arrayList;
        }
        for (Bubble bubble : this.mBubbles) {
            NotificationEntry pendingOrActiveNotif = notificationEntryManager.getPendingOrActiveNotif(bubble.getKey());
            if (pendingOrActiveNotif != null && str.equals(pendingOrActiveNotif.getSbn().getGroupKey())) {
                arrayList.add(bubble);
            }
        }
        return arrayList;
    }

    public void removeBubblesWithInvalidShortcuts(String str, List<ShortcutInfo> list, int i) {
        HashSet hashSet = new HashSet();
        for (ShortcutInfo shortcutInfo : list) {
            hashSet.add(shortcutInfo.getId());
        }
        $$Lambda$BubbleData$OJROTAbwBF0fCA1oF1e2LMcjfg r4 = new Predicate(str, hashSet) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleData$OJROTAbwBF0fCA1oF1e2LMcjfg */
            public final /* synthetic */ String f$0;
            public final /* synthetic */ Set f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return BubbleData.lambda$removeBubblesWithInvalidShortcuts$0(this.f$0, this.f$1, (Bubble) obj);
            }
        };
        $$Lambda$BubbleData$3x9c7VXMa5ZgtScfM_PLfdJBhCY r3 = new Consumer(i) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleData$3x9c7VXMa5ZgtScfM_PLfdJBhCY */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BubbleData.this.lambda$removeBubblesWithInvalidShortcuts$1$BubbleData(this.f$1, (Bubble) obj);
            }
        };
        performActionOnBubblesMatching(getBubbles(), r4, r3);
        performActionOnBubblesMatching(getOverflowBubbles(), r4, r3);
    }

    static /* synthetic */ boolean lambda$removeBubblesWithInvalidShortcuts$0(String str, Set set, Bubble bubble) {
        boolean equals = str.equals(bubble.getPackageName());
        boolean hasMetadataShortcutId = bubble.hasMetadataShortcutId();
        if (!equals || !hasMetadataShortcutId) {
            return false;
        }
        boolean z = bubble.hasMetadataShortcutId() && bubble.getShortcutInfo() != null && bubble.getShortcutInfo().isEnabled() && set.contains(bubble.getShortcutInfo().getId());
        if (!equals || z) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeBubblesWithInvalidShortcuts$1 */
    public /* synthetic */ void lambda$removeBubblesWithInvalidShortcuts$1$BubbleData(int i, Bubble bubble) {
        dismissBubbleWithKey(bubble.getKey(), i);
    }

    public void removeBubblesWithPackageName(String str, int i) {
        $$Lambda$BubbleData$yNv2b3CKuwvR21gLk1U8HP86Gac r0 = new Predicate(str) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleData$yNv2b3CKuwvR21gLk1U8HP86Gac */
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((Bubble) obj).getPackageName().equals(this.f$0);
            }
        };
        $$Lambda$BubbleData$xKiHMLOJXi3HkPeIm_knLGFnA8A r2 = new Consumer(i) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleData$xKiHMLOJXi3HkPeIm_knLGFnA8A */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BubbleData.this.lambda$removeBubblesWithPackageName$3$BubbleData(this.f$1, (Bubble) obj);
            }
        };
        performActionOnBubblesMatching(getBubbles(), r0, r2);
        performActionOnBubblesMatching(getOverflowBubbles(), r0, r2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeBubblesWithPackageName$3 */
    public /* synthetic */ void lambda$removeBubblesWithPackageName$3$BubbleData(int i, Bubble bubble) {
        dismissBubbleWithKey(bubble.getKey(), i);
    }

    private void doAdd(Bubble bubble) {
        this.mBubbles.add(0, bubble);
        Update update = this.mStateChange;
        update.addedBubble = bubble;
        boolean z = true;
        if (this.mBubbles.size() <= 1) {
            z = false;
        }
        update.orderChanged = z;
        if (!isExpanded()) {
            setSelectedBubbleInternal(this.mBubbles.get(0));
        }
    }

    private void trim() {
        if (this.mBubbles.size() > this.mMaxBubbles) {
            this.mBubbles.stream().sorted(Comparator.comparingLong($$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY.INSTANCE)).filter(new Predicate() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleData$fdQdGUozu7xCn6j8BuMSn_4JPo */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return BubbleData.this.lambda$trim$4$BubbleData((Bubble) obj);
                }
            }).findFirst().ifPresent(new Consumer() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleData$8l9nPNZ1SFL5Nh0WWQItDAiTp7Y */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BubbleData.this.lambda$trim$5$BubbleData((Bubble) obj);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$trim$4 */
    public /* synthetic */ boolean lambda$trim$4$BubbleData(Bubble bubble) {
        return !bubble.equals(this.mSelectedBubble);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$trim$5 */
    public /* synthetic */ void lambda$trim$5$BubbleData(Bubble bubble) {
        doRemove(bubble.getKey(), 2);
    }

    private void doUpdate(Bubble bubble) {
        this.mStateChange.updatedBubble = bubble;
        if (!isExpanded()) {
            int indexOf = this.mBubbles.indexOf(bubble);
            this.mBubbles.remove(bubble);
            this.mBubbles.add(0, bubble);
            this.mStateChange.orderChanged = indexOf != 0;
            setSelectedBubbleInternal(this.mBubbles.get(0));
        }
    }

    private void performActionOnBubblesMatching(List<Bubble> list, Predicate<Bubble> predicate, Consumer<Bubble> consumer) {
        ArrayList<Bubble> arrayList = new ArrayList();
        for (Bubble bubble : list) {
            if (predicate.test(bubble)) {
                arrayList.add(bubble);
            }
        }
        for (Bubble bubble2 : arrayList) {
            consumer.accept(bubble2);
        }
    }

    private void doRemove(String str, int i) {
        if (this.mPendingBubbles.containsKey(str)) {
            this.mPendingBubbles.remove(str);
        }
        int indexForKey = indexForKey(str);
        if (indexForKey != -1) {
            Bubble bubble = this.mBubbles.get(indexForKey);
            bubble.stopInflation();
            if (this.mBubbles.size() == 1) {
                setExpandedInternal(false);
                this.mSelectedBubble = null;
            }
            if (indexForKey < this.mBubbles.size() - 1) {
                this.mStateChange.orderChanged = true;
            }
            this.mBubbles.remove(indexForKey);
            this.mStateChange.bubbleRemoved(bubble, i);
            if (!isExpanded()) {
                this.mStateChange.orderChanged |= repackAll();
            }
            overflowBubble(i, bubble);
            if (Objects.equals(this.mSelectedBubble, bubble)) {
                setSelectedBubbleInternal(this.mBubbles.get(Math.min(indexForKey, this.mBubbles.size() - 1)));
            }
            maybeSendDeleteIntent(i, bubble);
        } else if (!hasOverflowBubbleWithKey(str)) {
        } else {
            if (i == 5 || i == 9 || i == 7 || i == 4 || i == 12 || i == 13) {
                Bubble overflowBubbleWithKey = getOverflowBubbleWithKey(str);
                if (overflowBubbleWithKey != null) {
                    overflowBubbleWithKey.stopInflation();
                }
                this.mLogger.logOverflowRemove(overflowBubbleWithKey, i);
                this.mStateChange.bubbleRemoved(overflowBubbleWithKey, i);
                this.mOverflowBubbles.remove(overflowBubbleWithKey);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void overflowBubble(int i, Bubble bubble) {
        if (bubble.getPendingIntentCanceled()) {
            return;
        }
        if (i == 2 || i == 1) {
            this.mLogger.logOverflowAdd(bubble, i);
            this.mOverflowBubbles.add(0, bubble);
            bubble.stopInflation();
            if (this.mOverflowBubbles.size() == this.mMaxOverflowBubbles + 1) {
                List<Bubble> list = this.mOverflowBubbles;
                Bubble bubble2 = list.get(list.size() - 1);
                this.mStateChange.bubbleRemoved(bubble2, 11);
                this.mLogger.log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_MAX_REACHED);
                this.mOverflowBubbles.remove(bubble2);
            }
        }
    }

    public void dismissAll(int i) {
        if (!this.mBubbles.isEmpty()) {
            setExpandedInternal(false);
            setSelectedBubbleInternal(null);
            while (!this.mBubbles.isEmpty()) {
                doRemove(this.mBubbles.get(0).getKey(), i);
            }
            dispatchPendingChanges();
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyDisplayEmpty(int i) {
        for (Bubble bubble : this.mBubbles) {
            if (bubble.getDisplayId() == i) {
                if (bubble.getExpandedView() != null) {
                    bubble.getExpandedView().notifyDisplayEmpty();
                    return;
                }
                return;
            }
        }
    }

    private void dispatchPendingChanges() {
        if (this.mListener != null && this.mStateChange.anythingChanged()) {
            this.mListener.applyUpdate(this.mStateChange);
        }
        this.mStateChange = new Update(this.mBubbles, this.mOverflowBubbles);
    }

    private void setSelectedBubbleInternal(Bubble bubble) {
        if (!this.mShowingOverflow && Objects.equals(bubble, this.mSelectedBubble)) {
            return;
        }
        if (bubble == null || this.mBubbles.contains(bubble) || this.mOverflowBubbles.contains(bubble)) {
            if (this.mExpanded && bubble != null) {
                bubble.markAsAccessedAt(this.mTimeSource.currentTimeMillis());
            }
            this.mSelectedBubble = bubble;
            Update update = this.mStateChange;
            update.selectedBubble = bubble;
            update.selectionChanged = true;
            return;
        }
        Log.e("Bubbles", "Cannot select bubble which doesn't exist! (" + bubble + ") bubbles=" + this.mBubbles);
    }

    private void setExpandedInternal(boolean z) {
        if (this.mExpanded != z) {
            if (z) {
                if (this.mBubbles.isEmpty()) {
                    Log.e("Bubbles", "Attempt to expand stack when empty!");
                    return;
                }
                Bubble bubble = this.mSelectedBubble;
                if (bubble == null) {
                    Log.e("Bubbles", "Attempt to expand stack without selected bubble!");
                    return;
                }
                bubble.markAsAccessedAt(this.mTimeSource.currentTimeMillis());
                this.mStateChange.orderChanged |= repackAll();
            } else if (!this.mBubbles.isEmpty()) {
                this.mStateChange.orderChanged |= repackAll();
                if (this.mShowingOverflow) {
                    setSelectedBubbleInternal(this.mSelectedBubble);
                }
                if (this.mBubbles.indexOf(this.mSelectedBubble) > 0 && this.mBubbles.indexOf(this.mSelectedBubble) != 0) {
                    this.mBubbles.remove(this.mSelectedBubble);
                    this.mBubbles.add(0, this.mSelectedBubble);
                    this.mStateChange.orderChanged = true;
                }
            }
            this.mExpanded = z;
            Update update = this.mStateChange;
            update.expanded = z;
            update.expandedChanged = true;
        }
    }

    /* access modifiers changed from: private */
    public static long sortKey(Bubble bubble) {
        return bubble.getLastActivity();
    }

    private boolean repackAll() {
        if (this.mBubbles.isEmpty()) {
            return false;
        }
        ArrayList arrayList = new ArrayList(this.mBubbles.size());
        this.mBubbles.stream().sorted(BUBBLES_BY_SORT_KEY_DESCENDING).forEachOrdered(new Consumer(arrayList) {
            /* class com.android.systemui.bubbles.$$Lambda$0tU2wih_2wwdAnw6hE7FT9YuCis */
            public final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add((Bubble) obj);
            }
        });
        if (arrayList.equals(this.mBubbles)) {
            return false;
        }
        this.mBubbles.clear();
        this.mBubbles.addAll(arrayList);
        return true;
    }

    private void maybeSendDeleteIntent(int i, Bubble bubble) {
        PendingIntent deleteIntent;
        if (i == 1 && (deleteIntent = bubble.getDeleteIntent()) != null) {
            try {
                deleteIntent.send();
            } catch (PendingIntent.CanceledException unused) {
                Log.w("Bubbles", "Failed to send delete intent for bubble with key: " + bubble.getKey());
            }
        }
    }

    private int indexForKey(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            if (this.mBubbles.get(i).getKey().equals(str)) {
                return i;
            }
        }
        return -1;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public List<Bubble> getBubbles() {
        return Collections.unmodifiableList(this.mBubbles);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public List<Bubble> getOverflowBubbles() {
        return Collections.unmodifiableList(this.mOverflowBubbles);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public Bubble getAnyBubbleWithkey(String str) {
        Bubble bubbleInStackWithKey = getBubbleInStackWithKey(str);
        return bubbleInStackWithKey == null ? getOverflowBubbleWithKey(str) : bubbleInStackWithKey;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public Bubble getBubbleInStackWithKey(String str) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            Bubble bubble = this.mBubbles.get(i);
            if (bubble.getKey().equals(str)) {
                return bubble;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Bubble getBubbleWithView(View view) {
        for (int i = 0; i < this.mBubbles.size(); i++) {
            Bubble bubble = this.mBubbles.get(i);
            if (bubble.getIconView() != null && bubble.getIconView().equals(view)) {
                return bubble;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public Bubble getOverflowBubbleWithKey(String str) {
        for (int i = 0; i < this.mOverflowBubbles.size(); i++) {
            Bubble bubble = this.mOverflowBubbles.get(i);
            if (bubble.getKey().equals(str)) {
                return bubble;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void setTimeSource(TimeSource timeSource) {
        this.mTimeSource = timeSource;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setMaxOverflowBubbles(int i) {
        this.mMaxOverflowBubbles = i;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("selected: ");
        Bubble bubble = this.mSelectedBubble;
        printWriter.println(bubble != null ? bubble.getKey() : "null");
        printWriter.print("expanded: ");
        printWriter.println(this.mExpanded);
        printWriter.print("count:    ");
        printWriter.println(this.mBubbles.size());
        for (Bubble bubble2 : this.mBubbles) {
            bubble2.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.print("summaryKeys: ");
        printWriter.println(this.mSuppressedGroupKeys.size());
        Iterator<String> it = this.mSuppressedGroupKeys.keySet().iterator();
        while (it.hasNext()) {
            printWriter.println("   suppressing: " + it.next());
        }
    }
}
