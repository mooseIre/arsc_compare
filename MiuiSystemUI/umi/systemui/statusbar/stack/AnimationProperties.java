package com.android.systemui.statusbar.stack;

import android.animation.AnimatorListenerAdapter;
import android.util.ArrayMap;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;

public class AnimationProperties {
    public long delay;
    public long duration;
    private ArrayMap<Property, Interpolator> mInterpolatorMap;

    public AnimationFilter getAnimationFilter() {
        throw null;
    }

    public AnimatorListenerAdapter getAnimationFinishListener() {
        return null;
    }

    public boolean wasAdded(View view) {
        return false;
    }

    public Interpolator getCustomInterpolator(View view, Property property) {
        ArrayMap<Property, Interpolator> arrayMap = this.mInterpolatorMap;
        if (arrayMap != null) {
            return arrayMap.get(property);
        }
        return null;
    }

    public void combineCustomInterpolators(AnimationProperties animationProperties) {
        ArrayMap<Property, Interpolator> arrayMap = animationProperties.mInterpolatorMap;
        if (arrayMap != null) {
            if (this.mInterpolatorMap == null) {
                this.mInterpolatorMap = new ArrayMap<>();
            }
            this.mInterpolatorMap.putAll(arrayMap);
        }
    }

    public AnimationProperties setCustomInterpolator(Property property, Interpolator interpolator) {
        if (this.mInterpolatorMap == null) {
            this.mInterpolatorMap = new ArrayMap<>();
        }
        this.mInterpolatorMap.put(property, interpolator);
        return this;
    }

    public AnimationProperties setDuration(long j) {
        this.duration = j;
        return this;
    }

    public AnimationProperties setDelay(long j) {
        this.delay = j;
        return this;
    }

    public AnimationProperties resetCustomInterpolators() {
        this.mInterpolatorMap = null;
        return this;
    }
}
