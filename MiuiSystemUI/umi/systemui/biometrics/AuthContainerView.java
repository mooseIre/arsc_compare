package com.android.systemui.biometrics;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.biometrics.AuthBiometricView;
import com.android.systemui.biometrics.AuthContainerView;
import com.android.systemui.biometrics.AuthCredentialView;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.R;

public class AuthContainerView extends LinearLayout implements AuthDialog, WakefulnessLifecycle.Observer {
    @VisibleForTesting
    final ImageView mBackgroundView;
    @VisibleForTesting
    final BiometricCallback mBiometricCallback;
    @VisibleForTesting
    final ScrollView mBiometricScrollView;
    @VisibleForTesting
    AuthBiometricView mBiometricView;
    final Config mConfig;
    /* access modifiers changed from: private */
    public int mContainerState = 0;
    byte[] mCredentialAttestation;
    private final CredentialCallback mCredentialCallback;
    @VisibleForTesting
    AuthCredentialView mCredentialView;
    final int mEffectiveUserId;
    @VisibleForTesting
    final FrameLayout mFrameLayout;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final Injector mInjector;
    private final Interpolator mLinearOutSlowIn;
    private final AuthPanelController mPanelController;
    private final View mPanelView;
    Integer mPendingCallbackReason;
    private final float mTranslationY;
    @VisibleForTesting
    final WakefulnessLifecycle mWakefulnessLifecycle;
    private final WindowManager mWindowManager;
    private final IBinder mWindowToken = new Binder();

    public void onFinishedGoingToSleep() {
    }

    public void onFinishedWakingUp() {
    }

    public void onStartedWakingUp() {
    }

    static class Config {
        Bundle mBiometricPromptBundle;
        AuthDialogCallback mCallback;
        Context mContext;
        int mModalityMask;
        String mOpPackageName;
        long mOperationId;
        boolean mRequireConfirmation;
        boolean mSkipIntro;
        int mSysUiSessionId;
        int mUserId;

        Config() {
        }
    }

    public static class Builder {
        Config mConfig;

        public Builder(Context context) {
            Config config = new Config();
            this.mConfig = config;
            config.mContext = context;
        }

        public Builder setCallback(AuthDialogCallback authDialogCallback) {
            this.mConfig.mCallback = authDialogCallback;
            return this;
        }

        public Builder setBiometricPromptBundle(Bundle bundle) {
            this.mConfig.mBiometricPromptBundle = bundle;
            return this;
        }

        public Builder setRequireConfirmation(boolean z) {
            this.mConfig.mRequireConfirmation = z;
            return this;
        }

        public Builder setUserId(int i) {
            this.mConfig.mUserId = i;
            return this;
        }

        public Builder setOpPackageName(String str) {
            this.mConfig.mOpPackageName = str;
            return this;
        }

        public Builder setSkipIntro(boolean z) {
            this.mConfig.mSkipIntro = z;
            return this;
        }

        public Builder setOperationId(long j) {
            this.mConfig.mOperationId = j;
            return this;
        }

        public Builder setSysUiSessionId(int i) {
            this.mConfig.mSysUiSessionId = i;
            return this;
        }

        public AuthContainerView build(int i) {
            this.mConfig.mModalityMask = i;
            return new AuthContainerView(this.mConfig, new Injector());
        }
    }

    public static class Injector {
        /* access modifiers changed from: package-private */
        public int getAnimateCredentialStartDelayMs() {
            return 300;
        }

        /* access modifiers changed from: package-private */
        public ScrollView getBiometricScrollView(FrameLayout frameLayout) {
            return (ScrollView) frameLayout.findViewById(R.id.biometric_scrollview);
        }

        /* access modifiers changed from: package-private */
        public FrameLayout inflateContainerView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
            return (FrameLayout) layoutInflater.inflate(R.layout.auth_container_view, viewGroup, false);
        }

        /* access modifiers changed from: package-private */
        public AuthPanelController getPanelController(Context context, View view) {
            return new AuthPanelController(context, view);
        }

