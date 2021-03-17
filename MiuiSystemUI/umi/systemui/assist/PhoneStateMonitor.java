package com.android.systemui.assist;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public final class PhoneStateMonitor {
    private static final String[] DEFAULT_HOME_CHANGE_ACTIONS = {"android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED", "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED"};
    private final Context mContext;
    private ComponentName mDefaultHome;
    private boolean mLauncherShowing;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    private final StatusBarStateController mStatusBarStateController = ((StatusBarStateController) Dependency.get(StatusBarStateController.class));

    private boolean isLauncherInAllApps() {
        return false;
    }

    private boolean isLauncherInOverview() {
        return false;
    }

    PhoneStateMonitor(Context context, BroadcastDispatcher broadcastDispatcher, Optional<Lazy<StatusBar>> optional, BootCompleteCache bootCompleteCache) {
        this.mContext = context;
        this.mStatusBarOptionalLazy = optional;
        ActivityManagerWrapper instance = ActivityManagerWrapper.getInstance();
        this.mDefaultHome = getCurrentDefaultHome();
        bootCompleteCache.addListener(new BootCompleteCache.BootCompleteListener() {
            /* class com.android.systemui.assist.$$Lambda$PhoneStateMonitor$kU1yau2iyc4oGSlu9ejSJU0AW3w */

            @Override // com.android.systemui.BootCompleteCache.BootCompleteListener
            public final void onBootComplete() {
                PhoneStateMonitor.this.lambda$new$0$PhoneStateMonitor();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        for (String str : DEFAULT_HOME_CHANGE_ACTIONS) {
            intentFilter.addAction(str);
        }
        broadcastDispatcher.registerReceiver(new BroadcastReceiver() {
            /* class com.android.systemui.assist.PhoneStateMonitor.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                PhoneStateMonitor.this.mDefaultHome = PhoneStateMonitor.getCurrentDefaultHome();
            }
        }, intentFilter);
        this.mLauncherShowing = isLauncherShowing(instance.getRunningTask());
        instance.registerTaskStackListener(new TaskStackChangeListener() {
            /* class com.android.systemui.assist.PhoneStateMonitor.AnonymousClass2 */

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
                PhoneStateMonitor phoneStateMonitor = PhoneStateMonitor.this;
                phoneStateMonitor.mLauncherShowing = phoneStateMonitor.isLauncherShowing(runningTaskInfo);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PhoneStateMonitor() {
        this.mDefaultHome = getCurrentDefaultHome();
    }

    public int getPhoneState() {
        if (isShadeFullscreen()) {
            return getPhoneLockscreenState();
        }
        if (this.mLauncherShowing) {
            return getPhoneLauncherState();
        }
        return getPhoneAppState();
    }

    /* access modifiers changed from: private */
    public static ComponentName getCurrentDefaultHome() {
        ArrayList arrayList = new ArrayList();
        ComponentName homeActivities = PackageManagerWrapper.getInstance().getHomeActivities(arrayList);
        if (homeActivities != null) {
            return homeActivities;
        }
        int i = Integer.MIN_VALUE;
        Iterator it = arrayList.iterator();
        while (true) {
            ComponentName componentName = null;
            while (true) {
                if (!it.hasNext()) {
                    return componentName;
                }
                ResolveInfo resolveInfo = (ResolveInfo) it.next();
                int i2 = resolveInfo.priority;
                if (i2 > i) {
                    componentName = resolveInfo.activityInfo.getComponentName();
                    i = resolveInfo.priority;
                } else if (i2 == i) {
                }
            }
        }
    }

    private int getPhoneLockscreenState() {
        if (isDozing()) {
            return 1;
        }
        if (isBouncerShowing()) {
            return 3;
        }
        return isKeyguardLocked() ? 2 : 4;
    }

    private int getPhoneLauncherState() {
        if (isLauncherInOverview()) {
            return 6;
        }
        return isLauncherInAllApps() ? 7 : 5;
    }

    private int getPhoneAppState() {
        if (isAppImmersive()) {
            return 9;
        }
        return isAppFullscreen() ? 10 : 8;
    }

    private boolean isShadeFullscreen() {
        int state = this.mStatusBarStateController.getState();
        return state == 1 || state == 2;
    }

    private boolean isDozing() {
        return this.mStatusBarStateController.isDozing();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLauncherShowing(ActivityManager.RunningTaskInfo runningTaskInfo) {
        ComponentName componentName;
        if (runningTaskInfo == null || (componentName = runningTaskInfo.topActivity) == null) {
            return false;
        }
        return componentName.equals(this.mDefaultHome);
    }

    private boolean isAppImmersive() {
        return this.mStatusBarOptionalLazy.get().get().inImmersiveMode();
    }

    private boolean isAppFullscreen() {
        return this.mStatusBarOptionalLazy.get().get().inFullscreenMode();
    }

    private boolean isBouncerShowing() {
        return this.mStatusBarOptionalLazy.map($$Lambda$PhoneStateMonitor$m3mFsd47OeaWHKnwhEEoNbkyA.INSTANCE).orElse((U) Boolean.FALSE).booleanValue();
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
        return keyguardManager != null && keyguardManager.isKeyguardLocked();
    }
}
