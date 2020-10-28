package com.android.systemui.recents.misc;

import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.analytics.StatManager;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import java.util.ArrayList;
import java.util.HashMap;

public class RecentsPushEventHelper {
    /* access modifiers changed from: private */
    public static boolean DEBUG = Constants.DEBUG;
    /* access modifiers changed from: private */
    public static String mLastBottomStackPkg;
    /* access modifiers changed from: private */
    public static String mLastTopStackPkg;

    /* access modifiers changed from: private */
    public static void sendEvent(final String str, final HashMap<String, Object> hashMap) {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                if (RecentsPushEventHelper.DEBUG) {
                    Log.d("RecentsPushEventHelper", "trackEvent  eventName=" + str + " params=" + hashMap.toString());
                }
                StatManager.trackGenericEvent(str, hashMap);
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
                    java.util.HashMap r1 = new java.util.HashMap
                    r1.<init>()
                    r1.put(r3, r0)
                    java.lang.String r0 = "multi_window_topTaskChanged"
                    com.android.systemui.recents.misc.RecentsPushEventHelper.sendEvent(r0, r1)
                L_0x0093:
                    java.lang.String r0 = com.android.systemui.recents.misc.RecentsPushEventHelper.mLastBottomStackPkg
                    boolean r0 = android.text.TextUtils.equals(r0, r2)
                    if (r0 != 0) goto L_0x00b6
                    java.lang.String unused = com.android.systemui.recents.misc.RecentsPushEventHelper.mLastBottomStackPkg = r2
                    java.util.HashMap r0 = new java.util.HashMap
                    r0.<init>()
                    r0.put(r3, r2)
                    java.lang.String r6 = android.content.pm.ActivityInfo.resizeModeToString(r6)
                    java.lang.String r1 = "multi_window_resizeMode"
                    r0.put(r1, r6)
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
        HashMap hashMap = new HashMap();
        hashMap.put("recents_taskCount", Integer.valueOf(taskStack.getTaskCount()));
        hashMap.put("recents_taskLockedCount", Integer.valueOf(i));
        hashMap.put("recents_enterType", str);
        hashMap.put("recents_screenOrientation", str2);
        sendEvent("recents_enterRecents", hashMap);
    }

    public static void sendHideRecentsEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_hideType", str);
        sendEvent("recents_hideRecents", hashMap);
    }

    public static void sendSwitchAppEvent(String str, int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_switchType", str);
        hashMap.put("recents_taskIndex", Integer.valueOf(i));
        sendEvent("recents_switchApp", hashMap);
    }

    public static void sendRemoveTaskEvent(String str, int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_packageName", str);
        hashMap.put("recents_taskIndex", Integer.valueOf(i));
        sendEvent("recents_removeTask", hashMap);
    }

    public static void sendLongCLickTaskEvent(String str, int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_packageName", str);
        hashMap.put("recents_taskIndex", Integer.valueOf(i));
        sendEvent("recents_longClickTask", hashMap);
    }

    public static void sendLockTaskEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_packageName", str);
        sendEvent("recents_longClickTask_lockTask", hashMap);
    }

    public static void sendUnlockTaskEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_packageName", str);
        sendEvent("recents_longClickTask_unLockTask", hashMap);
    }

    public static void sendShowAppInfoEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_packageName", str);
        sendEvent("recents_longClickTask_showAppInfo", hashMap);
    }

    public static void sendClickMultiWindowMenuEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_packageName", str);
        sendEvent("recents_longClickTask_clickMultiWindowMenu", hashMap);
    }

    public static void sendOneKeyCleanEvent(long j, long j2, long j3) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_freeMemoryBeforeClean", Integer.valueOf(((int) j) / 1024));
        hashMap.put("recents_freeMemoryAfterClean", Integer.valueOf(((int) j2) / 1024));
        hashMap.put("recents_cleanedMemory", Integer.valueOf((int) ((j / 1024) - (j2 / 1024))));
        hashMap.put("recents_totalMemory", Integer.valueOf(((int) j3) / 1024));
        sendEvent("recents_oneKeyCleanStart", hashMap);
    }

    public static void sendShowRecommendCardEvent(boolean z) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_isShowCard", z ? "show" : "hide");
        sendEvent("recents_cardShow", hashMap);
    }

    public static void sendClickRecommendCardEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("recents_cardClickType", str);
        sendEvent("recents_cardClick", hashMap);
    }

    public static void sendEnterMultiWindowEvent(String str, String str2) {
        HashMap hashMap = new HashMap();
        hashMap.put("multi_window_enterType", str);
        hashMap.put("multi_window_packageName", str2);
        sendEvent("multi_window_enterMultiWindow", hashMap);
    }

    public static void sendEnterMultiWindowFailedEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("multi_window_componentName", str);
        sendEvent("multi_window_tryEnterMultiWindowFailed", hashMap);
    }

    public static void sendClickStatusBarToReturnMultiWindowEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("multi_window_componentName", str);
        sendEvent("multi_window_clickStatusBarToReturnMultiWindow", hashMap);
    }
}
