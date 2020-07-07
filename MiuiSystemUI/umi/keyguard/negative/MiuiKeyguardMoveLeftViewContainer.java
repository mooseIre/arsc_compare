package com.android.keyguard.negative;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBar;

public class MiuiKeyguardMoveLeftViewContainer extends FrameLayout {
    MiuiKeyguardMoveLeftBaseView mKeyguardMoveLeftView;
    protected NotificationPanelView mPanel;
    protected StatusBar mStatusBar;

    public MiuiKeyguardMoveLeftViewContainer(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardMoveLeftViewContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        inflateLeftView();
    }

    public void inflateLeftView() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            removeView(miuiKeyguardMoveLeftBaseView);
            this.mKeyguardMoveLeftView = null;
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isSupportLockScreenMagazineLeft()) {
            this.mKeyguardMoveLeftView = (MiuiKeyguardMoveLeftLockScreenMagazineView) LayoutInflater.from(getContext()).inflate(R.layout.miui_keyguard_left_view_lock_screen_magazine_layout, (ViewGroup) null, false);
        } else {
            this.mKeyguardMoveLeftView = (MiuiKeyguardMoveLeftControlCenterView) LayoutInflater.from(getContext()).inflate(R.layout.miui_keyguard_left_view_control_center_layout, (ViewGroup) null, false);
        }
        this.mKeyguardMoveLeftView.setStatusBar(this.mStatusBar);
        NotificationPanelView notificationPanelView = this.mPanel;
        if (notificationPanelView != null) {
            this.mKeyguardMoveLeftView.setPanel(notificationPanelView);
            setCustomBackground();
        }
        addView(this.mKeyguardMoveLeftView);
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.setStatusBar(statusBar);
        }
    }

    public void setPanel(NotificationPanelView notificationPanelView) {
        this.mPanel = notificationPanelView;
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.setPanel(notificationPanelView);
        }
    }

    public void initLeftView() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.initLeftView();
        }
    }

    public void uploadData() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.uploadData();
        }
    }

    public boolean isSupportRightMove() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView == null) {
            return false;
        }
        return miuiKeyguardMoveLeftBaseView.isSupportRightMove();
    }

    public void setCustomBackground() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            miuiKeyguardMoveLeftBaseView.setCustomBackground();
        }
    }

    public boolean hasBackgroundImageDrawable() {
        MiuiKeyguardMoveLeftBaseView miuiKeyguardMoveLeftBaseView = this.mKeyguardMoveLeftView;
        if (miuiKeyguardMoveLeftBaseView != null) {
            return miuiKeyguardMoveLeftBaseView.hasBackgroundImageDrawable();
        }
        return false;
    }
}
