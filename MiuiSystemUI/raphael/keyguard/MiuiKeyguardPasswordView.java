package com.android.keyguard;

import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.BackButton;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import miui.os.Build;

public abstract class MiuiKeyguardPasswordView extends LinearLayout implements EmergencyButton.EmergencyButtonCallback, BackButton.BackButtonCallback {
    protected BackButton mBackButton;
    protected KeyguardSecurityCallback mCallback;
    private Configuration mConfiguration;
    protected TextView mDeleteButton;
    protected EmergencyButton mEmergencyButton;
    protected EmergencyCarrierArea mEmergencyCarrierArea;
    protected MiuiKeyguardFaceUnlockView mFaceUnlockView;
    protected KeyguardBouncerMessageView mKeyguardBouncerMessageView;
    protected KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    protected LockPatternUtils mLockPatternUtils;
    protected UserManager mUm;
    protected Vibrator mVibrator;

    /* access modifiers changed from: protected */
    public abstract void handleConfigurationFontScaleChanged();

    /* access modifiers changed from: protected */
    public abstract void handleConfigurationOrientationChanged();

    /* access modifiers changed from: protected */
    public abstract void handleConfigurationSmallWidthChanged();

    /* access modifiers changed from: protected */
    public void handleWrongPassword() {
    }

    public MiuiKeyguardPasswordView(Context context) {
        super(context);
        this.mConfiguration = new Configuration();
    }

    public MiuiKeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mConfiguration = new Configuration();
        this.mVibrator = (Vibrator) ((LinearLayout) this).mContext.getSystemService("vibrator");
        this.mUm = (UserManager) ((LinearLayout) this).mContext.getSystemService("user");
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mConfiguration.updateFrom(context.getResources().getConfiguration());
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        KeyguardSecurityModel.SecurityMode securityMode;
        super.onFinishInflate();
        this.mEmergencyCarrierArea = (EmergencyCarrierArea) findViewById(C0015R$id.keyguard_selector_fade_container);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(C0015R$id.emergency_call_button);
        this.mEmergencyButton = emergencyButton;
        emergencyButton.setCallback(this);
        BackButton backButton = (BackButton) findViewById(C0015R$id.back_button);
        this.mBackButton = backButton;
        backButton.setCallback(this);
        this.mDeleteButton = (TextView) findViewById(C0015R$id.delete_button);
        if (Build.IS_INTERNATIONAL_BUILD && ((securityMode = ((KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class)).getSecurityMode(KeyguardUpdateMonitor.getCurrentUser())) == KeyguardSecurityModel.SecurityMode.Pattern || securityMode == KeyguardSecurityModel.SecurityMode.PIN || securityMode == KeyguardSecurityModel.SecurityMode.Password)) {
            findViewById(C0015R$id.empty_space_for_global).setVisibility(0);
            findViewById(C0015R$id.empty_space).setVisibility(8);
            this.mEmergencyButton.setBackgroundResource(C0013R$drawable.emergency_btn_global);
        }
        this.mKeyguardBouncerMessageView = (KeyguardBouncerMessageView) findViewById(C0015R$id.keyguard_security_bouncer_message);
        MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = (MiuiKeyguardFaceUnlockView) findViewById(C0015R$id.miui_keyguard_face_unlock_view);
        this.mFaceUnlockView = miuiKeyguardFaceUnlockView;
        miuiKeyguardFaceUnlockView.setKeyguardFaceUnlockView(false);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int updateFrom = this.mConfiguration.updateFrom(configuration);
        boolean z = true;
        boolean z2 = (updateFrom & 2048) != 0;
        boolean z3 = (1073741824 & updateFrom) != 0;
        if ((updateFrom & 128) == 0) {
            z = false;
        }
        if (z2) {
            handleConfigurationSmallWidthChanged();
        }
        if (z3) {
            handleConfigurationFontScaleChanged();
        }
        if (z) {
            handleConfigurationOrientationChanged();
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
        if (i != 0 && !this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot()) {
            setSwitchUserWrongMessage(C0021R$string.input_password_after_boot_msg_must_enter_owner_space);
            handleWrongPassword();
            return false;
        } else if (i != KeyguardUpdateMonitor.getCurrentUser() && MiuiKeyguardUtils.isSuperPowerActive(((LinearLayout) this).mContext)) {
            setSwitchUserWrongMessage(C0021R$string.input_password_after_boot_msg_can_not_switch_when_superpower_active);
            handleWrongPassword();
            return false;
        } else if (i != KeyguardUpdateMonitor.getCurrentUser() && MiuiKeyguardUtils.isGreenKidActive(((LinearLayout) this).mContext)) {
            setSwitchUserWrongMessage(C0021R$string.input_password_after_boot_msg_can_not_switch_when_greenkid_active);
            handleWrongPassword();
            return false;
        } else if (i != KeyguardUpdateMonitor.getCurrentUser() && PhoneUtils.isInCall(((LinearLayout) this).mContext)) {
            Log.d("miui_keyguard_password", "Can't switch user to " + i + " when calling");
            setSwitchUserWrongMessage(C0021R$string.input_password_after_boot_msg_can_not_switch_when_calling);
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
        int[] profileIdsWithDisabled = userManager.getProfileIdsWithDisabled(i);
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

    @Override // com.android.keyguard.BackButton.BackButtonCallback
    public void onBackButtonClicked() {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.reset();
        }
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
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
        return ((DevicePolicyManager) ((LinearLayout) this).mContext.getSystemService("device_policy")).getRequiredStrongAuthTimeout(null, KeyguardUpdateMonitor.getCurrentUser());
    }
}
