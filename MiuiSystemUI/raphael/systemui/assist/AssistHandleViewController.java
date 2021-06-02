package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.util.MathUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0015R$id;
import com.android.systemui.CornerHandleView;
import com.android.systemui.statusbar.phone.NavigationBarTransitions;

public class AssistHandleViewController implements NavigationBarTransitions.DarkIntensityListener {
    @VisibleForTesting
    boolean mAssistHintBlocked = false;
    private CornerHandleView mAssistHintLeft;
    private CornerHandleView mAssistHintRight;
    @VisibleForTesting
    boolean mAssistHintVisible;
    private int mBottomOffset;
    private Handler mHandler;

    public AssistHandleViewController(Handler handler, View view) {
        this.mHandler = handler;
        this.mAssistHintLeft = (CornerHandleView) view.findViewById(C0015R$id.assist_hint_left);
        this.mAssistHintRight = (CornerHandleView) view.findViewById(C0015R$id.assist_hint_right);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationBarTransitions.DarkIntensityListener
    public void onDarkIntensity(float f) {
        this.mAssistHintLeft.updateDarkness(f);
        this.mAssistHintRight.updateDarkness(f);
    }

    public void setBottomOffset(int i) {
        if (this.mBottomOffset != i) {
            this.mBottomOffset = i;
            if (this.mAssistHintVisible) {
                hideAssistHandles();
                lambda$setAssistHintVisible$0(true);
            }
        }
    }

    /* renamed from: setAssistHintVisible */
    public void lambda$setAssistHintVisible$0(boolean z) {
        if (!this.mHandler.getLooper().isCurrentThread()) {
            this.mHandler.post(new Runnable(z) {
                /* class com.android.systemui.assist.$$Lambda$AssistHandleViewController$UwVsLlCfHOnxZGhueV8hx6rT9Z4 */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AssistHandleViewController.this.lambda$setAssistHintVisible$0$AssistHandleViewController(this.f$1);
                }
            });
        } else if ((!this.mAssistHintBlocked || !z) && this.mAssistHintVisible != z) {
            this.mAssistHintVisible = z;
            fade(this.mAssistHintLeft, z, true);
            fade(this.mAssistHintRight, this.mAssistHintVisible, false);
        }
    }

    /* renamed from: setAssistHintBlocked */
    public void lambda$setAssistHintBlocked$1(boolean z) {
        if (!this.mHandler.getLooper().isCurrentThread()) {
            this.mHandler.post(new Runnable(z) {
                /* class com.android.systemui.assist.$$Lambda$AssistHandleViewController$aiftoeNqQkXVTgh21US20St6A */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AssistHandleViewController.this.lambda$setAssistHintBlocked$1$AssistHandleViewController(this.f$1);
                }
            });
            return;
        }
        this.mAssistHintBlocked = z;
        if (this.mAssistHintVisible && z) {
            hideAssistHandles();
        }
    }

    private void hideAssistHandles() {
        this.mAssistHintLeft.setVisibility(8);
        this.mAssistHintRight.setVisibility(8);
        this.mAssistHintVisible = false;
    }

    /* access modifiers changed from: package-private */
    public Animator getHandleAnimator(View view, float f, float f2, boolean z, long j, Interpolator interpolator) {
        float lerp = MathUtils.lerp(2.0f, 1.0f, f);
        float lerp2 = MathUtils.lerp(2.0f, 1.0f, f2);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.SCALE_X, lerp, lerp2);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, View.SCALE_Y, lerp, lerp2);
        float lerp3 = MathUtils.lerp(0.2f, 0.0f, f);
        float lerp4 = MathUtils.lerp(0.2f, 0.0f, f2);
        float f3 = (float) (z ? -1 : 1);
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, f3 * lerp3 * ((float) view.getWidth()), f3 * lerp4 * ((float) view.getWidth()));
        ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, (lerp3 * ((float) view.getHeight())) + ((float) this.mBottomOffset), (lerp4 * ((float) view.getHeight())) + ((float) this.mBottomOffset));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ofFloat).with(ofFloat2);
        animatorSet.play(ofFloat).with(ofFloat3);
        animatorSet.play(ofFloat).with(ofFloat4);
        animatorSet.setDuration(j);
        animatorSet.setInterpolator(interpolator);
        return animatorSet;
    }

    private void fade(View view, boolean z, boolean z2) {
        if (z) {
            view.animate().cancel();
            view.setAlpha(1.0f);
            view.setVisibility(0);
            AnimatorSet animatorSet = new AnimatorSet();
            Animator handleAnimator = getHandleAnimator(view, 0.0f, 1.1f, z2, 750, new PathInterpolator(0.0f, 0.45f, 0.67f, 1.0f));
            PathInterpolator pathInterpolator = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);
            Animator handleAnimator2 = getHandleAnimator(view, 1.1f, 0.97f, z2, 400, pathInterpolator);
            Animator handleAnimator3 = getHandleAnimator(view, 0.97f, 1.02f, z2, 400, pathInterpolator);
            Animator handleAnimator4 = getHandleAnimator(view, 1.02f, 1.0f, z2, 400, pathInterpolator);
            animatorSet.play(handleAnimator).before(handleAnimator2);
            animatorSet.play(handleAnimator2).before(handleAnimator3);
            animatorSet.play(handleAnimator3).before(handleAnimator4);
            animatorSet.start();
            return;
        }
        view.animate().cancel();
        view.animate().setInterpolator(new AccelerateInterpolator(1.5f)).setDuration(250).alpha(0.0f);
    }
}
