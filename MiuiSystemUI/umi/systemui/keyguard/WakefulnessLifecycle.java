package com.android.systemui.keyguard;

import android.os.Trace;
import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class WakefulnessLifecycle extends Lifecycle<Observer> implements Dumpable {
    private int mWakefulness = 0;

    public interface Observer {
        void onFinishedGoingToSleep() {
        }

        void onFinishedWakingUp() {
        }

        void onStartedGoingToSleep() {
        }

        void onStartedWakingUp() {
        }
    }

    public int getWakefulness() {
        return this.mWakefulness;
    }

    public void dispatchStartedWakingUp() {
        if (getWakefulness() != 1) {
            setWakefulness(1);
            dispatch($$Lambda$TPhVA13qrDBGFKbgQpRNBPBvAqI.INSTANCE);
        }
    }

    public void dispatchFinishedWakingUp() {
        if (getWakefulness() != 2) {
            setWakefulness(2);
            dispatch($$Lambda$v8UUYbN3IpgugNoVVCKpk3ABDI.INSTANCE);
        }
    }

    public void dispatchStartedGoingToSleep() {
        if (getWakefulness() != 3) {
            setWakefulness(3);
            dispatch($$Lambda$ASgSeR7gTZT1Q2JGNWCU20EppLY.INSTANCE);
        }
    }

    public void dispatchFinishedGoingToSleep() {
        if (getWakefulness() != 0) {
            setWakefulness(0);
            dispatch($$Lambda$AKoGNPXjF07Pzc3_fzdQTCHgk6E.INSTANCE);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("WakefulnessLifecycle:");
        printWriter.println("  mWakefulness=" + this.mWakefulness);
    }

    private void setWakefulness(int i) {
        this.mWakefulness = i;
        Trace.traceCounter(4096, "wakefulness", i);
    }
}
