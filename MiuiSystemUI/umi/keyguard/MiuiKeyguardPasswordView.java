package com.android.keyguard;

import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerCompat;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.BackButton;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.plugins.R;
import miui.util.HapticFeedbackUtil;

public abstract class MiuiKeyguardPasswordView extends LinearLayout implements EmergencyButton.EmergencyButtonCallback, BackButton.BackButtonCallback {
    protected BackButton mBackButton;
    protected KeyguardSecurityCallback mCallback;
    protected TextView mDeleteButton;
    private int mDensityDpi;
    protected EmergencyButton mEmergencyButton;
    protected EmergencyCarrierArea mEmergencyCarrierArea;
    protected MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private float mFontScale;
    protected HapticFeedbackUtil mHapticFeedbackUtil;
    protected KeyguardBouncerMessageView mKeyguardBouncerMessageView;
    protected KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    protected LockPatternUtils mLockPatternUtils;
    private int mOrientation;
    protected UserManager mUm;
    private TextView mUsePassword;
    protected Vibrator mVibrator;

    /* access modifiers changed from: protected */
    public void dismissFodView() {
    }

    /* access modifiers changed from: protected */
    public abstract void handleConfigurationFontScaleChanged();

    /* access modifiers changed from: protected */
    public abstract void handleConfigurationOrientationChanged();

    /* access modifiers changed from: protected */
    public void handleWrongPassword() {
    }

    /* access modifiers changed from: protected */
    public void showFodViewIfNeed() {
    }

