package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.DemoModeController;
import java.util.Objects;
import miui.os.Build;

public class StatusBarMobileView extends LinearLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable, DemoMode {
    private int mColor;
    private int mDarkColor;
    private float mDarkIntensity;
    private StatusBarIconView mDotView;
    private boolean mDrip;
    private boolean mForceUpdate;
    private boolean mInDemoMode;
    private String mLastShowName;
    private ImageView mLeftInOut;
    private boolean mLight = true;
    private int mLightColor;
    private ImageView mMobile;
    private LinearLayout mMobileContent;
    private LinearLayout mMobileGroup;
    private View mMobileLeftContainer;
    private View mMobileRightContainer;
    private ImageView mMobileRoaming;
    private TextView mMobileType;
    private ImageView mMobileTypeImage;
    private Rect mRect = new Rect();
    private ImageView mRightInOut;
    private String mSlot;
    private ImageView mSmallHd;
    private ImageView mSmallRoaming;
    private ImageView mSpeechHd;
    private StatusBarSignalPolicy.MobileIconState mState;
    private int mTint;
    private boolean mUseTint = false;
    private int mVisibleState = -1;
    private ImageView mVolte;
    private ImageView mVolteNoService;
    private ImageView mVowifi;

    public static StatusBarMobileView fromContext(Context context, String str) {
        StatusBarMobileView statusBarMobileView = (StatusBarMobileView) LayoutInflater.from(context).inflate(C0017R$layout.status_bar_mobile_signal_group, (ViewGroup) null);
        statusBarMobileView.setSlot(str);
        statusBarMobileView.init();
        statusBarMobileView.setVisibleState(0);
        return statusBarMobileView;
    }

    public StatusBarMobileView(Context context) {
        super(context);
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
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
        this.mMobileGroup = (LinearLayout) findViewById(C0015R$id.mobile_group);
        this.mMobileContent = (LinearLayout) findViewById(C0015R$id.mobile_content);
        this.mMobile = (ImageView) findViewById(C0015R$id.mobile_signal);
        this.mMobileType = (TextView) findViewById(C0015R$id.mobile_type);
        this.mMobileRoaming = (ImageView) findViewById(C0015R$id.mobile_roaming);
        this.mVolte = (ImageView) findViewById(C0015R$id.mobile_volte);
        this.mLeftInOut = (ImageView) findViewById(C0015R$id.mobile_left_mobile_inout);
        this.mRightInOut = (ImageView) findViewById(C0015R$id.mobile_right_mobile_inout);
        this.mSmallHd = (ImageView) findViewById(C0015R$id.mobile_small_hd);
        this.mSmallRoaming = (ImageView) findViewById(C0015R$id.mobile_small_roam);
        this.mVowifi = (ImageView) findViewById(C0015R$id.mobile_vowifi);
        this.mSpeechHd = (ImageView) findViewById(C0015R$id.mobile_speech_hd);
        this.mMobileLeftContainer = findViewById(C0015R$id.mobile_container_left);
        this.mMobileRightContainer = findViewById(C0015R$id.mobile_container_right);
        this.mMobileTypeImage = (ImageView) findViewById(C0015R$id.mobile_type_image);
        this.mVolteNoService = (ImageView) findViewById(C0015R$id.mobile_volte_no_service);
        initDotView();
    }

