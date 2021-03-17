package com.android.systemui.biometrics;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.android.systemui.C0012R$dimen;

public class AuthPanelController extends ViewOutlineProvider {
    private int mContainerHeight;
    private int mContainerWidth;
    private int mContentHeight;
    private int mContentWidth;
    private final Context mContext;
    private float mCornerRadius;
    private int mMargin;
    private final View mPanelView;
    private boolean mUseFullScreen;

    public void getOutline(View view, Outline outline) {
        int i;
        int i2 = this.mContainerWidth;
        int i3 = (i2 - this.mContentWidth) / 2;
        int i4 = i2 - i3;
        int i5 = this.mContentHeight;
        int i6 = this.mContainerHeight;
        if (i5 < i6) {
            i = (i6 - i5) - this.mMargin;
        } else {
            i = this.mMargin;
        }
        outline.setRoundRect(i3, i, i4, (this.mContainerHeight - this.mMargin) + 1, this.mCornerRadius);
    }

    public void setContainerDimensions(int i, int i2) {
        this.mContainerWidth = i;
        this.mContainerHeight = i2;
    }

    public void setUseFullScreen(boolean z) {
        this.mUseFullScreen = z;
    }

    public void updateForContentDimensions(int i, int i2, int i3) {
        int i4;
        float f;
        if (this.mContainerWidth == 0 || this.mContainerHeight == 0) {
            Log.w("BiometricPrompt/AuthPanelController", "Not done measuring yet");
            return;
        }
        if (this.mUseFullScreen) {
            i4 = 0;
        } else {
            i4 = (int) this.mContext.getResources().getDimension(C0012R$dimen.biometric_dialog_border_padding);
        }
        if (this.mUseFullScreen) {
            f = 0.0f;
        } else {
            f = this.mContext.getResources().getDimension(C0012R$dimen.biometric_dialog_corner_size);
        }
        if (i3 > 0) {
            ValueAnimator ofInt = ValueAnimator.ofInt(this.mMargin, i4);
            ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.biometrics.$$Lambda$AuthPanelController$FfAW_fJIxdruLyni5niGyYZPKQI */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AuthPanelController.this.lambda$updateForContentDimensions$2$AuthPanelController(valueAnimator);
                }
            });
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mCornerRadius, f);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.biometrics.$$Lambda$AuthPanelController$InH1YHCYbFS1oQ8661noD2sY0tQ */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AuthPanelController.this.lambda$updateForContentDimensions$3$AuthPanelController(valueAnimator);
                }
            });
            ValueAnimator ofInt2 = ValueAnimator.ofInt(this.mContentHeight, i2);
            ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.biometrics.$$Lambda$AuthPanelController$gEQd5p8htInmfU5UNk3JBrR4jEs */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AuthPanelController.this.lambda$updateForContentDimensions$4$AuthPanelController(valueAnimator);
                }
            });
            ValueAnimator ofInt3 = ValueAnimator.ofInt(this.mContentWidth, i);
            ofInt3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.biometrics.$$Lambda$AuthPanelController$T_ye3d_LoD4zTMypSnctnhLSMzU */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AuthPanelController.this.lambda$updateForContentDimensions$5$AuthPanelController(valueAnimator);
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration((long) i3);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(ofFloat, ofInt2, ofInt3, ofInt);
            animatorSet.start();
            return;
        }
        this.mMargin = i4;
        this.mCornerRadius = f;
        this.mContentWidth = i;
        this.mContentHeight = i2;
        this.mPanelView.invalidateOutline();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateForContentDimensions$2 */
    public /* synthetic */ void lambda$updateForContentDimensions$2$AuthPanelController(ValueAnimator valueAnimator) {
        this.mMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateForContentDimensions$3 */
    public /* synthetic */ void lambda$updateForContentDimensions$3$AuthPanelController(ValueAnimator valueAnimator) {
        this.mCornerRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateForContentDimensions$4 */
    public /* synthetic */ void lambda$updateForContentDimensions$4$AuthPanelController(ValueAnimator valueAnimator) {
        this.mContentHeight = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        this.mPanelView.invalidateOutline();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateForContentDimensions$5 */
    public /* synthetic */ void lambda$updateForContentDimensions$5$AuthPanelController(ValueAnimator valueAnimator) {
        this.mContentWidth = ((Integer) valueAnimator.getAnimatedValue()).intValue();
    }

    /* access modifiers changed from: package-private */
    public int getContainerWidth() {
        return this.mContainerWidth;
    }

    /* access modifiers changed from: package-private */
    public int getContainerHeight() {
        return this.mContainerHeight;
    }

    AuthPanelController(Context context, View view) {
        this.mContext = context;
        this.mPanelView = view;
        this.mCornerRadius = context.getResources().getDimension(C0012R$dimen.biometric_dialog_corner_size);
        this.mMargin = (int) context.getResources().getDimension(C0012R$dimen.biometric_dialog_border_padding);
        this.mPanelView.setOutlineProvider(this);
        this.mPanelView.setClipToOutline(true);
    }
}
