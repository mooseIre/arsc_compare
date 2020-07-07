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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.security.MiuiLockPatternUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import com.android.keyguard.PasswordTextView;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Iterator;

public class PasswordTextViewForPIN extends PasswordTextView {
    /* access modifiers changed from: private */
    public Interpolator mAppearInterpolator;
    /* access modifiers changed from: private */
    public int mCharPadding;
    /* access modifiers changed from: private */
    public Interpolator mDisappearInterpolator;
    /* access modifiers changed from: private */
    public int mDotSize;
    /* access modifiers changed from: private */
    public final Paint mDrawPaint;
    private Interpolator mFastOutSlowInInterpolator;
    private final int mGravity;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsResetAnimating;
    private int mPasswordLength;
    Runnable mResetAnimRunnable;
    boolean mShowPassword;
    private float mStrokeWidth;
    private String mText;
    /* access modifiers changed from: private */
    public ArrayList<CharState> mTextChars;
    private final int mTextHeightRaw;
    private PasswordTextView.UserActivityListener mUserActivityListener;
    private int mWidth;

    public PasswordTextViewForPIN(Context context) {
        this(context, (AttributeSet) null);
    }

    public PasswordTextViewForPIN(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PasswordTextViewForPIN(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* JADX INFO: finally extract failed */
    public PasswordTextViewForPIN(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTextChars = new ArrayList<>();
        this.mText = "";
        this.mDrawPaint = new Paint();
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mResetAnimRunnable = new Runnable() {
            public void run() {
                int size = PasswordTextViewForPIN.this.mTextChars.size();
                for (int i = 0; i < size; i++) {
                    ((CharState) PasswordTextViewForPIN.this.mTextChars.get(i)).startRemoveAnimation(((long) (size - i)) * 40);
                }
            }
        };
        boolean z = true;
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PasswordTextView);
        try {
            this.mTextHeightRaw = obtainStyledAttributes.getInt(3, 0);
            this.mGravity = obtainStyledAttributes.getInt(0, 17);
            this.mDotSize = obtainStyledAttributes.getDimensionPixelSize(2, getContext().getResources().getDimensionPixelSize(R.dimen.password_dot_size));
            this.mCharPadding = obtainStyledAttributes.getDimensionPixelSize(1, getContext().getResources().getDimensionPixelSize(R.dimen.password_char_padding));
            this.mStrokeWidth = (float) getContext().getResources().getDimensionPixelSize(R.dimen.keyboard_password_dot_stroke_width);
            obtainStyledAttributes.recycle();
            this.mDrawPaint.setFlags(129);
            this.mDrawPaint.setTextAlign(Paint.Align.CENTER);
            this.mDrawPaint.setColor(-1);
            this.mDrawPaint.setStrokeWidth(this.mStrokeWidth);
            this.mDrawPaint.setTypeface(Typeface.create("sans-serif-light", 0));
            this.mShowPassword = Settings.System.getInt(this.mContext.getContentResolver(), "show_password", 1) != 1 ? false : z;
            this.mAppearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
            this.mDisappearInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563663);
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
            this.mShowPassword = false;
            this.mPasswordLength = (int) new MiuiLockPatternUtils(context).getLockPasswordLength(KeyguardUpdateMonitor.getCurrentUser());
            if (this.mPasswordLength < 4) {
                this.mPasswordLength = 4;
                Log.e("PasswordTextViewForPIN", "get password length = " + this.mPasswordLength);
            }
            for (int i3 = 0; i3 < this.mPasswordLength; i3++) {
                this.mTextChars.add(new CharState());
            }
            this.mWidth = getResources().getDimensionPixelSize(R.dimen.keyguard_security_pin_entry_width);
            initCharPadding();
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private void initCharPadding() {
        int i = this.mCharPadding;
        int i2 = this.mPasswordLength;
        Rect charBounds = getCharBounds();
        int i3 = (this.mWidth - ((charBounds.right - charBounds.left) * i2)) / (i2 - 1);
        if (i3 <= i) {
            i = i3;
        }
        this.mCharPadding = i;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0088 A[LOOP:0: B:11:0x0084->B:13:0x0088, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00a3 A[LOOP:1: B:15:0x009d->B:17:0x00a3, LOOP_END] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r12) {
        /*
            r11 = this;
            float r0 = r11.getDrawingWidth()
            int r1 = r11.mGravity
            r2 = r1 & 7
            r3 = 1073741824(0x40000000, float:2.0)
            r4 = 3
            if (r2 != r4) goto L_0x0028
            r2 = 8388608(0x800000, float:1.17549435E-38)
            r1 = r1 & r2
            if (r1 == 0) goto L_0x0022
            int r1 = r11.getLayoutDirection()
            r2 = 1
            if (r1 != r2) goto L_0x0022
            int r1 = r11.mWidth
            int r2 = r11.getPaddingRight()
            int r1 = r1 - r2
            float r1 = (float) r1
            goto L_0x002e
        L_0x0022:
            int r0 = r11.getPaddingLeft()
            float r1 = (float) r0
            goto L_0x002f
        L_0x0028:
            int r1 = r11.mWidth
            int r1 = r1 / 2
            float r1 = (float) r1
            float r0 = r0 / r3
        L_0x002e:
            float r1 = r1 - r0
        L_0x002f:
            android.graphics.Rect r0 = r11.getCharBounds()
            int r2 = r0.bottom
            int r4 = r0.top
            int r2 = r2 - r4
            int r4 = r11.getHeight()
            int r5 = r11.getPaddingBottom()
            int r4 = r4 - r5
            int r5 = r11.getPaddingTop()
            int r4 = r4 - r5
            int r4 = r4 / 2
            int r5 = r11.getPaddingTop()
            int r4 = r4 + r5
            float r4 = (float) r4
            int r5 = r11.getPaddingLeft()
            int r6 = r11.getPaddingTop()
            int r7 = r11.mWidth
            int r8 = r11.getPaddingRight()
            int r7 = r7 - r8
            int r8 = r11.getHeight()
            int r9 = r11.getPaddingBottom()
            int r8 = r8 - r9
            r12.clipRect(r5, r6, r7, r8)
            int r5 = r0.right
            int r0 = r0.left
            int r5 = r5 - r0
            float r0 = (float) r5
            float r3 = r0 / r3
            float r1 = r1 + r3
            android.graphics.Paint r3 = r11.mDrawPaint
            android.graphics.Paint$Style r5 = android.graphics.Paint.Style.STROKE
            r3.setStyle(r5)
            android.graphics.Paint r3 = r11.mDrawPaint
            r5 = -1275068417(0xffffffffb3ffffff, float:-1.1920928E-7)
            r3.setColor(r5)
            r3 = 0
            r6 = r1
            r5 = r3
        L_0x0084:
            int r7 = r11.mPasswordLength
            if (r5 >= r7) goto L_0x0090
            float r7 = r11.initGrayDotDraw(r12, r6, r4, r0)
            float r6 = r6 + r7
            int r5 = r5 + 1
            goto L_0x0084
        L_0x0090:
            android.graphics.Paint r5 = r11.mDrawPaint
            r6 = -1
            r5.setColor(r6)
            android.graphics.Paint r5 = r11.mDrawPaint
            android.graphics.Paint$Style r6 = android.graphics.Paint.Style.FILL
            r5.setStyle(r6)
        L_0x009d:
            int r5 = r11.getVisibleTextCharSize()
            if (r3 >= r5) goto L_0x00b8
            java.util.ArrayList<com.android.keyguard.PasswordTextViewForPIN$CharState> r5 = r11.mTextChars
            java.lang.Object r5 = r5.get(r3)
            com.android.keyguard.PasswordTextViewForPIN$CharState r5 = (com.android.keyguard.PasswordTextViewForPIN.CharState) r5
            r6 = r12
            r7 = r1
            r8 = r2
            r9 = r4
            r10 = r0
            float r5 = r5.draw(r6, r7, r8, r9, r10)
            float r1 = r1 + r5
            int r3 = r3 + 1
            goto L_0x009d
        L_0x00b8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.PasswordTextViewForPIN.onDraw(android.graphics.Canvas):void");
    }

    /* access modifiers changed from: private */
    public int getVisibleTextCharSize() {
        Iterator<CharState> it = this.mTextChars.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().isVisible) {
                i++;
            }
        }
        return i;
    }

    private float initGrayDotDraw(Canvas canvas, float f, float f2, float f3) {
        canvas.save();
        canvas.drawCircle(f, f2, ((float) (this.mDotSize / 2)) - (this.mStrokeWidth / 2.0f), this.mDrawPaint);
        canvas.restore();
        return f3 + ((float) this.mCharPadding);
    }

    private Rect getCharBounds() {
        this.mDrawPaint.setTextSize(((float) this.mTextHeightRaw) * getResources().getDisplayMetrics().scaledDensity);
        Rect rect = new Rect();
        this.mDrawPaint.getTextBounds("0", 0, 1, rect);
        return rect;
    }

    private float getDrawingWidth() {
        Rect charBounds = getCharBounds();
        int i = charBounds.right - charBounds.left;
        int i2 = 0;
        for (int i3 = 0; i3 < this.mPasswordLength; i3++) {
            if (i3 != 0) {
                i2 += this.mCharPadding;
            }
            i2 += i;
        }
        return (float) i2;
    }

    public void append(char c) {
        String str = this.mText;
        this.mText += c;
        int length = this.mText.length();
        if (length <= this.mPasswordLength) {
            if (this.mIsResetAnimating) {
                this.mHandler.removeCallbacks(this.mResetAnimRunnable);
                Iterator<CharState> it = this.mTextChars.iterator();
                while (it.hasNext()) {
                    it.next().reset();
                }
                this.mIsResetAnimating = false;
            }
            this.mTextChars.get(length - 1).startAppearAnimation();
            PasswordTextView.TextChangeListener textChangeListener = this.mTextChangeListener;
            if (textChangeListener != null) {
                textChangeListener.onTextChanged(length);
            }
            userActivity();
            sendAccessibilityEventTypeViewTextChanged(str, str.length(), 0, 1);
        }
    }

    public void setUserActivityListener(PasswordTextView.UserActivityListener userActivityListener) {
        this.mUserActivityListener = userActivityListener;
    }

    private void userActivity() {
        PasswordTextView.UserActivityListener userActivityListener = this.mUserActivityListener;
        if (userActivityListener != null) {
            userActivityListener.onUserActivity();
        }
    }

    public void deleteLastChar() {
        int length = this.mText.length();
        String str = this.mText;
        if (length > 0) {
            int i = length - 1;
            this.mText = str.substring(0, i);
            this.mTextChars.get(i).startRemoveAnimation(0);
            sendAccessibilityEventTypeViewTextChanged(str, str.length() - 1, 1, 0);
        }
        userActivity();
        PasswordTextView.TextChangeListener textChangeListener = this.mTextChangeListener;
        if (textChangeListener != null) {
            textChangeListener.onTextChanged(this.mText.length());
        }
    }

    public String getText() {
        return this.mText;
    }

    public void reset(boolean z, boolean z2) {
        String str = this.mText;
        this.mText = "";
        this.mIsResetAnimating = true;
        if (z) {
            this.mHandler.postDelayed(this.mResetAnimRunnable, 320);
        } else {
            Iterator<CharState> it = this.mTextChars.iterator();
            while (it.hasNext()) {
                it.next().reset();
            }
            this.mIsResetAnimating = false;
        }
        if (z2) {
            sendAccessibilityEventTypeViewTextChanged(str, 0, str.length(), 0);
        }
        PasswordTextView.TextChangeListener textChangeListener = this.mTextChangeListener;
        if (textChangeListener != null) {
            textChangeListener.onTextChanged(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAccessibilityEventTypeViewTextChanged(String str, int i, int i2, int i3) {
        if (!AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            return;
        }
        if (isFocused() || (isSelected() && isShown())) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(16);
            obtain.setFromIndex(i);
            obtain.setRemovedCount(i2);
            obtain.setAddedCount(i3);
            obtain.setBeforeText(str);
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
        accessibilityNodeInfo.setClassName(PasswordTextView.class.getName());
        accessibilityNodeInfo.setPassword(true);
        accessibilityNodeInfo.setEditable(true);
        accessibilityNodeInfo.setInputType(16);
    }

    private class CharState {
        float currentDotSizeFactor;
        boolean dotAnimationIsGrowing;
        Animator dotAnimator;
        Animator.AnimatorListener dotFinishListener;
        private ValueAnimator.AnimatorUpdateListener dotSizeUpdater;
        boolean isVisible;
        Animator.AnimatorListener removeDotFinishListener;

        private CharState() {
            this.dotFinishListener = new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    CharState.this.dotAnimator = null;
                }
            };
            this.removeDotFinishListener = new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    CharState charState = CharState.this;
                    charState.isVisible = false;
                    if (!this.mCancelled) {
                        charState.reset();
                        CharState.this.dotAnimator = null;
                    }
                    if (PasswordTextViewForPIN.this.mIsResetAnimating && PasswordTextViewForPIN.this.getVisibleTextCharSize() == 0) {
                        boolean unused = PasswordTextViewForPIN.this.mIsResetAnimating = false;
                    }
                }
            };
            this.dotSizeUpdater = new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CharState.this.currentDotSizeFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    PasswordTextViewForPIN.this.invalidate();
                }
            };
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.currentDotSizeFactor = 0.0f;
            cancelAnimator(this.dotAnimator);
            this.dotAnimator = null;
            this.isVisible = false;
        }

