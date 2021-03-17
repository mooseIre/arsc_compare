package com.android.keyguard.charge.container;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.ChargeUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;

public class MiuiChargeIconView extends RelativeLayout {
    private int mCarIconAlpha;
    private Drawable mCarIconDrawable;
    private int mCarIconHeight;
    private int mCarIconScaleXY;
    private int mCarIconWidth;
    private ImageView mCarModeIcon;
    private int mChargeSpeed;
    private Interpolator mCubicInterpolator;
    private int mDoubleAlpha;
    private Drawable mDoubleLightningDrawable;
    private ImageView mDoubleLightningIcon;
    private int mDoubleLightningIconHeight;
    private int mDoubleLightningIconWidth;
    private int mDoubleScaleXY;
    private int mIconPaddingTop;
    private boolean mIsCarMode;
    private boolean mIsFoldChargeVideo;
    private int mPivotX;
    private Point mScreenSize;
    private int mSingleAlpha;
    private Drawable mSingleLightningDrawable;
    private ImageView mSingleLightningIcon;
    private int mSingleLightningIconHeight;
    private int mSingleLightningIconWidth;
    private int mSingleScaleXY;
    private Drawable mSpecialDoubleLightningDrawable;
    private ImageView mSpecialDoubleLightningIcon;
    private int mSpecialDoubleLightningIconHeight;
    private int mSpecialDoubleLightningIconWidth;
    private AnimatorSet mSwitchAnimator;
    private WindowManager mWindowManager;
    private int mYellowDoubleAlpha;
    private int mYellowDoubleScaleXY;

    public MiuiChargeIconView(Context context) {
        this(context, null);
    }

