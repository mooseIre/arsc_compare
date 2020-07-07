package com.android.systemui.recents.misc;

import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.Constants;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.xiaomi.stat.MiStatParams;
import java.util.ArrayList;

public class RecentsPushEventHelper {
    /* access modifiers changed from: private */
    public static boolean DEBUG = Constants.DEBUG;
    /* access modifiers changed from: private */
    public static String mLastBottomStackPkg;
    /* access modifiers changed from: private */
    public static String mLastTopStackPkg;

    /* access modifiers changed from: private */
    public static void sendEvent(final String str, final MiStatParams miStatParams) {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                if (RecentsPushEventHelper.DEBUG) {
                    Log.d("RecentsPushEventHelper", "trackEvent  eventName=" + str + " params=" + miStatParams.toJsonString());
                }
                AnalyticsWrapper.trackEvent(str, miStatParams);
            }
        });
    }

    public static void sendTaskStackChangedEvent() {
        BackgroundThread.getHandler().post(new Runnable() {
            /* JADX WARNING: Removed duplicated region for block: B:40:0x0083  */
            /* JADX WARNING: Removed duplicated region for block: B:43:0x009d  */
            /* JADX WARNING: Removed duplicated region for block: B:46:? A[RETURN, SYNTHETIC] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                    r6 = this;
                    com.android.systemui.recents.misc.SystemServicesProxy r6 = com.android.systemui.recents.Recents.getSystemServices()
                    boolean r6 = r6.hasDockedTask()
                    if (r6 == 0) goto L_0x00b6
                    r6 = -1
                    r0 = 3
                    r1 = 0
                    r2 = 0
                    android.app.ActivityManager$StackInfo r0 = android.app.ActivityManagerCompat.getStackInfo(r0, r0, r1)     // Catch:{ Exception -> 0x006c }
                    if (r0 == 0) goto L_0x0017
                    android.content.ComponentName r3 = r0.topActivity     // Catch:{ Exception -> 0x006c }
                    goto L_0x0018
                L_0x0017:
                    r3 = r2
                L_0x0018:
                    if (r3 == 0) goto L_0x0023
                    boolean r0 = r0.visible     // Catch:{ Exception -> 0x006c }
                    if (r0 == 0) goto L_0x0023
                    java.lang.String r0 = r3.getPackageName()     // Catch:{ Exception -> 0x006c }
                    goto L_0x0024
                L_0x0023:
                    r0 = r2
                L_0x0024:
                    r3 = 1
                    android.app.ActivityManager$StackInfo r3 = android.app.ActivityManagerCompat.getStackInfo(r3, r3, r1)     // Catch:{ Exception -> 0x0066 }
                    if (r3 == 0) goto L_0x002e
                    android.content.ComponentName r4 = r3.topActivity     // Catch:{ Exception -> 0x0066 }
                    goto L_0x002f
                L_0x002e:
                    r4 = r2
                L_0x002f:
                    if (r4 == 0) goto L_0x0046
                    boolean r3 = r3.visible     // Catch:{ Exception -> 0x0066 }
                    if (r3 == 0) goto L_0x0046
                    java.lang.String r2 = r4.getPackageName()     // Catch:{ Exception -> 0x0066 }
                    com.android.systemui.recents.misc.SystemServicesProxy r1 = com.android.systemui.recents.Recents.getSystemServices()     // Catch:{ Exception -> 0x0066 }
                    android.content.pm.ActivityInfo r1 = r1.getActivityInfo(r4)     // Catch:{ Exception -> 0x0066 }
                    if (r1 == 0) goto L_0x0077
                    int r6 = r1.resizeMode     // Catch:{ Exception -> 0x0066 }
                    goto L_0x0077
                L_0x0046:
                    r3 = 2
                    android.app.ActivityManager$StackInfo r1 = android.app.ActivityManagerCompat.getStackInfo(r1, r1, r3)     // Catch:{ Exception -> 0x0066 }
                    if (r1 == 0) goto L_0x0050
                    android.content.ComponentName r3 = r1.topActivity     // Catch:{ Exception -> 0x0066 }
                    goto L_0x0051
                L_0x0050:
                    r3 = r2
                L_0x0051:
                    if (r3 == 0) goto L_0x0077
                    boolean r1 = r1.visible     // Catch:{ Exception -> 0x0066 }
                    if (r1 == 0) goto L_0x0077
                    java.lang.String r2 = r3.getPackageName()     // Catch:{ Exception -> 0x0066 }
                    com.android.systemui.recents.misc.SystemServicesProxy r1 = com.android.systemui.recents.Recents.getSystemServices()     // Catch:{ Exception -> 0x0066 }
                    android.content.pm.ActivityInfo r1 = r1.getActivityInfo(r3)     // Catch:{ Exception -> 0x0066 }
                    int r6 = r1.resizeMode     // Catch:{ Exception -> 0x0066 }
                    goto L_0x0077
                L_0x0066:
                    r1 = move-exception
                    r5 = r2
                    r2 = r0
                    r0 = r1
                    r1 = r5
                    goto L_0x006e
                L_0x006c:
                    r0 = move-exception
                    r1 = r2
                L_0x006e:
                    java.lang.String r3 = "RecentsPushEventHelper"
                    java.lang.String r4 = "sendTaskStackChangedEvent error"
                    android.util.Log.e(r3, r4, r0)
                    r0 = r2
                    r2 = r1
                L_0x0077:
                    java.lang.String r1 = com.android.systemui.recents.misc.RecentsPushEventHelper.mLastTopStackPkg
                    boolean r1 = android.text.TextUtils.equals(r1, r0)
                    java.lang.String r3 = "multi_window_packageName"
                    if (r1 != 0) goto L_0x0093
                    java.lang.String unused = com.android.systemui.recents.misc.RecentsPushEventHelper.mLastTopStackPkg = r0
                    com.xiaomi.stat.MiStatParams r1 = new com.xiaomi.stat.MiStatParams
                    r1.<init>()
                    r1.putString(r3, r0)
                    java.lang.String r0 = "multi_window_topTaskChanged"
                    com.android.systemui.recents.misc.RecentsPushEventHelper.sendEvent(r0, r1)
                L_0x0093:
                    java.lang.String r0 = com.android.systemui.recents.misc.RecentsPushEventHelper.mLastBottomStackPkg
                    boolean r0 = android.text.TextUtils.equals(r0, r2)
                    if (r0 != 0) goto L_0x00b6
                    java.lang.String unused = com.android.systemui.recents.misc.RecentsPushEventHelper.mLastBottomStackPkg = r2
                    com.xiaomi.stat.MiStatParams r0 = new com.xiaomi.stat.MiStatParams
                    r0.<init>()
                    r0.putString(r3, r2)
                    java.lang.String r6 = android.content.pm.ActivityInfo.resizeModeToString(r6)
                    java.lang.String r1 = "multi_window_resizeMode"
                    r0.putString(r1, r6)
                    java.lang.String r6 = "multi_window_bottomTaskChanged"
                    com.android.systemui.recents.misc.RecentsPushEventHelper.sendEvent(r6, r0)
                L_0x00b6:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.misc.RecentsPushEventHelper.AnonymousClass2.run():void");
            }
        });
    }

    public static void sendEnterRecentsEvent(TaskStack taskStack, String str, String str2) {
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        int i = 0;
        for (int i2 = 0; i2 < stackTasks.size(); i2++) {
            if (stackTasks.get(i2).isLocked) {
                i++;
            }
        }
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("recents_taskCount", taskStack.getTaskCount());
        miStatParams.putInt("recents_taskLockedCount", i);
        miStatParams.putString("recents_enterType", str);
        miStatParams.putString("recents_screenOrientation", str2);
        sendEvent("recents_enterRecents", miStatParams);
    }

    public static void sendHideRecentsEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_hideType", str);
        sendEvent("recents_hideRecents", miStatParams);
    }

    public static void sendSwitchAppEvent(String str, int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_switchType", str);
        miStatParams.putInt("recents_taskIndex", i);
        sendEvent("recents_switchApp", miStatParams);
    }

    public static void sendRemoveTaskEvent(String str, int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_packageName", str);
        miStatParams.putInt("recents_taskIndex", i);
        sendEvent("recents_removeTask", miStatParams);
    }

    public static void sendLongCLickTaskEvent(String str, int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_packageName", str);
        miStatParams.putInt("recents_taskIndex", i);
        sendEvent("recents_longClickTask", miStatParams);
    }

    public static void sendLockTaskEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_packageName", str);
        sendEvent("recents_longClickTask_lockTask", miStatParams);
    }

    public static void sendUnlockTaskEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_packageName", str);
        sendEvent("recents_longClickTask_unLockTask", miStatParams);
    }

    public static void sendShowAppInfoEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_packageName", str);
        sendEvent("recents_longClickTask_showAppInfo", miStatParams);
    }

    public static void sendClickMultiWindowMenuEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_packageName", str);
        sendEvent("recents_longClickTask_clickMultiWindowMenu", miStatParams);
    }

    public static void sendOneKeyCleanEvent(long j, long j2, long j3) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("recents_freeMemoryBeforeClean", ((int) j) / 1024);
        miStatParams.putInt("recents_freeMemoryAfterClean", ((int) j2) / 1024);
        miStatParams.putInt("recents_cleanedMemory", (int) ((j / 1024) - (j2 / 1024)));
        miStatParams.putInt("recents_totalMemory", ((int) j3) / 1024);
        sendEvent("recents_oneKeyCleanStart", miStatParams);
    }

    public static void sendShowRecommendCardEvent(boolean z) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_isShowCard", z ? "show" : "hide");
        sendEvent("recents_cardShow", miStatParams);
    }

    public static void sendClickRecommendCardEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("recents_cardClickType", str);
        sendEvent("recents_cardClick", miStatParams);
    }

    public static void sendEnterMultiWindowEvent(String str, String str2) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("multi_window_enterType", str);
        miStatParams.putString("multi_window_packageName", str2);
        sendEvent("multi_window_enterMultiWindow", miStatParams);
    }

    public static void sendExitMultiWindowEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("multi_window_exitType", str);
        sendEvent("multi_window_exitMultiWindow", miStatParams);
    }

    public static void sendEnterMultiWindowFailedEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("multi_window_componentName", str);
        sendEvent("multi_window_tryEnterMultiWindowFailed", miStatParams);
    }

    public static void sendResizeStackEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("multi_window_position", str);
        sendEvent("multi_window_resizeStack", miStatParams);
    }

    public static void sendClickStatusBarToReturnMultiWindowEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("multi_window_componentName", str);
        sendEvent("multi_window_clickStatusBarToReturnMultiWindow", miStatParams);
    }
}
