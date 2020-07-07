package com.android.systemui.fsgesture;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFsGestureCallback extends IInterface {
    void changeAlphaScale(float f, float f2, int i, int i2, int i3, int i4, boolean z) throws RemoteException;

    TransitionAnimationSpec getSpec(String str, int i) throws RemoteException;

    void notifyHomeModeFsGestureStart() throws RemoteException;

    void notifyMiuiAnimationEnd() throws RemoteException;

    void notifyMiuiAnimationStart() throws RemoteException;

    public static abstract class Stub extends Binder implements IFsGestureCallback {
        public static IFsGestureCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.fsgesture.IFsGestureCallback");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IFsGestureCallback)) {
                return new Proxy(iBinder);
            }
            return (IFsGestureCallback) queryLocalInterface;
        }

        private static class Proxy implements IFsGestureCallback {
            public static IFsGestureCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void changeAlphaScale(float f, float f2, int i, int i2, int i3, int i4, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.fsgesture.IFsGestureCallback");
                    float f3 = f;
                    obtain.writeFloat(f);
                    float f4 = f2;
                    obtain.writeFloat(f2);
                    int i5 = i;
                    obtain.writeInt(i);
                    int i6 = i2;
                    obtain.writeInt(i2);
                    int i7 = i3;
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(z ? 1 : 0);
                    if (this.mRemote.transact(1, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().changeAlphaScale(f, f2, i, i2, i3, i4, z);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public TransitionAnimationSpec getSpec(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.fsgesture.IFsGestureCallback");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    if (!this.mRemote.transact(2, obtain, obtain2, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpec(str, i);
                    }
                    obtain2.readException();
                    TransitionAnimationSpec createFromParcel = obtain2.readInt() != 0 ? TransitionAnimationSpec.CREATOR.createFromParcel(obtain2) : null;
                    obtain2.recycle();
                    obtain.recycle();
                    return createFromParcel;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void notifyMiuiAnimationStart() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.fsgesture.IFsGestureCallback");
                    if (this.mRemote.transact(3, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyMiuiAnimationStart();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void notifyMiuiAnimationEnd() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.fsgesture.IFsGestureCallback");
                    if (this.mRemote.transact(4, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyMiuiAnimationEnd();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void notifyHomeModeFsGestureStart() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.fsgesture.IFsGestureCallback");
                    if (this.mRemote.transact(5, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyHomeModeFsGestureStart();
                    }
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static IFsGestureCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
