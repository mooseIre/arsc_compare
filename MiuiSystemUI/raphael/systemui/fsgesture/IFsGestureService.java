package com.android.systemui.fsgesture;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.systemui.fsgesture.IFsGestureCallback;

public interface IFsGestureService extends IInterface {
    void notifyHomeStatus(boolean z) throws RemoteException;

    void registerCallback(String str, IFsGestureCallback iFsGestureCallback) throws RemoteException;

    void unregisterCallback(String str, IFsGestureCallback iFsGestureCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IFsGestureService {
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.fsgesture.IFsGestureService");
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface("com.android.systemui.fsgesture.IFsGestureService");
                registerCallback(parcel.readString(), IFsGestureCallback.Stub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                return true;
            } else if (i == 2) {
                parcel.enforceInterface("com.android.systemui.fsgesture.IFsGestureService");
                unregisterCallback(parcel.readString(), IFsGestureCallback.Stub.asInterface(parcel.readStrongBinder()));
                parcel2.writeNoException();
                return true;
            } else if (i == 3) {
                parcel.enforceInterface("com.android.systemui.fsgesture.IFsGestureService");
                notifyHomeStatus(parcel.readInt() != 0);
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString("com.android.systemui.fsgesture.IFsGestureService");
                return true;
            }
        }
    }
}
