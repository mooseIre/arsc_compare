package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import androidx.annotation.Keep;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R$styleable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;

@Keep
/* compiled from: LightSourceDrawable.kt */
public final class LightSourceDrawable extends Drawable {
    private boolean active;
    private int highlightColor = -1;
    private Paint paint = new Paint();
    private boolean pressed;
    private Animator rippleAnimation;
    private final RippleData rippleData = new RippleData(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private int[] themeAttrs;

    public int getOpacity() {
        return -2;
    }

    public void getOutline(@NotNull Outline outline) {
        Intrinsics.checkParameterIsNotNull(outline, "outline");
    }

    public boolean hasFocusStateSpecified() {
        return true;
    }

    public boolean isProjected() {
        return true;
    }

    public boolean isStateful() {
        return true;
    }

    public final int getHighlightColor() {
        return this.highlightColor;
    }

    public final void setHighlightColor(int i) {
        if (this.highlightColor != i) {
            this.highlightColor = i;
            invalidateSelf();
        }
    }

    private final void setActive(boolean z) {
        if (z != this.active) {
            this.active = z;
            if (z) {
                Animator animator = this.rippleAnimation;
                if (animator != null) {
                    animator.cancel();
                }
                this.rippleData.setAlpha(1.0f);
                this.rippleData.setProgress(0.05f);
            } else {
                Animator animator2 = this.rippleAnimation;
                if (animator2 != null) {
                    animator2.cancel();
                }
                ValueAnimator ofFloat = ValueAnimator.ofFloat(this.rippleData.getAlpha(), 0.0f);
                ofFloat.setDuration(200L);
                ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                ofFloat.addUpdateListener(new LightSourceDrawable$active$$inlined$apply$lambda$1(this));
                ofFloat.addListener(new LightSourceDrawable$active$$inlined$apply$lambda$2(this));
                ofFloat.start();
                this.rippleAnimation = ofFloat;
            }
            invalidateSelf();
        }
    }

    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        float lerp = MathUtils.lerp(this.rippleData.getMinSize(), this.rippleData.getMaxSize(), this.rippleData.getProgress());
        int alphaComponent = ColorUtils.setAlphaComponent(this.highlightColor, (int) (this.rippleData.getAlpha() * ((float) 255)));
        this.paint.setShader(new RadialGradient(this.rippleData.getX(), this.rippleData.getY(), lerp, new int[]{alphaComponent, 0}, LightSourceDrawableKt.access$getGRADIENT_STOPS$p(), Shader.TileMode.CLAMP));
        canvas.drawCircle(this.rippleData.getX(), this.rippleData.getY(), lerp, this.paint);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(@NotNull Resources resources, @NotNull XmlPullParser xmlPullParser, @NotNull AttributeSet attributeSet, @Nullable Resources.Theme theme) {
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
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_rippleMinSize)) {
            this.rippleData.setMinSize(typedArray.getDimension(R$styleable.IlluminationDrawable_rippleMinSize, 0.0f));
        }
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_rippleMaxSize)) {
            this.rippleData.setMaxSize(typedArray.getDimension(R$styleable.IlluminationDrawable_rippleMaxSize, 0.0f));
        }
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_highlight)) {
            this.rippleData.setHighlight(((float) typedArray.getInteger(R$styleable.IlluminationDrawable_highlight, 0)) / 100.0f);
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.LightSourceDrawable.canApplyTheme():boolean");
    }

    public void applyTheme(@NotNull Resources.Theme theme) {
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

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Color filters are not supported");
    }

    public void setAlpha(int i) {
        throw new UnsupportedOperationException("Alpha is not supported");
    }

    private final void illuminate() {
        this.rippleData.setAlpha(1.0f);
        invalidateSelf();
        Animator animator = this.rippleAnimation;
        if (animator != null) {
            animator.cancel();
        }
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setStartDelay(133);
        ofFloat.setDuration(800 - ofFloat.getStartDelay());
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.addUpdateListener(new LightSourceDrawable$illuminate$$inlined$apply$lambda$1(this));
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(this.rippleData.getProgress(), 1.0f);
        ofFloat2.setDuration(800L);
        ofFloat2.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat2.addUpdateListener(new LightSourceDrawable$illuminate$$inlined$apply$lambda$2(this));
        animatorSet.playTogether(ofFloat, ofFloat2);
        animatorSet.addListener(new LightSourceDrawable$illuminate$$inlined$apply$lambda$3(this));
        animatorSet.start();
        this.rippleAnimation = animatorSet;
    }

    public void setHotspot(float f, float f2) {
        this.rippleData.setX(f);
        this.rippleData.setY(f2);
        if (this.active) {
            invalidateSelf();
        }
    }

    @NotNull
    public Rect getDirtyBounds() {
        float lerp = MathUtils.lerp(this.rippleData.getMinSize(), this.rippleData.getMaxSize(), this.rippleData.getProgress());
        Rect rect = new Rect((int) (this.rippleData.getX() - lerp), (int) (this.rippleData.getY() - lerp), (int) (this.rippleData.getX() + lerp), (int) (this.rippleData.getY() + lerp));
        rect.union(super.getDirtyBounds());
        return rect;
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(@Nullable int[] iArr) {
        boolean onStateChange = super.onStateChange(iArr);
        if (iArr == null) {
            return onStateChange;
        }
        boolean z = this.pressed;
        boolean z2 = false;
        this.pressed = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        for (int i : iArr) {
            switch (i) {
                case 16842908:
                    z4 = true;
                    break;
                case 16842910:
                    z3 = true;
                    break;
                case 16842919:
                    this.pressed = true;
                    break;
                case 16843623:
                    z5 = true;
                    break;
            }
        }
        if (z3 && (this.pressed || z4 || z5)) {
            z2 = true;
        }
        setActive(z2);
        if (z && !this.pressed) {
            illuminate();
        }
        return onStateChange;
    }
}
