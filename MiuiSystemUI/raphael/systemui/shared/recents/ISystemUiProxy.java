package com.android.systemui.shared.recents;

import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.view.MotionEvent;
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: android.view.MotionEvent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: android.graphics.Insets} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v10, resolved type: com.android.systemui.shared.recents.model.Task$TaskKey} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v13 */
        /* JADX WARNING: type inference failed for: r3v14 */
        /* JADX WARNING: type inference failed for: r3v15 */
        /* JADX WARNING: type inference failed for: r3v16 */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTransact(int r5, android.os.Parcel r6, android.os.Parcel r7, int r8) throws android.os.RemoteException {
            /*
                r4 = this;
                r0 = 2
                r1 = 1
                java.lang.String r2 = "com.android.systemui.shared.recents.ISystemUiProxy"
                if (r5 == r0) goto L_0x01dd
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                if (r5 == r0) goto L_0x01d9
                r0 = 0
                r3 = 0
                switch(r5) {
                    case 6: goto L_0x01cf;
                    case 7: goto L_0x01be;
                    case 8: goto L_0x01a7;
                    case 9: goto L_0x0192;
                    case 10: goto L_0x0179;
                    default: goto L_0x0010;
                }
            L_0x0010:
                switch(r5) {
                    case 13: goto L_0x016b;
                    case 14: goto L_0x0152;
                    case 15: goto L_0x0133;
                    case 16: goto L_0x0125;
                    case 17: goto L_0x011b;
                    case 18: goto L_0x0111;
                    case 19: goto L_0x0103;
                    case 20: goto L_0x00ee;
                    case 21: goto L_0x00d9;
                    case 22: goto L_0x009c;
                    case 23: goto L_0x008b;
                    case 24: goto L_0x0081;
                    case 25: goto L_0x006f;
                    case 26: goto L_0x0061;
                    case 27: goto L_0x0018;
                    default: goto L_0x0013;
                }
            L_0x0013:
                boolean r4 = super.onTransact(r5, r6, r7, r8)
                return r4
            L_0x0018:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x002a
                android.os.Parcelable$Creator r5 = android.os.Bundle.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                android.os.Bundle r5 = (android.os.Bundle) r5
                goto L_0x002b
            L_0x002a:
                r5 = r3
            L_0x002b:
                int r8 = r6.readInt()
                if (r8 == 0) goto L_0x003a
                android.os.Parcelable$Creator r8 = android.graphics.Rect.CREATOR
                java.lang.Object r8 = r8.createFromParcel(r6)
                android.graphics.Rect r8 = (android.graphics.Rect) r8
                goto L_0x003b
            L_0x003a:
                r8 = r3
            L_0x003b:
                int r0 = r6.readInt()
                if (r0 == 0) goto L_0x004a
                android.os.Parcelable$Creator r0 = android.graphics.Insets.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r6)
                android.graphics.Insets r0 = (android.graphics.Insets) r0
                goto L_0x004b
            L_0x004a:
                r0 = r3
            L_0x004b:
                int r2 = r6.readInt()
                if (r2 == 0) goto L_0x005a
                android.os.Parcelable$Creator<com.android.systemui.shared.recents.model.Task$TaskKey> r2 = com.android.systemui.shared.recents.model.Task$TaskKey.CREATOR
                java.lang.Object r6 = r2.createFromParcel(r6)
                r3 = r6
                com.android.systemui.shared.recents.model.Task$TaskKey r3 = (com.android.systemui.shared.recents.model.Task$TaskKey) r3
            L_0x005a:
                r4.handleImageBundleAsScreenshot(r5, r8, r0, r3)
                r7.writeNoException()
                return r1
            L_0x0061:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                r4.onQuickSwitchToNewTask(r5)
                r7.writeNoException()
                return r1
            L_0x006f:
                r6.enforceInterface(r2)
                android.os.IBinder r5 = r6.readStrongBinder()
                com.android.systemui.shared.recents.IPinnedStackAnimationListener r5 = com.android.systemui.shared.recents.IPinnedStackAnimationListener.Stub.asInterface(r5)
                r4.setPinnedStackAnimationListener(r5)
                r7.writeNoException()
                return r1
            L_0x0081:
                r6.enforceInterface(r2)
                r4.notifySwipeToHomeFinished()
                r7.writeNoException()
                return r1
            L_0x008b:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x0095
                r0 = r1
            L_0x0095:
                r4.setSplitScreenMinimized(r0)
                r7.writeNoException()
                return r1
            L_0x009c:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x00ae
                android.os.Parcelable$Creator r5 = android.graphics.Bitmap.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                android.graphics.Bitmap r5 = (android.graphics.Bitmap) r5
                goto L_0x00af
            L_0x00ae:
                r5 = r3
            L_0x00af:
                int r8 = r6.readInt()
                if (r8 == 0) goto L_0x00be
                android.os.Parcelable$Creator r8 = android.graphics.Rect.CREATOR
                java.lang.Object r8 = r8.createFromParcel(r6)
                android.graphics.Rect r8 = (android.graphics.Rect) r8
                goto L_0x00bf
            L_0x00be:
                r8 = r3
            L_0x00bf:
                int r0 = r6.readInt()
                if (r0 == 0) goto L_0x00ce
                android.os.Parcelable$Creator r0 = android.graphics.Insets.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r6)
                r3 = r0
                android.graphics.Insets r3 = (android.graphics.Insets) r3
            L_0x00ce:
                int r6 = r6.readInt()
                r4.handleImageAsScreenshot(r5, r8, r3, r6)
                r7.writeNoException()
                return r1
            L_0x00d9:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x00e3
                r0 = r1
            L_0x00e3:
                int r5 = r6.readInt()
                r4.setShelfHeight(r0, r5)
                r7.writeNoException()
                return r1
            L_0x00ee:
                r6.enforceInterface(r2)
                float r5 = r6.readFloat()
                int r6 = r6.readInt()
                if (r6 == 0) goto L_0x00fc
                r0 = r1
            L_0x00fc:
                r4.setNavBarButtonAlpha(r5, r0)
                r7.writeNoException()
                return r1
            L_0x0103:
                r6.enforceInterface(r2)
                float r5 = r6.readFloat()
                r4.onAssistantGestureCompletion(r5)
                r7.writeNoException()
                return r1
            L_0x0111:
                r6.enforceInterface(r2)
                r4.stopScreenPinning()
                r7.writeNoException()
                return r1
            L_0x011b:
                r6.enforceInterface(r2)
                r4.notifyAccessibilityButtonLongClicked()
                r7.writeNoException()
                return r1
            L_0x0125:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                r4.notifyAccessibilityButtonClicked(r5)
                r7.writeNoException()
                return r1
            L_0x0133:
                r6.enforceInterface(r2)
                java.lang.String r5 = r6.readString()
                int r6 = r6.readInt()
                android.os.Bundle r4 = r4.monitorGestureInput(r5, r6)
                r7.writeNoException()
                if (r4 == 0) goto L_0x014e
                r7.writeInt(r1)
                r4.writeToParcel(r7, r1)
                goto L_0x0151
            L_0x014e:
                r7.writeInt(r0)
            L_0x0151:
                return r1
            L_0x0152:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x0164
                android.os.Parcelable$Creator r5 = android.os.Bundle.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                r3 = r5
                android.os.Bundle r3 = (android.os.Bundle) r3
            L_0x0164:
                r4.startAssistant(r3)
                r7.writeNoException()
                return r1
            L_0x016b:
                r6.enforceInterface(r2)
                float r5 = r6.readFloat()
                r4.onAssistantProgress(r5)
                r7.writeNoException()
                return r1
            L_0x0179:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x018b
                android.os.Parcelable$Creator r5 = android.view.MotionEvent.CREATOR
                java.lang.Object r5 = r5.createFromParcel(r6)
                r3 = r5
                android.view.MotionEvent r3 = (android.view.MotionEvent) r3
            L_0x018b:
                r4.onStatusBarMotionEvent(r3)
                r7.writeNoException()
                return r1
            L_0x0192:
                r6.enforceInterface(r2)
                float r5 = r6.readFloat()
                int r6 = r6.readInt()
                if (r6 == 0) goto L_0x01a0
                r0 = r1
            L_0x01a0:
                r4.setBackButtonAlpha(r5, r0)
                r7.writeNoException()
                return r1
            L_0x01a7:
                r6.enforceInterface(r2)
                android.graphics.Rect r4 = r4.getNonMinimizedSplitScreenSecondaryBounds()
                r7.writeNoException()
                if (r4 == 0) goto L_0x01ba
                r7.writeInt(r1)
                r4.writeToParcel(r7, r1)
                goto L_0x01bd
            L_0x01ba:
                r7.writeInt(r0)
            L_0x01bd:
                return r1
            L_0x01be:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                if (r5 == 0) goto L_0x01c8
                r0 = r1
            L_0x01c8:
                r4.onOverviewShown(r0)
                r7.writeNoException()
                return r1
            L_0x01cf:
                r6.enforceInterface(r2)
                r4.onSplitScreenInvoked()
                r7.writeNoException()
                return r1
            L_0x01d9:
                r7.writeString(r2)
                return r1
            L_0x01dd:
                r6.enforceInterface(r2)
                int r5 = r6.readInt()
                r4.startScreenPinning(r5)
                r7.writeNoException()
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.shared.recents.ISystemUiProxy.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }
}
