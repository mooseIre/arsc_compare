package com.android.systemui.shared.recents;

import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOverviewProxy extends IInterface {
    void onActiveNavBarRegionChanges(Region region) throws RemoteException;

    void onInitialize(Bundle bundle) throws RemoteException;

    void onOverviewHidden(boolean z, boolean z2) throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onOverviewToggle() throws RemoteException;

    void onSystemUiStateChanged(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IOverviewProxy {
        public static IOverviewProxy asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.shared.recents.IOverviewProxy");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IOverviewProxy)) {
                return new Proxy(iBinder);
            }
            return (IOverviewProxy) queryLocalInterface;
        }

        private static class Proxy implements IOverviewProxy {
            public static IOverviewProxy sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onActiveNavBarRegionChanges(Region region) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IOverviewProxy");
                    if (region != null) {
                        obtain.writeInt(1);
                        region.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(12, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onActiveNavBarRegionChanges(region);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void onInitialize(Bundle bundle) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IOverviewProxy");
                    if (bundle != null) {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(13, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onInitialize(bundle);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void onOverviewToggle() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IOverviewProxy");
                    if (this.mRemote.transact(7, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onOverviewToggle();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void onOverviewShown(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IOverviewProxy");
                    obtain.writeInt(z ? 1 : 0);
                    if (this.mRemote.transact(8, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onOverviewShown(z);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void onOverviewHidden(boolean z, boolean z2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IOverviewProxy");
                    int i = 0;
                    obtain.writeInt(z ? 1 : 0);
                    if (z2) {
                        i = 1;
                    }
                    obtain.writeInt(i);
                    if (this.mRemote.transact(9, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onOverviewHidden(z, z2);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void onSystemUiStateChanged(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IOverviewProxy");
                    obtain.writeInt(i);
                    if (this.mRemote.transact(17, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onSystemUiStateChanged(i);
                    }
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static IOverviewProxy getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
