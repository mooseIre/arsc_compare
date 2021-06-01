package com.android.systemui.assist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;

public class AssistOrbContainer extends FrameLayout {
    private boolean mAnimatingOut;
    private View mNavbarScrim;
    private AssistOrbView mOrb;
    private View mScrim;

    public AssistOrbContainer(Context context) {
        this(context, null);
    }

    public AssistOrbContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AssistOrbContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mScrim = findViewById(C0015R$id.assist_orb_scrim);
        this.mNavbarScrim = findViewById(C0015R$id.assist_orb_navbar_scrim);
        this.mOrb = (AssistOrbView) findViewById(C0015R$id.assist_orb);
    }

    public void show(boolean z, boolean z2) {
        if (z) {
            if (getVisibility() != 0) {
                setVisibility(0);
                if (z2) {
                    startEnterAnimation();
                } else {
                    reset();
                }
            }
        } else if (z2) {
            startExitAnimation(new Runnable() {
                /* class com.android.systemui.assist.AssistOrbContainer.AnonymousClass1 */

                public void run() {
                    AssistOrbContainer.this.mAnimatingOut = false;
                    AssistOrbContainer.this.setVisibility(8);
                }
            });
        } else {
            setVisibility(8);
        }
    }

    private void reset() {
        this.mAnimatingOut = false;
        this.mOrb.reset();
        this.mScrim.setAlpha(1.0f);
        this.mNavbarScrim.setAlpha(1.0f);
    }

    private void startEnterAnimation() {
        if (!this.mAnimatingOut) {
            this.mOrb.startEnterAnimation();
            this.mScrim.setAlpha(0.0f);
            this.mNavbarScrim.setAlpha(0.0f);
            post(new Runnable() {
                /* class com.android.systemui.assist.AssistOrbContainer.AnonymousClass2 */

                public void run() {
                    AssistOrbContainer.this.mScrim.animate().alpha(1.0f).setDuration(300).setStartDelay(0).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                    AssistOrbContainer.this.mNavbarScrim.animate().alpha(1.0f).setDuration(300).setStartDelay(0).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                }
            });
        }
    }

    private void startExitAnimation(Runnable runnable) {
        if (!this.mAnimatingOut) {
            this.mAnimatingOut = true;
            this.mOrb.startExitAnimation(150);
            this.mScrim.animate().alpha(0.0f).setDuration(250).setStartDelay(150).setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mNavbarScrim.animate().alpha(0.0f).setDuration(250).setStartDelay(150).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).withEndAction(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public boolean isShowing() {
        return getVisibility() == 0 && !this.mAnimatingOut;
    }

    public AssistOrbView getOrb() {
        return this.mOrb;
    }
}
