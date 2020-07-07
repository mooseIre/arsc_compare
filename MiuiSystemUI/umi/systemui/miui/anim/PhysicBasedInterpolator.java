package com.android.systemui.miui.anim;

import android.view.animation.Interpolator;

public class PhysicBasedInterpolator implements Interpolator {
    private float c;
    private float c1;
    private float c2;
    private float damping = 0.95f;
    private float initial = -1.0f;
    private float k;
    private float m = 1.0f;
    private float r;
    private float response = 0.6f;
    private float w;

    public PhysicBasedInterpolator(float f, float f2) {
        double pow = Math.pow(6.283185307179586d / ((double) this.response), 2.0d);
        float f3 = this.m;
        this.k = (float) (pow * ((double) f3));
        this.c = (float) (((((double) this.damping) * 12.566370614359172d) * ((double) f3)) / ((double) this.response));
        float f4 = f3 * 4.0f * this.k;
        float f5 = this.c;
        float f6 = this.m;
        this.w = ((float) Math.sqrt((double) (f4 - (f5 * f5)))) / (f6 * 2.0f);
        this.r = -((this.c / 2.0f) * f6);
        float f7 = this.initial;
        this.c1 = f7;
        this.c2 = (0.0f - (this.r * f7)) / this.w;
        this.damping = f;
        this.response = f2;
    }

    public float getInterpolation(float f) {
        return (float) ((Math.pow(2.718281828459045d, (double) (this.r * f)) * ((((double) this.c1) * Math.cos((double) (this.w * f))) + (((double) this.c2) * Math.sin((double) (this.w * f))))) + 1.0d);
    }
}
