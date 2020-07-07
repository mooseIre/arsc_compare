package com.android.keyguard;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Slog;
import com.android.keyguard.utils.PhoneUtils;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import miui.telephony.TelephonyManager;

public class PhoneSignalController {
    private int mPhoneCount;
    private boolean[] mPhoneSignalAvailable;
    private int[] mPhoneSignalLevel;
    private ArrayList<PhoneStateListener> mPhoneStateListeners = new ArrayList<>(4);
    private boolean mSignalAvailable;
    private ArrayList<PhoneSignalChangeCallback> mSignalChangeCallbacks = Lists.newArrayList();

    interface PhoneSignalChangeCallback {
        void onSignalChange(boolean z);
    }

    public PhoneSignalController(Context context) {
    }

    private PhoneStateListener getPhoneStateListener(final int i) {
        AnonymousClass1 r0 = new PhoneStateListener() {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                PhoneSignalController.this.handleSignalStrengthsChanged(signalStrength, i);
            }

            public void onServiceStateChanged(ServiceState serviceState) {
                PhoneSignalController.this.handleServiceStateChanged(serviceState, i);
            }
        };
        r0.setSubId(i);
        this.mPhoneStateListeners.add(i, r0);
        return r0;
    }

    /* access modifiers changed from: private */
    public void handleSignalStrengthsChanged(SignalStrength signalStrength, int i) {
        int miuiLevel = getMiuiLevel(signalStrength);
        if (miuiLevel < 1 || miuiLevel > 5) {
            this.mPhoneSignalAvailable[i] = false;
        } else {
            this.mPhoneSignalAvailable[i] = true;
        }
        this.mPhoneSignalLevel[i] = miuiLevel;
        Slog.d("PhoneSignalController", "level=" + miuiLevel + " " + this.mSignalChangeCallbacks.size());
        Iterator<PhoneSignalChangeCallback> it = this.mSignalChangeCallbacks.iterator();
        while (it.hasNext()) {
            notifyPhoneSignalChangeCallback(it.next());
        }
    }

    private int getMiuiLevel(SignalStrength signalStrength) {
        if (Build.VERSION.SDK_INT > 28) {
            return TelephonyManager.getDefault().getMiuiLevel(signalStrength);
        }
        return signalStrength.getLevel();
    }

    /* access modifiers changed from: private */
    public void handleServiceStateChanged(ServiceState serviceState, int i) {
        if (serviceState == null) {
            this.mPhoneSignalAvailable[i] = false;
            return;
        }
        if (serviceState.getVoiceRegState() == 0 || serviceState.getVoiceRegState() == 2 || serviceState.isEmergencyOnly()) {
            this.mPhoneSignalAvailable[i] = true;
        } else {
            this.mPhoneSignalAvailable[i] = false;
        }
        Slog.d("PhoneSignalController", "level=" + this.mPhoneSignalLevel[i] + ";servicestate=" + serviceState + ";mPhoneSignalAvailable=" + this.mPhoneSignalAvailable[i]);
        Iterator<PhoneSignalChangeCallback> it = this.mSignalChangeCallbacks.iterator();
        while (it.hasNext()) {
            notifyPhoneSignalChangeCallback(it.next());
        }
    }

    private void addPhoneStateListener() {
        int phoneCount = PhoneUtils.getPhoneCount();
        this.mPhoneCount = phoneCount;
        this.mPhoneSignalAvailable = new boolean[phoneCount];
        this.mPhoneSignalLevel = new int[phoneCount];
        this.mPhoneStateListeners = new ArrayList<>(this.mPhoneCount);
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mPhoneSignalAvailable[i] = false;
            this.mPhoneSignalLevel[i] = 0;
            TelephonyManager.getDefault().listenForSlot(i, getPhoneStateListener(i), 257);
        }
    }

    private void removePhoneStateListener() {
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mPhoneSignalAvailable[i] = false;
            this.mPhoneSignalLevel[i] = 0;
            TelephonyManager.getDefault().listenForSlot(i, this.mPhoneStateListeners.get(i), 0);
        }
    }

    public void registerPhoneSignalChangeCallback(PhoneSignalChangeCallback phoneSignalChangeCallback) {
        if (this.mSignalChangeCallbacks.isEmpty()) {
            addPhoneStateListener();
        }
        if (!this.mSignalChangeCallbacks.contains(phoneSignalChangeCallback)) {
            this.mSignalChangeCallbacks.add(phoneSignalChangeCallback);
            notifyPhoneSignalChangeCallback(phoneSignalChangeCallback);
        }
    }

    private void notifyPhoneSignalChangeCallback(PhoneSignalChangeCallback phoneSignalChangeCallback) {
        int i = 0;
        boolean z = false;
        while (true) {
            boolean[] zArr = this.mPhoneSignalAvailable;
            if (i < zArr.length) {
                z |= zArr[i];
                i++;
            } else {
                this.mSignalAvailable = z;
                Slog.d("PhoneSignalController", "signalAvailable=" + z);
                phoneSignalChangeCallback.onSignalChange(z);
                return;
            }
        }
    }

    public void removePhoneSignalChangeCallback(PhoneSignalChangeCallback phoneSignalChangeCallback) {
        this.mSignalChangeCallbacks.remove(phoneSignalChangeCallback);
        if (this.mSignalChangeCallbacks.isEmpty()) {
            removePhoneStateListener();
        }
    }

    public boolean isSignalAvailable() {
        return this.mSignalAvailable;
    }
}
