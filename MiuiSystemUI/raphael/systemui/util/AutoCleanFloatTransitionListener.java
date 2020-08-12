package com.android.systemui.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import miuix.animation.Folme;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;

public class AutoCleanFloatTransitionListener extends TransitionListener {
    private Map<String, Float> mInfos = new HashMap();
    private boolean mStarted;
    private Object mTarget;

    public void onEnd() {
    }

    public void onStart() {
    }

    public void onUpdate(Map<String, Float> map) {
        throw null;
    }

    public AutoCleanFloatTransitionListener(Object obj) {
        this.mTarget = obj;
    }

    public void onBegin(Object obj, UpdateInfo updateInfo) {
        this.mInfos.put(updateInfo.property.getName(), Float.valueOf(updateInfo.getFloatValue()));
    }

    public void onUpdate(Object obj, Collection<UpdateInfo> collection) {
        if (!this.mStarted) {
            this.mStarted = true;
            onStart();
        }
        collection.forEach(new Consumer() {
            public final void accept(Object obj) {
                AutoCleanFloatTransitionListener.this.lambda$onUpdate$0$AutoCleanFloatTransitionListener((UpdateInfo) obj);
            }
        });
        onUpdate(this.mInfos);
    }

    public /* synthetic */ void lambda$onUpdate$0$AutoCleanFloatTransitionListener(UpdateInfo updateInfo) {
        this.mInfos.put(updateInfo.property.getName(), Float.valueOf(updateInfo.getFloatValue()));
    }

    public void onComplete(Object obj, UpdateInfo updateInfo) {
        this.mInfos.put(updateInfo.property.getName(), Float.valueOf(updateInfo.getFloatValue()));
    }

    public void onComplete(Object obj) {
        onEnd();
        this.mStarted = false;
        Object obj2 = this.mTarget;
        if (obj2 != null) {
            Folme.clean(obj2);
        }
    }

    public float getFloatValue(String str, float f) {
        Float f2 = this.mInfos.get(str);
        return f2 != null ? f2.floatValue() : f;
    }
}
