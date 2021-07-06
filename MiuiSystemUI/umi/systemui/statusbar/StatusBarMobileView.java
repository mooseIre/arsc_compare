package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import codeinjection.CodeInjection;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0022R$style;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.DemoModeController;
import miui.os.Build;

public class StatusBarMobileView extends LinearLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable, DemoMode {
    private int mColor;
    private boolean mDrip;
    private boolean mForceUpdate;
    private boolean mInDemoMode;
    private ImageView mLeftInOut;
    private int mLeftInOutResId;
    private boolean mLight = true;
    private ImageView mMobile;
    private LinearLayout mMobileContent;
    private LinearLayout mMobileGroup;
    private View mMobileLeftContainer;
    private int mMobileResId;
    private View mMobileRightContainer;
    private ImageView mMobileRoaming;
    private TextView mMobileType;
    private ImageView mMobileTypeImage;
    private int mMobileTypeImageResId;
    private TextView mMobileTypeSingle;
    private Rect mRect = new Rect();
    private ImageView mRightInOut;
    private int mRightInOutResId;
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
    private int mVolteResId;
    private ImageView mVowifi;
    private int mVowifiResId;

    private void initDotView() {
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isSignalView() {
        return true;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setDecorColor(int i) {
    }

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

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z && getVisibility() != 8) {
            ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).reapply(this);
        }
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

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void onDensityOrFontScaleChanged() {
        TextView textView = this.mMobileType;
        if (textView != null) {
            textView.setTextAppearance(C0022R$style.TextAppearance_StatusBar_Signal);
            updateMobileTypeLayout(this.mState.networkName);
        }
        TextView textView2 = this.mMobileTypeSingle;
        if (textView2 != null) {
            textView2.setTextAppearance(C0022R$style.TextAppearance_StatusBar_Clock);
        }
    }

    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
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
        this.mMobileTypeSingle = (TextView) findViewById(C0015R$id.mobile_type_single);
        this.mVolteNoService = (ImageView) findViewById(C0015R$id.mobile_volte_no_service);
        initDotView();
    }

    public void applyMobileState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        boolean z;
        boolean z2 = true;
        if (mobileIconState == null) {
            z = getVisibility() != 8;
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
            if (mobileIconState2 == null) {
                initViewState(mobileIconState.copy());
                z = true;
            } else {
                z = !mobileIconState2.equals(mobileIconState) ? updateState(mobileIconState.copy()) : false;
            }
        }
        if (mobileIconState == null || getVisibility() == 0) {
            z2 = z;
        } else {
            setVisibility(0);
        }
        if (z2) {
            requestLayout();
        }
    }

    private void initViewState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        String str;
        boolean z;
        int i = 0;
        this.mMobileGroup.setVisibility((!mobileIconState.visible || mobileIconState.airplane) ? 8 : 0);
        StringBuilder sb = new StringBuilder();
        if (mobileIconState.networkName != null) {
            str = mobileIconState.networkName + " ";
        } else {
            str = CodeInjection.MD5;
        }
        sb.append(str);
        sb.append(mobileIconState.contentDescription);
        setContentDescription(sb.toString());
        int i2 = mobileIconState.vowifiId;
        if (i2 > 0) {
            this.mVowifiResId = i2;
            this.mVowifi.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i2, this.mUseTint, this.mLight));
        }
        int i3 = mobileIconState.volteId;
        if (i3 > 0) {
            this.mVolteResId = i3;
            this.mVolte.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i3, this.mUseTint, this.mLight));
        }
        int i4 = mobileIconState.fiveGDrawableId;
        if (i4 > 0) {
            this.mMobileTypeImageResId = i4;
            this.mMobileTypeImage.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i4, this.mUseTint, this.mLight));
            this.mMobileTypeImage.setVisibility(0);
            this.mMobileTypeSingle.setVisibility(8);
            this.mMobileType.setVisibility(8);
        } else if (mobileIconState.showMobileDataTypeSingle) {
            this.mMobileTypeSingle.setText(mobileIconState.networkName);
            this.mMobileTypeImage.setVisibility(8);
            this.mMobileType.setVisibility(8);
            this.mMobileTypeSingle.setVisibility(0);
        } else {
            this.mMobileType.setText(mobileIconState.networkName);
            updateMobileTypeLayout(mobileIconState.networkName);
            this.mMobileTypeImage.setVisibility(8);
            this.mMobileTypeSingle.setVisibility(8);
            this.mMobileType.setVisibility(0);
        }
        if (mobileIconState.dataConnected) {
            if (mobileIconState.activityIn && mobileIconState.activityOut) {
                int i5 = C0013R$drawable.stat_sys_signal_inout_left;
                this.mLeftInOutResId = i5;
                this.mRightInOutResId = i5;
            } else if (mobileIconState.activityIn) {
                int i6 = C0013R$drawable.stat_sys_signal_in_left;
                this.mLeftInOutResId = i6;
                this.mRightInOutResId = i6;
            } else if (mobileIconState.activityOut) {
                int i7 = C0013R$drawable.stat_sys_signal_out_left;
                this.mLeftInOutResId = i7;
                this.mRightInOutResId = i7;
            } else {
                int i8 = C0013R$drawable.stat_sys_signal_data_left;
                this.mLeftInOutResId = i8;
                this.mRightInOutResId = i8;
            }
            this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(this.mLeftInOutResId, this.mUseTint, this.mLight));
            this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(this.mRightInOutResId, this.mUseTint, this.mLight));
        }
        int i9 = mobileIconState.strengthId;
        if (i9 > 0) {
            this.mMobileResId = i9;
            this.mMobile.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i9, this.mUseTint, this.mLight));
        }
        this.mMobileRoaming.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_data_connected_roam, this.mUseTint, this.mLight));
        this.mSmallHd.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_hd_notch, this.mUseTint, this.mLight));
        this.mSmallRoaming.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_data_connected_roam_small, this.mUseTint, this.mLight));
        this.mSpeechHd.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_speech_hd, this.mUseTint, this.mLight));
        this.mVolteNoService.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_volte_no_service, this.mUseTint, this.mLight));
        if (mobileIconState.dataConnected) {
            this.mLeftInOut.setVisibility(0);
            this.mRightInOut.setVisibility(0);
        } else {
            this.mLeftInOut.setVisibility(8);
            this.mRightInOut.setVisibility(8);
        }
        char c = 1;
        if (!isBuildTest()) {
            z = !mobileIconState.wifiAvailable || mobileIconState.showDataTypeWhenWifiOn;
            if (!mobileIconState.dataConnected && !mobileIconState.showDataTypeDataDisconnected) {
                z = false;
            }
        } else {
            z = true;
        }
        if (!z) {
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
        applyDarknessInternal();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:181:0x0303, code lost:
        if (r2.showMobileDataTypeSingle == r9.showMobileDataTypeSingle) goto L_0x0306;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateState(com.android.systemui.statusbar.phone.StatusBarSignalPolicy.MobileIconState r9) {
        /*
        // Method dump skipped, instructions count: 781
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.StatusBarMobileView.updateState(com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState):boolean");
    }

    private boolean isBuildTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CT_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0069  */
    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDarkChanged(android.graphics.Rect r3, float r4, int r5, int r6, int r7, boolean r8) {
        /*
        // Method dump skipped, instructions count: 112
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.StatusBarMobileView.onDarkChanged(android.graphics.Rect, float, int, int, int, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void updateImageDrawable() {
        int i = this.mVowifiResId;
        if (i > 0) {
            this.mVowifi.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i, this.mUseTint, this.mLight));
        }
        int i2 = this.mVolteResId;
        if (i2 > 0) {
            this.mVolte.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i2, this.mUseTint, this.mLight));
        }
        int i3 = this.mMobileTypeImageResId;
        if (i3 > 0) {
            this.mMobileTypeImage.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i3, this.mUseTint, this.mLight));
        }
        int i4 = this.mMobileResId;
        if (i4 > 0) {
            this.mMobile.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i4, this.mUseTint, this.mLight));
        }
        int i5 = this.mLeftInOutResId;
        if (i5 > 0) {
            this.mLeftInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i5, this.mUseTint, this.mLight));
        }
        int i6 = this.mRightInOutResId;
        if (i6 > 0) {
            this.mRightInOut.setImageResource(MiuiStatusBarIconViewHelper.transformResId(i6, this.mUseTint, this.mLight));
        }
        this.mMobileRoaming.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_data_connected_roam, this.mUseTint, this.mLight));
        this.mSmallHd.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_signal_hd_notch, this.mUseTint, this.mLight));
        this.mSmallRoaming.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_data_connected_roam_small, this.mUseTint, this.mLight));
        this.mSpeechHd.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_speech_hd, this.mUseTint, this.mLight));
        this.mVolteNoService.setImageResource(MiuiStatusBarIconViewHelper.transformResId(C0013R$drawable.stat_sys_volte_no_service, this.mUseTint, this.mLight));
    }

    /* access modifiers changed from: protected */
    public void applyDarknessInternal() {
        if (this.mUseTint) {
            ColorStateList valueOf = ColorStateList.valueOf(DarkIconDispatcher.getTint(this.mRect, this, this.mTint));
            this.mMobile.setImageTintList(valueOf);
            this.mVowifi.setImageTintList(valueOf);
            this.mSpeechHd.setImageTintList(valueOf);
            this.mSmallRoaming.setImageTintList(valueOf);
            this.mRightInOut.setImageTintList(valueOf);
            this.mLeftInOut.setImageTintList(valueOf);
            this.mMobileTypeImage.setImageTintList(valueOf);
            this.mSmallHd.setImageTintList(valueOf);
            this.mMobileType.setTextColor(valueOf);
            this.mMobileTypeSingle.setTextColor(valueOf);
            this.mVolte.setImageTintList(valueOf);
            this.mMobileRoaming.setImageTintList(valueOf);
            this.mVolteNoService.setImageTintList(valueOf);
            return;
        }
        this.mMobileType.setTextColor(this.mColor);
        this.mMobileTypeSingle.setTextColor(this.mColor);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    public void setSlot(String str) {
        this.mSlot = str;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setStaticDrawableColor(int i) {
        ColorStateList valueOf = ColorStateList.valueOf(i);
        this.mMobile.setImageTintList(valueOf);
        this.mVowifi.setImageTintList(valueOf);
        this.mSpeechHd.setImageTintList(valueOf);
        this.mSmallRoaming.setImageTintList(valueOf);
        this.mRightInOut.setImageTintList(valueOf);
        this.mLeftInOut.setImageTintList(valueOf);
        this.mMobileTypeImage.setImageTintList(valueOf);
        this.mSmallHd.setImageTintList(valueOf);
        this.mMobileType.setTextColor(valueOf);
        this.mMobileTypeSingle.setTextColor(valueOf);
        this.mVolte.setImageTintList(valueOf);
        this.mMobileRoaming.setImageTintList(valueOf);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        return getVisibility() == 0;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        if (this.mInDemoMode) {
            this.mVisibleState = i;
            this.mMobileContent.setVisibility(8);
        } else if (i != this.mVisibleState || this.mForceUpdate) {
            this.mForceUpdate = false;
            this.mVisibleState = i;
            requestLayout();
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

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setDrip(boolean z) {
        this.mDrip = z;
    }

    public String toString() {
        return "StatusBarMobileView(slot=" + this.mSlot + " state=" + this.mState + ", measuredWidth = " + Integer.toHexString(getMeasuredHeightAndState()) + ", width = " + getWidth() + ") , " + super.toString();
    }

    private void updateMobileTypeLayout(String str) {
        if (str != null) {
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
            layoutParams3.setMarginEnd((int) (-(f2 + f3)));
            this.mMobileLeftContainer.setLayoutParams(layoutParams3);
        }
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
