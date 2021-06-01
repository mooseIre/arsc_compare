package com.android.keyguard.charge.particle;

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;
import miuix.animation.property.FloatProperty;
import miuix.animation.utils.EaseManager;

/* access modifiers changed from: package-private */
public class ParticleTargetLightning {
    private static final AtomicInteger sIdGenerator = new AtomicInteger();
    private final List<PointF> lightningEdgePointLIst = new ArrayList();
    private final List<PointF> lightningInnerPointList = new ArrayList();
    private float mCenterX = 540.0f;
    private float mCenterY = 945.0f;
    private final AnimState mFromState = new AnimState("particleLightningFrom");
    private final int mId = sIdGenerator.incrementAndGet();
    private final String mName = ("ParticleTargetLightning" + this.mId);
    private final List<Particle> mParticles = new ArrayList();
    private float mRadius = 368.0f;
    private final Random mRandom = new Random();
    private float mTargetX = 399.62f;
    private float mTargetY = 728.25f;
    private final AnimState mToState = new AnimState("particleLightningTo");

    private float toRad(float f) {
        return (float) (((double) (f / 180.0f)) * 3.141592653589793d);
    }

    /* access modifiers changed from: private */
    public static class Particle {
        public float angle;
        public float distance;
        public final int id;
        public float innerSpeed;
        public float innerSpeedRandom;
        public float isLightning;
        public float isRatio;
        public float per;
        public float radius;
        public float random;
        public float random_area;
        public int size;
        public float t;
        public String tag;
        public double targetX;
        public double targetY;
        public float timeX;
        public float timeY;

        Particle(int i) {
            this.id = i;
        }

        public String toString() {
            return "Particle{id=" + this.id + '}';
        }
    }

    ParticleTargetLightning(int i) {
        Folme.getValueTarget(this.mName).setDefaultMinVisibleChange(1.0f);
        Folme.useValue(this.mName).addListener(new TransitionListenerWrapper(this));
        for (int i2 = 0; i2 < i; i2++) {
            this.mParticles.add(new Particle(i2));
        }
    }

    /* access modifiers changed from: package-private */
    public void setCenter(float f, float f2) {
        this.mCenterX = f;
        this.mCenterY = f2;
    }

    /* access modifiers changed from: package-private */
    public void updateRadius(float f) {
        this.mRadius = 368.0f * f;
        this.mTargetX = 399.62f * f;
        this.mTargetY = f * 728.25f;
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        initParticles();
    }

    /* access modifiers changed from: package-private */
    public void initParticles() {
        int i = 1;
        for (Particle particle : this.mParticles) {
            setParticle(particle, this.mCenterX, this.mCenterY);
            String valueOf = String.valueOf(i);
            String valueOf2 = String.valueOf(i + 1);
            AnimState animState = this.mFromState;
            animState.add(valueOf, (double) particle.radius);
            animState.add(valueOf2, (double) particle.angle);
            i += 2;
            particle.timeX = (float) (Math.random() * 100.0d);
            particle.timeY = (float) (Math.random() * 100.0d);
        }
        Folme.useValue(this.mName).cancel();
        Folme.useValue(this.mName).setTo(this.mFromState);
    }

    /* access modifiers changed from: package-private */
    public void startAnimation() {
        AnimConfig config = this.mToState.getConfig();
        int size = this.mParticles.size();
        int i = 1;
        for (int i2 = 0; i2 < size; i2++) {
            Particle particle = this.mParticles.get(i2);
            String valueOf = String.valueOf(i);
            String valueOf2 = String.valueOf(i + 1);
            particle.t = 0.0f;
            particle.innerSpeed = 0.0f;
            AnimState animState = this.mToState;
            double d = (double) 0.0f;
            animState.add(valueOf, d);
            animState.add(valueOf2, d);
            EaseManager.EaseStyle style = EaseManager.getStyle(-2, 0.95f, (new Random().nextFloat() * 0.5f) + 2.0f);
            if (particle.isLightning > 0.0f) {
                style = EaseManager.getStyle(-2, 0.9f, (new Random().nextFloat() * 2.0f) + 1.0f);
            }
            config.setEase(style);
            i += 2;
        }
        Folme.useValue(this.mName).to(this.mToState, new AnimConfig[0]).setConfig(config, new FloatProperty[0]);
    }

