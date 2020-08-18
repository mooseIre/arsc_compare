package com.android.keyguard.charge.rapid;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.plugins.R;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;

public class GTChargeAniView extends RelativeLayout {
    private AnimatorSet animatorSet;
    private Interpolator cubicEaseOutInterpolator;
    private ImageView mChargeIcon;
    private Drawable mChargeIconDrawable;
    private int mChargeIconHeight;
    private int mChargeIconWidth;
    private Context mContext;
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
    private int mWirelessStrongChargeIconWidth;

    public GTChargeAniView(Context context) {
        this(context, (AttributeSet) null);
    }

    public GTChargeAniView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GTChargeAniView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.cubicEaseOutInterpolator = new CubicEaseOutInterpolater();
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setLayoutDirection(0);
        this.mChargeIconDrawable = context.getDrawable(R.drawable.charge_animation_charge_icon);
        this.mTurboIconDrawable = context.getDrawable(R.drawable.charge_animation_turbo_icon);
        this.mTurboTailIconDrawable = context.getDrawable(R.drawable.charge_animation_turbo_tail_icon);
        this.mWiredStrongChargeIconDrawable = context.getDrawable(R.drawable.charge_animation_wired_strong_charge_icon);
        this.mWirelessStrongChargeIconDrawable = context.getDrawable(R.drawable.charge_animation_wireless_strong_charge_icon);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        this.mChargeIcon = new ImageView(context);
        this.mChargeIcon.setImageDrawable(this.mChargeIconDrawable);
        this.mChargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(this.mChargeIconWidth, this.mChargeIconHeight);
        layoutParams.addRule(9);
        addView(this.mChargeIcon, layoutParams);
        this.mTailIcon = new ImageView(context);
        this.mTailIcon.setId(View.generateViewId());
        this.mTailIcon.setImageDrawable(this.mTurboTailIconDrawable);
        this.mTailIcon.setPivotX((float) this.mTailIconWidth);
        this.mTailIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(this.mTailIconWidth, this.mTailIconHeight);
        layoutParams2.addRule(9);
        addView(this.mTailIcon, layoutParams2);
        this.mTurboIcon = new ImageView(context);
        this.mTurboIcon.setImageDrawable(this.mTurboIconDrawable);
        this.mTurboIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(this.mTurboIconWidth, this.mTurboIconHeight);
        layoutParams3.addRule(1, this.mChargeIcon.getId());
        layoutParams3.leftMargin = this.mChargeIconWidth + 10;
        addView(this.mTurboIcon, layoutParams3);
        this.mWiredStrongChargeIcon = new ImageView(context);
        this.mWiredStrongChargeIcon.setId(View.generateViewId());
        this.mWiredStrongChargeIcon.setImageDrawable(this.mWiredStrongChargeIconDrawable);
        this.mWiredStrongChargeIcon.setPivotX((float) this.mWiredStrongChargeIconWidth);
        this.mWiredStrongChargeIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(this.mWiredStrongChargeIconWidth, this.mWiredStrongChargeIconHeight);
        layoutParams4.addRule(14);
        addView(this.mWiredStrongChargeIcon, layoutParams4);
        this.mWirelessStrongChargeIcon = new ImageView(context);
        this.mWirelessStrongChargeIcon.setId(View.generateViewId());
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
        AnimatorSet animatorSet2 = this.animatorSet;
        if (animatorSet2 != null) {
            animatorSet2.cancel();
        }
        setViewInitState();
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(RelativeLayout.ALPHA, new float[]{0.0f, 1.0f});
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(RelativeLayout.ALPHA, new float[]{1.0f, 0.0f});
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(RelativeLayout.SCALE_X, new float[]{1.0f, 0.0f});
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mChargeIcon, new PropertyValuesHolder[]{ofFloat}).setDuration(300);
        duration.setInterpolator(this.cubicEaseOutInterpolator);
        PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(RelativeLayout.TRANSLATION_X, new float[]{(float) (-this.mTranslation), 0.0f});
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mTurboIcon, new PropertyValuesHolder[]{ofFloat4}).setDuration(300);
        duration2.setInterpolator(this.cubicEaseOutInterpolator);
        ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mTailIcon, new PropertyValuesHolder[]{ofFloat4}).setDuration(300);
        duration3.setInterpolator(this.cubicEaseOutInterpolator);
        ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mTailIcon, new PropertyValuesHolder[]{ofFloat2, ofFloat3}).setDuration(100);
        duration4.setInterpolator(this.cubicEaseOutInterpolator);
        this.animatorSet = new AnimatorSet();
        this.animatorSet.playTogether(new Animator[]{duration, duration2, duration3});
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

    public void setStrongViewInitState() {
        this.mChargeIcon.setAlpha(0.0f);
        this.mTailIcon.setAlpha(0.0f);
        this.mTurboIcon.setAlpha(0.0f);
        this.mWiredStrongChargeIcon.setAlpha(0.0f);
        this.mWirelessStrongChargeIcon.setAlpha(0.0f);
    }

    public void animationWiredStrongToShow() {
        setStrongViewInitState();
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(RelativeLayout.ALPHA, new float[]{0.0f, 1.0f});
        PropertyValuesHolder.ofFloat(RelativeLayout.ALPHA, new float[]{1.0f, 0.0f});
        PropertyValuesHolder.ofFloat(RelativeLayout.SCALE_X, new float[]{1.0f, 0.0f});
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this.mWiredStrongChargeIcon, new PropertyValuesHolder[]{ofFloat}).setDuration(300);
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
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mTailIcon.getLayoutParams();
        int i = this.mTailIconWidth;
        layoutParams2.width = i;
        layoutParams2.height = this.mTailIconHeight;
        this.mTailIcon.setPivotX((float) i);
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mTurboIcon.getLayoutParams();
        int i2 = this.mTurboIconWidth;
        layoutParams3.width = i2;
        layoutParams3.height = this.mTurboIconHeight;
        layoutParams3.leftMargin = (-i2) / 15;
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mWiredStrongChargeIcon.getLayoutParams();
        int i3 = this.mWiredStrongChargeIconWidth;
        layoutParams4.width = i3;
        layoutParams4.height = this.mWiredStrongChargeIconHeight;
        this.mWiredStrongChargeIcon.setPivotX((float) i3);
        RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mWirelessStrongChargeIcon.getLayoutParams();
        int i4 = this.mWirelessStrongChargeIconWidth;
        layoutParams5.width = i4;
        layoutParams5.height = this.mWirelessStrongChargeIconHeight;
        this.mWirelessStrongChargeIcon.setPivotX((float) i4);
    }
}
