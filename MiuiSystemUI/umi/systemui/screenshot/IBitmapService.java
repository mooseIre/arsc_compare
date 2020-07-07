package com.android.systemui.screenshot;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBitmapService extends IInterface {
    void registerCallback(IScreenShotCallback iScreenShotCallback) throws RemoteException;

    void unregisterCallback(IScreenShotCallback iScreenShotCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IBitmapService {
        public static IBitmapService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.screenshot.IBitmapService");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IBitmapService)) {
                return new Proxy(iBinder);
            }
            return (IBitmapService) queryLocalInterface;
        }

        private static class Proxy implements IBitmapService {
            public static IBitmapService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void registerCallback(IScreenShotCallback iScreenShotCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.screenshot.IBitmapService");
                    obtain.writeStrongBinder(iScreenShotCallback != null ? iScreenShotCallback.asBinder() : null);
                    if (this.mRemote.transact(1, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(iScreenShotCallback);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void unregisterCallback(IScreenShotCallback iScreenShotCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.screenshot.IBitmapService");
                    obtain.writeStrongBinder(iScreenShotCallback != null ? iScreenShotCallback.asBinder() : null);
                    if (this.mRemote.transact(2, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                        obtain2.recycle();
                        obtain.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterCallback(iScreenShotCallback);
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static IBitmapService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