    /* access modifiers changed from: package-private */
    public void onDraw(Canvas canvas, Paint paint) {
        float f;
        Paint paint2;
        Canvas canvas2;
        Canvas canvas3;
        for (Particle particle : this.mParticles) {
            if (!"null".equals(particle.tag)) {
                particle.timeX = (float) (((double) particle.timeX) + 0.01d);
                particle.timeY = (float) (((double) particle.timeY) + 0.02d);
                particle.t += 1.0f;
                particle.innerSpeed += (particle.innerSpeedRandom * 8.0f) + 10.0f;
                double d = particle.targetX;
                float f2 = particle.radius;
                float f3 = this.mRadius;
                double d2 = d + ((((double) f2) * (d - ((double) this.mCenterX))) / ((double) f3));
                int i = particle.size;
                float f4 = (float) (d2 + ((double) (i / 2)));
                double d3 = particle.targetY;
                float f5 = (float) (d3 + ((((double) f2) * (d3 - ((double) this.mCenterY))) / ((double) f3)) + ((double) (i / 2)));
                particle.per = 360.0f;
                float f6 = 1.0f - (f2 / 300.0f);
                if (!"ring".equals(particle.tag)) {
                    float f7 = this.mCenterX;
                    f = f7 + ((float) (((particle.targetX - ((double) f7)) * Math.cos((double) toRad(particle.angle / 8.0f))) + ((((double) particle.radius) * (particle.targetX - ((double) this.mCenterX))) / ((double) this.mRadius))));
                    if ("inner".equals(particle.tag)) {
                        f += perlin(particle.timeX) * 10.0f;
                        f5 += perlin(particle.timeX) * 10.0f;
                    }
                } else {
                    f = f4;
                }
                int intValue = ((Integer) new ArgbEvaluator().evaluate(clamp((Math.abs(f5 - this.mCenterY) * 2.0f) / 220.0f), 127155, 3064169)).intValue();
                if ("edge".equals(particle.tag)) {
                    paint2 = paint;
                    paint2.setColor(intValue);
                    float f8 = particle.random;
                    if (((double) f8) < 0.3d) {
                        paint2.setAlpha((int) (f6 * 255.0f));
                        paint2.setShader(null);
                        canvas3 = canvas;
                        canvas3.drawCircle(f, f5, ((float) particle.size) / 2.0f, paint2);
                    } else {
                        canvas3 = canvas;
                        if (((double) f8) < 0.6d) {
                            paint2.setAlpha((int) (f6 * 255.0f));
                            paint2.setShader(null);
                            canvas3.drawCircle(f, f5, (((float) particle.size) / 2.0f) * (clamp((float) Math.sin((double) ((particle.t / 25.0f) + (f8 * 500.0f)))) + 0.1f), paint2);
                        } else {
                            paint2.setAlpha((int) (f6 * 255.0f * clamp((float) Math.sin((double) ((particle.t / 25.0f) + (f8 * 500.0f))))));
                            paint2.setShader(null);
                            canvas3.drawCircle(f, f5, ((float) particle.size) / 2.0f, paint2);
                        }
                    }
                } else {
                    canvas3 = canvas;
                    paint2 = paint;
                    float f9 = particle.random;
                    if (((double) f9) < 0.8d) {
                        paint2.setColor(intValue);
                        paint2.setAlpha((int) (f6 * 180.0f * particle.innerSpeedRandom));
                        paint2.setShader(null);
                        canvas3.drawCircle(f, f5, ((float) particle.size) / 2.0f, paint2);
                    } else {
                        float clamp = clamp((float) Math.sin((double) ((particle.t / 25.0f) + (f9 * 500.0f)))) + 0.1f;
                        if (((double) particle.random) < 0.85d) {
                            intValue = 16777215;
                        }
                        RadialGradient radialGradient = new RadialGradient(f, f5, ((((float) particle.size) / 2.0f) * clamp) + 0.1f, Color.argb(255, Color.red(intValue), Color.green(intValue), Color.blue(intValue)), Color.argb(0, Color.red(intValue), Color.green(intValue), Color.blue(intValue)), Shader.TileMode.CLAMP);
                        paint2.setAlpha((int) (f6 * 255.0f));
                        if (((double) particle.random) < 0.85d) {
                            paint2.setAlpha(0);
                        }
                        paint2.setShader(radialGradient);
                        canvas2 = canvas;
                        canvas2.drawCircle(f, f5, (((float) particle.size) / 2.0f) * clamp, paint2);
                    }
                }
                canvas2 = canvas3;
            }
        }
    }

