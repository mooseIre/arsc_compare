package com.android.keyguard.charge.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.ChargeUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;

public class MiuiChargeTurboView extends RelativeLayout {
    private AnimatorSet animatorSet;
    private Interpolator cubicEaseOutInterpolator;
    private ImageView mChargeIcon;
    private Drawable mChargeIconDrawable;
    private int mChargeIconHeight;
    private int mChargeIconWidth;
    private boolean mIsFoldChargeVideo;
    private Point mScreenSize;
    private ImageView mTailIcon;
    private int mTailIconHeight;
    private int mTailIconWidth;
    private int mTranslation;
    private ImageView mTurboIcon;
    private Drawable mTurboIconDrawable;
    private int mTurboIconHeight;
    private int mTurboIconWidth;
    private Drawable mTurboTailIconDrawable;
    private WindowManager mWindowManager;
    protected ImageView mWiredStrongChargeIcon;
    private Drawable mWiredStrongChargeIconDrawable;
    private int mWiredStrongChargeIconHeight;
    private int mWiredStrongChargeIconWidth;
    protected ImageView mWirelessStrongChargeIcon;
    private Drawable mWirelessStrongChargeIconDrawable;
    private int mWirelessStrongChargeIconHeight;
    private Drawable mWirelessStrongChargeIconSswDrawable;
    private int mWirelessStrongChargeIconWidth;

    public MiuiChargeTurboView(Context context) {
        this(context, null);
    }

