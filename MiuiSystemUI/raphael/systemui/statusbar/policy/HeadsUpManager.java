package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pools;
import android.view.View;
import android.view.ViewTreeObserver;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import miui.hardware.display.DisplayFeatureManager;

public class HeadsUpManager implements ViewTreeObserver.OnComputeInternalInsetsListener, VisualStabilityManager.Callback, ConfigurationController.ConfigurationListener {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Constants.DEBUG;
    private StatusBar mBar;
    /* access modifiers changed from: private */
    public Clock mClock;
    private final Context mContext;
    private final int mDefaultSnoozeLengthMs;
    private int mDisplayCutoutTouchableRegionSize;
    /* access modifiers changed from: private */
    public HashSet<NotificationData.Entry> mEntriesToRemoveAfterExpand = new HashSet<>();
    /* access modifiers changed from: private */
    public ArraySet<NotificationData.Entry> mEntriesToRemoveWhenReorderingAllowed = new ArraySet<>();
    private final Pools.Pool<HeadsUpEntry> mEntryPool = new Pools.Pool<HeadsUpEntry>() {
        private Stack<HeadsUpEntry> mPoolObjects = new Stack<>();

        public HeadsUpEntry acquire() {
            if (!this.mPoolObjects.isEmpty()) {
                return this.mPoolObjects.pop();
            }
            return new HeadsUpEntry();
        }

        public boolean release(HeadsUpEntry headsUpEntry) {
            headsUpEntry.reset();
            this.mPoolObjects.push(headsUpEntry);
            return true;
        }
    };
    private final NotificationGroupManager mGroupManager;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private boolean mHasPinnedNotification;
    private HashMap<String, HeadsUpEntry> mHeadsUpEntries = new HashMap<>();
    private boolean mHeadsUpGoingAway;
    /* access modifiers changed from: private */
    public final int mHeadsUpNotificationDecay;
    private boolean mIsExpanded;
    private boolean mIsObserving;
    private final HashSet<OnHeadsUpChangedListener> mListeners = new HashSet<>();
    /* access modifiers changed from: private */
    public final int mMinimumDisplayTime;
    private final MiuiStatusBarPromptController mMiuiStatusBarPromptController;
    private Rect mPinnedNotificationBounds = new Rect();
    private boolean mReleaseOnExpandFinish;
    private ContentObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public int mSnoozeLengthMs;
    private final ArrayMap<String, Long> mSnoozedPackages;
    /* access modifiers changed from: private */
    public int mStatusBarHeight;
    private int mStatusBarState;
    /* access modifiers changed from: private */
    public final View mStatusBarWindowView;
    private HashSet<String> mSwipedOutKeys = new HashSet<>();
    private int[] mTmpTwoArray = new int[2];
    /* access modifiers changed from: private */
    public final int mTouchAcceptanceDelay;
    /* access modifiers changed from: private */
    public boolean mTrackingHeadsUp;
    private int mUser;
    /* access modifiers changed from: private */
    public VisualStabilityManager mVisualStabilityManager;
    /* access modifiers changed from: private */
    public boolean mWaitingOnCollapseWhenGoingAway;

    public void onConfigChanged(Configuration configuration) {
    }

