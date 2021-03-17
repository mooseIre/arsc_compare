package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Keep;
import com.android.systemui.R$styleable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;

@Keep
/* compiled from: RadiusDrawable.kt */
public final class RadiusDrawable extends Drawable {
    private int backgroundColor;
    private float cornerRadius;
    private Paint paint;

    public int getOpacity() {
        return -2;
    }

    public RadiusDrawable() {
        Paint paint2 = new Paint();
        this.paint = paint2;
        paint2.setStyle(Paint.Style.FILL);
    }

    private final void setBackgroundColor(int i) {
        if (i != this.backgroundColor) {
            this.backgroundColor = i;
            this.paint.setColor(i);
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(@NotNull Resources resources, @NotNull XmlPullParser xmlPullParser, @NotNull AttributeSet attributeSet, @Nullable Resources.Theme theme) {
        Intrinsics.checkParameterIsNotNull(resources, "r");
        Intrinsics.checkParameterIsNotNull(xmlPullParser, "parser");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
        TypedArray obtainAttributes = Drawable.obtainAttributes(resources, theme, attributeSet, R$styleable.RadiusDrawable);
        if (obtainAttributes.hasValue(R$styleable.RadiusDrawable_radius)) {
            this.cornerRadius = obtainAttributes.getDimension(R$styleable.RadiusDrawable_radius, this.cornerRadius);
            obtainAttributes.recycle();
        }
    }

    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        float f = this.cornerRadius;
        canvas.drawRoundRect(0.0f, 0.0f, (float) getBounds().width(), (float) getBounds().height(), f, f, this.paint);
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Color filters are not supported");
    }

    public void setAlpha(int i) {
        throw new UnsupportedOperationException("Alpha is not supported");
    }

    public void setTintList(@Nullable ColorStateList colorStateList) {
        super.setTintList(colorStateList);
        if (colorStateList != null) {
            setBackgroundColor(colorStateList.getDefaultColor());
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
