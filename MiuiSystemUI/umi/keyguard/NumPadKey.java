package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.plugins.R;
import miui.view.MiuiHapticFeedbackConstants;

public class NumPadKey extends ViewGroup {
    static String[] sKlondike;
    private AnimatorSet mBackgroundAnimatorSet;
    /* access modifiers changed from: private */
    public boolean mBackgroundAppearAnimatorRunning;
    private float mBackgroundCircleCenterX;
    private float mBackgroundCircleCenterY;
    /* access modifiers changed from: private */
    public int mBackgroundCircleOriginalRadius;
    /* access modifiers changed from: private */
    public Paint mBackgroundCirclePaint;
    /* access modifiers changed from: private */
    public int mBackgroundCircleRadius;
    /* access modifiers changed from: private */
    public int mDigit;
    private TextView mDigitText;
    private boolean mEnableHaptics;
    private TextView mKlondikeText;
    private View.OnClickListener mListener;
    /* access modifiers changed from: private */
    public boolean mPendingBackgroundDisappearAnimate;
    /* access modifiers changed from: private */
    public PasswordTextView mTextView;
    /* access modifiers changed from: private */
    public int mTextViewResId;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void userActivity() {
        MiuiKeyguardUtils.userActivity(this.mContext);
    }

    public NumPadKey(Context context) {
        this(context, (AttributeSet) null);
    }

    public NumPadKey(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumPadKey(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, R.layout.keyguard_num_pad_key);
    }

