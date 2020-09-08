package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.session.MediaSessionLegacyHelperCompat;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowCompat;
import android.view.WindowInsets;
import android.view.WindowInsetsCompat;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.view.FloatingActionModeCompat;
import com.android.internal.widget.FloatingToolbar;
import com.android.internal.widget.FloatingToolbarCompat;
import com.android.systemui.Dependency;
import com.android.systemui.DynamicStatusController;
import com.android.systemui.R$styleable;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowView;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;

public class StatusBarWindowView extends FrameLayout {
    public static final boolean DEBUG = StatusBar.DEBUG;
    private View mBrightnessMirror;
    public ControlPanelWindowView mControllerPanel;
    private DoubleTapHelper mDoubleTapHelper;
    private DragDownHelper mDragDownHelper;
    private boolean mExpandAnimationPending;
    private boolean mExpandAnimationRunning;
    private boolean mExpandingBelowNotch;
    private Window mFakeWindow = new WindowCompat(this.mContext) {
        public View getDecorView() {
            return StatusBarWindowView.this;
        }
    };
    private FalsingManager mFalsingManager;
    /* access modifiers changed from: private */
    public ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private FloatingToolbar mFloatingToolbar;
    private ViewTreeObserver.OnPreDrawListener mFloatingToolbarPreDrawListener;
    private KeyguardStatusBarView mKeyguardStatusBarView;
    private int mLeftInset = 0;
    private boolean mNotTouchable = false;
    private NotificationPanelView mNotificationPanel;
    private boolean mPassingToControllerPanel;
    private int mRightInset = 0;
    /* access modifiers changed from: private */
    public StatusBar mService;
    private NotificationStackScrollLayout mStackScrollLayout;
    private FrameLayout mStatusBarContainer;
    private PhoneStatusBarView mStatusBarView;
    private int mTopInset = 0;
    private boolean mTouchActive;
    private boolean mTouchCancelled;
    private final Paint mTransparentSrcPaint = new Paint();

