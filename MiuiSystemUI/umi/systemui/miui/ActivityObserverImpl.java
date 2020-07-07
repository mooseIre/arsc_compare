package com.android.systemui.miui;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.IActivityManager;
import android.app.IMiuiActivityObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityObserverImpl implements ActivityObserver, Dumpable, DeviceProvisionedController.DeviceProvisionedListener {
    /* access modifiers changed from: private */
    public final List<ActivityObserver.ActivityObserverCallback> mCallbacks = new ArrayList();
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                ActivityObserverImpl.this.handleEvaluateTopIsLauncher();
            } else if (i == 1) {
                ActivityObserverImpl activityObserverImpl = ActivityObserverImpl.this;
                activityObserverImpl.ensureLastResumedActivity(activityObserverImpl.mContext);
            }
        }
    };
    private ComponentName mLastResumedActivity = null;
    private ComponentName mLauncherComponent = null;
    private final IMiuiActivityObserver mMiuiActivityObserver = new IMiuiActivityObserver.Stub() {
        /* JADX WARNING: type inference failed for: r0v0, types: [com.android.systemui.miui.ActivityObserverImpl$2, android.os.IBinder] */
        public IBinder asBinder() {
            return this;
        }

        public void activityIdle(Intent intent) throws RemoteException {
            synchronized (ActivityObserverImpl.this.mCallbacks) {
                for (ActivityObserver.ActivityObserverCallback activityIdle : ActivityObserverImpl.this.mCallbacks) {
                    activityIdle.activityIdle(intent);
                }
            }
        }

        public void activityResumed(Intent intent) throws RemoteException {
            if (!(intent == null || intent.getComponent() == null)) {
                if (Constants.DEBUG) {
                    Log.i("ActivityObserver", "resumed " + intent.getComponent());
                }
                ActivityObserverImpl.this.mResumedActivities.add(0, intent.getComponent().clone());
                ActivityObserverImpl.this.scheduleEvaluateTopIsLauncher();
            }
            synchronized (ActivityObserverImpl.this.mCallbacks) {
                for (ActivityObserver.ActivityObserverCallback activityResumed : ActivityObserverImpl.this.mCallbacks) {
                    activityResumed.activityResumed(intent);
                }
            }
        }

        public void activityPaused(Intent intent) throws RemoteException {
            if (!(intent == null || intent.getComponent() == null)) {
                if (Constants.DEBUG) {
                    Log.e("ActivityObserver", "paused " + intent.getComponent());
                }
                ActivityObserverImpl.this.mResumedActivities.remove(intent.getComponent());
                ActivityObserverImpl.this.scheduleEnsureLastResumeActivity();
                ActivityObserverImpl.this.scheduleEvaluateTopIsLauncher();
            }
            synchronized (ActivityObserverImpl.this.mCallbacks) {
                for (ActivityObserver.ActivityObserverCallback activityPaused : ActivityObserverImpl.this.mCallbacks) {
                    activityPaused.activityPaused(intent);
                }
            }
        }

        public void activityStopped(Intent intent) throws RemoteException {
            synchronized (ActivityObserverImpl.this.mCallbacks) {
                for (ActivityObserver.ActivityObserverCallback activityStopped : ActivityObserverImpl.this.mCallbacks) {
                    activityStopped.activityStopped(intent);
                }
            }
        }

        public void activityDestroyed(Intent intent) throws RemoteException {
            synchronized (ActivityObserverImpl.this.mCallbacks) {
                for (ActivityObserver.ActivityObserverCallback activityDestroyed : ActivityObserverImpl.this.mCallbacks) {
                    activityDestroyed.activityDestroyed(intent);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final List<ComponentName> mResumedActivities = new ArrayList();
    private boolean mTopIsLauncher;

    public ActivityObserverImpl(Context context) {
        this.mContext = context;
        IActivityManager service = ActivityManagerCompat.getService();
        if (service != null) {
            try {
                service.registerActivityObserver(this.mMiuiActivityObserver, new Intent());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ensureLastResumedActivity(context);
            checkLauncherInfo();
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    ActivityObserverImpl.this.checkLauncherInfo();
                }
            }, new IntentFilter("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED"));
            ((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class)).addCallback(this);
        }
    }

    public ComponentName getTopActivity() {
        ArrayList<ComponentName> arrayList = new ArrayList<>(this.mResumedActivities);
        for (ComponentName componentName : arrayList) {
            if (!taskIsLauncher(componentName, this.mLauncherComponent)) {
                return componentName;
            }
        }
        return arrayList.isEmpty() ? this.mLastResumedActivity : (ComponentName) arrayList.get(0);
    }

    public boolean isTopActivityLauncher() {
        return this.mTopIsLauncher;
    }

    public void addCallback(ActivityObserver.ActivityObserverCallback activityObserverCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(activityObserverCallback);
        }
    }

    public void removeCallback(ActivityObserver.ActivityObserverCallback activityObserverCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(activityObserverCallback);
        }
    }

    /* access modifiers changed from: private */
    public void checkLauncherInfo() {
        ActivityInfo activityInfo;
        ResolveInfo currentLauncherInfo = getCurrentLauncherInfo(this.mContext);
        if (currentLauncherInfo == null || (activityInfo = currentLauncherInfo.activityInfo) == null) {
            this.mLauncherComponent = null;
        } else {
            this.mLauncherComponent = new ComponentName(activityInfo.packageName, currentLauncherInfo.activityInfo.name);
        }
        if (Constants.DEBUG) {
            Log.i("ActivityObserver", "Launcher is: " + this.mLauncherComponent);
        }
        scheduleEvaluateTopIsLauncher();
    }

    /* access modifiers changed from: private */
    public void scheduleEnsureLastResumeActivity() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 50);
    }

    /* access modifiers changed from: private */
    public void scheduleEvaluateTopIsLauncher() {
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessageDelayed(0, 80);
    }

    /* access modifiers changed from: private */
    public void handleEvaluateTopIsLauncher() {
        boolean z;
        ComponentName topActivity = getTopActivity();
        if (topActivity == null) {
            z = true;
            Log.wtf("ActivityObserver", "top activity is null, we assume it's launcher now.");
        } else {
            z = taskIsLauncher(topActivity, this.mLauncherComponent);
        }
        if (Constants.DEBUG) {
            Log.i("ActivityObserver", "Top app is launcher: " + z + " top=" + getTopActivity());
        }
        this.mTopIsLauncher = z;
    }

    /* access modifiers changed from: private */
    public void ensureLastResumedActivity(Context context) {
        ComponentName topActivityComponent;
        if (this.mResumedActivities.isEmpty() && (topActivityComponent = getTopActivityComponent(context)) != null) {
            this.mLastResumedActivity = topActivityComponent;
            Log.i("ActivityObserver", "filling one resumed activity " + topActivityComponent);
        }
    }

    public void onDeviceProvisionedChanged() {
        if (((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class)).isDeviceProvisioned()) {
            checkLauncherInfo();
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("ActivityObserver");
        printWriter.println("  ResumedActivities: " + Arrays.toString(this.mResumedActivities.toArray()));
        printWriter.println("  TopIsLauncher: " + this.mTopIsLauncher);
    }

    private static boolean taskIsLauncher(ComponentName componentName, ComponentName componentName2) {
        if (componentName2 == null) {
            return TextUtils.equals(componentName.getPackageName(), Constants.HOME_LAUCNHER_PACKAGE_NAME);
        }
        return componentName2.equals(componentName);
    }

    private static ComponentName getTopActivityComponent(Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            return null;
        }
        return runningTasks.get(0).topActivity;
    }

    private static ResolveInfo getCurrentLauncherInfo(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        return context.getPackageManager().resolveActivity(intent, 0);
    }
}