    /* JADX INFO: finally extract failed */
    protected NumPadKey(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        int i3;
        this.mDigit = -1;
        this.mListener = new View.OnClickListener() {
            public void onClick(View view) {
                View findViewById;
                if (NumPadKey.this.mTextView == null && NumPadKey.this.mTextViewResId > 0 && (findViewById = NumPadKey.this.getRootView().findViewById(NumPadKey.this.mTextViewResId)) != null && (findViewById instanceof PasswordTextView)) {
                    PasswordTextView unused = NumPadKey.this.mTextView = (PasswordTextView) findViewById;
                }
                if (NumPadKey.this.mTextView != null && NumPadKey.this.mTextView.isEnabled()) {
                    NumPadKey.this.mTextView.append(Character.forDigit(NumPadKey.this.mDigit, 10));
                }
                NumPadKey.this.userActivity();
            }
        };
        setFocusable(true);
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.NumPadKey);
        try {
            this.mDigit = obtainStyledAttributes.getInt(0, this.mDigit);
            this.mTextViewResId = obtainStyledAttributes.getResourceId(1, 0);
            obtainStyledAttributes.recycle();
            setOnClickListener(this.mListener);
            setOnHoverListener(new LiftToActivateListener(context));
            this.mEnableHaptics = new LockPatternUtils(context).isTactileFeedbackEnabled();
            ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(i2, this, true);
            this.mDigitText = (TextView) findViewById(R.id.digit_text);
            this.mDigitText.setText(Integer.toString(this.mDigit));
            this.mDigitText.setTextSize(0, getResources().getDimension(R.dimen.lock_screen_numeric_keyboard_number_text_size));
            this.mDigitText.setTextColor(getResources().getColor(R.color.lock_screen_numeric_keyboard_number_text_color));
            this.mDigitText.setTypeface(Typeface.create("miui-light", 0));
            this.mDigitText.setLineSpacing(0.0f, 1.0f);
            this.mDigitText.setIncludeFontPadding(false);
            this.mKlondikeText = (TextView) findViewById(R.id.klondike_text);
            this.mKlondikeText.setTextSize(0, getResources().getDimension(R.dimen.lock_screen_numeric_keyboard_alphabet_text_size));
            this.mKlondikeText.setTextColor(getResources().getColor(R.color.lock_screen_numeric_keyboard_alphabet_text_color));
            this.mKlondikeText.setTypeface(Typeface.create("miui-regular", 0));
            this.mKlondikeText.setLineSpacing(0.0f, 1.0f);
            this.mKlondikeText.setIncludeFontPadding(false);
            if (this.mDigit >= 0) {
                if (sKlondike == null) {
                    sKlondike = getResources().getStringArray(R.array.lockscreen_num_pad_klondike);
                }
                String[] strArr = sKlondike;
                if (strArr != null && strArr.length > (i3 = this.mDigit)) {
                    String str = strArr[i3];
                    if (str.length() > 0) {
                        this.mKlondikeText.setText(str);
                    } else {
                        this.mKlondikeText.setVisibility(4);
                    }
                }
            }
            setContentDescription(this.mDigitText.getText().toString());
            this.mBackgroundCirclePaint = new Paint();
            this.mBackgroundCirclePaint.setColor(this.mContext.getResources().getColor(R.color.miui_keyguard_pin_num_pad_key_bg_color));
            this.mBackgroundCirclePaint.setAntiAlias(true);
            this.mBackgroundCircleOriginalRadius = this.mContext.getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_num_pad_width) / 2;
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mBackgroundCircleCenterX = (float) (i / 2);
        this.mBackgroundCircleCenterY = (float) (i2 / 2);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.mBackgroundCircleRadius;
        if (i != 0) {
            canvas.drawCircle(this.mBackgroundCircleCenterX, this.mBackgroundCircleCenterY, (float) i, this.mBackgroundCirclePaint);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            doHapticKeyClick();
            startAppearBackgroundAnimate();
        } else if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
            startDisappearBackgroundAnimate();
        }
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        measureChildren(i, i2);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredHeight = this.mDigitText.getMeasuredHeight();
        int measuredHeight2 = this.mKlondikeText.getMeasuredHeight();
        int height = (getHeight() / 2) - ((measuredHeight + measuredHeight2) / 2);
        int width = getWidth() / 2;
        int measuredWidth = width - (this.mDigitText.getMeasuredWidth() / 2);
        int i5 = measuredHeight + height;
        TextView textView = this.mDigitText;
        textView.layout(measuredWidth, height, textView.getMeasuredWidth() + measuredWidth, i5);
        int i6 = (int) (((float) i5) - (((float) measuredHeight2) * 0.35f));
        int measuredWidth2 = width - (this.mKlondikeText.getMeasuredWidth() / 2);
        TextView textView2 = this.mKlondikeText;
        textView2.layout(measuredWidth2, i6, textView2.getMeasuredWidth() + measuredWidth2, measuredHeight2 + i6);
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE ? MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_LIGHT : 1, 3);
        }
    }

    private void startAppearBackgroundAnimate() {
        this.mPendingBackgroundDisappearAnimate = false;
        cancelBackgroundAnimatorSet();
        this.mBackgroundAnimatorSet = createBackgroundAnimatorSet(true, 200, new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                boolean unused = NumPadKey.this.mBackgroundAppearAnimatorRunning = true;
            }

            public void onAnimationEnd(Animator animator) {
                boolean unused = NumPadKey.this.mBackgroundAppearAnimatorRunning = false;
                if (NumPadKey.this.mPendingBackgroundDisappearAnimate) {
                    boolean unused2 = NumPadKey.this.mPendingBackgroundDisappearAnimate = false;
                    NumPadKey.this.startDisappearBackgroundAnimate();
                }
            }
        });
        this.mBackgroundAnimatorSet.start();
    }

    /* access modifiers changed from: private */
    public void startDisappearBackgroundAnimate() {
        if (this.mBackgroundAppearAnimatorRunning) {
            this.mPendingBackgroundDisappearAnimate = true;
            return;
        }
        cancelBackgroundAnimatorSet();
        this.mBackgroundAnimatorSet = createBackgroundAnimatorSet(false, 300, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                int unused = NumPadKey.this.mBackgroundCircleRadius = 0;
            }
        });
        this.mBackgroundAnimatorSet.start();
    }

    private AnimatorSet createBackgroundAnimatorSet(boolean z, long j, AnimatorListenerAdapter animatorListenerAdapter) {
        AnimatorSet animatorSet = new AnimatorSet();
        float[] fArr = new float[2];
        float f = 0.0f;
        fArr[0] = z ? 0.0f : 0.1f;
        if (z) {
            f = 0.1f;
        }
        fArr[1] = f;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NumPadKey.this.mBackgroundCirclePaint.setAlpha((int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * 255.0f));
                NumPadKey.this.invalidate();
            }
        });
        float[] fArr2 = new float[2];
        float f2 = 1.0f;
        fArr2[0] = z ? 1.0f : 1.35f;
        if (z) {
            f2 = 1.35f;
        }
        fArr2[1] = f2;
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(fArr2);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NumPadKey numPadKey = NumPadKey.this;
                int unused = numPadKey.mBackgroundCircleRadius = (int) (((float) numPadKey.mBackgroundCircleOriginalRadius) * ((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
        animatorSet.setDuration(j);
        animatorSet.setInterpolator(Ease$Quad.easeInOut);
        animatorSet.addListener(animatorListenerAdapter);
        return animatorSet;
    }

    private void cancelBackgroundAnimatorSet() {
        AnimatorSet animatorSet = this.mBackgroundAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mBackgroundAnimatorSet.removeAllListeners();
        }
    }
}
