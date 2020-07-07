package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRecentsSystemUserCallbacks extends IInterface {
    void registerNonSystemUserCallbacks(IBinder iBinder, int i) throws RemoteException;

    void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException;

    void sendLaunchRecentsEvent() throws RemoteException;

    void sendRecentsDrawnEvent() throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void updateRecentsVisibility(boolean z) throws RemoteException;

    public static abstract class Stub extends Binder implements IRecentsSystemUserCallbacks {
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.recents.IRecentsSystemUserCallbacks");
        }

        public static IRecentsSystemUserCallbacks asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IRecentsSystemUserCallbacks)) {
                return new Proxy(iBinder);
            }
            return (IRecentsSystemUserCallbacks) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                        registerNonSystemUserCallbacks(parcel.readStrongBinder(), parcel.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                        updateRecentsVisibility(parcel.readInt() != 0);
                        return true;
                    case 3:
                        parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                        startScreenPinning(parcel.readInt());
                        return true;
                    case 4:
                        parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                        sendRecentsDrawnEvent();
                        return true;
                    case 5:
                        parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                        sendDockingTopTaskEvent(parcel.readInt(), parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null);
                        return true;
                    case 6:
                        parcel.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                        sendLaunchRecentsEvent();
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                return true;
            }
        }

        private static class Proxy implements IRecentsSystemUserCallbacks {
            public static IRecentsSystemUserCallbacks sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void registerNonSystemUserCallbacks(IBinder iBinder, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeStrongBinder(iBinder);
                    obtain.writeInt(i);
                    if (this.mRemote.transact(1, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().registerNonSystemUserCallbacks(iBinder, i);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void updateRecentsVisibility(boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(z ? 1 : 0);
                    if (this.mRemote.transact(2, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().updateRecentsVisibility(z);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void startScreenPinning(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(i);
                    if (this.mRemote.transact(3, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().startScreenPinning(i);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void sendRecentsDrawnEvent() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    if (this.mRemote.transact(4, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().sendRecentsDrawnEvent();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    obtain.writeInt(i);
                    if (rect != null) {
                        obtain.writeInt(1);
                        rect.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(5, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().sendDockingTopTaskEvent(i, rect);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            public void sendLaunchRecentsEvent() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    if (this.mRemote.transact(6, obtain, (Parcel) null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().sendLaunchRecentsEvent();
                    }
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static IRecentsSystemUserCallbacks getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