        /* access modifiers changed from: package-private */
        public ImageView getBackgroundView(FrameLayout frameLayout) {
            return (ImageView) frameLayout.findViewById(R.id.background);
        }

        /* access modifiers changed from: package-private */
        public View getPanelView(FrameLayout frameLayout) {
            return frameLayout.findViewById(R.id.panel);
        }

        /* access modifiers changed from: package-private */
        public UserManager getUserManager(Context context) {
            return UserManager.get(context);
        }

        /* access modifiers changed from: package-private */
        public int getCredentialType(Context context, int i) {
            return Utils.getCredentialType(context, i);
        }
    }

    @VisibleForTesting
    final class BiometricCallback implements AuthBiometricView.Callback {
        BiometricCallback() {
        }

        public void onAction(int i) {
            Log.d("BiometricPrompt/AuthContainerView", "onAction: " + i + ", sysUiSessionId: " + AuthContainerView.this.mConfig.mSysUiSessionId + ", state: " + AuthContainerView.this.mContainerState);
            switch (i) {
                case 1:
                    AuthContainerView.this.animateAway(4);
                    return;
                case 2:
                    AuthContainerView.this.sendEarlyUserCanceled();
                    AuthContainerView.this.animateAway(1);
                    return;
                case 3:
                    AuthContainerView.this.animateAway(2);
                    return;
                case 4:
                    AuthContainerView.this.mConfig.mCallback.onTryAgainPressed();
                    return;
                case 5:
                    AuthContainerView.this.animateAway(5);
                    return;
                case 6:
                    AuthContainerView.this.mConfig.mCallback.onDeviceCredentialPressed();
                    AuthContainerView.this.mHandler.postDelayed(new Runnable() {
                        public final void run() {
                            AuthContainerView.BiometricCallback.this.lambda$onAction$0$AuthContainerView$BiometricCallback();
                        }
                    }, (long) AuthContainerView.this.mInjector.getAnimateCredentialStartDelayMs());
                    return;
                default:
                    Log.e("BiometricPrompt/AuthContainerView", "Unhandled action: " + i);
                    return;
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAction$0 */
        public /* synthetic */ void lambda$onAction$0$AuthContainerView$BiometricCallback() {
            AuthContainerView.this.addCredentialView(false, true);
        }
    }

    final class CredentialCallback implements AuthCredentialView.Callback {
        CredentialCallback() {
        }

        public void onCredentialMatched(byte[] bArr) {
            AuthContainerView authContainerView = AuthContainerView.this;
            authContainerView.mCredentialAttestation = bArr;
            authContainerView.animateAway(7);
        }
    }

    @VisibleForTesting
    AuthContainerView(Config config, Injector injector) {
        super(config.mContext);
        this.mConfig = config;
        this.mInjector = injector;
        this.mEffectiveUserId = injector.getUserManager(this.mContext).getCredentialOwnerProfile(this.mConfig.mUserId);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(WindowManager.class);
        this.mWakefulnessLifecycle = (WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class);
        this.mTranslationY = getResources().getDimension(R.dimen.biometric_dialog_animation_translation_offset);
        this.mLinearOutSlowIn = Interpolators.LINEAR_OUT_SLOW_IN;
        this.mBiometricCallback = new BiometricCallback();
        this.mCredentialCallback = new CredentialCallback();
        LayoutInflater from = LayoutInflater.from(this.mContext);
        FrameLayout inflateContainerView = this.mInjector.inflateContainerView(from, this);
        this.mFrameLayout = inflateContainerView;
        View panelView = this.mInjector.getPanelView(inflateContainerView);
        this.mPanelView = panelView;
        this.mPanelController = this.mInjector.getPanelController(this.mContext, panelView);
        if (Utils.isBiometricAllowed(this.mConfig.mBiometricPromptBundle)) {
            int i = config.mModalityMask;
            if (i == 2) {
                this.mBiometricView = (AuthBiometricFingerprintView) from.inflate(R.layout.auth_biometric_fingerprint_view, (ViewGroup) null, false);
            } else if (i == 8) {
                this.mBiometricView = (AuthBiometricFaceView) from.inflate(R.layout.auth_biometric_face_view, (ViewGroup) null, false);
            } else {
                Log.e("BiometricPrompt/AuthContainerView", "Unsupported biometric modality: " + config.mModalityMask);
                this.mBiometricView = null;
                this.mBackgroundView = null;
                this.mBiometricScrollView = null;
                return;
            }
        }
        this.mBiometricScrollView = this.mInjector.getBiometricScrollView(this.mFrameLayout);
        this.mBackgroundView = this.mInjector.getBackgroundView(this.mFrameLayout);
        addView(this.mFrameLayout);
        setOnKeyListener(new View.OnKeyListener() {
            public final boolean onKey(View view, int i, KeyEvent keyEvent) {
                return AuthContainerView.this.lambda$new$0$AuthContainerView(view, i, keyEvent);
            }
        });
        setFocusableInTouchMode(true);
        requestFocus();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ boolean lambda$new$0$AuthContainerView(View view, int i, KeyEvent keyEvent) {
        if (i != 4) {
            return false;
        }
        if (keyEvent.getAction() == 1) {
            sendEarlyUserCanceled();
            animateAway(1);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void sendEarlyUserCanceled() {
        this.mConfig.mCallback.onSystemEvent(1);
    }

    public boolean isAllowDeviceCredentials() {
        return Utils.isDeviceCredentialAllowed(this.mConfig.mBiometricPromptBundle);
    }

    private void addBiometricView() {
        this.mBiometricView.setRequireConfirmation(this.mConfig.mRequireConfirmation);
        this.mBiometricView.setPanelController(this.mPanelController);
        this.mBiometricView.setBiometricPromptBundle(this.mConfig.mBiometricPromptBundle);
        this.mBiometricView.setCallback(this.mBiometricCallback);
        this.mBiometricView.setBackgroundView(this.mBackgroundView);
        this.mBiometricView.setUserId(this.mConfig.mUserId);
        this.mBiometricView.setEffectiveUserId(this.mEffectiveUserId);
        this.mBiometricScrollView.addView(this.mBiometricView);
    }

    /* access modifiers changed from: private */
    public void addCredentialView(boolean z, boolean z2) {
        LayoutInflater from = LayoutInflater.from(this.mContext);
        int credentialType = this.mInjector.getCredentialType(this.mContext, this.mEffectiveUserId);
        if (credentialType != 1) {
            if (credentialType == 2) {
                this.mCredentialView = (AuthCredentialView) from.inflate(R.layout.auth_credential_pattern_view, (ViewGroup) null, false);
                this.mBackgroundView.setOnClickListener((View.OnClickListener) null);
                this.mBackgroundView.setImportantForAccessibility(2);
                this.mCredentialView.setContainerView(this);
                this.mCredentialView.setUserId(this.mConfig.mUserId);
                this.mCredentialView.setOperationId(this.mConfig.mOperationId);
                this.mCredentialView.setEffectiveUserId(this.mEffectiveUserId);
                this.mCredentialView.setCredentialType(credentialType);
                this.mCredentialView.setCallback(this.mCredentialCallback);
                this.mCredentialView.setBiometricPromptBundle(this.mConfig.mBiometricPromptBundle);
                this.mCredentialView.setPanelController(this.mPanelController, z);
                this.mCredentialView.setShouldAnimateContents(z2);
                this.mFrameLayout.addView(this.mCredentialView);
            } else if (credentialType != 3) {
                throw new IllegalStateException("Unknown credential type: " + credentialType);
            }
        }
        this.mCredentialView = (AuthCredentialView) from.inflate(R.layout.auth_credential_password_view, (ViewGroup) null, false);
        this.mBackgroundView.setOnClickListener((View.OnClickListener) null);
        this.mBackgroundView.setImportantForAccessibility(2);
        this.mCredentialView.setContainerView(this);
        this.mCredentialView.setUserId(this.mConfig.mUserId);
        this.mCredentialView.setOperationId(this.mConfig.mOperationId);
        this.mCredentialView.setEffectiveUserId(this.mEffectiveUserId);
        this.mCredentialView.setCredentialType(credentialType);
        this.mCredentialView.setCallback(this.mCredentialCallback);
        this.mCredentialView.setBiometricPromptBundle(this.mConfig.mBiometricPromptBundle);
        this.mCredentialView.setPanelController(this.mPanelController, z);
        this.mCredentialView.setShouldAnimateContents(z2);
        this.mFrameLayout.addView(this.mCredentialView);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mPanelController.setContainerDimensions(getMeasuredWidth(), getMeasuredHeight());
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttachedToWindowInternal();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void onAttachedToWindowInternal() {
        this.mWakefulnessLifecycle.addObserver(this);
        if (Utils.isBiometricAllowed(this.mConfig.mBiometricPromptBundle)) {
            addBiometricView();
        } else if (Utils.isDeviceCredentialAllowed(this.mConfig.mBiometricPromptBundle)) {
            addCredentialView(true, false);
        } else {
            throw new IllegalStateException("Unknown configuration: " + Utils.getAuthenticators(this.mConfig.mBiometricPromptBundle));
        }
        if (this.mConfig.mSkipIntro) {
            this.mContainerState = 3;
            return;
        }
        this.mContainerState = 1;
        this.mPanelView.setY(this.mTranslationY);
        this.mBiometricScrollView.setY(this.mTranslationY);
        setAlpha(0.0f);
        postOnAnimation(new Runnable() {
            public final void run() {
                AuthContainerView.this.lambda$onAttachedToWindowInternal$1$AuthContainerView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAttachedToWindowInternal$1 */
    public /* synthetic */ void lambda$onAttachedToWindowInternal$1$AuthContainerView() {
        this.mPanelView.animate().translationY(0.0f).setDuration(250).setInterpolator(this.mLinearOutSlowIn).withLayer().withEndAction(new Runnable() {
            public final void run() {
                AuthContainerView.this.onDialogAnimatedIn();
            }
        }).start();
        this.mBiometricScrollView.animate().translationY(0.0f).setDuration(250).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
        AuthCredentialView authCredentialView = this.mCredentialView;
        if (authCredentialView != null && authCredentialView.isAttachedToWindow()) {
            this.mCredentialView.setY(this.mTranslationY);
            this.mCredentialView.animate().translationY(0.0f).setDuration(250).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
        }
        animate().alpha(1.0f).setDuration(250).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWakefulnessLifecycle.removeObserver(this);
    }

    public void onStartedGoingToSleep() {
        animateAway(1);
    }

    public void show(WindowManager windowManager, Bundle bundle) {
        AuthBiometricView authBiometricView = this.mBiometricView;
        if (authBiometricView != null) {
            authBiometricView.restoreState(bundle);
        }
        windowManager.addView(this, getLayoutParams(this.mWindowToken));
    }

    public void dismissWithoutCallback(boolean z) {
        if (z) {
            animateAway(false, 0);
        } else {
            removeWindowIfAttached(false);
        }
    }

    public void dismissFromSystemServer() {
        removeWindowIfAttached(true);
    }

    public void onAuthenticationSucceeded() {
        Log.d("BiometricPrompt/AuthContainerView", "onAuthenticationSucceeded");
        this.mBiometricView.onAuthenticationSucceeded();
    }

    public void onAuthenticationFailed(String str) {
        Log.d("BiometricPrompt/AuthContainerView", "onAuthenticationFailed: " + str);
        this.mBiometricView.onAuthenticationFailed(str);
    }

    public void onHelp(String str) {
        this.mBiometricView.onHelp(str);
    }

    public void onError(String str) {
        this.mBiometricView.onError(str);
    }

    public void onSaveState(Bundle bundle) {
        bundle.putInt("container_state", this.mContainerState);
        boolean z = true;
        bundle.putBoolean("biometric_showing", this.mBiometricView != null && this.mCredentialView == null);
        if (this.mCredentialView == null) {
            z = false;
        }
        bundle.putBoolean("credential_showing", z);
        AuthBiometricView authBiometricView = this.mBiometricView;
        if (authBiometricView != null) {
            authBiometricView.onSaveState(bundle);
        }
    }

    public void animateToCredentialUI() {
        this.mBiometricView.startTransitionToCredentialUI();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void animateAway(int i) {
        animateAway(true, i);
    }

    private void animateAway(boolean z, int i) {
        int i2 = this.mContainerState;
        if (i2 == 1) {
            Log.w("BiometricPrompt/AuthContainerView", "startDismiss(): waiting for onDialogAnimatedIn");
            this.mContainerState = 2;
        } else if (i2 == 4) {
            Log.w("BiometricPrompt/AuthContainerView", "Already dismissing, sendReason: " + z + " reason: " + i);
        } else {
            this.mContainerState = 4;
            if (z) {
                this.mPendingCallbackReason = Integer.valueOf(i);
            } else {
                this.mPendingCallbackReason = null;
            }
            postOnAnimation(new Runnable(new Runnable() {
                public final void run() {
                    AuthContainerView.this.lambda$animateAway$2$AuthContainerView();
                }
            }) {
                public final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AuthContainerView.this.lambda$animateAway$3$AuthContainerView(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateAway$2 */
    public /* synthetic */ void lambda$animateAway$2$AuthContainerView() {
        setVisibility(4);
        removeWindowIfAttached(true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateAway$3 */
    public /* synthetic */ void lambda$animateAway$3$AuthContainerView(Runnable runnable) {
        this.mPanelView.animate().translationY(this.mTranslationY).setDuration(350).setInterpolator(this.mLinearOutSlowIn).withLayer().withEndAction(runnable).start();
        this.mBiometricScrollView.animate().translationY(this.mTranslationY).setDuration(350).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
        AuthCredentialView authCredentialView = this.mCredentialView;
        if (authCredentialView != null && authCredentialView.isAttachedToWindow()) {
            this.mCredentialView.animate().translationY(this.mTranslationY).setDuration(350).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
        }
        animate().alpha(0.0f).setDuration(350).setInterpolator(this.mLinearOutSlowIn).withLayer().start();
    }

    private void sendPendingCallbackIfNotNull() {
        Log.d("BiometricPrompt/AuthContainerView", "pendingCallback: " + this.mPendingCallbackReason + " sysUISessionId: " + this.mConfig.mSysUiSessionId);
        Integer num = this.mPendingCallbackReason;
        if (num != null) {
            this.mConfig.mCallback.onDismissed(num.intValue(), this.mCredentialAttestation);
            this.mPendingCallbackReason = null;
        }
    }

    private void removeWindowIfAttached(boolean z) {
        if (z) {
            sendPendingCallbackIfNotNull();
        }
        if (this.mContainerState == 5) {
            Log.w("BiometricPrompt/AuthContainerView", "Container already STATE_GONE, mSysUiSessionId: " + this.mConfig.mSysUiSessionId);
            return;
        }
        Log.d("BiometricPrompt/AuthContainerView", "Removing container, mSysUiSessionId: " + this.mConfig.mSysUiSessionId);
        this.mContainerState = 5;
        this.mWindowManager.removeView(this);
    }

    /* access modifiers changed from: private */
    public void onDialogAnimatedIn() {
        if (this.mContainerState == 2) {
            Log.d("BiometricPrompt/AuthContainerView", "onDialogAnimatedIn(): mPendingDismissDialog=true, dismissing now");
            animateAway(false, 0);
            return;
        }
        this.mContainerState = 3;
        AuthBiometricView authBiometricView = this.mBiometricView;
        if (authBiometricView != null) {
            authBiometricView.onDialogAnimatedIn();
        }
    }

    public static WindowManager.LayoutParams getLayoutParams(IBinder iBinder) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2017, 16785408, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("BiometricPrompt");
        layoutParams.token = iBinder;
        layoutParams.setFitInsetsTypes(layoutParams.getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
        return layoutParams;
    }
}
