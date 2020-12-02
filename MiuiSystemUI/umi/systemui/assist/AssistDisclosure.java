package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;

public class AssistDisclosure {
    private final Context mContext;
    private final Handler mHandler;
    private Runnable mShowRunnable = new Runnable() {
        public void run() {
            AssistDisclosure.this.show();
        }
    };
    private AssistDisclosureView mView;
    private boolean mViewAdded;
    private final WindowManager mWm;

    public AssistDisclosure(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWm = (WindowManager) context.getSystemService(WindowManager.class);
    }

    public void postShow() {
        this.mHandler.removeCallbacks(this.mShowRunnable);
        this.mHandler.post(this.mShowRunnable);
    }

    /* access modifiers changed from: private */
    public void show() {
        if (this.mView == null) {
            this.mView = new AssistDisclosureView(this.mContext);
        }
        if (!this.mViewAdded) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2015, 525576, -3);
            layoutParams.setTitle("AssistDisclosure");
            layoutParams.setFitInsetsTypes(0);
            this.mWm.addView(this.mView, layoutParams);
            this.mViewAdded = true;
        }
    }

    /* access modifiers changed from: private */
    public void hide() {
        if (this.mViewAdded) {
            this.mWm.removeView(this.mView);
            this.mViewAdded = false;
        }
    }

    private class AssistDisclosureView extends View implements ValueAnimator.AnimatorUpdateListener {
        private int mAlpha = 0;
        private final ValueAnimator mAlphaInAnimator;
        private final ValueAnimator mAlphaOutAnimator;
        private final AnimatorSet mAnimator;
        private final Paint mPaint = new Paint();
        private final Paint mShadowPaint = new Paint();
        private float mShadowThickness;
        private float mThickness;

        public AssistDisclosureView(Context context) {
            super(context);
            ValueAnimator duration = ValueAnimator.ofInt(new int[]{0, 222}).setDuration(400);
            this.mAlphaInAnimator = duration;
            duration.addUpdateListener(this);
            this.mAlphaInAnimator.setInterpolator(Interpolators.CUSTOM_40_40);
            ValueAnimator duration2 = ValueAnimator.ofInt(new int[]{222, 0}).setDuration(300);
            this.mAlphaOutAnimator = duration2;
            duration2.addUpdateListener(this);
            this.mAlphaOutAnimator.setInterpolator(Interpolators.CUSTOM_40_40);
            AnimatorSet animatorSet = new AnimatorSet();
            this.mAnimator = animatorSet;
            animatorSet.play(this.mAlphaInAnimator).before(this.mAlphaOutAnimator);
            this.mAnimator.addListener(new AnimatorListenerAdapter(AssistDisclosure.this) {
                boolean mCancelled;

                public void onAnimationStart(Animator animator) {
                    this.mCancelled = false;
                }

                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    if (!this.mCancelled) {
                        AssistDisclosure.this.hide();
                    }
                }
            });
            PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
            this.mPaint.setColor(-1);
            this.mPaint.setXfermode(porterDuffXfermode);
            this.mShadowPaint.setColor(-12303292);
            this.mShadowPaint.setXfermode(porterDuffXfermode);
            this.mThickness = getResources().getDimension(C0012R$dimen.assist_disclosure_thickness);
            this.mShadowThickness = getResources().getDimension(C0012R$dimen.assist_disclosure_shadow_thickness);
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            startAnimation();
            sendAccessibilityEvent(16777216);
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mAnimator.cancel();
            this.mAlpha = 0;
        }

        private void startAnimation() {
            this.mAnimator.cancel();
            this.mAnimator.start();
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            this.mPaint.setAlpha(this.mAlpha);
            this.mShadowPaint.setAlpha(this.mAlpha / 4);
            drawGeometry(canvas, this.mShadowPaint, this.mShadowThickness);
            drawGeometry(canvas, this.mPaint, 0.0f);
        }

        private void drawGeometry(Canvas canvas, Paint paint, float f) {
            int width = getWidth();
            int height = getHeight();
            float f2 = this.mThickness;
            float f3 = (float) height;
            float f4 = f3 - f2;
            float f5 = (float) width;
            Canvas canvas2 = canvas;
            Paint paint2 = paint;
            float f6 = f;
            drawBeam(canvas2, 0.0f, f4, f5, f3, paint2, f6);
            float f7 = f4;
            drawBeam(canvas2, 0.0f, 0.0f, f2, f7, paint2, f6);
            float f8 = f5 - f2;
            drawBeam(canvas2, f8, 0.0f, f5, f7, paint2, f6);
            drawBeam(canvas2, f2, 0.0f, f8, f2, paint2, f6);
        }

        private void drawBeam(Canvas canvas, float f, float f2, float f3, float f4, Paint paint, float f5) {
            canvas.drawRect(f - f5, f2 - f5, f3 + f5, f4 + f5, paint);
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            ValueAnimator valueAnimator2 = this.mAlphaOutAnimator;
            if (valueAnimator == valueAnimator2) {
                this.mAlpha = ((Integer) valueAnimator2.getAnimatedValue()).intValue();
            } else {
                ValueAnimator valueAnimator3 = this.mAlphaInAnimator;
                if (valueAnimator == valueAnimator3) {
                    this.mAlpha = ((Integer) valueAnimator3.getAnimatedValue()).intValue();
                }
            }
            invalidate();
        }
    }
}
