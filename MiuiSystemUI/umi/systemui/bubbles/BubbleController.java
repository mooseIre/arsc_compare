package com.android.systemui.bubbles;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.notification.Condition;
import android.service.notification.NotificationListenerService;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.StatusBarServiceCompat;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleStackView;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ZenModeController;
import java.util.List;

public class BubbleController implements ConfigurationController.ConfigurationListener {
    /* access modifiers changed from: private */
    public IStatusBarService mBarService;
    /* access modifiers changed from: private */
    public BubbleData mBubbleData;
    private final BubbleData.Listener mBubbleDataListener;
    /* access modifiers changed from: private */
    public final Context mContext;
    private BubbleExpandListener mExpandListener;
    private int mOrientation;
    /* access modifiers changed from: private */
    public BubbleStackView mStackView;
    private BubbleStateChangeListener mStateChangeListener;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    private int mStatusBarState;
    private final StatusBarWindowManager mStatusBarWindowManager;
    private BubbleStackView.SurfaceSynchronizer mSurfaceSynchronizer;
    private final BubbleTaskStackListener mTaskStackListener;
    private Rect mTempRect;
    private ZenModeController.Callback mZenCallback;

    public interface BubbleExpandListener {
        void onBubbleExpandChanged(boolean z, String str);
    }

    public interface BubbleStateChangeListener {
        void onHasBubblesChanged(boolean z);
    }

    public BubbleController(Context context) {
        this(context, new BubbleData(context));
    }

    public BubbleController(Context context, BubbleData bubbleData) {
        this(context, bubbleData, (BubbleStackView.SurfaceSynchronizer) null);
    }

