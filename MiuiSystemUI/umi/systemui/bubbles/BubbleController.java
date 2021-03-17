package com.android.systemui.bubbles;

import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.ZenModeConfig;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.util.SparseSetArray;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleLogger;
import com.android.systemui.bubbles.BubbleStackView;
import com.android.systemui.bubbles.BubbleViewInfoTask;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoveInterceptor;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.notification.NotificationChannelHelper;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.FloatingContentCoordinator;
import com.miui.systemui.events.ModalExitMode;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class BubbleController implements ConfigurationController.ConfigurationListener, Dumpable {
    private boolean mAddedToWindowManager = false;
    private IStatusBarService mBarService;
    private BubbleData mBubbleData;
    private final BubbleData.Listener mBubbleDataListener = new BubbleData.Listener() {
        /* class com.android.systemui.bubbles.BubbleController.AnonymousClass10 */

        @Override // com.android.systemui.bubbles.BubbleData.Listener
        public void applyUpdate(BubbleData.Update update) {
            NotificationEntry pendingOrActiveNotif;
            BubbleController.this.ensureStackViewCreated();
            BubbleController.this.loadOverflowBubblesFromDisk();
            if (BubbleController.this.mOverflowCallback != null) {
                BubbleController.this.mOverflowCallback.run();
            }
            if (update.expandedChanged && !update.expanded) {
                BubbleController.this.mStackView.setExpanded(false);
                BubbleController.this.mNotificationShadeWindowController.setForceHasTopUi(BubbleController.this.mHadTopUi);
            }
            ArrayList arrayList = new ArrayList(update.removedBubbles);
            ArrayList arrayList2 = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Pair pair = (Pair) it.next();
                Bubble bubble = (Bubble) pair.first;
                int intValue = ((Integer) pair.second).intValue();
                if (BubbleController.this.mStackView != null) {
                    BubbleController.this.mStackView.removeBubble(bubble);
                }
                if (intValue != 8) {
                    if (intValue == 5) {
                        arrayList2.add(bubble);
                    }
                    NotificationEntry pendingOrActiveNotif2 = BubbleController.this.mNotificationEntryManager.getPendingOrActiveNotif(bubble.getKey());
                    if (!BubbleController.this.mBubbleData.hasBubbleInStackWithKey(bubble.getKey())) {
                        if (BubbleController.this.mBubbleData.hasOverflowBubbleWithKey(bubble.getKey()) || !(!bubble.showInShade() || intValue == 5 || intValue == 9)) {
                            if (bubble.isBubble()) {
                                BubbleController.this.setIsBubble(bubble, false);
                            }
                            if (!(pendingOrActiveNotif2 == null || pendingOrActiveNotif2.getRow() == null)) {
                                pendingOrActiveNotif2.getRow().updateBubbleButton();
                            }
                        } else {
                            for (NotifCallback notifCallback : BubbleController.this.mCallbacks) {
                                if (pendingOrActiveNotif2 != null) {
                                    notifCallback.removeNotification(pendingOrActiveNotif2, 2);
                                }
                            }
                        }
                    }
                    if (pendingOrActiveNotif2 != null) {
                        if (BubbleController.this.mBubbleData.getBubblesInGroup(pendingOrActiveNotif2.getSbn().getGroupKey(), BubbleController.this.mNotificationEntryManager).isEmpty()) {
                            for (NotifCallback notifCallback2 : BubbleController.this.mCallbacks) {
                                notifCallback2.maybeCancelSummary(pendingOrActiveNotif2);
                            }
                        }
                    }
                }
            }
            BubbleController.this.mDataRepository.removeBubbles(BubbleController.this.mCurrentUserId, arrayList2);
            if (!(update.addedBubble == null || BubbleController.this.mStackView == null)) {
                BubbleController.this.mDataRepository.addBubble(BubbleController.this.mCurrentUserId, update.addedBubble);
                BubbleController.this.mStackView.addBubble(update.addedBubble);
            }
            if (!(update.updatedBubble == null || BubbleController.this.mStackView == null)) {
                BubbleController.this.mStackView.updateBubble(update.updatedBubble);
            }
            if (update.orderChanged && BubbleController.this.mStackView != null) {
                BubbleController.this.mDataRepository.addBubbles(BubbleController.this.mCurrentUserId, update.bubbles);
                BubbleController.this.mStackView.updateBubbleOrder(update.bubbles);
            }
            if (update.selectionChanged && BubbleController.this.mStackView != null) {
                BubbleController.this.mStackView.setSelectedBubble(update.selectedBubble);
                if (!(update.selectedBubble == null || (pendingOrActiveNotif = BubbleController.this.mNotificationEntryManager.getPendingOrActiveNotif(update.selectedBubble.getKey())) == null)) {
                    BubbleController.this.mNotificationGroupManager.updateSuppression(pendingOrActiveNotif);
                }
            }
            if (update.expandedChanged && update.expanded && BubbleController.this.mStackView != null) {
                BubbleController.this.mStackView.setExpanded(true);
                BubbleController bubbleController = BubbleController.this;
                bubbleController.mHadTopUi = bubbleController.mNotificationShadeWindowController.getForceHasTopUi();
                BubbleController.this.mNotificationShadeWindowController.setForceHasTopUi(true);
            }
            for (NotifCallback notifCallback3 : BubbleController.this.mCallbacks) {
                notifCallback3.invalidateNotifications("BubbleData.Listener.applyUpdate");
            }
            BubbleController.this.updateStack();
        }
    };
    private BubbleIconFactory mBubbleIconFactory;
    private ScrimView mBubbleScrim;
    private final List<NotifCallback> mCallbacks = new ArrayList();
    private final Context mContext;
    private int mCurrentUserId;
    private final BubbleDataRepository mDataRepository;
    private int mDensityDpi = 0;
    private BubbleExpandListener mExpandListener;
    private final FloatingContentCoordinator mFloatingContentCoordinator;
    private boolean mHadTopUi = false;
    private Handler mHandler = new Handler();
    private INotificationManager mINotificationManager;
    private boolean mImeVisible = false;
    private boolean mInflateSynchronously;
    private int mLayoutDirection = -1;
    private BubbleLogger mLogger = new BubbleLoggerImpl();
    private NotificationEntry mNotifEntryToExpandOnShadeUnlock;
    private final NotifPipeline mNotifPipeline;
    private final NotificationLockscreenUserManager mNotifUserManager;
    private final NotificationEntryManager mNotificationEntryManager;
    private final NotificationGroupManager mNotificationGroupManager;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private int mOrientation = 0;
    private Runnable mOverflowCallback = null;
    private boolean mOverflowDataLoaded = false;
    private final SparseSetArray<String> mSavedBubbleKeysPerUser;
    private final ShadeController mShadeController;
    private BubbleStackView mStackView;
    private StatusBarStateListener mStatusBarStateListener;
    private BubbleStackView.SurfaceSynchronizer mSurfaceSynchronizer;
    private SysUiState mSysUiState;
    private final BubbleTaskStackListener mTaskStackListener;
    private NotificationListenerService.Ranking mTmpRanking;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmLayoutParams;
    private final ZenModeController mZenModeController;

    public interface BubbleExpandListener {
        void onBubbleExpandChanged(boolean z, String str);
    }

    public interface NotifCallback {
        void invalidateNotifications(String str);

        void maybeCancelSummary(NotificationEntry notificationEntry);

        void removeNotification(NotificationEntry notificationEntry, int i);
    }

    public interface NotificationSuppressionChangedListener {
        void onBubbleNotificationSuppressionChange(Bubble bubble);
    }

    public interface PendingIntentCanceledListener {
        void onPendingIntentCanceled(Bubble bubble);
    }

    /* access modifiers changed from: private */
    public class StatusBarStateListener implements StatusBarStateController.StateListener {
        private int mState;

        private StatusBarStateListener() {
        }

        public int getCurrentState() {
            return this.mState;
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            this.mState = i;
            if (i != 0) {
                BubbleController.this.collapseStack();
            }
            if (BubbleController.this.mNotifEntryToExpandOnShadeUnlock != null) {
                BubbleController bubbleController = BubbleController.this;
                bubbleController.expandStackAndSelectBubble(bubbleController.mNotifEntryToExpandOnShadeUnlock);
                BubbleController.this.mNotifEntryToExpandOnShadeUnlock = null;
            }
            BubbleController.this.updateStack();
        }
    }

    public BubbleController(Context context, NotificationShadeWindowController notificationShadeWindowController, StatusBarStateController statusBarStateController, ShadeController shadeController, BubbleData bubbleData, BubbleStackView.SurfaceSynchronizer surfaceSynchronizer, ConfigurationController configurationController, NotificationInterruptStateProvider notificationInterruptStateProvider, ZenModeController zenModeController, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGroupManager notificationGroupManager, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, FeatureFlags featureFlags, DumpManager dumpManager, FloatingContentCoordinator floatingContentCoordinator, BubbleDataRepository bubbleDataRepository, SysUiState sysUiState, INotificationManager iNotificationManager, IStatusBarService iStatusBarService, WindowManager windowManager, LauncherApps launcherApps) {
        dumpManager.registerDumpable("Bubbles", this);
        this.mContext = context;
        this.mShadeController = shadeController;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mNotifUserManager = notificationLockscreenUserManager;
        this.mZenModeController = zenModeController;
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        this.mDataRepository = bubbleDataRepository;
        this.mINotificationManager = iNotificationManager;
        zenModeController.addCallback(new ZenModeController.Callback() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
            public void onConfigChanged(ZenModeConfig zenModeConfig) {
                for (Bubble bubble : BubbleController.this.mBubbleData.getBubbles()) {
                    bubble.setShowDot(bubble.showInShade());
                }
            }
        });
        configurationController.addCallback(this);
        this.mSysUiState = sysUiState;
        this.mBubbleData = bubbleData;
        bubbleData.setListener(this.mBubbleDataListener);
        this.mBubbleData.setSuppressionChangedListener(new NotificationSuppressionChangedListener() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass2 */

            @Override // com.android.systemui.bubbles.BubbleController.NotificationSuppressionChangedListener
            public void onBubbleNotificationSuppressionChange(Bubble bubble) {
                try {
                    BubbleController.this.mBarService.onBubbleNotificationSuppressionChanged(bubble.getKey(), !bubble.showInShade());
                } catch (RemoteException unused) {
                }
            }
        });
        this.mBubbleData.setPendingIntentCancelledListener(new PendingIntentCanceledListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleController$nWhc82cKJiqwvfEf64TRF3Q5U6g */

            @Override // com.android.systemui.bubbles.BubbleController.PendingIntentCanceledListener
            public final void onPendingIntentCanceled(Bubble bubble) {
                BubbleController.this.lambda$new$1$BubbleController(bubble);
            }
        });
        this.mNotificationEntryManager = notificationEntryManager;
        this.mNotificationGroupManager = notificationGroupManager;
        this.mNotifPipeline = notifPipeline;
        if (!featureFlags.isNewNotifPipelineRenderingEnabled()) {
            setupNEM();
        } else {
            setupNotifPipeline();
        }
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        StatusBarStateListener statusBarStateListener = new StatusBarStateListener();
        this.mStatusBarStateListener = statusBarStateListener;
        statusBarStateController.addCallback(statusBarStateListener);
        this.mTaskStackListener = new BubbleTaskStackListener();
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new BubblesImeListener());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mSurfaceSynchronizer = surfaceSynchronizer;
        this.mWindowManager = windowManager;
        this.mBarService = iStatusBarService == null ? IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")) : iStatusBarService;
        ScrimView scrimView = new ScrimView(this.mContext);
        this.mBubbleScrim = scrimView;
        scrimView.setImportantForAccessibility(2);
        this.mSavedBubbleKeysPerUser = new SparseSetArray<>();
        this.mCurrentUserId = this.mNotifUserManager.getCurrentUserId();
        this.mNotifUserManager.addUserChangedListener(new NotificationLockscreenUserManager.UserChangedListener() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
            public void onUserChanged(int i) {
                BubbleController bubbleController = BubbleController.this;
                bubbleController.saveBubbles(bubbleController.mCurrentUserId);
                BubbleController.this.mBubbleData.dismissAll(8);
                BubbleController.this.restoreBubbles(i);
                BubbleController.this.mCurrentUserId = i;
            }
        });
        this.mBubbleIconFactory = new BubbleIconFactory(context);
        launcherApps.registerCallback(new LauncherApps.Callback() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass4 */

            public void onPackageAdded(String str, UserHandle userHandle) {
            }

            public void onPackageChanged(String str, UserHandle userHandle) {
            }

            public void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z) {
            }

            public void onPackageRemoved(String str, UserHandle userHandle) {
                BubbleController.this.mBubbleData.removeBubblesWithPackageName(str, 13);
            }

            public void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z) {
                for (String str : strArr) {
                    BubbleController.this.mBubbleData.removeBubblesWithPackageName(str, 13);
                }
            }

            @Override // android.content.pm.LauncherApps.Callback
            public void onShortcutsChanged(String str, List<ShortcutInfo> list, UserHandle userHandle) {
                super.onShortcutsChanged(str, list, userHandle);
                BubbleController.this.mBubbleData.removeBubblesWithInvalidShortcuts(str, list, 12);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$BubbleController(Bubble bubble) {
        if (bubble.getBubbleIntent() != null) {
            if (bubble.isIntentActive()) {
                bubble.setPendingIntentCanceled();
            } else {
                this.mHandler.post(new Runnable(bubble) {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleController$itibjTNfFjFc1_LsSQYkBCjvNQ */
                    public final /* synthetic */ Bubble f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        BubbleController.this.lambda$new$0$BubbleController(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$BubbleController(Bubble bubble) {
        removeBubble(bubble.getKey(), 10);
    }

    public void addNotifCallback(NotifCallback notifCallback) {
        this.mCallbacks.add(notifCallback);
    }

    public void hideCurrentInputMethod() {
        try {
            this.mBarService.hideCurrentInputMethodForBubbles();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setupNEM() {
        this.mNotificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass5 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryAdded(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryUpdated(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                BubbleController.this.onEntryRemoved(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
                BubbleController.this.onRankingUpdated(rankingMap);
            }
        });
        this.mNotificationEntryManager.addNotificationRemoveInterceptor(new NotificationRemoveInterceptor() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass6 */

            @Override // com.android.systemui.statusbar.NotificationRemoveInterceptor
            public boolean onNotificationRemoveRequested(String str, NotificationEntry notificationEntry, int i) {
                boolean z = true;
                boolean z2 = i == 3;
                boolean z3 = i == 2 || i == 1;
                boolean z4 = i == 8 || i == 9;
                boolean z5 = i == 12;
                if ((notificationEntry == null || !notificationEntry.isRowDismissed() || z4) && !z2 && !z3 && !z5) {
                    z = false;
                }
                if (z) {
                    return BubbleController.this.handleDismissalInterception(notificationEntry);
                }
                return false;
            }
        });
        this.mNotificationGroupManager.addOnGroupChangeListener(new NotificationGroupManager.OnGroupChangeListener() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass7 */

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupSuppressionChanged(NotificationGroupManager.NotificationGroup notificationGroup, boolean z) {
                NotificationEntry notificationEntry = notificationGroup.summary;
                String groupKey = notificationEntry != null ? notificationEntry.getSbn().getGroupKey() : null;
                if (!z && groupKey != null && BubbleController.this.mBubbleData.isSummarySuppressed(groupKey)) {
                    BubbleController.this.mBubbleData.removeSuppressedSummary(groupKey);
                }
            }
        });
        addNotifCallback(new NotifCallback() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass8 */

            @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
            public void removeNotification(NotificationEntry notificationEntry, int i) {
                BubbleController.this.mNotificationEntryManager.performRemoveNotification(notificationEntry.getSbn(), i);
            }

            @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
            public void invalidateNotifications(String str) {
                BubbleController.this.mNotificationEntryManager.updateNotifications(str);
            }

            @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
            public void maybeCancelSummary(NotificationEntry notificationEntry) {
                String groupKey = notificationEntry.getSbn().getGroupKey();
                if (BubbleController.this.mBubbleData.isSummarySuppressed(groupKey)) {
                    BubbleController.this.mBubbleData.removeSuppressedSummary(groupKey);
                    NotificationEntry activeNotificationUnfiltered = BubbleController.this.mNotificationEntryManager.getActiveNotificationUnfiltered(BubbleController.this.mBubbleData.getSummaryKey(groupKey));
                    if (activeNotificationUnfiltered != null) {
                        BubbleController.this.mNotificationEntryManager.performRemoveNotification(activeNotificationUnfiltered.getSbn(), 0);
                    }
                }
                NotificationEntry logicalGroupSummary = BubbleController.this.mNotificationGroupManager.getLogicalGroupSummary(notificationEntry.getSbn());
                if (logicalGroupSummary != null) {
                    ArrayList<NotificationEntry> logicalChildren = BubbleController.this.mNotificationGroupManager.getLogicalChildren(logicalGroupSummary.getSbn());
                    if (logicalGroupSummary.getKey().equals(notificationEntry.getKey())) {
                        return;
                    }
                    if (logicalChildren == null || logicalChildren.isEmpty()) {
                        BubbleController.this.mNotificationEntryManager.performRemoveNotification(logicalGroupSummary.getSbn(), 0);
                    }
                }
            }
        });
    }

    private void setupNotifPipeline() {
        this.mNotifPipeline.addCollectionListener(new NotifCollectionListener() {
            /* class com.android.systemui.bubbles.BubbleController.AnonymousClass9 */

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryAdded(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryAdded(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryUpdated(NotificationEntry notificationEntry) {
                BubbleController.this.onEntryUpdated(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
                BubbleController.this.onRankingUpdated(rankingMap);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
                BubbleController.this.onEntryRemoved(notificationEntry);
            }
        });
    }

    public ScrimView getScrimForBubble() {
        return this.mBubbleScrim;
    }

    public void onStatusBarVisibilityChanged(boolean z) {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setTemporarilyInvisible(!z && !isStackExpanded());
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setInflateSynchronously(boolean z) {
        this.mInflateSynchronously = z;
    }

    /* access modifiers changed from: package-private */
    public void setOverflowCallback(Runnable runnable) {
        this.mOverflowCallback = runnable;
    }

    /* access modifiers changed from: package-private */
    public List<Bubble> getOverflowBubbles() {
        return this.mBubbleData.getOverflowBubbles();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ensureStackViewCreated() {
        if (this.mStackView == null) {
            BubbleStackView bubbleStackView = new BubbleStackView(this.mContext, this.mBubbleData, this.mSurfaceSynchronizer, this.mFloatingContentCoordinator, this.mSysUiState, new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleController$9ZgvorygajtGrm7C34N9mjHiRg4 */

                public final void run() {
                    BubbleController.this.onAllBubblesAnimatedOut();
                }
            }, new Consumer() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleController$n5Txkm3_6gK60tp4RYvQGkCgICc */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BubbleController.this.onImeVisibilityChanged(((Boolean) obj).booleanValue());
                }
            }, new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$jFCV6yHjrilnLmu1dvU8ruP2Llk */

                public final void run() {
                    BubbleController.this.hideCurrentInputMethod();
                }
            });
            this.mStackView = bubbleStackView;
            bubbleStackView.addView(this.mBubbleScrim);
            BubbleExpandListener bubbleExpandListener = this.mExpandListener;
            if (bubbleExpandListener != null) {
                this.mStackView.setExpandListener(bubbleExpandListener);
            }
            this.mStackView.setUnbubbleConversationCallback(new Consumer() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleController$QJVf62b5wCS3J_DHWUbuCKTKs3M */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BubbleController.this.lambda$ensureStackViewCreated$2$BubbleController((String) obj);
                }
            });
        }
        addToWindowManagerMaybe();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$ensureStackViewCreated$2 */
    public /* synthetic */ void lambda$ensureStackViewCreated$2$BubbleController(String str) {
        NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(str);
        if (pendingOrActiveNotif != null) {
            onUserChangedBubble(pendingOrActiveNotif, false);
        }
    }

    private void addToWindowManagerMaybe() {
        if (this.mStackView != null && !this.mAddedToWindowManager) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2042, 16777224, -3);
            this.mWmLayoutParams = layoutParams;
            layoutParams.setFitInsetsTypes(0);
            WindowManager.LayoutParams layoutParams2 = this.mWmLayoutParams;
            layoutParams2.softInputMode = 16;
            layoutParams2.token = new Binder();
            this.mWmLayoutParams.setTitle("Bubbles!");
            this.mWmLayoutParams.packageName = this.mContext.getPackageName();
            WindowManager.LayoutParams layoutParams3 = this.mWmLayoutParams;
            layoutParams3.layoutInDisplayCutoutMode = 3;
            try {
                this.mAddedToWindowManager = true;
                this.mWindowManager.addView(this.mStackView, layoutParams3);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                updateWmFlags();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onImeVisibilityChanged(boolean z) {
        this.mImeVisible = z;
        updateWmFlags();
    }

    private void removeFromWindowManagerMaybe() {
        if (this.mAddedToWindowManager) {
            try {
                this.mAddedToWindowManager = false;
                if (this.mStackView != null) {
                    this.mWindowManager.removeView(this.mStackView);
                    this.mStackView.removeView(this.mBubbleScrim);
                    this.mStackView = null;
                    return;
                }
                Log.w("Bubbles", "StackView added to WindowManager, but was null when removing!");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWmFlags() {
        if (this.mStackView != null) {
            if (!isStackExpanded() || this.mImeVisible) {
                this.mWmLayoutParams.flags |= 8;
            } else {
                this.mWmLayoutParams.flags &= -9;
            }
            if (this.mAddedToWindowManager) {
                try {
                    this.mWindowManager.updateViewLayout(this.mStackView, this.mWmLayoutParams);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onAllBubblesAnimatedOut() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setVisibility(4);
            removeFromWindowManagerMaybe();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveBubbles(int i) {
        this.mSavedBubbleKeysPerUser.remove(i);
        for (Bubble bubble : this.mBubbleData.getBubbles()) {
            this.mSavedBubbleKeysPerUser.add(i, bubble.getKey());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreBubbles(int i) {
        ArraySet arraySet = this.mSavedBubbleKeysPerUser.get(i);
        if (arraySet != null) {
            for (NotificationEntry notificationEntry : this.mNotificationEntryManager.getActiveNotificationsForCurrentUser()) {
                if (arraySet.contains(notificationEntry.getKey()) && this.mNotificationInterruptStateProvider.shouldBubbleUp(notificationEntry) && notificationEntry.isBubble() && canLaunchInActivityView(this.mContext, notificationEntry)) {
                    updateBubble(notificationEntry, true, false);
                }
            }
            this.mSavedBubbleKeysPerUser.remove(this.mCurrentUserId);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        updateForThemeChanges();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        updateForThemeChanges();
    }

    private void updateForThemeChanges() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.onThemeChanged();
        }
        this.mBubbleIconFactory = new BubbleIconFactory(this.mContext);
        for (Bubble bubble : this.mBubbleData.getBubbles()) {
            bubble.inflate(null, this.mContext, this.mStackView, this.mBubbleIconFactory, false);
        }
        for (Bubble bubble2 : this.mBubbleData.getOverflowBubbles()) {
            bubble2.inflate(null, this.mContext, this.mStackView, this.mBubbleIconFactory, false);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null && configuration != null) {
            int i = configuration.orientation;
            if (i != this.mOrientation) {
                this.mOrientation = i;
                bubbleStackView.onOrientationChanged(i);
            }
            int i2 = configuration.densityDpi;
            if (i2 != this.mDensityDpi) {
                this.mDensityDpi = i2;
                this.mBubbleIconFactory = new BubbleIconFactory(this.mContext);
                this.mStackView.onDisplaySizeChanged();
            }
            if (configuration.getLayoutDirection() != this.mLayoutDirection) {
                int layoutDirection = configuration.getLayoutDirection();
                this.mLayoutDirection = layoutDirection;
                this.mStackView.onLayoutDirectionChanged(layoutDirection);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inLandscape() {
        return this.mOrientation == 2;
    }

    public void setExpandListener(BubbleExpandListener bubbleExpandListener) {
        $$Lambda$BubbleController$0VC4vw4gvzIdDv19h0ZrywF_riU r0 = new BubbleExpandListener(bubbleExpandListener) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleController$0VC4vw4gvzIdDv19h0ZrywF_riU */
            public final /* synthetic */ BubbleController.BubbleExpandListener f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.bubbles.BubbleController.BubbleExpandListener
            public final void onBubbleExpandChanged(boolean z, String str) {
                BubbleController.this.lambda$setExpandListener$3$BubbleController(this.f$1, z, str);
            }
        };
        this.mExpandListener = r0;
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.setExpandListener(r0);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setExpandListener$3 */
    public /* synthetic */ void lambda$setExpandListener$3$BubbleController(BubbleExpandListener bubbleExpandListener, boolean z, String str) {
        if (bubbleExpandListener != null) {
            bubbleExpandListener.onBubbleExpandChanged(z, str);
        }
        updateWmFlags();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
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

    public boolean isBubbleNotificationSuppressedFromShade(NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        boolean z = this.mBubbleData.hasAnyBubbleWithKey(key) && !this.mBubbleData.getAnyBubbleWithkey(key).showInShade();
        String groupKey = notificationEntry.getSbn().getGroupKey();
        return (key.equals(this.mBubbleData.getSummaryKey(groupKey)) && this.mBubbleData.isSummarySuppressed(groupKey)) || z;
    }

    public boolean isBubbleExpanded(NotificationEntry notificationEntry) {
        BubbleData bubbleData;
        return isStackExpanded() && (bubbleData = this.mBubbleData) != null && bubbleData.getSelectedBubble() != null && this.mBubbleData.getSelectedBubble().getKey().equals(notificationEntry.getKey());
    }

    /* access modifiers changed from: package-private */
    public void promoteBubbleFromOverflow(Bubble bubble) {
        this.mLogger.log(bubble, BubbleLogger.Event.BUBBLE_OVERFLOW_REMOVE_BACK_TO_STACK);
        bubble.setInflateSynchronously(this.mInflateSynchronously);
        bubble.setShouldAutoExpand(true);
        bubble.markAsAccessedAt(System.currentTimeMillis());
        setIsBubble(bubble, true);
    }

    public void expandStackAndSelectBubble(NotificationEntry notificationEntry) {
        if (this.mStatusBarStateListener.getCurrentState() == 0) {
            this.mNotifEntryToExpandOnShadeUnlock = null;
            String key = notificationEntry.getKey();
            Bubble bubbleInStackWithKey = this.mBubbleData.getBubbleInStackWithKey(key);
            if (bubbleInStackWithKey != null) {
                this.mBubbleData.setSelectedBubble(bubbleInStackWithKey);
                this.mBubbleData.setExpanded(true);
                return;
            }
            Bubble overflowBubbleWithKey = this.mBubbleData.getOverflowBubbleWithKey(key);
            if (overflowBubbleWithKey != null) {
                promoteBubbleFromOverflow(overflowBubbleWithKey);
            } else if (notificationEntry.canBubble()) {
                setIsBubble(notificationEntry, true, true);
            }
        } else {
            this.mNotifEntryToExpandOnShadeUnlock = notificationEntry;
        }
    }

    public void onUserChangedImportance(NotificationEntry notificationEntry) {
        try {
            this.mBarService.onNotificationBubbleChanged(notificationEntry.getKey(), true, 3);
        } catch (RemoteException e) {
            Log.e("Bubbles", e.getMessage());
        }
        this.mShadeController.collapsePanel(true);
        if (notificationEntry.getRow() != null) {
            notificationEntry.getRow().updateBubbleButton();
        }
    }

    public void performBackPressIfNeeded() {
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.performBackPressIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBubble(NotificationEntry notificationEntry) {
        updateBubble(notificationEntry, false, true);
    }

    /* access modifiers changed from: package-private */
    public void loadOverflowBubblesFromDisk() {
        if (this.mBubbleData.getOverflowBubbles().isEmpty() && !this.mOverflowDataLoaded) {
            this.mOverflowDataLoaded = true;
            this.mDataRepository.loadBubbles(new Function1() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleController$DOCWkNShFlaO6cGprPyPx98P4oE */

                @Override // kotlin.jvm.functions.Function1
                public final Object invoke(Object obj) {
                    return BubbleController.this.lambda$loadOverflowBubblesFromDisk$6$BubbleController((List) obj);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadOverflowBubblesFromDisk$6 */
    public /* synthetic */ Unit lambda$loadOverflowBubblesFromDisk$6$BubbleController(List list) {
        list.forEach(new Consumer() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleController$_mg4UF9QSoehsZuSwJtOultVl1U */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BubbleController.this.lambda$loadOverflowBubblesFromDisk$5$BubbleController((Bubble) obj);
            }
        });
        return null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadOverflowBubblesFromDisk$5 */
    public /* synthetic */ void lambda$loadOverflowBubblesFromDisk$5$BubbleController(Bubble bubble) {
        if (!this.mBubbleData.hasAnyBubbleWithKey(bubble.getKey())) {
            bubble.inflate(new BubbleViewInfoTask.Callback(bubble) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleController$2Y1b47AMtxrttMHzCCBd8aAXiw */
                public final /* synthetic */ Bubble f$1;

                {
                    this.f$1 = r2;
                }

                @Override // com.android.systemui.bubbles.BubbleViewInfoTask.Callback
                public final void onBubbleViewsReady(Bubble bubble) {
                    BubbleController.this.lambda$loadOverflowBubblesFromDisk$4$BubbleController(this.f$1, bubble);
                }
            }, this.mContext, this.mStackView, this.mBubbleIconFactory, true);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadOverflowBubblesFromDisk$4 */
    public /* synthetic */ void lambda$loadOverflowBubblesFromDisk$4$BubbleController(Bubble bubble, Bubble bubble2) {
        this.mBubbleData.overflowBubble(2, bubble);
    }

    /* access modifiers changed from: package-private */
    public void updateBubble(NotificationEntry notificationEntry, boolean z, boolean z2) {
        if (notificationEntry.getImportance() >= 4) {
            notificationEntry.setInterruption();
        }
        inflateAndAdd(this.mBubbleData.getOrCreateBubble(notificationEntry, null), z, z2);
    }

    /* access modifiers changed from: package-private */
    public void inflateAndAdd(Bubble bubble, boolean z, boolean z2) {
        ensureStackViewCreated();
        bubble.setInflateSynchronously(this.mInflateSynchronously);
        bubble.inflate(new BubbleViewInfoTask.Callback(z, z2) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleController$kBf4keAqVIQRxl9SNI0prTDdem4 */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.systemui.bubbles.BubbleViewInfoTask.Callback
            public final void onBubbleViewsReady(Bubble bubble) {
                BubbleController.this.lambda$inflateAndAdd$7$BubbleController(this.f$1, this.f$2, bubble);
            }
        }, this.mContext, this.mStackView, this.mBubbleIconFactory, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$inflateAndAdd$7 */
    public /* synthetic */ void lambda$inflateAndAdd$7$BubbleController(boolean z, boolean z2, Bubble bubble) {
        this.mBubbleData.notificationEntryUpdated(bubble, z, z2);
    }

    public void onUserChangedBubble(NotificationEntry notificationEntry, boolean z) {
        NotificationChannel channel = notificationEntry.getChannel();
        String packageName = notificationEntry.getSbn().getPackageName();
        int uid = notificationEntry.getSbn().getUid();
        if (channel != null && packageName != null) {
            try {
                this.mBarService.onNotificationBubbleChanged(notificationEntry.getKey(), z, 3);
            } catch (RemoteException unused) {
            }
            NotificationChannel createConversationChannelIfNeeded = NotificationChannelHelper.createConversationChannelIfNeeded(this.mContext, this.mINotificationManager, notificationEntry, channel);
            createConversationChannelIfNeeded.setAllowBubbles(z);
            try {
                int bubblePreferenceForPackage = this.mINotificationManager.getBubblePreferenceForPackage(packageName, uid);
                if (z && bubblePreferenceForPackage == 0) {
                    this.mINotificationManager.setBubblesAllowed(packageName, uid, 2);
                }
                this.mINotificationManager.updateNotificationChannelForPackage(packageName, uid, createConversationChannelIfNeeded);
            } catch (RemoteException e) {
                Log.e("Bubbles", e.getMessage());
            }
            if (z) {
                this.mShadeController.collapsePanel(true);
                ((ModalController) Dependency.get(ModalController.class)).animExitModal(150, true, ModalExitMode.OTHER.name());
                if (notificationEntry.getRow() != null) {
                    notificationEntry.getRow().updateBubbleButton();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeBubble(String str, int i) {
        if (this.mBubbleData.hasAnyBubbleWithKey(str)) {
            this.mBubbleData.dismissBubbleWithKey(str, i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEntryAdded(NotificationEntry notificationEntry) {
        if (this.mNotificationInterruptStateProvider.shouldBubbleUp(notificationEntry) && notificationEntry.isBubble() && canLaunchInActivityView(this.mContext, notificationEntry)) {
            updateBubble(notificationEntry);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEntryUpdated(NotificationEntry notificationEntry) {
        boolean z = this.mNotificationInterruptStateProvider.shouldBubbleUp(notificationEntry) && canLaunchInActivityView(this.mContext, notificationEntry);
        if (!z && this.mBubbleData.hasAnyBubbleWithKey(notificationEntry.getKey())) {
            removeBubble(notificationEntry.getKey(), 7);
        } else if (z && notificationEntry.isBubble()) {
            updateBubble(notificationEntry);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEntryRemoved(NotificationEntry notificationEntry) {
        if (isSummaryOfBubbles(notificationEntry)) {
            String groupKey = notificationEntry.getSbn().getGroupKey();
            this.mBubbleData.removeSuppressedSummary(groupKey);
            ArrayList<Bubble> bubblesInGroup = this.mBubbleData.getBubblesInGroup(groupKey, this.mNotificationEntryManager);
            for (int i = 0; i < bubblesInGroup.size(); i++) {
                removeBubble(bubblesInGroup.get(i).getKey(), 9);
            }
            return;
        }
        removeBubble(notificationEntry.getKey(), 5);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
        if (this.mTmpRanking == null) {
            this.mTmpRanking = new NotificationListenerService.Ranking();
        }
        String[] orderedKeys = rankingMap.getOrderedKeys();
        for (String str : orderedKeys) {
            NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(str);
            rankingMap.getRanking(str, this.mTmpRanking);
            boolean hasAnyBubbleWithKey = this.mBubbleData.hasAnyBubbleWithKey(str);
            if (hasAnyBubbleWithKey && !this.mTmpRanking.canBubble()) {
                this.mBubbleData.dismissBubbleWithKey(pendingOrActiveNotif.getKey(), 4);
            } else if (pendingOrActiveNotif != null && this.mTmpRanking.isBubble() && !hasAnyBubbleWithKey) {
                pendingOrActiveNotif.setFlagBubble(true);
                onEntryUpdated(pendingOrActiveNotif);
            }
        }
    }

    private void setIsBubble(NotificationEntry notificationEntry, boolean z, boolean z2) {
        Objects.requireNonNull(notificationEntry);
        if (z) {
            notificationEntry.getSbn().getNotification().flags |= 4096;
        } else {
            notificationEntry.getSbn().getNotification().flags &= -4097;
        }
        int i = 0;
        if (z2) {
            i = 3;
        }
        try {
            this.mBarService.onNotificationBubbleChanged(notificationEntry.getKey(), z, i);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setIsBubble(Bubble bubble, boolean z) {
        Objects.requireNonNull(bubble);
        bubble.setIsBubble(z);
        NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(bubble.getKey());
        if (pendingOrActiveNotif != null) {
            setIsBubble(pendingOrActiveNotif, z, bubble.shouldAutoExpand());
        } else if (z) {
            Bubble orCreateBubble = this.mBubbleData.getOrCreateBubble(null, bubble);
            inflateAndAdd(orCreateBubble, orCreateBubble.shouldAutoExpand(), !orCreateBubble.shouldAutoExpand());
        }
    }

    public boolean handleDismissalInterception(NotificationEntry notificationEntry) {
        if (notificationEntry == null) {
            return false;
        }
        if (isSummaryOfBubbles(notificationEntry)) {
            handleSummaryDismissalInterception(notificationEntry);
        } else {
            Bubble bubbleInStackWithKey = this.mBubbleData.getBubbleInStackWithKey(notificationEntry.getKey());
            if (bubbleInStackWithKey == null || !notificationEntry.isBubble()) {
                bubbleInStackWithKey = this.mBubbleData.getOverflowBubbleWithKey(notificationEntry.getKey());
            }
            if (bubbleInStackWithKey == null) {
                return false;
            }
            bubbleInStackWithKey.setSuppressNotification(true);
            bubbleInStackWithKey.setShowDot(false);
        }
        for (NotifCallback notifCallback : this.mCallbacks) {
            notifCallback.invalidateNotifications("BubbleController.handleDismissalInterception");
        }
        return true;
    }

    private boolean isSummaryOfBubbles(NotificationEntry notificationEntry) {
        if (notificationEntry == null) {
            return false;
        }
        String groupKey = notificationEntry.getSbn().getGroupKey();
        ArrayList<Bubble> bubblesInGroup = this.mBubbleData.getBubblesInGroup(groupKey, this.mNotificationEntryManager);
        boolean z = this.mBubbleData.isSummarySuppressed(groupKey) && this.mBubbleData.getSummaryKey(groupKey).equals(notificationEntry.getKey());
        boolean isGroupSummary = notificationEntry.getSbn().getNotification().isGroupSummary();
        if ((z || isGroupSummary) && bubblesInGroup != null && !bubblesInGroup.isEmpty()) {
            return true;
        }
        return false;
    }

    private void handleSummaryDismissalInterception(NotificationEntry notificationEntry) {
        List<NotificationEntry> attachedNotifChildren = notificationEntry.getAttachedNotifChildren();
        if (attachedNotifChildren != null) {
            for (int i = 0; i < attachedNotifChildren.size(); i++) {
                NotificationEntry notificationEntry2 = attachedNotifChildren.get(i);
                if (this.mBubbleData.hasAnyBubbleWithKey(notificationEntry2.getKey())) {
                    Bubble anyBubbleWithkey = this.mBubbleData.getAnyBubbleWithkey(notificationEntry2.getKey());
                    if (anyBubbleWithkey != null) {
                        NotificationEntry pendingOrActiveNotif = this.mNotificationEntryManager.getPendingOrActiveNotif(anyBubbleWithkey.getKey());
                        if (pendingOrActiveNotif != null) {
                            this.mNotificationGroupManager.onEntryRemoved(pendingOrActiveNotif);
                        }
                        anyBubbleWithkey.setSuppressNotification(true);
                        anyBubbleWithkey.setShowDot(false);
                    }
                } else {
                    for (NotifCallback notifCallback : this.mCallbacks) {
                        notifCallback.removeNotification(notificationEntry2, 12);
                    }
                }
            }
        }
        this.mNotificationGroupManager.onEntryRemoved(notificationEntry);
        this.mBubbleData.addSummaryToSuppress(notificationEntry.getSbn().getGroupKey(), notificationEntry.getKey());
    }

    public void updateStack() {
        if (this.mStackView != null) {
            if (this.mStatusBarStateListener.getCurrentState() != 0) {
                this.mStackView.setVisibility(4);
            } else if (hasBubbles()) {
                this.mStackView.setVisibility(0);
            }
            this.mStackView.updateContentDescription();
        }
    }

    public int getExpandedDisplayId(Context context) {
        if (this.mStackView == null) {
            return -1;
        }
        boolean z = context.getDisplay() != null && context.getDisplay().getDisplayId() == 0;
        BubbleViewProvider expandedBubble = this.mStackView.getExpandedBubble();
        if (!z || expandedBubble == null || !isStackExpanded() || this.mNotificationShadeWindowController.getPanelExpanded()) {
            return -1;
        }
        return expandedBubble.getDisplayId();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public BubbleStackView getStackView() {
        return this.mStackView;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BubbleController state:");
        this.mBubbleData.dump(fileDescriptor, printWriter, strArr);
        printWriter.println();
        BubbleStackView bubbleStackView = this.mStackView;
        if (bubbleStackView != null) {
            bubbleStackView.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println();
    }

    private class BubbleTaskStackListener extends TaskStackChangeListener {
        private BubbleTaskStackListener() {
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (BubbleController.this.mStackView != null && runningTaskInfo.displayId == 0 && !BubbleController.this.mStackView.isExpansionAnimating()) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            for (Bubble bubble : BubbleController.this.mBubbleData.getBubbles()) {
                if (bubble.getDisplayId() == runningTaskInfo.displayId) {
                    BubbleController.this.mBubbleData.setSelectedBubble(bubble);
                    BubbleController.this.mBubbleData.setExpanded(true);
                    return;
                }
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityLaunchOnSecondaryDisplayRerouted() {
            if (BubbleController.this.mStackView != null) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (BubbleController.this.mStackView != null) {
                int i = runningTaskInfo.displayId;
                BubbleController bubbleController = BubbleController.this;
                if (i != bubbleController.getExpandedDisplayId(bubbleController.mContext)) {
                    return;
                }
                if (BubbleController.this.mImeVisible) {
                    BubbleController.this.hideCurrentInputMethod();
                } else {
                    BubbleController.this.mBubbleData.setExpanded(false);
                }
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onSingleTaskDisplayDrawn(int i) {
            if (BubbleController.this.mStackView != null) {
                BubbleController.this.mStackView.showExpandedViewContents(i);
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onSingleTaskDisplayEmpty(int i) {
            BubbleViewProvider expandedBubble = BubbleController.this.mStackView != null ? BubbleController.this.mStackView.getExpandedBubble() : null;
            int displayId = expandedBubble != null ? expandedBubble.getDisplayId() : -1;
            if (BubbleController.this.mStackView != null && BubbleController.this.mStackView.isExpanded() && displayId == i) {
                BubbleController.this.mBubbleData.setExpanded(false);
            }
            BubbleController.this.mBubbleData.notifyDisplayEmpty(i);
        }
    }

    static boolean canLaunchInActivityView(Context context, NotificationEntry notificationEntry) {
        PendingIntent intent = notificationEntry.getBubbleMetadata() != null ? notificationEntry.getBubbleMetadata().getIntent() : null;
        if (notificationEntry.getBubbleMetadata() != null && notificationEntry.getBubbleMetadata().getShortcutId() != null) {
            return true;
        }
        if (intent == null) {
            Log.w("Bubbles", "Unable to create bubble -- no intent: " + notificationEntry.getKey());
            return false;
        }
        ActivityInfo resolveActivityInfo = intent.getIntent().resolveActivityInfo(StatusBar.getPackageManagerForUser(context, notificationEntry.getSbn().getUser().getIdentifier()), 0);
        if (resolveActivityInfo == null) {
            Log.w("Bubbles", "Unable to send as bubble, " + notificationEntry.getKey() + " couldn't find activity info for intent: " + intent);
            return false;
        } else if (ActivityInfo.isResizeableMode(resolveActivityInfo.resizeMode)) {
            return true;
        } else {
            Log.w("Bubbles", "Unable to send as bubble, " + notificationEntry.getKey() + " activity is not resizable for intent: " + intent);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class BubblesImeListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private BubblesImeListener() {
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(boolean z, int i) {
            if (BubbleController.this.mStackView != null) {
                BubbleController.this.mStackView.post(new Runnable(z, i) {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleController$BubblesImeListener$k3Ccv01hiK8jFFaKEuMmcHqId4 */
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
