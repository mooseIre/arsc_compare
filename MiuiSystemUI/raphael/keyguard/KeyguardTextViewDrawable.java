package com.android.keyguard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class KeyguardTextViewDrawable extends TextView {
    private boolean mAliganCenter;
    private int mWidth;

    public KeyguardTextViewDrawable(Context context) {
        this(context, null);
    }

    public KeyguardTextViewDrawable(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardTextViewDrawable(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAliganCenter = true;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mWidth = i;
        Drawable[] compoundDrawables = getCompoundDrawables();
        Drawable drawable = compoundDrawables[0];
        Drawable drawable2 = compoundDrawables[1];
        Drawable drawable3 = compoundDrawables[2];
        Drawable drawable4 = compoundDrawables[3];
        if (drawable != null) {
            setDrawable(drawable, 0, 0, 0);
        }
        if (drawable2 != null) {
            setDrawable(drawable2, 1, 0, 0);
        }
        if (drawable3 != null) {
            setDrawable(drawable3, 2, 0, 0);
        }
        if (drawable4 != null) {
            setDrawable(drawable4, 3, 0, 0);
        }
        setCompoundDrawables(drawable, drawable2, drawable3, drawable4);
    }

    private void setDrawable(Drawable drawable, int i, int i2, int i3) {
        int i4;
        int i5;
        if (i2 == 0) {
            i2 = drawable.getIntrinsicWidth();
        }
        if (i3 == 0) {
            i3 = drawable.getIntrinsicHeight();
        }
        int i6 = 0;
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        i5 = 0;
                        i2 = 0;
                        i4 = 0;
                        drawable.setBounds(i6, i5, i2, i4);
                    }
                }
            }
            int i7 = this.mAliganCenter ? 0 : ((-this.mWidth) / 2) + (i2 / 2);
            i2 += i7;
            i4 = i3 + 0;
            i5 = 0;
            i6 = i7;
            drawable.setBounds(i6, i5, i2, i4);
        }
        i5 = this.mAliganCenter ? 0 : (((-getLineCount()) * getLineHeight()) / 2) + (getLineHeight() / 2);
        i4 = i3 + i5;
        drawable.setBounds(i6, i5, i2, i4);
    }
}
