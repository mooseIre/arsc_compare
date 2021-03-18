package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.R$styleable;
import java.util.ArrayList;
import java.util.Stack;

public class PasswordTextView extends View {
    private static char DOT = 8226;
    private Interpolator mAppearInterpolator;
    private int mCharPadding;
    private Stack<CharState> mCharPool;
    private Interpolator mDisappearInterpolator;
    private int mDotSize;
    private final Paint mDrawPaint;
    private final int mGravity;
    private PowerManager mPM;
    private boolean mShowPassword;
    private String mText;
    public TextChangeListener mTextChangeListener;
    private ArrayList<CharState> mTextChars;
    private final int mTextHeightRaw;
    private UserActivityListener mUserActivityListener;

    public interface TextChangeListener {
        void onTextChanged(int i);
    }

    public interface UserActivityListener {
        void onUserActivity();
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public PasswordTextView(Context context) {
        this(context, null);
    }

    public PasswordTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PasswordTextView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* JADX INFO: finally extract failed */
    public PasswordTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTextChars = new ArrayList<>();
        this.mText = "";
        this.mCharPool = new Stack<>();
        this.mDrawPaint = new Paint();
        boolean z = true;
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PasswordTextView);
        try {
            this.mTextHeightRaw = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_scaledTextSize, 0);
            this.mGravity = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_android_gravity, 17);
            this.mDotSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_dotSize, getContext().getResources().getDimensionPixelSize(C0012R$dimen.password_dot_size));
            this.mCharPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_charPadding, getContext().getResources().getDimensionPixelSize(C0012R$dimen.password_char_padding));
            this.mDrawPaint.setColor(obtainStyledAttributes.getColor(R$styleable.PasswordTextView_android_textColor, -1));
            obtainStyledAttributes.recycle();
            this.mDrawPaint.setFlags(129);
            this.mDrawPaint.setTextAlign(Paint.Align.CENTER);
            this.mDrawPaint.setTypeface(Typeface.create(context.getString(17039925), 0));
            this.mShowPassword = Settings.System.getInt(((View) this).mContext.getContentResolver(), "show_password", 1) != 1 ? false : z;
            this.mAppearInterpolator = AnimationUtils.loadInterpolator(((View) this).mContext, 17563662);
            this.mDisappearInterpolator = AnimationUtils.loadInterpolator(((View) this).mContext, 17563663);
            AnimationUtils.loadInterpolator(((View) this).mContext, 17563661);
            this.mPM = (PowerManager) ((View) this).mContext.getSystemService("power");
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        float drawingWidth = getDrawingWidth();
        int i = this.mGravity;
        if ((i & 7) != 3) {
            f = ((float) (getWidth() - getPaddingRight())) - drawingWidth;
            float width = (((float) getWidth()) / 2.0f) - (drawingWidth / 2.0f);
            if (width > 0.0f) {
                f = width;
            }
        } else if ((i & 8388608) == 0 || getLayoutDirection() != 1) {
            f = (float) getPaddingLeft();
        } else {
            f = ((float) (getWidth() - getPaddingRight())) - drawingWidth;
        }
        int size = this.mTextChars.size();
        Rect charBounds = getCharBounds();
        int i2 = charBounds.bottom - charBounds.top;
        float height = (float) ((((getHeight() - getPaddingBottom()) - getPaddingTop()) / 2) + getPaddingTop());
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        float f2 = (float) (charBounds.right - charBounds.left);
        for (int i3 = 0; i3 < size; i3++) {
            f += this.mTextChars.get(i3).draw(canvas, f, i2, height, f2);
        }
    }

    private Rect getCharBounds() {
        this.mDrawPaint.setTextSize(((float) this.mTextHeightRaw) * getResources().getDisplayMetrics().scaledDensity);
        Rect rect = new Rect();
        this.mDrawPaint.getTextBounds("0", 0, 1, rect);
        return rect;
    }

    private float getDrawingWidth() {
        int size = this.mTextChars.size();
        Rect charBounds = getCharBounds();
        int i = charBounds.right - charBounds.left;
        int i2 = 0;
        for (int i3 = 0; i3 < size; i3++) {
            CharState charState = this.mTextChars.get(i3);
            if (i3 != 0) {
                i2 = (int) (((float) i2) + (((float) this.mCharPadding) * charState.currentWidthFactor));
            }
            i2 = (int) (((float) i2) + (((float) i) * charState.currentWidthFactor));
        }
        return (float) i2;
    }

    public void addTextChangedListener(TextChangeListener textChangeListener) {
        this.mTextChangeListener = textChangeListener;
    }

    public void removeTextChangedListener() {
        this.mTextChangeListener = null;
    }

    public void append(char c) {
        CharState charState;
        int size = this.mTextChars.size();
        CharSequence transformedText = getTransformedText();
        String str = this.mText + c;
        this.mText = str;
        int length = str.length();
        TextChangeListener textChangeListener = this.mTextChangeListener;
        if (textChangeListener != null) {
            textChangeListener.onTextChanged(length);
        }
        if (length > size) {
            charState = obtainCharState(c);
            this.mTextChars.add(charState);
        } else {
            CharState charState2 = this.mTextChars.get(length - 1);
            charState2.whichChar = c;
            charState = charState2;
        }
        charState.startAppearAnimation();
        if (length > 1) {
            CharState charState3 = this.mTextChars.get(length - 2);
            if (charState3.isDotSwapPending) {
                charState3.swapToDotWhenAppearFinished();
            }
        }
        userActivity();
        sendAccessibilityEventTypeViewTextChanged(transformedText, transformedText.length(), 0, 1);
    }

    public void setUserActivityListener(UserActivityListener userActivityListener) {
        this.mUserActivityListener = userActivityListener;
    }

    private void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
        UserActivityListener userActivityListener = this.mUserActivityListener;
        if (userActivityListener != null) {
            userActivityListener.onUserActivity();
        }
    }

    public void deleteLastChar() {
        int length = this.mText.length();
        CharSequence transformedText = getTransformedText();
        if (length > 0) {
            int i = length - 1;
            this.mText = this.mText.substring(0, i);
            this.mTextChars.get(i).startRemoveAnimation(0, 0);
            sendAccessibilityEventTypeViewTextChanged(transformedText, transformedText.length() - 1, 1, 0);
        }
        userActivity();
    }

    public String getText() {
        return this.mText;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CharSequence getTransformedText() {
        int size = this.mTextChars.size();
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            CharState charState = this.mTextChars.get(i);
            if (charState.dotAnimator == null || charState.dotAnimationIsGrowing) {
                sb.append(charState.isCharVisibleForA11y() ? charState.whichChar : DOT);
            }
        }
        return sb;
    }

    private CharState obtainCharState(char c) {
        CharState charState;
        if (this.mCharPool.isEmpty()) {
            charState = new CharState();
        } else {
            charState = this.mCharPool.pop();
            charState.reset();
        }
        charState.whichChar = c;
        return charState;
    }

    public void reset(boolean z, boolean z2) {
        CharSequence transformedText = getTransformedText();
        this.mText = "";
        int size = this.mTextChars.size();
        int i = size - 1;
        int i2 = i / 2;
        int i3 = 0;
        while (i3 < size) {
            CharState charState = this.mTextChars.get(i3);
            if (z) {
                charState.startRemoveAnimation(Math.min(((long) (i3 <= i2 ? i3 * 2 : i - (((i3 - i2) - 1) * 2))) * 40, 200L), Math.min(40 * ((long) i), 200L) + 160);
                charState.removeDotSwapCallbacks();
            } else {
                this.mCharPool.push(charState);
            }
            i3++;
        }
        if (!z) {
            this.mTextChars.clear();
        }
        if (z2) {
            sendAccessibilityEventTypeViewTextChanged(transformedText, 0, transformedText.length(), 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAccessibilityEventTypeViewTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (!AccessibilityManager.getInstance(((View) this).mContext).isEnabled()) {
            return;
        }
        if (isFocused() || (isSelected() && isShown())) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(16);
            obtain.setFromIndex(i);
            obtain.setRemovedCount(i2);
            obtain.setAddedCount(i3);
            obtain.setBeforeText(charSequence);
            CharSequence transformedText = getTransformedText();
            if (!TextUtils.isEmpty(transformedText)) {
                obtain.getText().add(transformedText);
            }
            obtain.setPassword(true);
            sendAccessibilityEventUnchecked(obtain);
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(EditText.class.getName());
        accessibilityEvent.setPassword(true);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(EditText.class.getName());
        accessibilityNodeInfo.setPassword(true);
        accessibilityNodeInfo.setText(getTransformedText());
        accessibilityNodeInfo.setEditable(true);
        accessibilityNodeInfo.setInputType(16);
    }

    /* access modifiers changed from: private */
    public class CharState {
        float currentDotSizeFactor;
        float currentTextSizeFactor;
        float currentTextTranslationY;
        float currentWidthFactor;
        boolean dotAnimationIsGrowing;
        Animator dotAnimator;
        Animator.AnimatorListener dotFinishListener;
        private ValueAnimator.AnimatorUpdateListener dotSizeUpdater;
        private Runnable dotSwapperRunnable;
        boolean isDotSwapPending;
        Animator.AnimatorListener removeEndListener;
        boolean textAnimationIsGrowing;
        ValueAnimator textAnimator;
        Animator.AnimatorListener textFinishListener;
        private ValueAnimator.AnimatorUpdateListener textSizeUpdater;
        ValueAnimator textTranslateAnimator;
        Animator.AnimatorListener textTranslateFinishListener;
        private ValueAnimator.AnimatorUpdateListener textTranslationUpdater;
        char whichChar;
        boolean widthAnimationIsGrowing;
        ValueAnimator widthAnimator;
        Animator.AnimatorListener widthFinishListener;
        private ValueAnimator.AnimatorUpdateListener widthUpdater;

        private CharState() {
            this.currentTextTranslationY = 1.0f;
            this.removeEndListener = new AnimatorListenerAdapter() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass1 */
                private boolean mCancelled;

                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    if (!this.mCancelled) {
                        PasswordTextView.this.mTextChars.remove(CharState.this);
                        PasswordTextView.this.mCharPool.push(CharState.this);
                        CharState.this.reset();
                        CharState charState = CharState.this;
                        charState.cancelAnimator(charState.textTranslateAnimator);
                        CharState.this.textTranslateAnimator = null;
                    }
                }

                public void onAnimationStart(Animator animator) {
                    this.mCancelled = false;
                }
            };
            this.dotFinishListener = new AnimatorListenerAdapter() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass2 */

                public void onAnimationEnd(Animator animator) {
                    CharState.this.dotAnimator = null;
                }
            };
            this.textFinishListener = new AnimatorListenerAdapter() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass3 */

                public void onAnimationEnd(Animator animator) {
                    CharState.this.textAnimator = null;
                }
            };
            this.textTranslateFinishListener = new AnimatorListenerAdapter() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass4 */

                public void onAnimationEnd(Animator animator) {
                    CharState.this.textTranslateAnimator = null;
                }
            };
            this.widthFinishListener = new AnimatorListenerAdapter() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass5 */

                public void onAnimationEnd(Animator animator) {
                    CharState.this.widthAnimator = null;
                }
            };
            this.dotSizeUpdater = new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass6 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentDotSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.textSizeUpdater = new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass7 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    boolean isCharVisibleForA11y = CharState.this.isCharVisibleForA11y();
                    CharState charState = CharState.this;
                    float f = charState.currentTextSizeFactor;
                    charState.currentTextSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    if (isCharVisibleForA11y != CharState.this.isCharVisibleForA11y()) {
                        CharState charState2 = CharState.this;
                        charState2.currentTextSizeFactor = f;
                        CharSequence transformedText = PasswordTextView.this.getTransformedText();
                        CharState.this.currentTextSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        int indexOf = PasswordTextView.this.mTextChars.indexOf(CharState.this);
                        if (indexOf >= 0) {
                            PasswordTextView.this.sendAccessibilityEventTypeViewTextChanged(transformedText, indexOf, 1, 1);
                        }
                    }
                    PasswordTextView.this.invalidate();
                }
            };
            this.textTranslationUpdater = new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass8 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentTextTranslationY = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.widthUpdater = new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass9 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentWidthFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    PasswordTextView.this.invalidate();
                }
            };
            this.dotSwapperRunnable = new Runnable() {
                /* class com.android.keyguard.PasswordTextView.CharState.AnonymousClass10 */

                public void run() {
                    CharState.this.performSwap();
                    CharState.this.isDotSwapPending = false;
                }
            };
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.whichChar = 0;
            this.currentTextSizeFactor = 0.0f;
            this.currentDotSizeFactor = 0.0f;
            this.currentWidthFactor = 0.0f;
            cancelAnimator(this.textAnimator);
            this.textAnimator = null;
            cancelAnimator(this.dotAnimator);
            this.dotAnimator = null;
            cancelAnimator(this.widthAnimator);
            this.widthAnimator = null;
            this.currentTextTranslationY = 1.0f;
            removeDotSwapCallbacks();
        }

        /* access modifiers changed from: package-private */
        public void startRemoveAnimation(long j, long j2) {
            boolean z = true;
            boolean z2 = (this.currentDotSizeFactor > 0.0f && this.dotAnimator == null) || (this.dotAnimator != null && this.dotAnimationIsGrowing);
            boolean z3 = (this.currentTextSizeFactor > 0.0f && this.textAnimator == null) || (this.textAnimator != null && this.textAnimationIsGrowing);
            if ((this.currentWidthFactor <= 0.0f || this.widthAnimator != null) && (this.widthAnimator == null || !this.widthAnimationIsGrowing)) {
                z = false;
            }
            if (z2) {
                startDotDisappearAnimation(j);
            }
            if (z3) {
                startTextDisappearAnimation(j);
            }
            if (z) {
                startWidthDisappearAnimation(j2);
            }
        }

        /* access modifiers changed from: package-private */
        public void startAppearAnimation() {
            boolean z = true;
            boolean z2 = !PasswordTextView.this.mShowPassword && (this.dotAnimator == null || !this.dotAnimationIsGrowing);
            boolean z3 = PasswordTextView.this.mShowPassword && (this.textAnimator == null || !this.textAnimationIsGrowing);
            if (this.widthAnimator != null && this.widthAnimationIsGrowing) {
                z = false;
            }
            if (z2) {
                startDotAppearAnimation(0);
            }
            if (z3) {
                startTextAppearAnimation();
            }
            if (z) {
                startWidthAppearAnimation();
            }
            if (PasswordTextView.this.mShowPassword) {
                postDotSwap(1300);
            }
        }

        private void postDotSwap(long j) {
            removeDotSwapCallbacks();
            PasswordTextView.this.postDelayed(this.dotSwapperRunnable, j);
            this.isDotSwapPending = true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeDotSwapCallbacks() {
            PasswordTextView.this.removeCallbacks(this.dotSwapperRunnable);
            this.isDotSwapPending = false;
        }

        /* access modifiers changed from: package-private */
        public void swapToDotWhenAppearFinished() {
            removeDotSwapCallbacks();
            ValueAnimator valueAnimator = this.textAnimator;
            if (valueAnimator != null) {
                postDotSwap((valueAnimator.getDuration() - this.textAnimator.getCurrentPlayTime()) + 100);
            } else {
                performSwap();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void performSwap() {
            startTextDisappearAnimation(0);
            startDotAppearAnimation(30);
        }

        private void startWidthDisappearAnimation(long j) {
            cancelAnimator(this.widthAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentWidthFactor, 0.0f);
            this.widthAnimator = ofFloat;
            ofFloat.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.addListener(this.removeEndListener);
            this.widthAnimator.setDuration((long) (this.currentWidthFactor * 160.0f));
            this.widthAnimator.setStartDelay(j);
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = false;
        }

        private void startTextDisappearAnimation(long j) {
            cancelAnimator(this.textAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentTextSizeFactor, 0.0f);
            this.textAnimator = ofFloat;
            ofFloat.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(PasswordTextView.this.mDisappearInterpolator);
            this.textAnimator.setDuration((long) (this.currentTextSizeFactor * 160.0f));
            this.textAnimator.setStartDelay(j);
            this.textAnimator.start();
            this.textAnimationIsGrowing = false;
        }

        private void startDotDisappearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentDotSizeFactor, 0.0f);
            ofFloat.addUpdateListener(this.dotSizeUpdater);
            ofFloat.addListener(this.dotFinishListener);
            ofFloat.setInterpolator(PasswordTextView.this.mDisappearInterpolator);
            ofFloat.setDuration((long) (Math.min(this.currentDotSizeFactor, 1.0f) * 160.0f));
            ofFloat.setStartDelay(j);
            ofFloat.start();
            this.dotAnimator = ofFloat;
            this.dotAnimationIsGrowing = false;
        }

        private void startWidthAppearAnimation() {
            cancelAnimator(this.widthAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentWidthFactor, 1.0f);
            this.widthAnimator = ofFloat;
            ofFloat.addUpdateListener(this.widthUpdater);
            this.widthAnimator.addListener(this.widthFinishListener);
            this.widthAnimator.setDuration((long) ((1.0f - this.currentWidthFactor) * 160.0f));
            this.widthAnimator.start();
            this.widthAnimationIsGrowing = true;
        }

        private void startTextAppearAnimation() {
            cancelAnimator(this.textAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentTextSizeFactor, 1.0f);
            this.textAnimator = ofFloat;
            ofFloat.addUpdateListener(this.textSizeUpdater);
            this.textAnimator.addListener(this.textFinishListener);
            this.textAnimator.setInterpolator(PasswordTextView.this.mAppearInterpolator);
            this.textAnimator.setDuration((long) ((1.0f - this.currentTextSizeFactor) * 160.0f));
            this.textAnimator.start();
            this.textAnimationIsGrowing = true;
            if (this.textTranslateAnimator == null) {
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
                this.textTranslateAnimator = ofFloat2;
                ofFloat2.addUpdateListener(this.textTranslationUpdater);
                this.textTranslateAnimator.addListener(this.textTranslateFinishListener);
                this.textTranslateAnimator.setInterpolator(PasswordTextView.this.mAppearInterpolator);
                this.textTranslateAnimator.setDuration(160L);
                this.textTranslateAnimator.start();
            }
        }

        private void startDotAppearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            if (!PasswordTextView.this.mShowPassword) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(this.currentDotSizeFactor, 1.5f);
                ofFloat.addUpdateListener(this.dotSizeUpdater);
                ofFloat.setInterpolator(PasswordTextView.this.mAppearInterpolator);
                ofFloat.setDuration(160L);
                ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.5f, 1.0f);
                ofFloat2.addUpdateListener(this.dotSizeUpdater);
                ofFloat2.setDuration(160L);
                ofFloat2.addListener(this.dotFinishListener);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(ofFloat, ofFloat2);
                animatorSet.setStartDelay(j);
                animatorSet.start();
                this.dotAnimator = animatorSet;
            } else {
                ValueAnimator ofFloat3 = ValueAnimator.ofFloat(this.currentDotSizeFactor, 1.0f);
                ofFloat3.addUpdateListener(this.dotSizeUpdater);
                ofFloat3.setDuration((long) ((1.0f - this.currentDotSizeFactor) * 160.0f));
                ofFloat3.addListener(this.dotFinishListener);
                ofFloat3.setStartDelay(j);
                ofFloat3.start();
                this.dotAnimator = ofFloat3;
            }
            this.dotAnimationIsGrowing = true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancelAnimator(Animator animator) {
            if (animator != null) {
                animator.cancel();
            }
        }

        public float draw(Canvas canvas, float f, int i, float f2, float f3) {
            boolean z = true;
            boolean z2 = this.currentTextSizeFactor > 0.0f;
            if (this.currentDotSizeFactor <= 0.0f) {
                z = false;
            }
            float f4 = f3 * this.currentWidthFactor;
            if (z2) {
                float f5 = (float) i;
                float f6 = ((f5 / 2.0f) * this.currentTextSizeFactor) + f2 + (f5 * this.currentTextTranslationY * 0.8f);
                canvas.save();
                canvas.translate((f4 / 2.0f) + f, f6);
                float f7 = this.currentTextSizeFactor;
                canvas.scale(f7, f7);
                canvas.drawText(Character.toString(this.whichChar), 0.0f, 0.0f, PasswordTextView.this.mDrawPaint);
                canvas.restore();
            }
            if (z) {
                canvas.save();
                canvas.translate(f + (f4 / 2.0f), f2);
                canvas.drawCircle(0.0f, 0.0f, ((float) (PasswordTextView.this.mDotSize / 2)) * this.currentDotSizeFactor, PasswordTextView.this.mDrawPaint);
                canvas.restore();
            }
            return f4 + (((float) PasswordTextView.this.mCharPadding) * this.currentWidthFactor);
        }

        public boolean isCharVisibleForA11y() {
            return this.currentTextSizeFactor > 0.0f || (this.textAnimator != null && this.textAnimationIsGrowing);
        }
    }
}
