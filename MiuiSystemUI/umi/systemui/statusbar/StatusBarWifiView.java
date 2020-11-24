package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.DemoModeController;

public class StatusBarWifiView extends FrameLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable, DemoMode {
    private int mColor;
    private StatusBarIconView mDotView;
    private boolean mForceUpdate;
    private boolean mInDemoMode;
    private boolean mLight = true;
    private String mSlot;
    private StatusBarSignalPolicy.WifiIconState mState;
    private boolean mUseTint = false;
    private int mVisibleState = -1;
    private ImageView mWifiActivityView;
    private FrameLayout mWifiGroup;
    private ImageView mWifiIcon;
    private TextView mWifiStandardView;

    public static StatusBarWifiView fromContext(Context context, String str) {
        StatusBarWifiView statusBarWifiView = (StatusBarWifiView) LayoutInflater.from(context).inflate(C0014R$layout.miui_status_bar_wifi_group, (ViewGroup) null);
        statusBarWifiView.setSlot(str);
        statusBarWifiView.init();
        statusBarWifiView.setVisibleState(0);
        return statusBarWifiView;
    }

    public StatusBarWifiView(Context context) {
        super(context);
    }

    public StatusBarWifiView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public StatusBarWifiView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public StatusBarWifiView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void setSlot(String str) {
        this.mSlot = str;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DemoModeController) Dependency.get(DemoModeController.class)).removeCallback(this);
    }

    public void setDecorColor(int i) {
        this.mDotView.setDecorColor(i);
    }

    public String getSlot() {
        return this.mSlot;
    }

    public boolean isIconVisible() {
        StatusBarSignalPolicy.WifiIconState wifiIconState = this.mState;
        return wifiIconState != null && wifiIconState.visible;
    }

    public void setVisibleState(int i, boolean z) {
        if (this.mInDemoMode) {
            this.mVisibleState = i;
            this.mWifiGroup.setVisibility(8);
            this.mDotView.setVisibility(8);
        } else if (i != this.mVisibleState || this.mForceUpdate) {
            this.mForceUpdate = false;
            this.mVisibleState = i;
            if (i == 0) {
                this.mWifiGroup.setVisibility(0);
                this.mDotView.setVisibility(8);
            } else if (i != 1) {
                this.mWifiGroup.setVisibility(8);
                this.mDotView.setVisibility(8);
            } else {
                this.mWifiGroup.setVisibility(8);
                this.mDotView.setVisibility(0);
            }
        }
    }

    public int getVisibleState() {
        return this.mVisibleState;
    }

    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        float translationX = getTranslationX();
        float translationY = getTranslationY();
        rect.left = (int) (((float) rect.left) + translationX);
        rect.right = (int) (((float) rect.right) + translationX);
        rect.top = (int) (((float) rect.top) + translationY);
        rect.bottom = (int) (((float) rect.bottom) + translationY);
    }

    private void init() {
        this.mWifiGroup = (FrameLayout) findViewById(C0012R$id.wifi_group);
        this.mWifiIcon = (ImageView) findViewById(C0012R$id.wifi_signal);
        this.mWifiActivityView = (ImageView) findViewById(C0012R$id.wifi_activity);
        this.mWifiStandardView = (TextView) findViewById(C0012R$id.wifi_standard);
        initDotView();
    }

    private void initDotView() {
        StatusBarIconView statusBarIconView = new StatusBarIconView(this.mContext, this.mSlot, (ExpandedNotification) null);
        this.mDotView = statusBarIconView;
        statusBarIconView.setVisibleState(1);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.status_bar_icon_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 8388627;
        addView(this.mDotView, layoutParams);
    }

    public void applyWifiState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        boolean z = true;
        if (wifiIconState == null) {
            if (getVisibility() == 8) {
                z = false;
            }
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.WifiIconState wifiIconState2 = this.mState;
            if (wifiIconState2 == null) {
                this.mState = wifiIconState.copy();
                updateState(wifiIconState, true);
            } else {
                z = !wifiIconState2.equals(wifiIconState) ? updateState(wifiIconState.copy(), false) : false;
            }
        }
        if (z) {
            requestLayout();
        }
    }

    private boolean updateState(StatusBarSignalPolicy.WifiIconState wifiIconState, boolean z) {
        boolean z2;
        int i;
        int i2;
        setContentDescription(wifiIconState.contentDescription);
        if ((this.mState.resId != wifiIconState.resId || z) && (i2 = wifiIconState.resId) > 0) {
            this.mWifiIcon.setImageDrawable(this.mContext.getDrawable(MiuiStatusBarIconViewHelper.transformResId(i2, this.mUseTint, this.mLight)));
        }
        if ((this.mState.activityResId != wifiIconState.activityResId || z) && (i = wifiIconState.activityResId) > 0) {
            this.mWifiActivityView.setImageDrawable(this.mContext.getDrawable(MiuiStatusBarIconViewHelper.transformResId(i, this.mUseTint, this.mLight)));
        }
        boolean z3 = this.mState.activityVisible;
        boolean z4 = wifiIconState.activityVisible;
        int i3 = 8;
        if (z3 != z4) {
            this.mWifiActivityView.setVisibility(z4 ? 0 : 8);
            z2 = true;
        } else {
            z2 = false;
        }
        if (wifiIconState.showWifiStandard != this.mState.showWifiStandard) {
            z2 |= true;
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mWifiActivityView.getLayoutParams();
            if (wifiIconState.showWifiStandard) {
                this.mWifiStandardView.setText(String.valueOf(wifiIconState.wifiStandard));
                this.mWifiStandardView.setVisibility(0);
                layoutParams.gravity = 83;
            } else {
                this.mWifiStandardView.setVisibility(8);
                layoutParams.gravity = 85;
            }
        }
        boolean z5 = this.mState.visible;
        boolean z6 = wifiIconState.visible;
        if (z5 != z6) {
            z2 |= true;
            if (z6) {
                i3 = 0;
            }
            setVisibility(i3);
        }
        this.mState = wifiIconState;
        return z2;
    }

    public void setLight(boolean z, int i) {
        this.mUseTint = false;
        if (this.mLight != z) {
            this.mLight = z;
            updateState(this.mState, true);
        }
        if (this.mColor != i) {
            this.mColor = i;
            setDecorColor(i);
        }
    }

    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        boolean z2;
        if (this.mUseTint != z) {
            this.mUseTint = z;
            if (!z) {
                this.mWifiIcon.setImageTintList((ColorStateList) null);
                this.mWifiActivityView.setImageTintList((ColorStateList) null);
            }
            z2 = true;
        } else {
            z2 = false;
        }
        if (!this.mUseTint) {
            boolean z3 = DarkIconDispatcher.getDarkIntensity(rect, this, f) == 0.0f;
            if (!z3) {
                i2 = i3;
            }
            if (!(this.mLight == z3 && this.mColor == i2)) {
                this.mLight = z3;
                this.mColor = i2;
                z2 = true;
            }
        }
        if (z2) {
            updateState(this.mState, true);
        }
        if (this.mUseTint) {
            int tint = DarkIconDispatcher.getTint(rect, this, i);
            ColorStateList valueOf = ColorStateList.valueOf(tint);
            this.mWifiIcon.setImageTintList(valueOf);
            this.mWifiActivityView.setImageTintList(valueOf);
            this.mWifiStandardView.setTextColor(tint);
            this.mDotView.setDecorColor(tint);
            this.mDotView.setIconColor(tint, false);
            return;
        }
        this.mWifiStandardView.setTextColor(this.mColor);
        this.mDotView.setDecorColor(this.mColor);
        this.mDotView.setIconColor(this.mColor, false);
    }

    public String toString() {
        return "StatusBarWifiView(slot=" + this.mSlot + " state=" + this.mState + ")";
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mInDemoMode && str.equals("enter")) {
            this.mInDemoMode = true;
            this.mForceUpdate = true;
            setVisibleState(this.mVisibleState, false);
        } else if (this.mInDemoMode && str.equals("exit")) {
            this.mInDemoMode = false;
            this.mForceUpdate = true;
            setVisibleState(this.mVisibleState, false);
        }
    }
}
