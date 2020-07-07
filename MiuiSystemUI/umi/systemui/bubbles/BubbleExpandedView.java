package com.android.systemui.bubbles;

import android.app.ActivityOptions;
import android.app.ActivityView;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.ServiceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.bubbles.BubbleExpandedView;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.stack.ExpandableViewState;
import miui.view.MiuiHapticFeedbackConstants;

public class BubbleExpandedView extends LinearLayout implements View.OnClickListener {
    /* access modifiers changed from: private */
    public ActivityView mActivityView;
    /* access modifiers changed from: private */
    public boolean mActivityViewReady;
    private Drawable mAppIcon;
    private String mAppName;
    /* access modifiers changed from: private */
    public BubbleController mBubbleController;
    /* access modifiers changed from: private */
    public PendingIntent mBubbleIntent;
    /* access modifiers changed from: private */
    public NotificationData.Entry mEntry;
    private boolean mKeyboardVisible;
    private int mMinHeight;
    private boolean mNeedsNewHeight;
    private ExpandableNotificationRow mNotifRow;
    private PackageManager mPm;
    private ShapeDrawable mPointerDrawable;
    private int mPointerHeight;
    private int mPointerMargin;
    private View mPointerView;
    private int mPointerWidth;
    private AlphaOptimizedButton mSettingsIcon;
    private int mSettingsIconHeight;
    private BubbleStackView mStackView;
    private ActivityView.StateCallback mStateCallback;

    public interface OnBubbleBlockedListener {
    }

    public void setOnBlockedListener(OnBubbleBlockedListener onBubbleBlockedListener) {
    }

