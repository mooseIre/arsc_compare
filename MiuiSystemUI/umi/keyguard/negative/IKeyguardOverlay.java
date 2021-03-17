package com.android.keyguard.negative;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.WindowManager;

public interface IKeyguardOverlay extends IInterface {
    void closeOverlay(int i) throws RemoteException;

    void endScroll() throws RemoteException;

    void onPause() throws RemoteException;

    void onResume() throws RemoteException;

    void onScroll(float f) throws RemoteException;

    void startScroll() throws RemoteException;

    void windowAttached(WindowManager.LayoutParams layoutParams, IKeyguardOverlayCallback iKeyguardOverlayCallback, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IKeyguardOverlay {
        public static IKeyguardOverlay asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.keyguard.negative.IKeyguardOverlay");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IKeyguardOverlay)) {
                return new Proxy(iBinder);
            }
            return (IKeyguardOverlay) queryLocalInterface;
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IKeyguardOverlay {
            public static IKeyguardOverlay sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void startScroll() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    if (this.mRemote.transact(1, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().startScroll();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void onScroll(float f) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    obtain.writeFloat(f);
                    if (this.mRemote.transact(2, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onScroll(f);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void endScroll() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    if (this.mRemote.transact(3, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().endScroll();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void windowAttached(WindowManager.LayoutParams layoutParams, IKeyguardOverlayCallback iKeyguardOverlayCallback, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    if (layoutParams != null) {
                        obtain.writeInt(1);
                        layoutParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iKeyguardOverlayCallback != null ? iKeyguardOverlayCallback.asBinder() : null);
                    obtain.writeInt(i);
                    if (this.mRemote.transact(4, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().windowAttached(layoutParams, iKeyguardOverlayCallback, i);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void closeOverlay(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    obtain.writeInt(i);
                    if (this.mRemote.transact(6, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().closeOverlay(i);
                    }
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void onPause() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    if (this.mRemote.transact(7, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onPause();
                    }
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.android.keyguard.negative.IKeyguardOverlay
            public void onResume() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.keyguard.negative.IKeyguardOverlay");
                    if (this.mRemote.transact(8, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onResume();
                    }
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static IKeyguardOverlay getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
