package com.android.systemui.bubbles;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.ActivityView;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.display.VirtualDisplay;
import android.os.Binder;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleExpandedView;
import com.android.systemui.recents.TriangleShape;
import com.android.systemui.statusbar.AlphaOptimizedButton;

public class BubbleExpandedView extends LinearLayout {
    private ActivityManager mActivityManager;
    private SurfaceView mActivitySurface;
    private ActivityView mActivityView;
    private FrameLayout mActivityViewContainer;
    private ActivityViewStatus mActivityViewStatus;
    private Bubble mBubble;
    private BubbleController mBubbleController;
    private float mCornerRadius;
    private Point mDisplaySize;
    private int[] mExpandedViewContainerLocation;
    private int mExpandedViewPadding;
    private boolean mImeShowing;
    private boolean mIsOverflow;
    private boolean mKeyboardVisible;
    private int mMinHeight;
    private boolean mNeedsNewHeight;
    private int mOverflowHeight;
    private PendingIntent mPendingIntent;
    private ShapeDrawable mPointerDrawable;
    private int mPointerHeight;
    private int mPointerMargin;
    private View mPointerView;
    private int mPointerWidth;
    private AlphaOptimizedButton mSettingsIcon;
    private int mSettingsIconHeight;
    private BubbleStackView mStackView;
    private ActivityView.StateCallback mStateCallback;
    private int mTaskId;
    private WindowManager mVirtualDisplayWindowManager;
    private View mVirtualImeView;
    private WindowManager mWindowManager;

    /* access modifiers changed from: private */
    public enum ActivityViewStatus {
        INITIALIZING,
        INITIALIZED,
        ACTIVITY_STARTED,
        RELEASED
    }

    /* renamed from: com.android.systemui.bubbles.BubbleExpandedView$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.android.systemui.bubbles.BubbleExpandedView$ActivityViewStatus[] r0 = com.android.systemui.bubbles.BubbleExpandedView.ActivityViewStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass4.$SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus = r0
                com.android.systemui.bubbles.BubbleExpandedView$ActivityViewStatus r1 = com.android.systemui.bubbles.BubbleExpandedView.ActivityViewStatus.INITIALIZING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass4.$SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.systemui.bubbles.BubbleExpandedView$ActivityViewStatus r1 = com.android.systemui.bubbles.BubbleExpandedView.ActivityViewStatus.INITIALIZED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass4.$SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.systemui.bubbles.BubbleExpandedView$ActivityViewStatus r1 = com.android.systemui.bubbles.BubbleExpandedView.ActivityViewStatus.ACTIVITY_STARTED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass4.<clinit>():void");
        }
    }

    public BubbleExpandedView(Context context) {
        this(context, null);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BubbleExpandedView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mActivityViewStatus = ActivityViewStatus.INITIALIZING;
        this.mTaskId = -1;
        this.mBubbleController = (BubbleController) Dependency.get(BubbleController.class);
        this.mImeShowing = false;
        this.mCornerRadius = 0.0f;
        this.mActivityViewContainer = new FrameLayout(getContext());
        this.mStateCallback = new ActivityView.StateCallback() {
            /* class com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass1 */