    public BubbleController(Context context, BubbleData bubbleData, BubbleStackView.SurfaceSynchronizer surfaceSynchronizer) {
        this.mTempRect = new Rect();
        this.mOrientation = 0;
        this.mZenCallback = new ZenModeController.Callback() {
            public void onConditionsChanged(Condition[] conditionArr) {
            }

            public void onEffectsSupressorChanged() {
            }

            public void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
            }

            public void onNextAlarmChanged() {
            }

            public void onZenAvailableChanged(boolean z) {
            }

            public void onZenChanged(int i) {
                BubbleController.this.updateStackViewForZenConfig();
            }

            public void onConfigChanged(ZenModeConfig zenModeConfig) {
                BubbleController.this.updateStackViewForZenConfig();
            }
        };
        AnonymousClass2 r0 = new BubbleData.Listener() {
            public void applyUpdate(BubbleData.Update update) {
                if (BubbleController.this.mStackView == null && update.addedBubble != null) {
                    BubbleController.this.ensureStackViewCreated();
                }
                if (BubbleController.this.mStackView != null) {
                    if (update.addedBubble != null) {
                        BubbleController.this.mStackView.addBubble(update.addedBubble);
                    }
                    if (update.expandedChanged && !update.expanded) {
                        BubbleController.this.mStackView.setExpanded(false);
                    }
                    for (Pair next : update.removedBubbles) {
                        Bubble bubble = (Bubble) next.first;
                        ((Integer) next.second).intValue();
                        BubbleController.this.mStackView.removeBubble(bubble);
                        if (BubbleController.this.mBubbleData.hasBubbleWithKey(bubble.getKey()) || bubble.entry.showInShadeWhenBubble()) {
                            bubble.entry.notification.getNotification().flags &= -4097;
                            try {
                                StatusBarServiceCompat.onNotificationBubbleChanged(BubbleController.this.mBarService, bubble.getKey(), false, 2);
                            } catch (RemoteException unused) {
                            }
                        } else {
                            BubbleController.this.mStatusBar.removeNotification(bubble.entry.notification.getKey(), (NotificationListenerService.RankingMap) null);
                        }
                    }
                    if (update.updatedBubble != null) {
                        BubbleController.this.mStackView.updateBubble(update.updatedBubble);
                    }
                    if (update.orderChanged) {
                        BubbleController.this.mStackView.updateBubbleOrder(update.bubbles);
                    }
                    if (update.selectionChanged) {
                        BubbleController.this.mStackView.setSelectedBubble(update.selectedBubble);
                    }
                    if (update.expandedChanged && update.expanded) {
                        BubbleController.this.mStackView.setExpanded(true);
                    }
                    BubbleController.this.mStatusBar.updateNotifications();
                    BubbleController.this.updateStack();
                }
            }
        };
        this.mBubbleDataListener = r0;
        this.mContext = context;
        this.mBubbleData = bubbleData;
        bubbleData.setListener(r0);
        ((ZenModeController) Dependency.get(ZenModeController.class)).addCallback(this.mZenCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mStatusBarWindowManager = (StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class);
        this.mTaskStackListener = new BubbleTaskStackListener();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new BubblesImeListener());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mSurfaceSynchronizer = surfaceSynchronizer;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }

    public void setStatusBarState(int i) {
        this.mStatusBarState = i;
        if (i != 0) {
            collapseStack();
        }
        updateStack();
    }

    /* access modifiers changed from: private */
    public void ensureStackViewCreated() {
        if (this.mStackView == null) {
            this.mStackView = new BubbleStackView(this.mContext, this.mBubbleData, this.mSurfaceSynchronizer);
            ViewGroup statusBarView = this.mStatusBarWindowManager.getStatusBarView();
            statusBarView.addView(this.mStackView, statusBarView.indexOfChild(statusBarView.findViewById(R.id.scrim_behind)) + 1, new FrameLayout.LayoutParams(-1, -1));
            BubbleExpandListener bubbleExpandListener = this.mExpandListener;
            if (bubbleExpandListener != null) {
                this.mStackView.setExpandListener(bubbleExpandListener);
            }
            updateStackViewForZenConfig();
        }
    }

    public void onDensityOrFontScaleChanged() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.onThemeChanged();
        }
    }

    public void onConfigChanged(Configuration configuration) {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null && configuration != null && configuration.orientation != this.mOrientation) {
            bubbleStackView.onOrientationChanged();
            this.mOrientation = configuration.orientation;
        }
    }

    public void setBubbleStateChangeListener(BubbleStateChangeListener bubbleStateChangeListener) {
        this.mStateChangeListener = bubbleStateChangeListener;
    }

    public void setExpandListener(BubbleExpandListener bubbleExpandListener) {
        $$Lambda$BubbleController$B9Rf8Lqgsvsjhuncdnt9rJlYfA r0 = new BubbleExpandListener(bubbleExpandListener) {
            public final /* synthetic */ BubbleController.BubbleExpandListener f$1;

            {
                this.f$1 = r2;
            }

            public final void onBubbleExpandChanged(boolean z, String str) {
                BubbleController.this.lambda$setExpandListener$0$BubbleController(this.f$1, z, str);
            }
        };
        this.mExpandListener = r0;
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setExpandListener(r0);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setExpandListener$0 */
    public /* synthetic */ void lambda$setExpandListener$0$BubbleController(BubbleExpandListener bubbleExpandListener, boolean z, String str) {
        if (bubbleExpandListener != null) {
            bubbleExpandListener.onBubbleExpandChanged(z, str);
        }
        this.mStatusBarWindowManager.setBubbleExpanded(z);
    }

    public boolean hasBubbles() {
        if (this.mStackView == null) {
            return false;
        }
        return this.mBubbleData.hasBubbles();
    }

    public boolean isStackExpanded() {
        return this.mBubbleData.isExpanded();
    }

    public void collapseStack() {
        this.mBubbleData.setExpanded(false);
    }

    /* access modifiers changed from: package-private */
    public void selectBubble(Bubble bubble) {
        this.mBubbleData.setSelectedBubble(bubble);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void selectBubble(String str) {
        selectBubble(this.mBubbleData.getBubbleWithKey(str));
    }

    public void expandStackAndSelectBubble(String str) {
        Bubble bubbleWithKey = this.mBubbleData.getBubbleWithKey(str);
        if (bubbleWithKey != null) {
            this.mBubbleData.setSelectedBubble(bubbleWithKey);
            this.mBubbleData.setExpanded(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void dismissStack(int i) {
        this.mBubbleData.dismissAll(i);
    }

    public void performBackPressIfNeeded() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.performBackPressIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBubble(NotificationData.Entry entry) {
        if (entry.notification.getImportance() >= 4) {
            entry.setInterruption();
        }
        this.mBubbleData.notificationEntryUpdated(entry);
    }

    /* access modifiers changed from: package-private */
    public void removeBubble(String str, int i) {
        Bubble bubbleWithKey = this.mBubbleData.getBubbleWithKey(str);
        if (bubbleWithKey != null) {
            this.mBubbleData.notificationEntryRemoved(bubbleWithKey.entry, i);
        }
    }

    public boolean onNotificationRemoveRequested(String str, int i) {
        if (!this.mBubbleData.hasBubbleWithKey(str)) {
            return false;
        }
        NotificationData.Entry entry = this.mBubbleData.getBubbleWithKey(str).entry;
        boolean z = (entry.isRowDismissed() && !(i == 8 || i == 9)) || (i == 3) || (i == 2);
        if (entry.isBubble() && !entry.isBubbleDismissed() && z) {
            entry.setShowInShadeWhenBubble(false);
            BubbleStackView bubbleStackView = this.mStackView;
            if (bubbleStackView != null) {
                bubbleStackView.updateDotVisibility(entry.key);
            }
            this.mStatusBar.updateNotifications();
            return true;
        }
        if (!z && !entry.isBubbleDismissed()) {
            this.mBubbleData.notificationEntryRemoved(entry, 5);
        }
        return false;
    }

    public void onPendingEntryAdded(NotificationData.Entry entry) {
        if (areBubblesEnabled(this.mContext) && shouldBubbleUp(entry) && canLaunchInActivityView(this.mContext, entry)) {
            updateShowInShadeForSuppressNotification(entry);
        }
    }

    public void onEntryInflated(NotificationData.Entry entry, int i) {
        if (areBubblesEnabled(this.mContext) && shouldBubbleUp(entry) && canLaunchInActivityView(this.mContext, entry)) {
            updateBubble(entry);
        }
    }

    public void onPreEntryUpdated(NotificationData.Entry entry) {
        if (areBubblesEnabled(this.mContext)) {
            boolean z = shouldBubbleUp(entry) && canLaunchInActivityView(this.mContext, entry);
            if (!z && this.mBubbleData.hasBubbleWithKey(entry.key)) {
                removeBubble(entry.key, 7);
            } else if (z) {
                updateShowInShadeForSuppressNotification(entry);
                entry.setBubbleDismissed(false);
                updateBubble(entry);
            }
        }
    }

    private boolean shouldBubbleUp(NotificationData.Entry entry) {
        ExpandedNotification expandedNotification = entry.notification;
        if (!entry.canBubble || !entry.isBubble()) {
            return false;
        }
        Notification notification = expandedNotification.getNotification();
        if (notification.getBubbleMetadata() == null || notification.getBubbleMetadata().getIntent() == null || !canHeadsUpCommon(entry)) {
            return false;
        }
        return true;
    }

    public boolean canHeadsUpCommon(NotificationData.Entry entry) {
        ExpandedNotification expandedNotification = entry.notification;
        if (!this.mStatusBar.isUseHeadsUp() || this.mStatusBar.isDeviceInVrMode() || this.mStatusBar.shouldSuppressPeek(entry.key) || this.mStatusBar.isSnoozedPackage(expandedNotification) || entry.hasJustLaunchedFullScreenIntent()) {
            return false;
        }
        return true;
    }

    public void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
        this.mBubbleData.notificationRankingUpdated(rankingMap);
    }

    /* access modifiers changed from: private */
    public void updateStackViewForZenConfig() {
        Class cls = ZenModeController.class;
        ZenModeConfig config = ((ZenModeController) Dependency.get(cls)).getConfig();
        if (config != null && this.mStackView != null) {
            int i = config.suppressedVisualEffects;
            boolean z = true;
            boolean z2 = (i & 64) != 0;
            boolean z3 = (i & 16) != 0;
            boolean z4 = (i & 256) != 0;
            boolean z5 = ((ZenModeController) Dependency.get(cls)).getZen() != 0;
            this.mStackView.setSuppressNewDot(z5 && z2);
            BubbleStackView bubbleStackView = this.mStackView;
            if (!z5 || (!z3 && !z4)) {
                z = false;
            }
            bubbleStackView.setSuppressFlyout(z);
        }
    }

    public void updateStack() {
        if (this.mStackView != null) {
            boolean z = false;
            int i = 4;
            if (this.mStatusBarState != 0 || !hasBubbles()) {
                BubbleStackView bubbleStackView = this.mStackView;
                if (bubbleStackView != null) {
                    bubbleStackView.setVisibility(4);
                }
            } else {
                BubbleStackView bubbleStackView2 = this.mStackView;
                if (hasBubbles()) {
                    i = 0;
                }
                bubbleStackView2.setVisibility(i);
            }
            boolean bubblesShowing = this.mStatusBarWindowManager.getBubblesShowing();
            if (hasBubbles() && this.mStackView.getVisibility() == 0) {
                z = true;
            }
            this.mStatusBarWindowManager.setBubblesShowing(z);
            BubbleStateChangeListener bubbleStateChangeListener = this.mStateChangeListener;
            if (!(bubbleStateChangeListener == null || bubblesShowing == z)) {
                bubbleStateChangeListener.onHasBubblesChanged(z);
            }
            this.mStackView.updateContentDescription();
        }
    }

    public Rect getTouchableRegion() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView == null || bubbleStackView.getVisibility() != 0) {
            return null;
        }
        this.mStackView.getBoundsOnScreen(this.mTempRect);
        return this.mTempRect;
    }

    public int getExpandedDisplayId(Context context) {
        if (this.mStackView == null) {
            return -1;
        }
        boolean z = context.getDisplay() != null && context.getDisplay().getDisplayId() == 0;
        Bubble expandedBubble = this.mStackView.getExpandedBubble();
        if (!z || expandedBubble == null || !isStackExpanded() || this.mStatusBarWindowManager.getPanelExpanded()) {
            return -1;
        }
        return expandedBubble.expandedView.getVirtualDisplayId();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public BubbleStackView getStackView() {
        return this.mStackView;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x005c  */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldAutoBubbleForFlags(android.content.Context r8, com.android.systemui.statusbar.NotificationData.Entry r9) {
        /*
            r7 = this;
            boolean r7 = r9.isBubbleDismissed()
            r0 = 0
            if (r7 == 0) goto L_0x0008
            return r0
        L_0x0008:
            com.android.systemui.miui.statusbar.ExpandedNotification r7 = r9.notification
            boolean r9 = shouldAutoBubbleMessages(r8)
            r1 = 1
            if (r9 != 0) goto L_0x0013
            r9 = r0
            goto L_0x0014
        L_0x0013:
            r9 = r1
        L_0x0014:
            boolean r2 = shouldAutoBubbleOngoing(r8)
            if (r2 != 0) goto L_0x001c
            r2 = r0
            goto L_0x001d
        L_0x001c:
            r2 = r1
        L_0x001d:
            boolean r8 = shouldAutoBubbleAll(r8)
            if (r8 != 0) goto L_0x0025
            r8 = r0
            goto L_0x0026
        L_0x0025:
            r8 = r1
        L_0x0026:
            android.app.Notification r3 = r7.getNotification()
            android.app.Notification$Action[] r3 = r3.actions
            if (r3 == 0) goto L_0x0045
            android.app.Notification r3 = r7.getNotification()
            android.app.Notification$Action[] r3 = r3.actions
            int r4 = r3.length
            r5 = r0
        L_0x0036:
            if (r5 >= r4) goto L_0x0045
            r6 = r3[r5]
            android.app.RemoteInput[] r6 = r6.getRemoteInputs()
            if (r6 == 0) goto L_0x0042
            r3 = r1
            goto L_0x0046
        L_0x0042:
            int r5 = r5 + 1
            goto L_0x0036
        L_0x0045:
            r3 = r0
        L_0x0046:
            android.app.Notification r4 = r7.getNotification()
            java.lang.String r4 = r4.category
            java.lang.String r5 = "call"
            boolean r4 = r5.equals(r4)
            if (r4 == 0) goto L_0x005c
            boolean r4 = r7.isOngoing()
            if (r4 == 0) goto L_0x005c
            r4 = r1
            goto L_0x005d
        L_0x005c:
            r4 = r0
        L_0x005d:
            android.app.Notification r5 = r7.getNotification()
            boolean r5 = r5.hasMediaSession()
            if (r5 != 0) goto L_0x006c
            if (r4 == 0) goto L_0x006a
            goto L_0x006c
        L_0x006a:
            r4 = r0
            goto L_0x006d
        L_0x006c:
            r4 = r1
        L_0x006d:
            android.app.Notification r5 = r7.getNotification()
            java.lang.Class r5 = r5.getNotificationStyle()
            android.app.Notification r7 = r7.getNotification()
            java.lang.String r7 = r7.category
            java.lang.String r6 = "msg"
            boolean r7 = r6.equals(r7)
            java.lang.Class<android.app.Notification$MessagingStyle> r6 = android.app.Notification.MessagingStyle.class
            boolean r5 = r6.equals(r5)
            if (r7 == 0) goto L_0x008b
            if (r3 != 0) goto L_0x008d
        L_0x008b:
            if (r5 == 0) goto L_0x008f
        L_0x008d:
            if (r9 != 0) goto L_0x0095
        L_0x008f:
            if (r4 == 0) goto L_0x0093
            if (r2 != 0) goto L_0x0095
        L_0x0093:
            if (r8 == 0) goto L_0x0096
        L_0x0095:
            r0 = r1
        L_0x0096:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.BubbleController.shouldAutoBubbleForFlags(android.content.Context, com.android.systemui.statusbar.NotificationData$Entry):boolean");
    }

    private void updateShowInShadeForSuppressNotification(NotificationData.Entry entry) {
        Notification.BubbleMetadata bubbleMetadata = entry.notification.getNotification().getBubbleMetadata();
        entry.setShowInShadeWhenBubble(!(bubbleMetadata != null && bubbleMetadata.isNotificationSuppressed() && isForegroundApp(this.mContext, entry.notification.getPackageName())));
    }

    public static boolean isForegroundApp(Context context, String str) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService(ActivityManager.class)).getRunningTasks(1);
        if (runningTasks.isEmpty() || !str.equals(runningTasks.get(0).topActivity.getPackageName())) {
            return false;
        }
        return true;
    }

    private class BubbleTaskStackListener extends TaskStackChangeListener {
        private BubbleTaskStackListener() {
        }

        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (BubbleController.this.mStackView != null && runningTaskInfo.displayId == 0) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
        }

        public void onActivityLaunchOnSecondaryDisplayRerouted() {
            if (BubbleController.this.mStackView != null) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
        }

        public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (BubbleController.this.mStackView != null) {
                int i = runningTaskInfo.displayId;
                BubbleController bubbleController = BubbleController.this;
                if (i == bubbleController.getExpandedDisplayId(bubbleController.mContext)) {
                    BubbleController.this.mBubbleData.setExpanded(false);
                }
            }
        }
    }

    private static boolean shouldAutoBubbleMessages(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "experiment_autobubble_messaging", 0) != 0;
    }

    private static boolean shouldAutoBubbleOngoing(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "experiment_autobubble_ongoing", 0) != 0;
    }

    private static boolean shouldAutoBubbleAll(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "experiment_autobubble_all", 0) != 0;
    }

    private static boolean areBubblesEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "experiment_enable_bubbles", 1) != 0;
    }

    static boolean canLaunchInActivityView(Context context, NotificationData.Entry entry) {
        Notification.BubbleMetadata bubbleMetadata = entry.notification.getNotification().getBubbleMetadata();
        PendingIntent intent = bubbleMetadata != null ? bubbleMetadata.getIntent() : null;
        if (intent == null) {
            Log.w("BubbleController", "Unable to create bubble -- no intent");
            return false;
        }
        ActivityInfo resolveActivityInfo = intent.getIntent().resolveActivityInfo(context.getPackageManager(), 0);
        if (resolveActivityInfo == null) {
            Log.w("BubbleController", "Unable to send as bubble -- couldn't find activity info for intent: " + intent);
            return false;
        } else if (!ActivityInfo.isResizeableMode(resolveActivityInfo.resizeMode)) {
            Log.w("BubbleController", "Unable to send as bubble -- activity is not resizable for intent: " + intent);
            return false;
        } else if (resolveActivityInfo.documentLaunchMode != 2) {
            Log.w("BubbleController", "Unable to send as bubble -- activity is not documentLaunchMode=always for intent: " + intent);
            return false;
        } else if ((resolveActivityInfo.flags & Integer.MIN_VALUE) != 0) {
            return true;
        } else {
            Log.w("BubbleController", "Unable to send as bubble -- activity is not embeddable for intent: " + intent);
            return false;
        }
    }

    private class BubblesImeListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private BubblesImeListener() {
        }

        public void onImeVisibilityChanged(boolean z, int i) {
            if (BubbleController.this.mStackView != null && BubbleController.this.mStackView.getBubbleCount() > 0) {
                BubbleController.this.mStackView.post(new Runnable(z, i) {
                    public final /* synthetic */ boolean f$1;
                    public final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        BubbleController.BubblesImeListener.this.lambda$onImeVisibilityChanged$0$BubbleController$BubblesImeListener(this.f$1, this.f$2);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onImeVisibilityChanged$0 */
        public /* synthetic */ void lambda$onImeVisibilityChanged$0$BubbleController$BubblesImeListener(boolean z, int i) {
            BubbleController.this.mStackView.onImeVisibilityChanged(z, i);
        }
    }
}
