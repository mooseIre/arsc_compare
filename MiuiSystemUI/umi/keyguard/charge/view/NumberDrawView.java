package com.android.keyguard.charge.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.systemui.C0021R$string;
import java.util.Locale;

public class NumberDrawView extends TextView {
    private Context mContext;
    private String mDot;
    private int mDotWidth;
    private Paint.FontMetrics mFontMetrics;
    private int mLargeMaxNumWidth;
    private TextPaint mLargeTextPaint;
    private int mLargeTextSizePx;
    private String mLevel;
    private String mLocaleName;
    private Typeface mNormalTypeface;
    private TextPaint mPercentTextPaint;
    private int mPercentTextSizePx;
    private int mPercentWidth;
    private Typeface mRegularTypeface;
    private Resources mResources;
    private Point mScreenSize;
    private String mShowLevel;
    private int mSmallMaxNumWidth;
    private TextPaint mSmallTextPaint;
    private int mSmallTextSizePx;
    private int mStrHeight;

    public NumberDrawView(Context context) {
        this(context, null);
    }

    public NumberDrawView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumberDrawView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLevel = "";
        this.mShowLevel = "";
        this.mDot = ".";
        this.mLocaleName = "";
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mScreenSize = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(this.mScreenSize);
        this.mRegularTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-60.otf");
        Typeface createFromAsset = Typeface.createFromAsset(context.getAssets(), "fonts/Mitype2018-35.otf");
        this.mNormalTypeface = createFromAsset;
        setTypeface(createFromAsset);
        TextPaint textPaint = new TextPaint(1);
        this.mLargeTextPaint = textPaint;
        textPaint.setColor(-1);
        TextPaint textPaint2 = new TextPaint(1);
        this.mSmallTextPaint = textPaint2;
        textPaint2.setColor(-1);
        TextPaint textPaint3 = new TextPaint(1);
        this.mPercentTextPaint = textPaint3;
        textPaint3.setColor(-1);
        updateTextPaint();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int indexOf = this.mShowLevel.indexOf(this.mDot);
        int indexOf2 = this.mShowLevel.indexOf("%");
        measureLargeNumWidth();
        measureSmallNumWidth();
        int i = 0;
        if (indexOf == -1) {
            int length = this.mShowLevel.length() - 1;
            int measuredWidth = (getMeasuredWidth() - ((this.mLargeMaxNumWidth * length) + this.mPercentWidth)) / 2;
            if (indexOf2 == 0) {
                canvas.drawText("%", (float) measuredWidth, (float) this.mStrHeight, this.mPercentTextPaint);
                int i2 = measuredWidth + this.mPercentWidth;
                while (i < length) {
                    int i3 = i + 1;
                    canvas.drawText(this.mShowLevel.substring(i3, i + 2), (float) ((i * this.mLargeMaxNumWidth) + i2), (float) this.mStrHeight, this.mLargeTextPaint);
                    i = i3;
                }
                return;
            }
            while (i < length) {
                int i4 = i + 1;
                canvas.drawText(this.mShowLevel.substring(i, i4), (float) ((i * this.mLargeMaxNumWidth) + measuredWidth), (float) this.mStrHeight, this.mLargeTextPaint);
                i = i4;
            }
            canvas.drawText("%", (float) (measuredWidth + (indexOf2 * this.mLargeMaxNumWidth)), (float) this.mStrHeight, this.mPercentTextPaint);
        } else if (indexOf2 == 0) {
            int i5 = indexOf - 1;
            int measuredWidth2 = (getMeasuredWidth() - ((((this.mLargeMaxNumWidth * i5) + (((this.mShowLevel.length() - i5) - 2) * this.mSmallMaxNumWidth)) + this.mPercentWidth) + this.mDotWidth)) / 2;
            canvas.drawText("%", (float) measuredWidth2, (float) this.mStrHeight, this.mPercentTextPaint);
            int i6 = measuredWidth2 + this.mPercentWidth;
            int i7 = 1;
            while (i7 < indexOf) {
                int i8 = i7 + 1;
                canvas.drawText(this.mShowLevel.substring(i7, i8), (float) (((i7 - 1) * this.mLargeMaxNumWidth) + i6), (float) this.mStrHeight, this.mLargeTextPaint);
                i7 = i8;
            }
            canvas.drawText(this.mDot, (float) ((this.mLargeMaxNumWidth * i5) + i6), (float) this.mStrHeight, this.mPercentTextPaint);
            int i9 = i6 + this.mDotWidth;
            int i10 = indexOf + 1;
            while (i10 < this.mShowLevel.length()) {
                int i11 = i10 + 1;
                canvas.drawText(this.mShowLevel.substring(i10, i11), (float) ((this.mLargeMaxNumWidth * i5) + i9 + (((i10 - indexOf) - 1) * this.mSmallMaxNumWidth)), (float) this.mStrHeight, this.mSmallTextPaint);
                i10 = i11;
            }
        } else {
            int measuredWidth3 = (getMeasuredWidth() - ((((this.mLargeMaxNumWidth * indexOf) + (((this.mShowLevel.length() - indexOf) - 2) * this.mSmallMaxNumWidth)) + this.mPercentWidth) + this.mDotWidth)) / 2;
            while (i < indexOf) {
                int i12 = i + 1;
                canvas.drawText(this.mShowLevel.substring(i, i12), (float) ((i * this.mLargeMaxNumWidth) + measuredWidth3), (float) this.mStrHeight, this.mLargeTextPaint);
                i = i12;
            }
            canvas.drawText(this.mDot, (float) ((this.mLargeMaxNumWidth * indexOf) + measuredWidth3), (float) this.mStrHeight, this.mPercentTextPaint);
            int i13 = measuredWidth3 + this.mDotWidth + (this.mLargeMaxNumWidth * indexOf);
            int i14 = indexOf + 1;
            while (i14 < this.mShowLevel.length() - 1) {
                int i15 = i14 + 1;
                canvas.drawText(this.mShowLevel.substring(i14, i15), (float) ((((i14 - indexOf) - 1) * this.mSmallMaxNumWidth) + i13), (float) this.mStrHeight, this.mSmallTextPaint);
                i14 = i15;
            }
            canvas.drawText("%", (float) (i13 + (((this.mShowLevel.length() - indexOf) - 2) * this.mSmallMaxNumWidth)), (float) this.mStrHeight, this.mPercentTextPaint);
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
            String string = this.mResources.getString(C0021R$string.keyguard_charging_battery_level, str);
            this.mShowLevel = string;
            this.mShowLevel = string.replace(" ", "");
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

    private void updateDrawParams() {
        if (!this.mLocaleName.equals(Locale.getDefault().getDisplayName())) {
            doUpdateDrawParams();
        }
    }

    private void handleDot() {
        String substring = String.format(Locale.getDefault(), "%1.2f", Float.valueOf(8.88f)).substring(1, 2);
        this.mDot = substring;
        this.mDotWidth = (int) this.mPercentTextPaint.measureText(substring);
    }

    private void resetStatus() {
        this.mLargeMaxNumWidth = -1;
        this.mSmallMaxNumWidth = -1;
    }

    private void measureLargeNumWidth() {
        if (this.mLargeMaxNumWidth <= 0) {
            int measureText = (int) this.mLargeTextPaint.measureText(String.format(Locale.getDefault(), "%d", 4));
            for (int i = 0; i <= 9; i++) {
                int measureText2 = (int) this.mLargeTextPaint.measureText(String.format(Locale.getDefault(), "%d", Integer.valueOf(i)));
                if (measureText2 > measureText) {
                    measureText = measureText2;
                }
            }
            this.mLargeMaxNumWidth = measureText;
        }
    }

    private void measureSmallNumWidth() {
        if (this.mSmallMaxNumWidth <= 0) {
            int measureText = (int) this.mSmallTextPaint.measureText(String.format(Locale.getDefault(), "%d", 4));
            for (int i = 0; i <= 9; i++) {
                int measureText2 = (int) this.mSmallTextPaint.measureText(String.format(Locale.getDefault(), "%d", Integer.valueOf(i)));
                if (measureText2 > measureText) {
                    measureText = measureText2;
                }
            }
            this.mSmallMaxNumWidth = measureText;
        }
    }

    private int getShowWidth() {
        Point point = this.mScreenSize;
        if (point != null) {
            return point.x;
        }
        return 1080;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(getShowWidth(), this.mStrHeight + 3);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mResources = this.mContext.getResources();
    }

    public void updateSizeForScreenSizeChange(int i, int i2, int i3) {
        setSize(i, i2, i3);
        doUpdateDrawParams();
    }

    private void doUpdateDrawParams() {
        resetStatus();
        measureLargeNumWidth();
        measureSmallNumWidth();
        this.mStrHeight = (int) Math.abs(this.mFontMetrics.top);
        this.mPercentWidth = (int) this.mPercentTextPaint.measureText("%");
        handleDot();
        this.mLocaleName = Locale.getDefault().getDisplayName();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateDrawParams();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mLevel = "";
    }
}
