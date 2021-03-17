package com.android.systemui.shared.recents;

import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.MotionEvent;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.shared.recents.model.Task$TaskKey;

public interface ISystemUiProxy extends IInterface {
    Rect getNonMinimizedSplitScreenSecondaryBounds() throws RemoteException;

    void handleImageAsScreenshot(Bitmap bitmap, Rect rect, Insets insets, int i) throws RemoteException;

    void handleImageBundleAsScreenshot(Bundle bundle, Rect rect, Insets insets, Task$TaskKey task$TaskKey) throws RemoteException;

    Bundle monitorGestureInput(String str, int i) throws RemoteException;

    void notifyAccessibilityButtonClicked(int i) throws RemoteException;

    void notifyAccessibilityButtonLongClicked() throws RemoteException;

    void notifySwipeToHomeFinished() throws RemoteException;

    void onAssistantGestureCompletion(float f) throws RemoteException;

    void onAssistantProgress(float f) throws RemoteException;

    void onOverviewShown(boolean z) throws RemoteException;

    void onQuickSwitchToNewTask(int i) throws RemoteException;

    void onSplitScreenInvoked() throws RemoteException;

    void onStatusBarMotionEvent(MotionEvent motionEvent) throws RemoteException;

    void setBackButtonAlpha(float f, boolean z) throws RemoteException;

    void setNavBarButtonAlpha(float f, boolean z) throws RemoteException;

    void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) throws RemoteException;

    void setShelfHeight(boolean z, int i) throws RemoteException;

    void setSplitScreenMinimized(boolean z) throws RemoteException;

    void startAssistant(Bundle bundle) throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void stopScreenPinning() throws RemoteException;

    public static abstract class Stub extends Binder implements ISystemUiProxy {
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.shared.recents.ISystemUiProxy");
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 2) {
                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                startScreenPinning(parcel.readInt());
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                boolean z = false;
                MotionEvent motionEvent = null;
                Task$TaskKey task$TaskKey = null;
                Insets insets = null;
                Bundle bundle = null;
                switch (i) {
                    case 6:
                        parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                        onSplitScreenInvoked();
                        parcel2.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        onOverviewShown(z);
                        parcel2.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                        Rect nonMinimizedSplitScreenSecondaryBounds = getNonMinimizedSplitScreenSecondaryBounds();
                        parcel2.writeNoException();
                        if (nonMinimizedSplitScreenSecondaryBounds != null) {
                            parcel2.writeInt(1);
                            nonMinimizedSplitScreenSecondaryBounds.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 9:
                        parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                        float readFloat = parcel.readFloat();
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        setBackButtonAlpha(readFloat, z);
                        parcel2.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                        if (parcel.readInt() != 0) {
                            motionEvent = (MotionEvent) MotionEvent.CREATOR.createFromParcel(parcel);
                        }
                        onStatusBarMotionEvent(motionEvent);
                        parcel2.writeNoException();
                        return true;
                    default:
                        switch (i) {
                            case 13:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                onAssistantProgress(parcel.readFloat());
                                parcel2.writeNoException();
                                return true;
                            case 14:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                if (parcel.readInt() != 0) {
                                    bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                                }
                                startAssistant(bundle);
                                parcel2.writeNoException();
                                return true;
                            case 15:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                Bundle monitorGestureInput = monitorGestureInput(parcel.readString(), parcel.readInt());
                                parcel2.writeNoException();
                                if (monitorGestureInput != null) {
                                    parcel2.writeInt(1);
                                    monitorGestureInput.writeToParcel(parcel2, 1);
                                } else {
                                    parcel2.writeInt(0);
                                }
                                return true;
                            case 16:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                notifyAccessibilityButtonClicked(parcel.readInt());
                                parcel2.writeNoException();
                                return true;
                            case 17:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                notifyAccessibilityButtonLongClicked();
                                parcel2.writeNoException();
                                return true;
                            case 18:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                stopScreenPinning();
                                parcel2.writeNoException();
                                return true;
                            case 19:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                onAssistantGestureCompletion(parcel.readFloat());
                                parcel2.writeNoException();
                                return true;
                            case 20:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                float readFloat2 = parcel.readFloat();
                                if (parcel.readInt() != 0) {
                                    z = true;
                                }
                                setNavBarButtonAlpha(readFloat2, z);
                                parcel2.writeNoException();
                                return true;
                            case 21:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                if (parcel.readInt() != 0) {
                                    z = true;
                                }
                                setShelfHeight(z, parcel.readInt());
                                parcel2.writeNoException();
                                return true;
                            case 22:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                Bitmap bitmap = parcel.readInt() != 0 ? (Bitmap) Bitmap.CREATOR.createFromParcel(parcel) : null;
                                Rect rect = parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null;
                                if (parcel.readInt() != 0) {
                                    insets = (Insets) Insets.CREATOR.createFromParcel(parcel);
                                }
                                handleImageAsScreenshot(bitmap, rect, insets, parcel.readInt());
                                parcel2.writeNoException();
                                return true;
                            case 23:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                if (parcel.readInt() != 0) {
                                    z = true;
                                }
                                setSplitScreenMinimized(z);
                                parcel2.writeNoException();
                                return true;
                            case 24:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                notifySwipeToHomeFinished();
                                parcel2.writeNoException();
                                return true;
                            case 25:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                setPinnedStackAnimationListener(IPinnedStackAnimationListener.Stub.asInterface(parcel.readStrongBinder()));
                                parcel2.writeNoException();
                                return true;
                            case 26:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                onQuickSwitchToNewTask(parcel.readInt());
                                parcel2.writeNoException();
                                return true;
                            case 27:
                                parcel.enforceInterface("com.android.systemui.shared.recents.ISystemUiProxy");
                                Bundle bundle2 = parcel.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(parcel) : null;
                                Rect rect2 = parcel.readInt() != 0 ? (Rect) Rect.CREATOR.createFromParcel(parcel) : null;
                                Insets insets2 = parcel.readInt() != 0 ? (Insets) Insets.CREATOR.createFromParcel(parcel) : null;
                                if (parcel.readInt() != 0) {
                                    task$TaskKey = Task$TaskKey.CREATOR.createFromParcel(parcel);
                                }
                                handleImageBundleAsScreenshot(bundle2, rect2, insets2, task$TaskKey);
                                parcel2.writeNoException();
                                return true;
                            default:
                                return super.onTransact(i, parcel, parcel2, i2);
                        }
                }
            } else {
                parcel2.writeString("com.android.systemui.shared.recents.ISystemUiProxy");
                return true;
            }
        }
    }
}
