package com.android.systemui.recents;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.DefaultHomeChangedEvent;
import com.android.systemui.recents.events.activity.RecentsWithinLauncherChangedEvent;
import com.android.systemui.recents.events.activity.SuperPowerModeChangedEvent;
import com.android.systemui.recents.events.activity.UseFsGestureVersionThreeChangedEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.statusbar.CommandQueue;
import java.util.HashSet;
import java.util.Set;

public class Recents extends SystemUI implements RecentsComponent, CommandQueue.Callbacks {
    public static final Set<String> RECENTS_ACTIVITIES = new HashSet();
    private static RecentsConfiguration sConfiguration;
    private static RecentsDebugFlags sDebugFlags;
    /* access modifiers changed from: private */
    public static SystemServicesProxy sSystemServicesProxy;
    private static RecentsTaskLoader sTaskLoader;
    private boolean mIsLowMemoryDevice;
    /* access modifiers changed from: private */
    public boolean mIsRecentsWithinLauncher;
    private LauncherPackageMonitor mPackageMonitor;
    private RecentsImplementation mRecentsImplementation;
    private ContentObserver mSuperSavePowerObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean z) {
            RecentsEventBus.getDefault().send(new SuperPowerModeChangedEvent(MiuiSettings.System.isSuperSaveModeOpen(Recents.this.mContext, UserHandle.myUserId())));
        }
    };
    private boolean mUseFsGestureVersionThree;
    /* access modifiers changed from: private */
    public boolean mUseMiuiHomeAsDefaultHome;
    private final BroadcastReceiver mUserPreferenceChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean useMiuiHomeAsDefaultHome = Recents.sSystemServicesProxy.useMiuiHomeAsDefaultHome(Recents.this.mContext);
            if (Recents.this.mUseMiuiHomeAsDefaultHome != useMiuiHomeAsDefaultHome) {
                Recents.this.updateDefaultHome(useMiuiHomeAsDefaultHome);
                Recents.this.updateRecentsImplementation();
            }
        }
    };

    private static String getMetricsCounterForResizeMode(int i) {
        return (i == 2 || i == 3) ? "window_enter_supported" : i != 4 ? "window_enter_incompatible" : "window_enter_unsupported";
    }

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    static {
        RECENTS_ACTIVITIES.add("com.android.systemui.recents.RecentsActivity");
    }

    public IBinder getSystemUserCallbacks() {
        RecentsImplementation recentsImplementation = this.mRecentsImplementation;
        if (recentsImplementation instanceof LegacyRecentsImpl) {
            return ((LegacyRecentsImpl) recentsImplementation).getSystemUserCallbacks();
        }
        return null;
    }

    public static RecentsTaskLoader getTaskLoader() {
        return sTaskLoader;
    }

    public static SystemServicesProxy getSystemServices() {
        return sSystemServicesProxy;
    }

    public static RecentsConfiguration getConfiguration() {
        return sConfiguration;
    }

    public static RecentsDebugFlags getDebugFlags() {
        return sDebugFlags;
    }

    public RecentsImpl getRecentsImpl() {
        RecentsImplementation recentsImplementation = this.mRecentsImplementation;
        if (recentsImplementation instanceof LegacyRecentsImpl) {
            return ((LegacyRecentsImpl) recentsImplementation).getRecentsImpl();
        }
        return null;
    }

    public void start() {
        sDebugFlags = new RecentsDebugFlags(this.mContext);
        sSystemServicesProxy = SystemServicesProxy.getInstance(this.mContext);
        sTaskLoader = new RecentsTaskLoader(this.mContext);
        sConfiguration = new RecentsConfiguration(this.mContext);
        updateDefaultHome(sSystemServicesProxy.useMiuiHomeAsDefaultHome(this.mContext));
        this.mIsRecentsWithinLauncher = sSystemServicesProxy.isRecentsWithinLauncher(this.mContext);
        this.mIsLowMemoryDevice = Utilities.isLowMemoryDevice();
        this.mUseFsGestureVersionThree = useFsGestureVersionThree();
        RecentsEventBus.getDefault().send(new UseFsGestureVersionThreeChangedEvent(this.mUseFsGestureVersionThree));
        if (this.mUseFsGestureVersionThree) {
            this.mRecentsImplementation = new OverviewProxyRecentsImpl();
        } else {
            this.mRecentsImplementation = new LegacyRecentsImpl(this.mContext);
        }
        this.mRecentsImplementation.onStart(this.mContext, this);
        if (Utilities.isAndroidQorNewer()) {
            this.mPackageMonitor = new LauncherPackageMonitor();
            LauncherPackageMonitor launcherPackageMonitor = this.mPackageMonitor;
            Context context = this.mContext;
            launcherPackageMonitor.register(context, context.getMainLooper(), UserHandle.ALL, true);
            this.mContext.registerReceiver(this.mUserPreferenceChangeReceiver, new IntentFilter("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED"));
        }
        registerSuperSavePowerObserver();
        RecentsEventBus.getDefault().register(this, 1);
        RecentsEventBus.getDefault().register(sSystemServicesProxy, 1);
        RecentsEventBus.getDefault().register(sTaskLoader, 1);
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            ((CommandQueue) getComponent(CommandQueue.class)).addCallbacks(this);
        }
        putComponent(Recents.class, this);
    }

    private void registerSuperSavePowerObserver() {
        if (UserHandle.myUserId() == 0) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("power_supersave_mode_open"), false, this.mSuperSavePowerObserver, UserHandle.myUserId());
            this.mSuperSavePowerObserver.onChange(false);
        }
    }

    public boolean useFsGestureVersionThree() {
        return (Utilities.isAndroidQorNewer() && this.mUseMiuiHomeAsDefaultHome && this.mIsRecentsWithinLauncher && !this.mIsLowMemoryDevice) || (Utilities.isAndroidRorNewer() && this.mUseMiuiHomeAsDefaultHome);
    }

    /* access modifiers changed from: private */
    public void updateDefaultHome(boolean z) {
        this.mUseMiuiHomeAsDefaultHome = z;
        RecentsEventBus.getDefault().send(new DefaultHomeChangedEvent(this.mUseMiuiHomeAsDefaultHome));
        if (Utilities.isAndroidRorNewer()) {
            boolean z2 = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
            if (!this.mUseMiuiHomeAsDefaultHome && z2) {
                MiuiSettings.Global.putBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar", false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateRecentsImplementation() {
        boolean useFsGestureVersionThree = useFsGestureVersionThree();
        RecentsEventBus.getDefault().send(new UseFsGestureVersionThreeChangedEvent(useFsGestureVersionThree));
        if (this.mUseFsGestureVersionThree != useFsGestureVersionThree) {
            this.mUseFsGestureVersionThree = useFsGestureVersionThree;
            this.mRecentsImplementation.release();
            if (this.mUseFsGestureVersionThree) {
                this.mRecentsImplementation = new OverviewProxyRecentsImpl();
            } else {
                this.mRecentsImplementation = new LegacyRecentsImpl(this.mContext);
            }
            this.mRecentsImplementation.onStart(this.mContext, this);
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
            Log.e("Recents", "packageMonitor   onPackageModified  packageName=" + str + "   mIsRecentsWithinLauncher=" + Recents.this.mIsRecentsWithinLauncher);
            if (str != null && "com.miui.home".equals(str)) {
                boolean isRecentsWithinLauncher = Recents.sSystemServicesProxy.isRecentsWithinLauncher(Recents.this.mContext);
                if (Recents.this.mIsRecentsWithinLauncher != isRecentsWithinLauncher) {
                    boolean unused = Recents.this.mIsRecentsWithinLauncher = isRecentsWithinLauncher;
                    RecentsEventBus.getDefault().send(new RecentsWithinLauncherChangedEvent(Recents.this.mIsRecentsWithinLauncher));
                }
                boolean useMiuiHomeAsDefaultHome = Recents.sSystemServicesProxy.useMiuiHomeAsDefaultHome(Recents.this.mContext);
                if (Recents.this.mUseMiuiHomeAsDefaultHome != useMiuiHomeAsDefaultHome) {
                    Recents.this.updateDefaultHome(useMiuiHomeAsDefaultHome);
                }
                Recents.this.updateRecentsImplementation();
            }
        }
    }

    public void onBootCompleted() {
        this.mRecentsImplementation.onBootCompleted();
    }

    public void showRecentApps(boolean z, boolean z2) {
        if (isUserSetup()) {
            this.mRecentsImplementation.showRecentApps(z, z2);
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        if (isUserSetup()) {
            this.mRecentsImplementation.hideRecentApps(z, z2);
        }
    }

    public void toggleRecentApps() {
        if (isUserSetup()) {
            this.mRecentsImplementation.toggleRecentApps();
        }
    }

    public void preloadRecentApps() {
        if (isUserSetup()) {
            this.mRecentsImplementation.preloadRecentApps();
        }
    }

    public void cancelPreloadRecentApps() {
        if (isUserSetup()) {
            this.mRecentsImplementation.cancelPreloadRecentApps();
        }
    }

    public boolean dockTopTask(int i, int i2, Rect rect, int i3) {
        if (!isUserSetup()) {
            return false;
        }
        return this.mRecentsImplementation.dockTopTask(i, i2, rect, i3);
    }

    public static void logDockAttempt(Context context, ComponentName componentName, int i) {
        if (i == 0) {
            MetricsLogger.action(context, 391, componentName.flattenToShortString());
            RecentsPushEventHelper.sendEnterMultiWindowFailedEvent(componentName.flattenToShortString());
        }
        MetricsLogger.count(context, getMetricsCounterForResizeMode(i), 1);
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.mRecentsImplementation.onConfigurationChanged(configuration);
    }

    private boolean isUserSetup() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (Settings.Global.getInt(contentResolver, "device_provisioned", 0) == 0 || Settings.Secure.getIntForUser(contentResolver, "user_setup_complete", 0, -2) == 0) {
            return false;
        }
        return true;
    }
}
