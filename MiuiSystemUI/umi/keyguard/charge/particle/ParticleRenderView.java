package com.android.keyguard.charge.particle;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import java.util.ArrayList;
import java.util.List;

class ParticleRenderView extends View {
    private ParticleTargetLightning lightning;
    private final List<PointF> lightningEdgeList;
    private final List<PointF> lightningInnerList;
    private boolean mBeginAnimation;
    private int mLastRatioOffset;
    private final Paint mPaint;
    private ParticleTargetRing ring;
    private final List<PointF> ringList;
    private ValueAnimator valueAnimator;

    public ParticleRenderView(Context context) {
        this(context, null);
    }

    public ParticleRenderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint = new Paint();
        this.mLastRatioOffset = 0;
        this.ringList = new ArrayList();
        this.lightningInnerList = new ArrayList();
        this.lightningEdgeList = new ArrayList();
        initTargets();
    }

    private void travelBitmap() {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), C0013R$drawable.charge_animation_particel_lightning);
        Bitmap decodeResource2 = BitmapFactory.decodeResource(getResources(), C0013R$drawable.charge_animation_particle_lightning_inner);
        Bitmap decodeResource3 = BitmapFactory.decodeResource(getResources(), C0013R$drawable.charge_animation_particle_lightning_edge);
        for (int i = 0; i < decodeResource.getWidth(); i++) {
            for (int i2 = 0; i2 < decodeResource.getHeight(); i2++) {
                if (Color.alpha(decodeResource.getPixel(i, i2)) > 0) {
                    this.ringList.add(new PointF((float) i, (float) i2));
                }
            }
        }
        for (int i3 = 0; i3 < decodeResource2.getWidth(); i3++) {
            for (int i4 = 0; i4 < decodeResource2.getHeight(); i4++) {
                if (Color.alpha(decodeResource2.getPixel(i3, i4)) > 0) {
                    this.lightningInnerList.add(new PointF((float) i3, (float) i4));
                }
            }
        }
        for (int i5 = 0; i5 < decodeResource3.getWidth(); i5++) {
            for (int i6 = 0; i6 < decodeResource3.getHeight(); i6++) {
                if (Color.alpha(decodeResource3.getPixel(i5, i6)) > 0) {
                    this.lightningEdgeList.add(new PointF((float) i5, (float) i6));
                }
            }
        }
        decodeResource.recycle();
        decodeResource2.recycle();
        decodeResource3.recycle();
    }

    /* access modifiers changed from: package-private */
    public void initTargets() {
        travelBitmap();
        this.mPaint.setColor(ResourcesCompat.getColor(getResources(), C0011R$color.keyguard_charging_particle_color, null));
        this.mPaint.setAntiAlias(true);
        this.lightning = new ParticleTargetLightning(1400);
        ParticleTargetRing particleTargetRing = new ParticleTargetRing(10000);
        this.ring = particleTargetRing;
        particleTargetRing.setRingPointList(this.ringList);
        this.lightning.setLightningInnerPointList(this.lightningInnerList);
        this.lightning.setLightningEdgePointLIst(this.lightningEdgeList);
        this.lightning.initParticles();
        this.ring.initParticles();
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(int i) {
        reset();
        this.mBeginAnimation = true;
        this.ring.startAnimation();
        this.lightning.startAnimation();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, (float) i);
        this.valueAnimator = ofFloat;
        ofFloat.setDuration(2000L);
        this.valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.charge.particle.$$Lambda$ParticleRenderView$uMiRXOV9SKGZ5XZimvapLXk10WI */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ParticleRenderView.this.lambda$startAnimation$0$ParticleRenderView(valueAnimator);
            }
        });
        this.mLastRatioOffset = i;
        this.valueAnimator.setStartDelay(1500);
        this.valueAnimator.start();
        invalidate();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAnimation$0 */
    public /* synthetic */ void lambda$startAnimation$0$ParticleRenderView(ValueAnimator valueAnimator2) {
        this.ring.setRatioOffset(((Float) valueAnimator2.getAnimatedValue()).floatValue() / 1000.0f);
    }

    /* access modifiers changed from: package-private */
    public void updateProgress(int i) {
        if (this.mLastRatioOffset != i) {
            ValueAnimator valueAnimator2 = this.valueAnimator;
            if (valueAnimator2 == null || !valueAnimator2.isRunning()) {
                this.ring.setRatioOffset(((float) i) / 1000.0f);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        ValueAnimator valueAnimator2 = this.valueAnimator;
        if (valueAnimator2 != null && valueAnimator2.isStarted()) {
            this.valueAnimator.cancel();
        }
        this.mLastRatioOffset = 0;
        this.mBeginAnimation = false;
        this.lightning.reset();
        this.ring.reset();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.lightning.onDraw(canvas, this.mPaint);
        this.ring.onDraw(canvas, this.mPaint);
        if (isAttachedToWindow() && this.mBeginAnimation) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateSizeForScreenSizeChange(int i, int i2) {
        float min = (((float) Math.min(i, i2)) * 1.0f) / 1080.0f;
        float f = 540.0f * min;
        float f2 = 945.0f * min;
        this.lightning.setCenter(f, f2);
        this.ring.setCenter(f, f2);
        this.ring.updateRadius(min);
        this.lightning.updateRadius(min);
    }
}
