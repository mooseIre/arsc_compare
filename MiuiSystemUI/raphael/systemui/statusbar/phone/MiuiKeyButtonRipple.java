package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Trace;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;
import java.util.ArrayList;
import java.util.HashSet;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyButtonRipple.kt */
public final class MiuiKeyButtonRipple extends Drawable {
    private final AnimatorListenerAdapter mAnimatorListener;
    private CanvasProperty<Float> mBottomProp;
    private boolean mDark;
    private boolean mDelayTouchFeedback;
    private boolean mDrawingHardwareGlow;
    private Rect mEndRect;
    private final TraceAnimatorListener mEnterHwTraceAnimator;
    private final TraceAnimatorListener mExitHwTraceAnimator;
    private float mFirstLeftEnd;
    private float mFirstLeftStart;
    private float mFirstRightEnd;
    private float mFirstRightStart;
    private float mGlowAlpha;
    private float mGlowScale;
    private final Handler mHandler;
    private final Interpolator mInterpolator;
    private boolean mLastDark;
    private CanvasProperty<Float> mLeftProp;
    private int mMaxWidth;
    private CanvasProperty<Paint> mPaintProp;
    private boolean mPressed;
    private CanvasProperty<Float> mRightProp;
    private Paint mRipplePaint;
    @NotNull
    private final HashSet<Animator> mRunningAnimations;
    private CanvasProperty<Float> mRxProp;
    private CanvasProperty<Float> mRyProp;
    private float mSecondLeftEnd;
    private float mSecondLeftStart;
    private float mSecondRightEnd;
    private float mSecondRightStart;
    private final Interpolator mSineInterpolator;
    private final AnimatorListenerAdapter mSlideAnimatorListener;
    private boolean mSlideToRight;
    private Rect mStartRect;
    private boolean mSupportHardware;
    private View mTargetView;
    private final ArrayList<Animator> mTmpArray;
    private CanvasProperty<Float> mTopProp;
    private Type mType;
    private boolean mVisible;

    /* compiled from: MiuiKeyButtonRipple.kt */
    public enum Type {
        OVAL,
        ROUNDED_RECT
    }

    public int getOpacity() {
        return -3;
    }

    public boolean hasFocusStateSpecified() {
        return true;
    }

    public boolean isStateful() {
        return true;
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    public MiuiKeyButtonRipple(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "mTargetView");
        this.mTargetView = view;
        this.mSineInterpolator = new SineInterpolator();
        this.mGlowScale = 1.0f;
        this.mInterpolator = new LogInterpolator();
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mRunningAnimations = new HashSet<>();
        this.mTmpArray = new ArrayList<>();
        this.mExitHwTraceAnimator = new TraceAnimatorListener("exitHardware");
        this.mEnterHwTraceAnimator = new TraceAnimatorListener("enterHardware");
        this.mType = Type.ROUNDED_RECT;
        this.mAnimatorListener = new MiuiKeyButtonRipple$mAnimatorListener$1(this);
        this.mSlideAnimatorListener = new MiuiKeyButtonRipple$mSlideAnimatorListener$1(this);
    }

    public final boolean getMPressed() {
        return this.mPressed;
    }

    public final void setMDrawingHardwareGlow(boolean z) {
        this.mDrawingHardwareGlow = z;
    }

