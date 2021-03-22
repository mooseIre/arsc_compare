package com.android.keyguard;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.R$styleable;
import com.miui.systemui.drawable.NumPadRippleDrawable;
import miuix.animation.Folme;

public class NumPadKey extends ViewGroup {
    static String[] sKlondike;
    private int mDigit;
    private final TextView mDigitText;
    private final TextView mKlondikeText;
    private View.OnClickListener mListener;
    private final LockPatternUtils mLockPatternUtils;
    private final PowerManager mPM;
    private PasswordTextView mTextView;
    private int mTextViewResId;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    public NumPadKey(Context context) {
        this(context, null);
    }

    public NumPadKey(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumPadKey(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, C0017R$layout.keyguard_num_pad_key);
    }

    /* JADX INFO: finally extract failed */
    protected NumPadKey(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        int i3;
        this.mDigit = -1;
        this.mListener = new View.OnClickListener() {
            /* class com.android.keyguard.NumPadKey.AnonymousClass1 */

            public void onClick(View view) {
                View findViewById;
                if (NumPadKey.this.mTextView == null && NumPadKey.this.mTextViewResId > 0 && (findViewById = NumPadKey.this.getRootView().findViewById(NumPadKey.this.mTextViewResId)) != null && (findViewById instanceof PasswordTextView)) {
                    NumPadKey.this.mTextView = (PasswordTextView) findViewById;
                }
                if (NumPadKey.this.mTextView != null && NumPadKey.this.mTextView.isEnabled()) {
                    NumPadKey.this.mTextView.append(Character.forDigit(NumPadKey.this.mDigit, 10));
                }
                NumPadKey.this.userActivity();
            }
        };
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.NumPadKey);
        try {
            this.mDigit = obtainStyledAttributes.getInt(R$styleable.NumPadKey_digit, this.mDigit);
            this.mTextViewResId = obtainStyledAttributes.getResourceId(R$styleable.NumPadKey_textView, 0);
            obtainStyledAttributes.recycle();
            setOnClickListener(this.mListener);
            setOnHoverListener(new LiftToActivateListener(context));
            this.mLockPatternUtils = new LockPatternUtils(context);
            this.mPM = (PowerManager) ((ViewGroup) this).mContext.getSystemService("power");
            ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(i2, (ViewGroup) this, true);
            TextView textView = (TextView) findViewById(C0015R$id.digit_text);
            this.mDigitText = textView;
            textView.setText(Integer.toString(this.mDigit));
            this.mDigitText.setTextSize(0, getResources().getDimension(C0012R$dimen.lock_screen_numeric_keyboard_number_text_size));
            this.mDigitText.setTextColor(getResources().getColor(C0011R$color.lock_screen_numeric_keyboard_number_text_color));
            this.mDigitText.setTypeface(Typeface.create("miui-light", 0));
            this.mDigitText.setLineSpacing(0.0f, 1.0f);
            this.mDigitText.setIncludeFontPadding(false);
            TextView textView2 = (TextView) findViewById(C0015R$id.klondike_text);
            this.mKlondikeText = textView2;
            textView2.setTextSize(0, getResources().getDimension(C0012R$dimen.lock_screen_numeric_keyboard_alphabet_text_size));
            this.mKlondikeText.setTextColor(getResources().getColor(C0011R$color.lock_screen_numeric_keyboard_alphabet_text_color));
            this.mKlondikeText.setTypeface(Typeface.create("miui-regular", 0));
            this.mKlondikeText.setLineSpacing(0.0f, 1.0f);
            this.mKlondikeText.setIncludeFontPadding(false);
            if (this.mDigit >= 0) {
                if (sKlondike == null) {
                    sKlondike = getResources().getStringArray(C0008R$array.lockscreen_num_pad_klondike);
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
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.View);
            if (!obtainStyledAttributes2.hasValueOrEmpty(13)) {
                setBackground(((ViewGroup) this).mContext.getDrawable(C0013R$drawable.ripple_drawable_pin));
            }
            obtainStyledAttributes2.recycle();
            setContentDescription(this.mDigitText.getText().toString());
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            doHapticKeyClick();
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
        if (this.mLockPatternUtils.isTactileFeedbackEnabled()) {
            performHapticFeedback(1, 3);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Drawable background = getBackground();
        if (background instanceof NumPadRippleDrawable) {
            Folme.clean(((NumPadRippleDrawable) background).getNumPadAnimTarget());
        }
    }
}
