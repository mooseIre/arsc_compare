package com.android.systemui.fsgesture;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.miui.systemui.util.HapticFeedBackImpl;

public class GestureBackArrowView extends View {
    private static final Interpolator CUBIC_EASE_OUT_INTERPOLATOR = new DecelerateInterpolator(1.5f);
    private static final Interpolator QUAD_EASE_OUT_INTERPOLATOR = new DecelerateInterpolator();
    private Bitmap mArrow;
    private ValueAnimator mArrowAnimator;
    private Rect mArrowDstRect;
    private int mArrowHeight;
    private Paint mArrowPaint;
    private boolean mArrowShown;
    private int mArrowWidth;
    private Rect mBackDstRect;
    private int mBackHeight;
    private int mBackWidth;
    private Paint mBgPaint;
    private ContentResolver mContentResolver;
    private int mCurArrowAlpha;
    private float mCurrentY;
    private int mDisplayWidth;
    private float mExpectBackHeight;
    private int mIconHeight;
    private boolean mIconNeedDraw;
    private float mIconScale;
    private int mIconWidth;
    private KeyguardManager mKeyguardManager;
    private Configuration mLastConfiguration;
    private ValueAnimator mLastIconAnimator;
    private Bitmap mLeftBackground;
    private Drawable mNoneTaskIcon;
    private float mOffsetX;
    private int mPosition;
    private ReadyState mReadyState;
    private Drawable mRecentTaskIcon;
    private Bitmap mRightBackground;
    private float mScale;
    private float mStartX;
    private Vibrator mVibrator;
    private ValueAnimator mWaveChangeAnimator;

    /* access modifiers changed from: package-private */
    public enum ReadyState {
        READY_STATE_NONE,
        READY_STATE_BACK,
        READY_STATE_RECENT
    }

    public GestureBackArrowView(Context context, int i) {
        this(context, null, i);
    }

