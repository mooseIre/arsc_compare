package com.android.systemui;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.app.INotificationManager;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.Picture;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.MiuiMultiWindowUtils;
import android.view.InputMonitor;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.RegisterStatusBarResult;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.MutableBoolean;
import com.android.systemui.recents.model.Task;
import java.util.List;
import miui.securityspace.XSpaceUserHandle;

public class SystemUICompat {
    private static INotificationManager sINM = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    private static IActivityTaskManager sITM = ActivityTaskManager.getService();

    public static void cancelTaskThumbnailTransition(IActivityManager iActivityManager, int i) throws RemoteException {
    }

    public static int getNotificationDefaultColor() {
        return 17170892;
    }

    public static boolean isHighPriority(String str, int i) throws RemoteException {
        return false;
    }

    public static void registerStatusBar(IStatusBarService iStatusBarService, IStatusBar iStatusBar, List<String> list, List<StatusBarIcon> list2, int[] iArr, List<IBinder> list3, Rect rect, Rect rect2) throws RemoteException {
        RegisterStatusBarResult registerStatusBar = iStatusBarService.registerStatusBar(iStatusBar);
        for (String str : registerStatusBar.mIcons.keySet()) {
            list.add(str);
            list2.add((StatusBarIcon) registerStatusBar.mIcons.get(str));
        }
        iArr[0] = registerStatusBar.mDisabledFlags1;
        iArr[1] = registerStatusBar.mSystemUiVisibility;
        iArr[2] = registerStatusBar.mMenuVisible;
        iArr[3] = registerStatusBar.mImeWindowVis;
        iArr[4] = registerStatusBar.mImeBackDisposition;
        iArr[5] = registerStatusBar.mShowImeSwitcher;
        iArr[6] = registerStatusBar.mDisabledFlags2;
        iArr[7] = registerStatusBar.mFullscreenStackSysUiVisibility;
        iArr[8] = registerStatusBar.mDockedStackSysUiVisibility;
        list3.add(registerStatusBar.mImeToken);
        rect.set(registerStatusBar.mFullscreenStackBounds);
        rect2.set(registerStatusBar.mDockedStackBounds);
    }

    public static Object getLocales(Configuration configuration) {
        return configuration.getLocales();
    }

    public static void setRecentsVisibility(Context context, boolean z) {
        try {
            WindowManagerGlobal.getWindowManagerService().setRecentsVisibility(z);
        } catch (RemoteException e) {
            Log.e("SystemServicesProxy", "Unable to reach window manager", e);
        }
    }

    public static void dismissKeyguardOnNextActivity() {
        try {
            WindowManagerGlobal.getWindowManagerService().dismissKeyguard(new IKeyguardDismissCallback.Stub() {
                public void onDismissCancelled() {
                }

                public void onDismissError() {
                }

                public void onDismissSucceeded() {
                }
            }, "");
        } catch (RemoteException e) {
            Log.w("SystemUICompat", "Error dismissing keyguard", e);
        }
    }

