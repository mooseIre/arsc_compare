package com.android.systemui.biometrics;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.util.leak.RotationUtils;

public abstract class BiometricDialogView extends LinearLayout {
    /* access modifiers changed from: private */
    public boolean mAnimatingAway;
    /* access modifiers changed from: private */
    public final float mAnimationTranslationOffset;
    protected final ImageView mBiometricIcon;
    private Bundle mBundle;
    /* access modifiers changed from: private */
    public final DialogViewCallback mCallback;
    protected final TextView mDescriptionText;
    private final DevicePolicyManager mDevicePolicyManager;
    protected final LinearLayout mDialog;
    private final float mDialogHeight;
    private final float mDialogWidth;
    private final int mErrorColor;
    protected final TextView mErrorText;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what != 1) {
                Log.e("BiometricDialogView", "Unhandled message: " + message.what);
                return;
            }
            BiometricDialogView.this.handleClearMessage(((Boolean) message.obj).booleanValue());
        }
    };
    private int mLastState;
    protected final ViewGroup mLayout;
    /* access modifiers changed from: private */
    public final Interpolator mLinearOutSlowIn;
    protected final Button mNegativeButton;
    protected final Button mPositiveButton;
    protected boolean mRequireConfirmation;
    private final Runnable mShowAnimationRunnable = new Runnable() {
        public void run() {
            BiometricDialogView.this.mLayout.animate().alpha(1.0f).setDuration(250).setInterpolator(BiometricDialogView.this.mLinearOutSlowIn).withLayer().start();
            BiometricDialogView.this.mDialog.animate().translationY(0.0f).setDuration(250).setInterpolator(BiometricDialogView.this.mLinearOutSlowIn).withLayer().start();
        }
    };
    private boolean mSkipIntro;
    protected final TextView mSubtitleText;
    protected final int mTextColor;
    protected final TextView mTitleText;
    protected final Button mTryAgainButton;
    private int mUserId;
    private final UserManager mUserManager;
    private boolean mWasForceRemoved;
    /* access modifiers changed from: private */
    public final WindowManager mWindowManager;
    private final IBinder mWindowToken = new Binder();

    /* access modifiers changed from: protected */
    public abstract Drawable getAnimationForTransition(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract int getAuthenticatedAccessibilityResourceId();

    /* access modifiers changed from: protected */
    public abstract int getDelayAfterAuthenticatedDurationMs();

    /* access modifiers changed from: protected */
    public abstract int getHintStringResourceId();

    /* access modifiers changed from: protected */
    public abstract int getIconDescriptionResourceId();

    /* access modifiers changed from: protected */
    public abstract void handleClearMessage(boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean shouldAnimateForTransition(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract boolean shouldGrayAreaDismissDialog();

    public void showTryAgainButton(boolean z) {
    }

    public BiometricDialogView(Context context, DialogViewCallback dialogViewCallback) {
        super(context);
        this.mCallback = dialogViewCallback;
        this.mLinearOutSlowIn = Interpolators.LINEAR_OUT_SLOW_IN;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(WindowManager.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
        this.mAnimationTranslationOffset = getResources().getDimension(R.dimen.biometric_dialog_animation_translation_offset);
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(new int[]{16844099, 16842808});
        int i = 0;
        this.mErrorColor = obtainStyledAttributes.getColor(0, 0);
        this.mTextColor = obtainStyledAttributes.getColor(1, 0);
        obtainStyledAttributes.recycle();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.mDialogWidth = (float) Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mDialogHeight = (float) displayMetrics.heightPixels;
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.biometric_dialog, this, false);
        this.mLayout = viewGroup;
        addView(viewGroup);
        this.mLayout.setOnKeyListener(new View.OnKeyListener() {
            boolean downPressed = false;

            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i != 4) {
                    return false;
                }
                if (keyEvent.getAction() == 0 && !this.downPressed) {
                    this.downPressed = true;
                } else if (keyEvent.getAction() == 0) {
                    this.downPressed = false;
                } else if (keyEvent.getAction() == 1 && this.downPressed) {
                    this.downPressed = false;
                    BiometricDialogView.this.mCallback.onUserCanceled();
                }
                return true;
            }
        });
        View findViewById = this.mLayout.findViewById(R.id.space);
        View findViewById2 = this.mLayout.findViewById(R.id.left_space);
        View findViewById3 = this.mLayout.findViewById(R.id.right_space);
        this.mDialog = (LinearLayout) this.mLayout.findViewById(R.id.dialog);
        this.mTitleText = (TextView) this.mLayout.findViewById(R.id.title);
        this.mSubtitleText = (TextView) this.mLayout.findViewById(R.id.subtitle);
        this.mDescriptionText = (TextView) this.mLayout.findViewById(R.id.description);
        this.mBiometricIcon = (ImageView) this.mLayout.findViewById(R.id.biometric_icon);
        this.mErrorText = (TextView) this.mLayout.findViewById(R.id.error);
        this.mNegativeButton = (Button) this.mLayout.findViewById(R.id.button2);
        this.mPositiveButton = (Button) this.mLayout.findViewById(R.id.button1);
        this.mTryAgainButton = (Button) this.mLayout.findViewById(R.id.button_try_again);
        Space space = (Space) this.mLayout.findViewById(R.id.fod_icon_space);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) space.getLayoutParams();
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            Rect fodPosition = MiuiGxzwManager.getFodPosition(getContext());
            int height = ((((int) this.mDialogHeight) - fodPosition.top) + (fodPosition.height() / 2)) - getResources().getDimensionPixelOffset(R.dimen.fingerprint_dialog_button_container_height);
            layoutParams.height = height > 0 ? height : i;
            this.mBiometricIcon.setVisibility(8);
        } else {
            layoutParams.height = 0;
            this.mBiometricIcon.setVisibility(0);
        }
        space.setLayoutParams(layoutParams);
        this.mBiometricIcon.setContentDescription(getResources().getString(getIconDescriptionResourceId()));
        setDismissesDialog(findViewById);
        setDismissesDialog(findViewById2);
        setDismissesDialog(findViewById3);
        this.mNegativeButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                BiometricDialogView.this.lambda$new$0$BiometricDialogView(view);
            }
        });
        this.mPositiveButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                BiometricDialogView.this.lambda$new$2$BiometricDialogView(view);
            }
        });
        this.mTryAgainButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                BiometricDialogView.this.lambda$new$3$BiometricDialogView(view);
            }
        });
        this.mLayout.setFocusableInTouchMode(true);
        this.mLayout.requestFocus();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$BiometricDialogView(View view) {
        this.mCallback.onNegativePressed();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$BiometricDialogView(View view) {
        updateState(4);
        this.mHandler.postDelayed(new Runnable() {
            public final void run() {
                BiometricDialogView.this.lambda$new$1$BiometricDialogView();
            }
        }, (long) getDelayAfterAuthenticatedDurationMs());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$BiometricDialogView() {
        this.mCallback.onPositivePressed();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$BiometricDialogView(View view) {
        showTryAgainButton(false);
        handleClearMessage(false);
        this.mCallback.onTryAgainPressed();
    }

    public void onSaveState(Bundle bundle) {
        bundle.putInt("key_try_again_visibility", this.mTryAgainButton.getVisibility());
        bundle.putInt("key_confirm_visibility", this.mPositiveButton.getVisibility());
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mErrorText.setText(getHintStringResourceId());
        ImageView imageView = (ImageView) this.mLayout.findViewById(R.id.background);
        if (this.mUserManager.isManagedProfile(this.mUserId)) {
            Drawable drawable = getResources().getDrawable(R.drawable.work_challenge_background, this.mContext.getTheme());
            drawable.setColorFilter(this.mDevicePolicyManager.getOrganizationColorForUser(this.mUserId), PorterDuff.Mode.DARKEN);
            imageView.setImageDrawable(drawable);
        } else {
            imageView.setImageDrawable((Drawable) null);
            imageView.setBackgroundColor(R.color.biometric_dialog_dim_color);
        }
        this.mNegativeButton.setVisibility(0);
        this.mErrorText.setVisibility(0);
        if (RotationUtils.getRotation(this.mContext) != 0) {
            this.mDialog.getLayoutParams().width = (int) this.mDialogWidth;
        }
        this.mLastState = 0;
        updateState(1);
        CharSequence charSequence = this.mBundle.getCharSequence("title");
        this.mTitleText.setVisibility(0);
        this.mTitleText.setText(charSequence);
        this.mTitleText.setSelected(true);
        CharSequence charSequence2 = this.mBundle.getCharSequence("subtitle");
        if (TextUtils.isEmpty(charSequence2)) {
            this.mSubtitleText.setVisibility(8);
        } else {
            this.mSubtitleText.setVisibility(0);
            this.mSubtitleText.setText(charSequence2);
        }
        CharSequence charSequence3 = this.mBundle.getCharSequence("description");
        if (TextUtils.isEmpty(charSequence3)) {
            this.mDescriptionText.setVisibility(8);
        } else {
            this.mDescriptionText.setVisibility(0);
            this.mDescriptionText.setText(charSequence3);
        }
        this.mNegativeButton.setText(this.mBundle.getCharSequence("negative_text"));
        if (this.mWasForceRemoved || this.mSkipIntro) {
            this.mLayout.animate().cancel();
            this.mDialog.animate().cancel();
            this.mDialog.setAlpha(1.0f);
            this.mDialog.setTranslationY(0.0f);
            this.mLayout.setAlpha(1.0f);
        } else {
            this.mDialog.setTranslationY(this.mAnimationTranslationOffset);
            this.mLayout.setAlpha(0.0f);
            postOnAnimation(this.mShowAnimationRunnable);
        }
        this.mWasForceRemoved = false;
        this.mSkipIntro = false;
    }

    /* access modifiers changed from: protected */
    public void updateIcon(int i, int i2) {
        Drawable animationForTransition = getAnimationForTransition(i, i2);
        if (animationForTransition == null) {
            Log.e("BiometricDialogView", "Animation not found");
            return;
        }
        AnimatedVectorDrawable animatedVectorDrawable = animationForTransition instanceof AnimatedVectorDrawable ? (AnimatedVectorDrawable) animationForTransition : null;
        this.mBiometricIcon.setImageDrawable(animationForTransition);
        if (animatedVectorDrawable != null && shouldAnimateForTransition(i, i2)) {
            animatedVectorDrawable.forceAnimationOnUI();
            animatedVectorDrawable.start();
        }
    }

    private void setDismissesDialog(View view) {
        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return BiometricDialogView.this.lambda$setDismissesDialog$4$BiometricDialogView(view, motionEvent);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setDismissesDialog$4 */
    public /* synthetic */ boolean lambda$setDismissesDialog$4$BiometricDialogView(View view, MotionEvent motionEvent) {
        if (this.mLastState == 4 || !shouldGrayAreaDismissDialog()) {
            return true;
        }
        this.mCallback.onUserCanceled();
        return true;
    }

    public void startDismiss() {
        final AnonymousClass4 r0 = new Runnable() {
            public void run() {
                BiometricDialogView.this.mWindowManager.removeView(BiometricDialogView.this);
                boolean unused = BiometricDialogView.this.mAnimatingAway = false;
                BiometricDialogView.this.handleClearMessage(false);
                BiometricDialogView.this.showTryAgainButton(false);
                BiometricDialogView.this.updateState(0);
            }
        };
        postOnAnimation(new Runnable() {
            public void run() {
                BiometricDialogView.this.mLayout.animate().alpha(0.0f).setDuration(350).setInterpolator(BiometricDialogView.this.mLinearOutSlowIn).withLayer().start();
                BiometricDialogView.this.mDialog.animate().translationY(BiometricDialogView.this.mAnimationTranslationOffset).setDuration(350).setInterpolator(BiometricDialogView.this.mLinearOutSlowIn).withLayer().withEndAction(r0).start();
            }
        });
    }

    public void forceRemove() {
        this.mLayout.animate().cancel();
        this.mDialog.animate().cancel();
        this.mWindowManager.removeView(this);
        this.mWasForceRemoved = true;
    }

    public void setSkipIntro(boolean z) {
        this.mSkipIntro = z;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public void setRequireConfirmation(boolean z) {
        this.mRequireConfirmation = z;
    }

    public boolean requiresConfirmation() {
        return this.mRequireConfirmation;
    }

    public void showConfirmationButton(boolean z) {
        if (z) {
            updateState(3);
            this.mPositiveButton.setVisibility(0);
            return;
        }
        this.mPositiveButton.setVisibility(8);
    }

    public void setUserId(int i) {
        this.mUserId = i;
    }

    private void showTemporaryMessage(String str, boolean z) {
        this.mHandler.removeMessages(1);
        updateState(2);
        this.mErrorText.setText(str);
        this.mErrorText.setTextColor(this.mErrorColor);
        this.mErrorText.setContentDescription(str);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1, Boolean.valueOf(z)), 2000);
    }

    public void clearTemporaryMessage() {
        this.mHandler.removeMessages(1);
        this.mHandler.obtainMessage(1, Boolean.FALSE).sendToTarget();
    }

    public void showHelpMessage(String str, boolean z) {
        showTemporaryMessage(str, z);
    }

    public void showErrorMessage(String str) {
        showTemporaryMessage(str, false);
        showTryAgainButton(false);
        this.mCallback.onErrorShown();
    }

    public void updateState(int i) {
        if (i == 3) {
            this.mErrorText.setVisibility(4);
        } else if (i == 4) {
            this.mPositiveButton.setVisibility(8);
            this.mNegativeButton.setVisibility(8);
            this.mErrorText.setVisibility(4);
        }
        updateIcon(this.mLastState, i);
        this.mLastState = i;
    }

    public void restoreState(Bundle bundle) {
        this.mTryAgainButton.setVisibility(bundle.getInt("key_try_again_visibility"));
        this.mPositiveButton.setVisibility(bundle.getInt("key_confirm_visibility"));
    }

    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2014, 16777216, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("BiometricDialogView");
        layoutParams.token = this.mWindowToken;
        return layoutParams;
    }
}
