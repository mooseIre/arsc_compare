package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import java.util.function.Consumer;

public class NotificationDozeHelper {
    private static final int DOZE_ANIMATOR_TAG = C0015R$id.doze_intensity_tag;
    private final ColorMatrix mGrayscaleColorMatrix = new ColorMatrix();

    public void updateGrayscale(ImageView imageView, float f) {
        if (f > 0.0f) {
            updateGrayscaleMatrix(f);
            imageView.setColorFilter(new ColorMatrixColorFilter(this.mGrayscaleColorMatrix));
            return;
        }
        imageView.setColorFilter((ColorFilter) null);
    }

    public void startIntensityAnimation(ValueAnimator.AnimatorUpdateListener animatorUpdateListener, boolean z, long j, Animator.AnimatorListener animatorListener) {
        float f = 0.0f;
        float f2 = z ? 0.0f : 1.0f;
        if (z) {
            f = 1.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f2, f);
        ofFloat.addUpdateListener(animatorUpdateListener);
        ofFloat.setDuration(500L);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.setStartDelay(j);
        if (animatorListener != null) {
            ofFloat.addListener(animatorListener);
        }
        ofFloat.start();
    }

    public void setDozing(Consumer<Float> consumer, boolean z, boolean z2, long j, final View view) {
        if (z2) {
            startIntensityAnimation(new ValueAnimator.AnimatorUpdateListener(consumer) {
                /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationDozeHelper$VENFYNxPWcqtSl2MMr8F4aMPH78 */
                public final /* synthetic */ Consumer f$0;

                {
                    this.f$0 = r1;
                }

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.accept((Float) valueAnimator.getAnimatedValue());
                }
            }, z, j, new AnimatorListenerAdapter(this) {
                /* class com.android.systemui.statusbar.notification.NotificationDozeHelper.AnonymousClass3 */

                public void onAnimationEnd(Animator animator) {
                    view.setTag(NotificationDozeHelper.DOZE_ANIMATOR_TAG, null);
                }

                public void onAnimationStart(Animator animator) {
                    view.setTag(NotificationDozeHelper.DOZE_ANIMATOR_TAG, animator);
                }
            });
            return;
        }
        Animator animator = (Animator) view.getTag(DOZE_ANIMATOR_TAG);
        if (animator != null) {
            animator.cancel();
        }
        consumer.accept(Float.valueOf(z ? 1.0f : 0.0f));
    }

    public void updateGrayscaleMatrix(float f) {
        this.mGrayscaleColorMatrix.setSaturation(1.0f - f);
    }
}