    private void initDotView() {
        StatusBarIconView statusBarIconView = new StatusBarIconView(((LinearLayout) this).mContext, this.mSlot, null);
        this.mDotView = statusBarIconView;
        statusBarIconView.setVisibleState(1);
        int dimensionPixelSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_icon_size);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 8388627;
        addView(this.mDotView, layoutParams);
    }

    public void applyMobileState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        boolean z = true;
        if (mobileIconState == null) {
            if (getVisibility() == 8) {
                z = false;
            }
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
            if (mobileIconState2 == null) {
                StatusBarSignalPolicy.MobileIconState copy = mobileIconState.copy();
                this.mState = copy;
                initViewState(copy);
            } else {
                z = !mobileIconState2.equals(mobileIconState) ? updateState(mobileIconState.copy(), false) : false;
            }
        }
        if (z) {
            requestLayout();
        }
        if (needFixVisibleState()) {
            Log.d("StatusBarMobileView", "fix VisibleState width=" + getWidth() + " height=" + getHeight());
            this.mVisibleState = 0;
            setVisibility(0);
            requestLayout();
        } else if (needFixInVisibleState()) {
            Log.d("StatusBarMobileView", "fix InVisibleState width=" + getWidth() + " height=" + getHeight());
            this.mVisibleState = -1;
            setVisibility(4);
            requestLayout();
        }
    }

    private void initViewState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        updateState(mobileIconState, true);
    }

    private boolean updateState(StatusBarSignalPolicy.MobileIconState mobileIconState, boolean z) {
        String str;
        boolean z2;
        char c;
        int i = 0;
        this.mMobileGroup.setVisibility((!mobileIconState.visible || mobileIconState.airplane) ? 8 : 0);
        StringBuilder sb = new StringBuilder();
        if (mobileIconState.networkName != null) {
            str = mobileIconState.networkName + " ";
        } else {
            str = "";
        }
        sb.append(str);
        sb.append(mobileIconState.contentDescription);
        setContentDescription(sb.toString());
        int i2 = mobileIconState.vowifiId;
        if (i2 > 0 && (this.mState.vowifiId != i2 || z)) {
            this.mVowifi.setImageResource(MiuiStatusBarIconViewHelper.transformResId(mobileIconState.vowifiId, this.mUseTint, this.mLight));
        }
        int i3 = mobileIconState.volteId;
        if (i3 > 0 && (this.mState.volteId != i3 || z)) {
            this.mVolte.setImageResource(MiuiStatusBarIconViewHelper.transformResId(mobileIconState.volteId, this.mUseTint, this.mLight));
        }
        int i4 = mobileIconState.fiveGDrawableId;
        if (i4 > 0) {
            if (this.mState.fiveGDrawableId != i4 || z) {
                this.mMobileTypeImage.setImageResource(MiuiStatusBarIconViewHelper.transformResId(mobileIconState.fiveGDrawableId, this.mUseTint, this.mLight));
                this.mMobileTypeImage.setVisibility(0);
                this.mMobileType.setVisibility(8);
            }
        } else if (!Objects.equals(this.mState.networkName, mobileIconState.networkName) || z) {
            this.mMobileType.setText(mobileIconState.networkName);
            updateMobileTypeLayout(mobileIconState.networkName);
            this.mMobileTypeImage.setVisibility(8);
            this.mMobileType.setVisibility(0);
        }
        StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
        if ((mobileIconState2.dataConnected == mobileIconState.dataConnected && mobileIconState2.activityIn == mobileIconState.activityIn && mobileIconState2.activityOut == mobileIconState.activityOut && !z) ? false : true) {
            if (!mobileIconState.dataConnected) {
                this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_data_left, this.mUseTint, this.mLight));
                this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_data_left, this.mUseTint, this.mLight));
            } else if (mobileIconState.activityIn && mobileIconState.activityOut) {
                this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_inout_left, this.mUseTint, this.mLight));
                this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_inout_left, this.mUseTint, this.mLight));
            } else if (mobileIconState.activityIn) {
                this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_in_left, this.mUseTint, this.mLight));
                this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_in_left, this.mUseTint, this.mLight));
            } else if (mobileIconState.activityOut) {
                this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_out_left, this.mUseTint, this.mLight));
                this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_out_left, this.mUseTint, this.mLight));
            } else {
                this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_data_left, this.mUseTint, this.mLight));
                this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_data_left, this.mUseTint, this.mLight));
            }
        }
        int i5 = mobileIconState.strengthId;
        if (i5 > 0 && (this.mState.strengthId != i5 || z)) {
            this.mMobile.setImageResource(MiuiStatusBarIconViewHelper.transformResId(mobileIconState.strengthId, this.mUseTint, this.mLight));
        }
        if (z) {
            this.mMobileRoaming.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_data_connected_roam, this.mUseTint, this.mLight));
            this.mSmallHd.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_hd_notch, this.mUseTint, this.mLight));
            this.mSmallRoaming.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_data_connected_roam_small, this.mUseTint, this.mLight));
            this.mSpeechHd.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_speech_hd, this.mUseTint, this.mLight));
            this.mVolteNoService.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_volte_no_service, this.mUseTint, this.mLight));
        }
        if (mobileIconState.dataConnected) {
            this.mLeftInOut.setVisibility(0);
            this.mRightInOut.setVisibility(0);
        } else {
            this.mLeftInOut.setVisibility(8);
            this.mRightInOut.setVisibility(8);
        }
        if (!isBuildTest()) {
            z2 = !mobileIconState.wifiAvailable || mobileIconState.showDataTypeWhenWifiOn;
            if (!mobileIconState.dataConnected && !mobileIconState.showDataTypeDataDisconnected) {
                z2 = false;
            }
        } else {
            z2 = true;
        }
        if (!z2) {
            this.mMobileLeftContainer.setVisibility(8);
            this.mMobileRightContainer.setVisibility(8);
            c = 0;
        } else if (mobileIconState.fiveGDrawableId > 0) {
            c = 2;
            this.mMobileLeftContainer.setVisibility(8);
            this.mMobileRightContainer.setVisibility(0);
        } else {
            this.mMobileLeftContainer.setVisibility(0);
            this.mMobileRightContainer.setVisibility(8);
            c = 1;
        }
        if (Build.IS_INTERNATIONAL_BUILD) {
            this.mSmallRoaming.setVisibility(8);
            this.mSmallHd.setVisibility(8);
            this.mMobileRoaming.setVisibility(mobileIconState.roaming ? 0 : 8);
            if (!this.mDrip || !mobileIconState.wifiAvailable) {
                this.mVolte.setVisibility((!mobileIconState.volte || mobileIconState.hideVolte || mobileIconState.roaming) ? 8 : 0);
            } else {
                this.mVolte.setVisibility(8);
            }
        } else if (c == 0) {
            if (mobileIconState.roaming) {
                this.mSmallRoaming.setVisibility(0);
                this.mSmallHd.setVisibility(8);
            } else if (mobileIconState.volte) {
                this.mSmallHd.setVisibility(0);
                this.mSmallRoaming.setVisibility(8);
            } else {
                this.mSmallRoaming.setVisibility(8);
                this.mSmallHd.setVisibility(8);
            }
            this.mMobileRoaming.setVisibility(8);
            this.mVolte.setVisibility(8);
        } else {
            this.mSmallRoaming.setVisibility(8);
            this.mSmallHd.setVisibility(8);
            this.mMobileRoaming.setVisibility(mobileIconState.roaming ? 0 : 8);
            this.mVolte.setVisibility((!mobileIconState.volte || mobileIconState.hideVolte || mobileIconState.roaming) ? 8 : 0);
        }
        this.mVowifi.setVisibility((!mobileIconState.vowifi || mobileIconState.hideVowifi) ? 8 : 0);
        this.mSpeechHd.setVisibility(mobileIconState.speechHd ? 0 : 8);
        ImageView imageView = this.mVolteNoService;
        if (!mobileIconState.volteNoSerivce) {
            i = 8;
        }
        imageView.setVisibility(i);
        this.mState = mobileIconState;
        onDarkChanged(this.mRect, this.mDarkIntensity, this.mTint, this.mLightColor, this.mDarkColor, this.mUseTint);
        return true;
    }

    private boolean isBuildTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CT_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        boolean z2;
        this.mRect.set(rect);
        this.mDarkIntensity = f;
        this.mTint = i;
        this.mLightColor = i2;
        this.mDarkColor = i3;
        if (this.mUseTint != z) {
            this.mUseTint = z;
            if (!z) {
                this.mMobile.setImageTintList(null);
                this.mSmallRoaming.setImageTintList(null);
                this.mSmallHd.setImageTintList(null);
                this.mMobileRoaming.setImageTintList(null);
                this.mVolte.setImageTintList(null);
                this.mVowifi.setImageTintList(null);
                this.mSpeechHd.setImageTintList(null);
                this.mLeftInOut.setImageTintList(null);
                this.mRightInOut.setImageTintList(null);
                this.mMobileTypeImage.setImageTintList(null);
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
            ColorStateList valueOf = ColorStateList.valueOf(DarkIconDispatcher.getTint(rect, this, i));
            this.mMobile.setImageTintList(valueOf);
            this.mVowifi.setImageTintList(valueOf);
            this.mSpeechHd.setImageTintList(valueOf);
            this.mSmallRoaming.setImageTintList(valueOf);
            this.mRightInOut.setImageTintList(valueOf);
            this.mLeftInOut.setImageTintList(valueOf);
            this.mMobileTypeImage.setImageTintList(valueOf);
            this.mSmallHd.setImageTintList(valueOf);
            this.mMobileType.setTextColor(valueOf);
            this.mVolte.setImageTintList(valueOf);
            this.mMobileRoaming.setImageTintList(valueOf);
            this.mVolteNoService.setImageTintList(valueOf);
            this.mDotView.setDecorColor(i);
            this.mDotView.setIconColor(i, false);
            return;
        }
        this.mMobileType.setTextColor(this.mColor);
        this.mDotView.setDecorColor(this.mColor);
        this.mDotView.setIconColor(this.mColor, false);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    public void setSlot(String str) {
        this.mSlot = str;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        return this.mState.visible;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        if (this.mInDemoMode) {
            this.mVisibleState = i;
            this.mMobileContent.setVisibility(8);
            this.mDotView.setVisibility(8);
        } else if (i != this.mVisibleState || this.mForceUpdate) {
            this.mForceUpdate = false;
            this.mVisibleState = i;
            if (i == 0) {
                this.mMobileContent.setVisibility(0);
                this.mDotView.setVisibility(8);
            } else if (i != 1) {
                this.mMobileContent.setVisibility(4);
                this.mDotView.setVisibility(4);
            } else {
                this.mMobileContent.setVisibility(4);
                this.mDotView.setVisibility(0);
            }
        }
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public int getVisibleState() {
        return this.mVisibleState;
    }

    @VisibleForTesting
    public StatusBarSignalPolicy.MobileIconState getState() {
        return this.mState;
    }

    private boolean needFixVisibleState() {
        return this.mState.visible && getVisibility() != 0;
    }

    private boolean needFixInVisibleState() {
        return !this.mState.visible && getVisibility() == 0;
    }

    public void setDrip(boolean z) {
        this.mDrip = z;
    }

    public String toString() {
        return "StatusBarMobileView(slot=" + this.mSlot + " state=" + this.mState + ") , " + super.toString();
    }

    private boolean updateMobileTypeLayout(String str) {
        if (str != null && !Objects.equals(str, this.mLastShowName)) {
            this.mLastShowName = str;
            TextPaint paint = this.mMobileType.getPaint();
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float f = fontMetrics.bottom - fontMetrics.top;
            float measureText = paint.measureText(str);
            int dimensionPixelSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_mobile_type_half_to_top_distance);
            int dimensionPixelSize2 = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_mobile_left_inout_over_strength);
            int dimensionPixelSize3 = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_mobile_type_middle_to_strength_start);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mMobileType.getLayoutParams();
            layoutParams.topMargin = (int) (((float) dimensionPixelSize) - (f / 2.0f));
            this.mMobileType.setLayoutParams(layoutParams);
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mLeftInOut.getLayoutParams();
            float f2 = measureText / 2.0f;
            float f3 = (float) dimensionPixelSize2;
            layoutParams2.setMarginEnd((int) ((f2 - f3) + ((float) dimensionPixelSize3)));
            this.mLeftInOut.setLayoutParams(layoutParams2);
            LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mMobileLeftContainer.getLayoutParams();
            layoutParams3.rightMargin = (int) (-(f2 + f3));
            this.mMobileLeftContainer.setLayoutParams(layoutParams3);
        }
        return !Objects.equals(str, this.mLastShowName);
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
