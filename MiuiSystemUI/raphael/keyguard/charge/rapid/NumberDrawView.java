package com.android.keyguard.charge.rapid;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
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
    private String mShowLevel;
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
        this.mShowLevel = "";
        init(context);
    }

    private void init(Context context) {
        this.mRegularTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-60.otf");
        this.mNormalTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-35.otf");
        this.mResources = this.mContext.getResources();
        setTypeface(this.mNormalTypeface);
        this.mLargeTextPaint = new TextPaint(1);
        this.mLargeTextPaint.setColor(-1);
        this.mSmallTextPaint = new TextPaint(1);
        this.mSmallTextPaint.setColor(-1);
        this.mPercentTextPaint = new TextPaint(1);
        this.mPercentTextPaint.setColor(-1);
        updateTextPaint();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int abs = (int) Math.abs(this.mFontMetrics.top);
        int indexOf = this.mShowLevel.indexOf(46);
        int indexOf2 = this.mShowLevel.indexOf(37);
        int measureText = (int) this.mPercentTextPaint.measureText(".");
        int measureText2 = (int) this.mPercentTextPaint.measureText("%");
        int i = 0;
        if (indexOf == -1) {
            int length = this.mShowLevel.length() - 1;
            int measuredWidth = (getMeasuredWidth() - ((getLargeMaxNumLength() * length) + measureText2)) / 2;
            if (indexOf2 == 0) {
                float f = (float) abs;
                canvas.drawText("%", (float) measuredWidth, f, this.mPercentTextPaint);
                while (i < length) {
                    int i2 = i + 1;
                    canvas.drawText(this.mShowLevel.substring(i2, i + 2), (float) (measuredWidth + measureText2 + (i * getLargeMaxNumLength())), f, this.mLargeTextPaint);
                    i = i2;
                }
                return;
            }
            while (i < length) {
                int i3 = i + 1;
                canvas.drawText(this.mShowLevel.substring(i, i3), (float) ((i * getLargeMaxNumLength()) + measuredWidth), (float) abs, this.mLargeTextPaint);
                i = i3;
            }
            canvas.drawText("%", (float) (measuredWidth + (indexOf2 * getLargeMaxNumLength())), (float) abs, this.mPercentTextPaint);
        } else if (indexOf2 == 0) {
            int i4 = indexOf - 1;
            int measuredWidth2 = (getMeasuredWidth() - ((((getLargeMaxNumLength() * i4) + (((this.mShowLevel.length() - i4) - 2) * getSmallMaxNumLength())) + measureText2) + measureText)) / 2;
            float f2 = (float) abs;
            canvas.drawText("%", (float) measuredWidth2, f2, this.mPercentTextPaint);
            int i5 = 1;
            while (i5 < indexOf) {
                int i6 = i5 + 1;
                canvas.drawText(this.mShowLevel.substring(i5, i6), (float) (measuredWidth2 + measureText2 + ((i5 - 1) * getLargeMaxNumLength())), f2, this.mLargeTextPaint);
                i5 = i6;
            }
            int i7 = measuredWidth2 + measureText2;
            canvas.drawText(".", (float) ((getLargeMaxNumLength() * i4) + i7), f2, this.mPercentTextPaint);
            int i8 = indexOf + 1;
            while (i8 < this.mShowLevel.length()) {
                int i9 = i8 + 1;
                canvas.drawText(this.mShowLevel.substring(i8, i9), (float) (i7 + measureText + (getLargeMaxNumLength() * i4) + (((i8 - indexOf) - 1) * getSmallMaxNumLength())), f2, this.mSmallTextPaint);
                i8 = i9;
            }
        } else {
            int measuredWidth3 = (getMeasuredWidth() - ((((getLargeMaxNumLength() * indexOf) + (((this.mShowLevel.length() - indexOf) - 2) * getSmallMaxNumLength())) + measureText2) + measureText)) / 2;
            while (i < indexOf) {
                int i10 = i + 1;
                canvas.drawText(this.mShowLevel.substring(i, i10), (float) ((i * getLargeMaxNumLength()) + measuredWidth3), (float) abs, this.mLargeTextPaint);
                i = i10;
            }
            float f3 = (float) abs;
            canvas.drawText(".", (float) ((getLargeMaxNumLength() * indexOf) + measuredWidth3), f3, this.mPercentTextPaint);
            int i11 = indexOf + 1;
            while (i11 < this.mShowLevel.length() - 1) {
                int i12 = i11 + 1;
                canvas.drawText(this.mShowLevel.substring(i11, i12), (float) (measuredWidth3 + measureText + (getLargeMaxNumLength() * indexOf) + (((i11 - indexOf) - 1) * getSmallMaxNumLength())), f3, this.mSmallTextPaint);
                i11 = i12;
            }
            canvas.drawText("%", (float) (measuredWidth3 + measureText + (getLargeMaxNumLength() * indexOf) + (((this.mShowLevel.length() - indexOf) - 2) * getSmallMaxNumLength())), f3, this.mPercentTextPaint);
        }
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
            this.mShowLevel = this.mResources.getString(R.string.keyguard_charging_battery_level, new Object[]{str});
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

    private int getLargeMaxNumLength() {
        int measureText = (int) this.mLargeTextPaint.measureText("4");
        for (int i = 0; i <= 9; i++) {
            int measureText2 = (int) this.mLargeTextPaint.measureText(String.valueOf(i));
            if (measureText2 > measureText) {
                measureText = measureText2;
            }
        }
        return measureText;
    }

    private int getSmallMaxNumLength() {
        int measureText = (int) this.mSmallTextPaint.measureText("4");
        for (int i = 0; i <= 9; i++) {
            int measureText2 = (int) this.mSmallTextPaint.measureText(String.valueOf(i));
            if (measureText2 > measureText) {
                measureText = measureText2;
            }
        }
        return measureText;
    }

    private int getStringLength() {
        int largeMaxNumLength;
        int smallMaxNumLength;
        if (TextUtils.isEmpty(this.mLevel)) {
            return 0;
        }
        int measureText = (int) this.mPercentTextPaint.measureText("%");
        int measureText2 = (int) this.mPercentTextPaint.measureText(".");
        int length = this.mLevel.length();
        int measureText3 = (int) this.mLargeTextPaint.measureText(this.mLevel);
        if (length == 1) {
            measureText3 = getLargeMaxNumLength();
        } else if (length == 2) {
            measureText3 = getLargeMaxNumLength() * 2;
        } else if (length == 3) {
            measureText3 = getLargeMaxNumLength() * 3;
        } else {
            if (length == 4) {
                largeMaxNumLength = getLargeMaxNumLength() + measureText2;
                smallMaxNumLength = getSmallMaxNumLength();
            } else if (length == 5) {
                largeMaxNumLength = (getLargeMaxNumLength() * 2) + measureText2;
                smallMaxNumLength = getSmallMaxNumLength();
            }
            measureText3 = largeMaxNumLength + (smallMaxNumLength * 2);
        }
        return measureText3 + measureText;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(getStringLength(), (int) (Math.abs(this.mFontMetrics.top) + 3.0f));
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

    private static class CustomTypefaceSpan extends AbsoluteSizeSpan {
        private final Typeface newType;

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
