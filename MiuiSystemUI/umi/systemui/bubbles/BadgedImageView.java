package com.android.systemui.bubbles;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.PathParser;
import android.widget.ImageView;
import com.android.launcher3.icons.DotRenderer;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;
import java.util.EnumSet;

public class BadgedImageView extends ImageView {
    private float mAnimatingToDotScale;
    private BubbleViewProvider mBubble;
    private int mBubbleBitmapSize;
    private int mDotColor;
    private boolean mDotIsAnimating;
    private DotRenderer mDotRenderer;
    private float mDotScale;
    private final EnumSet<SuppressionFlag> mDotSuppressionFlags;
    private DotRenderer.DrawParams mDrawParams;
    private boolean mOnLeft;
    private Rect mTempBounds;

    /* access modifiers changed from: package-private */
    public enum SuppressionFlag {
        FLYOUT_VISIBLE,
        BEHIND_STACK
    }

    public BadgedImageView(Context context) {
        this(context, null);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BadgedImageView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDotSuppressionFlags = EnumSet.of(SuppressionFlag.FLYOUT_VISIBLE);
        this.mDotScale = 0.0f;
        this.mAnimatingToDotScale = 0.0f;
        this.mDotIsAnimating = false;
        this.mTempBounds = new Rect();
        this.mBubbleBitmapSize = getResources().getDimensionPixelSize(C0012R$dimen.bubble_bitmap_size);
        this.mDrawParams = new DotRenderer.DrawParams();
        this.mDotRenderer = new DotRenderer(this.mBubbleBitmapSize, PathParser.createPathFromPathData(getResources().getString(17039929)), 100);
        setFocusable(true);
        setClickable(true);
    }

    public void setRenderedBubble(BubbleViewProvider bubbleViewProvider) {
        this.mBubble = bubbleViewProvider;
        setImageBitmap(bubbleViewProvider.getBadgedImage());
        this.mDotColor = bubbleViewProvider.getDotColor();
        drawDot(bubbleViewProvider.getDotPath());
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (shouldDrawDot()) {
            getDrawingRect(this.mTempBounds);
            DotRenderer.DrawParams drawParams = this.mDrawParams;
            drawParams.color = this.mDotColor;
            drawParams.iconBounds = this.mTempBounds;
            drawParams.leftAlign = this.mOnLeft;
            drawParams.scale = this.mDotScale;
            this.mDotRenderer.draw(canvas, drawParams);
        }
    }

    /* access modifiers changed from: package-private */
    public void addDotSuppressionFlag(SuppressionFlag suppressionFlag) {
        if (this.mDotSuppressionFlags.add(suppressionFlag)) {
            updateDotVisibility(suppressionFlag == SuppressionFlag.BEHIND_STACK);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeDotSuppressionFlag(SuppressionFlag suppressionFlag) {
        if (this.mDotSuppressionFlags.remove(suppressionFlag)) {
            updateDotVisibility(suppressionFlag == SuppressionFlag.BEHIND_STACK);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDotVisibility(boolean z) {
        float f = shouldDrawDot() ? 1.0f : 0.0f;
        if (z) {
            animateDotScale(f, null);
            return;
        }
        this.mDotScale = f;
        this.mAnimatingToDotScale = f;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setDotOnLeft(boolean z) {
        this.mOnLeft = z;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void drawDot(Path path) {
        this.mDotRenderer = new DotRenderer(this.mBubbleBitmapSize, path, 100);
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setDotScale(float f) {
        this.mDotScale = f;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public boolean getDotOnLeft() {
        return this.mOnLeft;
    }

    /* access modifiers changed from: package-private */
    public float[] getDotCenter() {
        float[] fArr;
        if (this.mOnLeft) {
            fArr = this.mDotRenderer.getLeftDotPosition();
        } else {
            fArr = this.mDotRenderer.getRightDotPosition();
        }
        getDrawingRect(this.mTempBounds);
        return new float[]{((float) this.mTempBounds.width()) * fArr[0], ((float) this.mTempBounds.height()) * fArr[1]};
    }

    public String getKey() {
        BubbleViewProvider bubbleViewProvider = this.mBubble;
        if (bubbleViewProvider != null) {
            return bubbleViewProvider.getKey();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getDotColor() {
        return this.mDotColor;
    }

    /* access modifiers changed from: package-private */
    public void setDotPositionOnLeft(boolean z, boolean z2) {
        if (!z2 || z == getDotOnLeft() || !shouldDrawDot()) {
            setDotOnLeft(z);
        } else {
            animateDotScale(0.0f, new Runnable(z) {
                /* class com.android.systemui.bubbles.$$Lambda$BadgedImageView$Z7e3tGxE0eQYPk5Be9lp1Zt58bs */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BadgedImageView.this.lambda$setDotPositionOnLeft$0$BadgedImageView(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setDotPositionOnLeft$0 */
    public /* synthetic */ void lambda$setDotPositionOnLeft$0$BadgedImageView(boolean z) {
        setDotOnLeft(z);
        animateDotScale(1.0f, null);
    }

    /* access modifiers changed from: package-private */
    public boolean getDotPositionOnLeft() {
        return getDotOnLeft();
    }

    private boolean shouldDrawDot() {
        return this.mDotIsAnimating || (this.mBubble.showDot() && this.mDotSuppressionFlags.isEmpty());
    }

    private void animateDotScale(float f, Runnable runnable) {
        boolean z = true;
        this.mDotIsAnimating = true;
        if (this.mAnimatingToDotScale == f || !shouldDrawDot()) {
            this.mDotIsAnimating = false;
            return;
        }
        this.mAnimatingToDotScale = f;
        if (f <= 0.0f) {
            z = false;
        }
        clearAnimation();
        animate().setDuration(200).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setUpdateListener(new ValueAnimator.AnimatorUpdateListener(z) {
            /* class com.android.systemui.bubbles.$$Lambda$BadgedImageView$5JtatqU5fJ_DVxOW3Qg2hefSWas */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                BadgedImageView.this.lambda$animateDotScale$1$BadgedImageView(this.f$1, valueAnimator);
            }
        }).withEndAction(new Runnable(z, runnable) {
            /* class com.android.systemui.bubbles.$$Lambda$BadgedImageView$v47cozs89EavNMNnxmtPzE3ZmYs */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ Runnable f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                BadgedImageView.this.lambda$animateDotScale$2$BadgedImageView(this.f$1, this.f$2);
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateDotScale$1 */
    public /* synthetic */ void lambda$animateDotScale$1$BadgedImageView(boolean z, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (!z) {
            animatedFraction = 1.0f - animatedFraction;
        }
        setDotScale(animatedFraction);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateDotScale$2 */
    public /* synthetic */ void lambda$animateDotScale$2$BadgedImageView(boolean z, Runnable runnable) {
        setDotScale(z ? 1.0f : 0.0f);
        this.mDotIsAnimating = false;
        if (runnable != null) {
            runnable.run();
        }
    }
}
