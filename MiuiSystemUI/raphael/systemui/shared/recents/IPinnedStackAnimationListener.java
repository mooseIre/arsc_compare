package com.android.systemui.shared.recents;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPinnedStackAnimationListener extends IInterface {
    void onPinnedStackAnimationStarted() throws RemoteException;

    public static abstract class Stub extends Binder implements IPinnedStackAnimationListener {
        public static IPinnedStackAnimationListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.android.systemui.shared.recents.IPinnedStackAnimationListener");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IPinnedStackAnimationListener)) {
                return new Proxy(iBinder);
            }
            return (IPinnedStackAnimationListener) queryLocalInterface;
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPinnedStackAnimationListener {
            public static IPinnedStackAnimationListener sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.android.systemui.shared.recents.IPinnedStackAnimationListener
            public void onPinnedStackAnimationStarted() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.android.systemui.shared.recents.IPinnedStackAnimationListener");
                    if (this.mRemote.transact(1, obtain, null, 1) || Stub.getDefaultImpl() == null) {
                        obtain.recycle();
                    } else {
                        Stub.getDefaultImpl().onPinnedStackAnimationStarted();
                    }
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static IPinnedStackAnimationListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
