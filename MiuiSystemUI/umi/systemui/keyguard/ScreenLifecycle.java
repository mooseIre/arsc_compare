package com.android.systemui.keyguard;

import android.os.Trace;
import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ScreenLifecycle extends Lifecycle<Observer> implements Dumpable {
    private int mScreenState = 0;

    public interface Observer {
        default void onScreenTurnedOff() {
        }

        default void onScreenTurnedOn() {
        }

        default void onScreenTurningOff() {
        }

        default void onScreenTurningOn() {
        }
    }

    public int getScreenState() {
        return this.mScreenState;
    }

    public void dispatchScreenTurningOn() {
        setScreenState(1);
        dispatch($$Lambda$w9PiqN50NESCg48fJRhE_dJBSdc.INSTANCE);
    }

    public void dispatchScreenTurnedOn() {
        setScreenState(2);
        dispatch($$Lambda$n4aPxVrHdTzFo5NE6H_ILivOadQ.INSTANCE);
    }

    public void dispatchScreenTurningOff() {
        setScreenState(3);
        dispatch($$Lambda$DmSZzOb4vxXoGU7unAMsJYIcFwE.INSTANCE);
    }

    public void dispatchScreenTurnedOff() {
        setScreenState(0);
        dispatch($$Lambda$K8LiTMkPknhhclqjA2eboLxaGEU.INSTANCE);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("ScreenLifecycle:");
        printWriter.println("  mScreenState=" + this.mScreenState);
    }

    private void setScreenState(int i) {
        this.mScreenState = i;
        Trace.traceCounter(4096, "screenState", i);
    }
}
