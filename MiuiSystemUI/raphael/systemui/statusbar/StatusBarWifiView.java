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
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.DemoModeController;

public class StatusBarWifiView extends FrameLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable, DemoMode {
    private int mColor;
    private StatusBarIconView mDotView;
    private boolean mForceUpdate;
    private boolean mInDemoMode;
    private boolean mLight = true;
    private Rect mRect = new Rect();
    private String mSlot;
    private StatusBarSignalPolicy.WifiIconState mState;
    private int mTint;
    private boolean mUseTint = false;
    private int mVisibleState = -1;
    private ImageView mWifiActivityView;
    private FrameLayout mWifiGroup;
    private ImageView mWifiIcon;
    private TextView mWifiStandardView;

    public static StatusBarWifiView fromContext(Context context, String str) {
        StatusBarWifiView statusBarWifiView = (StatusBarWifiView) LayoutInflater.from(context).inflate(C0017R$layout.miui_status_bar_wifi_group, (ViewGroup) null);
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

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        StatusBarSignalPolicy.WifiIconState wifiIconState = this.mState;
        return wifiIconState != null && wifiIconState.visible;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
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

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
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
        this.mWifiGroup = (FrameLayout) findViewById(C0015R$id.wifi_group);
        this.mWifiIcon = (ImageView) findViewById(C0015R$id.wifi_signal);
        this.mWifiActivityView = (ImageView) findViewById(C0015R$id.wifi_activity);
        this.mWifiStandardView = (TextView) findViewById(C0015R$id.wifi_standard);
        initDotView();
    }

    private void initDotView() {
        StatusBarIconView statusBarIconView = new StatusBarIconView(((FrameLayout) this).mContext, this.mSlot, null);
        this.mDotView = statusBarIconView;
        statusBarIconView.setVisibleState(1);
        int dimensionPixelSize = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_icon_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 8388627;
        addView(this.mDotView, layoutParams);
    }

    public void applyWifiState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        if (wifiIconState == null) {
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.WifiIconState wifiIconState2 = this.mState;
            if (wifiIconState2 == null) {
                StatusBarSignalPolicy.WifiIconState copy = wifiIconState.copy();
                this.mState = copy;
                updateState(copy);
            } else if (!wifiIconState2.equals(wifiIconState)) {
                StatusBarSignalPolicy.WifiIconState copy2 = wifiIconState.copy();
                this.mState = copy2;
                updateState(copy2);
            }
        }
        requestLayout();
    }

    private void updateState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        setContentDescription(wifiIconState.contentDescription);
        if (wifiIconState.wifiNoNetwork) {
            this.mWifiIcon.setImageDrawable(((FrameLayout) this).mContext.getDrawable(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_wifi_signal_null, this.mUseTint, this.mLight)));
        } else {
            int i = wifiIconState.resId;
            if (i > 0) {
                this.mWifiIcon.setImageDrawable(((FrameLayout) this).mContext.getDrawable(MiuiStatusBarIconViewHelper.transformResId(i, this.mUseTint, this.mLight)));
            }
        }
        int i2 = wifiIconState.activityResId;
        if (i2 > 0) {
            this.mWifiActivityView.setImageDrawable(((FrameLayout) this).mContext.getDrawable(MiuiStatusBarIconViewHelper.transformResId(i2, this.mUseTint, this.mLight)));
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mWifiActivityView.getLayoutParams();
        int i3 = 0;
        if (wifiIconState.showWifiStandard) {
            this.mWifiStandardView.setText(String.valueOf(wifiIconState.wifiStandard));
            this.mWifiStandardView.setVisibility(0);
            layoutParams.gravity = 83;
        } else {
            this.mWifiStandardView.setVisibility(8);
            layoutParams.gravity = 85;
        }
        this.mWifiActivityView.setLayoutParams(layoutParams);
        this.mWifiActivityView.setVisibility(wifiIconState.activityVisible ? 0 : 8);
        if (!wifiIconState.visible) {
            i3 = 8;
        }
        setVisibility(i3);
        applyDarknessInternal();
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0041  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0047  */
    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDarkChanged(android.graphics.Rect r3, float r4, int r5, int r6, int r7, boolean r8) {
        /*
            r2 = this;
            android.graphics.Rect r0 = r2.mRect
            r0.set(r3)
            r2.mTint = r5
            boolean r5 = r2.mUseTint
            r0 = 0
            r1 = 1
            if (r5 == r8) goto L_0x001e
            r2.mUseTint = r8
            if (r8 != 0) goto L_0x001c
            android.widget.ImageView r5 = r2.mWifiIcon
            r8 = 0
            r5.setImageTintList(r8)
            android.widget.ImageView r5 = r2.mWifiActivityView
            r5.setImageTintList(r8)
        L_0x001c:
            r5 = r1
            goto L_0x001f
        L_0x001e:
            r5 = r0
        L_0x001f:
            boolean r8 = r2.mUseTint
            if (r8 != 0) goto L_0x003e
            float r3 = com.android.systemui.plugins.DarkIconDispatcher.getDarkIntensity(r3, r2, r4)
            r4 = 0
            int r3 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1))
            if (r3 != 0) goto L_0x002d
            r0 = r1
        L_0x002d:
            if (r0 == 0) goto L_0x0030
            goto L_0x0031
        L_0x0030:
            r6 = r7
        L_0x0031:
            boolean r3 = r2.mLight
            if (r3 != r0) goto L_0x0039
            int r3 = r2.mColor
            if (r3 == r6) goto L_0x003e
        L_0x0039:
            r2.mLight = r0
            r2.mColor = r6
            goto L_0x003f
        L_0x003e:
            r1 = r5
        L_0x003f:
            if (r1 == 0) goto L_0x0047
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$WifiIconState r3 = r2.mState
            r2.updateState(r3)
            goto L_0x004a
        L_0x0047:
            r2.applyDarknessInternal()
        L_0x004a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.StatusBarWifiView.onDarkChanged(android.graphics.Rect, float, int, int, int, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void applyDarknessInternal() {
        if (this.mUseTint) {
            int tint = DarkIconDispatcher.getTint(this.mRect, this, this.mTint);
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

    @Override // com.android.systemui.DemoMode
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
