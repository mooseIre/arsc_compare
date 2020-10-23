package com.android.keyguard.charge.rapid;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class NumberDrawView extends TextView {
    private Paint.FontMetrics mFontMetrics;
    private TextPaint mLargeTextPaint;
    private int mLargeTextSizePx;
    private String mLevel;
    private Typeface mNormalTypeface;
    private TextPaint mPercentTextPaint;
    private int mPercentTextSizePx;
    private Typeface mRegularTypeface;
    private Resources mResources;
    private TextPaint mSmallTextPaint;
    private int mSmallTextSizePx;

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
    }

    public NumberDrawView(Context context) {
        this(context, (AttributeSet) null);
    }

    public NumberDrawView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumberDrawView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLevel = "";
        init(context);
    }

    private void init(Context context) {
        this.mRegularTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-60.otf");
        this.mNormalTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-35.otf");
        this.mResources = this.mContext.getResources();
        setTypeface(this.mNormalTypeface);
        this.mLargeTextPaint = new TextPaint(1);
        this.mSmallTextPaint = new TextPaint(1);
        this.mPercentTextPaint = new TextPaint(1);
        updateTextPaint();
    }

    public void setSize(int i, int i2, int i3) {
        this.mLargeTextSizePx = (int) TypedValue.applyDimension(0, (float) i, this.mResources.getDisplayMetrics());
        this.mSmallTextSizePx = (int) TypedValue.applyDimension(0, (float) i2, this.mResources.getDisplayMetrics());
        this.mPercentTextSizePx = (int) TypedValue.applyDimension(0, (float) i3, this.mResources.getDisplayMetrics());
        setTextSize(0, (float) this.mLargeTextSizePx);
        updateTextPaint();
        requestLayout();
    }

    public void setLevelText(String str) {
        if (TextUtils.isEmpty(str)) {
            this.mLevel = "";
        } else if (!str.equals(this.mLevel)) {
            if (str.length() > 5) {
                str = "100";
            }
            this.mLevel = str;
            setText(getSpannableLevel(str));
            invalidate();
            requestLayout();
        }
    }

    private void updateTextPaint() {
        this.mLargeTextPaint.setTypeface(this.mNormalTypeface);
        this.mLargeTextPaint.setTextSize((float) this.mLargeTextSizePx);
        this.mFontMetrics = this.mLargeTextPaint.getFontMetrics();
        this.mSmallTextPaint.setTypeface(this.mRegularTypeface);
        this.mSmallTextPaint.setTextSize((float) this.mSmallTextSizePx);
        this.mPercentTextPaint.setTypeface(this.mRegularTypeface);
        this.mPercentTextPaint.setTextSize((float) this.mPercentTextSizePx);
    }

    private int getStringLength() {
        float measureText;
        int i;
        float measureText2;
        float measureText3;
        if (TextUtils.isEmpty(this.mLevel)) {
            return 0;
        }
        int measureText4 = (int) this.mPercentTextPaint.measureText("%");
        int length = this.mLevel.length();
        if (length == 1) {
            measureText = this.mLargeTextPaint.measureText("4");
        } else if (length == 2) {
            measureText = this.mLargeTextPaint.measureText("44");
        } else if (length != 3) {
            if (length == 4) {
                measureText2 = this.mLargeTextPaint.measureText("4");
                measureText3 = this.mSmallTextPaint.measureText(".44");
            } else if (length != 5) {
                measureText = this.mLargeTextPaint.measureText(this.mLevel);
            } else {
                measureText2 = this.mLargeTextPaint.measureText("44");
                measureText3 = this.mSmallTextPaint.measureText(".44");
            }
            i = (int) (measureText2 + measureText3);
            return measureText4 + i;
        } else {
            measureText = this.mLargeTextPaint.measureText("100");
        }
        i = (int) measureText;
        return measureText4 + i;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(getStringLength() + 1, (int) (Math.abs(this.mFontMetrics.top) + 3.0f));
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mResources = this.mContext.getResources();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mLevel = "";
    }

    private SpannableString getSpannableLevel(String str) {
        String string = this.mResources.getString(R.string.keyguard_charging_battery_level, new Object[]{str});
        SpannableString spannableString = new SpannableString(string);
        int indexOf = string.indexOf(37);
        if (indexOf != -1) {
            spannableString.setSpan(new CustomTypefaceSpan(this.mPercentTextSizePx, this.mRegularTypeface), indexOf, indexOf + 1, 0);
        }
        int indexOf2 = string.indexOf(46);
        if (indexOf2 == -1) {
            indexOf2 = string.indexOf(44);
        }
        if (indexOf2 != -1) {
            if (indexOf2 < indexOf) {
                spannableString.setSpan(new CustomTypefaceSpan(this.mSmallTextSizePx, this.mRegularTypeface), indexOf2, indexOf, 0);
            } else if (indexOf2 < string.length()) {
                spannableString.setSpan(new CustomTypefaceSpan(this.mSmallTextSizePx, this.mRegularTypeface), indexOf2, string.length(), 0);
            }
        }
        return spannableString;
    }

    private static class CustomTypefaceSpan extends AbsoluteSizeSpan {
        private final Typeface newType;

        public CustomTypefaceSpan(int i, Typeface typeface) {
            super(i);
            this.newType = typeface;
        }

        public void updateDrawState(TextPaint textPaint) {
            super.updateDrawState(textPaint);
            applyCustomTypeFace(textPaint, this.newType);
        }

        public void updateMeasureState(TextPaint textPaint) {
            super.updateMeasureState(textPaint);
            applyCustomTypeFace(textPaint, this.newType);
        }

        private void applyCustomTypeFace(TextPaint textPaint, Typeface typeface) {
            int i;
            Typeface typeface2 = textPaint.getTypeface();
            if (typeface2 == null) {
                i = 0;
            } else {
                i = typeface2.getStyle();
            }
            int i2 = i & (~typeface.getStyle());
            if ((i2 & 1) != 0) {
                textPaint.setFakeBoldText(true);
            }
            if ((i2 & 2) != 0) {
                textPaint.setTextSkewX(-0.25f);
            }
            textPaint.setTypeface(typeface);
        }
    }
}
