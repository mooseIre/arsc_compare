package com.android.keyguard.AwesomeLockScreenImp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import miui.maml.data.Expression;
import miui.maml.elements.AdvancedSlider;
import org.w3c.dom.Element;

public class UnlockerScreenElement extends AdvancedSlider {
    private boolean mAlwaysShow;
    private Expression mDelay;
    private boolean mNoUnlock;
    private float mPreX;
    private float mPreY;
    private boolean mUnlockingHide;

    public UnlockerScreenElement(Element element, LockScreenRoot lockScreenRoot) {
        super(element, lockScreenRoot);
        this.mAlwaysShow = Boolean.parseBoolean(element.getAttribute("alwaysShow"));
        this.mNoUnlock = Boolean.parseBoolean(element.getAttribute("noUnlock"));
        this.mDelay = Expression.build(getVariables(), element.getAttribute("delay"));
        ((AdvancedSlider) this).mIsHaptic = ((AdvancedSlider) this).mIsHaptic || element.getAttribute("haptic").isEmpty();
    }

    public void finish() {
        UnlockerScreenElement.super.finish();
        this.mUnlockingHide = false;
    }

    public boolean isVisible() {
        return UnlockerScreenElement.super.isVisible() && !this.mUnlockingHide;
    }

    public void endUnlockMoving(UnlockerScreenElement unlockerScreenElement) {
        if (unlockerScreenElement != this && !this.mAlwaysShow) {
            this.mUnlockingHide = false;
        }
    }

    public void startUnlockMoving(UnlockerScreenElement unlockerScreenElement) {
        if (unlockerScreenElement != this && !this.mAlwaysShow) {
            this.mUnlockingHide = true;
            resetInner();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        UnlockerScreenElement.super.onStart();
        getLockScreenRoot().startUnlockMoving(this);
        getLockScreenRoot().pokeWakelock();
    }

    private LockScreenRoot getLockScreenRoot() {
        return ((AdvancedSlider) this).mRoot;
    }

    /* access modifiers changed from: protected */
    public void onCancel() {
        UnlockerScreenElement.super.onCancel();
        getLockScreenRoot().endUnlockMoving(this);
    }

    /* access modifiers changed from: protected */
    public void onMove(float f, float f2) {
        UnlockerScreenElement.super.onMove(f, f2);
        float f3 = f - this.mPreX;
        float f4 = f2 - this.mPreY;
        if ((f3 * f3) + (f4 * f4) >= 50.0f) {
            getLockScreenRoot().pokeWakelock();
            this.mPreX = f;
            this.mPreY = f2;
        }
    }

    /* access modifiers changed from: protected */
    public boolean onLaunch(String str, Intent intent) {
        UnlockerScreenElement.super.onLaunch(str, intent);
        int i = 0;
        if (!this.mNoUnlock || intent != null) {
            getLockScreenRoot().endUnlockMoving(this);
            try {
                LockScreenRoot lockScreenRoot = getLockScreenRoot();
                if (this.mDelay != null) {
                    i = (int) this.mDelay.evaluate();
                }
                lockScreenRoot.unlocked(intent, i);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e("LockScreen_UnlockerScreenElement", e.toString());
                e.printStackTrace();
                return true;
            }
        } else {
            getLockScreenRoot().pokeWakelock();
            return false;
        }
    }
}
