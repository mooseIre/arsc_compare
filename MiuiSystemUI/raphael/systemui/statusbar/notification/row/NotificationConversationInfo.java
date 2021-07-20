package com.android.systemui.statusbar.notification.row;

import android.animation.TimeInterpolator;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.notification.ConversationIconFactory;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationChannelHelper;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationConversationInfo;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import java.util.Objects;
import javax.inject.Provider;

public class NotificationConversationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private int mActualHeight;
    private int mAppBubble;
    private String mAppName;
    private int mAppUid;
    private Handler mBgHandler;
    private BubbleController mBubbleController;
    private Notification.BubbleMetadata mBubbleMetadata;
    private Provider<PriorityOnboardingDialogController.Builder> mBuilderProvider;
    private TextView mDefaultDescriptionView;
    private String mDelegatePkg;
    private NotificationEntry mEntry;
    private NotificationGuts mGutsContainer;
    private INotificationManager mINotificationManager;
    private ConversationIconFactory mIconFactory;
    private boolean mIsDeviceProvisioned;
    private Handler mMainHandler;
    private NotificationChannel mNotificationChannel;
    private OnConversationSettingsClickListener mOnConversationSettingsClickListener;
    private View.OnClickListener mOnDefaultClick = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$MMDb1SDKJPIzmFXTDLDSkJw5h7E */

        public final void onClick(View view) {
            NotificationConversationInfo.this.lambda$new$1$NotificationConversationInfo(view);
        }
    };
    private View.OnClickListener mOnDone = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$a2pWqojuRPeVF2IYicCGQXQa0w */

        public final void onClick(View view) {
            NotificationConversationInfo.this.lambda$new$3$NotificationConversationInfo(view);
        }
    };
    private View.OnClickListener mOnFavoriteClick = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$2f29XNzTuIQXwaIUqXUdjSebnE */

        public final void onClick(View view) {
            NotificationConversationInfo.this.lambda$new$0$NotificationConversationInfo(view);
        }
    };
    private View.OnClickListener mOnMuteClick = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$b1cMFzfzYhzwNF5Nsg_2Oi0i80o */

        public final void onClick(View view) {
            NotificationConversationInfo.this.lambda$new$2$NotificationConversationInfo(view);
        }
    };
    private OnSettingsClickListener mOnSettingsClickListener;
    private String mPackageName;
    private PackageManager mPm;
    private boolean mPressedApply;
    private TextView mPriorityDescriptionView;
    private StatusBarNotification mSbn;
    private int mSelectedAction = -1;
    private ShortcutInfo mShortcutInfo;
    private TextView mSilentDescriptionView;
    @VisibleForTesting
    boolean mSkipPost = false;
    private Context mUserContext;
    private VisualStabilityManager mVisualStabilityManager;

    public interface OnConversationSettingsClickListener {
        void onClick();
    }

    public interface OnSettingsClickListener {
        void onClick(View view, NotificationChannel notificationChannel, int i);
    }

    public interface OnSnoozeClickListener {
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public View getContentView() {
        return this;
    }

    @VisibleForTesting
    public boolean isAnimating() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean needsFalsingProtection() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void onFinishedClosing() {
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NotificationConversationInfo(View view) {
        setSelectedAction(2);
        updateToggleActions(this.mSelectedAction, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$NotificationConversationInfo(View view) {
        setSelectedAction(0);
        updateToggleActions(this.mSelectedAction, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NotificationConversationInfo(View view) {
        setSelectedAction(4);
        updateToggleActions(this.mSelectedAction, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$NotificationConversationInfo(View view) {
        this.mPressedApply = true;
        if (this.mSelectedAction == 2 && shouldShowPriorityOnboarding()) {
            showPriorityOnboarding();
        }
        this.mGutsContainer.closeControls(view, true);
    }

    public NotificationConversationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setSelectedAction(int i) {
        if (this.mSelectedAction != i) {
            this.mSelectedAction = i;
        }
    }

    public void bindNotification(ShortcutManager shortcutManager, PackageManager packageManager, INotificationManager iNotificationManager, VisualStabilityManager visualStabilityManager, String str, NotificationChannel notificationChannel, NotificationEntry notificationEntry, Notification.BubbleMetadata bubbleMetadata, OnSettingsClickListener onSettingsClickListener, OnSnoozeClickListener onSnoozeClickListener, ConversationIconFactory conversationIconFactory, Context context, Provider<PriorityOnboardingDialogController.Builder> provider, boolean z, Handler handler, Handler handler2, OnConversationSettingsClickListener onConversationSettingsClickListener, BubbleController bubbleController) {
        this.mSelectedAction = -1;
        this.mINotificationManager = iNotificationManager;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mPackageName = str;
        this.mEntry = notificationEntry;
        ExpandedNotification sbn = notificationEntry.getSbn();
        this.mSbn = sbn;
        this.mPm = packageManager;
        this.mAppName = this.mPackageName;
        this.mOnSettingsClickListener = onSettingsClickListener;
        this.mNotificationChannel = notificationChannel;
        this.mAppUid = sbn.getUid();
        this.mDelegatePkg = this.mSbn.getOpPkg();
        this.mIsDeviceProvisioned = z;
        this.mOnConversationSettingsClickListener = onConversationSettingsClickListener;
        this.mIconFactory = conversationIconFactory;
        this.mUserContext = context;
        this.mBubbleMetadata = bubbleMetadata;
        this.mBubbleController = bubbleController;
        this.mBuilderProvider = provider;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        ShortcutInfo shortcutInfo = notificationEntry.getRanking().getShortcutInfo();
        this.mShortcutInfo = shortcutInfo;
        if (shortcutInfo != null) {
            this.mNotificationChannel = NotificationChannelHelper.createConversationChannelIfNeeded(getContext(), this.mINotificationManager, notificationEntry, this.mNotificationChannel);
            try {
                this.mAppBubble = this.mINotificationManager.getBubblePreferenceForPackage(this.mPackageName, this.mAppUid);
            } catch (RemoteException e) {
                Log.e("ConversationGuts", "can't reach OS", e);
                this.mAppBubble = 2;
            }
            bindHeader();
            bindActions();
            View findViewById = findViewById(C0015R$id.done);
            findViewById.setOnClickListener(this.mOnDone);
            findViewById.setAccessibilityDelegate(this.mGutsContainer.getAccessibilityDelegate());
            return;
        }
        throw new IllegalArgumentException("Does not have required information");
    }

    private void bindActions() {
        if (this.mAppBubble == 1) {
            ((TextView) findViewById(C0015R$id.default_summary)).setText(getResources().getString(C0021R$string.notification_channel_summary_default_with_bubbles, this.mAppName));
        }
        findViewById(C0015R$id.priority).setOnClickListener(this.mOnFavoriteClick);
        findViewById(C0015R$id.default_behavior).setOnClickListener(this.mOnDefaultClick);
        findViewById(C0015R$id.silence).setOnClickListener(this.mOnMuteClick);
        View findViewById = findViewById(C0015R$id.info);
        findViewById.setOnClickListener(getSettingsOnClickListener());
        findViewById.setVisibility(findViewById.hasOnClickListeners() ? 0 : 8);
        updateToggleActions(getSelectedAction(), false);
    }

    private void bindHeader() {
        bindConversationDetails();
        bindDelegate();
    }

    private View.OnClickListener getSettingsOnClickListener() {
        int i = this.mAppUid;
        if (i < 0 || this.mOnSettingsClickListener == null || !this.mIsDeviceProvisioned) {
            return null;
        }
        return new View.OnClickListener(i) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$jd7IzkV9FIzPNu4O1qyUjmumXQA */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                NotificationConversationInfo.this.lambda$getSettingsOnClickListener$4$NotificationConversationInfo(this.f$1, view);
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getSettingsOnClickListener$4 */
    public /* synthetic */ void lambda$getSettingsOnClickListener$4$NotificationConversationInfo(int i, View view) {
        this.mOnSettingsClickListener.onClick(view, this.mNotificationChannel, i);
    }

    private void bindConversationDetails() {
        ((TextView) findViewById(C0015R$id.parent_channel_name)).setText(this.mNotificationChannel.getName());
        bindGroup();
        bindPackage();
        bindIcon(this.mNotificationChannel.isImportantConversation());
    }

    private void bindIcon(boolean z) {
        ((ImageView) findViewById(C0015R$id.conversation_icon)).setImageDrawable(this.mIconFactory.getConversationDrawable(this.mShortcutInfo, this.mPackageName, this.mAppUid, z));
    }

    private void bindPackage() {
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(this.mPackageName, 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        ((TextView) findViewById(C0015R$id.pkg_name)).setText(this.mAppName);
    }

    private void bindDelegate() {
        TextView textView = (TextView) findViewById(C0015R$id.delegate_name);
        if (!TextUtils.equals(this.mPackageName, this.mDelegatePkg)) {
            textView.setVisibility(0);
        } else {
            textView.setVisibility(8);
        }
    }

    private void bindGroup() {
        NotificationChannel notificationChannel = this.mNotificationChannel;
        CharSequence charSequence = null;
        if (!(notificationChannel == null || notificationChannel.getGroup() == null)) {
            try {
                NotificationChannelGroup notificationChannelGroupForPackage = this.mINotificationManager.getNotificationChannelGroupForPackage(this.mNotificationChannel.getGroup(), this.mPackageName, this.mAppUid);
                if (notificationChannelGroupForPackage != null) {
                    charSequence = notificationChannelGroupForPackage.getName();
                }
            } catch (RemoteException unused) {
            }
        }
        TextView textView = (TextView) findViewById(C0015R$id.group_name);
        if (charSequence != null) {
            textView.setText(charSequence);
            textView.setVisibility(0);
            return;
        }
        textView.setVisibility(8);
    }

    public boolean post(Runnable runnable) {
        if (!this.mSkipPost) {
            return super.post(runnable);
        }
        runnable.run();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPriorityDescriptionView = (TextView) findViewById(C0015R$id.priority_summary);
        this.mDefaultDescriptionView = (TextView) findViewById(C0015R$id.default_summary);
        this.mSilentDescriptionView = (TextView) findViewById(C0015R$id.silence_summary);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mGutsContainer != null && accessibilityEvent.getEventType() == 32) {
            if (this.mGutsContainer.isExposed()) {
                accessibilityEvent.getText().add(((LinearLayout) this).mContext.getString(C0021R$string.notification_channel_controls_opened_accessibility, this.mAppName));
                return;
            }
            accessibilityEvent.getText().add(((LinearLayout) this).mContext.getString(C0021R$string.notification_channel_controls_closed_accessibility, this.mAppName));
        }
    }

    private void updateToggleActions(int i, boolean z) {
        int i2;
        boolean z2 = true;
        if (z) {
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.setOrdering(0);
            transitionSet.addTransition(new Fade(2)).addTransition(new ChangeBounds()).addTransition(new Fade(1).setStartDelay(150).setDuration(200).setInterpolator(Interpolators.FAST_OUT_SLOW_IN));
            transitionSet.setDuration(350L);
            transitionSet.setInterpolator((TimeInterpolator) Interpolators.FAST_OUT_SLOW_IN);
            TransitionManager.beginDelayedTransition(this, transitionSet);
        }
        View findViewById = findViewById(C0015R$id.priority);
        View findViewById2 = findViewById(C0015R$id.default_behavior);
        View findViewById3 = findViewById(C0015R$id.silence);
        if (i == 0) {
            this.mDefaultDescriptionView.setVisibility(0);
            this.mSilentDescriptionView.setVisibility(8);
            this.mPriorityDescriptionView.setVisibility(8);
            post(new Runnable(findViewById, findViewById2, findViewById3) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$u26KNDMKK01ES1xNd4IeWex638 */
                public final /* synthetic */ View f$0;
                public final /* synthetic */ View f$1;
                public final /* synthetic */ View f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationConversationInfo.lambda$updateToggleActions$7(this.f$0, this.f$1, this.f$2);
                }
            });
        } else if (i == 2) {
            this.mPriorityDescriptionView.setVisibility(0);
            this.mDefaultDescriptionView.setVisibility(8);
            this.mSilentDescriptionView.setVisibility(8);
            post(new Runnable(findViewById, findViewById2, findViewById3) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$B9YxfzUwTI8bD0EYToJb0uOXmZg */
                public final /* synthetic */ View f$0;
                public final /* synthetic */ View f$1;
                public final /* synthetic */ View f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationConversationInfo.lambda$updateToggleActions$5(this.f$0, this.f$1, this.f$2);
                }
            });
        } else if (i == 4) {
            this.mSilentDescriptionView.setVisibility(0);
            this.mDefaultDescriptionView.setVisibility(8);
            this.mPriorityDescriptionView.setVisibility(8);
            post(new Runnable(findViewById, findViewById2, findViewById3) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$kfzFLSnOxGZqEtBbC1UizD6b0 */
                public final /* synthetic */ View f$0;
                public final /* synthetic */ View f$1;
                public final /* synthetic */ View f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationConversationInfo.lambda$updateToggleActions$6(this.f$0, this.f$1, this.f$2);
                }
            });
        } else {
            throw new IllegalArgumentException("Unrecognized behavior: " + this.mSelectedAction);
        }
        boolean z3 = getSelectedAction() != i;
        TextView textView = (TextView) findViewById(C0015R$id.done);
        if (z3) {
            i2 = C0021R$string.inline_ok_button;
        } else {
            i2 = C0021R$string.inline_done_button;
        }
        textView.setText(i2);
        if (i != 2) {
            z2 = false;
        }
        bindIcon(z2);
    }

    static /* synthetic */ void lambda$updateToggleActions$5(View view, View view2, View view3) {
        view.setSelected(true);
        view2.setSelected(false);
        view3.setSelected(false);
    }

    static /* synthetic */ void lambda$updateToggleActions$6(View view, View view2, View view3) {
        view.setSelected(false);
        view2.setSelected(false);
        view3.setSelected(true);
    }

    static /* synthetic */ void lambda$updateToggleActions$7(View view, View view2, View view3) {
        view.setSelected(false);
        view2.setSelected(true);
        view3.setSelected(false);
    }

    /* access modifiers changed from: package-private */
    public int getSelectedAction() {
        if (this.mNotificationChannel.getImportance() <= 2 && this.mNotificationChannel.getImportance() > -1000) {
            return 4;
        }
        if (this.mNotificationChannel.isImportantConversation()) {
            return 2;
        }
        return 0;
    }

    private void updateChannel() {
        this.mBgHandler.post(new UpdateChannelRunnable(this.mINotificationManager, this.mPackageName, this.mAppUid, this.mSelectedAction, this.mNotificationChannel));
        this.mEntry.markForUserTriggeredMovement(true);
        Handler handler = this.mMainHandler;
        VisualStabilityManager visualStabilityManager = this.mVisualStabilityManager;
        Objects.requireNonNull(visualStabilityManager);
        handler.postDelayed(new Runnable() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$s0tJwNXwV57TLmqR7wIRXxgPwoc */

            public final void run() {
                VisualStabilityManager.this.temporarilyAllowReordering();
            }
        }, 300);
    }

    private boolean shouldShowPriorityOnboarding() {
        return !Prefs.getBoolean(this.mUserContext, "HasUserSeenPriorityOnboarding", false);
    }

    private void showPriorityOnboarding() {
        boolean z;
        View inflate = LayoutInflater.from(((LinearLayout) this).mContext).inflate(C0017R$layout.priority_onboarding_half_shell, (ViewGroup) null);
        boolean z2 = true;
        try {
            if (this.mINotificationManager.getConsolidatedNotificationPolicy().priorityConversationSenders == 2) {
                z = true;
                Notification.BubbleMetadata bubbleMetadata = this.mBubbleMetadata;
                if (bubbleMetadata == null || !bubbleMetadata.getAutoExpandBubble() || Settings.Global.getInt(((LinearLayout) this).mContext.getContentResolver(), "notification_bubbles", 0) != 1) {
                    z2 = false;
                }
                PriorityOnboardingDialogController.Builder builder = this.mBuilderProvider.get();
                builder.setContext(this.mUserContext);
                builder.setView(inflate);
                builder.setIgnoresDnd(z);
                builder.setShowsAsBubble(z2);
                builder.setIcon(this.mIconFactory.getBaseIconDrawable(this.mShortcutInfo));
                builder.setBadge(this.mIconFactory.getAppBadge(this.mPackageName, UserHandle.getUserId(this.mSbn.getUid())));
                builder.setOnSettingsClick(this.mOnConversationSettingsClickListener);
                PriorityOnboardingDialogController build = builder.build();
                build.init();
                build.show();
            }
        } catch (RemoteException e) {
            Log.e("ConversationGuts", "Could not check conversation senders", e);
        }
        z = false;
        Notification.BubbleMetadata bubbleMetadata2 = this.mBubbleMetadata;
        z2 = false;
        PriorityOnboardingDialogController.Builder builder2 = this.mBuilderProvider.get();
        builder2.setContext(this.mUserContext);
        builder2.setView(inflate);
        builder2.setIgnoresDnd(z);
        builder2.setShowsAsBubble(z2);
        builder2.setIcon(this.mIconFactory.getBaseIconDrawable(this.mShortcutInfo));
        builder2.setBadge(this.mIconFactory.getAppBadge(this.mPackageName, UserHandle.getUserId(this.mSbn.getUid())));
        builder2.setOnSettingsClick(this.mOnConversationSettingsClickListener);
        PriorityOnboardingDialogController build2 = builder2.build();
        build2.init();
        build2.show();
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return this.mPressedApply;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean handleCloseControls(boolean z, boolean z2) {
        if (!z || this.mSelectedAction <= -1) {
            return false;
        }
        updateChannel();
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public int getActualHeight() {
        return this.mActualHeight;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mActualHeight = getHeight();
    }

    /* access modifiers changed from: package-private */
    public class UpdateChannelRunnable implements Runnable {
        private final int mAction;
        private final String mAppPkg;
        private final int mAppUid;
        private NotificationChannel mChannelToUpdate;
        private final INotificationManager mINotificationManager;

        public UpdateChannelRunnable(INotificationManager iNotificationManager, String str, int i, int i2, NotificationChannel notificationChannel) {
            this.mINotificationManager = iNotificationManager;
            this.mAppPkg = str;
            this.mAppUid = i;
            this.mChannelToUpdate = notificationChannel;
            this.mAction = i2;
        }

        public void run() {
            try {
                int i = this.mAction;
                if (i == 0) {
                    this.mChannelToUpdate.setImportance(Math.max(this.mChannelToUpdate.getOriginalImportance(), 3));
                    if (this.mChannelToUpdate.isImportantConversation()) {
                        this.mChannelToUpdate.setImportantConversation(false);
                        this.mChannelToUpdate.setAllowBubbles(false);
                    }
                } else if (i == 2) {
                    this.mChannelToUpdate.setImportantConversation(true);
                    if (this.mChannelToUpdate.isImportantConversation()) {
                        this.mChannelToUpdate.setAllowBubbles(true);
                        if (NotificationConversationInfo.this.mAppBubble == 0) {
                            this.mINotificationManager.setBubblesAllowed(this.mAppPkg, this.mAppUid, 2);
                        }
                        NotificationConversationInfo.this.post(new Runnable() {
                            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationConversationInfo$UpdateChannelRunnable$_TdBndU_iWORDLe32ALCWoJYfU */

                            public final void run() {
                                NotificationConversationInfo.UpdateChannelRunnable.this.lambda$run$0$NotificationConversationInfo$UpdateChannelRunnable();
                            }
                        });
                    }
                    this.mChannelToUpdate.setImportance(Math.max(this.mChannelToUpdate.getOriginalImportance(), 3));
                } else if (i == 4) {
                    if (this.mChannelToUpdate.getImportance() == -1000 || this.mChannelToUpdate.getImportance() >= 3) {
                        this.mChannelToUpdate.setImportance(2);
                    }
                    if (this.mChannelToUpdate.isImportantConversation()) {
                        this.mChannelToUpdate.setImportantConversation(false);
                        this.mChannelToUpdate.setAllowBubbles(false);
                    }
                }
                this.mINotificationManager.updateNotificationChannelForPackage(this.mAppPkg, this.mAppUid, this.mChannelToUpdate);
            } catch (RemoteException e) {
                Log.e("ConversationGuts", "Unable to update notification channel", e);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$run$0 */
        public /* synthetic */ void lambda$run$0$NotificationConversationInfo$UpdateChannelRunnable() {
            NotificationConversationInfo.this.mBubbleController.onUserChangedImportance(NotificationConversationInfo.this.mEntry);
        }
    }
}