        /* access modifiers changed from: private */
        public void startRemoveAnimation(long j) {
            if ((this.currentDotSizeFactor > 0.0f && this.dotAnimator == null) || (this.dotAnimator != null && this.dotAnimationIsGrowing)) {
                startDotDisappearAnimation(j);
            }
        }

        /* access modifiers changed from: private */
        public void startAppearAnimation() {
            if (this.dotAnimator == null || !this.dotAnimationIsGrowing) {
                this.isVisible = true;
                startDotAppearAnimation(0);
            }
        }

        private void startDotDisappearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.currentDotSizeFactor, 0.0f});
            ofFloat.addUpdateListener(this.dotSizeUpdater);
            ofFloat.addListener(this.removeDotFinishListener);
            ofFloat.setInterpolator(PasswordTextViewForPIN.this.mDisappearInterpolator);
            ofFloat.setDuration((long) (Math.min(this.currentDotSizeFactor, 1.0f) * 160.0f));
            ofFloat.setStartDelay(j);
            ofFloat.start();
            this.dotAnimator = ofFloat;
            this.dotAnimationIsGrowing = false;
        }

        private void startDotAppearAnimation(long j) {
            cancelAnimator(this.dotAnimator);
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.currentDotSizeFactor, 1.5f});
            ofFloat.addUpdateListener(this.dotSizeUpdater);
            ofFloat.setInterpolator(PasswordTextViewForPIN.this.mAppearInterpolator);
            ofFloat.setDuration(160);
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{1.5f, 1.0f});
            ofFloat2.addUpdateListener(this.dotSizeUpdater);
            ofFloat2.setDuration(160);
            ofFloat2.addListener(this.dotFinishListener);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(new Animator[]{ofFloat, ofFloat2});
            animatorSet.setStartDelay(j);
            animatorSet.start();
            this.dotAnimator = animatorSet;
            this.dotAnimationIsGrowing = true;
        }

        private void cancelAnimator(Animator animator) {
            if (animator != null) {
                animator.cancel();
            }
        }

        public float draw(Canvas canvas, float f, int i, float f2, float f3) {
            if (this.currentDotSizeFactor > 0.0f) {
                canvas.save();
                canvas.translate(f, f2);
                canvas.drawCircle(0.0f, 0.0f, ((float) (PasswordTextViewForPIN.this.mDotSize / 2)) * this.currentDotSizeFactor, PasswordTextViewForPIN.this.mDrawPaint);
                canvas.restore();
            }
            return f3 + ((float) PasswordTextViewForPIN.this.mCharPadding);
        }
    }
}
