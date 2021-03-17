package com.android.keyguard;

public interface IPhoneSignalController {

    public interface PhoneSignalChangeCallback {
        void onSignalChange(boolean z);
    }

    boolean isSignalAvailable();

    void registerPhoneSignalChangeCallback(PhoneSignalChangeCallback phoneSignalChangeCallback);

    void removePhoneSignalChangeCallback(PhoneSignalChangeCallback phoneSignalChangeCallback);
}
