package com.android.systemui.statusbar.notification;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.Pair;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.InstantAppNotifier;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.NotificationChannels;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class InstantAppNotifier extends SystemUI implements CommandQueue.Callbacks, KeyguardStateController.Callback {
    private final CommandQueue mCommandQueue;
    private final ArraySet<Pair<String, Integer>> mCurrentNotifs = new ArraySet<>();
    private final Divider mDivider;
    private boolean mDockedStackExists;
    private final Handler mHandler = new Handler();
    private KeyguardStateController mKeyguardStateController;
    private final Executor mUiBgExecutor;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new SynchronousUserSwitchObserver() {
        /* class com.android.systemui.statusbar.notification.InstantAppNotifier.AnonymousClass1 */

        public void onUserSwitching(int i) throws RemoteException {
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            InstantAppNotifier.this.mHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.notification.$$Lambda$InstantAppNotifier$1$2maFdVbSGSmI45ss9sfIaHkOm8U */

                public final void run() {
                    InstantAppNotifier.AnonymousClass1.this.lambda$onUserSwitchComplete$0$InstantAppNotifier$1();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onUserSwitchComplete$0 */
        public /* synthetic */ void lambda$onUserSwitchComplete$0$InstantAppNotifier$1() {
            InstantAppNotifier.this.updateForegroundInstantApps();
        }
    };

    public InstantAppNotifier(Context context, CommandQueue commandQueue, Executor executor, Divider divider) {
        super(context);
        this.mDivider = divider;
        this.mCommandQueue = commandQueue;
        this.mUiBgExecutor = executor;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        try {
            ActivityManager.getService().registerUserSwitchObserver(this.mUserSwitchListener, "InstantAppNotifier");
        } catch (RemoteException unused) {
        }
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mKeyguardStateController.addCallback(this);
        this.mDivider.registerInSplitScreenListener(new Consumer() {
            /* class com.android.systemui.statusbar.notification.$$Lambda$InstantAppNotifier$QG8UJHrN7yIZpZAc2flFn_csdY */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                InstantAppNotifier.this.lambda$start$0$InstantAppNotifier((Boolean) obj);
            }
        });
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (statusBarNotification.getId() == 7) {
                notificationManager.cancel(statusBarNotification.getTag(), statusBarNotification.getId());
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$start$0 */
    public /* synthetic */ void lambda$start$0$InstantAppNotifier(Boolean bool) {
        this.mDockedStackExists = bool.booleanValue();
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        if (this.mContext.getDisplayId() == i) {
            updateForegroundInstantApps();
        }
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        updateForegroundInstantApps();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void preloadRecentApps() {
        updateForegroundInstantApps();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForegroundInstantApps() {
        this.mUiBgExecutor.execute(new Runnable((NotificationManager) this.mContext.getSystemService(NotificationManager.class), AppGlobals.getPackageManager()) {
            /* class com.android.systemui.statusbar.notification.$$Lambda$InstantAppNotifier$jG9EvYNY9H1cwQp_C5lfrjo3s */
            public final /* synthetic */ NotificationManager f$1;
            public final /* synthetic */ IPackageManager f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                InstantAppNotifier.this.lambda$updateForegroundInstantApps$2$InstantAppNotifier(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateForegroundInstantApps$2 */
    public /* synthetic */ void lambda$updateForegroundInstantApps$2$InstantAppNotifier(NotificationManager notificationManager, IPackageManager iPackageManager) {
        int windowingMode;
        ArraySet<Pair<String, Integer>> arraySet = new ArraySet<>(this.mCurrentNotifs);
        try {
            ActivityManager.StackInfo focusedStackInfo = ActivityTaskManager.getService().getFocusedStackInfo();
            if (focusedStackInfo != null && ((windowingMode = focusedStackInfo.configuration.windowConfiguration.getWindowingMode()) == 1 || windowingMode == 4 || windowingMode == 5)) {
                checkAndPostForStack(focusedStackInfo, arraySet, notificationManager, iPackageManager);
            }
            if (this.mDockedStackExists) {
                checkAndPostForPrimaryScreen(arraySet, notificationManager, iPackageManager);
            }
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        arraySet.forEach(new Consumer(notificationManager) {
            /* class com.android.systemui.statusbar.notification.$$Lambda$InstantAppNotifier$Z2PXeDEr90IgG8nCbYZnqFPYPNc */
            public final /* synthetic */ NotificationManager f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                InstantAppNotifier.this.lambda$updateForegroundInstantApps$1$InstantAppNotifier(this.f$1, (Pair) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateForegroundInstantApps$1 */
    public /* synthetic */ void lambda$updateForegroundInstantApps$1$InstantAppNotifier(NotificationManager notificationManager, Pair pair) {
        this.mCurrentNotifs.remove(pair);
        notificationManager.cancelAsUser((String) pair.first, 7, new UserHandle(((Integer) pair.second).intValue()));
    }

    private void checkAndPostForPrimaryScreen(ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        try {
            checkAndPostForStack(ActivityTaskManager.getService().getStackInfo(3, 0), arraySet, notificationManager, iPackageManager);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    private void checkAndPostForStack(ActivityManager.StackInfo stackInfo, ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        if (stackInfo != null) {
            try {
                if (stackInfo.topActivity != null) {
                    String packageName = stackInfo.topActivity.getPackageName();
                    if (!arraySet.remove(new Pair(packageName, Integer.valueOf(stackInfo.userId)))) {
                        ApplicationInfo applicationInfo = iPackageManager.getApplicationInfo(packageName, 8192, stackInfo.userId);
                        if (applicationInfo.isInstantApp()) {
                            postInstantAppNotif(packageName, stackInfo.userId, applicationInfo, notificationManager, stackInfo.taskIds[stackInfo.taskIds.length - 1]);
                        }
                    }
                }
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    }

    private void postInstantAppNotif(String str, int i, ApplicationInfo applicationInfo, NotificationManager notificationManager, int i2) {
        int i3;
        Notification.Action action;
        PendingIntent pendingIntent;
        String str2;
        int i4;
        PendingIntent pendingIntent2;
        Notification.Builder builder;
        ComponentName componentName;
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", this.mContext.getString(C0021R$string.instant_apps));
        this.mCurrentNotifs.add(new Pair<>(str, Integer.valueOf(i)));
        String string = this.mContext.getString(C0021R$string.instant_apps_help_url);
        boolean z = !string.isEmpty();
        Context context = this.mContext;
        if (z) {
            i3 = C0021R$string.instant_apps_message_with_help;
        } else {
            i3 = C0021R$string.instant_apps_message;
        }
        String string2 = context.getString(i3);
        UserHandle of = UserHandle.of(i);
        Notification.Action build = new Notification.Action.Builder((Icon) null, this.mContext.getString(C0021R$string.app_info), PendingIntent.getActivityAsUser(this.mContext, 0, new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", str, null)), 67108864, null, of)).build();
        if (z) {
            str2 = "android.intent.action.VIEW";
            action = build;
            pendingIntent = PendingIntent.getActivityAsUser(this.mContext, 0, new Intent("android.intent.action.VIEW").setData(Uri.parse(string)), 67108864, null, of);
            i4 = i2;
        } else {
            str2 = "android.intent.action.VIEW";
            action = build;
            i4 = i2;
            pendingIntent = null;
        }
        Intent taskIntent = getTaskIntent(i4, i);
        Notification.Builder builder2 = new Notification.Builder(this.mContext, NotificationChannels.GENERAL);
        if (taskIntent == null || !taskIntent.isWebIntent()) {
            builder = builder2;
            pendingIntent2 = pendingIntent;
        } else {
            taskIntent.setComponent(null).setPackage(null).addFlags(512).addFlags(268435456);
            pendingIntent2 = pendingIntent;
            PendingIntent activityAsUser = PendingIntent.getActivityAsUser(this.mContext, 0, taskIntent, 67108864, null, of);
            try {
                componentName = AppGlobals.getPackageManager().getInstantAppInstallerComponent();
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
                componentName = null;
            }
            Intent addCategory = new Intent().setComponent(componentName).setAction(str2).addCategory("android.intent.category.BROWSABLE");
            builder = builder2;
            builder.addAction(new Notification.Action.Builder((Icon) null, this.mContext.getString(C0021R$string.go_to_web), PendingIntent.getActivityAsUser(this.mContext, 0, addCategory.addCategory("unique:" + System.currentTimeMillis()).putExtra("android.intent.extra.PACKAGE_NAME", applicationInfo.packageName).putExtra("android.intent.extra.VERSION_CODE", applicationInfo.versionCode & Integer.MAX_VALUE).putExtra("android.intent.extra.LONG_VERSION_CODE", applicationInfo.longVersionCode).putExtra("android.intent.extra.INSTANT_APP_FAILURE", activityAsUser), 67108864, null, of)).build());
        }
        Notification.Builder color = builder.addExtras(bundle).addAction(action).setContentIntent(pendingIntent2).setColor(this.mContext.getColor(C0011R$color.instant_apps_color));
        Context context2 = this.mContext;
        notificationManager.notifyAsUser(str, 7, color.setContentTitle(context2.getString(C0021R$string.instant_apps_title, applicationInfo.loadLabel(context2.getPackageManager()))).setLargeIcon(Icon.createWithResource(str, applicationInfo.icon)).setSmallIcon(Icon.createWithResource(this.mContext.getPackageName(), C0013R$drawable.instant_icon)).setContentText(string2).setStyle(new Notification.BigTextStyle().bigText(string2)).setOngoing(true).build(), new UserHandle(i));
    }

    private Intent getTaskIntent(int i, int i2) {
        try {
            List list = ActivityTaskManager.getService().getRecentTasks(5, 0, i2).getList();
            for (int i3 = 0; i3 < list.size(); i3++) {
                if (((ActivityManager.RecentTaskInfo) list.get(i3)).id == i) {
                    return ((ActivityManager.RecentTaskInfo) list.get(i3)).baseIntent;
                }
            }
            return null;
        } catch (RemoteException unused) {
            return null;
        }
    }
}
