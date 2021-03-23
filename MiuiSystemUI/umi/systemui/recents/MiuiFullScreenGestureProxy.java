package com.android.systemui.recents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.systemui.statusbar.CommandQueue;
import java.util.ArrayList;

public class MiuiFullScreenGestureProxy implements CommandQueue.Callbacks {
    public static final ArrayList<String> sMiuiHomePkgNameList;
    private CommandQueue mCommandQueue;
    private Context mContext;
    private LauncherPackageMonitor mPackageMonitor;
    private boolean mUseMiuiHomeAsDefaultHome;
    private final BroadcastReceiver mUserPreferenceChangeReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.recents.MiuiFullScreenGestureProxy.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            MiuiFullScreenGestureProxy miuiFullScreenGestureProxy = MiuiFullScreenGestureProxy.this;
            boolean useMiuiHomeAsDefaultHome = miuiFullScreenGestureProxy.useMiuiHomeAsDefaultHome(miuiFullScreenGestureProxy.mContext);
            Log.w("MiuiFullScreenGestureProxy", "mUserPreferenceChangeReceiver   useMiuiHomeAsDefaultHome=" + useMiuiHomeAsDefaultHome);
            if (MiuiFullScreenGestureProxy.this.mUseMiuiHomeAsDefaultHome != useMiuiHomeAsDefaultHome) {
                MiuiFullScreenGestureProxy.this.updateDefaultHome(useMiuiHomeAsDefaultHome);
            }
        }
    };

    static {
        ArrayList<String> arrayList = new ArrayList<>();
        sMiuiHomePkgNameList = arrayList;
        arrayList.add("com.miui.home");
        sMiuiHomePkgNameList.add("com.mi.android.globallauncher");
    }

    public MiuiFullScreenGestureProxy(Context context, CommandQueue commandQueue) {
        this.mContext = context;
        this.mCommandQueue = commandQueue;
    }

    public void start() {
        updateDefaultHome(useMiuiHomeAsDefaultHome(this.mContext));
        LauncherPackageMonitor launcherPackageMonitor = new LauncherPackageMonitor();
        this.mPackageMonitor = launcherPackageMonitor;
        Context context = this.mContext;
        launcherPackageMonitor.register(context, context.getMainLooper(), UserHandle.ALL, true);
        this.mContext.registerReceiver(this.mUserPreferenceChangeReceiver, new IntentFilter("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED"));
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean useMiuiHomeAsDefaultHome(Context context) {
        ActivityInfo activityInfo;
        String str;
        ResolveInfo resolveActivity = context.getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 786432);
        if (resolveActivity == null || (activityInfo = resolveActivity.activityInfo) == null || (str = activityInfo.packageName) == null || sMiuiHomePkgNameList.contains(str)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDefaultHome(boolean z) {
        Log.w("MiuiFullScreenGestureProxy", "updateDefaultHome   useMiuiHomeAsDefaultHome=" + z);
        this.mUseMiuiHomeAsDefaultHome = z;
        boolean z2 = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
        if (!this.mUseMiuiHomeAsDefaultHome && z2) {
            MiuiSettings.Global.putBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar", false);
        }
    }

    private class LauncherPackageMonitor extends PackageMonitor {
        private LauncherPackageMonitor() {
        }

        public boolean onPackageChanged(String str, int i, String[] strArr) {
            onPackageModified(str);
            return true;
        }

        public void onPackageAdded(String str, int i) {
            onPackageModified(str);
        }

        public void onPackageRemoved(String str, int i) {
            onPackageModified(str);
        }

        public void onPackageModified(String str) {
            Log.w("MiuiFullScreenGestureProxy", "packageMonitor   onPackageModified  packageName=" + str);
            if (str != null && MiuiFullScreenGestureProxy.sMiuiHomePkgNameList.contains(str)) {
                MiuiFullScreenGestureProxy miuiFullScreenGestureProxy = MiuiFullScreenGestureProxy.this;
                boolean useMiuiHomeAsDefaultHome = miuiFullScreenGestureProxy.useMiuiHomeAsDefaultHome(miuiFullScreenGestureProxy.mContext);
                if (MiuiFullScreenGestureProxy.this.mUseMiuiHomeAsDefaultHome != useMiuiHomeAsDefaultHome) {
                    MiuiFullScreenGestureProxy.this.updateDefaultHome(useMiuiHomeAsDefaultHome);
                }
            }
        }
    }
}
