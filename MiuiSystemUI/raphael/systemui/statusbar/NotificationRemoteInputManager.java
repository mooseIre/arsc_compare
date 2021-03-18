package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.RemoteInputHistoryItem;
import android.content.Context;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.miui.systemui.events.ModalExitMode;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class NotificationRemoteInputManager implements Dumpable {
    public static final boolean ENABLE_REMOTE_INPUT = SystemProperties.getBoolean("debug.enable_remote_input", true);
    public static boolean FORCE_REMOTE_INPUT_HISTORY = SystemProperties.getBoolean("debug.force_remoteinput_history", true);
    protected IStatusBarService mBarService;
    protected Callback mCallback;
    private final NotificationClickNotifier mClickNotifier;
    protected final Context mContext;
    protected final ArraySet<NotificationEntry> mEntriesKeptForRemoteInputActive = new ArraySet<>();
    private final NotificationEntryManager mEntryManager;
    private final KeyguardManager mKeyguardManager;
    protected final ArraySet<String> mKeysKeptForRemoteInputHistory = new ArraySet<>();
    protected final ArrayList<NotificationLifetimeExtender> mLifetimeExtenders = new ArrayList<>();
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private final ActionClickLogger mLogger;
    private final Handler mMainHandler;
    protected NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationLifetimeFinishedCallback;
    private final RemoteViews.OnClickHandler mOnClickHandler = new RemoteViews.OnClickHandler() {
        /* class com.android.systemui.statusbar.NotificationRemoteInputManager.AnonymousClass1 */

        public boolean onClickHandler(View view, PendingIntent pendingIntent, RemoteViews.RemoteResponse remoteResponse) {
            ((StatusBar) NotificationRemoteInputManager.this.mStatusBarLazy.get()).wakeUpIfDozing(SystemClock.uptimeMillis(), view, "NOTIFICATION_CLICK");
            NotificationEntry notificationForParent = getNotificationForParent(view.getParent());
            NotificationRemoteInputManager.this.mLogger.logInitialClick(notificationForParent, pendingIntent);
            if (handleRemoteInput(view, pendingIntent)) {
                NotificationRemoteInputManager.this.mLogger.logRemoteInputWasHandled(notificationForParent);
                return true;
            }
            logActionClick(view, notificationForParent, pendingIntent);
            ((ModalController) Dependency.get(ModalController.class)).animExitModal(ModalExitMode.MANUAL.name());
            try {
                ActivityManager.getService().resumeAppSwitches();
            } catch (RemoteException unused) {
            }
            return NotificationRemoteInputManager.this.mCallback.handleRemoteViewClick(view, pendingIntent, new ClickHandler(remoteResponse, view, notificationForParent, pendingIntent) {
                /* class com.android.systemui.statusbar.$$Lambda$NotificationRemoteInputManager$1$ZtHhq33lnHmB3dDn_Na8muulDlc */
                public final /* synthetic */ RemoteViews.RemoteResponse f$1;
                public final /* synthetic */ View f$2;
                public final /* synthetic */ NotificationEntry f$3;
                public final /* synthetic */ PendingIntent f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.ClickHandler
                public final boolean handleClick() {
                    return NotificationRemoteInputManager.AnonymousClass1.this.lambda$onClickHandler$0$NotificationRemoteInputManager$1(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onClickHandler$0 */
        public /* synthetic */ boolean lambda$onClickHandler$0$NotificationRemoteInputManager$1(RemoteViews.RemoteResponse remoteResponse, View view, NotificationEntry notificationEntry, PendingIntent pendingIntent) {
            Pair launchOptions = remoteResponse.getLaunchOptions(view);
            ((ActivityOptions) launchOptions.second).setLaunchWindowingMode(4);
            NotificationRemoteInputManager.this.mLogger.logStartingIntentWithDefaultHandler(notificationEntry, pendingIntent);
            return RemoteViews.startPendingIntent(view, pendingIntent, launchOptions);
        }

        private void logActionClick(View view, NotificationEntry notificationEntry, PendingIntent pendingIntent) {
            Integer num = (Integer) view.getTag(16909233);
            if (num != null) {
                ViewParent parent = view.getParent();
                if (notificationEntry == null) {
                    Log.w("NotifRemoteInputManager", "Couldn't determine notification for click.");
                    return;
                }
                ExpandedNotification sbn = notificationEntry.getSbn();
                String key = sbn.getKey();
                int indexOfChild = (view.getId() != 16908698 || parent == null || !(parent instanceof ViewGroup)) ? -1 : ((ViewGroup) parent).indexOfChild(view);
                int activeNotificationsCount = NotificationRemoteInputManager.this.mEntryManager.getActiveNotificationsCount();
                int rank = NotificationRemoteInputManager.this.mEntryManager.getActiveNotificationUnfiltered(key).getRanking().getRank();
                Notification.Action[] actionArr = sbn.getNotification().actions;
                if (actionArr == null || num.intValue() >= actionArr.length) {
                    Log.w("NotifRemoteInputManager", "statusBarNotification.getNotification().actions is null or invalid");
                    return;
                }
                Notification.Action action = sbn.getNotification().actions[num.intValue()];
                if (!Objects.equals(action.actionIntent, pendingIntent)) {
                    Log.w("NotifRemoteInputManager", "actionIntent does not match");
                } else {
                    NotificationRemoteInputManager.this.mClickNotifier.onNotificationActionClick(key, indexOfChild, action, NotificationVisibility.obtain(key, rank, activeNotificationsCount, true, NotificationLogger.getNotificationLocation(NotificationRemoteInputManager.this.mEntryManager.getActiveNotificationUnfiltered(key))), false);
                }
            }
        }

        private NotificationEntry getNotificationForParent(ViewParent viewParent) {
            while (viewParent != null) {
                if (viewParent instanceof ExpandableNotificationRow) {
                    return ((ExpandableNotificationRow) viewParent).getEntry();
                }
                viewParent = viewParent.getParent();
            }
            return null;
        }

        private boolean handleRemoteInput(View view, PendingIntent pendingIntent) {
            if (NotificationRemoteInputManager.this.mCallback.shouldHandleRemoteInput(view, pendingIntent)) {
                return true;
            }
            Object tag = view.getTag(16909358);
            RemoteInput[] remoteInputArr = tag instanceof RemoteInput[] ? (RemoteInput[]) tag : null;
            if (remoteInputArr == null) {
                return false;
            }
            RemoteInput remoteInput = null;
            for (RemoteInput remoteInput2 : remoteInputArr) {
                if (remoteInput2.getAllowFreeFormInput()) {
                    remoteInput = remoteInput2;
                }
            }
            if (remoteInput == null) {
                return false;
            }
            return NotificationRemoteInputManager.this.activateRemoteInput(view, remoteInputArr, remoteInput, pendingIntent, null);
        }
    };
    protected RemoteInputController mRemoteInputController;
    private final RemoteInputUriController mRemoteInputUriController;
    private final SmartReplyController mSmartReplyController;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController;
    private final UserManager mUserManager;

    public interface Callback {
        boolean handleRemoteViewClick(View view, PendingIntent pendingIntent, ClickHandler clickHandler);

        void onLockedRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view);

        void onLockedWorkRemoteInput(int i, ExpandableNotificationRow expandableNotificationRow, View view);

        void onMakeExpandedVisibleForRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view);

        boolean shouldHandleRemoteInput(View view, PendingIntent pendingIntent);
    }

    public interface ClickHandler {
        boolean handleClick();
    }

    public NotificationRemoteInputManager(Context context, NotificationLockscreenUserManager notificationLockscreenUserManager, SmartReplyController smartReplyController, NotificationEntryManager notificationEntryManager, Lazy<StatusBar> lazy, StatusBarStateController statusBarStateController, Handler handler, RemoteInputUriController remoteInputUriController, NotificationClickNotifier notificationClickNotifier, ActionClickLogger actionClickLogger) {
        this.mContext = context;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mSmartReplyController = smartReplyController;
        this.mEntryManager = notificationEntryManager;
        this.mStatusBarLazy = lazy;
        this.mMainHandler = handler;
        this.mLogger = actionClickLogger;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        addLifetimeExtenders();
        this.mKeyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class);
        this.mStatusBarStateController = statusBarStateController;
        this.mRemoteInputUriController = remoteInputUriController;
        this.mClickNotifier = notificationClickNotifier;
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.NotificationRemoteInputManager.AnonymousClass2 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                NotificationRemoteInputManager.this.mSmartReplyController.stopSending(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                NotificationRemoteInputManager.this.mSmartReplyController.stopSending(notificationEntry);
                if (z && notificationEntry != null) {
                    NotificationRemoteInputManager.this.onPerformRemoveNotification(notificationEntry, notificationEntry.getKey());
                }
            }
        });
    }

    public void setUpWithCallback(Callback callback, RemoteInputController.Delegate delegate) {
        this.mCallback = callback;
        RemoteInputController remoteInputController = new RemoteInputController(delegate, this.mRemoteInputUriController);
        this.mRemoteInputController = remoteInputController;
        remoteInputController.addCallback(new RemoteInputController.Callback() {
            /* class com.android.systemui.statusbar.NotificationRemoteInputManager.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.RemoteInputController.Callback
            public void onRemoteInputSent(NotificationEntry notificationEntry) {
                if (NotificationRemoteInputManager.FORCE_REMOTE_INPUT_HISTORY && NotificationRemoteInputManager.this.isNotificationKeptForRemoteInputHistory(notificationEntry.getKey())) {
                    NotificationRemoteInputManager.this.mNotificationLifetimeFinishedCallback.onSafeToRemove(notificationEntry.getKey());
                } else if (NotificationRemoteInputManager.this.mEntriesKeptForRemoteInputActive.contains(notificationEntry)) {
                    NotificationRemoteInputManager.this.mMainHandler.postDelayed(new Runnable(notificationEntry) {
                        /* class com.android.systemui.statusbar.$$Lambda$NotificationRemoteInputManager$3$4_sgjm8NgJs8c5OYAKLP29ZAlfg */
                        public final /* synthetic */ NotificationEntry f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            NotificationRemoteInputManager.AnonymousClass3.this.lambda$onRemoteInputSent$0$NotificationRemoteInputManager$3(this.f$1);
                        }
                    }, 200);
                }
                try {
                    NotificationRemoteInputManager.this.mBarService.onNotificationDirectReplied(notificationEntry.getSbn().getKey());
                    if (notificationEntry.editedSuggestionInfo != null) {
                        NotificationRemoteInputManager.this.mBarService.onNotificationSmartReplySent(notificationEntry.getSbn().getKey(), notificationEntry.editedSuggestionInfo.index, notificationEntry.editedSuggestionInfo.originalText, NotificationLogger.getNotificationLocation(notificationEntry).toMetricsEventEnum(), !TextUtils.equals(notificationEntry.remoteInputText, notificationEntry.editedSuggestionInfo.originalText));
                    }
                } catch (RemoteException unused) {
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onRemoteInputSent$0 */
            public /* synthetic */ void lambda$onRemoteInputSent$0$NotificationRemoteInputManager$3(NotificationEntry notificationEntry) {
                if (NotificationRemoteInputManager.this.mEntriesKeptForRemoteInputActive.remove(notificationEntry)) {
                    NotificationRemoteInputManager.this.mNotificationLifetimeFinishedCallback.onSafeToRemove(notificationEntry.getKey());
                }
            }
        });
        this.mSmartReplyController.setCallback(new SmartReplyController.Callback() {
            /* class com.android.systemui.statusbar.$$Lambda$NotificationRemoteInputManager$Nf_J1NPWba8TQAi27YtXiB5drE */

            @Override // com.android.systemui.statusbar.SmartReplyController.Callback
            public final void onSmartReplySent(NotificationEntry notificationEntry, CharSequence charSequence) {
                NotificationRemoteInputManager.this.lambda$setUpWithCallback$0$NotificationRemoteInputManager(notificationEntry, charSequence);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpWithCallback$0 */
    public /* synthetic */ void lambda$setUpWithCallback$0$NotificationRemoteInputManager(NotificationEntry notificationEntry, CharSequence charSequence) {
        this.mEntryManager.updateNotification(rebuildNotificationWithRemoteInput(notificationEntry, charSequence, true, null, null), null);
    }

    public boolean activateRemoteInput(View view, RemoteInput[] remoteInputArr, RemoteInput remoteInput, PendingIntent pendingIntent, NotificationEntry.EditedSuggestionInfo editedSuggestionInfo) {
        RemoteInputView remoteInputView;
        RemoteInputView remoteInputView2;
        ExpandableNotificationRow expandableNotificationRow;
        UserInfo profileParent;
        ViewParent parent = view.getParent();
        while (true) {
            remoteInputView = null;
            if (parent == null) {
                remoteInputView2 = null;
                expandableNotificationRow = null;
                break;
            }
            if (parent instanceof View) {
                View view2 = (View) parent;
                if (view2.isRootNamespace()) {
                    remoteInputView2 = findRemoteInputView(view2);
                    expandableNotificationRow = (ExpandableNotificationRow) view2.getTag(C0015R$id.row_tag_for_content_view);
                    break;
                }
            }
            parent = parent.getParent();
        }
        if (expandableNotificationRow == null) {
            return false;
        }
        expandableNotificationRow.setUserExpanded(true);
        if (!this.mLockscreenUserManager.shouldAllowLockscreenRemoteInput()) {
            int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
            boolean z = this.mUserManager.getUserInfo(identifier).isManagedProfile() && this.mKeyguardManager.isDeviceLocked(identifier);
            boolean z2 = z && (profileParent = this.mUserManager.getProfileParent(identifier)) != null && this.mKeyguardManager.isDeviceLocked(profileParent.id);
            if (this.mLockscreenUserManager.isLockscreenPublicMode(identifier) || this.mStatusBarStateController.getState() == 1) {
                if (!z || z2) {
                    this.mCallback.onLockedRemoteInput(expandableNotificationRow, view);
                } else {
                    this.mCallback.onLockedWorkRemoteInput(identifier, expandableNotificationRow, view);
                }
                return true;
            } else if (z) {
                this.mCallback.onLockedWorkRemoteInput(identifier, expandableNotificationRow, view);
                return true;
            }
        }
        if (remoteInputView2 == null || remoteInputView2.isAttachedToWindow()) {
            remoteInputView = remoteInputView2;
        }
        if (remoteInputView == null && (remoteInputView = findRemoteInputView(expandableNotificationRow.getPrivateLayout().getExpandedChild())) == null) {
            return false;
        }
        if (remoteInputView == expandableNotificationRow.getPrivateLayout().getExpandedRemoteInput() && !expandableNotificationRow.getPrivateLayout().getExpandedChild().isShown()) {
            this.mCallback.onMakeExpandedVisibleForRemoteInput(expandableNotificationRow, view);
            return true;
        } else if (!remoteInputView.isAttachedToWindow()) {
            return false;
        } else {
            int width = view.getWidth();
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getLayout() != null) {
                    width = Math.min(width, ((int) textView.getLayout().getLineWidth(0)) + textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight());
                }
            }
            int left = view.getLeft() + (width / 2);
            int top = view.getTop() + (view.getHeight() / 2);
            int width2 = remoteInputView.getWidth();
            int height = remoteInputView.getHeight() - top;
            int i = width2 - left;
            remoteInputView.setRevealParameters(left, top, Math.max(Math.max(left + top, left + height), Math.max(i + top, i + height)));
            remoteInputView.setPendingIntent(pendingIntent);
            remoteInputView.setRemoteInput(remoteInputArr, remoteInput, editedSuggestionInfo);
            remoteInputView.focusAnimated();
            return true;
        }
    }

    private RemoteInputView findRemoteInputView(View view) {
        if (view == null) {
            return null;
        }
        return (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
    }

    /* access modifiers changed from: protected */
    public void addLifetimeExtenders() {
        this.mLifetimeExtenders.add(new RemoteInputHistoryExtender());
        this.mLifetimeExtenders.add(new SmartReplyHistoryExtender());
        this.mLifetimeExtenders.add(new RemoteInputActiveExtender());
    }

    public ArrayList<NotificationLifetimeExtender> getLifetimeExtenders() {
        return this.mLifetimeExtenders;
    }

    public RemoteInputController getController() {
        return this.mRemoteInputController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void onPerformRemoveNotification(NotificationEntry notificationEntry, String str) {
        if (this.mKeysKeptForRemoteInputHistory.contains(str)) {
            this.mKeysKeptForRemoteInputHistory.remove(str);
        }
        if (this.mRemoteInputController.isRemoteInputActive(notificationEntry)) {
            this.mRemoteInputController.removeRemoteInput(notificationEntry, null);
        }
    }

    public void onPanelCollapsed() {
        for (int i = 0; i < this.mEntriesKeptForRemoteInputActive.size(); i++) {
            NotificationEntry valueAt = this.mEntriesKeptForRemoteInputActive.valueAt(i);
            this.mRemoteInputController.removeRemoteInput(valueAt, null);
            NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback = this.mNotificationLifetimeFinishedCallback;
            if (notificationSafeToRemoveCallback != null) {
                notificationSafeToRemoveCallback.onSafeToRemove(valueAt.getKey());
            }
        }
        this.mEntriesKeptForRemoteInputActive.clear();
    }

    public boolean isNotificationKeptForRemoteInputHistory(String str) {
        return this.mKeysKeptForRemoteInputHistory.contains(str);
    }

    public boolean shouldKeepForRemoteInputHistory(NotificationEntry notificationEntry) {
        if (!FORCE_REMOTE_INPUT_HISTORY) {
            return false;
        }
        if (this.mRemoteInputController.isSpinning(notificationEntry.getKey()) || notificationEntry.hasJustSentRemoteInput()) {
            return true;
        }
        return false;
    }

    public boolean shouldKeepForSmartReplyHistory(NotificationEntry notificationEntry) {
        if (!FORCE_REMOTE_INPUT_HISTORY) {
            return false;
        }
        return this.mSmartReplyController.isSendingSmartReply(notificationEntry.getKey());
    }

    public void checkRemoteInputOutside(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 4 && motionEvent.getX() == 0.0f && motionEvent.getY() == 0.0f && this.mRemoteInputController.isRemoteInputActive()) {
            this.mRemoteInputController.closeRemoteInputs();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public StatusBarNotification rebuildNotificationForCanceledSmartReplies(NotificationEntry notificationEntry) {
        return rebuildNotificationWithRemoteInput(notificationEntry, null, false, null, null);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public StatusBarNotification rebuildNotificationWithRemoteInput(NotificationEntry notificationEntry, CharSequence charSequence, boolean z, String str, Uri uri) {
        RemoteInputHistoryItem[] remoteInputHistoryItemArr;
        RemoteInputHistoryItem remoteInputHistoryItem;
        ExpandedNotification sbn = notificationEntry.getSbn();
        Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(this.mContext, sbn.getNotification().clone());
        if (!(charSequence == null && uri == null)) {
            RemoteInputHistoryItem[] parcelableArray = sbn.getNotification().extras.getParcelableArray("android.remoteInputHistoryItems");
            if (parcelableArray == null) {
                remoteInputHistoryItemArr = new RemoteInputHistoryItem[1];
            } else {
                RemoteInputHistoryItem[] remoteInputHistoryItemArr2 = new RemoteInputHistoryItem[(parcelableArray.length + 1)];
                System.arraycopy(parcelableArray, 0, remoteInputHistoryItemArr2, 1, parcelableArray.length);
                remoteInputHistoryItemArr = remoteInputHistoryItemArr2;
            }
            if (uri != null) {
                remoteInputHistoryItem = new RemoteInputHistoryItem(str, uri, charSequence);
            } else {
                remoteInputHistoryItem = new RemoteInputHistoryItem(charSequence);
            }
            remoteInputHistoryItemArr[0] = remoteInputHistoryItem;
            recoverBuilder.setRemoteInputHistory(remoteInputHistoryItemArr);
        }
        recoverBuilder.setShowRemoteInputSpinner(z);
        recoverBuilder.setHideSmartReplies(true);
        Notification build = recoverBuilder.build();
        build.contentView = sbn.getNotification().contentView;
        build.bigContentView = sbn.getNotification().bigContentView;
        build.headsUpContentView = sbn.getNotification().headsUpContentView;
        return new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), build, sbn.getUser(), sbn.getOverrideGroupKey(), sbn.getPostTime());
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationRemoteInputManager state:");
        printWriter.print("  mKeysKeptForRemoteInputHistory: ");
        printWriter.println(this.mKeysKeptForRemoteInputHistory);
        printWriter.print("  mEntriesKeptForRemoteInputActive: ");
        printWriter.println(this.mEntriesKeptForRemoteInputActive);
    }

    public void bindRow(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setRemoteInputController(this.mRemoteInputController);
    }

    public RemoteViews.OnClickHandler getRemoteViewsOnClickHandler() {
        return this.mOnClickHandler;
    }

    @VisibleForTesting
    public Set<NotificationEntry> getEntriesKeptForRemoteInputActive() {
        return this.mEntriesKeptForRemoteInputActive;
    }

    protected abstract class RemoteInputExtender implements NotificationLifetimeExtender {
        protected RemoteInputExtender() {
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
            NotificationRemoteInputManager notificationRemoteInputManager = NotificationRemoteInputManager.this;
            if (notificationRemoteInputManager.mNotificationLifetimeFinishedCallback == null) {
                notificationRemoteInputManager.mNotificationLifetimeFinishedCallback = notificationSafeToRemoveCallback;
            }
        }
    }

    /* access modifiers changed from: protected */
    public class RemoteInputHistoryExtender extends RemoteInputExtender {
        protected RemoteInputHistoryExtender() {
            super();
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
            return NotificationRemoteInputManager.this.shouldKeepForRemoteInputHistory(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
            if (z) {
                CharSequence charSequence = notificationEntry.remoteInputText;
                if (TextUtils.isEmpty(charSequence)) {
                    charSequence = notificationEntry.remoteInputTextWhenReset;
                }
                StatusBarNotification rebuildNotificationWithRemoteInput = NotificationRemoteInputManager.this.rebuildNotificationWithRemoteInput(notificationEntry, charSequence, false, notificationEntry.remoteInputMimeType, notificationEntry.remoteInputUri);
                notificationEntry.onRemoteInputInserted();
                if (rebuildNotificationWithRemoteInput != null) {
                    NotificationRemoteInputManager.this.mEntryManager.updateNotification(rebuildNotificationWithRemoteInput, null);
                    if (!notificationEntry.isRemoved()) {
                        if (Log.isLoggable("NotifRemoteInputManager", 3)) {
                            Log.d("NotifRemoteInputManager", "Keeping notification around after sending remote input " + notificationEntry.getKey());
                        }
                        NotificationRemoteInputManager.this.mKeysKeptForRemoteInputHistory.add(notificationEntry.getKey());
                        return;
                    }
                    return;
                }
                return;
            }
            NotificationRemoteInputManager.this.mKeysKeptForRemoteInputHistory.remove(notificationEntry.getKey());
        }
    }

    /* access modifiers changed from: protected */
    public class SmartReplyHistoryExtender extends RemoteInputExtender {
        protected SmartReplyHistoryExtender() {
            super();
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
            return NotificationRemoteInputManager.this.shouldKeepForSmartReplyHistory(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
            if (z) {
                StatusBarNotification rebuildNotificationForCanceledSmartReplies = NotificationRemoteInputManager.this.rebuildNotificationForCanceledSmartReplies(notificationEntry);
                if (rebuildNotificationForCanceledSmartReplies != null) {
                    NotificationRemoteInputManager.this.mEntryManager.updateNotification(rebuildNotificationForCanceledSmartReplies, null);
                    if (!notificationEntry.isRemoved()) {
                        if (Log.isLoggable("NotifRemoteInputManager", 3)) {
                            Log.d("NotifRemoteInputManager", "Keeping notification around after sending smart reply " + notificationEntry.getKey());
                        }
                        NotificationRemoteInputManager.this.mKeysKeptForRemoteInputHistory.add(notificationEntry.getKey());
                        return;
                    }
                    return;
                }
                return;
            }
            NotificationRemoteInputManager.this.mKeysKeptForRemoteInputHistory.remove(notificationEntry.getKey());
            NotificationRemoteInputManager.this.mSmartReplyController.stopSending(notificationEntry);
        }
    }

    /* access modifiers changed from: protected */
    public class RemoteInputActiveExtender extends RemoteInputExtender {
        protected RemoteInputActiveExtender() {
            super();
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
            return NotificationRemoteInputManager.this.mRemoteInputController.isRemoteInputActive(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
        public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
            if (z) {
                if (Log.isLoggable("NotifRemoteInputManager", 3)) {
                    Log.d("NotifRemoteInputManager", "Keeping notification around while remote input active " + notificationEntry.getKey());
                }
                NotificationRemoteInputManager.this.mEntriesKeptForRemoteInputActive.add(notificationEntry);
                return;
            }
            NotificationRemoteInputManager.this.mEntriesKeptForRemoteInputActive.remove(notificationEntry);
        }
    }
}
