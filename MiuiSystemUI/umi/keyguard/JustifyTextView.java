package com.android.keyguard;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class JustifyTextView extends TextView {
    private int mLineY;
    private int mViewWidth;

    public JustifyTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        this.mViewWidth = getMeasuredWidth();
        String charSequence = getText().toString();
        this.mLineY = (int) getTextSize();
        Layout layout = getLayout();
        if (layout != null) {
            int lineCount = layout.getLineCount();
            for (int i = 0; i < lineCount; i++) {
                int lineStart = layout.getLineStart(i);
                int lineEnd = layout.getLineEnd(i);
                float desiredWidth = StaticLayout.getDesiredWidth(charSequence, lineStart, lineEnd, getPaint());
                String substring = charSequence.substring(lineStart, lineEnd);
                if (i >= lineCount - 1 || !needScale(substring)) {
                    canvas.drawText(substring, 0.0f, (float) this.mLineY, paint);
                } else {
                    drawScaledText(canvas, substring, desiredWidth);
                }
                this.mLineY += getLineHeight();
            }
        }
    }

    private void drawScaledText(Canvas canvas, String str, float f) {
        float f2 = 0.0f;
        if (isFirstLineOfParagraph(str)) {
            canvas.drawText("  ", 0.0f, (float) this.mLineY, getPaint());
            f2 = 0.0f + StaticLayout.getDesiredWidth("  ", getPaint());
            str = str.substring(3);
        }
        int i = 0;
        if (str.length() > 2 && str.charAt(0) == 12288 && str.charAt(1) == 12288) {
            String substring = str.substring(0, 2);
            float desiredWidth = StaticLayout.getDesiredWidth(substring, getPaint());
            canvas.drawText(substring, f2, (float) this.mLineY, getPaint());
            f2 += desiredWidth;
            i = 2;
        }
        String[] split = str.trim().split(" ");
        if (split == null || split.length < 2) {
            float length = (((float) this.mViewWidth) - f) / ((float) (str.length() - 1));
            while (i < str.length()) {
                String valueOf = String.valueOf(str.charAt(i));
                float desiredWidth2 = StaticLayout.getDesiredWidth(valueOf, getPaint());
                canvas.drawText(valueOf, f2, (float) this.mLineY, getPaint());
                f2 += desiredWidth2 + length;
                i++;
            }
            return;
        }
        float length2 = (((float) this.mViewWidth) - f) / ((float) (split.length - 1));
        while (i < split.length) {
            String str2 = split[i];
            if (i != split.length - 1) {
                str2 = str2 + " ";
            }
            float desiredWidth3 = StaticLayout.getDesiredWidth(str2, getPaint());
            canvas.drawText(str2, f2, (float) this.mLineY, getPaint());
            f2 += desiredWidth3 + length2;
            i++;
        }
    }

    private boolean isFirstLineOfParagraph(String str) {
        return str.length() > 3 && str.charAt(0) == ' ' && str.charAt(1) == ' ';
    }

    private boolean needScale(String str) {
        if (str == null || str.length() == 0 || str.charAt(str.length() - 1) == 10) {
            return false;
        }
        return true;
    }
}
