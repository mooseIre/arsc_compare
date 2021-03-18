package com.android.systemui.keyguard;

import android.os.RemoteException;
import android.util.Log;
import com.android.internal.policy.IKeyguardDismissCallback;

public class DismissCallbackWrapper {
    private IKeyguardDismissCallback mCallback;

    public DismissCallbackWrapper(IKeyguardDismissCallback iKeyguardDismissCallback) {
        this.mCallback = iKeyguardDismissCallback;
    }

    public void notifyDismissError() {
        try {
            this.mCallback.onDismissError();
        } catch (RemoteException e) {
            Log.i("DismissCallbackWrapper", "Failed to call callback", e);
        }
    }

    public void notifyDismissCancelled() {
        try {
            this.mCallback.onDismissCancelled();
        } catch (RemoteException e) {
            Log.i("DismissCallbackWrapper", "Failed to call callback", e);
        }
    }

    public void notifyDismissSucceeded() {
        try {
            this.mCallback.onDismissSucceeded();
        } catch (RemoteException e) {
            Log.i("DismissCallbackWrapper", "Failed to call callback", e);
        }
    }
}
