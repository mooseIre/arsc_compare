package com.android.keyguard;

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
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import com.android.keyguard.PasswordTextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.R$styleable;
import java.util.ArrayList;
import java.util.Iterator;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;

public class PasswordTextViewForPIN extends PasswordTextView {
    private int mCharPadding;
    private int mDotSize;
    private final Paint mDrawPaint;
    private final Paint mFillPaint;
    private final int mGravity;
    private boolean mIsResetAnimating;
    private int mPasswordLength;
    private float mStrokeWidth;
    private String mText;
    private ArrayList<CharState> mTextChars;
    private final int mTextHeightRaw;
    private PasswordTextView.UserActivityListener mUserActivityListener;
    private int mWidth;

    public PasswordTextViewForPIN(Context context) {
        this(context, null);
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
        this.mFillPaint = new Paint();
        new Handler(Looper.getMainLooper());
        setFocusableInTouchMode(true);
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.PasswordTextView);
        try {
            this.mTextHeightRaw = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_scaledTextSize, 0);
            this.mGravity = obtainStyledAttributes.getInt(R$styleable.PasswordTextView_android_gravity, 17);
            this.mDotSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_dotSize, getContext().getResources().getDimensionPixelSize(C0012R$dimen.password_dot_size));
            this.mCharPadding = obtainStyledAttributes.getDimensionPixelSize(R$styleable.PasswordTextView_charPadding, getContext().getResources().getDimensionPixelSize(C0012R$dimen.password_char_padding));
            this.mStrokeWidth = (float) getContext().getResources().getDimensionPixelSize(C0012R$dimen.keyboard_password_dot_stroke_width);
            obtainStyledAttributes.recycle();
            Settings.System.getInt(((View) this).mContext.getContentResolver(), "show_password", 1);
            AnimationUtils.loadInterpolator(((View) this).mContext, 17563662);
            AnimationUtils.loadInterpolator(((View) this).mContext, 17563663);
            AnimationUtils.loadInterpolator(((View) this).mContext, 17563661);
            int lockPasswordLength = (int) new MiuiLockPatternUtils(context).getLockPasswordLength(KeyguardUpdateMonitor.getCurrentUser());
            this.mPasswordLength = lockPasswordLength;
            if (lockPasswordLength < 4) {
                this.mPasswordLength = 4;
                Log.e("PasswordTextViewForPIN", "get password length = " + this.mPasswordLength);
            }
            for (int i3 = 0; i3 < this.mPasswordLength; i3++) {
                this.mTextChars.add(new CharState());
            }
            this.mWidth = getResources().getDimensionPixelSize(C0012R$dimen.keyguard_security_pin_entry_width);
            initPaints();
            initCharPadding();
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private void initPaints() {
        initPaintSettings(this.mDrawPaint, this.mFillPaint);
        this.mDrawPaint.setStyle(Paint.Style.STROKE);
        this.mDrawPaint.setColor(-1275068417);
        this.mFillPaint.setStyle(Paint.Style.FILL);
        this.mFillPaint.setColor(-1);
    }

    private void initPaintSettings(Paint... paintArr) {
        for (Paint paint : paintArr) {
            paint.setFlags(129);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStrokeWidth(this.mStrokeWidth);
            paint.setTypeface(Typeface.create("sans-serif-light", 0));
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
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0077 A[LOOP:0: B:11:0x0073->B:13:0x0077, LOOP_END] */
    @Override // com.android.keyguard.PasswordTextView
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r12) {
        /*
        // Method dump skipped, instructions count: 141
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.PasswordTextViewForPIN.onDraw(android.graphics.Canvas):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getVisibleTextCharSize() {
        Iterator<CharState> it = this.mTextChars.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().isVisible) {
                i++;
            }
        }
        return i;
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

    @Override // com.android.keyguard.PasswordTextView
    public void append(char c) {
        String str = this.mText;
        String str2 = this.mText + c;
        this.mText = str2;
        int length = str2.length();
        if (length <= this.mPasswordLength) {
            if (this.mIsResetAnimating) {
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

    @Override // com.android.keyguard.PasswordTextView
    public void setUserActivityListener(PasswordTextView.UserActivityListener userActivityListener) {
        this.mUserActivityListener = userActivityListener;
    }

    private void userActivity() {
        PasswordTextView.UserActivityListener userActivityListener = this.mUserActivityListener;
        if (userActivityListener != null) {
            userActivityListener.onUserActivity();
        }
    }

    @Override // com.android.keyguard.PasswordTextView
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

    @Override // com.android.keyguard.PasswordTextView
    public String getText() {
        return this.mText;
    }

    @Override // com.android.keyguard.PasswordTextView
    public void reset(boolean z, boolean z2) {
        String str = this.mText;
        this.mText = "";
        this.mIsResetAnimating = true;
        if (z) {
            int size = this.mTextChars.size();
            for (int i = 0; i < size; i++) {
                this.mTextChars.get(i).startResetAnimation(z2, (((long) (size - i)) * 50) + 50);
            }
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
        if (!AccessibilityManager.getInstance(((View) this).mContext).isEnabled()) {
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

    @Override // com.android.keyguard.PasswordTextView
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(EditText.class.getName());
        accessibilityEvent.setPassword(true);
    }

    @Override // com.android.keyguard.PasswordTextView
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(PasswordTextView.class.getName());
        accessibilityNodeInfo.setPassword(true);
        accessibilityNodeInfo.setEditable(true);
        accessibilityNodeInfo.setInputType(16);
    }

    /* access modifiers changed from: private */
    public class CharState {
        private final AnimConfig CONFIG;
        private final AnimConfig Y_CONFIG;
        float alpha;
        float currentDotSizeFactor = 1.0f;
        boolean isVisible;
        private final String mAlphaTarget = ("char_alpha_" + hashCode());
        private final float mMaxYOffset;
        private final String mScaleTarget = ("char_scale_" + hashCode());
        private final String mYTarget = ("char_y_" + hashCode());
        private int tag = 0;
        float yOffset;

        CharState() {
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(-2, 0.9f, 0.25f);
            this.CONFIG = animConfig;
            AnimConfig animConfig2 = new AnimConfig();
            animConfig2.setEase(-2, 0.8f, 0.3f);
            this.Y_CONFIG = animConfig2;
            setupFolmeAnimations();
            this.mMaxYOffset = (PasswordTextViewForPIN.this.getContext().getResources().getDisplayMetrics().density * 7.0f) + 0.5f;
        }

        private void setupFolmeAnimations() {
            Folme.useValue(this.mScaleTarget).addListener(new TransitionListener() {
                /* class com.android.keyguard.PasswordTextViewForPIN.CharState.AnonymousClass1 */

                @Override // miuix.animation.listener.TransitionListener
                public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                    CharState charState = CharState.this;
                    charState.currentDotSizeFactor = f;
                    PasswordTextViewForPIN.this.postInvalidateOnAnimation();
                }
            });
            Folme.useValue(this.mAlphaTarget).addListener(new TransitionListener() {
                /* class com.android.keyguard.PasswordTextViewForPIN.CharState.AnonymousClass2 */

                @Override // miuix.animation.listener.TransitionListener
                public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                    if (((Integer) obj).intValue() == CharState.this.tag) {
                        CharState charState = CharState.this;
                        charState.alpha = f;
                        PasswordTextViewForPIN.this.postInvalidateOnAnimation();
                    }
                }

                @Override // miuix.animation.listener.TransitionListener
                public void onComplete(Object obj) {
                    if (((Integer) obj).intValue() == CharState.this.tag && PasswordTextViewForPIN.this.mIsResetAnimating && PasswordTextViewForPIN.this.getVisibleTextCharSize() == 0) {
                        PasswordTextViewForPIN.this.mIsResetAnimating = false;
                    }
                }
            });
            Folme.useValue(this.mYTarget).addListener(new TransitionListener() {
                /* class com.android.keyguard.PasswordTextViewForPIN.CharState.AnonymousClass3 */

                @Override // miuix.animation.listener.TransitionListener
                public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                    CharState charState = CharState.this;
                    charState.yOffset = f;
                    PasswordTextViewForPIN.this.postInvalidateOnAnimation();
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            Folme.useValue(this.mAlphaTarget).cancel();
            Folme.useValue(this.mAlphaTarget).setup(Integer.valueOf(this.tag)).clean();
            this.tag++;
            Folme.useValue(this.mAlphaTarget).setup(Integer.valueOf(this.tag)).to(Float.valueOf(0.0f), new AnimConfig[0]);
            Folme.useValue(this.mScaleTarget).cancel();
            Folme.useValue(this.mScaleTarget).to(Float.valueOf(1.0f), new AnimConfig[0]);
            this.isVisible = false;
        }

        /* access modifiers changed from: package-private */
        public void clean() {
            Folme.clean(this.mScaleTarget);
            Folme.clean(this.mAlphaTarget);
            Folme.clean(this.mYTarget);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startResetAnimation(boolean z, long j) {
            startRemoveAnimation(j);
            if (z) {
                startDotAnnounceAnimation(j);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startRemoveAnimation(long j) {
            if (this.isVisible) {
                startDotAlphaAnimation(0.0f, j);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startAppearAnimation() {
            this.isVisible = true;
            startDotAppearAnimation();
        }

        private void startDotAnnounceAnimation(long j) {
            IStateStyle useValue = Folme.useValue(this.mYTarget);
            Float valueOf = Float.valueOf(-this.mMaxYOffset);
            AnimConfig animConfig = new AnimConfig(this.Y_CONFIG);
            animConfig.setDelay(j);
            IStateStyle iStateStyle = useValue.to(valueOf, animConfig);
            Float valueOf2 = Float.valueOf(0.0f);
            AnimConfig animConfig2 = new AnimConfig(this.Y_CONFIG);
            animConfig2.setDelay(j + 100);
            iStateStyle.to(valueOf2, animConfig2);
        }

        private void startDotAppearAnimation() {
            startDotAlphaAnimation(1.0f, 0);
            IStateStyle iStateStyle = Folme.useValue(this.mScaleTarget).to(Float.valueOf(0.8f), this.CONFIG);
            Float valueOf = Float.valueOf(1.25f);
            AnimConfig animConfig = new AnimConfig(this.CONFIG);
            animConfig.setDelay(50);
            IStateStyle iStateStyle2 = iStateStyle.to(valueOf, animConfig);
            Float valueOf2 = Float.valueOf(1.0f);
            AnimConfig animConfig2 = new AnimConfig(this.CONFIG);
            animConfig2.setDelay(150);
            iStateStyle2.to(valueOf2, animConfig2);
        }

        private void startDotAlphaAnimation(float f, long j) {
            Folme.useValue(this.mAlphaTarget).cancel();
            IStateStyle upVar = Folme.useValue(this.mAlphaTarget).setup(Integer.valueOf(this.tag));
            Float valueOf = Float.valueOf(f);
            AnimConfig animConfig = new AnimConfig(this.CONFIG);
            animConfig.setDelay(j);
            upVar.to(valueOf, animConfig);
        }

        public float draw(Canvas canvas, float f, int i, float f2, float f3) {
            canvas.save();
            canvas.translate(f, f2 + this.yOffset);
            canvas.drawCircle(0.0f, 0.0f, (((float) (PasswordTextViewForPIN.this.mDotSize / 2)) - (PasswordTextViewForPIN.this.mStrokeWidth / 2.0f)) * this.currentDotSizeFactor, PasswordTextViewForPIN.this.mDrawPaint);
            if (this.isVisible && this.alpha > 0.0f) {
                PasswordTextViewForPIN.this.mFillPaint.setAlpha((int) (this.alpha * 255.0f));
                canvas.drawCircle(0.0f, 0.0f, ((float) (PasswordTextViewForPIN.this.mDotSize / 2)) * this.currentDotSizeFactor, PasswordTextViewForPIN.this.mFillPaint);
            }
            canvas.restore();
            return f3 + ((float) PasswordTextViewForPIN.this.mCharPadding);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < this.mPasswordLength; i++) {
            this.mTextChars.get(i).clean();
        }
    }
}
