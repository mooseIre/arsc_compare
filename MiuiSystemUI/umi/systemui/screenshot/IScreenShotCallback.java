package com.android.systemui.screenshot;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IScreenShotCallback extends IInterface {
    void quitThumnail() throws RemoteException;

    public static abstract class Stub extends Binder implements IScreenShotCallback {
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.screenshot.IScreenShotCallback");
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface("com.android.systemui.screenshot.IScreenShotCallback");
                quitThumnail();
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString("com.android.systemui.screenshot.IScreenShotCallback");
                return true;
            }
        }
    }
}
