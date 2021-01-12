package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedSplitter;
import com.android.systemui.statusbar.views.NetworkSpeedView;

public class MiuiPhoneStatusBarView extends PhoneStatusBarView {
    protected int mCurrentStatusBarType = 0;
    protected NetworkSpeedSplitter mDripNetworkSpeedSplitter;
    protected NetworkSpeedView mDripNetworkSpeedView;
    protected View mDripStatusBarLeftStatusIconArea;
    protected View mDripStatusBarNotificationIconArea;
    protected View mDripStatusBarRightStatusIcons;
    protected NetworkSpeedView mFullScreenNetworkSpeedView;
    protected View mFullscreenStatusBarNotificationIconArea;
    protected View mFullscreenStatusBarStatusIcons;
    protected View mNotificationIconAreaInner;
    protected PhoneStatusBarTintController mPhoneStatusBarTintController = new PhoneStatusBarTintController(this, (MiuiLightBarController) Dependency.get(LightBarController.class));
    protected View mStatusBarLeftContainer;
    protected View mSystemIconArea;

    public MiuiPhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i != i7 - i5) {
                    ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).reapply();
                }
            }
        });
    }

    public void initMiuiViews() {
        super.initMiuiViews();
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
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).addPromptContainer((FrameLayout) findViewById(C0015R$id.prompt_container), 0);
    }

    public void onMiuiAttachedToWindow() {
        Class cls = DarkIconDispatcher.class;
        super.onMiuiAttachedToWindow();
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mDripNetworkSpeedView);
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mDripNetworkSpeedSplitter);
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mFullScreenNetworkSpeedView);
    }

    public void onMiuiDetachedToWindow() {
        Class cls = DarkIconDispatcher.class;
        super.onMiuiDetachedToWindow();
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mDripNetworkSpeedView);
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mDripNetworkSpeedSplitter);
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mFullScreenNetworkSpeedView);
    }

    public void setStatusBarType(int i) {
        super.setStatusBarType(i);
        if (this.mCurrentStatusBarType != i) {
            this.mCurrentStatusBarType = i;
            updateNotificationIconAreaInnnerParent();
        }
    }

    public void setNotificationIconAreaInnner(View view) {
        super.setNotificationIconAreaInnner(view);
        View view2 = this.mNotificationIconAreaInner;
        if (view2 != null) {
            ((ViewGroup) view2.getParent()).removeView(this.mNotificationIconAreaInner);
        }
        this.mNotificationIconAreaInner = view;
        updateNotificationIconAreaInnnerParent();
    }

    /* access modifiers changed from: protected */
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

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).dispatchToControlPanel(motionEvent, (float) getWidth())) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return false;
    }
}
