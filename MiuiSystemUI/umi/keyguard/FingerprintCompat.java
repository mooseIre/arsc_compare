package com.android.keyguard;

import android.hardware.fingerprint.Fingerprint;

public class FingerprintCompat {
    public static int getFingerIdForFingerprint(Fingerprint fingerprint) {
        return fingerprint.getBiometricId();
    }
}
