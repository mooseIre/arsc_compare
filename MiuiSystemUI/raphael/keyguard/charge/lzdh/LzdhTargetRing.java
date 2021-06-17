package com.android.keyguard.charge.lzdh;

import android.graphics.PointF;
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
import miuix.animation.utils.EaseManager;

public class LzdhTargetRing {
    private static final AtomicInteger sIdGenerator = new AtomicInteger();
    private float currentRatio = 0.0f;
    private float mCenterX = 540.0f;
    private float mCenterY = 945.0f;
    private final AnimState mFromState = new AnimState("particleRingFrom");
    private final int mId = sIdGenerator.incrementAndGet();
    private final String mName = ("ParticleTargetRing" + this.mId);
    private final List<Particle> mParticles = new ArrayList();
    private float mRadius = 368.0f;
    private final Random mRandom = new Random();
    private float mTargetX = 160.0f;
    private float mTargetY = 568.0f;
    private final AnimState mToState = new AnimState("particleRingTo");
    private final List<PointF> ringPointList = new ArrayList();

    private float toRad(float f) {
        return (float) (((double) (f / 180.0f)) * 3.141592653589793d);
    }

    /* access modifiers changed from: private */
    public static class Particle {
        public float angle;
        public final int id;
        public float innerSpeed;
        public float innerSpeedRandom;
        public float isExplode;
        public float isLightning;
        public float isRatio;
        public float per;
        public float radius;
        public float random;
        public int size;
        public float t;
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

    LzdhTargetRing(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            this.mParticles.add(new Particle(i2));
        }
        Folme.getValueTarget(this.mName).setDefaultMinVisibleChange(1.0f);
        Folme.useValue(this.mName).addListener(new TransitionListenerWrapper(this));
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.currentRatio = 0.0f;
        initParticles();
    }

    /* access modifiers changed from: package-private */
    public void setCenter(float f, float f2) {
        this.mCenterX = f;
        this.mCenterY = f2;
    }

    public void updateRadius(float f) {
        this.mRadius = 368.0f * f;
        this.mTargetX = 160.0f * f;
        this.mTargetY = f * 568.0f;
    }

    /* access modifiers changed from: package-private */
    public void initParticles() {
        int i = 1;
        for (Particle particle : this.mParticles) {
            setParticle(particle);
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
        this.currentRatio = 0.0f;
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
            EaseManager.EaseStyle style = EaseManager.getStyle(-2, 0.95f, (this.mRandom.nextFloat() * 0.5f) + 2.0f);
            if (particle.isLightning > 0.0f) {
                style = EaseManager.getStyle(-2, 0.9f, (this.mRandom.nextFloat() * 2.0f) + 1.0f);
            }
            config.setEase(style);
            i += 2;
        }
        Folme.useValue(this.mName).to(this.mToState, config);
    }

    /* access modifiers changed from: package-private */
    public void setRatioOffset(float f) {
        this.currentRatio = f;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0415  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x04a4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r30, android.graphics.Paint r31) {
        /*
        // Method dump skipped, instructions count: 1353
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.charge.lzdh.LzdhTargetRing.onDraw(android.graphics.Canvas, android.graphics.Paint):void");
    }

    private void setParticle(Particle particle) {
        particle.random = this.mRandom.nextFloat();
        particle.innerSpeedRandom = this.mRandom.nextFloat();
        int min = Math.min((int) Math.round(Math.random() * ((double) this.ringPointList.size())), this.ringPointList.size() - 1);
        particle.targetX = (double) this.ringPointList.get(min).x;
        double d = (double) this.ringPointList.get(min).y;
        particle.targetY = d;
        particle.targetX += (double) this.mTargetX;
        particle.targetY = d + ((double) this.mTargetY);
        particle.isRatio = 0.0f;
        if (((double) particle.random) > 0.5d) {
            particle.isRatio = 1.0f;
        }
        particle.t = 0.0f;
        particle.innerSpeed = 0.0f;
        particle.radius = 300.0f;
        particle.angle = 1000.0f;
        particle.per = 360.0f;
        if (particle.isRatio > 0.0f) {
            particle.size = 5;
            return;
        }
        float f = particle.random;
        if (((double) f) <= 0.015d) {
            particle.size = 12;
        } else if (((double) f) < 0.05d) {
            particle.size = (int) ((this.mRandom.nextFloat() * 8.0f) + 8.0f);
        } else {
            particle.size = (int) ((this.mRandom.nextFloat() * 3.0f) + 3.0f);
        }
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

    /* access modifiers changed from: package-private */
    public float clamp(float f) {
        return Math.max(0.0f, Math.min(f, 1.0f));
    }

    /* access modifiers changed from: package-private */
    public void setRingPointList(List<PointF> list) {
        this.ringPointList.clear();
        this.ringPointList.addAll(list);
    }

    private static class TransitionListenerWrapper extends TransitionListener {
        private final WeakReference<LzdhTargetRing> mParticleTargetRing;

        public TransitionListenerWrapper(LzdhTargetRing lzdhTargetRing) {
            this.mParticleTargetRing = new WeakReference<>(lzdhTargetRing);
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onUpdate(Object obj, Collection<UpdateInfo> collection) {
            LzdhTargetRing lzdhTargetRing = this.mParticleTargetRing.get();
            if (lzdhTargetRing != null) {
                for (UpdateInfo updateInfo : collection) {
                    lzdhTargetRing.setParticlePos(updateInfo);
                }
            }
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onCancel(Object obj) {
            LzdhTargetRing lzdhTargetRing = this.mParticleTargetRing.get();
            if (lzdhTargetRing != null) {
                lzdhTargetRing.initParticles();
            }
        }
    }
}