    public StatusBarWindowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setMotionEventSplittingEnabled(false);
        this.mTransparentSrcPaint.setColor(0);
        this.mTransparentSrcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mDoubleTapHelper = new DoubleTapHelper(this, new DoubleTapHelper.ActivationListener() {
            public void onActiveChanged(boolean z) {
            }
        }, new DoubleTapHelper.DoubleTapListener() {
            public boolean onDoubleTap() {
                StatusBarWindowView.this.mService.wakeUpIfDozing(SystemClock.uptimeMillis(), StatusBarWindowView.this, "DOUBLE_TAP");
                return true;
            }
        }, (DoubleTapHelper.SlideBackListener) null, (DoubleTapHelper.DoubleTapLogListener) null);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT <= 29) {
            return super.onApplyWindowInsets(windowInsets);
        }
        Insets insetsIgnoringVisibility = WindowInsetsCompat.getInsetsIgnoringVisibility(windowInsets);
        this.mLeftInset = insetsIgnoringVisibility.left;
        this.mRightInset = insetsIgnoringVisibility.right;
        this.mTopInset = 0;
        DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();
        if (displayCutout != null) {
            this.mTopInset = WindowInsetsCompat.getWaterfallInsetsTop(displayCutout);
        }
        applyMargins();
        return windowInsets;
    }

    /* access modifiers changed from: protected */
    public boolean fitSystemWindows(Rect rect) {
        if (Build.VERSION.SDK_INT > 29) {
            return super.fitSystemWindows(rect);
        }
        boolean z = true;
        if (getFitsSystemWindows()) {
            if (rect.top == getPaddingTop() && rect.bottom == getPaddingBottom()) {
                z = false;
            }
            if (!(rect.right == this.mRightInset && rect.left == this.mLeftInset)) {
                this.mRightInset = rect.right;
                this.mLeftInset = rect.left;
                applyMargins();
            }
            if (z) {
                setPadding(0, 0, 0, 0);
            }
            rect.left = 0;
            rect.top = 0;
            rect.right = 0;
        } else {
            if (!(this.mRightInset == 0 && this.mLeftInset == 0)) {
                this.mRightInset = 0;
                this.mLeftInset = 0;
                applyMargins();
            }
            if (getPaddingLeft() == 0 && getPaddingRight() == 0 && getPaddingTop() == 0 && getPaddingBottom() == 0) {
                z = false;
            }
            if (z) {
                setPadding(0, 0, 0, 0);
            }
            rect.top = 0;
        }
        return false;
    }

    private void applyMargins() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if ((childAt.getLayoutParams() instanceof LayoutParams) && childAt.getId() != R.id.brightness_mirror) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (!layoutParams.ignoreRightInset && !(layoutParams.rightMargin == this.mRightInset && layoutParams.leftMargin == this.mLeftInset && layoutParams.topMargin == this.mTopInset)) {
                    layoutParams.rightMargin = this.mRightInset;
                    layoutParams.leftMargin = this.mLeftInset;
                    layoutParams.topMargin = this.mTopInset;
                    childAt.requestLayout();
                }
            }
        }
    }

    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* access modifiers changed from: protected */
    public FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mStackScrollLayout = (NotificationStackScrollLayout) findViewById(R.id.notification_stack_scroller);
        this.mNotificationPanel = (NotificationPanelView) findViewById(R.id.notification_panel);
        this.mStatusBarContainer = (FrameLayout) findViewById(R.id.status_bar_container);
        this.mBrightnessMirror = findViewById(R.id.brightness_mirror);
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        if (view.getId() == R.id.brightness_mirror) {
            this.mBrightnessMirror = view;
        }
    }

    public void setStatusBarView(PhoneStatusBarView phoneStatusBarView) {
        this.mStatusBarView = phoneStatusBarView;
    }

    public void setKeyguardStatusBarView(KeyguardStatusBarView keyguardStatusBarView) {
        this.mKeyguardStatusBarView = keyguardStatusBarView;
    }

    public void setService(StatusBar statusBar) {
        this.mService = statusBar;
        setDragDownHelper(new DragDownHelper(getContext(), this, this.mStackScrollLayout, this.mService));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setDragDownHelper(DragDownHelper dragDownHelper) {
        this.mDragDownHelper = dragDownHelper;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mService.isScrimSrcModeEnabled()) {
            IBinder windowToken = getWindowToken();
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
            layoutParams.token = windowToken;
            setLayoutParams(layoutParams);
            WindowManagerGlobal.getInstance().changeCanvasOpacity(windowToken, true);
            setWillNotDraw(false);
            return;
        }
        setWillNotDraw(!DEBUG);
    }

    public void onDescendantInvalidated(View view, View view2) {
        super.onDescendantInvalidated(view, view2);
        if (((DynamicStatusController) Dependency.get(DynamicStatusController.class)).isDebug()) {
            Log.d("StatusBarWindowView", "onDescendantInvalidated  child=" + view + ";target=" + view2);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.mService.interceptMediaKey(keyEvent) || super.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        boolean z = keyEvent.getAction() == 0;
        int keyCode = keyEvent.getKeyCode();
        if (keyCode != 4) {
            if (keyCode != 62) {
                if (keyCode != 82) {
                    if ((keyCode == 24 || keyCode == 25) && this.mService.isDozing()) {
                        MediaSessionLegacyHelperCompat.sendVolumeKeyEvent(getContext(), keyEvent, Integer.MIN_VALUE, true);
                        return true;
                    }
                    return false;
                } else if (!z) {
                    return this.mService.onMenuPressed();
                }
            }
            if (!z) {
                return this.mService.onSpacePressed();
            }
            return false;
        }
        if (!z) {
            this.mService.onBackPressed();
        }
        return true;
    }

    public void setControlPanel(ControlPanelWindowView controlPanelWindowView) {
        this.mControllerPanel = controlPanelWindowView;
        if (controlPanelWindowView == null) {
            this.mPassingToControllerPanel = false;
        }
    }

    public void setNotTouchable(boolean z) {
        this.mNotTouchable = z;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        PhoneStatusBarView phoneStatusBarView;
        KeyguardStatusBarView keyguardStatusBarView;
        if (this.mNotTouchable) {
            return false;
        }
        boolean z = true;
        boolean z2 = motionEvent.getActionMasked() == 0;
        boolean z3 = motionEvent.getActionMasked() == 1;
        boolean z4 = motionEvent.getActionMasked() == 3;
        if (this.mService.isBouncerShowing()) {
            this.mPassingToControllerPanel = false;
        } else if (this.mPassingToControllerPanel) {
            this.mControllerPanel.dispatchTouchEvent(motionEvent);
            if (z4 || z3) {
                this.mPassingToControllerPanel = false;
            }
            this.mStatusBarView.processMiuiPromptClick(motionEvent);
            return true;
        } else if (z2 && this.mControllerPanel != null) {
            View view = null;
            if ((this.mService.getBarState() == 1 || this.mService.getBarState() == 2) && (keyguardStatusBarView = this.mKeyguardStatusBarView) != null && keyguardStatusBarView.getVisibility() == 0) {
                view = this.mKeyguardStatusBarView;
            } else if (this.mService.getBarState() == 0 && (phoneStatusBarView = this.mStatusBarView) != null && phoneStatusBarView.getVisibility() == 0) {
                view = this.mStatusBarView;
            }
            if (view != null && view.pointInView(motionEvent.getX(), motionEvent.getY(), 0.0f) && motionEvent.getX() >= ((float) view.getWidth()) / 2.0f && !this.mService.isHeadsUp() && this.mControllerPanel.dispatchTouchEvent(motionEvent)) {
                this.mPassingToControllerPanel = true;
                this.mStatusBarView.processMiuiPromptClick(motionEvent);
                return true;
            }
        }
        boolean z5 = this.mExpandingBelowNotch;
        if (z3 || z4) {
            this.mExpandingBelowNotch = false;
        }
        if (z2 && this.mNotificationPanel.isFullyCollapsed()) {
            this.mNotificationPanel.startExpandLatencyTracking();
        }
        if (z2) {
            this.mTouchActive = true;
            this.mTouchCancelled = false;
        } else if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
            this.mTouchActive = false;
        } else if (motionEvent.getActionMasked() == 5 && this.mService.getBarState() == 1 && this.mNotificationPanel.mIsDefaultTheme) {
            cancelCurrentTouch();
        }
        if (this.mTouchCancelled || this.mExpandAnimationRunning || this.mExpandAnimationPending) {
            return false;
        }
        this.mFalsingManager.onTouchEvent(motionEvent, getWidth(), getHeight());
        View view2 = this.mBrightnessMirror;
        if (view2 != null && view2.getVisibility() == 0 && motionEvent.getActionMasked() == 5) {
            return false;
        }
        if (z2) {
            this.mStackScrollLayout.closeControlsIfOutsideTouch(motionEvent);
        }
        if (this.mService.isDozing()) {
            this.mService.mDozeScrimController.extendPulse();
        }
        if (!z2 || motionEvent.getY() < ((float) this.mBottom)) {
            z = z5;
        } else {
            this.mExpandingBelowNotch = true;
        }
        if (z) {
            return this.mStatusBarView.dispatchTouchEvent(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mService.isDozing() && !this.mStackScrollLayout.hasPulsingNotifications()) {
            return true;
        }
        boolean z = false;
        if (this.mNotificationPanel.isFullyExpanded() && this.mNotificationPanel.isInCenterScreen() && this.mStackScrollLayout.getVisibility() == 0 && this.mService.getBarState() == 1 && !this.mService.isBouncerShowing() && !this.mService.isDozing()) {
            z = this.mDragDownHelper.onInterceptTouchEvent(motionEvent);
        }
        if (!z) {
            super.onInterceptTouchEvent(motionEvent);
        }
        if (z) {
            MotionEvent obtain = MotionEvent.obtain(motionEvent);
            obtain.setAction(3);
            this.mStackScrollLayout.onInterceptTouchEvent(obtain);
            this.mNotificationPanel.onInterceptTouchEvent(obtain);
            obtain.recycle();
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (this.mService.isDozing()) {
            this.mDoubleTapHelper.onTouchEvent(motionEvent);
            z = true;
        } else {
            z = false;
        }
        if ((this.mService.getBarState() == 1 && !z) || this.mDragDownHelper.isDraggingDown()) {
            z = this.mDragDownHelper.onTouchEvent(motionEvent);
        }
        if (!z) {
            z = super.onTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (!z && (action == 1 || action == 3)) {
            this.mService.setInteracting(1, false);
        }
        return z;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mService.isScrimSrcModeEnabled()) {
            int height = getHeight() - getPaddingBottom();
            int width = getWidth() - getPaddingRight();
            if (getPaddingTop() != 0) {
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getPaddingTop(), this.mTransparentSrcPaint);
            }
            if (getPaddingBottom() != 0) {
                canvas.drawRect(0.0f, (float) height, (float) getWidth(), (float) getHeight(), this.mTransparentSrcPaint);
            }
            if (getPaddingLeft() != 0) {
                canvas.drawRect(0.0f, (float) getPaddingTop(), (float) getPaddingLeft(), (float) height, this.mTransparentSrcPaint);
            }
            if (getPaddingRight() != 0) {
                canvas.drawRect((float) width, (float) getPaddingTop(), (float) getWidth(), (float) height, this.mTransparentSrcPaint);
            }
        }
        if (DEBUG) {
            Paint paint = new Paint();
            paint.setColor(-2130706688);
            paint.setStrokeWidth(12.0f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), paint);
        }
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        boolean useControlPanel = ((ControlPanelController) Dependency.get(ControlPanelController.class)).useControlPanel();
        boolean z = !this.mNotificationPanel.isFullyCollapsed();
        boolean isThemeBgVisible = this.mNotificationPanel.isThemeBgVisible();
        if (useControlPanel && z && isThemeBgVisible) {
            FrameLayout frameLayout = this.mStatusBarContainer;
            if (view == frameLayout) {
                return super.drawChild(canvas, this.mNotificationPanel, j);
            }
            if (view == this.mNotificationPanel) {
                return super.drawChild(canvas, frameLayout, j);
            }
        }
        return super.drawChild(canvas, view, j);
    }

    public void cancelExpandHelper() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScrollLayout;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.cancelExpandHelper();
        }
    }

    public void cancelCurrentTouch() {
        if (this.mTouchActive) {
            long uptimeMillis = SystemClock.uptimeMillis();
            MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
            obtain.setSource(4098);
            dispatchTouchEvent(obtain);
            obtain.recycle();
            this.mTouchCancelled = true;
        }
    }

    public void setExpandAnimationRunning(boolean z) {
        this.mExpandAnimationRunning = z;
    }

    public void setExpandAnimationPending(boolean z) {
        this.mExpandAnimationPending = z;
    }

    public class LayoutParams extends FrameLayout.LayoutParams {
        public boolean ignoreRightInset;

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.StatusBarWindowView_Layout);
            this.ignoreRightInset = obtainStyledAttributes.getBoolean(0, false);
            obtainStyledAttributes.recycle();
        }
    }

    public ActionMode startActionModeForChild(View view, ActionMode.Callback callback, int i) {
        if (i == 1) {
            return startActionMode(view, callback, i);
        }
        return super.startActionModeForChild(view, callback, i);
    }

    private ActionMode createFloatingActionMode(View view, ActionMode.Callback2 callback2) {
        ActionMode actionMode = this.mFloatingActionMode;
        if (actionMode != null) {
            actionMode.finish();
        }
        cleanupFloatingActionModeViews();
        FloatingToolbar newFloatingToolbar = FloatingToolbarCompat.newFloatingToolbar(this.mContext, this.mFakeWindow);
        this.mFloatingToolbar = newFloatingToolbar;
        final FloatingActionMode newFloatingActionMode = FloatingActionModeCompat.newFloatingActionMode(this.mContext, callback2, view, newFloatingToolbar);
        this.mFloatingActionModeOriginatingView = view;
        this.mFloatingToolbarPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                newFloatingActionMode.updateViewLocationInWindow();
                return true;
            }
        };
        return newFloatingActionMode;
    }

    private void setHandledFloatingActionMode(ActionMode actionMode) {
        this.mFloatingActionMode = actionMode;
        actionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    /* access modifiers changed from: private */
    public void cleanupFloatingActionModeViews() {
        FloatingToolbar floatingToolbar = this.mFloatingToolbar;
        if (floatingToolbar != null) {
            floatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        View view = this.mFloatingActionModeOriginatingView;
        if (view != null) {
            if (this.mFloatingToolbarPreDrawListener != null) {
                view.getViewTreeObserver().removeOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
                this.mFloatingToolbarPreDrawListener = null;
            }
            this.mFloatingActionModeOriginatingView = null;
        }
    }

    private ActionMode startActionMode(View view, ActionMode.Callback callback, int i) {
        ActionModeCallback2Wrapper actionModeCallback2Wrapper = new ActionModeCallback2Wrapper(callback);
        ActionMode createFloatingActionMode = createFloatingActionMode(view, actionModeCallback2Wrapper);
        if (createFloatingActionMode == null || !actionModeCallback2Wrapper.onCreateActionMode(createFloatingActionMode, createFloatingActionMode.getMenu())) {
            return null;
        }
        setHandledFloatingActionMode(createFloatingActionMode);
        return createFloatingActionMode;
    }

    private class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;

        public ActionModeCallback2Wrapper(ActionMode.Callback callback) {
            this.mWrapped = callback;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onCreateActionMode(actionMode, menu);
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            StatusBarWindowView.this.requestFitSystemWindows();
            return this.mWrapped.onPrepareActionMode(actionMode, menu);
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return this.mWrapped.onActionItemClicked(actionMode, menuItem);
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            this.mWrapped.onDestroyActionMode(actionMode);
            if (actionMode == StatusBarWindowView.this.mFloatingActionMode) {
                StatusBarWindowView.this.cleanupFloatingActionModeViews();
                ActionMode unused = StatusBarWindowView.this.mFloatingActionMode = null;
            }
            StatusBarWindowView.this.requestFitSystemWindows();
        }

        public void onGetContentRect(ActionMode actionMode, View view, Rect rect) {
            ActionMode.Callback callback = this.mWrapped;
            if (callback instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) callback).onGetContentRect(actionMode, view, rect);
            } else {
                super.onGetContentRect(actionMode, view, rect);
            }
        }
    }
}
