package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.core.view.GestureDetectorCompat;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedSplitter;
import com.android.systemui.statusbar.views.NetworkSpeedView;

public class MiuiPhoneStatusBarView extends PhoneStatusBarView {
    private ControlPanelWindowManager mControlPanelWindowManager;
    protected int mCurrentStatusBarType = 0;
    private MotionEvent mDown;
    private float mDownY;
    protected NetworkSpeedSplitter mDripNetworkSpeedSplitter;
    protected NetworkSpeedView mDripNetworkSpeedView;
    protected View mDripStatusBarLeftStatusIconArea;
    protected View mDripStatusBarNotificationIconArea;
    protected View mDripStatusBarRightStatusIcons;
    private boolean mFirstMove;
    protected NetworkSpeedView mFullScreenNetworkSpeedView;
    protected View mFullscreenStatusBarNotificationIconArea;
    protected View mFullscreenStatusBarStatusIcons;
    private GestureDetectorCompat mGestureDetector;
    private boolean mIsGiveAllEvent;
    protected View mNotificationIconAreaInner;
    protected PhoneStatusBarTintController mPhoneStatusBarTintController = new PhoneStatusBarTintController(this, (MiuiLightBarController) Dependency.get(LightBarController.class));
    protected View mStatusBarLeftContainer;
    protected View mSystemIconArea;

