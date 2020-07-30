package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.SpringAnimationUtils;

public class NavigationHandle extends View {
    private final int mBottom;
    private int mCurrentColor;
    private final int mDarkColor;
    private float mDarkIntensity;
    private boolean mEnableForceLight;
    private final int mLightColor;
    private final Paint mPaint;
    private boolean mPlusTranslationY;
    private final int mRadius;
    private SpringAnimation mSpringAnimation;
    private float mTranslationYFromOLEDHelper;

    public NavigationHandle(Context context) {
        this(context, (AttributeSet) null);
    }

    public NavigationHandle(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDarkIntensity = -1.0f;
        this.mPaint = new Paint();
        Resources resources = context.getResources();
        this.mRadius = resources.getDimensionPixelSize(R.dimen.navigation_handle_radius);
        this.mBottom = resources.getDimensionPixelSize(R.dimen.navigation_handle_bottom);
        this.mLightColor = resources.getColor(R.color.navigation_handle_light_color);
        this.mDarkColor = resources.getColor(R.color.navigation_handle_dark_color);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(this.mLightColor);
        setFocusable(false);
        initSpringAnimation();
    }

    public void initSpringAnimation() {
        SpringForce springForce = new SpringForce();
        springForce.setDampingRatio(0.99f);
        springForce.setStiffness(SpringAnimationUtils.getInstance().calculateStiffFromResponse(0.2f));
        SpringAnimation springAnimation = new SpringAnimation(this, DynamicAnimation.TRANSLATION_Y);
        springAnimation.setSpring(springForce);
        this.mSpringAnimation = springAnimation;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateWidth();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateWidth();
    }

    private void updateWidth() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(R.dimen.navigation_home_handle_width);
        setLayoutParams(layoutParams);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int i = this.mRadius * 2;
        int width = getWidth();
        int i2 = (height - this.mBottom) - i;
        float f = (float) (i2 + i);
        int i3 = this.mRadius;
        canvas.drawRoundRect(0.0f, (float) i2, (float) width, f, (float) i3, (float) i3, this.mPaint);
    }

    public void setDarkIntensity(float f) {
        if (this.mDarkIntensity != f) {
            this.mDarkIntensity = f;
            changeColor(((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mLightColor), Integer.valueOf(this.mDarkColor))).intValue());
        }
    }

    public void setColor(boolean z) {
        changeColor(z ? this.mDarkColor : this.mLightColor);
    }

    public void onGestureLineProgress(float f) {
        if (isAttachedToWindow() && getVisibility() == 0) {
            this.mSpringAnimation.animateToFinalPosition(getModifiedTranslationY((-f) * ((float) ((getHeight() / 2) - this.mRadius))));
        }
    }

    public float getModifiedTranslationY(float f) {
        float height = (float) ((getHeight() / 2) - this.mRadius);
        return Math.max(-height, Math.min(height, f + this.mTranslationYFromOLEDHelper));
    }

    public void updateNavigationHandleFromOLEDHelper() {
        if (isShown()) {
            float translationY = getTranslationY();
            float f = this.mTranslationYFromOLEDHelper;
            float f2 = translationY - f;
            if (this.mPlusTranslationY && f < ((float) this.mRadius)) {
                this.mTranslationYFromOLEDHelper = f + 1.0f;
            } else if (!this.mPlusTranslationY) {
                float f3 = this.mTranslationYFromOLEDHelper;
                if (f3 > ((float) (-this.mRadius))) {
                    this.mTranslationYFromOLEDHelper = f3 - 1.0f;
                }
            }
            if (Math.abs(this.mTranslationYFromOLEDHelper) == ((float) this.mRadius)) {
                this.mPlusTranslationY = !this.mPlusTranslationY;
            }
            this.mSpringAnimation.animateToFinalPosition(getModifiedTranslationY(f2));
        }
    }

    public void resetNavigationHandleFromOLEDHelper() {
        if (isShown()) {
            float translationY = getTranslationY() - this.mTranslationYFromOLEDHelper;
            this.mTranslationYFromOLEDHelper = 0.0f;
            this.mSpringAnimation.animateToFinalPosition(getModifiedTranslationY(translationY));
        }
    }

    public void setEnableForceLight(boolean z) {
        if (this.mEnableForceLight != z) {
            this.mEnableForceLight = z;
            changeColor(this.mCurrentColor);
        }
    }

    private void changeColor(int i) {
        this.mCurrentColor = i;
        if (this.mEnableForceLight) {
            this.mPaint.setColor(this.mLightColor);
        } else {
            this.mPaint.setColor(i);
        }
        invalidate();
    }
}