    public MiuiChargeTurboView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiChargeTurboView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.cubicEaseOutInterpolator = new CubicEaseOutInterpolater();
        this.mIsFoldChargeVideo = false;
        init(context);
    }

    private void init(Context context) {
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        setLayoutDirection(0);
        this.mChargeIconDrawable = context.getDrawable(C0013R$drawable.charge_animation_charge_icon);
        this.mTurboIconDrawable = context.getDrawable(C0013R$drawable.charge_animation_turbo_icon);
        this.mTurboTailIconDrawable = context.getDrawable(C0013R$drawable.charge_animation_turbo_tail_icon);
        this.mWiredStrongChargeIconDrawable = context.getDrawable(C0013R$drawable.charge_animation_wired_strong_charge_icon);
        this.mWirelessStrongChargeIconDrawable = context.getDrawable(C0013R$drawable.charge_animation_wireless_strong_charge_icon);
        this.mWirelessStrongChargeIconSswDrawable = context.getDrawable(C0013R$drawable.charge_animation_wireless_strong_charge_ssw_icon);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        ImageView imageView = new ImageView(context);
        this.mChargeIcon = imageView;
        imageView.setImageDrawable(this.mChargeIconDrawable);
        this.mChargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(this.mChargeIconWidth, this.mChargeIconHeight);
        layoutParams.addRule(9);
        addView(this.mChargeIcon, layoutParams);
        ImageView imageView2 = new ImageView(context);
        this.mTailIcon = imageView2;
        imageView2.setId(View.generateViewId());
        this.mTailIcon.setImageDrawable(this.mTurboTailIconDrawable);
        this.mTailIcon.setPivotX((float) this.mTailIconWidth);
        this.mTailIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(this.mTailIconWidth, this.mTailIconHeight);
        layoutParams2.addRule(9);
        addView(this.mTailIcon, layoutParams2);
        ImageView imageView3 = new ImageView(context);
        this.mTurboIcon = imageView3;
        imageView3.setImageDrawable(this.mTurboIconDrawable);
        this.mTurboIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(this.mTurboIconWidth, this.mTurboIconHeight);
        layoutParams3.addRule(1, this.mChargeIcon.getId());
        layoutParams3.leftMargin = this.mChargeIconWidth + 10;
        addView(this.mTurboIcon, layoutParams3);
        ImageView imageView4 = new ImageView(context);
        this.mWiredStrongChargeIcon = imageView4;
        imageView4.setId(View.generateViewId());
        this.mWiredStrongChargeIcon.setImageDrawable(this.mWiredStrongChargeIconDrawable);
        this.mWiredStrongChargeIcon.setPivotX((float) this.mWiredStrongChargeIconWidth);
        this.mWiredStrongChargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(this.mWiredStrongChargeIconWidth, this.mWiredStrongChargeIconHeight);
        layoutParams4.addRule(14);
        addView(this.mWiredStrongChargeIcon, layoutParams4);
        ImageView imageView5 = new ImageView(context);
        this.mWirelessStrongChargeIcon = imageView5;
        imageView5.setId(View.generateViewId());
        this.mWirelessStrongChargeIcon.setImageDrawable(this.mWirelessStrongChargeIconDrawable);
        this.mWirelessStrongChargeIcon.setPivotX((float) this.mWirelessStrongChargeIconWidth);
        this.mWirelessStrongChargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(this.mWirelessStrongChargeIconWidth, this.mWirelessStrongChargeIconHeight);
        layoutParams5.addRule(14);
        addView(this.mWirelessStrongChargeIcon, layoutParams5);
        this.mTranslation = this.mTailIconWidth;
    }

    public void setViewInitState() {
        this.mChargeIcon.setAlpha(0.0f);
        this.mTailIcon.setAlpha(1.0f);
        this.mTurboIcon.setAlpha(1.0f);
        this.mTailIcon.setScaleX(1.0f);
        this.mTailIcon.setTranslationX((float) (-this.mTranslation));
        this.mTurboIcon.setTranslationX((float) (-this.mTranslation));
        this.mWiredStrongChargeIcon.setAlpha(0.0f);
        this.mWirelessStrongChargeIcon.setAlpha(0.0f);
    }

    public void setViewShowState() {
        this.mChargeIcon.setAlpha(1.0f);
        this.mTailIcon.setTranslationX(0.0f);
        this.mTurboIcon.setTranslationX(0.0f);
        this.mTailIcon.setAlpha(0.0f);
        this.mTailIcon.setScaleX(0.0f);
        this.mTurboIcon.setAlpha(1.0f);
        this.mWiredStrongChargeIcon.setAlpha(0.0f);
        this.mWirelessStrongChargeIcon.setAlpha(0.0f);
    }

    public void animationToShow() {
        Property property = RelativeLayout.ALPHA;
        AnimatorSet animatorSet2 = this.animatorSet;
        if (animatorSet2 != null) {
            animatorSet2.cancel();
        }
        setViewInitState();
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property, 0.0f, 1.0f);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property, 1.0f, 0.0f);
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(RelativeLayout.SCALE_X, 1.0f, 0.0f);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mChargeIcon, ofFloat).setDuration(300L);
        duration.setInterpolator(this.cubicEaseOutInterpolator);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(RelativeLayout.TRANSLATION_X, (float) (-this.mTranslation), 0.0f);
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mTurboIcon, ofFloat4).setDuration(300L);
        duration2.setInterpolator(this.cubicEaseOutInterpolator);
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mTailIcon, ofFloat4).setDuration(300L);
        duration3.setInterpolator(this.cubicEaseOutInterpolator);
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mTailIcon, ofFloat2, ofFloat3).setDuration(100L);
        duration4.setInterpolator(this.cubicEaseOutInterpolator);
        AnimatorSet animatorSet3 = new AnimatorSet();
        this.animatorSet = animatorSet3;
        animatorSet3.playTogether(duration, duration2, duration3);
        this.animatorSet.play(duration4).after(duration3);
        this.animatorSet.start();
    }

    public void setWiredStrongViewShowState() {
        this.mChargeIcon.setAlpha(0.0f);
        this.mTailIcon.setAlpha(0.0f);
        this.mTurboIcon.setAlpha(0.0f);
        this.mWiredStrongChargeIcon.setAlpha(1.0f);
        this.mWirelessStrongChargeIcon.setAlpha(0.0f);
    }

    public void setWirelessStrongViewShowState() {
        updateWirelessStrongChargeIcon();
        this.mChargeIcon.setAlpha(0.0f);
        this.mTailIcon.setAlpha(0.0f);
        this.mTurboIcon.setAlpha(0.0f);
        this.mWiredStrongChargeIcon.setAlpha(0.0f);
        this.mWirelessStrongChargeIcon.setAlpha(1.0f);
    }

    public void setStrongViewInitState() {
        updateWirelessStrongChargeIcon();
        this.mChargeIcon.setAlpha(0.0f);
        this.mTailIcon.setAlpha(0.0f);
        this.mTurboIcon.setAlpha(0.0f);
        this.mWiredStrongChargeIcon.setAlpha(0.0f);
        this.mWirelessStrongChargeIcon.setAlpha(0.0f);
    }

    private void updateWirelessStrongChargeIcon() {
        if (ChargeUtils.isSupportWirelessStrongChargeSsw()) {
            this.mWirelessStrongChargeIcon.setImageDrawable(this.mWirelessStrongChargeIconSswDrawable);
        } else {
            this.mWirelessStrongChargeIcon.setImageDrawable(this.mWirelessStrongChargeIconDrawable);
        }
    }

    public void animationWiredStrongToShow() {
        setStrongViewInitState();
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(RelativeLayout.ALPHA, 0.0f, 1.0f);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mWiredStrongChargeIcon, ofFloat).setDuration(300L);
        duration.setInterpolator(this.cubicEaseOutInterpolator);
        duration.start();
    }

    public void animationWirelessStrongToShow() {
        setStrongViewInitState();
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(RelativeLayout.ALPHA, 0.0f, 1.0f);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mWirelessStrongChargeIcon, ofFloat).setDuration(300L);
        duration.setInterpolator(this.cubicEaseOutInterpolator);
        duration.start();
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
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        if (this.mIsFoldChargeVideo) {
            min = Math.min(min, 1.0f);
        }
        Drawable drawable = this.mChargeIconDrawable;
        if (drawable != null) {
            this.mChargeIconWidth = (int) (((float) drawable.getIntrinsicWidth()) * min);
            this.mChargeIconHeight = (int) (((float) this.mChargeIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable2 = this.mTurboIconDrawable;
        if (drawable2 != null) {
            this.mTurboIconWidth = (int) (((float) drawable2.getIntrinsicWidth()) * min);
            this.mTurboIconHeight = (int) (((float) this.mTurboIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable3 = this.mTurboTailIconDrawable;
        if (drawable3 != null) {
            this.mTailIconWidth = (int) (((float) drawable3.getIntrinsicWidth()) * min);
            this.mTailIconHeight = (int) (((float) this.mTurboTailIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable4 = this.mWiredStrongChargeIconDrawable;
        if (drawable4 != null) {
            this.mWiredStrongChargeIconWidth = (int) (((float) drawable4.getIntrinsicWidth()) * min);
            this.mWiredStrongChargeIconHeight = (int) (((float) this.mWiredStrongChargeIconDrawable.getIntrinsicHeight()) * min);
        }
        Drawable drawable5 = this.mWirelessStrongChargeIconDrawable;
        if (drawable5 != null) {
            this.mWirelessStrongChargeIconWidth = (int) (((float) drawable5.getIntrinsicWidth()) * min);
            this.mWirelessStrongChargeIconHeight = (int) (min * ((float) this.mWirelessStrongChargeIconDrawable.getIntrinsicHeight()));
        }
        this.mTranslation = this.mTailIconWidth;
    }

    private void updateLayoutParamForScreenSizeChange() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mChargeIcon.getLayoutParams();
        layoutParams.width = this.mChargeIconWidth;
        layoutParams.height = this.mChargeIconHeight;
        this.mChargeIcon.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mTailIcon.getLayoutParams();
        int i = this.mTailIconWidth;
        layoutParams2.width = i;
        layoutParams2.height = this.mTailIconHeight;
        this.mTailIcon.setPivotX((float) i);
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mTurboIcon.getLayoutParams();
        layoutParams3.width = this.mTurboIconWidth;
        layoutParams3.height = this.mTurboIconHeight;
        layoutParams3.leftMargin = this.mChargeIconWidth + 10;
        this.mTurboIcon.setLayoutParams(layoutParams3);
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mWiredStrongChargeIcon.getLayoutParams();
        int i2 = this.mWiredStrongChargeIconWidth;
        layoutParams4.width = i2;
        layoutParams4.height = this.mWiredStrongChargeIconHeight;
        this.mWiredStrongChargeIcon.setPivotX((float) i2);
        RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mWirelessStrongChargeIcon.getLayoutParams();
        int i3 = this.mWirelessStrongChargeIconWidth;
        layoutParams5.width = i3;
        layoutParams5.height = this.mWirelessStrongChargeIconHeight;
        this.mWirelessStrongChargeIcon.setPivotX((float) i3);
    }
}
