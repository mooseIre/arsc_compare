package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.HashSet;

public class KeyButtonRipple extends Drawable {
    private final Interpolator mAlphaExitInterpolator = new PathInterpolator(0.0f, 0.0f, 0.8f, 1.0f);
    private final AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            KeyButtonRipple.this.mRunningAnimations.remove(animator);
            if (KeyButtonRipple.this.mRunningAnimations.isEmpty() && !KeyButtonRipple.this.mPressed) {
                boolean unused = KeyButtonRipple.this.mDrawingHardwareGlow = false;
                KeyButtonRipple.this.invalidateSelf();
            }
        }
    };
    private CanvasProperty<Float> mBottomProp;
    /* access modifiers changed from: private */
    public boolean mDrawingHardwareGlow;
    private Rect mEndRect;
    private float mFirstLeftEnd;
    private float mFirstLeftStart;
    private float mFirstRightEnd;
    private float mFirstRightStart;
    private float mGlowAlpha = 0.0f;
    private float mGlowScale = 1.0f;
    private final Interpolator mInterpolator = new LogInterpolator();
    private CanvasProperty<Float> mLeftProp;
    private int mMaxWidth;
    private CanvasProperty<Paint> mPaintProp;
    /* access modifiers changed from: private */
    public boolean mPressed;
    private CanvasProperty<Float> mRightProp;
    private Paint mRipplePaint;
    /* access modifiers changed from: private */
    public final HashSet<Animator> mRunningAnimations = new HashSet<>();
    private CanvasProperty<Float> mRxProp;
    private CanvasProperty<Float> mRyProp;
    private float mSecondLeftEnd;
    private float mSecondLeftStart;
    private float mSecondRightEnd;
    private float mSecondRightStart;
    private final Interpolator mSineInterpolator = new SineInterpolator();
    private final AnimatorListenerAdapter mSlideAnimatorListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            KeyButtonRipple.this.mRunningAnimations.remove(animator);
            KeyButtonRipple.this.sildeSecondPart();
            if (KeyButtonRipple.this.mRunningAnimations.isEmpty() && !KeyButtonRipple.this.mPressed) {
                boolean unused = KeyButtonRipple.this.mDrawingHardwareGlow = false;
                KeyButtonRipple.this.invalidateSelf();
            }
        }
    };
    private boolean mSlideToRight = false;
    private Rect mStartRect;
    private boolean mSupportHardware;
    private final View mTargetView;
    private final ArrayList<Animator> mTmpArray = new ArrayList<>();
    private CanvasProperty<Float> mTopProp;

    public int getOpacity() {
        return -3;
    }

    public boolean isStateful() {
        return true;
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public KeyButtonRipple(Context context, View view) {
        this.mMaxWidth = context.getResources().getDimensionPixelSize(R.dimen.key_button_ripple_max_width);
        this.mTargetView = view;
    }

    private Paint getRipplePaint() {
        if (this.mRipplePaint == null) {
            this.mRipplePaint = new Paint();
            this.mRipplePaint.setAntiAlias(true);
            this.mRipplePaint.setColor(-3355444);
        }
        return this.mRipplePaint;
    }

    private void drawSoftware(Canvas canvas) {
        if (this.mGlowAlpha > 0.0f) {
            Paint ripplePaint = getRipplePaint();
            ripplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
            float width = (float) getBounds().width();
            float height = (float) getBounds().height();
            boolean z = width > height;
            float rippleSize = ((float) getRippleSize()) * this.mGlowScale * 0.5f;
            float f = width * 0.5f;
            float f2 = 0.5f * height;
            float f3 = z ? rippleSize : f;
            if (z) {
                rippleSize = f2;
            }
            float f4 = z ? f2 : f;
            canvas.drawRoundRect(f - f3, f2 - rippleSize, f + f3, f2 + rippleSize, f4, f4, ripplePaint);
        }
    }

    public void draw(Canvas canvas) {
        this.mSupportHardware = canvas.isHardwareAccelerated();
        if (this.mSupportHardware) {
            drawHardware((DisplayListCanvas) canvas);
        } else {
            drawSoftware(canvas);
        }
    }

    private boolean isHorizontal() {
        return getBounds().width() > getBounds().height();
    }

    private void drawHardware(DisplayListCanvas displayListCanvas) {
        if (this.mDrawingHardwareGlow) {
            displayListCanvas.drawRoundRect(this.mLeftProp, this.mTopProp, this.mRightProp, this.mBottomProp, this.mRxProp, this.mRyProp, this.mPaintProp);
        }
    }

    public float getGlowAlpha() {
        return this.mGlowAlpha;
    }

    public void setGlowAlpha(float f) {
        this.mGlowAlpha = f;
        invalidateSelf();
    }

    public float getGlowScale() {
        return this.mGlowScale;
    }

    public void setGlowScale(float f) {
        this.mGlowScale = f;
        invalidateSelf();
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] iArr) {
        boolean z;
        int i = 0;
        while (true) {
            if (i >= iArr.length) {
                z = false;
                break;
            } else if (iArr[i] == 16842919) {
                z = true;
                break;
            } else {
                i++;
            }
        }
        if (z == this.mPressed) {
            return false;
        }
        setPressed(z);
        this.mPressed = z;
        return true;
    }

    public void jumpToCurrentState() {
        cancelAnimations();
    }

    public void setPressed(boolean z) {
        if (this.mSupportHardware) {
            setPressedHardware(z);
        } else {
            setPressedSoftware(z);
        }
    }

    private void cancelAnimations() {
        this.mTmpArray.addAll(this.mRunningAnimations);
        int size = this.mTmpArray.size();
        for (int i = 0; i < size; i++) {
            this.mTmpArray.get(i).cancel();
        }
        this.mTmpArray.clear();
        this.mRunningAnimations.clear();
    }

    private void setPressedSoftware(boolean z) {
        if (z) {
            enterSoftware();
        } else {
            exitSoftware();
        }
    }

    private void enterSoftware() {
        cancelAnimations();
        this.mGlowAlpha = 0.25f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "glowScale", new float[]{0.0f, 1.35f});
        ofFloat.setInterpolator(this.mInterpolator);
        ofFloat.setDuration(350);
        ofFloat.addListener(this.mAnimatorListener);
        ofFloat.start();
        this.mRunningAnimations.add(ofFloat);
    }

    private void exitSoftware() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "glowAlpha", new float[]{this.mGlowAlpha, 0.0f});
        ofFloat.setInterpolator(this.mAlphaExitInterpolator);
        ofFloat.setDuration(450);
        ofFloat.addListener(this.mAnimatorListener);
        ofFloat.start();
        this.mRunningAnimations.add(ofFloat);
    }

    private void setPressedHardware(boolean z) {
        if (z) {
            enterHardware();
        } else {
            exitHardware();
        }
    }

    private void setExtendStart(CanvasProperty<Float> canvasProperty) {
        if (isHorizontal()) {
            this.mLeftProp = canvasProperty;
        } else {
            this.mTopProp = canvasProperty;
        }
    }

    private CanvasProperty<Float> getExtendStart() {
        return isHorizontal() ? this.mLeftProp : this.mTopProp;
    }

    private void setExtendEnd(CanvasProperty<Float> canvasProperty) {
        if (isHorizontal()) {
            this.mRightProp = canvasProperty;
        } else {
            this.mBottomProp = canvasProperty;
        }
    }

    private CanvasProperty<Float> getExtendEnd() {
        return isHorizontal() ? this.mRightProp : this.mBottomProp;
    }

    private int getExtendSize() {
        boolean isHorizontal = isHorizontal();
        Rect bounds = getBounds();
        return isHorizontal ? bounds.width() : bounds.height();
    }

    private int getRippleSize() {
        return Math.min(isHorizontal() ? getBounds().width() : getBounds().height(), this.mMaxWidth);
    }

    private void enterHardware() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        setExtendStart(CanvasProperty.createFloat((float) (getExtendSize() / 2)));
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), ((float) (getExtendSize() / 2)) - ((((float) getRippleSize()) * 1.35f) / 2.0f));
        renderNodeAnimator.setDuration(350);
        renderNodeAnimator.setInterpolator(this.mInterpolator);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        setExtendEnd(CanvasProperty.createFloat((float) (getExtendSize() / 2)));
        RenderNodeAnimator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), ((float) (getExtendSize() / 2)) + ((((float) getRippleSize()) * 1.35f) / 2.0f));
        renderNodeAnimator2.setDuration(350);
        renderNodeAnimator2.setInterpolator(this.mInterpolator);
        renderNodeAnimator2.addListener(this.mAnimatorListener);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            this.mTopProp = CanvasProperty.createFloat(0.0f);
            this.mBottomProp = CanvasProperty.createFloat((float) getBounds().height());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
        } else {
            this.mLeftProp = CanvasProperty.createFloat(0.0f);
            this.mRightProp = CanvasProperty.createFloat((float) getBounds().width());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
        }
        this.mGlowScale = 1.35f;
        this.mGlowAlpha = 0.25f;
        this.mRipplePaint = getRipplePaint();
        this.mRipplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        this.mPaintProp = CanvasProperty.createPaint(this.mRipplePaint);
        renderNodeAnimator.start();
        renderNodeAnimator2.start();
        this.mRunningAnimations.add(renderNodeAnimator);
        this.mRunningAnimations.add(renderNodeAnimator2);
        invalidateSelf();
    }

    private void exitHardware() {
        this.mPaintProp = CanvasProperty.createPaint(getRipplePaint());
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(this.mPaintProp, 1, 0.0f);
        renderNodeAnimator.setDuration(450);
        renderNodeAnimator.setInterpolator(this.mAlphaExitInterpolator);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        renderNodeAnimator.start();
        this.mRunningAnimations.add(renderNodeAnimator);
        invalidateSelf();
    }

    private static final class LogInterpolator implements Interpolator {
        private LogInterpolator() {
        }

        public float getInterpolation(float f) {
            return 1.0f - ((float) Math.pow(400.0d, ((double) (-f)) * 1.4d));
        }
    }

    private static final class SineInterpolator implements Interpolator {
        private SineInterpolator() {
        }

        public float getInterpolation(float f) {
            return ((float) (1.0d - Math.cos(((double) f) * 3.141592653589793d))) / 2.0f;
        }
    }

    public void gestureSlideEffect(Rect rect, Rect rect2) {
        this.mStartRect = rect;
        this.mEndRect = rect2;
        int i = this.mStartRect.left;
        int i2 = this.mEndRect.left;
        if (i < i2) {
            this.mSlideToRight = true;
            this.mFirstLeftStart = 0.0f;
            this.mFirstLeftEnd = (((float) getRippleSize()) * -0.35f) / 2.0f;
            Rect rect3 = this.mStartRect;
            int i3 = rect3.right;
            int i4 = rect3.left;
            this.mFirstRightStart = (float) (i3 - i4);
            this.mFirstRightEnd = ((float) (this.mEndRect.right - i4)) + ((((float) getRippleSize()) * 0.35f) / 2.0f);
            this.mSecondLeftStart = (((float) getRippleSize()) * -0.35f) / 2.0f;
            this.mSecondLeftEnd = ((float) (this.mEndRect.left - this.mStartRect.left)) - ((((float) getRippleSize()) * 0.35f) / 2.0f);
            this.mSecondRightStart = ((float) (this.mEndRect.right - this.mStartRect.left)) + ((((float) getRippleSize()) * 0.35f) / 2.0f);
            this.mSecondRightEnd = ((float) (this.mEndRect.right - this.mStartRect.left)) + ((((float) getRippleSize()) * 0.35f) / 2.0f);
        } else {
            this.mSlideToRight = false;
            this.mFirstLeftStart = 0.0f;
            this.mFirstLeftEnd = ((float) (i2 - i)) - ((((float) getRippleSize()) * 0.35f) / 2.0f);
            Rect rect4 = this.mStartRect;
            int i5 = rect4.right;
            int i6 = rect4.left;
            this.mFirstRightStart = (float) (i5 - i6);
            this.mFirstRightEnd = ((float) (i5 - i6)) + ((((float) getRippleSize()) * 0.35f) / 2.0f);
            this.mSecondLeftStart = ((float) (this.mEndRect.left - this.mStartRect.left)) - ((((float) getRippleSize()) * 0.35f) / 2.0f);
            this.mSecondLeftEnd = ((float) (this.mEndRect.left - this.mStartRect.left)) - ((((float) getRippleSize()) * 0.35f) / 2.0f);
            Rect rect5 = this.mStartRect;
            this.mSecondRightStart = ((float) (rect5.right - rect5.left)) + ((((float) getRippleSize()) * 0.35f) / 2.0f);
            this.mSecondRightEnd = ((float) (this.mEndRect.right - this.mStartRect.left)) + ((((float) getRippleSize()) * 0.35f) / 2.0f);
        }
        sildeFirstPart();
    }

    private void sildeFirstPart() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        setExtendStart(CanvasProperty.createFloat(this.mFirstLeftStart));
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), this.mFirstLeftEnd);
        renderNodeAnimator.setDuration(250);
        renderNodeAnimator.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator.addListener(this.mSlideToRight ? this.mAnimatorListener : this.mSlideAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        setExtendEnd(CanvasProperty.createFloat(this.mFirstRightStart));
        RenderNodeAnimator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), this.mFirstRightEnd);
        renderNodeAnimator2.setDuration(250);
        renderNodeAnimator2.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator2.addListener(this.mSlideToRight ? this.mSlideAnimatorListener : this.mAnimatorListener);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            this.mTopProp = CanvasProperty.createFloat(0.0f);
            this.mBottomProp = CanvasProperty.createFloat((float) getBounds().height());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
        } else {
            this.mLeftProp = CanvasProperty.createFloat(0.0f);
            this.mRightProp = CanvasProperty.createFloat((float) getBounds().width());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
        }
        this.mGlowScale = 1.35f;
        this.mRipplePaint = getRipplePaint();
        this.mRipplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        this.mPaintProp = CanvasProperty.createPaint(this.mRipplePaint);
        renderNodeAnimator.start();
        renderNodeAnimator2.start();
        this.mRunningAnimations.add(renderNodeAnimator);
        this.mRunningAnimations.add(renderNodeAnimator2);
        invalidateSelf();
    }

    /* access modifiers changed from: private */
    public void sildeSecondPart() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        setExtendStart(CanvasProperty.createFloat(this.mSecondLeftStart));
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), this.mSecondLeftEnd);
        renderNodeAnimator.setDuration(250);
        renderNodeAnimator.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        setExtendEnd(CanvasProperty.createFloat(this.mSecondRightStart));
        RenderNodeAnimator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), this.mSecondRightEnd);
        renderNodeAnimator2.setDuration(250);
        renderNodeAnimator2.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator2.addListener(this.mAnimatorListener);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            this.mTopProp = CanvasProperty.createFloat(0.0f);
            this.mBottomProp = CanvasProperty.createFloat((float) getBounds().height());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
        } else {
            this.mLeftProp = CanvasProperty.createFloat(0.0f);
            this.mRightProp = CanvasProperty.createFloat((float) getBounds().width());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
        }
        this.mGlowScale = 1.35f;
        this.mRipplePaint = getRipplePaint();
        this.mRipplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        this.mPaintProp = CanvasProperty.createPaint(this.mRipplePaint);
        renderNodeAnimator.start();
        renderNodeAnimator2.start();
        this.mRunningAnimations.add(renderNodeAnimator);
        this.mRunningAnimations.add(renderNodeAnimator2);
        invalidateSelf();
    }
}
