package com.android.keyguard.negative;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IKeyguardOverlayCallback extends IInterface {
    void overlayScrollChanged(float f) throws RemoteException;

    void overlayStatusChanged(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IKeyguardOverlayCallback {
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.keyguard.negative.IKeyguardOverlayCallback");
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface("com.android.keyguard.negative.IKeyguardOverlayCallback");
                overlayScrollChanged(parcel.readFloat());
                parcel2.writeNoException();
                return true;
            } else if (i == 2) {
                parcel.enforceInterface("com.android.keyguard.negative.IKeyguardOverlayCallback");
                overlayStatusChanged(parcel.readInt());
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString("com.android.keyguard.negative.IKeyguardOverlayCallback");
                return true;
            }
        }
    }
}