    @NotNull
    public final HashSet<Animator> getMRunningAnimations() {
        return this.mRunningAnimations;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public MiuiKeyButtonRipple(@NotNull Context context, @NotNull View view) {
        this(view);
        Intrinsics.checkParameterIsNotNull(context, "ctx");
        Intrinsics.checkParameterIsNotNull(view, "targetView");
        this.mMaxWidth = context.getResources().getDimensionPixelSize(C0012R$dimen.key_button_ripple_max_width);
        this.mTargetView = view;
    }

    public final void setDarkIntensity(float f) {
        this.mDark = f >= 0.5f;
    }

    public final void setDelayTouchFeedback(boolean z) {
        this.mDelayTouchFeedback = z;
    }

    public final void setType(@NotNull Type type) {
        Intrinsics.checkParameterIsNotNull(type, "type");
        this.mType = type;
    }

    private final Paint getRipplePaint() {
        if (this.mRipplePaint == null) {
            Paint paint = new Paint();
            this.mRipplePaint = paint;
            if (paint != null) {
                paint.setAntiAlias(true);
                Paint paint2 = this.mRipplePaint;
                if (paint2 != null) {
                    paint2.setColor(this.mLastDark ? -16777216 : -1);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        Paint paint3 = this.mRipplePaint;
        if (paint3 != null) {
            return paint3;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void drawSoftware(Canvas canvas) {
        if (this.mGlowAlpha > 0.0f) {
            Paint ripplePaint = getRipplePaint();
            ripplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
            float width = (float) getBounds().width();
            float height = (float) getBounds().height();
            boolean z = width > height;
            float rippleSize = ((float) getRippleSize()) * this.mGlowScale * 0.5f;
            float f = width * 0.5f;
            float f2 = height * 0.5f;
            float f3 = z ? rippleSize : f;
            if (z) {
                rippleSize = f2;
            }
            float f4 = z ? f2 : f;
            if (this.mType == Type.ROUNDED_RECT) {
                canvas.drawRoundRect(f - f3, f2 - rippleSize, f3 + f, f2 + rippleSize, f4, f4, ripplePaint);
                return;
            }
            canvas.save();
            canvas.translate(f, f2);
            float f5 = RangesKt___RangesKt.coerceAtMost(f3, rippleSize);
            float f6 = -f5;
            canvas.drawOval(f6, f6, f5, f5, ripplePaint);
            canvas.restore();
        }
    }

    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        boolean isHardwareAccelerated = canvas.isHardwareAccelerated();
        this.mSupportHardware = isHardwareAccelerated;
        if (isHardwareAccelerated) {
            drawHardware((RecordingCanvas) canvas);
        } else {
            drawSoftware(canvas);
        }
    }

    private final boolean isHorizontal() {
        return getBounds().width() > getBounds().height();
    }

    private final void drawHardware(RecordingCanvas recordingCanvas) {
        if (!this.mDrawingHardwareGlow) {
            return;
        }
        if (this.mType == Type.ROUNDED_RECT) {
            CanvasProperty<Float> canvasProperty = this.mLeftProp;
            if (canvasProperty != null) {
                CanvasProperty<Float> canvasProperty2 = this.mTopProp;
                if (canvasProperty2 != null) {
                    CanvasProperty<Float> canvasProperty3 = this.mRightProp;
                    if (canvasProperty3 != null) {
                        CanvasProperty<Float> canvasProperty4 = this.mBottomProp;
                        if (canvasProperty4 != null) {
                            CanvasProperty<Float> canvasProperty5 = this.mRxProp;
                            if (canvasProperty5 != null) {
                                CanvasProperty<Float> canvasProperty6 = this.mRyProp;
                                if (canvasProperty6 != null) {
                                    CanvasProperty<Paint> canvasProperty7 = this.mPaintProp;
                                    if (canvasProperty7 != null) {
                                        recordingCanvas.drawRoundRect(canvasProperty, canvasProperty2, canvasProperty3, canvasProperty4, canvasProperty5, canvasProperty6, canvasProperty7);
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("mPaintProp");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("mRyProp");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mRxProp");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mBottomProp");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mRightProp");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mTopProp");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mLeftProp");
                throw null;
            }
        } else {
            float f = (float) 2;
            CanvasProperty createFloat = CanvasProperty.createFloat(((float) getBounds().width()) / f);
            CanvasProperty createFloat2 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            CanvasProperty createFloat3 = CanvasProperty.createFloat((((float) RangesKt___RangesKt.coerceAtMost(getBounds().width(), getBounds().height())) * 1.0f) / f);
            CanvasProperty<Paint> canvasProperty8 = this.mPaintProp;
            if (canvasProperty8 != null) {
                recordingCanvas.drawCircle(createFloat, createFloat2, createFloat3, canvasProperty8);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mPaintProp");
                throw null;
            }
        }
    }

    private final float getMaxGlowAlpha() {
        return this.mLastDark ? 0.1f : 0.25f;
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(@NotNull int[] iArr) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(iArr, "state");
        int length = iArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
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
        endAnimations("jumpToCurrentState", false);
    }

    private final void setPressed(boolean z) {
        boolean z2 = this.mDark;
        if (z2 != this.mLastDark && z) {
            this.mRipplePaint = null;
            this.mLastDark = z2;
        }
        if (this.mSupportHardware) {
            setPressedHardware(z);
        } else {
            setPressedSoftware(z);
        }
    }

    public final void abortDelayedRipple() {
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private final void endAnimations(String str, boolean z) {
        Trace.beginSection("KeyButtonRipple.endAnim: reason=" + str + " cancel=" + z);
        Trace.endSection();
        this.mVisible = false;
        this.mTmpArray.addAll(this.mRunningAnimations);
        int size = this.mTmpArray.size();
        for (int i = 0; i < size; i++) {
            Animator animator = this.mTmpArray.get(i);
            Intrinsics.checkExpressionValueIsNotNull(animator, "mTmpArray[i]");
            Animator animator2 = animator;
            if (z) {
                animator2.cancel();
            } else {
                animator2.end();
            }
        }
        this.mTmpArray.clear();
        this.mRunningAnimations.clear();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private final void setPressedSoftware(boolean z) {
        if (!z) {
            exitSoftware();
        } else if (!this.mDelayTouchFeedback) {
            enterSoftware();
        } else if (this.mRunningAnimations.isEmpty()) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.postDelayed(new MiuiKeyButtonRipple$setPressedSoftware$1(this), (long) ViewConfiguration.getTapTimeout());
        } else if (this.mVisible) {
            enterSoftware();
        }
    }

    /* access modifiers changed from: private */
    public final void enterSoftware() {
        endAnimations("enterSoftware", true);
        this.mVisible = true;
        this.mGlowAlpha = getMaxGlowAlpha();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "glowScale", 0.0f, 1.35f);
        Intrinsics.checkExpressionValueIsNotNull(ofFloat, "scaleAnimator");
        ofFloat.setInterpolator(this.mInterpolator);
        ofFloat.setDuration((long) 350);
        ofFloat.addListener(this.mAnimatorListener);
        ofFloat.start();
        this.mRunningAnimations.add(ofFloat);
        if (this.mDelayTouchFeedback && !this.mPressed) {
            exitSoftware();
        }
    }

    private final void exitSoftware() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "glowAlpha", this.mGlowAlpha, 0.0f);
        Intrinsics.checkExpressionValueIsNotNull(ofFloat, "alphaAnimator");
        ofFloat.setInterpolator(Interpolators.ALPHA_OUT);
        ofFloat.setDuration((long) 450);
        ofFloat.addListener(this.mAnimatorListener);
        ofFloat.start();
        this.mRunningAnimations.add(ofFloat);
    }

    private final void setPressedHardware(boolean z) {
        if (!z) {
            exitHardware();
        } else if (!this.mDelayTouchFeedback) {
            enterHardware();
        } else if (this.mRunningAnimations.isEmpty()) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.postDelayed(new MiuiKeyButtonRipple$setPressedHardware$1(this), (long) ViewConfiguration.getTapTimeout());
        } else if (this.mVisible) {
            enterHardware();
        }
    }

    private final void setExtendStart(CanvasProperty<Float> canvasProperty) {
        if (isHorizontal()) {
            this.mLeftProp = canvasProperty;
        } else {
            this.mTopProp = canvasProperty;
        }
    }

    private final CanvasProperty<Float> getExtendStart() {
        CanvasProperty<Float> canvasProperty;
        if (isHorizontal()) {
            canvasProperty = this.mLeftProp;
            if (canvasProperty == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mLeftProp");
                throw null;
            }
        } else {
            canvasProperty = this.mTopProp;
            if (canvasProperty == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mTopProp");
                throw null;
            }
        }
        return canvasProperty;
    }

    private final void setExtendEnd(CanvasProperty<Float> canvasProperty) {
        if (isHorizontal()) {
            this.mRightProp = canvasProperty;
        } else {
            this.mBottomProp = canvasProperty;
        }
    }

    private final CanvasProperty<Float> getExtendEnd() {
        CanvasProperty<Float> canvasProperty;
        if (isHorizontal()) {
            canvasProperty = this.mRightProp;
            if (canvasProperty == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mRightProp");
                throw null;
            }
        } else {
            canvasProperty = this.mBottomProp;
            if (canvasProperty == null) {
                Intrinsics.throwUninitializedPropertyAccessException("mBottomProp");
                throw null;
            }
        }
        return canvasProperty;
    }

    private final int getExtendSize() {
        boolean isHorizontal = isHorizontal();
        Rect bounds = getBounds();
        return isHorizontal ? bounds.width() : bounds.height();
    }

    private final int getRippleSize() {
        return RangesKt___RangesKt.coerceAtMost(isHorizontal() ? getBounds().width() : getBounds().height(), this.mMaxWidth);
    }

    /* access modifiers changed from: private */
    public final void enterHardware() {
        endAnimations("enterHardware", true);
        this.mVisible = true;
        this.mDrawingHardwareGlow = true;
        float f = (float) 2;
        CanvasProperty<Float> createFloat = CanvasProperty.createFloat(((float) getExtendSize()) / f);
        Intrinsics.checkExpressionValueIsNotNull(createFloat, "CanvasProperty.createFlo…tendSize() / 2.toFloat())");
        setExtendStart(createFloat);
        Animator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), ((float) (getExtendSize() / 2)) - ((((float) getRippleSize()) * 1.35f) / f));
        long j = (long) 350;
        renderNodeAnimator.setDuration(j);
        renderNodeAnimator.setInterpolator(this.mInterpolator);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        CanvasProperty<Float> createFloat2 = CanvasProperty.createFloat(((float) getExtendSize()) / f);
        Intrinsics.checkExpressionValueIsNotNull(createFloat2, "CanvasProperty.createFlo…tendSize() / 2.toFloat())");
        setExtendEnd(createFloat2);
        Animator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), ((float) (getExtendSize() / 2)) + ((((float) getRippleSize()) * 1.35f) / f));
        renderNodeAnimator2.setDuration(j);
        renderNodeAnimator2.setInterpolator(this.mInterpolator);
        renderNodeAnimator2.addListener(this.mAnimatorListener);
        renderNodeAnimator2.addListener(this.mEnterHwTraceAnimator);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            CanvasProperty<Float> createFloat3 = CanvasProperty.createFloat(0.0f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat3, "CanvasProperty.createFloat(0f)");
            this.mTopProp = createFloat3;
            CanvasProperty<Float> createFloat4 = CanvasProperty.createFloat((float) getBounds().height());
            Intrinsics.checkExpressionValueIsNotNull(createFloat4, "CanvasProperty.createFlo…ounds.height().toFloat())");
            this.mBottomProp = createFloat4;
            CanvasProperty<Float> createFloat5 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat5, "CanvasProperty.createFlo…s.height() / 2.toFloat())");
            this.mRxProp = createFloat5;
            CanvasProperty<Float> createFloat6 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat6, "CanvasProperty.createFlo…s.height() / 2.toFloat())");
            this.mRyProp = createFloat6;
        } else {
            CanvasProperty<Float> createFloat7 = CanvasProperty.createFloat(0.0f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat7, "CanvasProperty.createFloat(0f)");
            this.mLeftProp = createFloat7;
            CanvasProperty<Float> createFloat8 = CanvasProperty.createFloat((float) getBounds().width());
            Intrinsics.checkExpressionValueIsNotNull(createFloat8, "CanvasProperty.createFlo…bounds.width().toFloat())");
            this.mRightProp = createFloat8;
            CanvasProperty<Float> createFloat9 = CanvasProperty.createFloat(((float) getBounds().width()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat9, "CanvasProperty.createFlo…ds.width() / 2.toFloat())");
            this.mRxProp = createFloat9;
            CanvasProperty<Float> createFloat10 = CanvasProperty.createFloat(((float) getBounds().width()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat10, "CanvasProperty.createFlo…ds.width() / 2.toFloat())");
            this.mRyProp = createFloat10;
        }
        this.mGlowScale = 1.35f;
        this.mGlowAlpha = getMaxGlowAlpha();
        Paint ripplePaint = getRipplePaint();
        this.mRipplePaint = ripplePaint;
        if (ripplePaint != null) {
            ripplePaint.setAlpha((int) (this.mGlowAlpha * ((float) 255)));
            CanvasProperty<Paint> createPaint = CanvasProperty.createPaint(this.mRipplePaint);
            Intrinsics.checkExpressionValueIsNotNull(createPaint, "CanvasProperty.createPaint(mRipplePaint)");
            this.mPaintProp = createPaint;
            renderNodeAnimator.start();
            renderNodeAnimator2.start();
            this.mRunningAnimations.add(renderNodeAnimator);
            this.mRunningAnimations.add(renderNodeAnimator2);
            invalidateSelf();
            if (this.mDelayTouchFeedback && !this.mPressed) {
                exitHardware();
                return;
            }
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void exitHardware() {
        CanvasProperty<Paint> createPaint = CanvasProperty.createPaint(getRipplePaint());
        Intrinsics.checkExpressionValueIsNotNull(createPaint, "CanvasProperty.createPaint(getRipplePaint())");
        this.mPaintProp = createPaint;
        CanvasProperty<Paint> canvasProperty = this.mPaintProp;
        if (canvasProperty != null) {
            Animator renderNodeAnimator = new RenderNodeAnimator(canvasProperty, 1, 0.0f);
            renderNodeAnimator.setDuration((long) 450);
            renderNodeAnimator.setInterpolator(Interpolators.ALPHA_OUT);
            renderNodeAnimator.addListener(this.mAnimatorListener);
            renderNodeAnimator.addListener(this.mExitHwTraceAnimator);
            renderNodeAnimator.setTarget(this.mTargetView);
            renderNodeAnimator.start();
            this.mRunningAnimations.add(renderNodeAnimator);
            invalidateSelf();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mPaintProp");
        throw null;
    }

    public final void gestureSlideEffect(@NotNull Rect rect, @NotNull Rect rect2) {
        Intrinsics.checkParameterIsNotNull(rect, "startRect");
        Intrinsics.checkParameterIsNotNull(rect2, "endRect");
        this.mStartRect = rect;
        this.mEndRect = rect2;
        if (rect != null) {
            int i = rect.left;
            if (rect2 != null) {
                int i2 = rect2.left;
                if (i < i2) {
                    this.mSlideToRight = true;
                    this.mFirstLeftStart = 0.0f;
                    float f = (float) 2;
                    this.mFirstLeftEnd = (((float) getRippleSize()) * -0.35f) / f;
                    Rect rect3 = this.mStartRect;
                    if (rect3 != null) {
                        float f2 = (float) rect3.right;
                        if (rect3 != null) {
                            int i3 = rect3.left;
                            this.mFirstRightStart = f2 - ((float) i3);
                            Rect rect4 = this.mEndRect;
                            if (rect4 != null) {
                                int i4 = rect4.right;
                                if (rect3 != null) {
                                    this.mFirstRightEnd = ((float) (i4 - i3)) + ((((float) getRippleSize()) * 0.35f) / f);
                                    this.mSecondLeftStart = (((float) getRippleSize()) * -0.35f) / f;
                                    Rect rect5 = this.mEndRect;
                                    if (rect5 != null) {
                                        int i5 = rect5.left;
                                        Rect rect6 = this.mStartRect;
                                        if (rect6 != null) {
                                            this.mSecondLeftEnd = ((float) (i5 - rect6.left)) - ((((float) getRippleSize()) * 0.35f) / f);
                                            Rect rect7 = this.mEndRect;
                                            if (rect7 != null) {
                                                int i6 = rect7.right;
                                                Rect rect8 = this.mStartRect;
                                                if (rect8 != null) {
                                                    this.mSecondRightStart = ((float) (i6 - rect8.left)) + ((((float) getRippleSize()) * 0.35f) / f);
                                                    Rect rect9 = this.mEndRect;
                                                    if (rect9 != null) {
                                                        int i7 = rect9.right;
                                                        Rect rect10 = this.mStartRect;
                                                        if (rect10 != null) {
                                                            this.mSecondRightEnd = ((float) (i7 - rect10.left)) + ((((float) getRippleSize()) * 0.35f) / f);
                                                        } else {
                                                            Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                                            throw null;
                                                        }
                                                    } else {
                                                        Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                                        throw null;
                                                    }
                                                } else {
                                                    Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                                    throw null;
                                                }
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                        throw null;
                    }
                } else {
                    this.mSlideToRight = false;
                    this.mFirstLeftStart = 0.0f;
                    if (rect2 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                        throw null;
                    } else if (rect != null) {
                        float f3 = (float) 2;
                        this.mFirstLeftEnd = ((float) (i2 - i)) - ((((float) getRippleSize()) * 0.35f) / f3);
                        Rect rect11 = this.mStartRect;
                        if (rect11 != null) {
                            int i8 = rect11.right;
                            float f4 = (float) i8;
                            if (rect11 != null) {
                                int i9 = rect11.left;
                                this.mFirstRightStart = f4 - ((float) i9);
                                if (rect11 == null) {
                                    Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                    throw null;
                                } else if (rect11 != null) {
                                    this.mFirstRightEnd = ((float) (i8 - i9)) + ((((float) getRippleSize()) * 0.35f) / f3);
                                    Rect rect12 = this.mEndRect;
                                    if (rect12 != null) {
                                        int i10 = rect12.left;
                                        Rect rect13 = this.mStartRect;
                                        if (rect13 != null) {
                                            this.mSecondLeftStart = ((float) (i10 - rect13.left)) - ((((float) getRippleSize()) * 0.35f) / f3);
                                            Rect rect14 = this.mEndRect;
                                            if (rect14 != null) {
                                                int i11 = rect14.left;
                                                Rect rect15 = this.mStartRect;
                                                if (rect15 != null) {
                                                    this.mSecondLeftEnd = ((float) (i11 - rect15.left)) - ((((float) getRippleSize()) * 0.35f) / f3);
                                                    Rect rect16 = this.mStartRect;
                                                    if (rect16 != null) {
                                                        int i12 = rect16.right;
                                                        if (rect16 != null) {
                                                            this.mSecondRightStart = ((float) (i12 - rect16.left)) + ((((float) getRippleSize()) * 0.35f) / f3);
                                                            Rect rect17 = this.mEndRect;
                                                            if (rect17 != null) {
                                                                int i13 = rect17.right;
                                                                Rect rect18 = this.mStartRect;
                                                                if (rect18 != null) {
                                                                    this.mSecondRightEnd = ((float) (i13 - rect18.left)) + ((((float) getRippleSize()) * 0.35f) / f3);
                                                                } else {
                                                                    Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                                                    throw null;
                                                                }
                                                            } else {
                                                                Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                                                throw null;
                                                            }
                                                        } else {
                                                            Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                                            throw null;
                                                        }
                                                    } else {
                                                        Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                                        throw null;
                                                    }
                                                } else {
                                                    Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                                    throw null;
                                                }
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
                        throw null;
                    }
                }
                sildeFirstPart();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("mEndRect");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStartRect");
        throw null;
    }

    private final void sildeFirstPart() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        CanvasProperty<Float> createFloat = CanvasProperty.createFloat(this.mFirstLeftStart);
        Intrinsics.checkExpressionValueIsNotNull(createFloat, "CanvasProperty.createFloat(mFirstLeftStart)");
        setExtendStart(createFloat);
        Animator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), this.mFirstLeftEnd);
        long j = (long) 250;
        renderNodeAnimator.setDuration(j);
        renderNodeAnimator.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator.addListener(this.mSlideToRight ? this.mAnimatorListener : this.mSlideAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        CanvasProperty<Float> createFloat2 = CanvasProperty.createFloat(this.mFirstRightStart);
        Intrinsics.checkExpressionValueIsNotNull(createFloat2, "CanvasProperty.createFloat(mFirstRightStart)");
        setExtendEnd(createFloat2);
        Animator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), this.mFirstRightEnd);
        renderNodeAnimator2.setDuration(j);
        renderNodeAnimator2.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator2.addListener(this.mSlideToRight ? this.mSlideAnimatorListener : this.mAnimatorListener);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            CanvasProperty<Float> createFloat3 = CanvasProperty.createFloat(0.0f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat3, "CanvasProperty.createFloat(0f)");
            this.mTopProp = createFloat3;
            CanvasProperty<Float> createFloat4 = CanvasProperty.createFloat((float) getBounds().height());
            Intrinsics.checkExpressionValueIsNotNull(createFloat4, "CanvasProperty.createFlo…ounds.height().toFloat())");
            this.mBottomProp = createFloat4;
            float f = (float) 2;
            CanvasProperty<Float> createFloat5 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat5, "CanvasProperty.createFlo…s.height() / 2.toFloat())");
            this.mRxProp = createFloat5;
            CanvasProperty<Float> createFloat6 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat6, "CanvasProperty.createFlo…s.height() / 2.toFloat())");
            this.mRyProp = createFloat6;
        } else {
            CanvasProperty<Float> createFloat7 = CanvasProperty.createFloat(0.0f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat7, "CanvasProperty.createFloat(0f)");
            this.mLeftProp = createFloat7;
            CanvasProperty<Float> createFloat8 = CanvasProperty.createFloat((float) getBounds().width());
            Intrinsics.checkExpressionValueIsNotNull(createFloat8, "CanvasProperty.createFlo…bounds.width().toFloat())");
            this.mRightProp = createFloat8;
            float f2 = (float) 2;
            CanvasProperty<Float> createFloat9 = CanvasProperty.createFloat(((float) getBounds().width()) / f2);
            Intrinsics.checkExpressionValueIsNotNull(createFloat9, "CanvasProperty.createFlo…ds.width() / 2.toFloat())");
            this.mRxProp = createFloat9;
            CanvasProperty<Float> createFloat10 = CanvasProperty.createFloat(((float) getBounds().width()) / f2);
            Intrinsics.checkExpressionValueIsNotNull(createFloat10, "CanvasProperty.createFlo…ds.width() / 2.toFloat())");
            this.mRyProp = createFloat10;
        }
        this.mGlowScale = 1.35f;
        Paint ripplePaint = getRipplePaint();
        this.mRipplePaint = ripplePaint;
        if (ripplePaint != null) {
            ripplePaint.setAlpha((int) (this.mGlowAlpha * ((float) 255)));
            CanvasProperty<Paint> createPaint = CanvasProperty.createPaint(this.mRipplePaint);
            Intrinsics.checkExpressionValueIsNotNull(createPaint, "CanvasProperty.createPaint(mRipplePaint)");
            this.mPaintProp = createPaint;
            renderNodeAnimator.start();
            renderNodeAnimator2.start();
            this.mRunningAnimations.add(renderNodeAnimator);
            this.mRunningAnimations.add(renderNodeAnimator2);
            invalidateSelf();
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: private */
    public final void sildeSecondPart() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        CanvasProperty<Float> createFloat = CanvasProperty.createFloat(this.mSecondLeftStart);
        Intrinsics.checkExpressionValueIsNotNull(createFloat, "CanvasProperty.createFloat(mSecondLeftStart)");
        setExtendStart(createFloat);
        Animator renderNodeAnimator = new RenderNodeAnimator(getExtendStart(), this.mSecondLeftEnd);
        long j = (long) 250;
        renderNodeAnimator.setDuration(j);
        renderNodeAnimator.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator.addListener(this.mAnimatorListener);
        renderNodeAnimator.setTarget(this.mTargetView);
        CanvasProperty<Float> createFloat2 = CanvasProperty.createFloat(this.mSecondRightStart);
        Intrinsics.checkExpressionValueIsNotNull(createFloat2, "CanvasProperty.createFloat(mSecondRightStart)");
        setExtendEnd(createFloat2);
        Animator renderNodeAnimator2 = new RenderNodeAnimator(getExtendEnd(), this.mSecondRightEnd);
        renderNodeAnimator2.setDuration(j);
        renderNodeAnimator2.setInterpolator(this.mSineInterpolator);
        renderNodeAnimator2.addListener(this.mAnimatorListener);
        renderNodeAnimator2.setTarget(this.mTargetView);
        if (isHorizontal()) {
            CanvasProperty<Float> createFloat3 = CanvasProperty.createFloat(0.0f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat3, "CanvasProperty.createFloat(0f)");
            this.mTopProp = createFloat3;
            CanvasProperty<Float> createFloat4 = CanvasProperty.createFloat((float) getBounds().height());
            Intrinsics.checkExpressionValueIsNotNull(createFloat4, "CanvasProperty.createFlo…ounds.height().toFloat())");
            this.mBottomProp = createFloat4;
            float f = (float) 2;
            CanvasProperty<Float> createFloat5 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat5, "CanvasProperty.createFlo…s.height() / 2.toFloat())");
            this.mRxProp = createFloat5;
            CanvasProperty<Float> createFloat6 = CanvasProperty.createFloat(((float) getBounds().height()) / f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat6, "CanvasProperty.createFlo…s.height() / 2.toFloat())");
            this.mRyProp = createFloat6;
        } else {
            CanvasProperty<Float> createFloat7 = CanvasProperty.createFloat(0.0f);
            Intrinsics.checkExpressionValueIsNotNull(createFloat7, "CanvasProperty.createFloat(0f)");
            this.mLeftProp = createFloat7;
            CanvasProperty<Float> createFloat8 = CanvasProperty.createFloat((float) getBounds().width());
            Intrinsics.checkExpressionValueIsNotNull(createFloat8, "CanvasProperty.createFlo…bounds.width().toFloat())");
            this.mRightProp = createFloat8;
            float f2 = (float) 2;
            CanvasProperty<Float> createFloat9 = CanvasProperty.createFloat(((float) getBounds().width()) / f2);
            Intrinsics.checkExpressionValueIsNotNull(createFloat9, "CanvasProperty.createFlo…ds.width() / 2.toFloat())");
            this.mRxProp = createFloat9;
            CanvasProperty<Float> createFloat10 = CanvasProperty.createFloat(((float) getBounds().width()) / f2);
            Intrinsics.checkExpressionValueIsNotNull(createFloat10, "CanvasProperty.createFlo…ds.width() / 2.toFloat())");
            this.mRyProp = createFloat10;
        }
        this.mGlowScale = 1.35f;
        Paint ripplePaint = getRipplePaint();
        this.mRipplePaint = ripplePaint;
        if (ripplePaint != null) {
            ripplePaint.setAlpha((int) (this.mGlowAlpha * ((float) 255)));
            CanvasProperty<Paint> createPaint = CanvasProperty.createPaint(this.mRipplePaint);
            Intrinsics.checkExpressionValueIsNotNull(createPaint, "CanvasProperty.createPaint(mRipplePaint)");
            this.mPaintProp = createPaint;
            renderNodeAnimator.start();
            renderNodeAnimator2.start();
            this.mRunningAnimations.add(renderNodeAnimator);
            this.mRunningAnimations.add(renderNodeAnimator2);
            invalidateSelf();
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void cancelAnimations() {
        this.mTmpArray.addAll(this.mRunningAnimations);
        int size = this.mTmpArray.size();
        for (int i = 0; i < size; i++) {
            Animator animator = this.mTmpArray.get(i);
            Intrinsics.checkExpressionValueIsNotNull(animator, "mTmpArray[i]");
            animator.cancel();
        }
        this.mTmpArray.clear();
        this.mRunningAnimations.clear();
    }
}