    public HeadsUpManager(final Context context, View view, NotificationGroupManager notificationGroupManager, StatusBar statusBar) {
        this.mContext = context;
        Resources resources = this.mContext.getResources();
        this.mTouchAcceptanceDelay = resources.getInteger(R.integer.touch_acceptance_delay);
        this.mSnoozedPackages = new ArrayMap<>();
        this.mDefaultSnoozeLengthMs = resources.getInteger(R.integer.heads_up_default_snooze_length_ms);
        this.mSnoozeLengthMs = this.mDefaultSnoozeLengthMs;
        this.mMinimumDisplayTime = resources.getInteger(R.integer.heads_up_notification_minimum_time);
        this.mHeadsUpNotificationDecay = resources.getInteger(R.integer.heads_up_notification_decay);
        this.mClock = new Clock();
        this.mMiuiStatusBarPromptController = (MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class);
        this.mSnoozeLengthMs = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", this.mDefaultSnoozeLengthMs);
        this.mSettingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                int i = Settings.Global.getInt(context.getContentResolver(), "heads_up_snooze_length_ms", -1);
                if (i > -1 && i != HeadsUpManager.this.mSnoozeLengthMs) {
                    int unused = HeadsUpManager.this.mSnoozeLengthMs = i;
                    if (HeadsUpManager.DEBUG) {
                        Log.v("HeadsUpManager", "mSnoozeLengthMs = " + HeadsUpManager.this.mSnoozeLengthMs);
                    }
                }
            }
        };
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("heads_up_snooze_length_ms"), false, this.mSettingsObserver);
        this.mStatusBarWindowView = view;
        this.mGroupManager = notificationGroupManager;
        this.mBar = statusBar;
        initResources();
        ((BubbleController) Dependency.get(BubbleController.class)).setBubbleStateChangeListener(new BubbleController.BubbleStateChangeListener() {
            public final void onHasBubblesChanged(boolean z) {
                HeadsUpManager.this.lambda$new$0$HeadsUpManager(z);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$HeadsUpManager(boolean z) {
        updateTouchableRegionListener();
    }

    public void updateTouchableRegionListener() {
        boolean z = this.mHasPinnedNotification || this.mHeadsUpGoingAway || ((BubbleController) Dependency.get(BubbleController.class)).hasBubbles() || this.mWaitingOnCollapseWhenGoingAway || DisplayCutoutCompat.hasCutout(this.mStatusBarWindowView);
        if (z != this.mIsObserving) {
            if (z) {
                this.mStatusBarWindowView.getViewTreeObserver().addOnComputeInternalInsetsListener(this);
                this.mStatusBarWindowView.requestLayout();
            } else {
                this.mStatusBarWindowView.getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
            }
            this.mIsObserving = z;
        }
    }

    public void addListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.add(onHeadsUpChangedListener);
    }

    public void removeListener(OnHeadsUpChangedListener onHeadsUpChangedListener) {
        this.mListeners.remove(onHeadsUpChangedListener);
    }

    public void showNotification(NotificationData.Entry entry) {
        if (DEBUG) {
            Log.v("HeadsUpManager", "showNotification");
        }
        addHeadsUpEntry(entry);
        updateNotification(entry, true);
        removeOldHeadsUpNotification();
        entry.setInterruption();
    }

    private void removeOldHeadsUpNotification() {
        if (!this.mHeadsUpEntries.isEmpty()) {
            HeadsUpEntry topEntry = getTopEntry();
            ArrayList<HeadsUpEntry> arrayList = new ArrayList<>();
            for (HeadsUpEntry next : this.mHeadsUpEntries.values()) {
                if (next != topEntry) {
                    arrayList.add(next);
                }
            }
            for (HeadsUpEntry headsUpEntry : arrayList) {
                removeNotification(headsUpEntry.entry.key, true);
            }
        }
    }

    public void updateNotification(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry;
        if (DEBUG) {
            Log.v("HeadsUpManager", "updateNotification");
        }
        entry.row.sendAccessibilityEvent(2048);
        if (z && (headsUpEntry = this.mHeadsUpEntries.get(entry.key)) != null) {
            headsUpEntry.updateEntry();
            setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(entry));
        }
    }

    private void addHeadsUpEntry(NotificationData.Entry entry) {
        HeadsUpEntry headsUpEntry = (HeadsUpEntry) this.mEntryPool.acquire();
        headsUpEntry.setEntry(entry);
        this.mHeadsUpEntries.put(entry.key, headsUpEntry);
        entry.row.setHeadsUp(true);
        setEntryPinned(headsUpEntry, shouldHeadsUpBecomePinned(entry));
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpStateChanged(entry, true);
        }
        entry.row.sendAccessibilityEvent(2048);
    }

    private boolean shouldHeadsUpBecomePinned(NotificationData.Entry entry) {
        if ((this.mStatusBarState == 1 || this.mIsExpanded) && !hasFullScreenIntent(entry)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean hasFullScreenIntent(NotificationData.Entry entry) {
        return entry.notification.getNotification().fullScreenIntent != null;
    }

    private void setEntryPinned(HeadsUpEntry headsUpEntry, boolean z) {
        ExpandableNotificationRow expandableNotificationRow = headsUpEntry.entry.row;
        if (expandableNotificationRow.isPinned() != z) {
            expandableNotificationRow.setPinned(z);
            updatePinnedMode();
            Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                OnHeadsUpChangedListener next = it.next();
                if (z) {
                    next.onHeadsUpPinned(expandableNotificationRow);
                } else {
                    next.onHeadsUpUnPinned(expandableNotificationRow);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeHeadsUpEntry(NotificationData.Entry entry) {
        HeadsUpEntry remove = this.mHeadsUpEntries.remove(entry.key);
        entry.row.sendAccessibilityEvent(2048);
        entry.row.setHeadsUp(false);
        setEntryPinned(remove, false);
        Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onHeadsUpStateChanged(entry, false);
        }
        this.mEntryPool.release(remove);
    }

    private void updatePinnedMode() {
        boolean hasPinnedNotificationInternal = hasPinnedNotificationInternal();
        if (hasPinnedNotificationInternal != this.mHasPinnedNotification) {
            if (Constants.SUPPORT_FPS_DYNAMIC_ACCOMMODATION) {
                DisplayFeatureManager.getInstance().setScreenEffect(24, 255, 256);
            }
            this.mHasPinnedNotification = hasPinnedNotificationInternal;
            if (this.mHasPinnedNotification) {
                MetricsLogger.count(this.mContext, "note_peek", 1);
            }
            updateTouchableRegionListener();
            Iterator<OnHeadsUpChangedListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onHeadsUpPinnedModeChanged(hasPinnedNotificationInternal);
            }
        }
    }

    public void removeHeadsUpNotification() {
        HeadsUpEntry topEntry = getTopEntry();
        if (topEntry == null || !topEntry.entry.row.isHeadsUp() || !topEntry.entry.row.isPinned()) {
            Log.w("HeadsUpManager", "removeHeadsUpNotification() no heads up notification on show");
        } else {
            removeNotification(topEntry.entry.key, true);
        }
    }

    public boolean removeNotification(String str, boolean z) {
        if (DEBUG) {
            Log.v("HeadsUpManager", "remove");
        }
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        if (headsUpEntry == null) {
            return true;
        }
        if (wasShownLongEnough(str) || z) {
            releaseImmediately(str);
            return true;
        }
        headsUpEntry.removeAsSoonAsPossible();
        return false;
    }

    private boolean wasShownLongEnough(String str) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        HeadsUpEntry topEntry = getTopEntry();
        if (this.mSwipedOutKeys.contains(str)) {
            this.mSwipedOutKeys.remove(str);
            return true;
        } else if (headsUpEntry != topEntry) {
            return true;
        } else {
            return headsUpEntry.wasShownLongEnough();
        }
    }

    public boolean isHeadsUp(String str) {
        return this.mHeadsUpEntries.containsKey(str);
    }

    public boolean isHeadsUp() {
        return this.mHeadsUpEntries.size() > 0;
    }

    public void releaseAllImmediately() {
        if (DEBUG) {
            Log.v("HeadsUpManager", "releaseAllImmediately");
        }
        Iterator it = new ArrayList(this.mHeadsUpEntries.keySet()).iterator();
        while (it.hasNext()) {
            releaseImmediately((String) it.next());
        }
    }

    public void releaseImmediately(String str) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        if (headsUpEntry != null) {
            removeHeadsUpEntry(headsUpEntry.entry);
        }
    }

    public boolean isSnoozed(String str) {
        String snoozeKey = snoozeKey(str, this.mUser);
        Long l = this.mSnoozedPackages.get(snoozeKey);
        if (l == null) {
            return false;
        }
        if (l.longValue() <= SystemClock.elapsedRealtime()) {
            this.mSnoozedPackages.remove(str);
            return false;
        } else if (!DEBUG) {
            return true;
        } else {
            Log.v("HeadsUpManager", snoozeKey + " snoozed");
            return true;
        }
    }

    public void snooze() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            this.mSnoozedPackages.put(snoozeKey(this.mHeadsUpEntries.get(str).entry.notification.getPackageName(), this.mUser), Long.valueOf(SystemClock.elapsedRealtime() + ((long) this.mSnoozeLengthMs)));
        }
        this.mReleaseOnExpandFinish = true;
    }

    private static String snoozeKey(String str, int i) {
        return i + "," + str;
    }

    public HeadsUpEntry getHeadsUpEntry(String str) {
        return this.mHeadsUpEntries.get(str);
    }

    public NotificationData.Entry getEntry(String str) {
        return this.mHeadsUpEntries.get(str).entry;
    }

    public Collection<HeadsUpEntry> getAllEntries() {
        return this.mHeadsUpEntries.values();
    }

    public HeadsUpEntry getTopEntry() {
        HeadsUpEntry headsUpEntry = null;
        if (this.mHeadsUpEntries.isEmpty()) {
            return null;
        }
        for (HeadsUpEntry next : this.mHeadsUpEntries.values()) {
            if (headsUpEntry == null || next.compareTo(headsUpEntry) == -1) {
                headsUpEntry = next;
            }
        }
        return headsUpEntry;
    }

    private void initResources() {
        Resources resources = this.mContext.getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105439);
        this.mDisplayCutoutTouchableRegionSize = resources.getDimensionPixelSize(R.dimen.display_cutout_touchable_region_size);
    }

    public void onDensityOrFontScaleChanged() {
        initResources();
    }

    public boolean shouldSwallowClick(String str) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(str);
        return headsUpEntry != null && this.mClock.currentTimeMillis() < headsUpEntry.postTime;
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        ExpandableNotificationRow groupSummary;
        if (!this.mIsExpanded && !this.mBar.isBouncerShowing()) {
            if (this.mHasPinnedNotification) {
                ExpandableNotificationRow expandableNotificationRow = getTopEntry().entry.row;
                if (expandableNotificationRow.isChildInGroup() && (groupSummary = this.mGroupManager.getGroupSummary((StatusBarNotification) expandableNotificationRow.getStatusBarNotification())) != null) {
                    expandableNotificationRow = groupSummary;
                }
                expandableNotificationRow.getLocationOnScreen(this.mTmpTwoArray);
                int width = this.mTmpTwoArray[0] + expandableNotificationRow.getWidth();
                int intrinsicHeight = expandableNotificationRow.getIntrinsicHeight() + ((int) expandableNotificationRow.getTranslationY());
                this.mPinnedNotificationBounds.set(this.mTmpTwoArray[0], (int) expandableNotificationRow.getTranslationY(), width, intrinsicHeight);
                internalInsetsInfo.setTouchableInsets(3);
                internalInsetsInfo.touchableRegion.set(this.mPinnedNotificationBounds);
                return;
            }
            setCollapsedTouchableInsets(internalInsetsInfo);
        }
    }

    public void getPinnedNotificationBounds(Rect rect) {
        rect.set(this.mPinnedNotificationBounds);
    }

    private void setCollapsedTouchableInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        internalInsetsInfo.touchableRegion.set(0, 0, this.mStatusBarWindowView.getWidth(), this.mStatusBarHeight);
        updateRegionForNotch(internalInsetsInfo.touchableRegion);
        updateRegionForPrompt(internalInsetsInfo.touchableRegion);
        updateRegionForBubble(internalInsetsInfo.touchableRegion);
    }

    private void updateRegionForNotch(Region region) {
        Rect rect = new Rect();
        DisplayCutoutCompat.boundsFromDirection(this.mStatusBarWindowView, 48, rect);
        if (!rect.isEmpty()) {
            rect.offset(0, this.mDisplayCutoutTouchableRegionSize);
            region.op(rect, Region.Op.UNION);
        }
    }

    private void updateRegionForPrompt(Region region) {
        if (this.mMiuiStatusBarPromptController.getTouchRegion() != null) {
            region.op(this.mMiuiStatusBarPromptController.getTouchRegion(), Region.Op.UNION);
        }
    }

    private void updateRegionForBubble(Region region) {
        Rect touchableRegion = ((BubbleController) Dependency.get(BubbleController.class)).getTouchableRegion();
        if (touchableRegion != null) {
            region.op(touchableRegion, Region.Op.UNION);
        }
    }

    public void setUser(int i) {
        this.mUser = i;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HeadsUpManager state:");
        printWriter.print("  mTouchAcceptanceDelay=");
        printWriter.println(this.mTouchAcceptanceDelay);
        printWriter.print("  mSnoozeLengthMs=");
        printWriter.println(this.mSnoozeLengthMs);
        printWriter.print("  now=");
        printWriter.println(SystemClock.elapsedRealtime());
        printWriter.print("  mUser=");
        printWriter.println(this.mUser);
        for (HeadsUpEntry headsUpEntry : this.mHeadsUpEntries.values()) {
            printWriter.print("  HeadsUpEntry=");
            printWriter.println(headsUpEntry.entry);
        }
        int size = this.mSnoozedPackages.size();
        printWriter.println("  snoozed packages: " + size);
        for (int i = 0; i < size; i++) {
            printWriter.print("    ");
            printWriter.print(this.mSnoozedPackages.valueAt(i));
            printWriter.print(", ");
            printWriter.println(this.mSnoozedPackages.keyAt(i));
        }
    }

    public boolean hasPinnedHeadsUp() {
        return this.mHasPinnedNotification;
    }

    private boolean hasPinnedNotificationInternal() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            if (this.mHeadsUpEntries.get(str).entry.row.isPinned()) {
                return true;
            }
        }
        return false;
    }

    public void addSwipedOutNotification(String str) {
        this.mSwipedOutKeys.add(str);
    }

    public void unpinAll() {
        for (String str : this.mHeadsUpEntries.keySet()) {
            HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(str);
            setEntryPinned(headsUpEntry, false);
            headsUpEntry.updateEntry(false);
        }
    }

    public void onExpandingFinished() {
        if (this.mReleaseOnExpandFinish) {
            releaseAllImmediately();
            this.mReleaseOnExpandFinish = false;
        } else {
            Iterator<NotificationData.Entry> it = this.mEntriesToRemoveAfterExpand.iterator();
            while (it.hasNext()) {
                NotificationData.Entry next = it.next();
                if (isHeadsUp(next.key)) {
                    removeHeadsUpEntry(next);
                }
            }
        }
        this.mEntriesToRemoveAfterExpand.clear();
    }

    public void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    public void setIsExpanded(boolean z) {
        if (z != this.mIsExpanded) {
            this.mIsExpanded = z;
            if (z) {
                this.mWaitingOnCollapseWhenGoingAway = false;
                this.mHeadsUpGoingAway = false;
                updateTouchableRegionListener();
            }
        }
    }

    public int getTopHeadsUpPinnedHeight() {
        NotificationData.Entry entry;
        ExpandableNotificationRow expandableNotificationRow;
        HeadsUpEntry topEntry = getTopEntry();
        if (topEntry == null || (entry = topEntry.entry) == null) {
            return 0;
        }
        ExpandableNotificationRow expandableNotificationRow2 = entry.row;
        if (!expandableNotificationRow2.isChildInGroup() || (expandableNotificationRow = this.mGroupManager.getGroupSummary((StatusBarNotification) expandableNotificationRow2.getStatusBarNotification())) == null) {
            expandableNotificationRow = expandableNotificationRow2;
        }
        return expandableNotificationRow.getPinnedHeadsUpHeight();
    }

    public int compare(NotificationData.Entry entry, NotificationData.Entry entry2) {
        HeadsUpEntry headsUpEntry = getHeadsUpEntry(entry.key);
        HeadsUpEntry headsUpEntry2 = getHeadsUpEntry(entry2.key);
        if (headsUpEntry == null || headsUpEntry2 == null) {
            return headsUpEntry == null ? 1 : -1;
        }
        return headsUpEntry.compareTo(headsUpEntry2);
    }

    public void setHeadsUpGoingAway(boolean z) {
        if (z != this.mHeadsUpGoingAway) {
            this.mHeadsUpGoingAway = z;
            if (!z) {
                waitForStatusBarLayout();
            }
            updateTouchableRegionListener();
        }
    }

    public boolean isHeadsUpGoingAway() {
        return this.mHeadsUpGoingAway;
    }

    private void waitForStatusBarLayout() {
        this.mWaitingOnCollapseWhenGoingAway = true;
        this.mStatusBarWindowView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (HeadsUpManager.this.mStatusBarWindowView.getHeight() <= HeadsUpManager.this.mStatusBarHeight) {
                    HeadsUpManager.this.mStatusBarWindowView.removeOnLayoutChangeListener(this);
                    boolean unused = HeadsUpManager.this.mWaitingOnCollapseWhenGoingAway = false;
                    HeadsUpManager.this.updateTouchableRegionListener();
                }
            }
        });
    }

    public static void setIsClickedNotification(View view, boolean z) {
        view.setTag(R.id.is_clicked_heads_up_tag, z ? true : null);
    }

    public static boolean isClickedHeadsUpNotification(View view) {
        Boolean bool = (Boolean) view.getTag(R.id.is_clicked_heads_up_tag);
        return bool != null && bool.booleanValue();
    }

    public void setRemoteInputActive(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry != null && headsUpEntry.remoteInputActive != z) {
            headsUpEntry.remoteInputActive = z;
            if (z) {
                headsUpEntry.removeAutoRemovalCallbacks();
            } else {
                headsUpEntry.updateEntry(false);
            }
        }
    }

    public void setExpanded(NotificationData.Entry entry, boolean z) {
        HeadsUpEntry headsUpEntry = this.mHeadsUpEntries.get(entry.key);
        if (headsUpEntry != null && headsUpEntry.expanded != z && entry.row.isPinned()) {
            headsUpEntry.expanded = z;
            if (z) {
                headsUpEntry.removeAutoRemovalCallbacks();
            } else {
                headsUpEntry.updateEntry(false);
            }
        }
    }

    public void setSticky(long j) {
        HeadsUpEntry topEntry;
        if (this.mHasPinnedNotification && (topEntry = getTopEntry()) != null) {
            topEntry.removeAutoRemovalCallbacks();
            this.mHandler.postDelayed(topEntry.mRemoveHeadsUpRunnable, j);
        }
    }

    public void onReorderingAllowed() {
        Iterator<NotificationData.Entry> it = this.mEntriesToRemoveWhenReorderingAllowed.iterator();
        while (it.hasNext()) {
            NotificationData.Entry next = it.next();
            if (isHeadsUp(next.key)) {
                removeHeadsUpEntry(next);
            }
        }
        this.mEntriesToRemoveWhenReorderingAllowed.clear();
    }

    public void setVisualStabilityManager(VisualStabilityManager visualStabilityManager) {
        this.mVisualStabilityManager = visualStabilityManager;
    }

    public void setStatusBarState(int i) {
        this.mStatusBarState = i;
    }

    public class HeadsUpEntry implements Comparable<HeadsUpEntry> {
        public long earliestRemovaltime;
        public NotificationData.Entry entry;
        public boolean expanded;
        /* access modifiers changed from: private */
        public Runnable mRemoveHeadsUpRunnable;
        public long postTime;
        public boolean remoteInputActive;

        public HeadsUpEntry() {
        }

        public void setEntry(final NotificationData.Entry entry2) {
            this.entry = entry2;
            this.postTime = HeadsUpManager.this.mClock.currentTimeMillis() + ((long) HeadsUpManager.this.mTouchAcceptanceDelay);
            this.mRemoveHeadsUpRunnable = new Runnable() {
                public void run() {
                    if (!HeadsUpManager.this.mVisualStabilityManager.isReorderingAllowed()) {
                        HeadsUpManager.this.mEntriesToRemoveWhenReorderingAllowed.add(entry2);
                        HeadsUpManager.this.mVisualStabilityManager.addReorderingAllowedCallback(HeadsUpManager.this);
                    } else if (!HeadsUpManager.this.mTrackingHeadsUp) {
                        HeadsUpManager.this.removeHeadsUpEntry(entry2);
                    } else {
                        HeadsUpManager.this.mEntriesToRemoveAfterExpand.add(entry2);
                    }
                }
            };
            updateEntry();
        }

        public void updateEntry() {
            updateEntry(true);
        }

        public void updateEntry(boolean z) {
            long currentTimeMillis = HeadsUpManager.this.mClock.currentTimeMillis();
            this.earliestRemovaltime = ((long) HeadsUpManager.this.mMinimumDisplayTime) + currentTimeMillis;
            if (z) {
                this.postTime = Math.max(this.postTime, currentTimeMillis);
            }
            removeAutoRemovalCallbacks();
            if (HeadsUpManager.this.mEntriesToRemoveAfterExpand.contains(this.entry)) {
                HeadsUpManager.this.mEntriesToRemoveAfterExpand.remove(this.entry);
            }
            if (HeadsUpManager.this.mEntriesToRemoveWhenReorderingAllowed.contains(this.entry)) {
                HeadsUpManager.this.mEntriesToRemoveWhenReorderingAllowed.remove(this.entry);
            }
            if (!isSticky()) {
                int floatTime = this.entry.notification.getFloatTime();
                long j = this.postTime;
                if (floatTime <= 0) {
                    floatTime = HeadsUpManager.this.mHeadsUpNotificationDecay;
                }
                HeadsUpManager.this.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, Math.max((j + ((long) floatTime)) - currentTimeMillis, (long) HeadsUpManager.this.mMinimumDisplayTime));
            }
        }

        private boolean isSticky() {
            return (this.entry.row.isPinned() && this.expanded) || this.remoteInputActive || HeadsUpManager.this.hasFullScreenIntent(this.entry);
        }

        public int compareTo(HeadsUpEntry headsUpEntry) {
            boolean isPinned = this.entry.row.isPinned();
            boolean isPinned2 = headsUpEntry.entry.row.isPinned();
            if (isPinned && !isPinned2) {
                return -1;
            }
            if (!isPinned && isPinned2) {
                return 1;
            }
            boolean access$1600 = HeadsUpManager.this.hasFullScreenIntent(this.entry);
            boolean access$16002 = HeadsUpManager.this.hasFullScreenIntent(headsUpEntry.entry);
            if (access$1600 && !access$16002) {
                return -1;
            }
            if (!access$1600 && access$16002) {
                return 1;
            }
            if (this.remoteInputActive && !headsUpEntry.remoteInputActive) {
                return -1;
            }
            if (!this.remoteInputActive && headsUpEntry.remoteInputActive) {
                return 1;
            }
            long j = this.postTime;
            long j2 = headsUpEntry.postTime;
            if (j < j2) {
                return 1;
            }
            if (j == j2) {
                return this.entry.key.compareTo(headsUpEntry.entry.key);
            }
            return -1;
        }

        public void removeAutoRemovalCallbacks() {
            HeadsUpManager.this.mHandler.removeCallbacks(this.mRemoveHeadsUpRunnable);
        }

        public boolean wasShownLongEnough() {
            return this.earliestRemovaltime < HeadsUpManager.this.mClock.currentTimeMillis();
        }

        public void removeAsSoonAsPossible() {
            removeAutoRemovalCallbacks();
            HeadsUpManager.this.mHandler.postDelayed(this.mRemoveHeadsUpRunnable, this.earliestRemovaltime - HeadsUpManager.this.mClock.currentTimeMillis());
        }

        public void reset() {
            removeAutoRemovalCallbacks();
            this.entry = null;
            this.mRemoveHeadsUpRunnable = null;
            this.expanded = false;
            this.remoteInputActive = false;
        }
    }

    public static class Clock {
        public long currentTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }

    public static int getHeadsUpTopMargin(Context context) {
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.notification_heads_up_margin_top);
        boolean z = context.getResources().getConfiguration().orientation == 2;
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (identifier <= 0 || z) ? dimensionPixelSize : dimensionPixelSize + context.getResources().getDimensionPixelSize(identifier);
    }
}