    public MiuiChargeIconView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiChargeIconView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCubicInterpolator = new CubicEaseOutInterpolater();
        this.mIsFoldChargeVideo = false;
        this.mChargeSpeed = 0;
        init(context);
    }

    private void init(Context context) {
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        this.mSingleLightningDrawable = context.getDrawable(C0013R$drawable.charge_animation_rapid_charge_icon);
        this.mDoubleLightningDrawable = context.getDrawable(C0013R$drawable.charge_animation_super_rapid_charge_icon);
        this.mSpecialDoubleLightningDrawable = context.getDrawable(C0013R$drawable.charge_animation_strong_super_rapid_charge_icon);
        this.mCarIconDrawable = context.getDrawable(C0013R$drawable.charge_animation_car_mode_icon);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        ImageView imageView = new ImageView(context);
        this.mSingleLightningIcon = imageView;
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mSingleLightningIcon.setImageDrawable(this.mSingleLightningDrawable);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(this.mSingleLightningIconWidth, this.mSingleLightningIconHeight + this.mIconPaddingTop);
        layoutParams.addRule(13);
        this.mSingleLightningIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mSingleLightningIcon.setPivotX((float) this.mPivotX);
        addView(this.mSingleLightningIcon, layoutParams);
        ImageView imageView2 = new ImageView(context);
        this.mDoubleLightningIcon = imageView2;
        imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mDoubleLightningIcon.setImageDrawable(this.mDoubleLightningDrawable);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(this.mDoubleLightningIconWidth, this.mDoubleLightningIconHeight + this.mIconPaddingTop);
        layoutParams2.addRule(13);
        this.mDoubleLightningIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mDoubleLightningIcon.setPivotX((float) this.mPivotX);
        addView(this.mDoubleLightningIcon, layoutParams2);
        ImageView imageView3 = new ImageView(context);
        this.mSpecialDoubleLightningIcon = imageView3;
        imageView3.setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mSpecialDoubleLightningIcon.setImageDrawable(this.mSpecialDoubleLightningDrawable);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(this.mSpecialDoubleLightningIconWidth, this.mSpecialDoubleLightningIconHeight + this.mIconPaddingTop);
        layoutParams3.addRule(13);
        this.mSpecialDoubleLightningIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.mSpecialDoubleLightningIcon.setPivotX((float) this.mPivotX);
        addView(this.mSpecialDoubleLightningIcon, layoutParams3);
        ImageView imageView4 = new ImageView(context);
        this.mCarModeIcon = imageView4;
        imageView4.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mCarModeIcon.setImageDrawable(this.mCarIconDrawable);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(this.mCarIconWidth, this.mCarIconHeight + this.mIconPaddingTop);
        layoutParams4.addRule(13);
        this.mCarModeIcon.setPivotX((float) this.mPivotX);
        this.mCarModeIcon.setPadding(0, this.mIconPaddingTop, 0, 0);
        addView(this.mCarModeIcon, layoutParams4);
    }

    private void resetCarMode() {
        this.mIsCarMode = ChargeUtils.isWirelessCarMode(ChargeUtils.sBatteryStatus.chargeDeviceType);
    }

    public void startLightningAnimation() {
        Log.d("MiuiChargeIconView", "startLightningAnimation: mChargeSpeed=" + this.mChargeSpeed);
        resetCarMode();
        resetIconViewState();
    }

    public void switchLightningAnimation(int i) {
        Log.d("MiuiChargeIconView", "switchLightningAnimation: mChargeSpeed=" + i);
        this.mChargeSpeed = i;
        resetCarMode();
        switchChargeIcon();
    }

    private void resetAllProperty() {
        this.mSingleScaleXY = 0;
        this.mSingleAlpha = 0;
        this.mDoubleScaleXY = 0;
        this.mDoubleAlpha = 0;
        this.mYellowDoubleScaleXY = 0;
        this.mYellowDoubleAlpha = 0;
        this.mCarIconScaleXY = 0;
        this.mCarIconAlpha = 0;
    }

    private void switchChargeIcon() {
        Property property = RelativeLayout.ALPHA;
        Property property2 = RelativeLayout.SCALE_Y;
        Property property3 = RelativeLayout.SCALE_X;
        AnimatorSet animatorSet = this.mSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        Log.d("MiuiChargeIconView", "switchChargeLightning: mChargeSpeed=" + this.mChargeSpeed + ",mIsCarMode=" + this.mIsCarMode);
        resetAllProperty();
        if (this.mIsCarMode) {
            this.mCarIconScaleXY = 1;
            this.mCarIconAlpha = 1;
        } else if (1 != this.mChargeSpeed || ChargeUtils.supportWaveChargeAnimation()) {
            int i = this.mChargeSpeed;
            if (2 == i) {
                this.mDoubleScaleXY = 1;
                this.mDoubleAlpha = 1;
            } else if (3 == i) {
                this.mYellowDoubleScaleXY = 1;
                this.mYellowDoubleAlpha = 1;
            } else if (i == 0 && ChargeUtils.sBatteryStatus.wireState == 10) {
                Log.d("MiuiChargeIconView", "switchChargeIcon: mWireState=WIRELESS");
                this.mSingleScaleXY = 1;
                this.mSingleAlpha = 1;
            }
        } else {
            this.mSingleScaleXY = 1;
            this.mSingleAlpha = 1;
        }
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, this.mSingleLightningIcon.getScaleX(), (float) this.mSingleScaleXY);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, this.mSingleLightningIcon.getScaleY(), (float) this.mSingleScaleXY);
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, this.mSingleLightningIcon.getAlpha(), (float) this.mSingleAlpha);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mSingleLightningIcon, ofFloat, ofFloat2, ofFloat3).setDuration(500L);
        duration.setInterpolator(this.mCubicInterpolator);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property3, this.mDoubleLightningIcon.getScaleX(), (float) this.mDoubleScaleXY);
        PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property2, this.mDoubleLightningIcon.getScaleY(), (float) this.mDoubleScaleXY);
        PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property, this.mDoubleLightningIcon.getAlpha(), (float) this.mDoubleAlpha);
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mDoubleLightningIcon, ofFloat4, ofFloat5, ofFloat6).setDuration(500L);
        duration2.setInterpolator(this.mCubicInterpolator);
        duration2.setInterpolator(new OvershootInterpolator(3.0f));
        PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property3, this.mSpecialDoubleLightningIcon.getScaleX(), (float) this.mYellowDoubleScaleXY);
        PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property2, this.mSpecialDoubleLightningIcon.getScaleY(), (float) this.mYellowDoubleScaleXY);
        PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property, this.mSpecialDoubleLightningIcon.getAlpha(), (float) this.mYellowDoubleAlpha);
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mSpecialDoubleLightningIcon, ofFloat7, ofFloat8, ofFloat9).setDuration(500L);
        duration3.setInterpolator(this.mCubicInterpolator);
        duration3.setInterpolator(new OvershootInterpolator(3.0f));
        PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property3, this.mCarModeIcon.getScaleX(), (float) this.mCarIconScaleXY);
        PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property2, this.mCarModeIcon.getScaleY(), (float) this.mCarIconScaleXY);
        PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property, this.mCarModeIcon.getAlpha(), (float) this.mCarIconAlpha);
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mCarModeIcon, ofFloat10, ofFloat11, ofFloat12).setDuration(500L);
        duration4.setInterpolator(this.mCubicInterpolator);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mSwitchAnimator = animatorSet2;
        animatorSet2.setStartDelay((long) ChargeUtils.getWaveItemDelayTime());
        this.mSwitchAnimator.playTogether(duration, duration2, duration3, duration4);
        this.mSwitchAnimator.start();
    }

    private void resetIconViewState() {
        Log.d("MiuiChargeIconView", "resetLightingViewState: mChargeSpeed=" + this.mChargeSpeed + ",mIsCarMode=" + this.mIsCarMode);
        resetAllViewState();
        if (this.mIsCarMode) {
            this.mCarModeIcon.setScaleX(1.0f);
            this.mCarModeIcon.setScaleY(1.0f);
            return;
        }
        int i = this.mChargeSpeed;
        if (1 == i) {
            if (!ChargeUtils.supportWaveChargeAnimation()) {
                this.mSingleLightningIcon.setScaleY(1.0f);
                this.mSingleLightningIcon.setScaleX(1.0f);
                this.mSingleLightningIcon.setAlpha(1.0f);
            }
        } else if (2 == i) {
            this.mDoubleLightningIcon.setScaleY(1.0f);
            this.mDoubleLightningIcon.setScaleX(1.0f);
            this.mDoubleLightningIcon.setAlpha(1.0f);
        } else if (3 == i) {
            this.mSpecialDoubleLightningIcon.setScaleY(1.0f);
            this.mSpecialDoubleLightningIcon.setScaleX(1.0f);
            this.mSpecialDoubleLightningIcon.setAlpha(1.0f);
        } else if (i == 0 && ChargeUtils.sBatteryStatus.wireState == 10) {
            Log.d("MiuiChargeIconView", "resetIconViewState: mWireState=WIRELESS");
            this.mSingleLightningIcon.setScaleY(1.0f);
            this.mSingleLightningIcon.setScaleX(1.0f);
            this.mSingleLightningIcon.setAlpha(1.0f);
        }
    }

    private void resetAllViewState() {
        this.mSingleLightningIcon.setAlpha(0.0f);
        this.mSingleLightningIcon.setScaleY(0.0f);
        this.mSingleLightningIcon.setScaleX(0.0f);
        this.mDoubleLightningIcon.setAlpha(0.0f);
        this.mDoubleLightningIcon.setScaleY(0.0f);
        this.mDoubleLightningIcon.setScaleX(0.0f);
        this.mSpecialDoubleLightningIcon.setAlpha(0.0f);
        this.mSpecialDoubleLightningIcon.setScaleY(0.0f);
        this.mSpecialDoubleLightningIcon.setScaleX(0.0f);
        this.mCarModeIcon.setScaleX(0.0f);
        this.mCarModeIcon.setScaleY(0.0f);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float f = 1.0f;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        if (this.mIsFoldChargeVideo) {
            if (min <= 1.0f) {
                f = min;
            }
            min = f;
        }
        this.mIconPaddingTop = (int) (275.0f * min);
        this.mPivotX = (int) (100.0f * min);
        Drawable drawable = this.mSingleLightningDrawable;
        if (drawable != null) {
            this.mSingleLightningIconWidth = (int) (((float) drawable.getIntrinsicWidth()) * min);
            this.mSingleLightningIconHeight = (int) (((float) this.mSingleLightningDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable2 = this.mDoubleLightningDrawable;
        if (drawable2 != null) {
            this.mDoubleLightningIconWidth = (int) (((float) drawable2.getIntrinsicWidth()) * min);
            this.mDoubleLightningIconHeight = (int) (((float) this.mDoubleLightningDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable3 = this.mSpecialDoubleLightningDrawable;
        if (drawable3 != null) {
            this.mSpecialDoubleLightningIconWidth = (int) (((float) drawable3.getIntrinsicWidth()) * min);
            this.mSpecialDoubleLightningIconHeight = (int) (((float) this.mSpecialDoubleLightningDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable4 = this.mCarIconDrawable;
        if (drawable4 != null) {
            this.mCarIconWidth = (int) (((float) drawable4.getIntrinsicWidth()) * min);
            this.mCarIconHeight = (int) (min * ((float) this.mCarIconDrawable.getIntrinsicHeight()));
        }
    }

    private void updateLayoutParamForScreenSizeChange() {
        ImageView imageView = this.mSingleLightningIcon;
        if (imageView != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.width = this.mSingleLightningIconWidth;
            int i = this.mSingleLightningIconHeight;
            int i2 = this.mIconPaddingTop;
            layoutParams.height = i + i2;
            this.mSingleLightningIcon.setPadding(0, i2, 0, 0);
            this.mSingleLightningIcon.setPivotX((float) this.mPivotX);
            this.mSingleLightningIcon.setLayoutParams(layoutParams);
        }
        ImageView imageView2 = this.mDoubleLightningIcon;
        if (imageView2 != null) {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) imageView2.getLayoutParams();
            layoutParams2.width = this.mDoubleLightningIconWidth;
            int i3 = this.mDoubleLightningIconHeight;
            int i4 = this.mIconPaddingTop;
            layoutParams2.height = i3 + i4;
            this.mDoubleLightningIcon.setPadding(0, i4, 0, 0);
            this.mDoubleLightningIcon.setPivotX((float) this.mPivotX);
            this.mDoubleLightningIcon.setLayoutParams(layoutParams2);
        }
        ImageView imageView3 = this.mSpecialDoubleLightningIcon;
        if (imageView3 != null) {
            RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) imageView3.getLayoutParams();
            layoutParams3.width = this.mSpecialDoubleLightningIconWidth;
            int i5 = this.mSpecialDoubleLightningIconHeight;
            int i6 = this.mIconPaddingTop;
            layoutParams3.height = i5 + i6;
            this.mSpecialDoubleLightningIcon.setPadding(0, i6, 0, 0);
            this.mSpecialDoubleLightningIcon.setPivotX((float) this.mPivotX);
            this.mSpecialDoubleLightningIcon.setLayoutParams(layoutParams3);
        }
        ImageView imageView4 = this.mCarModeIcon;
        if (imageView4 != null) {
            RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) imageView4.getLayoutParams();
            layoutParams4.width = this.mCarIconWidth;
            int i7 = this.mCarIconHeight;
            int i8 = this.mIconPaddingTop;
            layoutParams4.height = i7 + i8;
            this.mCarModeIcon.setPadding(0, i8, 0, 0);
            this.mCarModeIcon.setPivotX((float) this.mPivotX);
            this.mCarModeIcon.setLayoutParams(layoutParams4);
        }
    }
}
