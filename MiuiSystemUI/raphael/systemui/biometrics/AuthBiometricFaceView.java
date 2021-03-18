package com.android.systemui.biometrics;

import android.content.Context;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;

public class AuthBiometricFaceView extends AuthBiometricView {
    @VisibleForTesting
    IconController mIconController;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getDelayAfterAuthenticatedDurationMs() {
        return 500;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getStateForAfterError() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public boolean supportsSmallDialog() {
        return true;
    }

    public static class IconController extends Animatable2.AnimationCallback {
        Context mContext;
        ImageView mIconView;
        boolean mLastPulseLightToDark;
        int mState;

        IconController(Context context, ImageView imageView, TextView textView) {
            this.mContext = context;
            this.mIconView = imageView;
            new Handler(Looper.getMainLooper());
            showStaticDrawable(C0013R$drawable.face_dialog_pulse_dark_to_light);
        }

        /* access modifiers changed from: package-private */
        public void animateOnce(int i) {
            animateIcon(i, false);
        }

        public void showStaticDrawable(int i) {
            this.mIconView.setImageDrawable(this.mContext.getDrawable(i));
        }

        /* access modifiers changed from: package-private */
        public void animateIcon(int i, boolean z) {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) this.mContext.getDrawable(i);
            this.mIconView.setImageDrawable(animatedVectorDrawable);
            animatedVectorDrawable.forceAnimationOnUI();
            if (z) {
                animatedVectorDrawable.registerAnimationCallback(this);
            }
            animatedVectorDrawable.start();
        }

        /* access modifiers changed from: package-private */
        public void startPulsing() {
            this.mLastPulseLightToDark = false;
            animateIcon(C0013R$drawable.face_dialog_pulse_dark_to_light, true);
        }

        /* access modifiers changed from: package-private */
        public void pulseInNextDirection() {
            int i;
            if (this.mLastPulseLightToDark) {
                i = C0013R$drawable.face_dialog_pulse_dark_to_light;
            } else {
                i = C0013R$drawable.face_dialog_pulse_light_to_dark;
            }
            animateIcon(i, true);
            this.mLastPulseLightToDark = !this.mLastPulseLightToDark;
        }

        public void onAnimationEnd(Drawable drawable) {
            super.onAnimationEnd(drawable);
            int i = this.mState;
            if (i == 2 || i == 3) {
                pulseInNextDirection();
            }
        }

        public void updateState(int i, int i2) {
            boolean z = i == 4 || i == 3;
            if (i2 == 1) {
                showStaticDrawable(C0013R$drawable.face_dialog_pulse_dark_to_light);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_authenticating));
            } else if (i2 == 2) {
                startPulsing();
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_authenticating));
            } else if (i == 5 && i2 == 6) {
                animateOnce(C0013R$drawable.face_dialog_dark_to_checkmark);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_confirmed));
            } else if (z && i2 == 0) {
                animateOnce(C0013R$drawable.face_dialog_error_to_idle);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_idle));
            } else if (z && i2 == 6) {
                animateOnce(C0013R$drawable.face_dialog_dark_to_checkmark);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_authenticated));
            } else if (i2 == 4 && i != 4) {
                animateOnce(C0013R$drawable.face_dialog_dark_to_error);
            } else if (i == 2 && i2 == 6) {
                animateOnce(C0013R$drawable.face_dialog_dark_to_checkmark);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_authenticated));
            } else if (i2 == 5) {
                animateOnce(C0013R$drawable.face_dialog_wink_from_dark);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_authenticated));
            } else if (i2 == 0) {
                showStaticDrawable(C0013R$drawable.face_dialog_idle_static);
                this.mIconView.setContentDescription(this.mContext.getString(C0021R$string.biometric_dialog_face_icon_description_idle));
            } else {
                Log.w("BiometricPrompt/AuthBiometricFaceView", "Unhandled state: " + i2);
            }
            this.mState = i2;
        }
    }

    public AuthBiometricFaceView(Context context) {
        this(context, null);
    }

    public AuthBiometricFaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void handleResetAfterError() {
        resetErrorView(((LinearLayout) this).mContext, this.mIndicatorView);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void handleResetAfterHelp() {
        resetErrorView(((LinearLayout) this).mContext, this.mIndicatorView);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIconController = new IconController(((LinearLayout) this).mContext, this.mIconView, this.mIndicatorView);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void updateState(int i) {
        this.mIconController.updateState(this.mState, i);
        if (i == 1 || (i == 2 && this.mSize == 2)) {
            resetErrorView(((LinearLayout) this).mContext, this.mIndicatorView);
        }
        super.updateState(i);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onAuthenticationFailed(String str) {
        if (this.mSize == 2) {
            this.mTryAgainButton.setVisibility(0);
            this.mPositiveButton.setVisibility(8);
        }
        super.onAuthenticationFailed(str);
    }

    static void resetErrorView(Context context, TextView textView) {
        textView.setTextColor(context.getResources().getColor(C0011R$color.biometric_dialog_gray, context.getTheme()));
        textView.setVisibility(4);
    }
}
