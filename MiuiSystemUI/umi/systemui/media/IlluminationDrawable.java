package com.android.systemui.media;

import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R$styleable;
import java.util.ArrayList;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.xmlpull.v1.XmlPullParser;

public final class IlluminationDrawable extends Drawable {
    private ValueAnimator backgroundAnimation;
    private int backgroundColor;
    private float cornerRadius;
    private float highlight;
    private int highlightColor;
    private final ArrayList<LightSourceDrawable> lightSources = new ArrayList<>();
    private Paint paint = new Paint();
    private int[] themeAttrs;
    private float[] tmpHsl = {0.0f, 0.0f, 0.0f};

    public int getOpacity() {
        return -2;
    }

    /* access modifiers changed from: public */
    private final void setBackgroundColor(int i) {
        if (i != this.backgroundColor) {
            this.backgroundColor = i;
            animateBackground();
        }
    }

    public void draw(Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        float f = this.cornerRadius;
        canvas.drawRoundRect(0.0f, 0.0f, (float) getBounds().width(), (float) getBounds().height(), f, f, this.paint);
    }

    public void getOutline(Outline outline) {
        Intrinsics.checkParameterIsNotNull(outline, "outline");
        outline.setRoundRect(getBounds(), this.cornerRadius);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) {
        Intrinsics.checkParameterIsNotNull(resources, "r");
        Intrinsics.checkParameterIsNotNull(xmlPullParser, "parser");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
        TypedArray obtainAttributes = Drawable.obtainAttributes(resources, theme, attributeSet, R$styleable.IlluminationDrawable);
        this.themeAttrs = obtainAttributes.extractThemeAttrs();
        Intrinsics.checkExpressionValueIsNotNull(obtainAttributes, "a");
        updateStateFromTypedArray(obtainAttributes);
        obtainAttributes.recycle();
    }

    private final void updateStateFromTypedArray(TypedArray typedArray) {
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_cornerRadius)) {
            this.cornerRadius = typedArray.getDimension(R$styleable.IlluminationDrawable_cornerRadius, this.cornerRadius);
        }
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_highlight)) {
            this.highlight = ((float) typedArray.getInteger(R$styleable.IlluminationDrawable_highlight, 0)) / 100.0f;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0007, code lost:
        if (r0.length <= 0) goto L_0x000f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canApplyTheme() {
        /*
            r1 = this;
            int[] r0 = r1.themeAttrs
            if (r0 == 0) goto L_0x000f
            if (r0 == 0) goto L_0x000a
            int r0 = r0.length
            if (r0 > 0) goto L_0x0015
            goto L_0x000f
        L_0x000a:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            r1 = 0
            throw r1
        L_0x000f:
            boolean r1 = super.canApplyTheme()
            if (r1 == 0) goto L_0x0017
        L_0x0015:
            r1 = 1
            goto L_0x0018
        L_0x0017:
            r1 = 0
        L_0x0018:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.IlluminationDrawable.canApplyTheme():boolean");
    }

    public void applyTheme(Resources.Theme theme) {
        Intrinsics.checkParameterIsNotNull(theme, "t");
        super.applyTheme(theme);
        int[] iArr = this.themeAttrs;
        if (iArr != null) {
            TypedArray resolveAttributes = theme.resolveAttributes(iArr, R$styleable.IlluminationDrawable);
            Intrinsics.checkExpressionValueIsNotNull(resolveAttributes, "a");
            updateStateFromTypedArray(resolveAttributes);
            resolveAttributes.recycle();
        }
    }

    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Color filters are not supported");
    }

    public void setAlpha(int i) {
        throw new UnsupportedOperationException("Alpha is not supported");
    }

    private final void animateBackground() {
        ColorUtils.colorToHSL(this.backgroundColor, this.tmpHsl);
        float[] fArr = this.tmpHsl;
        float f = fArr[2];
        float f2 = this.highlight;
        fArr[2] = MathUtils.constrain(f < 1.0f - f2 ? f + f2 : f - f2, 0.0f, 1.0f);
        int color = this.paint.getColor();
        int i = this.highlightColor;
        int HSLToColor = ColorUtils.HSLToColor(this.tmpHsl);
        ValueAnimator valueAnimator = this.backgroundAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(370L);
        ofFloat.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        ofFloat.addUpdateListener(new IlluminationDrawable$animateBackground$$inlined$apply$lambda$1(this, color, i, HSLToColor));
        ofFloat.addListener(new IlluminationDrawable$animateBackground$$inlined$apply$lambda$2(this, color, i, HSLToColor));
        ofFloat.start();
        this.backgroundAnimation = ofFloat;
    }

    public void setTintList(ColorStateList colorStateList) {
        super.setTintList(colorStateList);
        if (colorStateList != null) {
            setBackgroundColor(colorStateList.getDefaultColor());
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void registerLightSource(View view) {
        Intrinsics.checkParameterIsNotNull(view, "lightSource");
        if (view.getBackground() instanceof LightSourceDrawable) {
            ArrayList<LightSourceDrawable> arrayList = this.lightSources;
            Drawable background = view.getBackground();
            if (background != null) {
                arrayList.add((LightSourceDrawable) background);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.LightSourceDrawable");
        } else if (view.getForeground() instanceof LightSourceDrawable) {
            ArrayList<LightSourceDrawable> arrayList2 = this.lightSources;
            Drawable foreground = view.getForeground();
            if (foreground != null) {
                arrayList2.add((LightSourceDrawable) foreground);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.LightSourceDrawable");
        }
    }
}
