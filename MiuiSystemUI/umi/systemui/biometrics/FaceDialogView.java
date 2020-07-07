package com.android.systemui.biometrics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.android.systemui.plugins.R;

public class FaceDialogView extends BiometricDialogView {
    private float mIconOriginalY;
    private DialogOutlineProvider mOutlineProvider = new DialogOutlineProvider();
    /* access modifiers changed from: private */
    public int mSize;

    /* access modifiers changed from: protected */
    public int getDelayAfterAuthenticatedDurationMs() {
        return 500;
    }

    /* access modifiers changed from: protected */
    public int getHintStringResourceId() {
        return R.string.face_dialog_looking_for_face;
    }

    /* access modifiers changed from: protected */
    public int getIconDescriptionResourceId() {
        return R.string.accessibility_face_dialog_face_icon;
    }

    /* access modifiers changed from: protected */
    public boolean shouldAnimateForTransition(int i, int i2) {
        if (i == 2 && i2 == 0) {
            return true;
        }
        if (i == 0 && i2 == 1) {
            return false;
        }
        if (i == 1 && i2 == 2) {
            return true;
        }
        if (i == 2 && i2 == 1) {
            return true;
        }
        if (i == 1 && i2 == 3) {
            return true;
        }
        if (i == 3 && i2 == 4) {
            return true;
        }
        return i == 1 && i2 == 4;
    }

    private final class DialogOutlineProvider extends ViewOutlineProvider {
        float mY;

