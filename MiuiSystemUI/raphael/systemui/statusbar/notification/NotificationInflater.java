package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.CancellationSignal;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.AppIconsManager;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.InCallUtils;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.InflationTask;
import com.android.systemui.statusbar.NotificationContentView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.InCallNotificationView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.Assert;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationInflater {
    @VisibleForTesting
    public static final int FLAG_REINFLATE_ALL = -1;
    @VisibleForTesting
    public static final int FLAG_REINFLATE_EXPANDED_VIEW = 2;
    private InflationCallback mCallback;
    private InCallNotificationView.InCallCallback mInCallCallback;
    private boolean mIsChildInGroup;
    private boolean mIsLowPriority;
    private boolean mRedactAmbient;
    private RemoteViews.OnClickHandler mRemoteViewClickHandler;
    private final ExpandableNotificationRow mRow;
    private boolean mUsesIncreasedHeadsUpHeight;
    private boolean mUsesIncreasedHeight;

    public interface InflationCallback {
        void handleInflationException(StatusBarNotification statusBarNotification, Exception exc);

        void onAsyncInflationFinished(NotificationData.Entry entry);
    }

    public NotificationInflater(ExpandableNotificationRow expandableNotificationRow) {
        this.mRow = expandableNotificationRow;
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
    }

    public void setIsChildInGroup(boolean z) {
        if (z != this.mIsChildInGroup) {
            this.mIsChildInGroup = z;
            if (this.mIsLowPriority) {
                inflateNotificationViews(3);
            }
        }
    }

    public void setUsesIncreasedHeight(boolean z) {
        this.mUsesIncreasedHeight = z;
    }

    public void setUsesIncreasedHeadsUpHeight(boolean z) {
        this.mUsesIncreasedHeadsUpHeight = z;
    }

    public void setRemoteViewClickHandler(RemoteViews.OnClickHandler onClickHandler) {
        this.mRemoteViewClickHandler = onClickHandler;
    }

    public void setRedactAmbient(boolean z) {
        if (this.mRedactAmbient != z) {
            this.mRedactAmbient = z;
            if (this.mRow.getEntry() != null) {
                inflateNotificationViews(16);
            }
        }
    }

    public void inflateNotificationViews() {
        inflateNotificationViews(this.mRow.getEntry().notification.getNotification().visibility == 1 ? -25 : -17);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void inflateNotificationViews(int i) {
        if (!this.mRow.isRemoved()) {
            new AsyncInflationTask(this.mRow.getEntry().notification, i, this.mRow, this.mIsLowPriority, this.mIsChildInGroup, this.mUsesIncreasedHeight, this.mUsesIncreasedHeadsUpHeight, this.mRedactAmbient, this.mCallback, this.mRemoteViewClickHandler, this.mInCallCallback).execute(new Void[0]);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public InflationProgress inflateNotificationViews(int i, Notification.Builder builder, Context context) {
        InflationProgress createRemoteViews = createRemoteViews(i, builder, this.mIsLowPriority, this.mIsChildInGroup, this.mUsesIncreasedHeight, this.mUsesIncreasedHeadsUpHeight, this.mRedactAmbient, context, this.mRow);
        apply(createRemoteViews, i, this.mRow, this.mRedactAmbient, this.mRemoteViewClickHandler, (InflationCallback) null, (InCallNotificationView.InCallCallback) null);
        return createRemoteViews;
    }

    /* access modifiers changed from: private */
    public static InflationProgress createRemoteViews(int i, Notification.Builder builder, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, Context context, ExpandableNotificationRow expandableNotificationRow) {
        InflationProgress inflationProgress = new InflationProgress();
        boolean z6 = z && !z2;
        if ((i & 1) != 0) {
            RemoteViews unused = inflationProgress.newContentView = NotificationInflaterHelper.createContentView(builder, z6, z3, expandableNotificationRow);
        }
        if ((i & 2) != 0) {
            RemoteViews unused2 = inflationProgress.newExpandedView = NotificationInflaterHelper.createExpandedView(builder, z6, expandableNotificationRow);
        }
        if ((i & 4) != 0) {
            RemoteViews unused3 = inflationProgress.newHeadsUpView = NotificationInflaterHelper.createHeadsUpView(builder, z4, expandableNotificationRow);
        }
        if ((i & 8) != 0) {
            RemoteViews unused4 = inflationProgress.newPublicView = NotificationInflaterHelper.createPublicContentView(builder, expandableNotificationRow);
        }
        if ((i & 16) != 0) {
            RemoteViews unused5 = inflationProgress.newAmbientView = NotificationInflaterHelper.createAmbientView(builder, z5);
        }
        inflationProgress.packageContext = context;
        return inflationProgress;
    }

    public static CancellationSignal apply(InflationProgress inflationProgress, int i, ExpandableNotificationRow expandableNotificationRow, boolean z, RemoteViews.OnClickHandler onClickHandler, InflationCallback inflationCallback, InCallNotificationView.InCallCallback inCallCallback) {
        NotificationContentView notificationContentView;
        NotificationContentView notificationContentView2;
        HashMap hashMap;
        NotificationData.Entry entry;
        NotificationData.Entry entry2;
        boolean z2;
        final InflationProgress inflationProgress2;
        NotificationContentView notificationContentView3;
        NotificationData.Entry entry3;
        boolean z3;
        final InflationProgress inflationProgress3 = inflationProgress;
        NotificationData.Entry entry4 = expandableNotificationRow.getEntry();
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        NotificationContentView publicLayout = expandableNotificationRow.getPublicLayout();
        HashMap hashMap2 = new HashMap();
        if ((i & 1) != 0) {
            boolean z4 = !compareRemoteViews(inflationProgress.newContentView, entry4.cachedContentView);
            AnonymousClass1 r8 = new ApplyCallback() {
                public void setResultView(View view) {
                    View unused = InflationProgress.this.inflatedContentView = view;
                }

                public RemoteViews getRemoteView() {
                    return InflationProgress.this.newContentView;
                }
            };
            hashMap = hashMap2;
            notificationContentView2 = publicLayout;
            notificationContentView = privateLayout;
            entry = entry4;
            NotificationInflaterHelper.applyRemoteView(inflationProgress, i, 1, expandableNotificationRow, z, z4, onClickHandler, inflationCallback, entry4, privateLayout, privateLayout.getContractedChild(), privateLayout.getVisibleWrapper(0), hashMap, r8, inCallCallback);
        } else {
            hashMap = hashMap2;
            notificationContentView2 = publicLayout;
            notificationContentView = privateLayout;
            entry = entry4;
        }
        if ((i & 2) == 0 || inflationProgress.newExpandedView == null) {
            z2 = true;
            entry2 = entry;
            inflationProgress2 = inflationProgress;
        } else {
            NotificationData.Entry entry5 = entry;
            inflationProgress2 = inflationProgress;
            NotificationContentView notificationContentView4 = notificationContentView;
            entry2 = entry5;
            z2 = true;
            NotificationInflaterHelper.applyRemoteView(inflationProgress, i, 2, expandableNotificationRow, z, !compareRemoteViews(inflationProgress.newExpandedView, entry.cachedBigContentView), onClickHandler, inflationCallback, entry5, notificationContentView4, notificationContentView.getExpandedChild(), notificationContentView4.getVisibleWrapper(1), hashMap, new ApplyCallback() {
                public void setResultView(View view) {
                    View unused = InflationProgress.this.inflatedExpandedView = view;
                }

                public RemoteViews getRemoteView() {
                    return InflationProgress.this.newExpandedView;
                }
            }, inCallCallback);
        }
        if ((i & 4) == 0 || inflationProgress.newHeadsUpView == null) {
            notificationContentView3 = notificationContentView;
            entry3 = entry2;
        } else {
            NotificationData.Entry entry6 = entry2;
            boolean z5 = (!compareRemoteViews(inflationProgress.newHeadsUpView, entry6.cachedHeadsUpContentView) || entry6.isGameModeWhenHeadsUp != StatusBar.sGameMode) ? z2 : false;
            AnonymousClass3 r13 = new ApplyCallback() {
                public void setResultView(View view) {
                    View unused = InflationProgress.this.inflatedHeadsUpView = view;
                }

                public RemoteViews getRemoteView() {
                    return InflationProgress.this.newHeadsUpView;
                }
            };
            NotificationContentView notificationContentView5 = notificationContentView;
            notificationContentView3 = notificationContentView5;
            entry3 = entry6;
            NotificationInflaterHelper.applyRemoteView(inflationProgress, i, 4, expandableNotificationRow, z, z5, onClickHandler, inflationCallback, entry6, notificationContentView5, notificationContentView.getHeadsUpChild(), notificationContentView5.getVisibleWrapper(2), hashMap, r13, inCallCallback);
        }
        if ((i & 8) != 0) {
            boolean z6 = !compareRemoteViews(inflationProgress.newPublicView, entry3.cachedPublicContentView);
            NotificationData.Entry entry7 = entry3;
            final InflationProgress inflationProgress4 = inflationProgress;
            AnonymousClass4 r132 = new ApplyCallback() {
                public void setResultView(View view) {
                    View unused = InflationProgress.this.inflatedPublicView = view;
                }

                public RemoteViews getRemoteView() {
                    return InflationProgress.this.newPublicView;
                }
            };
            NotificationContentView notificationContentView6 = notificationContentView2;
            z3 = false;
            notificationContentView2 = notificationContentView6;
            entry3 = entry7;
            NotificationInflaterHelper.applyRemoteView(inflationProgress, i, 8, expandableNotificationRow, z, z6, onClickHandler, inflationCallback, entry7, notificationContentView6, notificationContentView2.getContractedChild(), notificationContentView6.getVisibleWrapper(0), hashMap, r132, inCallCallback);
        } else {
            z3 = false;
        }
        if ((i & 16) != 0) {
            NotificationContentView notificationContentView7 = z ? notificationContentView2 : notificationContentView3;
            boolean z7 = (!canReapplyAmbient(expandableNotificationRow, z) || !compareRemoteViews(inflationProgress.newAmbientView, entry3.cachedAmbientContentView)) ? z2 : z3;
            NotificationData.Entry entry8 = entry3;
            final InflationProgress inflationProgress5 = inflationProgress;
            NotificationInflaterHelper.applyRemoteView(inflationProgress, i, 16, expandableNotificationRow, z, z7, onClickHandler, inflationCallback, entry8, notificationContentView7, notificationContentView7.getAmbientChild(), notificationContentView7.getVisibleWrapper(4), hashMap, new ApplyCallback() {
                public void setResultView(View view) {
                    View unused = InflationProgress.this.inflatedAmbientView = view;
                }

                public RemoteViews getRemoteView() {
                    return InflationProgress.this.newAmbientView;
                }
            }, inCallCallback);
        } else {
            InflationProgress inflationProgress6 = inflationProgress;
        }
        final HashMap hashMap3 = hashMap;
        finishIfDone(inflationProgress, i, hashMap3, inflationCallback, expandableNotificationRow, z, inCallCallback);
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            public void onCancel() {
                for (CancellationSignal cancel : hashMap3.values()) {
                    cancel.cancel();
                }
            }
        });
        return cancellationSignal;
    }

    public static boolean finishIfDone(InflationProgress inflationProgress, int i, HashMap<Integer, CancellationSignal> hashMap, InflationCallback inflationCallback, ExpandableNotificationRow expandableNotificationRow, boolean z, InCallNotificationView.InCallCallback inCallCallback) {
        Assert.isMainThread();
        NotificationData.Entry entry = expandableNotificationRow.getEntry();
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        NotificationContentView publicLayout = expandableNotificationRow.getPublicLayout();
        boolean z2 = false;
        if (!hashMap.isEmpty()) {
            return false;
        }
        if ((i & 1) != 0) {
            if (inflationProgress.inflatedContentView != null) {
                privateLayout.setContractedChild(inflationProgress.inflatedContentView);
            }
            entry.cachedContentView = inflationProgress.newContentView;
        }
        if ((i & 2) != 0) {
            if (inflationProgress.inflatedExpandedView != null) {
                privateLayout.setExpandedChild(inflationProgress.inflatedExpandedView);
            } else if (inflationProgress.newExpandedView == null) {
                privateLayout.setExpandedChild((View) null);
            }
            entry.cachedBigContentView = inflationProgress.newExpandedView;
            expandableNotificationRow.setExpandable(inflationProgress.newExpandedView != null);
        }
        if ((i & 4) != 0) {
            if (inflationProgress.inflatedHeadsUpView != null) {
                privateLayout.setHeadsUpChild(inflationProgress.inflatedHeadsUpView);
                entry.isGameModeWhenHeadsUp = StatusBar.sGameMode;
            } else if (inflationProgress.newHeadsUpView == null) {
                privateLayout.setHeadsUpChild((View) null);
            }
            entry.cachedHeadsUpContentView = inflationProgress.newHeadsUpView;
        }
        if ((i & 8) != 0) {
            if (inflationProgress.inflatedPublicView != null) {
                publicLayout.setContractedChild(inflationProgress.inflatedPublicView);
            }
            entry.cachedPublicContentView = inflationProgress.newPublicView;
        }
        if ((i & 16) != 0) {
            if (inflationProgress.inflatedAmbientView != null) {
                NotificationContentView notificationContentView = z ? publicLayout : privateLayout;
                if (!z) {
                    privateLayout = publicLayout;
                }
                notificationContentView.setAmbientChild(inflationProgress.inflatedAmbientView);
                privateLayout.setAmbientChild((View) null);
            }
            entry.cachedAmbientContentView = inflationProgress.newAmbientView;
        }
        boolean z3 = StatusBar.sGameMode;
        if (expandableNotificationRow.getResources().getConfiguration().orientation == 2) {
            z2 = true;
        }
        optimizeHeadsUpViewIfNeed(inflationProgress, expandableNotificationRow, z3, z2, inCallCallback);
        if (inflationCallback != null) {
            inflationCallback.onAsyncInflationFinished(expandableNotificationRow.getEntry());
        }
        return true;
    }

    private static void optimizeHeadsUpViewIfNeed(InflationProgress inflationProgress, ExpandableNotificationRow expandableNotificationRow, boolean z, boolean z2, InCallNotificationView.InCallCallback inCallCallback) {
        NotificationData.Entry entry = expandableNotificationRow.getEntry();
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        Context context = privateLayout.getContext();
        if (!InCallUtils.isGlobalInCallNotification(context, entry.notification)) {
            boolean z3 = privateLayout.getHeadsUpChild() instanceof OptimizedHeadsUpNotificationView;
            boolean isInCallNotification = InCallUtils.isInCallNotification(expandableNotificationRow.getEntry().notification);
            int i = 0;
            if (z || z2 || z3 || isInCallNotification) {
                if (inflationProgress.inflatedHeadsUpView != null) {
                    i = 2;
                } else if (inflationProgress.inflatedExpandedView != null) {
                    i = 1;
                }
                View viewForVisibleType = privateLayout.getViewForVisibleType(i);
                privateLayout.getVisibleWrapper(i).onContentUpdated(expandableNotificationRow);
                if (isInCallNotification) {
                    View unused = inflationProgress.inflatedHeadsUpView = inflateInCallHeadsUpNotification(context, viewForVisibleType, entry, inCallCallback);
                } else {
                    View unused2 = inflationProgress.inflatedHeadsUpView = inflateOptimizedHeadsUpNotification(context, viewForVisibleType, entry, z);
                }
                if (inflationProgress.inflatedHeadsUpView != null) {
                    privateLayout.setHeadsUpChild(inflationProgress.inflatedHeadsUpView);
                } else {
                    Log.w("NotificationInflater", "optimizeHeadsUpViewIfNeed() can not inflate optimized heads up child");
                }
            }
        }
    }

    private static InCallNotificationView inflateInCallHeadsUpNotification(Context context, View view, NotificationData.Entry entry, InCallNotificationView.InCallCallback inCallCallback) {
        InCallNotificationView inCallNotificationView = (InCallNotificationView) LayoutInflater.from(context).inflate(R.layout.in_call_heads_up_notification, (ViewGroup) null);
        inCallNotificationView.updateInfo(view, entry.notification.getNotification().extras);
        inCallNotificationView.setInCallCallback(inCallCallback);
        return inCallNotificationView;
    }

    private static OptimizedHeadsUpNotificationView inflateOptimizedHeadsUpNotification(Context context, View view, NotificationData.Entry entry, boolean z) {
        if (view == null) {
            Log.d("NotificationInflater", "inflateOptimizedHeadsUpNotification() oldHeadsUpView is null");
            return null;
        }
        ImageView imageView = (ImageView) view.findViewById(16908294);
        TextView textView = (TextView) view.findViewById(16908310);
        TextView textView2 = (TextView) view.findViewById(16909444);
        CharSequence text = textView != null ? textView.getText() : null;
        CharSequence text2 = textView2 != null ? textView2.getText() : null;
        if (imageView == null || (TextUtils.isEmpty(text) && TextUtils.isEmpty(text2))) {
            Log.d("NotificationInflater", "inflateOptimizedHeadsUpNotification() invalid content");
            return null;
        }
        OptimizedHeadsUpNotificationView optimizedHeadsUpNotificationView = (OptimizedHeadsUpNotificationView) ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.optimized_heads_up_notification, (ViewGroup) null);
        optimizedHeadsUpNotificationView.wrapIconView(imageView);
        optimizedHeadsUpNotificationView.wrapTitleView(textView, z);
        optimizedHeadsUpNotificationView.wrapTextView(textView2, z);
        optimizedHeadsUpNotificationView.wrapMiniWindowBar(entry.row, z);
        View findViewById = view.findViewById(R.id.content);
        if (findViewById != null && findViewById.hasOnClickListeners()) {
            optimizedHeadsUpNotificationView.setOnClickListener(new View.OnClickListener(findViewById) {
                private final /* synthetic */ View f$0;

                {
                    this.f$0 = r1;
                }

                public final void onClick(View view) {
                    this.f$0.callOnClick();
                }
            });
        }
        if (MiuiNotificationCompat.isShowMiuiAction(entry.notification.getNotification()) && entry.getPrivateContentView() != null) {
            TextView textView3 = (TextView) entry.getPrivateContentView().findViewById(16909124);
            TextView actionView = optimizedHeadsUpNotificationView.getActionView();
            if (!(textView3 == null || actionView == null)) {
                actionView.setVisibility(0);
                actionView.setText(entry.notification.getMiuiActionTitle());
                if (z) {
                    actionView.setTextColor(context.getColor(R.color.optimized_game_heads_up_notification_action_text));
                } else {
                    actionView.setTextColor(context.getColor(R.color.optimized_heads_up_notification_action_text));
                }
                actionView.setOnClickListener(new View.OnClickListener(textView3) {
                    private final /* synthetic */ TextView f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onClick(View view) {
                        this.f$0.callOnClick();
                    }
                });
            }
        }
        optimizedHeadsUpNotificationView.setRow(entry.row);
        return optimizedHeadsUpNotificationView;
    }

    private static boolean compareRemoteViews(RemoteViews remoteViews, RemoteViews remoteViews2) {
        return (remoteViews == null && remoteViews2 == null) || !(remoteViews == null || remoteViews2 == null || remoteViews2.getPackage() == null || remoteViews.getPackage() == null || !remoteViews.getPackage().equals(remoteViews2.getPackage()) || remoteViews.getLayoutId() != remoteViews2.getLayoutId());
    }

    public void setInflationCallback(InflationCallback inflationCallback) {
        this.mCallback = inflationCallback;
    }

    public void setInCallCallback(InCallNotificationView.InCallCallback inCallCallback) {
        this.mInCallCallback = inCallCallback;
    }

    public void onDensityOrFontScaleChanged() {
        NotificationData.Entry entry = this.mRow.getEntry();
        entry.cachedAmbientContentView = null;
        entry.cachedBigContentView = null;
        entry.cachedContentView = null;
        entry.cachedHeadsUpContentView = null;
        entry.cachedPublicContentView = null;
        inflateNotificationViews();
    }

    private static boolean canReapplyAmbient(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        NotificationContentView notificationContentView;
        if (z) {
            notificationContentView = expandableNotificationRow.getPublicLayout();
        } else {
            notificationContentView = expandableNotificationRow.getPrivateLayout();
        }
        return notificationContentView.getAmbientChild() != null;
    }

    public static class AsyncInflationTask extends AsyncTask<Void, Void, InflationProgress> implements InflationCallback, InflationTask {
        private final InflationCallback mCallback;
        private CancellationSignal mCancellationSignal;
        private final Context mContext;
        private Exception mError;
        private InCallNotificationView.InCallCallback mInCallCallback;
        private final boolean mIsChildInGroup;
        private final boolean mIsLowPriority;
        private int mReInflateFlags;
        private final boolean mRedactAmbient;
        private RemoteViews.OnClickHandler mRemoteViewClickHandler;
        private ExpandableNotificationRow mRow;
        private final ExpandedNotification mSbn;
        private final boolean mUsesIncreasedHeadsUpHeight;
        private final boolean mUsesIncreasedHeight;

        private AsyncInflationTask(ExpandedNotification expandedNotification, int i, ExpandableNotificationRow expandableNotificationRow, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, InflationCallback inflationCallback, RemoteViews.OnClickHandler onClickHandler, InCallNotificationView.InCallCallback inCallCallback) {
            this.mRow = expandableNotificationRow;
            this.mSbn = expandedNotification;
            this.mReInflateFlags = i;
            this.mContext = this.mRow.getContext();
            this.mIsLowPriority = z;
            this.mIsChildInGroup = z2;
            this.mUsesIncreasedHeight = z3;
            this.mUsesIncreasedHeadsUpHeight = z4;
            this.mRedactAmbient = z5;
            this.mRemoteViewClickHandler = onClickHandler;
            this.mCallback = inflationCallback;
            this.mInCallCallback = inCallCallback;
            expandableNotificationRow.getEntry().setInflationTask(this);
        }

        @VisibleForTesting
        public int getReInflateFlags() {
            return this.mReInflateFlags;
        }

        /* access modifiers changed from: protected */
        public InflationProgress doInBackground(Void... voidArr) {
            initAppInfo();
            try {
                Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(this.mContext, this.mSbn.getNotification());
                Context packageContext = NotificationUtil.getPackageContext(this.mContext, this.mSbn);
                Notification notification = this.mSbn.getNotification();
                if (notification.isMediaNotification()) {
                    new MediaNotificationProcessor(this.mContext, packageContext).processNotification(notification, recoverBuilder);
                }
                return NotificationInflater.createRemoteViews(this.mReInflateFlags, recoverBuilder, this.mIsLowPriority, this.mIsChildInGroup, this.mUsesIncreasedHeight, this.mUsesIncreasedHeadsUpHeight, this.mRedactAmbient, packageContext, this.mRow);
            } catch (Exception e) {
                this.mError = e;
                return null;
            }
        }

        private void initAppInfo() {
            int identifier = this.mSbn.getUser().getIdentifier();
            PackageManager packageManagerForUser = Util.getPackageManagerForUser(this.mContext, identifier);
            try {
                ApplicationInfo applicationInfo = packageManagerForUser.getApplicationInfo(this.mSbn.getPackageName(), 795136);
                if (applicationInfo != null) {
                    this.mSbn.setAppUid(identifier == 999 ? packageManagerForUser.getPackageUidAsUser(this.mSbn.getPackageName(), identifier) : applicationInfo.uid);
                    this.mSbn.setTargetSdk(applicationInfo.targetSdkVersion);
                    this.mSbn.setAppName(String.valueOf(packageManagerForUser.getApplicationLabel(applicationInfo)));
                    this.mSbn.setAppIcon(((AppIconsManager) Dependency.get(AppIconsManager.class)).getAppIcon(this.mContext, applicationInfo, packageManagerForUser, identifier));
                    this.mSbn.setRowIcon(NotificationUtil.getRowIcon(this.mContext, this.mSbn));
                }
            } catch (PackageManager.NameNotFoundException unused) {
                this.mSbn.setAppIcon(packageManagerForUser.getDefaultActivityIcon());
                ExpandedNotification expandedNotification = this.mSbn;
                expandedNotification.setRowIcon(expandedNotification.getAppIcon());
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(InflationProgress inflationProgress) {
            Exception exc = this.mError;
            if (exc == null) {
                this.mCancellationSignal = NotificationInflater.apply(inflationProgress, this.mReInflateFlags, this.mRow, this.mRedactAmbient, this.mRemoteViewClickHandler, this, this.mInCallCallback);
                return;
            }
            handleError(exc);
        }

        private void handleError(Exception exc) {
            this.mRow.getEntry().onInflationTaskFinished();
            ExpandedNotification statusBarNotification = this.mRow.getStatusBarNotification();
            Log.e("StatusBar", "couldn't inflate view for notification " + (statusBarNotification.getPackageName() + "/0x" + Integer.toHexString(statusBarNotification.getId())), exc);
            this.mCallback.handleInflationException(statusBarNotification, new InflationException("Couldn't inflate contentViews" + exc));
        }

        public void abort() {
            cancel(true);
            CancellationSignal cancellationSignal = this.mCancellationSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
        }

        public void supersedeTask(InflationTask inflationTask) {
            if (inflationTask instanceof AsyncInflationTask) {
                this.mReInflateFlags = ((AsyncInflationTask) inflationTask).mReInflateFlags | this.mReInflateFlags;
            }
        }

        public void handleInflationException(StatusBarNotification statusBarNotification, Exception exc) {
            handleError(exc);
        }

        public void onAsyncInflationFinished(NotificationData.Entry entry) {
            this.mRow.getEntry().onInflationTaskFinished();
            this.mRow.onNotificationUpdated(this.mReInflateFlags);
            this.mCallback.onAsyncInflationFinished(this.mRow.getEntry());
        }
    }

    @VisibleForTesting
    static class InflationProgress {
        /* access modifiers changed from: private */
        public View inflatedAmbientView;
        /* access modifiers changed from: private */
        public View inflatedContentView;
        /* access modifiers changed from: private */
        public View inflatedExpandedView;
        /* access modifiers changed from: private */
        public View inflatedHeadsUpView;
        /* access modifiers changed from: private */
        public View inflatedPublicView;
        /* access modifiers changed from: private */
        public RemoteViews newAmbientView;
        /* access modifiers changed from: private */
        public RemoteViews newContentView;
        /* access modifiers changed from: private */
        public RemoteViews newExpandedView;
        /* access modifiers changed from: private */
        public RemoteViews newHeadsUpView;
        /* access modifiers changed from: private */
        public RemoteViews newPublicView;
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

    public static class InflationExecutor implements Executor {
        private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int MAXIMUM_POOL_SIZE = ((CPU_COUNT * 2) + 1);
        private static final ThreadFactory sThreadFactory = new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "InflaterThread #" + this.mCount.getAndIncrement());
            }
        };
        private final ThreadPoolExecutor mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), sThreadFactory);

        public InflationExecutor() {
            this.mExecutor.allowCoreThreadTimeOut(true);
        }

        public void execute(Runnable runnable) {
            this.mExecutor.execute(runnable);
        }
    }
}
