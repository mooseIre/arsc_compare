package com.android.systemui.doze;

import android.os.Bundle;

public interface AodHost {
    void fireAodState(boolean z);

    void fireFingerprintPressed(boolean z);

    void onGxzwIconChanged(boolean z);

    void onSimPinSecureChanged(boolean z);

    void sendCommand(String str, int i, Bundle bundle);

    void stopDozing();
}
