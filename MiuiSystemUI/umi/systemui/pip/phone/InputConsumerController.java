package com.android.systemui.pip.phone;

import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.MotionEvent;
import java.io.PrintWriter;

public class InputConsumerController {
    private static final String TAG = "InputConsumerController";
    private PipInputEventReceiver mInputEventReceiver;
    /* access modifiers changed from: private */
    public TouchListener mListener;
    private RegistrationListener mRegistrationListener;
    private IWindowManager mWindowManager;

    public interface RegistrationListener {
        void onRegistrationChanged(boolean z);
    }

    public interface TouchListener {
        boolean onTouchEvent(MotionEvent motionEvent);
    }

    private final class PipInputEventReceiver extends BatchedInputEventReceiver {
        public PipInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper, Choreographer.getSfInstance());
        }

        public void onInputEvent(InputEvent inputEvent) {
            onInputEvent(inputEvent, 0);
        }

        public void onInputEvent(InputEvent inputEvent, int i) {
            boolean z = true;
            try {
                if (InputConsumerController.this.mListener != null && (inputEvent instanceof MotionEvent)) {
                    z = InputConsumerController.this.mListener.onTouchEvent((MotionEvent) inputEvent);
                }
            } finally {
                finishInputEvent(inputEvent, z);
            }
        }
    }

    public InputConsumerController(IWindowManager iWindowManager) {
        this.mWindowManager = iWindowManager;
        registerInputConsumer();
    }

    public void setTouchListener(TouchListener touchListener) {
        this.mListener = touchListener;
    }

    public void setRegistrationListener(RegistrationListener registrationListener) {
        this.mRegistrationListener = registrationListener;
        RegistrationListener registrationListener2 = this.mRegistrationListener;
        if (registrationListener2 != null) {
            registrationListener2.onRegistrationChanged(this.mInputEventReceiver != null);
        }
    }

    public boolean isRegistered() {
        return this.mInputEventReceiver != null;
    }

    public void registerInputConsumer() {
        if (this.mInputEventReceiver == null) {
            InputChannel inputChannel = new InputChannel();
            try {
                IWindowManagerCompat.destroyInputConsumer(this.mWindowManager, "pip_input_consumer", 0);
                IWindowManagerCompat.createInputConsumer(this.mWindowManager, (IBinder) null, "pip_input_consumer", 0, inputChannel);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to create PIP input consumer", e);
            }
            this.mInputEventReceiver = new PipInputEventReceiver(inputChannel, Looper.myLooper());
            RegistrationListener registrationListener = this.mRegistrationListener;
            if (registrationListener != null) {
                registrationListener.onRegistrationChanged(true);
            }
        }
    }

    public void unregisterInputConsumer() {
        if (this.mInputEventReceiver != null) {
            try {
                IWindowManagerCompat.destroyInputConsumer(this.mWindowManager, "pip_input_consumer", 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to destroy PIP input consumer", e);
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