        private DialogOutlineProvider() {
        }

        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, (int) this.mY, FaceDialogView.this.mDialog.getWidth(), FaceDialogView.this.mDialog.getBottom(), FaceDialogView.this.getResources().getDimension(R.dimen.biometric_dialog_corner_size));
        }

        /* access modifiers changed from: package-private */
        public int calculateSmall() {
            return (FaceDialogView.this.mDialog.getHeight() - FaceDialogView.this.mBiometricIcon.getHeight()) - (((int) FaceDialogView.this.dpToPixels(16.0f)) * 2);
        }

        /* access modifiers changed from: package-private */
        public void setOutlineY(float f) {
            this.mY = f;
        }
    }

    public FaceDialogView(Context context, DialogViewCallback dialogViewCallback) {
        super(context, dialogViewCallback);
    }

    private void updateSize(int i) {
        float height = ((float) (this.mDialog.getHeight() - this.mBiometricIcon.getHeight())) - dpToPixels(16.0f);
        if (i == 1) {
            this.mTitleText.setVisibility(4);
            this.mErrorText.setVisibility(4);
            this.mNegativeButton.setVisibility(4);
            if (!TextUtils.isEmpty(this.mSubtitleText.getText())) {
                this.mSubtitleText.setVisibility(4);
            }
            if (!TextUtils.isEmpty(this.mDescriptionText.getText())) {
                this.mDescriptionText.setVisibility(4);
            }
            this.mBiometricIcon.setY(height);
            this.mDialog.setOutlineProvider(this.mOutlineProvider);
            DialogOutlineProvider dialogOutlineProvider = this.mOutlineProvider;
            dialogOutlineProvider.setOutlineY((float) dialogOutlineProvider.calculateSmall());
            this.mDialog.setClipToOutline(true);
            this.mDialog.invalidateOutline();
            this.mSize = i;
        } else if (this.mSize == 1 && i == 3) {
            this.mSize = 2;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{(float) this.mOutlineProvider.calculateSmall(), 0.0f});
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    FaceDialogView.this.lambda$updateSize$0$FaceDialogView(valueAnimator);
                }
            });
            ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{height, this.mIconOriginalY});
            ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    FaceDialogView.this.lambda$updateSize$1$FaceDialogView(valueAnimator);
                }
            });
            ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{dpToPixels(32.0f), 0.0f});
            ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    FaceDialogView.this.lambda$updateSize$2$FaceDialogView(valueAnimator);
                }
            });
            ValueAnimator ofFloat4 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    FaceDialogView.this.lambda$updateSize$3$FaceDialogView(valueAnimator);
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(150);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    FaceDialogView.this.mTitleText.setVisibility(0);
                    FaceDialogView.this.mErrorText.setVisibility(0);
                    FaceDialogView.this.mNegativeButton.setVisibility(0);
                    FaceDialogView.this.mTryAgainButton.setVisibility(0);
                    if (!TextUtils.isEmpty(FaceDialogView.this.mSubtitleText.getText())) {
                        FaceDialogView.this.mSubtitleText.setVisibility(0);
                    }
                    if (!TextUtils.isEmpty(FaceDialogView.this.mDescriptionText.getText())) {
                        FaceDialogView.this.mDescriptionText.setVisibility(0);
                    }
                }

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    int unused = FaceDialogView.this.mSize = 3;
                }
            });
            animatorSet.play(ofFloat).with(ofFloat2).with(ofFloat4).with(ofFloat3);
            animatorSet.start();
        } else if (this.mSize == 3) {
            this.mDialog.setClipToOutline(false);
            this.mDialog.invalidateOutline();
            this.mBiometricIcon.setY(this.mIconOriginalY);
            this.mSize = i;
        }
    }

    public /* synthetic */ void lambda$updateSize$0$FaceDialogView(ValueAnimator valueAnimator) {
        this.mOutlineProvider.setOutlineY(((Float) valueAnimator.getAnimatedValue()).floatValue());
        this.mDialog.invalidateOutline();
    }

    public /* synthetic */ void lambda$updateSize$1$FaceDialogView(ValueAnimator valueAnimator) {
        this.mBiometricIcon.setY(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public /* synthetic */ void lambda$updateSize$2$FaceDialogView(ValueAnimator valueAnimator) {
        this.mErrorText.setTranslationY(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public /* synthetic */ void lambda$updateSize$3$FaceDialogView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mTitleText.setAlpha(floatValue);
        this.mErrorText.setAlpha(floatValue);
        this.mNegativeButton.setAlpha(floatValue);
        this.mTryAgainButton.setAlpha(floatValue);
        if (!TextUtils.isEmpty(this.mSubtitleText.getText())) {
            this.mSubtitleText.setAlpha(floatValue);
        }
        if (!TextUtils.isEmpty(this.mDescriptionText.getText())) {
            this.mDescriptionText.setAlpha(floatValue);
        }
    }

    public void onSaveState(Bundle bundle) {
        super.onSaveState(bundle);
        bundle.putInt("key_dialog_size", this.mSize);
    }

    /* access modifiers changed from: protected */
    public void handleClearMessage(boolean z) {
        if (!z) {
            updateState(1);
            this.mErrorText.setText(getHintStringResourceId());
            this.mErrorText.setTextColor(this.mTextColor);
            this.mErrorText.setVisibility(0);
            return;
        }
        updateState(0);
        this.mErrorText.setVisibility(4);
    }

    public void restoreState(Bundle bundle) {
        super.restoreState(bundle);
        this.mSize = bundle.getInt("key_dialog_size");
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mIconOriginalY == 0.0f) {
            this.mIconOriginalY = this.mBiometricIcon.getY();
        }
        int i5 = this.mSize;
        if (i5 != 0) {
            if (i5 == 1) {
                updateSize(1);
            }
        } else if (!requiresConfirmation()) {
            updateSize(1);
        } else {
            updateSize(3);
        }
    }

    public void showErrorMessage(String str) {
        super.showErrorMessage(str);
        if (this.mSize == 1) {
            updateSize(3);
        }
    }

    public void showTryAgainButton(boolean z) {
        if (z && this.mSize == 1) {
            updateSize(3);
        } else if (z) {
            this.mTryAgainButton.setVisibility(0);
        } else {
            this.mTryAgainButton.setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    public int getAuthenticatedAccessibilityResourceId() {
        return this.mRequireConfirmation ? 17040029 : 17040030;
    }

    /* access modifiers changed from: protected */
    public boolean shouldGrayAreaDismissDialog() {
        return this.mSize != 1;
    }

    /* access modifiers changed from: protected */
    public Drawable getAnimationForTransition(int i, int i2) {
        int i3 = R.drawable.face_dialog_face_to_error;
        if (!(i == 2 && i2 == 0)) {
            if (!((i == 0 && i2 == 1) || (i == 1 && i2 == 2))) {
                if (!(i == 2 && i2 == 1)) {
                    if (i == 1 && i2 == 3) {
                        i3 = R.drawable.face_dialog_face_gray_to_face_blue;
                    } else if (i == 3 && i2 == 4) {
                        i3 = R.drawable.face_dialog_face_blue_to_checkmark;
                    } else if (i != 1 || i2 != 4) {
                        return null;
                    } else {
                        i3 = R.drawable.face_dialog_face_gray_to_checkmark;
                    }
                }
            }
            return this.mContext.getDrawable(i3);
        }
        i3 = R.drawable.face_dialog_error_to_face;
        return this.mContext.getDrawable(i3);
    }

    /* access modifiers changed from: private */
    public float dpToPixels(float f) {
        return f * (((float) this.mContext.getResources().getDisplayMetrics().densityDpi) / 160.0f);
    }
}