            public void onActivityViewReady(ActivityView activityView) {
                int i = AnonymousClass4.$SwitchMap$com$android$systemui$bubbles$BubbleExpandedView$ActivityViewStatus[BubbleExpandedView.this.mActivityViewStatus.ordinal()];
                if (i == 1 || i == 2) {
                    ActivityOptions makeCustomAnimation = ActivityOptions.makeCustomAnimation(BubbleExpandedView.this.getContext(), 0, 0);
                    makeCustomAnimation.setTaskAlwaysOnTop(true);
                    makeCustomAnimation.setLaunchWindowingMode(6);
                    BubbleExpandedView.this.post(new Runnable(makeCustomAnimation) {
                        /* class com.android.systemui.bubbles.$$Lambda$BubbleExpandedView$1$g0YjNvBWtSGWit8uywvLlkarcag */
                        public final /* synthetic */ ActivityOptions f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            BubbleExpandedView.AnonymousClass1.this.lambda$onActivityViewReady$0$BubbleExpandedView$1(this.f$1);
                        }
                    });
                    BubbleExpandedView.this.mActivityViewStatus = ActivityViewStatus.ACTIVITY_STARTED;
                } else if (i == 3) {
                    BubbleExpandedView.this.post(new Runnable() {
                        /* class com.android.systemui.bubbles.$$Lambda$BubbleExpandedView$1$3ncDnIQQCskyrWWDiIMVoTXxvGg */

                        public final void run() {
                            BubbleExpandedView.AnonymousClass1.this.lambda$onActivityViewReady$1$BubbleExpandedView$1();
                        }
                    });
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onActivityViewReady$0 */
            public /* synthetic */ void lambda$onActivityViewReady$0$BubbleExpandedView$1(ActivityOptions activityOptions) {
                if (BubbleExpandedView.this.mActivityView == null) {
                    BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.getBubbleKey(), 10);
                    return;
                }
                try {
                    if (BubbleExpandedView.this.mIsOverflow || !BubbleExpandedView.this.mBubble.hasMetadataShortcutId() || BubbleExpandedView.this.mBubble.getShortcutInfo() == null) {
                        Intent intent = new Intent();
                        intent.addFlags(524288);
                        intent.addFlags(134217728);
                        if (BubbleExpandedView.this.mBubble != null) {
                            BubbleExpandedView.this.mBubble.setIntentActive();
                        }
                        BubbleExpandedView.this.mActivityView.startActivity(BubbleExpandedView.this.mPendingIntent, intent, activityOptions);
                        return;
                    }
                    activityOptions.setApplyActivityFlagsForBubbles(true);
                    BubbleExpandedView.this.mActivityView.startShortcutActivity(BubbleExpandedView.this.mBubble.getShortcutInfo(), activityOptions, (Rect) null);
                } catch (RuntimeException e) {
                    Log.w("Bubbles", "Exception while displaying bubble: " + BubbleExpandedView.this.getBubbleKey() + ", " + e.getMessage() + "; removing bubble");
                    BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.getBubbleKey(), 10);
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onActivityViewReady$1 */
            public /* synthetic */ void lambda$onActivityViewReady$1$BubbleExpandedView$1() {
                BubbleExpandedView.this.mActivityManager.moveTaskToFront(BubbleExpandedView.this.mTaskId, 0);
            }

            public void onActivityViewDestroyed(ActivityView activityView) {
                BubbleExpandedView.this.mActivityViewStatus = ActivityViewStatus.RELEASED;
            }

            public void onTaskCreated(int i, ComponentName componentName) {
                BubbleExpandedView.this.mTaskId = i;
            }

            public void onTaskRemovalStarted(int i) {
                if (BubbleExpandedView.this.mBubble != null) {
                    BubbleExpandedView.this.post(new Runnable() {
                        /* class com.android.systemui.bubbles.$$Lambda$BubbleExpandedView$1$8DxRCXjWXDjbCBbQO_LWChzL0s */

                        public final void run() {
                            BubbleExpandedView.AnonymousClass1.this.lambda$onTaskRemovalStarted$2$BubbleExpandedView$1();
                        }
                    });
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onTaskRemovalStarted$2 */
            public /* synthetic */ void lambda$onTaskRemovalStarted$2$BubbleExpandedView$1() {
                BubbleExpandedView.this.mBubbleController.removeBubble(BubbleExpandedView.this.mBubble.getKey(), 3);
            }
        };
        updateDimensions();
        this.mActivityManager = (ActivityManager) ((LinearLayout) this).mContext.getSystemService("activity");
    }

    /* access modifiers changed from: package-private */
    public void updateDimensions() {
        this.mDisplaySize = new Point();
        WindowManager windowManager = (WindowManager) ((LinearLayout) this).mContext.getSystemService("window");
        this.mWindowManager = windowManager;
        windowManager.getDefaultDisplay().getRealSize(this.mDisplaySize);
        Resources resources = getResources();
        this.mMinHeight = resources.getDimensionPixelSize(C0012R$dimen.bubble_expanded_default_height);
        this.mOverflowHeight = resources.getDimensionPixelSize(C0012R$dimen.bubble_overflow_height);
        this.mPointerMargin = resources.getDimensionPixelSize(C0012R$dimen.bubble_pointer_margin);
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"ClickableViewAccessibility"})
    public void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = getResources();
        this.mPointerView = findViewById(C0015R$id.pointer_view);
        this.mPointerWidth = resources.getDimensionPixelSize(C0012R$dimen.bubble_pointer_width);
        this.mPointerHeight = resources.getDimensionPixelSize(C0012R$dimen.bubble_pointer_height);
        this.mPointerDrawable = new ShapeDrawable(TriangleShape.create((float) this.mPointerWidth, (float) this.mPointerHeight, true));
        this.mPointerView.setVisibility(4);
        this.mSettingsIconHeight = getContext().getResources().getDimensionPixelSize(C0012R$dimen.bubble_manage_button_height);
        this.mSettingsIcon = (AlphaOptimizedButton) findViewById(C0015R$id.settings_button);
        this.mActivityView = new ActivityView(((LinearLayout) this).mContext, (AttributeSet) null, 0, true, false, true);
        setContentVisibility(false);
        this.mActivityViewContainer.setBackgroundColor(-1);
        this.mActivityViewContainer.setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass2 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), BubbleExpandedView.this.mCornerRadius);
            }
        });
        this.mActivityViewContainer.setClipToOutline(true);
        this.mActivityViewContainer.addView(this.mActivityView);
        this.mActivityViewContainer.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        addView(this.mActivityViewContainer);
        ActivityView activityView = this.mActivityView;
        if (activityView != null && activityView.getChildCount() > 0 && (this.mActivityView.getChildAt(0) instanceof SurfaceView)) {
            this.mActivitySurface = (SurfaceView) this.mActivityView.getChildAt(0);
        }
        bringChildToFront(this.mActivityView);
        bringChildToFront(this.mSettingsIcon);
        applyThemeAttrs();
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleExpandedView$BUIzmdcN6x4TJwxemNSjSITgNeY */

            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return BubbleExpandedView.this.lambda$onFinishInflate$0$BubbleExpandedView(view, windowInsets);
            }
        });
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.bubble_expanded_view_padding);
        this.mExpandedViewPadding = dimensionPixelSize;
        setPadding(dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize);
        setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleExpandedView$iXgIC2YqSeoFlnxX0VwZo_0Hqrs */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return BubbleExpandedView.this.lambda$onFinishInflate$1$BubbleExpandedView(view, motionEvent);
            }
        });
        setLayoutDirection(3);
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

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$1 */
    public /* synthetic */ boolean lambda$onFinishInflate$1$BubbleExpandedView(View view, MotionEvent motionEvent) {
        if (!usingActivityView()) {
            return false;
        }
        Rect rect = new Rect();
        this.mActivityView.getBoundsOnScreen(rect);
        if (motionEvent.getRawY() < ((float) rect.top) || motionEvent.getRawY() > ((float) rect.bottom) || (motionEvent.getRawX() >= ((float) rect.left) && motionEvent.getRawX() <= ((float) rect.right))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getBubbleKey() {
        Bubble bubble = this.mBubble;
        return bubble != null ? bubble.getKey() : "null";
    }

    /* access modifiers changed from: package-private */
    public void setSurfaceZOrderedOnTop(boolean z) {
        SurfaceView surfaceView = this.mActivitySurface;
        if (surfaceView != null) {
            surfaceView.setZOrderedOnTop(z, true);
        }
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.ScreenshotGraphicBuffer snapshotActivitySurface() {
        SurfaceView surfaceView = this.mActivitySurface;
        if (surfaceView == null) {
            return null;
        }
        return SurfaceControl.captureLayers(surfaceView.getSurfaceControl(), new Rect(0, 0, this.mActivityView.getWidth(), this.mActivityView.getHeight()), 1.0f);
    }

    /* access modifiers changed from: package-private */
    public int[] getActivityViewLocationOnScreen() {
        ActivityView activityView = this.mActivityView;
        return activityView != null ? activityView.getLocationOnScreen() : new int[]{0, 0};
    }

    /* access modifiers changed from: package-private */
    public void setManageClickListener(View.OnClickListener onClickListener) {
        findViewById(C0015R$id.settings_button).setOnClickListener(onClickListener);
    }

    /* access modifiers changed from: package-private */
    public void updateObscuredTouchableRegion() {
        ActivityView activityView = this.mActivityView;
        if (activityView != null) {
            activityView.onLocationChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void applyThemeAttrs() {
        TypedArray obtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{16844145});
        this.mCornerRadius = (float) obtainStyledAttributes.getDimensionPixelSize(0, 0);
        obtainStyledAttributes.recycle();
        if (this.mActivityView != null && ScreenDecorationsUtils.supportsRoundedCornersOnWindows(((LinearLayout) this).mContext.getResources())) {
            this.mActivityView.setCornerRadius(this.mCornerRadius);
        }
        int i = getResources().getConfiguration().uiMode & 48;
        if (i == 16) {
            this.mPointerDrawable.setTint(getResources().getColor(C0011R$color.bubbles_light));
        } else if (i == 32) {
            this.mPointerDrawable.setTint(getResources().getColor(C0011R$color.bubbles_dark));
        }
        this.mPointerView.setBackground(this.mPointerDrawable);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyboardVisible = false;
        this.mNeedsNewHeight = false;
        ActivityView activityView = this.mActivityView;
        if (activityView == null) {
            return;
        }
        if (ViewRootImpl.sNewInsetsMode == 2) {
            setImeWindowToDisplay(0, 0);
        } else {
            activityView.setForwardedInsets(Insets.of(0, 0, 0, 0));
        }
    }

    /* access modifiers changed from: package-private */
    public void setContentVisibility(boolean z) {
        float f = z ? 1.0f : 0.0f;
        this.mPointerView.setAlpha(f);
        ActivityView activityView = this.mActivityView;
        if (activityView != null && f != activityView.getAlpha()) {
            this.mActivityView.setAlpha(f);
            this.mActivityView.bringToFront();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityView getActivityView() {
        return this.mActivityView;
    }

    /* access modifiers changed from: package-private */
    public int getTaskId() {
        return this.mTaskId;
    }

    /* access modifiers changed from: package-private */
    public void updateInsets(WindowInsets windowInsets) {
        if (usingActivityView()) {
            int max = Math.max((this.mActivityView.getLocationOnScreen()[1] + this.mActivityView.getHeight()) - (this.mDisplaySize.y - Math.max(windowInsets.getSystemWindowInsetBottom(), windowInsets.getDisplayCutout() != null ? windowInsets.getDisplayCutout().getSafeInsetBottom() : 0)), 0);
            if (ViewRootImpl.sNewInsetsMode == 2) {
                setImeWindowToDisplay(getWidth(), max);
            } else {
                this.mActivityView.setForwardedInsets(Insets.of(0, 0, 0, max));
            }
        }
    }

    private void setImeWindowToDisplay(int i, int i2) {
        if (getVirtualDisplayId() != -1) {
            if (i2 != 0 && i != 0) {
                Context createDisplayContext = ((LinearLayout) this).mContext.createDisplayContext(getVirtualDisplay().getDisplay());
                if (this.mVirtualDisplayWindowManager == null) {
                    this.mVirtualDisplayWindowManager = (WindowManager) createDisplayContext.getSystemService("window");
                }
                View view = this.mVirtualImeView;
                if (view == null) {
                    View view2 = new View(createDisplayContext);
                    this.mVirtualImeView = view2;
                    view2.setVisibility(0);
                    this.mVirtualDisplayWindowManager.addView(this.mVirtualImeView, getVirtualImeViewAttrs(i, i2));
                } else {
                    this.mVirtualDisplayWindowManager.updateViewLayout(view, getVirtualImeViewAttrs(i, i2));
                    this.mVirtualImeView.setVisibility(0);
                }
                this.mImeShowing = true;
            } else if (this.mImeShowing) {
                this.mVirtualImeView.setVisibility(8);
                this.mImeShowing = false;
            }
        }
    }

    private WindowManager.LayoutParams getVirtualImeViewAttrs(int i, int i2) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i, i2, 2024, 536, -2);
        layoutParams.gravity = 80;
        layoutParams.setTitle("ImeInsetsWindowWithoutContent");
        layoutParams.token = new Binder();
        layoutParams.providesInsetsTypes = new int[]{13};
        layoutParams.alpha = 0.0f;
        return layoutParams;
    }

    /* access modifiers changed from: package-private */
    public void setStackView(BubbleStackView bubbleStackView) {
        this.mStackView = bubbleStackView;
    }

    public void setOverflow(boolean z) {
        this.mIsOverflow = z;
        this.mPendingIntent = PendingIntent.getActivity(((LinearLayout) this).mContext, 0, new Intent(((LinearLayout) this).mContext, BubbleOverflowActivity.class), 134217728);
        this.mSettingsIcon.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public void update(Bubble bubble) {
        boolean z = this.mBubble == null || didBackingContentChange(bubble);
        if (z || (bubble != null && bubble.getKey().equals(this.mBubble.getKey()))) {
            this.mBubble = bubble;
            this.mSettingsIcon.setContentDescription(getResources().getString(C0021R$string.bubbles_settings_button_description, bubble.getAppName()));
            this.mSettingsIcon.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                /* class com.android.systemui.bubbles.BubbleExpandedView.AnonymousClass3 */

                public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                    BubbleExpandedView.this.mStackView.setupLocalMenu(accessibilityNodeInfo);
                }
            });
            if (z) {
                PendingIntent bubbleIntent = this.mBubble.getBubbleIntent();
                this.mPendingIntent = bubbleIntent;
                if (bubbleIntent != null || this.mBubble.hasMetadataShortcutId()) {
                    setContentVisibility(false);
                    this.mActivityView.setVisibility(0);
                }
            }
            applyThemeAttrs();
            return;
        }
        Log.w("Bubbles", "Trying to update entry with different key, new bubble: " + bubble.getKey() + " old bubble: " + bubble.getKey());
    }

    private boolean didBackingContentChange(Bubble bubble) {
        return (this.mBubble != null && this.mPendingIntent != null) != (bubble.getBubbleIntent() != null);
    }

    /* access modifiers changed from: package-private */
    public void populateExpandedView() {
        if (usingActivityView()) {
            this.mActivityView.setCallback(this.mStateCallback);
        } else {
            Log.e("Bubbles", "Cannot populate expanded view.");
        }
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
    public void updateHeight() {
        if (this.mExpandedViewContainerLocation != null && usingActivityView()) {
            float f = (float) this.mOverflowHeight;
            if (!this.mIsOverflow) {
                f = Math.max(this.mBubble.getDesiredHeight(((LinearLayout) this).mContext), (float) this.mMinHeight);
            }
            float max = Math.max(Math.min(f, (float) getMaxExpandedHeight()), (float) this.mMinHeight);
            ViewGroup.LayoutParams layoutParams = this.mActivityView.getLayoutParams();
            this.mNeedsNewHeight = ((float) layoutParams.height) != max;
            if (!this.mKeyboardVisible) {
                layoutParams.height = (int) max;
                this.mActivityView.setLayoutParams(layoutParams);
                this.mNeedsNewHeight = false;
            }
        }
    }

    private int getMaxExpandedHeight() {
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mDisplaySize);
        return ((((((this.mDisplaySize.y - this.mExpandedViewContainerLocation[1]) - getPaddingTop()) - getPaddingBottom()) - this.mSettingsIconHeight) - this.mPointerHeight) - this.mPointerMargin) - (getRootWindowInsets() != null ? getRootWindowInsets().getStableInsetBottom() : 0);
    }

    public void updateView(int[] iArr) {
        this.mExpandedViewContainerLocation = iArr;
        if (usingActivityView() && this.mActivityView.getVisibility() == 0 && this.mActivityView.isAttachedToWindow()) {
            this.mActivityView.onLocationChanged();
            updateHeight();
        }
    }

    public void setPointerPosition(float f) {
        this.mPointerView.setTranslationX((f - (((float) this.mPointerWidth) / 2.0f)) - ((float) this.mExpandedViewPadding));
        this.mPointerView.setVisibility(0);
    }

    public void getManageButtonBoundsOnScreen(Rect rect) {
        this.mSettingsIcon.getBoundsOnScreen(rect);
    }

    public void cleanUpExpandedState() {
        ActivityView activityView = this.mActivityView;
        if (activityView != null) {
            activityView.release();
            if (this.mTaskId != -1) {
                try {
                    ActivityTaskManager.getService().removeTask(this.mTaskId);
                } catch (RemoteException unused) {
                    Log.w("Bubbles", "Failed to remove taskId " + this.mTaskId);
                }
                this.mTaskId = -1;
            }
            removeView(this.mActivityView);
            this.mActivityView = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyDisplayEmpty() {
        if (this.mActivityViewStatus == ActivityViewStatus.ACTIVITY_STARTED) {
            this.mActivityViewStatus = ActivityViewStatus.INITIALIZED;
        }
    }

    private boolean usingActivityView() {
        return (this.mPendingIntent != null || this.mBubble.hasMetadataShortcutId()) && this.mActivityView != null;
    }

    public int getVirtualDisplayId() {
        if (usingActivityView()) {
            return this.mActivityView.getVirtualDisplayId();
        }
        return -1;
    }

    private VirtualDisplay getVirtualDisplay() {
        if (usingActivityView()) {
            return this.mActivityView.getVirtualDisplay();
        }
        return null;
    }
}
