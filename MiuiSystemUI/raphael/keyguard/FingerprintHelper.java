package com.android.keyguard;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import java.util.ArrayList;
import java.util.List;

public class FingerprintHelper {
    private static List<Object> sFingerprintIdentifyCallbackList = new ArrayList();
    private Context mContext;
    private FingerprintManager mFingerprintMgr = null;

    public FingerprintHelper(Context context) {
        this.mContext = context;
    }

    public boolean isHardwareDetected() {
        initFingerprintManager();
        FingerprintManager fingerprintManager = this.mFingerprintMgr;
        return fingerprintManager != null && fingerprintManager.isHardwareDetected();
    }

    private void initFingerprintManager() {
        if (this.mFingerprintMgr == null && this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            this.mFingerprintMgr = (FingerprintManager) this.mContext.getSystemService("fingerprint");
        }
    }

    public List<String> getFingerprintIds() {
        initFingerprintManager();
        FingerprintManager fingerprintManager = this.mFingerprintMgr;
        if (fingerprintManager == null) {
            return new ArrayList();
        }
        List<Fingerprint> enrolledFingerprints = fingerprintManager.getEnrolledFingerprints();
        ArrayList arrayList = new ArrayList();
        if (enrolledFingerprints != null && enrolledFingerprints.size() > 0) {
            for (Fingerprint fingerIdForFingerprint : enrolledFingerprints) {
                arrayList.add(Integer.toString(FingerprintCompat.getFingerIdForFingerprint(fingerIdForFingerprint)));
            }
        }
        return arrayList;
    }
}