    public MiuiPhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mGestureDetector = new GestureDetectorCompat(context, new MyGestureListener(this));
        this.mControlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
        addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) {
            /* class com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView.AnonymousClass1 */

            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i != i7 - i5) {
                    ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).reapply();
                }
            }
        });
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarView, com.android.systemui.statusbar.phone.PanelBar
    public void onFinishInflate() {
        this.mStatusBarLeftContainer = findViewById(C0015R$id.phone_status_bar_left_container);
        this.mDripStatusBarNotificationIconArea = findViewById(C0015R$id.drip_notification_icon_area);
        this.mDripStatusBarLeftStatusIconArea = findViewById(C0015R$id.drip_left_status_icon_area);
        this.mFullscreenStatusBarNotificationIconArea = findViewById(C0015R$id.fullscreen_notification_icon_area);
        this.mFullscreenStatusBarStatusIcons = findViewById(C0015R$id.statusIcons);
        this.mDripStatusBarRightStatusIcons = findViewById(C0015R$id.drip_right_statusIcons);
        this.mSystemIconArea = findViewById(C0015R$id.system_icon_area);
        this.mDripNetworkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.drip_network_speed_view);
        this.mDripNetworkSpeedSplitter = (NetworkSpeedSplitter) findViewById(C0015R$id.drip_network_speed_splitter);
        this.mFullScreenNetworkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.fullscreen_network_speed_view);
        this.mDripNetworkSpeedView.addVisibilityListener(this.mDripNetworkSpeedSplitter);
        ((MiuiClock) findViewById(C0015R$id.clock)).addVisibilityListener(this.mDripNetworkSpeedSplitter);
        super.onFinishInflate();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarView
    public void onAttachedToWindow() {
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mDripNetworkSpeedView);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mDripNetworkSpeedSplitter);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mFullScreenNetworkSpeedView);
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).addPromptContainer((FrameLayout) findViewById(C0015R$id.prompt_container), 0);
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarView
    public void onDetachedFromWindow() {
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).removePromptContainer((FrameLayout) findViewById(C0015R$id.prompt_container));
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mDripNetworkSpeedView);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mDripNetworkSpeedSplitter);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mFullScreenNetworkSpeedView);
        super.onDetachedFromWindow();
    }

    public void setStatusBarType(int i) {
        if (this.mCurrentStatusBarType != i) {
            this.mCurrentStatusBarType = i;
            updateNotificationIconAreaInnnerParent();
        }
    }

    public void setNotificationIconAreaInnner(View view) {
        View view2 = this.mNotificationIconAreaInner;
        if (view2 != null) {
            ((ViewGroup) view2.getParent()).removeView(this.mNotificationIconAreaInner);
        }
        this.mNotificationIconAreaInner = view;
        updateNotificationIconAreaInnnerParent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarView
    public void updateCutoutLocation(Pair<Integer, Integer> pair) {
        if (this.mCutoutSpace != null) {
            DisplayCutout displayCutout = this.mDisplayCutout;
            if (displayCutout == null || displayCutout.isEmpty() || pair != null) {
                setStatusBarType(0);
                this.mCutoutSpace.setVisibility(8);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mStatusBarLeftContainer.getLayoutParams();
                layoutParams.width = -2;
                layoutParams.weight = 0.0f;
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mSystemIconArea.getLayoutParams();
                layoutParams2.width = -2;
                layoutParams2.weight = 0.0f;
                this.mFullscreenStatusBarNotificationIconArea.setVisibility(0);
                this.mFullscreenStatusBarStatusIcons.setVisibility(0);
                this.mFullScreenNetworkSpeedView.setVisibilityByStatusBar(true);
                this.mDripNetworkSpeedView.setVisibilityByStatusBar(false);
                this.mDripStatusBarLeftStatusIconArea.setVisibility(8);
                this.mDripStatusBarNotificationIconArea.setVisibility(8);
                this.mDripStatusBarRightStatusIcons.setVisibility(8);
                return;
            }
            setStatusBarType(1);
            this.mCutoutSpace.setVisibility(0);
            LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mCutoutSpace.getLayoutParams();
            Rect rect = new Rect();
            ScreenDecorations.DisplayCutoutView.boundsFromDirection(this.mDisplayCutout, 48, rect);
            int i = rect.left;
            int i2 = this.mCutoutSideNudge;
            rect.left = i + i2;
            rect.right -= i2;
            layoutParams3.width = rect.width();
            layoutParams3.height = rect.height();
            LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mStatusBarLeftContainer.getLayoutParams();
            layoutParams4.width = 0;
            layoutParams4.weight = 1.0f;
            LinearLayout.LayoutParams layoutParams5 = (LinearLayout.LayoutParams) this.mSystemIconArea.getLayoutParams();
            layoutParams5.width = 0;
            layoutParams5.weight = 1.0f;
            this.mFullscreenStatusBarNotificationIconArea.setVisibility(8);
            this.mFullscreenStatusBarStatusIcons.setVisibility(8);
            this.mFullScreenNetworkSpeedView.setVisibilityByStatusBar(false);
            this.mDripNetworkSpeedView.setVisibilityByStatusBar(true);
            this.mDripStatusBarLeftStatusIconArea.setVisibility(0);
            this.mDripStatusBarNotificationIconArea.setVisibility(0);
            this.mDripStatusBarRightStatusIcons.setVisibility(0);
        }
    }

    public void updateNotificationIconAreaInnnerParent() {
        ViewGroup viewGroup;
        if (this.mNotificationIconAreaInner != null) {
            if (this.mCurrentStatusBarType == 0) {
                viewGroup = (ViewGroup) findViewById(C0015R$id.fullscreen_notification_icon_area);
            } else {
                viewGroup = (ViewGroup) findViewById(C0015R$id.drip_notification_icon_area);
            }
            if (this.mNotificationIconAreaInner.getParent() != null) {
                ((ViewGroup) this.mNotificationIconAreaInner.getParent()).removeView(this.mNotificationIconAreaInner);
            }
            viewGroup.addView(this.mNotificationIconAreaInner);
        }
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        this.mPhoneStatusBarTintController.onDraw();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarView
    public boolean handleEvent(MotionEvent motionEvent) {
        boolean z = motionEvent.getActionMasked() == 0;
        boolean z2 = motionEvent.getAction() == 2;
        boolean z3 = motionEvent.getActionMasked() == 1;
        boolean z4 = motionEvent.getAction() == 3;
        if (z) {
            this.mDownY = motionEvent.getRawY();
            MotionEvent motionEvent2 = this.mDown;
            if (motionEvent2 != null) {
                motionEvent2.recycle();
                this.mDown = null;
            }
            this.mDown = MotionEvent.obtain(motionEvent);
            this.mIsGiveAllEvent = false;
            this.mFirstMove = true;
        } else if (z2 && Math.abs(motionEvent.getRawY() - this.mDownY) > 5.0f && this.mFirstMove) {
            this.mControlPanelWindowManager.dispatchToControlPanel(this.mDown, (float) getWidth());
            this.mFirstMove = false;
            this.mIsGiveAllEvent = true;
        } else if (z3 || z4) {
            this.mControlPanelWindowManager.dispatchToControlPanel(motionEvent, (float) getWidth());
            this.mFirstMove = false;
            this.mIsGiveAllEvent = false;
            MotionEvent motionEvent3 = this.mDown;
            if (motionEvent3 != null) {
                motionEvent3.recycle();
                this.mDown = null;
            }
        }
        return (this.mIsGiveAllEvent && this.mControlPanelWindowManager.dispatchToControlPanel(motionEvent, (float) getWidth())) || this.mGestureDetector.onTouchEvent(motionEvent) || !panelEnabled();
    }

    static class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        public boolean onDoubleTap(MotionEvent motionEvent) {
            return false;
        }

        public MyGestureListener(MiuiPhoneStatusBarView miuiPhoneStatusBarView) {
        }
    }
}
