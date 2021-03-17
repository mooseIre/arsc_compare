package com.android.systemui.shared.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMiuiSystemUiProxy extends IInterface {
    void exitSplitScreen() throws RemoteException;

    Rect getMiddleSplitScreenSecondaryBounds() throws RemoteException;

    void onAssistantGestureCompletion() throws RemoteException;

    void onGestureLineProgress(float f) throws RemoteException;

    public static abstract class Stub extends Binder implements IMiuiSystemUiProxy {
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.shared.recents.IMiuiSystemUiProxy");
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 2) {
                parcel.enforceInterface("com.android.systemui.shared.recents.IMiuiSystemUiProxy");
                exitSplitScreen();
                parcel2.writeNoException();
                return true;
            } else if (i == 3) {
                parcel.enforceInterface("com.android.systemui.shared.recents.IMiuiSystemUiProxy");
                Rect middleSplitScreenSecondaryBounds = getMiddleSplitScreenSecondaryBounds();
                parcel2.writeNoException();
                if (middleSplitScreenSecondaryBounds != null) {
                    parcel2.writeInt(1);
                    middleSplitScreenSecondaryBounds.writeToParcel(parcel2, 1);
                } else {
                    parcel2.writeInt(0);
                }
                return true;
            } else if (i == 4) {
                parcel.enforceInterface("com.android.systemui.shared.recents.IMiuiSystemUiProxy");
                onGestureLineProgress(parcel.readFloat());
                parcel2.writeNoException();
                return true;
            } else if (i == 5) {
                parcel.enforceInterface("com.android.systemui.shared.recents.IMiuiSystemUiProxy");
                onAssistantGestureCompletion();
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString("com.android.systemui.shared.recents.IMiuiSystemUiProxy");
                return true;
            }
        }
    }
}
