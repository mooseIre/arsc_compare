package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.plugins.R;

public class PhoneStatusBarView extends PanelBar implements MiuiStatusBarPromptController.OnPromptStateChangedListener {
    private static final boolean DEBUG = StatusBar.DEBUG;
    StatusBar mBar;
    private final PhoneStatusBarTransitions mBarTransitions = new PhoneStatusBarTransitions(this);
    private boolean mBlockClickActionToStatusBar;
    private long mDownTime;
    private float mDownX;
    private float mDownY;
    private Runnable mHideExpandedRunnable = new Runnable() {
        public void run() {
            if (PhoneStatusBarView.this.mPanel.isFullyCollapsed()) {
                PhoneStatusBarView.this.mBar.makeExpandedInvisible();
            }
        }
    };
    boolean mIsFullyOpenedPanel = false;
    private float mMinFraction;
    private MiuiStatusBarPromptController mMiuiStatusBarPromptController = ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class));
    private boolean mPanelClosedOnDown;
    private float mPanelFraction;
    private int mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private ScrimController mScrimController;
    private String mTag;
    private PhoneStatusBarTintController mTintController = new PhoneStatusBarTintController(this);

    public PhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public void setBar(StatusBar statusBar) {
        this.mBar = statusBar;
    }

    public void setPrompt(String str) {
        this.mTag = "PhoneStatusBarView" + str;
        this.mMiuiStatusBarPromptController.addStatusBarPrompt(this.mTag, this.mBar, this, 0, this);
    }

    public void clearPrompt() {
        this.mMiuiStatusBarPromptController.removePrompt(this.mTag);
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
    }

    public void initBarTransitions() {
        this.mBarTransitions.init();
    }

    public boolean panelEnabled() {
        StatusBar statusBar = this.mBar;
        return statusBar != null ? statusBar.panelsEnabled() : super.panelEnabled();
    }

    public boolean onRequestSendAccessibilityEventInternal(View view, AccessibilityEvent accessibilityEvent) {
        if (!super.onRequestSendAccessibilityEventInternal(view, accessibilityEvent)) {
            return false;
        }
        AccessibilityEvent obtain = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(obtain);
        dispatchPopulateAccessibilityEvent(obtain);
        accessibilityEvent.appendRecord(obtain);
        return true;
    }

    public void onPanelPeeked() {
        super.onPanelPeeked();
        this.mBar.makeExpandedVisible(false);
    }

    public void onPanelCollapsed() {
        super.onPanelCollapsed();
        post(this.mHideExpandedRunnable);
        this.mIsFullyOpenedPanel = false;
    }

    public void removePendingHideExpandedRunnables() {
        removeCallbacks(this.mHideExpandedRunnable);
    }

    public void onPanelFullyOpened() {
        super.onPanelFullyOpened();
        if (!this.mIsFullyOpenedPanel) {
            this.mPanel.sendAccessibilityEvent(32);
        }
        this.mIsFullyOpenedPanel = true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mBar == null || this.mPanel == null) {
            return super.onTouchEvent(motionEvent);
        }
        processMiuiPromptClick(motionEvent);
        return this.mBar.interceptTouchEvent(motionEvent) || super.onTouchEvent(motionEvent) || this.mBlockClickActionToStatusBar;
    }

    public void onTrackingStarted() {
        super.onTrackingStarted();
        this.mBar.onTrackingStarted();
        this.mScrimController.onTrackingStarted();
        removePendingHideExpandedRunnables();
    }

    public void onClosingFinished() {
        super.onClosingFinished();
        this.mBar.onClosingFinished();
    }

    public void onTrackingStopped(boolean z) {
        super.onTrackingStopped(z);
        this.mBar.onTrackingStopped(z);
    }

    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mScrimController.onExpandingFinished();
        this.mBar.onExpandingFinished();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        StatusBar statusBar = this.mBar;
        return (statusBar != null && statusBar.interceptTouchEvent(motionEvent)) || super.onInterceptTouchEvent(motionEvent);
    }

    public void processMiuiPromptClick(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        boolean z = true;
        if (motionEvent.getActionMasked() == 0) {
            if (this.mPanel.isKeyguardShowing() || !this.mMiuiStatusBarPromptController.blockClickAction()) {
                z = false;
            }
            this.mBlockClickActionToStatusBar = z;
            this.mPanelClosedOnDown = this.mPanel.isFullyCollapsed();
            this.mDownTime = SystemClock.uptimeMillis();
            this.mDownX = x;
            this.mDownY = y;
        } else if (this.mMiuiStatusBarPromptController.getTouchRegion(this.mTag).contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY()) && motionEvent.getActionMasked() == 1 && this.mPanelClosedOnDown && !this.mPanel.isPanelVisibleBecauseOfHeadsUp() && !this.mPanel.isTracking()) {
            float scaledTouchSlop = (float) ViewConfiguration.get(getContext()).getScaledTouchSlop();
            if (SystemClock.uptimeMillis() - this.mDownTime < ((long) ViewConfiguration.getLongPressTimeout()) && Math.abs(x - this.mDownX) < scaledTouchSlop && Math.abs(y - this.mDownY) < scaledTouchSlop && this.mBlockClickActionToStatusBar) {
                this.mPanel.cancelPeek();
                this.mMiuiStatusBarPromptController.handleClickAction();
            }
        }
    }

    public void panelScrimMinFractionChanged(float f) {
        if (this.mMinFraction != f) {
            this.mMinFraction = f;
            updateScrimFraction();
        }
    }

    public void panelExpansionChanged(float f, boolean z) {
        super.panelExpansionChanged(f, z);
        this.mPanelFraction = f;
        updateScrimFraction();
    }

    private void updateScrimFraction() {
        if (this.mBar.getBarState() != 2) {
            float f = this.mPanelFraction;
            float f2 = this.mMinFraction;
            if (f2 < 1.0f) {
                f = Math.max((f - f2) / (1.0f - f2), 0.0f);
            }
            this.mScrimController.setPanelExpansion(f);
        }
    }

    public void onDensityOrFontScaleChanged() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        setLayoutParams(layoutParams);
    }

    public void onPromptStateChanged(boolean z, String str) {
        this.mBar.refreshClockVisibility(z);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        this.mTintController.onDraw();
    }
}