    public MiuiKeyguardPasswordView(Context context) {
        super(context);
        this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onFingerprintError(int i, String str) {
                super.onFingerprintError(i, str);
                if (i == 7 || i == 9) {
                    MiuiKeyguardPasswordView.this.usePassword();
                }
            }
        };
    }

    public MiuiKeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onFingerprintError(int i, String str) {
                super.onFingerprintError(i, str);
                if (i == 7 || i == 9) {
                    MiuiKeyguardPasswordView.this.usePassword();
                }
            }
        };
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHapticFeedbackUtil = new HapticFeedbackUtil(context, false);
        this.mUm = (UserManager) this.mContext.getSystemService("user");
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initUsePassword();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHapticFeedbackUtil.release();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEmergencyCarrierArea = (EmergencyCarrierArea) findViewById(R.id.keyguard_selector_fade_container);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(R.id.emergency_call_button);
        this.mEmergencyButton = emergencyButton;
        emergencyButton.setCallback(this);
        BackButton backButton = (BackButton) findViewById(R.id.back_button);
        this.mBackButton = backButton;
        backButton.setCallback(this);
        this.mDeleteButton = (TextView) findViewById(R.id.delete_button);
        this.mKeyguardBouncerMessageView = (KeyguardBouncerMessageView) findViewById(R.id.keyguard_security_bouncer_message);
        MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = (MiuiKeyguardFaceUnlockView) findViewById(R.id.miui_keyguard_face_unlock_view);
        this.mFaceUnlockView = miuiKeyguardFaceUnlockView;
        miuiKeyguardFaceUnlockView.setKeyguardFaceUnlockView(false);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        float f = configuration.fontScale;
        if (this.mFontScale != f) {
            handleConfigurationFontScaleChanged();
            updateFodTextSize();
            this.mFontScale = f;
        }
        int i = configuration.orientation;
        if (this.mOrientation != i) {
            handleConfigurationOrientationChanged();
            this.mOrientation = i;
        }
        int i2 = configuration.densityDpi;
        if (this.mDensityDpi != i2) {
            updateFodPosition();
            this.mDensityDpi = i2;
        }
    }

    /* access modifiers changed from: protected */
    public void switchUser(int i) {
        if (KeyguardUpdateMonitor.getCurrentUser() != i) {
            try {
                ActivityManagerNative.getDefault().switchUser(i);
            } catch (RemoteException e) {
                Log.e("MiuiKeyguardPasswordView", "switchUser failed", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean allowUnlock(int i) {
        if (i != 0 && !KeyguardUpdateMonitor.getInstance(this.mContext).getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot()) {
            setSwitchUserWrongMessage(R.string.input_password_after_boot_msg_must_enter_owner_space);
            handleWrongPassword();
            return false;
        } else if (i != KeyguardUpdateMonitor.getCurrentUser() && MiuiKeyguardUtils.isSuperPowerActive(this.mContext)) {
            setSwitchUserWrongMessage(R.string.input_password_after_boot_msg_can_not_switch_when_superpower_active);
            handleWrongPassword();
            return false;
        } else if (i != KeyguardUpdateMonitor.getCurrentUser() && MiuiKeyguardUtils.isGreenKidActive(this.mContext)) {
            setSwitchUserWrongMessage(R.string.input_password_after_boot_msg_can_not_switch_when_greenkid_active);
            handleWrongPassword();
            return false;
        } else if (i != KeyguardUpdateMonitor.getCurrentUser() && PhoneUtils.isInCall(this.mContext)) {
            Log.d("miui_keyguard_password", "Can't switch user to " + i + " when calling");
            setSwitchUserWrongMessage(R.string.input_password_after_boot_msg_can_not_switch_when_calling);
            handleWrongPassword();
            return false;
        } else if (i == KeyguardUpdateMonitor.getCurrentUser() || i == 0 || i != getManagedProfileId(this.mUm, UserHandle.myUserId())) {
            return true;
        } else {
            Log.d("miui_keyguard_password", "Can't switch user to " + i + " when managed profile id");
            handleWrongPassword();
            return false;
        }
    }

    private int getManagedProfileId(UserManager userManager, int i) {
        int[] profileIdsWithDisabled = UserManagerCompat.getProfileIdsWithDisabled(userManager, i);
        if (profileIdsWithDisabled == null || profileIdsWithDisabled.length <= 0) {
            return -10000;
        }
        for (int i2 : profileIdsWithDisabled) {
            if (i2 != i) {
                return i2;
            }
        }
        return -10000;
    }

    public void onBackButtonClicked() {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.reset();
        }
    }

    public void onEmergencyButtonClickedWhenInCall() {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.reset();
            this.mCallback.userActivity();
        }
    }

    private void setSwitchUserWrongMessage(int i) {
        this.mKeyguardBouncerMessageView.showMessage(0, i);
    }

    /* access modifiers changed from: protected */
    public long getRequiredStrongAuthTimeout() {
        return DevicePolicyManagerCompat.getRequiredStrongAuthTimeout((DevicePolicyManager) this.mContext.getSystemService("device_policy"), (ComponentName) null, KeyguardUpdateMonitor.getCurrentUser());
    }

    private void initUsePassword() {
        View rootView;
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword() && (rootView = getRootView()) != null) {
            TextView textView = (TextView) rootView.findViewById(R.id.gxzw_password_button);
            this.mUsePassword = textView;
            if (textView != null) {
                updateFodPosition();
                updateFodTextSize();
                this.mUsePassword.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        MiuiKeyguardPasswordView.this.usePassword();
                    }
                });
            }
        }
    }

    private void updateFodPosition() {
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword() && this.mUsePassword != null) {
            Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.gxzw_bouncer_use_password_margin) + (Math.max(point.x, point.y) - MiuiGxzwManager.getFodPosition(getContext()).top);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mUsePassword.getLayoutParams();
            layoutParams.bottomMargin = dimensionPixelOffset;
            this.mUsePassword.setLayoutParams(layoutParams);
        }
    }

    private void updateFodTextSize() {
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword() && this.mUsePassword != null) {
            this.mUsePassword.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.gxzw_bouncer_use_password_size));
        }
    }

    /* access modifiers changed from: protected */
    public final void onShowFodView() {
        TextView textView = this.mUsePassword;
        if (textView != null) {
            textView.setVisibility(0);
        }
        KeyguardUpdateMonitor.getInstance(getContext()).registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public final void onDismissFodView() {
        TextView textView = this.mUsePassword;
        if (textView != null) {
            textView.setVisibility(8);
        }
        KeyguardUpdateMonitor.getInstance(getContext()).removeCallback(this.mKeyguardUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public void usePassword() {
        dismissFodView();
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().setDimissFodInBouncer(true);
        }
    }
}
