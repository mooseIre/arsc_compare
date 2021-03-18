package com.android.systemui.shared.system;

import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.WindowManagerGlobal;
import java.io.PrintWriter;

public class InputConsumerController {
    private static final String TAG = "InputConsumerController";
    private InputEventReceiver mInputEventReceiver;
    private InputListener mListener;
    private final String mName;
    private RegistrationListener mRegistrationListener;
    private final IBinder mToken = new Binder();
    private final IWindowManager mWindowManager;

    public interface InputListener {
        boolean onInputEvent(InputEvent inputEvent);
    }

    public interface RegistrationListener {
        void onRegistrationChanged(boolean z);
    }

    /* access modifiers changed from: private */
    public final class InputEventReceiver extends BatchedInputEventReceiver {
        InputEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
            super(inputChannel, looper, choreographer);
        }

        public void onInputEvent(InputEvent inputEvent) {
            boolean z = true;
            try {
                if (InputConsumerController.this.mListener != null) {
                    z = InputConsumerController.this.mListener.onInputEvent(inputEvent);
                }
            } finally {
                finishInputEvent(inputEvent, z);
            }
        }
    }

    public InputConsumerController(IWindowManager iWindowManager, String str) {
        this.mWindowManager = iWindowManager;
        this.mName = str;
    }

    public static InputConsumerController getPipInputConsumer() {
        return new InputConsumerController(WindowManagerGlobal.getWindowManagerService(), "pip_input_consumer");
    }

    public void setInputListener(InputListener inputListener) {
        this.mListener = inputListener;
    }

    public void setRegistrationListener(RegistrationListener registrationListener) {
        this.mRegistrationListener = registrationListener;
        if (registrationListener != null) {
            registrationListener.onRegistrationChanged(this.mInputEventReceiver != null);
        }
    }

    public void registerInputConsumer(boolean z) {
        if (this.mInputEventReceiver == null) {
            InputChannel inputChannel = new InputChannel();
            try {
                this.mWindowManager.destroyInputConsumer(this.mName, 0);
                this.mWindowManager.createInputConsumer(this.mToken, this.mName, 0, inputChannel);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to create input consumer", e);
            }
            this.mInputEventReceiver = new InputEventReceiver(inputChannel, Looper.myLooper(), z ? Choreographer.getSfInstance() : Choreographer.getInstance());
            RegistrationListener registrationListener = this.mRegistrationListener;
            if (registrationListener != null) {
                registrationListener.onRegistrationChanged(true);
            }
        }
    }

    public void unregisterInputConsumer() {
        if (this.mInputEventReceiver != null) {
            try {
                this.mWindowManager.destroyInputConsumer(this.mName, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to destroy input consumer", e);
            }
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
            RegistrationListener registrationListener = this.mRegistrationListener;
            if (registrationListener != null) {
                registrationListener.onRegistrationChanged(false);
            }
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + TAG);
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append("registered=");
        sb.append(this.mInputEventReceiver != null);
        printWriter.println(sb.toString());
    }
}