    public static boolean isHomeOrRecentsStack(int i, ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo == null) {
            return false;
        }
        int activityType = runningTaskInfo.configuration.windowConfiguration.getActivityType();
        return activityType == 2 || activityType == 3;
    }

    public static void cancelTaskWindowTransition(IActivityManager iActivityManager, int i) throws RemoteException {
        iActivityManager.cancelTaskWindowTransition(i);
    }

    public static void getStableInsets(Rect rect) throws RemoteException {
        WindowManagerGlobal.getWindowManagerService().getStableInsets(0, rect);
    }

    public static Rect getRecentsWindowRect(IActivityManager iActivityManager) {
        Rect rect = new Rect();
        if (iActivityManager == null) {
            return rect;
        }
        try {
            ActivityManager.StackInfo stackInfo = sITM.getStackInfo(0, 3);
            if (stackInfo == null) {
                stackInfo = sITM.getStackInfo(1, 1);
            }
            if (stackInfo != null) {
                rect.set(stackInfo.bounds);
            }
            return rect;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Throwable unused) {
        }
        return rect;
    }

    public static boolean isRecentsActivityVisible(MutableBoolean mutableBoolean, IActivityManager iActivityManager, PackageManager packageManager) {
        if (iActivityManager == null) {
            return false;
        }
        try {
            List allStackInfos = sITM.getAllStackInfos();
            ComponentName componentName = null;
            ActivityManager.StackInfo stackInfo = null;
            ActivityManager.StackInfo stackInfo2 = null;
            ActivityManager.StackInfo stackInfo3 = null;
            for (int i = 0; i < allStackInfos.size(); i++) {
                ActivityManager.StackInfo stackInfo4 = (ActivityManager.StackInfo) allStackInfos.get(i);
                WindowConfiguration windowConfiguration = stackInfo4.configuration.windowConfiguration;
                int activityType = windowConfiguration.getActivityType();
                int windowingMode = windowConfiguration.getWindowingMode();
                if (stackInfo == null && activityType == 2) {
                    stackInfo = stackInfo4;
                } else if (stackInfo2 == null && activityType == 1 && (windowingMode == 1 || windowingMode == 4)) {
                    stackInfo2 = stackInfo4;
                } else if (stackInfo3 == null && activityType == 3) {
                    stackInfo3 = stackInfo4;
                }
            }
            boolean isStackNotOccluded = isStackNotOccluded(stackInfo, stackInfo2);
            boolean isStackNotOccluded2 = isStackNotOccluded(stackInfo3, stackInfo2);
            if (mutableBoolean != null) {
                mutableBoolean.value = isStackNotOccluded;
            }
            if (stackInfo3 != null) {
                componentName = stackInfo3.topActivity;
            }
            if (!isStackNotOccluded2 || componentName == null || !componentName.getPackageName().equals("com.android.systemui") || !Recents.RECENTS_ACTIVITIES.contains(componentName.getClassName())) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isStackNotOccluded(ActivityManager.StackInfo stackInfo, ActivityManager.StackInfo stackInfo2) {
        boolean z = false;
        boolean z2 = stackInfo == null || stackInfo.visible;
        if (stackInfo2 == null || stackInfo == null) {
            return z2;
        }
        if (stackInfo2.visible && stackInfo2.position > stackInfo.position) {
            z = true;
        }
        return z2 & (!z);
    }

    private static ActivityManager.StackInfo getSplitScreenPrimaryStack(IActivityManager iActivityManager) {
        try {
            return sITM.getStackInfo(3, 0);
        } catch (RemoteException unused) {
            return null;
        }
    }

    public static boolean hasDockedTask(IActivityManager iActivityManager) {
        ActivityManager.StackInfo splitScreenPrimaryStack;
        if (iActivityManager == null || (splitScreenPrimaryStack = getSplitScreenPrimaryStack(iActivityManager)) == null) {
            return false;
        }
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        boolean z = false;
        for (int length = splitScreenPrimaryStack.taskUserIds.length - 1; length >= 0 && !z; length--) {
            int[] iArr = splitScreenPrimaryStack.taskUserIds;
            z = iArr[length] == currentUser || XSpaceUserHandle.isXSpaceUserId(iArr[length]);
        }
        return z;
    }

    public static boolean startTaskInDockedMode(Task task, int i, IActivityManager iActivityManager, Context context) {
        if (iActivityManager == null) {
            return false;
        }
        try {
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setLaunchWindowingMode(3);
            makeBasic.setSplitScreenCreateMode(i == 0 ? 0 : 1);
            sITM.startActivityFromRecents(task.key.id, makeBasic.toBundle());
            Log.i("SystemServicesProxy", "enter splitScreen mode");
            return true;
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to dock task: " + task + " with createMode: " + i, e);
            return false;
        }
    }

    public static String getConnectionSummary(Context context, CachedBluetoothDevice cachedBluetoothDevice) {
        return cachedBluetoothDevice == null ? "" : cachedBluetoothDevice.getConnectionSummary();
    }

    public static boolean setDeviceActive(CachedBluetoothDevice cachedBluetoothDevice) {
        if (cachedBluetoothDevice == null || cachedBluetoothDevice.isActiveDevice(2) || cachedBluetoothDevice.isActiveDevice(1) || cachedBluetoothDevice.isActiveDevice(21)) {
            return false;
        }
        return cachedBluetoothDevice.setActive();
    }

    public static boolean isDeviceActive(CachedBluetoothDevice cachedBluetoothDevice) {
        if (cachedBluetoothDevice == null) {
            return false;
        }
        if (cachedBluetoothDevice.isActiveDevice(2) || cachedBluetoothDevice.isActiveDevice(1) || cachedBluetoothDevice.isActiveDevice(21)) {
            return true;
        }
        return false;
    }

    public static Bitmap createHardwareBitmapFromSnapShot(ActivityManager.TaskSnapshot taskSnapshot) {
        if (taskSnapshot == null || taskSnapshot.getSnapshot() == null) {
            return null;
        }
        return Bitmap.wrapHardwareBuffer(taskSnapshot.getSnapshot(), taskSnapshot.getColorSpace());
    }

    public static Bitmap createHardwareBitmapFromGraphicBuffer(GraphicBuffer graphicBuffer) {
        if (graphicBuffer == null) {
            return null;
        }
        return Bitmap.wrapHardwareBuffer(graphicBuffer, (ColorSpace) null);
    }

    public static Bitmap drawViewIntoBitmap(int i, int i2, View view, float f, int i3) {
        Picture picture = new Picture();
        Canvas beginRecording = picture.beginRecording(i, i2);
        beginRecording.scale(f, f);
        if (i3 != 0) {
            beginRecording.drawColor(i3);
        }
        if (view != null) {
            view.draw(beginRecording);
        }
        picture.endRecording();
        return Bitmap.createBitmap(picture);
    }

    public static Bundle getInputManagerBundle(String str, String str2, int i) {
        InputMonitor monitorGestureInput = InputManager.getInstance().monitorGestureInput(str2, i);
        Bundle bundle = new Bundle();
        bundle.putParcelable(str, monitorGestureInput);
        return bundle;
    }

    public static boolean startFreeformActivity(Context context, Task task, String str) {
        if (!(task == null || task.key == null || TextUtils.isEmpty(str))) {
            try {
                ActivityOptions makeFreeformActivityOptions = makeFreeformActivityOptions(context, str);
                if (makeFreeformActivityOptions != null) {
                    SystemServicesProxy.getInstance(context).startActivityFromRecents(context, task.key, task.title, makeFreeformActivityOptions);
                    return true;
                }
            } catch (Exception e) {
                Log.e("SystemUICompat", "Failed to startFreeformActivity", e);
            }
        }
        return false;
    }

    public static ActivityOptions makeFreeformActivityOptions(Context context, String str) {
        ActivityOptions activityOptions = MiuiMultiWindowUtils.getActivityOptions(context, str, true, false);
        if (activityOptions != null) {
            return activityOptions;
        }
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchWindowingMode(5);
        makeBasic.setLaunchBounds(MiuiMultiWindowUtils.getFreeformRect(context));
        return makeBasic;
    }
}
