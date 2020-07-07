package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.plugins.R;
import java.lang.ref.WeakReference;

public class AntiMistakeTouchView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final boolean DEBUG = Log.isLoggable("AntiMistakeTouchView", 3);
    private Drawable mDrawable;
    private int mDrawableHeight;
    private int mDrawableWidth;
    private H mHandler;
    private Rect mRect;
    private ValueAnimator mSlideAnimator;
    private int mTopMargin;

    private static class H extends Handler {
        private WeakReference<AntiMistakeTouchView> mRef;

        private H(AntiMistakeTouchView antiMistakeTouchView) {
            this.mRef = new WeakReference<>(antiMistakeTouchView);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            WeakReference<AntiMistakeTouchView> weakReference = this.mRef;
            if (weakReference != null && weakReference.get() != null && message.what == 191) {
                ((AntiMistakeTouchView) this.mRef.get()).slideDown();
            }
        }
    }

    public AntiMistakeTouchView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AntiMistakeTouchView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AntiMistakeTouchView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHandler = new H();
        this.mDrawable = getResources().getDrawable(R.drawable.anti_touch_bar);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mDrawableWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mDrawableHeight = (int) (displayMetrics.density * 10.0f);
        if (DEBUG) {
            Log.i("AntiMistakeTouchView", "AntiMistakeTouchView: " + this.mDrawableWidth + " " + this.mDrawableHeight);
        }
        this.mTopMargin = this.mDrawableHeight;
        int i2 = this.mTopMargin;
        this.mRect = new Rect(0, i2, this.mDrawableWidth, this.mDrawableHeight + i2);
        setVisibility(8);
        setEnabled(false);
        setClickable(false);
    }

    public boolean containsLocation(float f) {
        int[] iArr = new int[2];
        getLocationOnScreen(iArr);
        if (DEBUG) {
            Log.i("AntiMistakeTouchView", "contains: " + f + " " + iArr[0]);
        }
        float f2 = f - ((float) iArr[0]);
        if (f2 < 0.01f || f2 > ((float) this.mDrawableWidth)) {
            return false;
        }
        return true;
    }

    private void initAnimator() {
        ValueAnimator valueAnimator = new ValueAnimator();
        this.mSlideAnimator = valueAnimator;
        valueAnimator.setDuration(200);
        this.mSlideAnimator.addUpdateListener(this);
    }

    public void slideUp() {
        if (DEBUG) {
            Log.i("AntiMistakeTouchView", "slideUp: ");
        }
        this.mHandler.removeMessages(191);
        ValueAnimator valueAnimator = this.mSlideAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        } else {
            initAnimator();
        }
        this.mSlideAnimator.setIntValues(new int[]{this.mRect.top, 0});
        this.mSlideAnimator.start();
        this.mHandler.sendEmptyMessageDelayed(191, 2000);
    }

    /* access modifiers changed from: private */
    public void slideDown() {
        ValueAnimator valueAnimator = this.mSlideAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        } else {
            initAnimator();
        }
        this.mSlideAnimator.setIntValues(new int[]{this.mRect.top, this.mTopMargin});
        this.mSlideAnimator.start();
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        this.mRect.set(0, intValue, this.mDrawableWidth, this.mDrawableHeight + intValue);
        Drawable drawable = this.mDrawable;
        int i = this.mTopMargin;
        drawable.setAlpha((((i - intValue) * 255) / i) + 0);
        if (DEBUG) {
            Log.i("AntiMistakeTouchView", "onAnimationUpdate: " + intValue);
        }
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mDrawable.setBounds(this.mRect);
        this.mDrawable.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages((Object) null);
    }

    public void updateVisibilityState(int i) {
        if (DEBUG) {
            Log.i("AntiMistakeTouchView", "updateVisibilityState: " + i);
        }
        if (i != getVisibility()) {
            ValueAnimator valueAnimator = this.mSlideAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (i == 0) {
                this.mRect.top = this.mTopMargin;
                this.mDrawable.setAlpha(0);
            }
            setVisibility(i);
        }
    }

    public FrameLayout.LayoutParams getFrameLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(this.mDrawableWidth, this.mDrawableHeight);
        layoutParams.gravity = 81;
        return layoutParams;
    }
}
