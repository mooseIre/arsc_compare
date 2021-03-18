package com.android.systemui.statusbar.notification.row;

import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ImageMessageConsumer;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.InflationTask;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.ConversationNotificationProcessor;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.MediaNotificationProcessor;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.InflatedSmartReplies;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.util.Assert;
import dagger.Lazy;
import java.util.HashMap;
import java.util.concurrent.Executor;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class NotificationContentInflater implements NotificationRowContentBinder {
    private final Executor mBgExecutor;
    private final ConversationNotificationProcessor mConversationProcessor;
    private boolean mInflateSynchronously = false;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final NotifRemoteViewCache mRemoteViewCache;
    private final Lazy<SmartReplyConstants> mSmartReplyConstants;
    private final Lazy<SmartReplyController> mSmartReplyController;

    NotificationContentInflater(NotifRemoteViewCache notifRemoteViewCache, NotificationRemoteInputManager notificationRemoteInputManager, Lazy<SmartReplyConstants> lazy, Lazy<SmartReplyController> lazy2, ConversationNotificationProcessor conversationNotificationProcessor, Executor executor) {
        this.mRemoteViewCache = notifRemoteViewCache;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mSmartReplyConstants = lazy;
        this.mSmartReplyController = lazy2;
        this.mConversationProcessor = conversationNotificationProcessor;
        this.mBgExecutor = executor;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder
    public void bindContent(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i, NotificationRowContentBinder.BindParams bindParams, boolean z, NotificationRowContentBinder.InflationCallback inflationCallback) {
        if (!expandableNotificationRow.isRemoved()) {
            expandableNotificationRow.getImageResolver().preloadImages(notificationEntry.getSbn().getNotification());
            if (z) {
                this.mRemoteViewCache.clearCache(notificationEntry);
            }
            cancelContentViewFrees(expandableNotificationRow, i);
            AsyncInflationTask asyncInflationTask = new AsyncInflationTask(this.mBgExecutor, this.mInflateSynchronously, i, this.mRemoteViewCache, notificationEntry, this.mSmartReplyConstants.get(), this.mSmartReplyController.get(), this.mConversationProcessor, expandableNotificationRow, bindParams.isLowPriority, bindParams.usesIncreasedHeight, bindParams.usesIncreasedHeadsUpHeight, inflationCallback, this.mRemoteInputManager.getRemoteViewsOnClickHandler());
            if (this.mInflateSynchronously) {
                asyncInflationTask.onPostExecute(asyncInflationTask.doInBackground(new Void[0]));
            } else {
                asyncInflationTask.executeOnExecutor(this.mBgExecutor, new Void[0]);
            }
        }
    }

    @VisibleForTesting
    public InflationProgress inflateNotificationViews(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, NotificationRowContentBinder.BindParams bindParams, boolean z, int i, Notification.Builder builder, Context context) {
        InflationProgress createRemoteViews = NotificationContentInflaterInjector.createRemoteViews(i, builder, bindParams.isLowPriority, bindParams.usesIncreasedHeight, bindParams.usesIncreasedHeadsUpHeight, context, expandableNotificationRow.getContext());
        inflateSmartReplyViews(createRemoteViews, i, notificationEntry, expandableNotificationRow.getContext(), context, expandableNotificationRow.getHeadsUpManager(), this.mSmartReplyConstants.get(), this.mSmartReplyController.get(), expandableNotificationRow.getExistingSmartRepliesAndActions());
        apply(this.mBgExecutor, z, createRemoteViews, i, this.mRemoteViewCache, notificationEntry, expandableNotificationRow, this.mRemoteInputManager.getRemoteViewsOnClickHandler(), null);
        return createRemoteViews;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder
    public void cancelBind(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        notificationEntry.abortTask();
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder
    public void unbindContent(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i) {
        int i2 = 1;
        while (i != 0) {
            if ((i & i2) != 0) {
                freeNotificationView(notificationEntry, expandableNotificationRow, i2);
            }
            i &= ~i2;
            i2 <<= 1;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeNotificationView$0 */
    public /* synthetic */ void lambda$freeNotificationView$0$NotificationContentInflater(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPrivateLayout().setContractedChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 1);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeNotificationView$1 */
    public /* synthetic */ void lambda$freeNotificationView$1$NotificationContentInflater(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPrivateLayout().setExpandedChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeNotificationView$2 */
    public /* synthetic */ void lambda$freeNotificationView$2$NotificationContentInflater(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPrivateLayout().setHeadsUpChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 4);
        expandableNotificationRow.getPrivateLayout().setHeadsUpInflatedSmartReplies(null);
    }

    private void freeNotificationView(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i == 1) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(0, new Runnable(expandableNotificationRow, notificationEntry) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationContentInflater$ILEJDQPFCuM8cozWhrmF0DOW6HA */
                public final /* synthetic */ ExpandableNotificationRow f$1;
                public final /* synthetic */ NotificationEntry f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationContentInflater.this.lambda$freeNotificationView$0$NotificationContentInflater(this.f$1, this.f$2);
                }
            });
        } else if (i == 2) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(1, new Runnable(expandableNotificationRow, notificationEntry) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationContentInflater$FvWiyXhpHqM8g_4pJOS1w2v23M */
                public final /* synthetic */ ExpandableNotificationRow f$1;
                public final /* synthetic */ NotificationEntry f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationContentInflater.this.lambda$freeNotificationView$1$NotificationContentInflater(this.f$1, this.f$2);
                }
            });
        } else if (i == 4) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(2, new Runnable(expandableNotificationRow, notificationEntry) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationContentInflater$Ti73O4iq7mj5nfG6KVanlUEnpY */
                public final /* synthetic */ ExpandableNotificationRow f$1;
                public final /* synthetic */ NotificationEntry f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationContentInflater.this.lambda$freeNotificationView$2$NotificationContentInflater(this.f$1, this.f$2);
                }
            });
        } else if (i == 8) {
            expandableNotificationRow.getPublicLayout().performWhenContentInactive(0, new Runnable(expandableNotificationRow, notificationEntry) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationContentInflater$rC0T9d2J8ihtZ7IYiND323QEy9I */
                public final /* synthetic */ ExpandableNotificationRow f$1;
                public final /* synthetic */ NotificationEntry f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationContentInflater.this.lambda$freeNotificationView$3$NotificationContentInflater(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeNotificationView$3 */
    public /* synthetic */ void lambda$freeNotificationView$3$NotificationContentInflater(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPublicLayout().setContractedChild(null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 8);
    }

    private void cancelContentViewFrees(ExpandableNotificationRow expandableNotificationRow, int i) {
        if ((i & 1) != 0) {
            expandableNotificationRow.getPrivateLayout().removeContentInactiveRunnable(0);
        }
        if ((i & 2) != 0) {
            expandableNotificationRow.getPrivateLayout().removeContentInactiveRunnable(1);
        }
        if ((i & 4) != 0) {
            expandableNotificationRow.getPrivateLayout().removeContentInactiveRunnable(2);
        }
        if ((i & 8) != 0) {
            expandableNotificationRow.getPublicLayout().removeContentInactiveRunnable(0);
        }
    }

    /* access modifiers changed from: private */
    public static InflationProgress inflateSmartReplyViews(InflationProgress inflationProgress, int i, NotificationEntry notificationEntry, Context context, Context context2, HeadsUpManager headsUpManager, SmartReplyConstants smartReplyConstants, SmartReplyController smartReplyController, InflatedSmartReplies.SmartRepliesAndActions smartRepliesAndActions) {
        if (!((i & 2) == 0 || inflationProgress.newExpandedView == null)) {
            inflationProgress.expandedInflatedSmartReplies = InflatedSmartReplies.inflate(context, context2, notificationEntry, smartReplyConstants, smartReplyController, headsUpManager, smartRepliesAndActions);
        }
        if (!((i & 4) == 0 || inflationProgress.newHeadsUpView == null)) {
            inflationProgress.headsUpInflatedSmartReplies = InflatedSmartReplies.inflate(context, context2, notificationEntry, smartReplyConstants, smartReplyController, headsUpManager, smartRepliesAndActions);
        }
        return inflationProgress;
    }

    static InflationProgress createRemoteViews(int i, Notification.Builder builder, boolean z, boolean z2, boolean z3, Context context) {
        InflationProgress inflationProgress = new InflationProgress();
        if ((i & 1) != 0) {
            inflationProgress.newContentView = createContentView(builder, z, z2);
        }
        if ((i & 2) != 0) {
            inflationProgress.newExpandedView = createExpandedView(builder, z);
        }
        if ((i & 4) != 0) {
            Context context2 = SystemUIApplication.getContext();
            Notification buildUnstyled = builder.buildUnstyled();
            RemoteViews remoteViews = buildUnstyled.headsUpContentView;
            if (remoteViews != null) {
                inflationProgress.newHeadsUpView = remoteViews;
            } else if (NotificationContentInflaterInjector.useOneLine(context2, context, buildUnstyled)) {
                inflationProgress.newHeadsUpView = NotificationContentInflaterInjector.buildOneLineContent(buildUnstyled, true, context2);
            } else {
                inflationProgress.newHeadsUpView = builder.createHeadsUpContentView(z3);
            }
        }
        if ((i & 8) != 0) {
            inflationProgress.newPublicView = builder.makePublicContentView(z);
        }
        inflationProgress.packageContext = context;
        inflationProgress.headsUpStatusBarText = builder.getHeadsUpStatusBarText(false);
        inflationProgress.headsUpStatusBarTextPublic = builder.getHeadsUpStatusBarText(true);
        return inflationProgress;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0174  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.os.CancellationSignal apply(java.util.concurrent.Executor r23, boolean r24, final com.android.systemui.statusbar.notification.row.NotificationContentInflater.InflationProgress r25, int r26, com.android.systemui.statusbar.notification.row.NotifRemoteViewCache r27, com.android.systemui.statusbar.notification.collection.NotificationEntry r28, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r29, android.widget.RemoteViews.OnClickHandler r30, com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback r31) {
        /*
        // Method dump skipped, instructions count: 460
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.NotificationContentInflater.apply(java.util.concurrent.Executor, boolean, com.android.systemui.statusbar.notification.row.NotificationContentInflater$InflationProgress, int, com.android.systemui.statusbar.notification.row.NotifRemoteViewCache, com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, android.widget.RemoteViews$OnClickHandler, com.android.systemui.statusbar.notification.row.NotificationRowContentBinder$InflationCallback):android.os.CancellationSignal");
    }

    @VisibleForTesting
    static void applyRemoteView(Executor executor, boolean z, final InflationProgress inflationProgress, final int i, final int i2, final NotifRemoteViewCache notifRemoteViewCache, final NotificationEntry notificationEntry, final ExpandableNotificationRow expandableNotificationRow, final boolean z2, final RemoteViews.OnClickHandler onClickHandler, final NotificationRowContentBinder.InflationCallback inflationCallback, final NotificationContentView notificationContentView, final View view, final NotificationViewWrapper notificationViewWrapper, final HashMap<Integer, CancellationSignal> hashMap, final ApplyCallback applyCallback) {
        CancellationSignal cancellationSignal;
        final RemoteViews remoteView = applyCallback.getRemoteView();
        if (!z) {
            AnonymousClass5 r17 = new RemoteViews.OnViewAppliedListener() {
                /* class com.android.systemui.statusbar.notification.row.NotificationContentInflater.AnonymousClass5 */

                public void onViewInflated(View view) {
                    if (view instanceof ImageMessageConsumer) {
                        ((ImageMessageConsumer) view).setImageResolver(ExpandableNotificationRow.this.getImageResolver());
                    }
                }

                public void onViewApplied(View view) {
                    if (z2) {
                        view.setIsRootNamespace(true);
                        applyCallback.setResultView(view);
                    } else {
                        NotificationViewWrapper notificationViewWrapper = notificationViewWrapper;
                        if (notificationViewWrapper != null) {
                            notificationViewWrapper.onReinflated();
                        }
                    }
                    hashMap.remove(Integer.valueOf(i2));
                    NotificationContentInflater.finishIfDone(inflationProgress, i, notifRemoteViewCache, hashMap, inflationCallback, notificationEntry, ExpandableNotificationRow.this);
                }

                public void onError(Exception exc) {
                    try {
                        View view = view;
                        if (z2) {
                            view = remoteView.apply(inflationProgress.packageContext, notificationContentView, onClickHandler);
                        } else {
                            remoteView.reapply(inflationProgress.packageContext, view, onClickHandler);
                        }
                        Log.wtf("NotifContentInflater", "Async Inflation failed but normal inflation finished normally.", exc);
                        onViewApplied(view);
                    } catch (Exception unused) {
                        hashMap.remove(Integer.valueOf(i2));
                        NotificationContentInflater.handleInflationError(hashMap, exc, ExpandableNotificationRow.this.getEntry(), inflationCallback);
                    }
                }
            };
            if (z2) {
                cancellationSignal = remoteView.applyAsync(inflationProgress.packageContext, notificationContentView, executor, r17, onClickHandler);
            } else {
                cancellationSignal = remoteView.reapplyAsync(inflationProgress.packageContext, view, executor, r17, onClickHandler);
            }
            hashMap.put(Integer.valueOf(i2), cancellationSignal);
        } else if (z2) {
            try {
                View apply = remoteView.apply(inflationProgress.packageContext, notificationContentView, onClickHandler);
                apply.setIsRootNamespace(true);
                applyCallback.setResultView(apply);
            } catch (Exception e) {
                handleInflationError(hashMap, e, expandableNotificationRow.getEntry(), inflationCallback);
                hashMap.put(Integer.valueOf(i2), new CancellationSignal());
            }
        } else {
            remoteView.reapply(inflationProgress.packageContext, view, onClickHandler);
            notificationViewWrapper.onReinflated();
        }
    }

    /* access modifiers changed from: private */
    public static void handleInflationError(HashMap<Integer, CancellationSignal> hashMap, Exception exc, NotificationEntry notificationEntry, NotificationRowContentBinder.InflationCallback inflationCallback) {
        Assert.isMainThread();
        hashMap.values().forEach($$Lambda$POlPJz26zF5Nt5Z2kVGSqFxN8Co.INSTANCE);
        if (inflationCallback != null) {
            inflationCallback.handleInflationException(notificationEntry, exc);
        }
    }

    /* access modifiers changed from: private */
    public static boolean finishIfDone(InflationProgress inflationProgress, int i, NotifRemoteViewCache notifRemoteViewCache, HashMap<Integer, CancellationSignal> hashMap, NotificationRowContentBinder.InflationCallback inflationCallback, NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        Assert.isMainThread();
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        NotificationContentView publicLayout = expandableNotificationRow.getPublicLayout();
        boolean z = false;
        if (!hashMap.isEmpty()) {
            return false;
        }
        if ((i & 1) != 0) {
            if (inflationProgress.inflatedContentView != null) {
                privateLayout.setContractedChild(inflationProgress.inflatedContentView);
                notifRemoteViewCache.putCachedView(notificationEntry, 1, inflationProgress.newContentView);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 1)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 1, inflationProgress.newContentView);
            }
        }
        if ((i & 2) != 0) {
            if (inflationProgress.inflatedExpandedView != null) {
                privateLayout.setExpandedChild(inflationProgress.inflatedExpandedView);
                notifRemoteViewCache.putCachedView(notificationEntry, 2, inflationProgress.newExpandedView);
            } else if (inflationProgress.newExpandedView == null) {
                privateLayout.setExpandedChild(null);
                notifRemoteViewCache.removeCachedView(notificationEntry, 2);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 2)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 2, inflationProgress.newExpandedView);
            }
            if (inflationProgress.newExpandedView != null) {
                privateLayout.setExpandedInflatedSmartReplies(inflationProgress.expandedInflatedSmartReplies);
            } else {
                privateLayout.setExpandedInflatedSmartReplies(null);
            }
            if (inflationProgress.newExpandedView != null) {
                z = true;
            }
            expandableNotificationRow.setExpandable(z);
        }
        if ((i & 4) != 0) {
            if (inflationProgress.inflatedHeadsUpView != null) {
                privateLayout.setHeadsUpChild(inflationProgress.inflatedHeadsUpView);
                notifRemoteViewCache.putCachedView(notificationEntry, 4, inflationProgress.newHeadsUpView);
            } else if (inflationProgress.newHeadsUpView == null) {
                privateLayout.setHeadsUpChild(null);
                notifRemoteViewCache.removeCachedView(notificationEntry, 4);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 4)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 4, inflationProgress.newHeadsUpView);
            }
            if (inflationProgress.newHeadsUpView != null) {
                privateLayout.setHeadsUpInflatedSmartReplies(inflationProgress.headsUpInflatedSmartReplies);
            } else {
                privateLayout.setHeadsUpInflatedSmartReplies(null);
            }
        }
        if ((i & 8) != 0) {
            if (inflationProgress.inflatedPublicView != null) {
                publicLayout.setContractedChild(inflationProgress.inflatedPublicView);
                notifRemoteViewCache.putCachedView(notificationEntry, 8, inflationProgress.newPublicView);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 8)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 8, inflationProgress.newPublicView);
            }
        }
        notificationEntry.headsUpStatusBarText = inflationProgress.headsUpStatusBarText;
        notificationEntry.headsUpStatusBarTextPublic = inflationProgress.headsUpStatusBarTextPublic;
        if (inflationCallback != null) {
            inflationCallback.onAsyncInflationFinished(notificationEntry);
        }
        return true;
    }

    static RemoteViews createExpandedView(Notification.Builder builder, boolean z) {
        RemoteViews createBigContentView = builder.createBigContentView();
        if (createBigContentView != null) {
            return createBigContentView;
        }
        if (!z) {
            return null;
        }
        RemoteViews createContentView = builder.createContentView();
        Notification.Builder.makeHeaderExpanded(createContentView);
        return createContentView;
    }

    static RemoteViews createContentView(Notification.Builder builder, boolean z, boolean z2) {
        if (z) {
            return builder.makeLowPriorityContentView(false);
        }
        return builder.createContentView(z2);
    }

    @VisibleForTesting
    static boolean canReapplyRemoteView(RemoteViews remoteViews, RemoteViews remoteViews2) {
        if (remoteViews == null && remoteViews2 == null) {
            return true;
        }
        if (remoteViews == null || remoteViews2 == null || remoteViews2.getPackage() == null || remoteViews.getPackage() == null || !remoteViews.getPackage().equals(remoteViews2.getPackage()) || remoteViews.getLayoutId() != remoteViews2.getLayoutId() || remoteViews2.hasFlags(1)) {
            return false;
        }
        return true;
    }

    @VisibleForTesting
    public void setInflateSynchronously(boolean z) {
        this.mInflateSynchronously = z;
    }

    public static class AsyncInflationTask extends AsyncTask<Void, Void, InflationProgress> implements NotificationRowContentBinder.InflationCallback, InflationTask {
        private final Executor mBgExecutor;
        private final NotificationRowContentBinder.InflationCallback mCallback;
        private CancellationSignal mCancellationSignal;
        private final Context mContext;
        private final ConversationNotificationProcessor mConversationProcessor;
        private final NotificationEntry mEntry;
        private Exception mError;
        private final boolean mInflateSynchronously;
        private final boolean mIsLowPriority;
        private final int mReInflateFlags;
        private final NotifRemoteViewCache mRemoteViewCache;
        private RemoteViews.OnClickHandler mRemoteViewClickHandler;
        private ExpandableNotificationRow mRow;
        private final SmartReplyConstants mSmartReplyConstants;
        private final SmartReplyController mSmartReplyController;
        private final boolean mUsesIncreasedHeadsUpHeight;
        private final boolean mUsesIncreasedHeight;

        private AsyncInflationTask(Executor executor, boolean z, int i, NotifRemoteViewCache notifRemoteViewCache, NotificationEntry notificationEntry, SmartReplyConstants smartReplyConstants, SmartReplyController smartReplyController, ConversationNotificationProcessor conversationNotificationProcessor, ExpandableNotificationRow expandableNotificationRow, boolean z2, boolean z3, boolean z4, NotificationRowContentBinder.InflationCallback inflationCallback, RemoteViews.OnClickHandler onClickHandler) {
            this.mEntry = notificationEntry;
            this.mRow = expandableNotificationRow;
            this.mSmartReplyConstants = smartReplyConstants;
            this.mSmartReplyController = smartReplyController;
            this.mBgExecutor = executor;
            this.mInflateSynchronously = z;
            this.mReInflateFlags = i;
            this.mRemoteViewCache = notifRemoteViewCache;
            this.mContext = expandableNotificationRow.getContext();
            this.mIsLowPriority = z2;
            this.mUsesIncreasedHeight = z3;
            this.mUsesIncreasedHeadsUpHeight = z4;
            this.mRemoteViewClickHandler = onClickHandler;
            this.mCallback = inflationCallback;
            this.mConversationProcessor = conversationNotificationProcessor;
            notificationEntry.setInflationTask(this);
        }

        @VisibleForTesting
        public int getReInflateFlags() {
            return this.mReInflateFlags;
        }

        /* access modifiers changed from: protected */
        public InflationProgress doInBackground(Void... voidArr) {
            NotificationContentInflaterInjector.initAppInfo(this.mEntry, this.mContext);
            try {
                ExpandedNotification sbn = this.mEntry.getSbn();
                Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(this.mContext, sbn.getNotification());
                Context packageContext = sbn.getPackageContext(this.mContext);
                RtlEnabledContext rtlEnabledContext = recoverBuilder.usesTemplate() ? new RtlEnabledContext(packageContext) : packageContext;
                Notification notification = sbn.getNotification();
                if (notification.isMediaNotification()) {
                    new MediaNotificationProcessor(this.mContext, rtlEnabledContext).processNotification(notification, recoverBuilder);
                }
                if (this.mEntry.getRanking().isConversation()) {
                    this.mConversationProcessor.processNotification(this.mEntry, recoverBuilder);
                }
                InflationProgress createRemoteViews = NotificationContentInflaterInjector.createRemoteViews(this.mReInflateFlags, recoverBuilder, this.mIsLowPriority, this.mUsesIncreasedHeight, this.mUsesIncreasedHeadsUpHeight, rtlEnabledContext, this.mContext);
                NotificationContentInflater.inflateSmartReplyViews(createRemoteViews, this.mReInflateFlags, this.mEntry, this.mRow.getContext(), rtlEnabledContext, this.mRow.getHeadsUpManager(), this.mSmartReplyConstants, this.mSmartReplyController, this.mRow.getExistingSmartRepliesAndActions());
                return createRemoteViews;
            } catch (Exception e) {
                this.mError = e;
                return null;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(InflationProgress inflationProgress) {
            Exception exc = this.mError;
            if (exc == null) {
                this.mCancellationSignal = NotificationContentInflater.apply(this.mBgExecutor, this.mInflateSynchronously, inflationProgress, this.mReInflateFlags, this.mRemoteViewCache, this.mEntry, this.mRow, this.mRemoteViewClickHandler, this);
            } else {
                handleError(exc);
            }
        }

        private void handleError(Exception exc) {
            this.mEntry.onInflationTaskFinished();
            ExpandedNotification sbn = this.mEntry.getSbn();
            Log.e("StatusBar", "couldn't inflate view for notification " + (sbn.getPackageName() + "/0x" + Integer.toHexString(sbn.getId())), exc);
            NotificationRowContentBinder.InflationCallback inflationCallback = this.mCallback;
            if (inflationCallback != null) {
                inflationCallback.handleInflationException(this.mRow.getEntry(), new InflationException("Couldn't inflate contentViews" + exc));
            }
        }

        @Override // com.android.systemui.statusbar.InflationTask
        public void abort() {
            cancel(true);
            CancellationSignal cancellationSignal = this.mCancellationSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }

        @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
        public void handleInflationException(NotificationEntry notificationEntry, Exception exc) {
            handleError(exc);
        }

        @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
        public void onAsyncInflationFinished(NotificationEntry notificationEntry) {
            this.mEntry.onInflationTaskFinished();
            this.mRow.onNotificationUpdated();
            NotificationRowContentBinder.InflationCallback inflationCallback = this.mCallback;
            if (inflationCallback != null) {
                inflationCallback.onAsyncInflationFinished(this.mEntry);
            }
            this.mRow.getImageResolver().purgeCache();
        }

        /* access modifiers changed from: private */
        public class RtlEnabledContext extends ContextWrapper {
            private RtlEnabledContext(AsyncInflationTask asyncInflationTask, Context context) {
                super(context);
            }

            public ApplicationInfo getApplicationInfo() {
                ApplicationInfo applicationInfo = super.getApplicationInfo();
                applicationInfo.flags |= 4194304;
                return applicationInfo;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class InflationProgress {
        private InflatedSmartReplies expandedInflatedSmartReplies;
        private InflatedSmartReplies headsUpInflatedSmartReplies;
        CharSequence headsUpStatusBarText;
        CharSequence headsUpStatusBarTextPublic;
        private View inflatedContentView;
        private View inflatedExpandedView;
        private View inflatedHeadsUpView;
        private View inflatedPublicView;
        RemoteViews newContentView;
        RemoteViews newExpandedView;
        RemoteViews newHeadsUpView;
        RemoteViews newPublicView;
        @VisibleForTesting
        Context packageContext;

        InflationProgress() {
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static abstract class ApplyCallback {
        public abstract RemoteViews getRemoteView();

        public abstract void setResultView(View view);

        ApplyCallback() {
        }
    }
}