    private void setParticle(Particle particle, float f, float f2) {
        particle.random = new Random().nextFloat();
        particle.random_area = new Random().nextFloat();
        particle.innerSpeedRandom = new Random().nextFloat();
        particle.tag = "null";
        if (((double) particle.random_area) < 0.5d) {
            particle.isLightning = 1.0f;
            int min = Math.min((int) Math.round(Math.random() * ((double) this.lightningInnerPointList.size())), this.lightningInnerPointList.size() - 1);
            particle.targetX = (double) (this.lightningInnerPointList.get(min).x + this.mTargetX);
            particle.targetY = (double) (this.lightningInnerPointList.get(min).y + this.mTargetY);
            particle.tag = "inner";
        } else {
            particle.isLightning = 1.0f;
            int min2 = Math.min((int) Math.round(Math.random() * ((double) this.lightningEdgePointLIst.size())), this.lightningEdgePointLIst.size() - 1);
            particle.targetX = (double) (this.lightningEdgePointLIst.get(min2).x + this.mTargetX);
            particle.targetY = (double) (this.lightningEdgePointLIst.get(min2).y + this.mTargetY);
            particle.tag = "edge";
        }
        if ("inner".equals(particle.tag)) {
            if (((double) particle.random) < 0.8d) {
                particle.size = (int) ((this.mRandom.nextFloat() * 3.0f) + 3.0f);
            } else {
                particle.size = (int) ((this.mRandom.nextFloat() * 10.0f) + 8.0f);
            }
        } else if ("edge".equals(particle.tag)) {
            particle.size = (int) ((this.mRandom.nextFloat() * 5.0f) + 1.0f);
        }
        particle.isRatio = 0.0f;
        float sqrt = (float) Math.sqrt(Math.pow(particle.targetX - ((double) f), 2.0d) + Math.pow(particle.targetY - ((double) f2), 2.0d));
        if (((double) particle.random) > 0.5d && sqrt >= 335.0f) {
            particle.isRatio = 1.0f;
        }
        particle.t = 0.0f;
        particle.innerSpeed = 0.0f;
        particle.radius = 300.0f;
        particle.angle = 1000.0f;
        particle.per = 360.0f;
        particle.distance = sqrt;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setParticlePos(UpdateInfo updateInfo) {
        int parseInt = Integer.parseInt(updateInfo.property.getName());
        if ((parseInt & 1) == 0) {
            this.mParticles.get((parseInt / 2) - 1).angle = updateInfo.getFloatValue();
            return;
        }
        this.mParticles.get(parseInt / 2).radius = updateInfo.getFloatValue();
    }

    private float perlin(float f) {
        double d = (double) 1.0f;
        double d2 = (double) f;
        return (float) (((double) ((float) (((double) ((float) (((double) ((float) (((double) ((float) (((double) ((float) Math.sin(d))) + (Math.sin((2.1d * d) + d2) * 4.5d)))) + (Math.sin((1.72d * d) + (1.121d * d2)) * 4.0d)))) + (Math.sin((2.221d * d) + (0.437d * d2)) * 5.0d)))) + (Math.sin((3.1122d * d) + (d2 * 4.269d)) * 2.5d)))) * d * 0.06d);
    }

    private float clamp(float f) {
        return Math.max(0.0f, Math.min(f, 1.0f));
    }

    public void setLightningInnerPointList(List<PointF> list) {
        this.lightningInnerPointList.clear();
        this.lightningInnerPointList.addAll(list);
    }

    /* access modifiers changed from: package-private */
    public void setLightningEdgePointLIst(List<PointF> list) {
        this.lightningEdgePointLIst.clear();
        this.lightningEdgePointLIst.addAll(list);
    }

    private static class TransitionListenerWrapper extends TransitionListener {
        private final WeakReference<ParticleTargetLightning> mParticleTargetLighting;

        public TransitionListenerWrapper(ParticleTargetLightning particleTargetLightning) {
            this.mParticleTargetLighting = new WeakReference<>(particleTargetLightning);
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onUpdate(Object obj, Collection<UpdateInfo> collection) {
            ParticleTargetLightning particleTargetLightning = this.mParticleTargetLighting.get();
            if (particleTargetLightning != null) {
                for (UpdateInfo updateInfo : collection) {
                    particleTargetLightning.setParticlePos(updateInfo);
                }
            }
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onCancel(Object obj) {
            ParticleTargetLightning particleTargetLightning = this.mParticleTargetLighting.get();
            if (particleTargetLightning != null) {
                particleTargetLightning.initParticles();
            }
        }
    }
}
