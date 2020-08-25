package com.android.systemui.biometrics;

import android.content.res.Configuration;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.systemui.SystemUI;
import com.android.systemui.biometrics.AuthContainerView;
import com.android.systemui.statusbar.CommandQueue;

public class BiometricDialogImpl extends SystemUI implements CommandQueue.Callbacks, AuthDialogCallback {
    @VisibleForTesting
    AuthDialog mCurrentDialog;
    private SomeArgs mCurrentDialogArgs;
    @VisibleForTesting
    IBiometricServiceReceiverInternal mReceiver;
    private WindowManager mWindowManager;

    public void onTryAgainPressed() {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "onTryAgainPressed: Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onTryAgainPressed();
        } catch (RemoteException e) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "RemoteException when handling try again", e);
        }
    }

    public void onDeviceCredentialPressed() {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "onDeviceCredentialPressed: Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onDeviceCredentialPressed();
        } catch (RemoteException e) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "RemoteException when handling credential button", e);
        }
    }

    public void onSystemEvent(int i) {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "onSystemEvent(" + i + "): Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onSystemEvent(i);
        } catch (RemoteException e) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "RemoteException when sending system event", e);
        }
    }

    public void onDismissed(int i, byte[] bArr) {
        Log.e("BiometricPrompt/BiometricDialogImpl", "onDismissed, reason: " + i);
        switch (i) {
            case 1:
                sendResultAndCleanUp(3, bArr);
                return;
            case 2:
                sendResultAndCleanUp(2, bArr);
                return;
            case 3:
                sendResultAndCleanUp(1, bArr);
                return;
            case 4:
                sendResultAndCleanUp(4, bArr);
                return;
            case 5:
                sendResultAndCleanUp(5, bArr);
                return;
            case 6:
                sendResultAndCleanUp(6, bArr);
                return;
            case 7:
                sendResultAndCleanUp(7, bArr);
                return;
            default:
                Log.e("BiometricPrompt/BiometricDialogImpl", "Unhandled reason: " + i);
                return;
        }
    }

    private void sendResultAndCleanUp(int i, byte[] bArr) {
        IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal = this.mReceiver;
        if (iBiometricServiceReceiverInternal == null) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "sendResultAndCleanUp: Receiver is null");
            return;
        }
        try {
            iBiometricServiceReceiverInternal.onDialogDismissed(i, bArr);
        } catch (RemoteException e) {
            Log.w("BiometricPrompt/BiometricDialogImpl", "Remote exception", e);
        }
        onDialogDismissed(i);
    }

    public void start() {
        ((CommandQueue) getComponent(CommandQueue.class)).addCallbacks(this);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    public void showBiometricDialog(SomeArgs someArgs) {
        boolean z;
        int authenticators = Utils.getAuthenticators((Bundle) someArgs.arg1);
        Log.d("BiometricPrompt/BiometricDialogImpl", "showAuthenticationDialog, authenticators: " + authenticators + ", biometricModality: " + someArgs.argi1 + ", requireConfirmation: " + ((Boolean) someArgs.arg3).booleanValue() + ", operationId: " + ((Long) someArgs.arg5).longValue() + ", sysUiSessionId: " + someArgs.argi3);
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = (Bundle) someArgs.arg1;
        obtain.arg2 = (IBiometricServiceReceiverInternal) someArgs.arg2;
        obtain.argi1 = someArgs.argi1;
        obtain.arg3 = Boolean.valueOf(((Boolean) someArgs.arg3).booleanValue());
        obtain.argi2 = someArgs.argi2;
        obtain.arg4 = (String) someArgs.arg4;
        obtain.arg5 = Long.valueOf(((Long) someArgs.arg5).longValue());
        obtain.argi3 = someArgs.argi3;
        if (this.mCurrentDialog != null) {
            Log.w("BiometricPrompt/BiometricDialogImpl", "mCurrentDialog: " + this.mCurrentDialog);
            z = true;
        } else {
            z = false;
        }
        showDialog(obtain, z, (Bundle) null);
    }

    public void onBiometricAuthenticated(boolean z) {
        Log.d("BiometricPrompt/BiometricDialogImpl", "onBiometricAuthenticated: " + z);
        this.mCurrentDialog.onAuthenticationSucceeded();
    }

    public void onBiometricHelp(String str) {
        Log.d("BiometricPrompt/BiometricDialogImpl", "onBiometricHelp: " + str);
        this.mCurrentDialog.onHelp(str);
    }

    private String getErrorString(int i, int i2, int i3) {
        if (i != 2) {
            return i != 8 ? "" : FaceManager.getErrorString(this.mContext, i2, i3);
        }
        return FingerprintManager.getErrorString(this.mContext, i2, i3);
    }

    public void onBiometricError(SomeArgs someArgs) {
        String str;
        int i = someArgs.argi1;
        int i2 = someArgs.argi2;
        int i3 = someArgs.argi3;
        boolean z = false;
        Log.d("BiometricPrompt/BiometricDialogImpl", String.format("onBiometricError(%d, %d, %d)", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)}));
        boolean z2 = i2 == 7 || i2 == 9;
        if (i2 == 100 || i2 == 3) {
            z = true;
        }
        if (this.mCurrentDialog.isAllowDeviceCredentials() && z2) {
            Log.d("BiometricPrompt/BiometricDialogImpl", "onBiometricError, lockout");
            this.mCurrentDialog.animateToCredentialUI();
        } else if (z) {
            if (i2 == 100) {
                str = this.mContext.getString(17039780);
            } else {
                str = getErrorString(i, i2, i3);
            }
            Log.d("BiometricPrompt/BiometricDialogImpl", "onBiometricError, soft error: " + str);
            this.mCurrentDialog.onAuthenticationFailed(str);
        } else {
            String errorString = getErrorString(i, i2, i3);
            Log.d("BiometricPrompt/BiometricDialogImpl", "onBiometricError, hard error: " + errorString);
            this.mCurrentDialog.onError(errorString);
        }
    }

    public void hideBiometricDialog() {
        Log.d("BiometricPrompt/BiometricDialogImpl", "hideBiometricDialog: " + this.mCurrentDialog);
        AuthDialog authDialog = this.mCurrentDialog;
        if (authDialog != null) {
            authDialog.dismissFromSystemServer();
            this.mCurrentDialog = null;
        }
    }

    private void showDialog(SomeArgs someArgs, boolean z, Bundle bundle) {
        SomeArgs someArgs2 = someArgs;
        Bundle bundle2 = bundle;
        this.mCurrentDialogArgs = someArgs2;
        int i = someArgs2.argi1;
        boolean booleanValue = ((Boolean) someArgs2.arg3).booleanValue();
        int i2 = someArgs2.argi2;
        long longValue = ((Long) someArgs2.arg5).longValue();
        int i3 = someArgs2.argi3;
        AuthDialog buildDialog = buildDialog((Bundle) someArgs2.arg1, booleanValue, i2, i, (String) someArgs2.arg4, z, longValue, i3);
        if (buildDialog == null) {
            Log.e("BiometricPrompt/BiometricDialogImpl", "Unsupported type: " + i);
            return;
        }
        Log.d("BiometricPrompt/BiometricDialogImpl", "userId: " + i2 + " savedState: " + bundle2 + " mCurrentDialog: " + this.mCurrentDialog + " newDialog: " + buildDialog + " type: " + i + " sysUiSessionId: " + i3);
        AuthDialog authDialog = this.mCurrentDialog;
        if (authDialog != null) {
            authDialog.dismissWithoutCallback(false);
        }
        this.mReceiver = (IBiometricServiceReceiverInternal) someArgs2.arg2;
        this.mCurrentDialog = buildDialog;
        buildDialog.show(this.mWindowManager, bundle2);
    }

    private void onDialogDismissed(int i) {
        Log.d("BiometricPrompt/BiometricDialogImpl", "onDialogDismissed: " + i);
        if (this.mCurrentDialog == null) {
            Log.w("BiometricPrompt/BiometricDialogImpl", "Dialog already dismissed");
        }
        this.mReceiver = null;
        this.mCurrentDialog = null;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mCurrentDialog != null) {
            Bundle bundle = new Bundle();
            this.mCurrentDialog.onSaveState(bundle);
            this.mCurrentDialog.dismissWithoutCallback(false);
            this.mCurrentDialog = null;
            if (bundle.getInt("container_state") != 4) {
                if (bundle.getBoolean("credential_showing")) {
                    ((Bundle) this.mCurrentDialogArgs.arg1).putInt("authenticators_allowed", 32768);
                }
                showDialog(this.mCurrentDialogArgs, true, bundle);
            }
        }
    }

    /* access modifiers changed from: protected */
    public AuthDialog buildDialog(Bundle bundle, boolean z, int i, int i2, String str, boolean z2, long j, int i3) {
        AuthContainerView.Builder builder = new AuthContainerView.Builder(this.mContext);
        builder.setCallback(this);
        builder.setBiometricPromptBundle(bundle);
        builder.setRequireConfirmation(z);
        builder.setUserId(i);
        builder.setOpPackageName(str);
        builder.setSkipIntro(z2);
        builder.setOperationId(j);
        builder.setSysUiSessionId(i3);
        return builder.build(i2);
    }
}