    public BubbleExpandedView(Context context) {
        this(context, (AttributeSet) null);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mActivityViewReady = false;
        this.mBubbleController = (BubbleController) Dependency.get(BubbleController.class);
        this.mStateCallback = new ActivityView.StateCallback() {
            public void onActivityViewReady(ActivityView activityView) {
                if (!BubbleExpandedView.this.mActivityViewReady) {
                    boolean unused = BubbleExpandedView.this.mActivityViewReady = true;
                    ActivityOptions.makeCustomAnimation(BubbleExpandedView.this.getContext(), 0, 0);
                    BubbleExpandedView.this.post(new Runnable() {
                        public final void run() {
                            BubbleExpandedView.AnonymousClass1.this.lambda$onActivityViewReady$0$BubbleExpandedView$1();
                        }
                    });
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onActivityViewReady$0 */
            public /* synthetic */ void lambda$onActivityViewReady$0$BubbleExpandedView$1() {
                BubbleExpandedView.this.mActivityView.startActivity(BubbleExpandedView.this.mBubbleIntent);
            }

            public void onActivityViewDestroyed(ActivityView activityView) {
                boolean unused = BubbleExpandedView.this.mActivityViewReady = false;
            }

            public void onTaskRemovalStarted(int i) {
                if (BubbleExpandedView.this.mEntry != null) {
                    BubbleExpandedView.this.post(new Runnable() {
                        public final void run() {
                            BubbleExpandedView.AnonymousClass1.this.lambda$onTaskRemovalStarted$1$BubbleExpandedView$1();
                        }
                    });
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onTaskRemovalStarted$1 */
            public /* synthetic */ void lambda$onTaskRemovalStarted$1$BubbleExpandedView$1() {
                BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.mEntry.key, 3);
            }
        };
        this.mPm = context.getPackageManager();
        this.mMinHeight = getResources().getDimensionPixelSize(R.dimen.bubble_expanded_default_height);
        this.mPointerMargin = getResources().getDimensionPixelSize(R.dimen.bubble_pointer_margin);
        try {
            INotificationManager.Stub.asInterface(ServiceManager.getServiceOrThrow("notification"));
        } catch (ServiceManager.ServiceNotFoundException e) {
            Log.w("BubbleExpandedView", e);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = getResources();
        this.mPointerView = findViewById(R.id.pointer_view);
        this.mPointerWidth = resources.getDimensionPixelSize(R.dimen.bubble_pointer_width);
        this.mPointerHeight = resources.getDimensionPixelSize(R.dimen.bubble_pointer_height);
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.create((float) this.mPointerWidth, (float) this.mPointerHeight, true));
        this.mPointerDrawable = shapeDrawable;
        this.mPointerView.setBackground(shapeDrawable);
        this.mPointerView.setVisibility(8);
        this.mSettingsIconHeight = getContext().getResources().getDimensionPixelSize(R.dimen.bubble_expanded_header_height);
        AlphaOptimizedButton alphaOptimizedButton = (AlphaOptimizedButton) findViewById(R.id.settings_button);
        this.mSettingsIcon = alphaOptimizedButton;
        alphaOptimizedButton.setOnClickListener(this);
        ActivityView activityView = new ActivityView(this.mContext, (AttributeSet) null, 0, true);
        this.mActivityView = activityView;
        addView(activityView);
        bringChildToFront(this.mActivityView);
        bringChildToFront(this.mSettingsIcon);
        applyThemeAttrs();
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return BubbleExpandedView.this.lambda$onFinishInflate$0$BubbleExpandedView(view, windowInsets);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ WindowInsets lambda$onFinishInflate$0$BubbleExpandedView(View view, WindowInsets windowInsets) {
        boolean z = windowInsets.getSystemWindowInsetBottom() - windowInsets.getStableInsetBottom() != 0;
        this.mKeyboardVisible = z;
        if (!z && this.mNeedsNewHeight) {
            updateHeight();
        }
        return view.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: package-private */
    public void applyThemeAttrs() {
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(R$styleable.BubbleExpandedView);
        int color = obtainStyledAttributes.getColor(0, -1);
        float dimension = obtainStyledAttributes.getDimension(1, 0.0f);
        obtainStyledAttributes.recycle();
        this.mPointerDrawable.setTint(color);
        if (ScreenDecorationsUtils.supportsRoundedCornersOnWindows(this.mContext.getResources())) {
            this.mActivityView.setCornerRadius(dimension);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyboardVisible = false;
        this.mNeedsNewHeight = false;
        ActivityView activityView = this.mActivityView;
        if (activityView != null) {
            activityView.setForwardedInsets(Insets.of(0, 0, 0, 0));
        }
    }

    /* access modifiers changed from: package-private */
    public void updateInsets(WindowInsets windowInsets) {
        if (usingActivityView()) {
            Point point = new Point();
            this.mActivityView.getContext().getDisplay().getSize(point);
            this.mActivityView.setForwardedInsets(Insets.of(0, 0, 0, Math.max(0, ((this.mActivityView.getLocationOnScreen()[1] + this.mActivityView.getHeight()) + (windowInsets.getSystemWindowInsetBottom() - windowInsets.getStableInsetBottom())) - point.y)));
        }
    }

    public void setEntry(NotificationData.Entry entry, BubbleStackView bubbleStackView, String str) {
        this.mStackView = bubbleStackView;
        this.mEntry = entry;
        this.mAppName = str;
        try {
            ApplicationInfo applicationInfo = this.mPm.getApplicationInfo(entry.notification.getPackageName(), 795136);
            if (applicationInfo != null) {
                this.mAppIcon = this.mPm.getApplicationIcon(applicationInfo);
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        if (this.mAppIcon == null) {
            this.mAppIcon = this.mPm.getDefaultActivityIcon();
        }
        applyThemeAttrs();
        showSettingsIcon();
        updateExpandedView();
    }

    public void populateExpandedView() {
        if (usingActivityView()) {
            this.mActivityView.setCallback(this.mStateCallback);
            return;
        }
        ViewGroup viewGroup = (ViewGroup) this.mNotifRow.getParent();
        if (viewGroup != this) {
            if (viewGroup != null) {
                viewGroup.removeView(this.mNotifRow);
            }
            addView(this.mNotifRow, 1);
        }
    }

    public void update(NotificationData.Entry entry) {
        if (entry.key.equals(this.mEntry.key)) {
            this.mEntry = entry;
            updateSettingsContentDescription();
            updateHeight();
            return;
        }
        Log.w("BubbleExpandedView", "Trying to update entry with different key, new entry: " + entry.key + " old entry: " + this.mEntry.key);
    }

    private void updateExpandedView() {
        PendingIntent bubbleIntent = getBubbleIntent(this.mEntry);
        this.mBubbleIntent = bubbleIntent;
        if (bubbleIntent != null) {
            ExpandableNotificationRow expandableNotificationRow = this.mNotifRow;
            if (expandableNotificationRow != null) {
                removeView(expandableNotificationRow);
                this.mNotifRow = null;
            }
            this.mActivityView.setVisibility(0);
        }
        updateView();
    }

    /* access modifiers changed from: package-private */
    public boolean performBackPressIfNeeded() {
        if (!usingActivityView()) {
            return false;
        }
        this.mActivityView.performBackPress();
        return true;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x008f  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateHeight() {
        /*
            r5 = this;
            boolean r0 = r5.usingActivityView()
            if (r0 == 0) goto L_0x00a1
            com.android.systemui.statusbar.NotificationData$Entry r0 = r5.mEntry
            com.android.systemui.miui.statusbar.ExpandedNotification r0 = r0.notification
            android.app.Notification r0 = r0.getNotification()
            android.app.Notification$BubbleMetadata r0 = r0.getBubbleMetadata()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x001e
            com.android.systemui.bubbles.BubbleStackView r0 = r5.mStackView
            int r0 = r0.getMaxExpandedHeight()
        L_0x001c:
            float r0 = (float) r0
            goto L_0x0064
        L_0x001e:
            int r3 = r0.getDesiredHeightResId()
            if (r3 == 0) goto L_0x0026
            r3 = r1
            goto L_0x0027
        L_0x0026:
            r3 = r2
        L_0x0027:
            if (r3 == 0) goto L_0x0047
            int r0 = r0.getDesiredHeightResId()
            com.android.systemui.statusbar.NotificationData$Entry r3 = r5.mEntry
            com.android.systemui.miui.statusbar.ExpandedNotification r3 = r3.notification
            java.lang.String r3 = r3.getPackageName()
            com.android.systemui.statusbar.NotificationData$Entry r4 = r5.mEntry
            com.android.systemui.miui.statusbar.ExpandedNotification r4 = r4.notification
            android.os.UserHandle r4 = r4.getUser()
            int r4 = r4.getIdentifier()
            int r0 = r5.getDimenForPackageUser(r0, r3, r4)
            float r0 = (float) r0
            goto L_0x005b
        L_0x0047:
            int r0 = r0.getDesiredHeight()
            float r0 = (float) r0
            android.content.Context r3 = r5.getContext()
            android.content.res.Resources r3 = r3.getResources()
            android.util.DisplayMetrics r3 = r3.getDisplayMetrics()
            float r3 = r3.density
            float r0 = r0 * r3
        L_0x005b:
            r3 = 0
            int r3 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r3 <= 0) goto L_0x0061
            goto L_0x0064
        L_0x0061:
            int r0 = r5.mMinHeight
            goto L_0x001c
        L_0x0064:
            com.android.systemui.bubbles.BubbleStackView r3 = r5.mStackView
            int r3 = r3.getMaxExpandedHeight()
            int r4 = r5.mSettingsIconHeight
            int r3 = r3 - r4
            int r4 = r5.mPointerHeight
            int r3 = r3 - r4
            int r4 = r5.mPointerMargin
            int r3 = r3 - r4
            float r3 = (float) r3
            float r0 = java.lang.Math.min(r0, r3)
            int r3 = r5.mMinHeight
            float r3 = (float) r3
            float r0 = java.lang.Math.max(r0, r3)
            android.app.ActivityView r3 = r5.mActivityView
            android.view.ViewGroup$LayoutParams r3 = r3.getLayoutParams()
            android.widget.LinearLayout$LayoutParams r3 = (android.widget.LinearLayout.LayoutParams) r3
            int r4 = r3.height
            float r4 = (float) r4
            int r4 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1))
            if (r4 == 0) goto L_0x008f
            goto L_0x0090
        L_0x008f:
            r1 = r2
        L_0x0090:
            r5.mNeedsNewHeight = r1
            boolean r1 = r5.mKeyboardVisible
            if (r1 != 0) goto L_0x00a8
            int r0 = (int) r0
            r3.height = r0
            android.app.ActivityView r0 = r5.mActivityView
            r0.setLayoutParams(r3)
            r5.mNeedsNewHeight = r2
            goto L_0x00a8
        L_0x00a1:
            com.android.systemui.statusbar.ExpandableNotificationRow r5 = r5.mNotifRow
            if (r5 == 0) goto L_0x00a8
            r5.getIntrinsicHeight()
        L_0x00a8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.BubbleExpandedView.updateHeight():void");
    }

    public void onClick(View view) {
        NotificationData.Entry entry = this.mEntry;
        if (entry != null) {
            entry.notification.getNotification();
            if (view.getId() == R.id.settings_button) {
                this.mStackView.collapseStack(new Runnable(getSettingsIntent(this.mEntry.notification.getPackageName(), this.mEntry.notification.getUid())) {
                    public final /* synthetic */ Intent f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        BubbleExpandedView.this.lambda$onClick$1$BubbleExpandedView(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClick$1 */
    public /* synthetic */ void lambda$onClick$1$BubbleExpandedView(Intent intent) {
        this.mContext.startActivityAsUser(intent, this.mEntry.notification.getUser());
        logBubbleClickEvent(this.mEntry, 9);
    }

    private void updateSettingsContentDescription() {
        this.mSettingsIcon.setContentDescription(getResources().getString(R.string.bubbles_settings_button_description, new Object[]{this.mAppName}));
    }

    /* access modifiers changed from: package-private */
    public void showSettingsIcon() {
        updateSettingsContentDescription();
        this.mSettingsIcon.setVisibility(0);
    }

    public void updateView() {
        if (!usingActivityView() || this.mActivityView.getVisibility() != 0 || !this.mActivityView.isAttachedToWindow()) {
            ExpandableNotificationRow expandableNotificationRow = this.mNotifRow;
            if (expandableNotificationRow != null) {
                applyRowState(expandableNotificationRow);
            }
        } else {
            this.mActivityView.onLocationChanged();
        }
        updateHeight();
    }

    public void setPointerPosition(float f) {
        this.mPointerView.setTranslationX(f - (((float) this.mPointerWidth) / 2.0f));
        this.mPointerView.setVisibility(0);
    }

    public void cleanUpExpandedState() {
        removeView(this.mNotifRow);
        ActivityView activityView = this.mActivityView;
        if (activityView != null) {
            if (this.mActivityViewReady) {
                activityView.release();
            }
            removeView(this.mActivityView);
            this.mActivityView = null;
            this.mActivityViewReady = false;
        }
    }

    private boolean usingActivityView() {
        return (this.mBubbleIntent == null || this.mActivityView == null) ? false : true;
    }

    public int getVirtualDisplayId() {
        if (usingActivityView()) {
            return this.mActivityView.getVirtualDisplayId();
        }
        return -1;
    }

    private void applyRowState(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.reset();
        expandableNotificationRow.setHeadsUp(false);
        expandableNotificationRow.resetTranslation();
        expandableNotificationRow.setOnKeyguard(false);
        expandableNotificationRow.setShowAmbient(false);
        expandableNotificationRow.setClipBottomAmount(0);
        expandableNotificationRow.setClipTopAmount(0);
        expandableNotificationRow.setContentTransformationAmount(0.0f, false);
        expandableNotificationRow.setIconsVisible(true);
        ExpandableViewState expandableViewState = new ExpandableViewState();
        expandableViewState.height = expandableNotificationRow.getIntrinsicHeight();
        expandableViewState.gone = false;
        expandableViewState.hidden = false;
        expandableViewState.dimmed = false;
        expandableViewState.dark = false;
        expandableViewState.alpha = 1.0f;
        expandableViewState.notGoneIndex = -1;
        expandableViewState.xTranslation = 0.0f;
        expandableViewState.yTranslation = 0.0f;
        expandableViewState.zTranslation = 0.0f;
        expandableViewState.scaleX = 1.0f;
        expandableViewState.scaleY = 1.0f;
        expandableViewState.inShelf = true;
        expandableViewState.headsUpIsVisible = false;
        expandableViewState.applyToView(expandableNotificationRow);
    }

    private Intent getSettingsIntent(String str, int i) {
        Intent intent = new Intent("android.settings.APP_NOTIFICATION_BUBBLE_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", str);
        intent.putExtra("app_uid", i);
        intent.addFlags(134217728);
        intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
        intent.addFlags(536870912);
        return intent;
    }

    private PendingIntent getBubbleIntent(NotificationData.Entry entry) {
        Notification.BubbleMetadata bubbleMetadata = entry.notification.getNotification().getBubbleMetadata();
        if (!BubbleController.canLaunchInActivityView(this.mContext, entry) || bubbleMetadata == null) {
            return null;
        }
        return bubbleMetadata.getIntent();
    }

    private void logBubbleClickEvent(NotificationData.Entry entry, int i) {
        ExpandedNotification expandedNotification = entry.notification;
        String packageName = expandedNotification.getPackageName();
        String channelId = expandedNotification.getNotification().getChannelId();
        int id = expandedNotification.getId();
        BubbleStackView bubbleStackView = this.mStackView;
        SysUiStatsLog.write(149, packageName, channelId, id, bubbleStackView.getBubbleIndex(bubbleStackView.getExpandedBubble()), this.mStackView.getBubbleCount(), i, this.mStackView.getNormalizedXPosition(), this.mStackView.getNormalizedYPosition(), entry.showInShadeWhenBubble(), entry.isForegroundService(), BubbleController.isForegroundApp(this.mContext, expandedNotification.getPackageName()));
    }

    private int getDimenForPackageUser(int i, String str, int i2) {
        if (str != null) {
            if (i2 == -1) {
                i2 = 0;
            }
            try {
                return this.mPm.getResourcesForApplicationAsUser(str, i2).getDimensionPixelSize(i);
            } catch (PackageManager.NameNotFoundException unused) {
            } catch (Resources.NotFoundException e) {
                Log.e("BubbleExpandedView", "Couldn't find desired height res id", e);
            }
        }
        return 0;
    }
}
