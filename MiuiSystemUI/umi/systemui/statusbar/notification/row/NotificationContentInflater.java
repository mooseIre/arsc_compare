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

    public void bindContent(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i, NotificationRowContentBinder.BindParams bindParams, boolean z, NotificationRowContentBinder.InflationCallback inflationCallback) {
        NotificationRowContentBinder.BindParams bindParams2 = bindParams;
        if (!expandableNotificationRow.isRemoved()) {
            expandableNotificationRow.getImageResolver().preloadImages(notificationEntry.getSbn().getNotification());
            if (z) {
                this.mRemoteViewCache.clearCache(notificationEntry);
            } else {
                NotificationEntry notificationEntry2 = notificationEntry;
            }
            cancelContentViewFrees(expandableNotificationRow, i);
            int i2 = i;
            NotificationEntry notificationEntry3 = notificationEntry;
            ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
            AsyncInflationTask asyncInflationTask = r3;
            AsyncInflationTask asyncInflationTask2 = new AsyncInflationTask(this.mBgExecutor, this.mInflateSynchronously, i2, this.mRemoteViewCache, notificationEntry3, this.mSmartReplyConstants.get(), this.mSmartReplyController.get(), this.mConversationProcessor, expandableNotificationRow2, bindParams2.isLowPriority, bindParams2.usesIncreasedHeight, bindParams2.usesIncreasedHeadsUpHeight, inflationCallback, this.mRemoteInputManager.getRemoteViewsOnClickHandler());
            if (this.mInflateSynchronously) {
                asyncInflationTask.onPostExecute(asyncInflationTask.doInBackground(new Void[0]));
            } else {
                asyncInflationTask.executeOnExecutor(this.mBgExecutor, new Void[0]);
            }
        }
    }

    @VisibleForTesting
    public InflationProgress inflateNotificationViews(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, NotificationRowContentBinder.BindParams bindParams, boolean z, int i, Notification.Builder builder, Context context) {
        NotificationRowContentBinder.BindParams bindParams2 = bindParams;
        InflationProgress createRemoteViews = NotificationContentInflaterInjector.createRemoteViews(i, builder, bindParams2.isLowPriority, bindParams2.usesIncreasedHeight, bindParams2.usesIncreasedHeadsUpHeight, context, expandableNotificationRow.getContext());
        inflateSmartReplyViews(createRemoteViews, i, notificationEntry, expandableNotificationRow.getContext(), context, expandableNotificationRow.getHeadsUpManager(), this.mSmartReplyConstants.get(), this.mSmartReplyController.get(), expandableNotificationRow.getExistingSmartRepliesAndActions());
        apply(this.mBgExecutor, z, createRemoteViews, i, this.mRemoteViewCache, notificationEntry, expandableNotificationRow, this.mRemoteInputManager.getRemoteViewsOnClickHandler(), (NotificationRowContentBinder.InflationCallback) null);
        return createRemoteViews;
    }

    public void cancelBind(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        notificationEntry.abortTask();
    }

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
        expandableNotificationRow.getPrivateLayout().setContractedChild((View) null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 1);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeNotificationView$1 */
    public /* synthetic */ void lambda$freeNotificationView$1$NotificationContentInflater(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPrivateLayout().setExpandedChild((View) null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeNotificationView$2 */
    public /* synthetic */ void lambda$freeNotificationView$2$NotificationContentInflater(ExpandableNotificationRow expandableNotificationRow, NotificationEntry notificationEntry) {
        expandableNotificationRow.getPrivateLayout().setHeadsUpChild((View) null);
        this.mRemoteViewCache.removeCachedView(notificationEntry, 4);
        expandableNotificationRow.getPrivateLayout().setHeadsUpInflatedSmartReplies((InflatedSmartReplies) null);
    }

    private void freeNotificationView(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i == 1) {
            expandableNotificationRow.getPrivateLayout().performWhenContentInactive(0, new Runnable(expandableNotificationRow, notificationEntry) {
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
        expandableNotificationRow.getPublicLayout().setContractedChild((View) null);
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
            InflatedSmartReplies unused = inflationProgress.expandedInflatedSmartReplies = InflatedSmartReplies.inflate(context, context2, notificationEntry, smartReplyConstants, smartReplyController, headsUpManager, smartRepliesAndActions);
        }
        if (!((i & 4) == 0 || inflationProgress.newHeadsUpView == null)) {
            InflatedSmartReplies unused2 = inflationProgress.headsUpInflatedSmartReplies = InflatedSmartReplies.inflate(context, context2, notificationEntry, smartReplyConstants, smartReplyController, headsUpManager, smartRepliesAndActions);
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
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x007e, code lost:
        r14 = r25;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00e3, code lost:
        r15 = r25;
     */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x010f  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0174  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static android.os.CancellationSignal apply(java.util.concurrent.Executor r23, boolean r24, com.android.systemui.statusbar.notification.row.NotificationContentInflater.InflationProgress r25, int r26, com.android.systemui.statusbar.notification.row.NotifRemoteViewCache r27, com.android.systemui.statusbar.notification.collection.NotificationEntry r28, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r29, android.widget.RemoteViews.OnClickHandler r30, com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback r31) {
        /*
            r15 = r25
            r14 = r27
            r13 = r28
            r12 = r29
            com.android.systemui.statusbar.notification.row.NotificationContentView r11 = r29.getPrivateLayout()
            com.android.systemui.statusbar.notification.row.NotificationContentView r10 = r29.getPublicLayout()
            java.util.HashMap r9 = new java.util.HashMap
            r9.<init>()
            r0 = r26 & 1
            r8 = 0
            r7 = 1
            if (r0 == 0) goto L_0x0073
            android.widget.RemoteViews r0 = r15.newContentView
            android.widget.RemoteViews r1 = r14.getCachedView(r13, r7)
            boolean r0 = canReapplyRemoteView(r0, r1)
            r0 = r0 ^ r7
            if (r0 != 0) goto L_0x0031
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r0 = r28.getModalRow()
            if (r12 != r0) goto L_0x002f
            goto L_0x0031
        L_0x002f:
            r0 = r8
            goto L_0x0032
        L_0x0031:
            r0 = r7
        L_0x0032:
            android.view.View r1 = r11.getContractedChild()
            if (r1 != 0) goto L_0x003b
            r16 = r7
            goto L_0x003d
        L_0x003b:
            r16 = r0
        L_0x003d:
            com.android.systemui.statusbar.notification.row.NotificationContentInflater$1 r6 = new com.android.systemui.statusbar.notification.row.NotificationContentInflater$1
            r6.<init>()
            android.view.View r17 = r11.getContractedChild()
            com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper r18 = r11.getVisibleWrapper(r8)
            r4 = 1
            r0 = r23
            r1 = r24
            r2 = r25
            r3 = r26
            r5 = r27
            r19 = r6
            r6 = r28
            r7 = r29
            r8 = r16
            r16 = r9
            r9 = r30
            r21 = r10
            r10 = r31
            r22 = r11
            r12 = r17
            r13 = r18
            r14 = r16
            r15 = r19
            applyRemoteView(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
            goto L_0x0079
        L_0x0073:
            r16 = r9
            r21 = r10
            r22 = r11
        L_0x0079:
            r0 = r26 & 2
            r15 = 2
            if (r0 == 0) goto L_0x00dd
            r14 = r25
            android.widget.RemoteViews r0 = r14.newExpandedView
            if (r0 == 0) goto L_0x00dd
            r13 = r27
            r12 = r28
            android.widget.RemoteViews r1 = r13.getCachedView(r12, r15)
            boolean r0 = canReapplyRemoteView(r0, r1)
            r11 = 1
            r0 = r0 ^ r11
            if (r0 != 0) goto L_0x009f
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r0 = r28.getModalRow()
            r10 = r29
            if (r10 != r0) goto L_0x009d
            goto L_0x00a1
        L_0x009d:
            r8 = 0
            goto L_0x00a2
        L_0x009f:
            r10 = r29
        L_0x00a1:
            r8 = r11
        L_0x00a2:
            android.view.View r0 = r22.getExpandedChild()
            if (r0 != 0) goto L_0x00a9
            r8 = r11
        L_0x00a9:
            com.android.systemui.statusbar.notification.row.NotificationContentInflater$2 r9 = new com.android.systemui.statusbar.notification.row.NotificationContentInflater$2
            r9.<init>()
            android.view.View r17 = r22.getExpandedChild()
            r7 = r22
            com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper r18 = r7.getVisibleWrapper(r11)
            r4 = 2
            r0 = r23
            r1 = r24
            r2 = r25
            r3 = r26
            r5 = r27
            r6 = r28
            r7 = r29
            r19 = r9
            r9 = r30
            r10 = r31
            r20 = r11
            r11 = r22
            r12 = r17
            r13 = r18
            r14 = r16
            r15 = r19
            applyRemoteView(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
            goto L_0x00df
        L_0x00dd:
            r20 = 1
        L_0x00df:
            r0 = r26 & 4
            if (r0 == 0) goto L_0x0145
            r15 = r25
            android.widget.RemoteViews r0 = r15.newHeadsUpView
            if (r0 == 0) goto L_0x0145
            r1 = 4
            r14 = r27
            r13 = r28
            android.widget.RemoteViews r1 = r14.getCachedView(r13, r1)
            boolean r0 = canReapplyRemoteView(r0, r1)
            r0 = r0 ^ 1
            if (r0 != 0) goto L_0x0105
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r0 = r28.getModalRow()
            r12 = r29
            if (r12 != r0) goto L_0x0103
            goto L_0x0107
        L_0x0103:
            r8 = 0
            goto L_0x0109
        L_0x0105:
            r12 = r29
        L_0x0107:
            r8 = r20
        L_0x0109:
            android.view.View r0 = r22.getHeadsUpChild()
            if (r0 != 0) goto L_0x0111
            r8 = r20
        L_0x0111:
            com.android.systemui.statusbar.notification.row.NotificationContentInflater$3 r11 = new com.android.systemui.statusbar.notification.row.NotificationContentInflater$3
            r11.<init>()
            android.view.View r17 = r22.getHeadsUpChild()
            r10 = r22
            r0 = 2
            com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper r18 = r10.getVisibleWrapper(r0)
            r4 = 4
            r0 = r23
            r1 = r24
            r2 = r25
            r3 = r26
            r5 = r27
            r6 = r28
            r7 = r29
            r9 = r30
            r19 = r10
            r10 = r31
            r22 = r11
            r11 = r19
            r12 = r17
            r13 = r18
            r14 = r16
            r15 = r22
            applyRemoteView(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
        L_0x0145:
            r0 = r26 & 8
            if (r0 == 0) goto L_0x01ab
            r15 = r25
            android.widget.RemoteViews r0 = r15.newPublicView
            r1 = 8
            r14 = r27
            r13 = r28
            android.widget.RemoteViews r1 = r14.getCachedView(r13, r1)
            boolean r0 = canReapplyRemoteView(r0, r1)
            r0 = r0 ^ 1
            if (r0 != 0) goto L_0x016a
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r0 = r28.getModalRow()
            r12 = r29
            if (r12 != r0) goto L_0x0168
            goto L_0x016c
        L_0x0168:
            r8 = 0
            goto L_0x016e
        L_0x016a:
            r12 = r29
        L_0x016c:
            r8 = r20
        L_0x016e:
            android.view.View r0 = r21.getContractedChild()
            if (r0 != 0) goto L_0x0176
            r8 = r20
        L_0x0176:
            com.android.systemui.statusbar.notification.row.NotificationContentInflater$4 r11 = new com.android.systemui.statusbar.notification.row.NotificationContentInflater$4
            r11.<init>()
            android.view.View r17 = r21.getContractedChild()
            r10 = r21
            r0 = 0
            com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper r18 = r10.getVisibleWrapper(r0)
            r4 = 8
            r0 = r23
            r1 = r24
            r2 = r25
            r3 = r26
            r5 = r27
            r6 = r28
            r7 = r29
            r9 = r30
            r19 = r10
            r10 = r31
            r20 = r11
            r11 = r19
            r12 = r17
            r13 = r18
            r14 = r16
            r15 = r20
            applyRemoteView(r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
        L_0x01ab:
            r0 = r25
            r1 = r26
            r2 = r27
            r3 = r16
            r4 = r31
            r5 = r28
            r6 = r29
            finishIfDone(r0, r1, r2, r3, r4, r5, r6)
            android.os.CancellationSignal r0 = new android.os.CancellationSignal
            r0.<init>()
            com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationContentInflater$OEvtjvTsy-AuHJidkBGe8RqtYkc r1 = new com.android.systemui.statusbar.notification.row.-$$Lambda$NotificationContentInflater$OEvtjvTsy-AuHJidkBGe8RqtYkc
            r2 = r16
            r1.<init>(r2)
            r0.setOnCancelListener(r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.NotificationContentInflater.apply(java.util.concurrent.Executor, boolean, com.android.systemui.statusbar.notification.row.NotificationContentInflater$InflationProgress, int, com.android.systemui.statusbar.notification.row.NotifRemoteViewCache, com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, android.widget.RemoteViews$OnClickHandler, com.android.systemui.statusbar.notification.row.NotificationRowContentBinder$InflationCallback):android.os.CancellationSignal");
    }

    @VisibleForTesting
    static void applyRemoteView(Executor executor, boolean z, InflationProgress inflationProgress, int i, int i2, NotifRemoteViewCache notifRemoteViewCache, NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, boolean z2, RemoteViews.OnClickHandler onClickHandler, NotificationRowContentBinder.InflationCallback inflationCallback, NotificationContentView notificationContentView, View view, NotificationViewWrapper notificationViewWrapper, HashMap<Integer, CancellationSignal> hashMap, ApplyCallback applyCallback) {
        CancellationSignal cancellationSignal;
        InflationProgress inflationProgress2 = inflationProgress;
        RemoteViews.OnClickHandler onClickHandler2 = onClickHandler;
        HashMap<Integer, CancellationSignal> hashMap2 = hashMap;
        RemoteViews remoteView = applyCallback.getRemoteView();
        if (!z) {
            NotificationRowContentBinder.InflationCallback inflationCallback2 = inflationCallback;
            NotificationContentView notificationContentView2 = notificationContentView;
            View view2 = view;
            final ApplyCallback applyCallback2 = applyCallback;
            final ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
            final boolean z3 = z2;
            final NotificationViewWrapper notificationViewWrapper2 = notificationViewWrapper;
            final HashMap<Integer, CancellationSignal> hashMap3 = hashMap;
            final int i3 = i2;
            final InflationProgress inflationProgress3 = inflationProgress;
            final int i4 = i;
            final NotifRemoteViewCache notifRemoteViewCache2 = notifRemoteViewCache;
            final NotificationRowContentBinder.InflationCallback inflationCallback3 = inflationCallback;
            final NotificationEntry notificationEntry2 = notificationEntry;
            RemoteViews remoteViews = remoteView;
            final View view3 = view;
            final RemoteViews remoteViews2 = remoteViews;
            final NotificationContentView notificationContentView3 = notificationContentView;
            final RemoteViews.OnClickHandler onClickHandler3 = onClickHandler;
            AnonymousClass5 r1 = new RemoteViews.OnViewAppliedListener() {
                public void onViewInflated(View view) {
                    if (view instanceof ImageMessageConsumer) {
                        ((ImageMessageConsumer) view).setImageResolver(ExpandableNotificationRow.this.getImageResolver());
                    }
                }

                public void onViewApplied(View view) {
                    if (z3) {
                        view.setIsRootNamespace(true);
                        applyCallback2.setResultView(view);
                    } else {
                        NotificationViewWrapper notificationViewWrapper = notificationViewWrapper2;
                        if (notificationViewWrapper != null) {
                            notificationViewWrapper.onReinflated();
                        }
                    }
                    hashMap3.remove(Integer.valueOf(i3));
                    boolean unused = NotificationContentInflater.finishIfDone(inflationProgress3, i4, notifRemoteViewCache2, hashMap3, inflationCallback3, notificationEntry2, ExpandableNotificationRow.this);
                }

                public void onError(Exception exc) {
                    try {
                        View view = view3;
                        if (z3) {
                            view = remoteViews2.apply(inflationProgress3.packageContext, notificationContentView3, onClickHandler3);
                        } else {
                            remoteViews2.reapply(inflationProgress3.packageContext, view3, onClickHandler3);
                        }
                        Log.wtf("NotifContentInflater", "Async Inflation failed but normal inflation finished normally.", exc);
                        onViewApplied(view);
                    } catch (Exception unused) {
                        hashMap3.remove(Integer.valueOf(i3));
                        NotificationContentInflater.handleInflationError(hashMap3, exc, ExpandableNotificationRow.this.getEntry(), inflationCallback3);
                    }
                }
            };
            if (z2) {
                cancellationSignal = remoteViews.applyAsync(inflationProgress2.packageContext, notificationContentView, executor, r1, onClickHandler);
            } else {
                cancellationSignal = remoteViews.reapplyAsync(inflationProgress2.packageContext, view, executor, r1, onClickHandler);
            }
            hashMap.put(Integer.valueOf(i2), cancellationSignal);
        } else if (z2) {
            try {
                View apply = remoteView.apply(inflationProgress2.packageContext, notificationContentView, onClickHandler2);
                apply.setIsRootNamespace(true);
                applyCallback.setResultView(apply);
            } catch (Exception e) {
                handleInflationError(hashMap2, e, expandableNotificationRow.getEntry(), inflationCallback);
                hashMap2.put(Integer.valueOf(i2), new CancellationSignal());
            }
        } else {
            remoteView.reapply(inflationProgress2.packageContext, view, onClickHandler2);
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
                privateLayout.setExpandedChild((View) null);
                notifRemoteViewCache.removeCachedView(notificationEntry, 2);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 2)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 2, inflationProgress.newExpandedView);
            }
            if (inflationProgress.newExpandedView != null) {
                privateLayout.setExpandedInflatedSmartReplies(inflationProgress.expandedInflatedSmartReplies);
            } else {
                privateLayout.setExpandedInflatedSmartReplies((InflatedSmartReplies) null);
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
                privateLayout.setHeadsUpChild((View) null);
                notifRemoteViewCache.removeCachedView(notificationEntry, 4);
            } else if (notifRemoteViewCache.hasCachedView(notificationEntry, 4)) {
                notifRemoteViewCache.putCachedView(notificationEntry, 4, inflationProgress.newHeadsUpView);
            }
            if (inflationProgress.newHeadsUpView != null) {
                privateLayout.setHeadsUpInflatedSmartReplies(inflationProgress.headsUpInflatedSmartReplies);
            } else {
                privateLayout.setHeadsUpInflatedSmartReplies((InflatedSmartReplies) null);
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
                InflationProgress unused = NotificationContentInflater.inflateSmartReplyViews(createRemoteViews, this.mReInflateFlags, this.mEntry, this.mRow.getContext(), rtlEnabledContext, this.mRow.getHeadsUpManager(), this.mSmartReplyConstants, this.mSmartReplyController, this.mRow.getExistingSmartRepliesAndActions());
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
                return;
            }
            handleError(exc);
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

        public void abort() {
            cancel(true);
            CancellationSignal cancellationSignal = this.mCancellationSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }

        public void handleInflationException(NotificationEntry notificationEntry, Exception exc) {
            handleError(exc);
        }

        public void onAsyncInflationFinished(NotificationEntry notificationEntry) {
            this.mEntry.onInflationTaskFinished();
            this.mRow.onNotificationUpdated();
            NotificationRowContentBinder.InflationCallback inflationCallback = this.mCallback;
            if (inflationCallback != null) {
                inflationCallback.onAsyncInflationFinished(this.mEntry);
            }
            this.mRow.getImageResolver().purgeCache();
        }

        private class RtlEnabledContext extends ContextWrapper {
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

    @VisibleForTesting
    static class InflationProgress {
        /* access modifiers changed from: private */
        public InflatedSmartReplies expandedInflatedSmartReplies;
        /* access modifiers changed from: private */
        public InflatedSmartReplies headsUpInflatedSmartReplies;
        CharSequence headsUpStatusBarText;
        CharSequence headsUpStatusBarTextPublic;
        /* access modifiers changed from: private */
        public View inflatedContentView;
        /* access modifiers changed from: private */
        public View inflatedExpandedView;
        /* access modifiers changed from: private */
        public View inflatedHeadsUpView;
        /* access modifiers changed from: private */
        public View inflatedPublicView;
        RemoteViews newContentView;
        RemoteViews newExpandedView;
        RemoteViews newHeadsUpView;
        RemoteViews newPublicView;
        @VisibleForTesting
        Context packageContext;

        InflationProgress() {
        }
    }

    @VisibleForTesting
    static abstract class ApplyCallback {
        public abstract RemoteViews getRemoteView();

        public abstract void setResultView(View view);

        ApplyCallback() {
        }
    }
}