    public GestureBackArrowView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, 0, i);
    }

    public GestureBackArrowView(Context context, AttributeSet attributeSet, int i, int i2) {
        this(context, attributeSet, i, 0, i2);
    }

    public GestureBackArrowView(Context context, AttributeSet attributeSet, int i, int i2, int i3) {
        super(context, attributeSet, i, i2);
        this.mScale = 0.0f;
        this.mIconScale = 1.0f;
        this.mReadyState = ReadyState.READY_STATE_NONE;
        Configuration configuration = new Configuration();
        this.mLastConfiguration = configuration;
        configuration.updateFrom(getResources().getConfiguration());
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.mContentResolver = context.getContentResolver();
        this.mPosition = i3;
        Paint paint = new Paint(1);
        this.mBgPaint = paint;
        paint.setFilterBitmap(true);
        this.mBgPaint.setDither(true);
        Paint paint2 = new Paint(1);
        this.mArrowPaint = paint2;
        paint2.setFilterBitmap(true);
        this.mArrowPaint.setDither(true);
        this.mArrowPaint.setAlpha(0);
        loadResources();
        int i4 = this.mPosition;
        if (i4 == 0) {
            this.mBackHeight = this.mLeftBackground.getHeight();
            this.mBackWidth = this.mLeftBackground.getWidth();
        } else if (i4 == 1) {
            this.mBackHeight = this.mRightBackground.getHeight();
            this.mBackWidth = this.mRightBackground.getWidth();
        }
        this.mBackDstRect = new Rect();
        this.mArrowDstRect = new Rect();
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
    }

    private void loadResources() {
        this.mLeftBackground = BitmapFactory.decodeResource(getContext().getResources(), C0013R$drawable.gesture_back_background);
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f, 1.0f);
        matrix.postRotate(180.0f);
        Bitmap bitmap = this.mLeftBackground;
        this.mRightBackground = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), this.mLeftBackground.getHeight(), matrix, true);
        Drawable drawable = getContext().getDrawable(C0013R$drawable.ic_quick_switch_empty);
        this.mNoneTaskIcon = drawable;
        this.mIconWidth = drawable.getIntrinsicWidth();
        this.mIconHeight = this.mNoneTaskIcon.getIntrinsicHeight();
        Bitmap decodeResource = BitmapFactory.decodeResource(getContext().getResources(), C0013R$drawable.gesture_back_arrow);
        this.mArrow = decodeResource;
        this.mArrowHeight = decodeResource.getHeight();
        this.mArrowWidth = this.mArrow.getWidth();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if ((this.mLastConfiguration.updateFrom(configuration) & Integer.MIN_VALUE) != 0) {
            loadResources();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        super.onDraw(canvas);
        Bitmap bitmap = this.mLeftBackground;
        int i7 = this.mBackWidth;
        float f = this.mScale;
        float f2 = ((float) i7) * f;
        int i8 = this.mPosition;
        if (i8 == 0) {
            float f3 = this.mStartX;
            int i9 = (int) f3;
            i3 = (int) (f2 + f3);
            int i10 = this.mArrowWidth;
            float f4 = this.mIconScale;
            int i11 = (int) (((f2 - (((float) i10) * f4)) / 2.0f) + f3);
            int i12 = (int) ((((((float) i10) * f4) + f2) / 2.0f) + f3);
            int i13 = this.mIconWidth;
            if (f2 >= ((float) i13) * f4) {
                f2 = (f2 + (((float) i13) * f4)) / 2.0f;
            }
            i5 = (int) (f3 + f2);
            i = i11;
            i2 = i12;
            i4 = (int) (((float) i5) - (((float) this.mIconWidth) * this.mIconScale));
            i6 = i9;
        } else if (i8 != 1) {
            i6 = 0;
            i5 = 0;
            i4 = 0;
            i3 = 0;
            i2 = 0;
            i = 0;
        } else {
            bitmap = this.mRightBackground;
            int i14 = this.mDisplayWidth;
            float f5 = ((float) i7) * f;
            float f6 = this.mStartX;
            i6 = i14 - ((int) (f5 + f6));
            i3 = i14 - ((int) f6);
            int i15 = this.mArrowWidth;
            float f7 = this.mIconScale;
            i = i14 - ((int) ((((((float) i15) * f7) + f2) / 2.0f) + f6));
            i2 = i14 - ((int) (((f2 - (((float) i15) * f7)) / 2.0f) + f6));
            int i16 = this.mIconWidth;
            if (f2 >= ((float) i16) * f7) {
                f2 = (f2 + (((float) i16) * f7)) / 2.0f;
            }
            i4 = i14 - ((int) (f6 + f2));
            i5 = (int) (((float) i4) + (((float) this.mIconWidth) * this.mIconScale));
        }
        Rect rect = this.mBackDstRect;
        float f8 = this.mCurrentY;
        float f9 = this.mExpectBackHeight;
        rect.set(i6, (int) (f8 - (f9 / 2.0f)), i3, (int) (f8 + (f9 / 2.0f)));
        canvas.drawBitmap(bitmap, (Rect) null, this.mBackDstRect, this.mBgPaint);
        ReadyState readyState = this.mReadyState;
        if (readyState == ReadyState.READY_STATE_BACK || readyState == ReadyState.READY_STATE_RECENT) {
            if (!this.mArrowShown) {
                this.mIconNeedDraw = true;
                startArrowAnimating(true, 100);
                this.mArrowShown = true;
            }
        } else if (this.mArrowShown) {
            startArrowAnimating(false, 50);
            this.mArrowShown = false;
        }
        if (this.mIconNeedDraw) {
            float f10 = this.mScale;
            if (((double) f10) <= 0.1d) {
                return;
            }
            if (this.mReadyState == ReadyState.READY_STATE_BACK) {
                Rect rect2 = this.mArrowDstRect;
                float f11 = this.mCurrentY;
                int i17 = this.mArrowHeight;
                float f12 = this.mIconScale;
                rect2.set(i, (int) (f11 - ((((float) i17) * f12) / 2.0f)), i2, (int) (f11 + ((((float) i17) * f12) / 2.0f)));
                canvas.drawBitmap(this.mArrow, (Rect) null, this.mArrowDstRect, this.mArrowPaint);
                return;
            }
            Drawable drawable = this.mRecentTaskIcon;
            if (drawable != null && f10 != 0.0f) {
                float f13 = this.mCurrentY;
                int i18 = this.mIconHeight;
                float f14 = this.mIconScale;
                drawable.setBounds(i4, (int) (f13 - ((((float) i18) * f14) / 2.0f)), i5, (int) (f13 + ((((float) i18) * f14) / 2.0f)));
                this.mRecentTaskIcon.draw(canvas);
            }
        }
    }

    private void startArrowAnimating(final boolean z, int i) {
        ValueAnimator valueAnimator = this.mArrowAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        int[] iArr = new int[2];
        int i2 = 0;
        iArr[0] = this.mCurArrowAlpha;
        if (z) {
            i2 = 255;
        }
        iArr[1] = i2;
        ValueAnimator ofInt = ValueAnimator.ofInt(iArr);
        this.mArrowAnimator = ofInt;
        ofInt.setDuration((long) i);
        this.mArrowAnimator.setInterpolator(CUBIC_EASE_OUT_INTERPOLATOR);
        this.mArrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.fsgesture.GestureBackArrowView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                GestureBackArrowView.this.mArrowPaint.setAlpha(intValue);
                GestureBackArrowView.this.invalidate();
                if (intValue == 0 && !z) {
                    GestureBackArrowView.this.mIconNeedDraw = false;
                }
                GestureBackArrowView.this.mCurArrowAlpha = intValue;
            }
        });
        this.mArrowAnimator.start();
    }

    /* access modifiers changed from: package-private */
    public void setReadyFinish(ReadyState readyState) {
        if (readyState == ReadyState.READY_STATE_RECENT) {
            Drawable drawable = this.mRecentTaskIcon;
            if (drawable == null || drawable == this.mNoneTaskIcon) {
                this.mRecentTaskIcon = loadRecentTaskIcon();
            }
        } else {
            this.mRecentTaskIcon = null;
        }
        ReadyState readyState2 = this.mReadyState;
        if (readyState != readyState2) {
            if (readyState2 == ReadyState.READY_STATE_BACK && readyState == ReadyState.READY_STATE_RECENT) {
                changeScale(this.mScale, 1.17f, 200, false);
                if (HapticFeedBackImpl.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).hapticFeedback("switch", false);
                } else {
                    this.mVibrator.vibrate(20);
                }
            } else if (this.mReadyState == ReadyState.READY_STATE_RECENT) {
                changeScale(this.mScale, 1.0f, 200, true);
            }
            this.mReadyState = readyState;
        }
    }

    private void changeScale(final float f, float f2, int i, final boolean z) {
        ValueAnimator valueAnimator = this.mWaveChangeAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
        this.mWaveChangeAnimator = ofFloat;
        ofFloat.setDuration((long) i);
        this.mWaveChangeAnimator.setInterpolator(CUBIC_EASE_OUT_INTERPOLATOR);
        this.mWaveChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.fsgesture.GestureBackArrowView.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (z) {
                    GestureBackArrowView gestureBackArrowView = GestureBackArrowView.this;
                    gestureBackArrowView.mScale = f + (((GesturesBackController.convertOffset(gestureBackArrowView.mOffsetX) / 20.0f) - f) * valueAnimator.getAnimatedFraction());
                } else {
                    GestureBackArrowView.this.mScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                }
                GestureBackArrowView.this.invalidate();
            }
        });
        this.mWaveChangeAnimator.start();
        ValueAnimator valueAnimator2 = this.mLastIconAnimator;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mLastIconAnimator = ofFloat2;
        ofFloat2.setDuration(100L);
        this.mLastIconAnimator.setInterpolator(QUAD_EASE_OUT_INTERPOLATOR);
        this.mLastIconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.fsgesture.GestureBackArrowView.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (GestureBackArrowView.this.mReadyState == ReadyState.READY_STATE_NONE) {
                    GestureBackArrowView.this.mLastIconAnimator.cancel();
                }
                GestureBackArrowView.this.mIconScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        this.mLastIconAnimator.start();
    }

    private Drawable loadRecentTaskIcon() {
        if (!GestureStubView.supportNextTask(this.mKeyguardManager, this.mContentResolver)) {
            return this.mNoneTaskIcon;
        }
        return this.mNoneTaskIcon;
    }

    /* access modifiers changed from: package-private */
    public void setDisplayWidth(int i) {
        this.mDisplayWidth = i;
    }

    /* access modifiers changed from: package-private */
    public void onActionDown(float f, float f2, float f3) {
        if (f3 > 0.0f) {
            this.mExpectBackHeight = f3;
            this.mCurrentY = f;
        } else {
            this.mExpectBackHeight = (float) this.mBackHeight;
            this.mCurrentY = f - 20.0f;
        }
        this.mStartX = f2;
        this.mArrowPaint.setAlpha(0);
        this.mArrowShown = false;
        this.mIconNeedDraw = false;
    }

    /* access modifiers changed from: package-private */
    public void onActionMove(float f) {
        this.mOffsetX = f;
        if (!skipChangeScaleOnAcitonMove()) {
            this.mScale = GesturesBackController.convertOffset(f) / 20.0f;
            invalidate();
        }
    }

    private boolean skipChangeScaleOnAcitonMove() {
        ValueAnimator valueAnimator;
        return this.mReadyState == ReadyState.READY_STATE_RECENT || ((valueAnimator = this.mWaveChangeAnimator) != null && valueAnimator.isRunning());
    }

    /* access modifiers changed from: package-private */
    public void onActionUp(float f, Animator.AnimatorListener animatorListener) {
        ValueAnimator valueAnimator = this.mArrowAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimator2 = this.mWaveChangeAnimator;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
        ValueAnimator valueAnimator3 = this.mLastIconAnimator;
        if (valueAnimator3 != null) {
            valueAnimator3.cancel();
        }
        this.mIconScale = 1.0f;
        float f2 = f / 20.0f;
        this.mScale = f2;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f2, 0.0f);
        ofFloat.setDuration(100L);
        ofFloat.setInterpolator(QUAD_EASE_OUT_INTERPOLATOR);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.fsgesture.GestureBackArrowView.AnonymousClass4 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                GestureBackArrowView.this.mScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                long currentPlayTime = valueAnimator.getCurrentPlayTime();
                if (currentPlayTime > 0 && currentPlayTime < 50) {
                    GestureBackArrowView gestureBackArrowView = GestureBackArrowView.this;
                    gestureBackArrowView.mArrowShown = false;
                    gestureBackArrowView.mIconNeedDraw = false;
                }
                GestureBackArrowView.this.invalidate();
            }
        });
        if (animatorListener != null) {
            ofFloat.addListener(animatorListener);
        }
        ofFloat.start();
        this.mReadyState = ReadyState.READY_STATE_NONE;
    }
}
