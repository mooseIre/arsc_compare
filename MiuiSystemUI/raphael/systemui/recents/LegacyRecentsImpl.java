package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.util.EventLog;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.IRecentsSystemUserCallbacks;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.stackdivider.Divider;
import java.util.ArrayList;
import java.util.Iterator;

public class LegacyRecentsImpl implements RecentsImplementation {
    /* access modifiers changed from: private */
    public static SystemServicesProxy sSystemServicesProxy;
    /* access modifiers changed from: private */
    public Context mContext;
    private int mDraggingInRecentsCurrentUser;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public RecentsImpl mImpl;
    private Configuration mLastConfiguration = new Configuration();
    private final ArrayList<Runnable> mOnConnectRunnables = new ArrayList<>();
    private String mOverrideRecentsPackageName;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1);
                UserInfo userInfo = UserManager.get(context).getUserInfo(intExtra);
                if (LegacyRecentsImpl.this.mSecondaryUser != -10000) {
                    context.stopServiceAsUser(LegacyRecentsImpl.this.mSecondaryUserServiceIntent, new UserHandle(LegacyRecentsImpl.this.mSecondaryUser));
                    int unused = LegacyRecentsImpl.this.mSecondaryUser = -10000;
                }
                if (userInfo != null && intExtra != 0) {
                    context.startServiceAsUser(LegacyRecentsImpl.this.mSecondaryUserServiceIntent, new UserHandle(userInfo.id));
                    int unused2 = LegacyRecentsImpl.this.mSecondaryUser = userInfo.id;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mSecondaryUser = -10000;
    /* access modifiers changed from: private */
    public Intent mSecondaryUserServiceIntent;
    private SysUiServiceProvider mSysUiServiceProvider;
    private RecentsSystemUser mSystemToUserCallbacks;
    /* access modifiers changed from: private */
    public IRecentsSystemUserCallbacks mUserToSystemCallbacks;
    /* access modifiers changed from: private */
    public final IBinder.DeathRecipient mUserToSystemCallbacksDeathRcpt = new IBinder.DeathRecipient() {
        public void binderDied() {
            IRecentsSystemUserCallbacks unused = LegacyRecentsImpl.this.mUserToSystemCallbacks = null;
            EventLog.writeEvent(36060, new Object[]{3, Integer.valueOf(LegacyRecentsImpl.sSystemServicesProxy.getProcessUser())});
            LegacyRecentsImpl.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    LegacyRecentsImpl.this.registerWithSystemUser();
                }
            }, 5000);
        }
    };
    private final ServiceConnection mUserToSystemServiceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName componentName) {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder != null) {
                IRecentsSystemUserCallbacks unused = LegacyRecentsImpl.this.mUserToSystemCallbacks = IRecentsSystemUserCallbacks.Stub.asInterface(iBinder);
                EventLog.writeEvent(36060, new Object[]{2, Integer.valueOf(LegacyRecentsImpl.sSystemServicesProxy.getProcessUser())});
                try {
                    iBinder.linkToDeath(LegacyRecentsImpl.this.mUserToSystemCallbacksDeathRcpt, 0);
                } catch (RemoteException e) {
                    Log.e("LegacyRecentsImpl", "Lost connection to (System) SystemUI", e);
                }
                LegacyRecentsImpl.this.runAndFlushOnConnectRunnables();
            }
            LegacyRecentsImpl.this.mContext.unbindService(this);
        }
    };

    public LegacyRecentsImpl(Context context) {
    }

    public void onStart(Context context, SysUiServiceProvider sysUiServiceProvider) {
        this.mContext = context;
        this.mSysUiServiceProvider = sysUiServiceProvider;
        sSystemServicesProxy = SystemServicesProxy.getInstance(context);
        RecentsEventBus.getDefault().register(this, 1);
        this.mHandler = new Handler();
        this.mImpl = new RecentsImpl(context);
        RecentsEventBus.getDefault().register(this.mImpl, 1);
        if ("userdebug".equals(Build.TYPE) || "eng".equals(Build.TYPE)) {
            String str = SystemProperties.get("persist.recents_override_pkg");
            if (!str.isEmpty()) {
                this.mOverrideRecentsPackageName = str;
            }
        }
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            this.mSystemToUserCallbacks = new RecentsSystemUser(this.mContext, this.mImpl);
            this.mSecondaryUserServiceIntent = new Intent(this.mContext, SystemUISecondaryUserService.class);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.OWNER, intentFilter, (String) null, (Handler) null);
            return;
        }
        registerWithSystemUser();
    }

    public void release() {
        try {
            this.mContext.unregisterReceiver(this.mReceiver);
            RecentsEventBus.getDefault().unregister(this);
            RecentsEventBus.getDefault().unregister(this.mImpl);
        } catch (Exception unused) {
            Log.e("LegacyRecentsImpl", "release error");
        }
        RecentsImpl recentsImpl = this.mImpl;
        if (recentsImpl != null) {
            recentsImpl.release();
        }
    }

    public void onBootCompleted() {
        this.mImpl.onBootCompleted();
    }

    public void onConfigurationChanged(Configuration configuration) {
        int currentUser = sSystemServicesProxy.getCurrentUser();
        if (sSystemServicesProxy.isSystemUser(currentUser)) {
            RecentsImpl recentsImpl = this.mImpl;
            if (recentsImpl != null) {
                recentsImpl.onConfigurationChanged();
            }
        } else {
            RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
            if (recentsSystemUser != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser != null) {
                    try {
                        nonSystemUserRecentsForUser.onConfigurationChanged();
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                } else {
                    Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
        int updateFrom = this.mLastConfiguration.updateFrom(configuration);
        boolean z = true;
        boolean z2 = (Integer.MIN_VALUE & updateFrom) != 0;
        if ((updateFrom & 4) == 0) {
            z = false;
        }
        if (z2 || z) {
            RecentsTaskLoader taskLoader = Recents.getTaskLoader();
            if (taskLoader != null) {
                if (z2) {
                    taskLoader.onThemeChanged();
                }
                if (z) {
                    taskLoader.onLanguageChange();
                }
            }
            if (z) {
                sSystemServicesProxy.onLanguageChange();
            }
        }
    }

    public void preloadRecentApps() {
        int currentUser = sSystemServicesProxy.getCurrentUser();
        if (sSystemServicesProxy.isSystemUser(currentUser)) {
            this.mImpl.preloadRecents();
            return;
        }
        RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
        if (recentsSystemUser != null) {
            IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
            if (nonSystemUserRecentsForUser != null) {
                try {
                    nonSystemUserRecentsForUser.preloadRecents();
                } catch (RemoteException e) {
                    Log.e("LegacyRecentsImpl", "Callback failed", e);
                }
            } else {
                Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
            }
        }
    }

    public void cancelPreloadRecentApps() {
        int currentUser = sSystemServicesProxy.getCurrentUser();
        if (sSystemServicesProxy.isSystemUser(currentUser)) {
            this.mImpl.cancelPreloadingRecents();
            return;
        }
        RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
        if (recentsSystemUser != null) {
            IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
            if (nonSystemUserRecentsForUser != null) {
                try {
                    nonSystemUserRecentsForUser.cancelPreloadingRecents();
                } catch (RemoteException e) {
                    Log.e("LegacyRecentsImpl", "Callback failed", e);
                }
            } else {
                Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
            }
        }
    }

    public void showRecentApps(boolean z, boolean z2) {
        if (!proxyToOverridePackage("com.android.systemui.recents.ACTION_SHOW")) {
            try {
                ActivityManagerCompat.getService().closeSystemDialogs("recentapps");
            } catch (RemoteException unused) {
            }
            int growsRecents = ((Divider) getComponent(Divider.class)).getView().growsRecents();
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.showRecents(z, false, true, false, z2, growsRecents, false);
                return;
            }
            RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
            if (recentsSystemUser != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser != null) {
                    try {
                        nonSystemUserRecentsForUser.showRecents(z, false, true, false, z2, growsRecents);
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                } else {
                    Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        if (!proxyToOverridePackage("com.android.systemui.recents.ACTION_HIDE")) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.hideRecents(z, z2, false);
                return;
            }
            RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
            if (recentsSystemUser != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser != null) {
                    try {
                        nonSystemUserRecentsForUser.hideRecents(z, z2);
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                } else {
                    Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public void toggleRecentApps() {
        if (!proxyToOverridePackage("com.android.systemui.recents.ACTION_TOGGLE")) {
            int growsRecents = ((Divider) getComponent(Divider.class)).getView().growsRecents();
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.toggleRecents(growsRecents);
                return;
            }
            RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
            if (recentsSystemUser != null) {
                IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
                if (nonSystemUserRecentsForUser != null) {
                    try {
                        nonSystemUserRecentsForUser.toggleRecents(growsRecents);
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                } else {
                    Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public boolean dockTopTask(int i, int i2, Rect rect, int i3) {
        if (BaseRecentsImpl.toastForbidDockedWhenScreening(this.mContext) || Utilities.isInSmallWindowMode(this.mContext)) {
            return false;
        }
        Point point = new Point();
        if (rect == null) {
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(point);
            rect = new Rect(0, 0, point.x, point.y);
        }
        int currentUser = sSystemServicesProxy.getCurrentUser();
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ActivityManager.RunningTaskInfo runningTask = systemServices.getRunningTask();
        boolean isScreenPinningActive = systemServices.isScreenPinningActive();
        boolean z = runningTask != null && SystemServicesProxy.isHomeOrRecentsStack(ActivityManagerCompat.getRunningTaskStackId(runningTask), runningTask);
        if (runningTask != null && !z && !isScreenPinningActive) {
            Recents.logDockAttempt(this.mContext, runningTask.topActivity, ActivityManagerCompat.getRunningTaskResizeMode(runningTask));
            if (ActivityManagerCompat.isRunningTaskDockable(runningTask)) {
                if (i3 != -1) {
                    MetricsLogger.action(this.mContext, i3, runningTask.topActivity.flattenToShortString());
                }
                if (runningTask.topActivity != null) {
                    RecentsPushEventHelper.sendEnterMultiWindowEvent("outOfRecents", runningTask.topActivity.getPackageName());
                }
                if (sSystemServicesProxy.isSystemUser(currentUser)) {
                    RecentsImpl recentsImpl = this.mImpl;
                    if (recentsImpl != null) {
                        recentsImpl.dockTopTask(runningTask.id, i, i2, rect);
                    }
                } else {
                    RecentsSystemUser recentsSystemUser = this.mSystemToUserCallbacks;
                    if (recentsSystemUser != null) {
                        IRecentsNonSystemUserCallbacks nonSystemUserRecentsForUser = recentsSystemUser.getNonSystemUserRecentsForUser(currentUser);
                        if (nonSystemUserRecentsForUser != null) {
                            try {
                                nonSystemUserRecentsForUser.dockTopTask(runningTask.id, i, i2, rect);
                            } catch (RemoteException e) {
                                Log.e("LegacyRecentsImpl", "Callback failed", e);
                            }
                        } else {
                            Log.e("LegacyRecentsImpl", "No SystemUI callbacks found for user: " + currentUser);
                        }
                    }
                }
                this.mDraggingInRecentsCurrentUser = currentUser;
                return true;
            }
            Toast.makeText(this.mContext, R.string.recents_incompatible_app_message, 0).show();
        }
        return false;
    }

    public final void onBusEvent(final RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (systemServices.isSystemUser(systemServices.getProcessUser())) {
            RecentsImpl recentsImpl = this.mImpl;
            if (recentsImpl != null) {
                recentsImpl.onVisibilityChanged(recentsVisibilityChangedEvent.applicationContext, recentsVisibilityChangedEvent.visible);
                return;
            }
            return;
        }
        postToSystemUser(new Runnable() {
            public void run() {
                try {
                    LegacyRecentsImpl.this.mUserToSystemCallbacks.updateRecentsVisibility(recentsVisibilityChangedEvent.visible);
                } catch (RemoteException e) {
                    Log.e("LegacyRecentsImpl", "Callback failed", e);
                }
            }
        });
    }

    public final void onBusEvent(final ScreenPinningRequestEvent screenPinningRequestEvent) {
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            RecentsImpl recentsImpl = this.mImpl;
            if (recentsImpl != null) {
                recentsImpl.onStartScreenPinning(screenPinningRequestEvent.applicationContext, screenPinningRequestEvent.taskId);
                return;
            }
            return;
        }
        postToSystemUser(new Runnable() {
            public void run() {
                try {
                    LegacyRecentsImpl.this.mUserToSystemCallbacks.startScreenPinning(screenPinningRequestEvent.taskId);
                } catch (RemoteException e) {
                    Log.e("LegacyRecentsImpl", "Callback failed", e);
                }
            }
        });
    }

    public final void onBusEvent(RecentsDrawnEvent recentsDrawnEvent) {
        if (!sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        LegacyRecentsImpl.this.mUserToSystemCallbacks.sendRecentsDrawnEvent();
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(final DockedTopTaskEvent dockedTopTaskEvent) {
        if (!sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        LegacyRecentsImpl.this.mUserToSystemCallbacks.sendDockingTopTaskEvent(dockedTopTaskEvent.dragMode, dockedTopTaskEvent.initialRect);
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        if (!sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        LegacyRecentsImpl.this.mUserToSystemCallbacks.sendLaunchRecentsEvent();
                    } catch (RemoteException e) {
                        Log.e("LegacyRecentsImpl", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(ConfigurationChangedEvent configurationChangedEvent) {
        RecentsImpl recentsImpl = this.mImpl;
        if (recentsImpl != null) {
            recentsImpl.onConfigurationChanged();
        }
    }

    public IBinder getSystemUserCallbacks() {
        return this.mSystemToUserCallbacks;
    }

    public RecentsImpl getRecentsImpl() {
        return this.mImpl;
    }

    /* access modifiers changed from: private */
    public void registerWithSystemUser() {
        final int processUser = sSystemServicesProxy.getProcessUser();
        postToSystemUser(new Runnable() {
            public void run() {
                try {
                    LegacyRecentsImpl.this.mUserToSystemCallbacks.registerNonSystemUserCallbacks(new RecentsImplProxy(LegacyRecentsImpl.this.mImpl), processUser);
                } catch (RemoteException e) {
                    Log.e("LegacyRecentsImpl", "Failed to register", e);
                }
            }
        });
    }

    private void postToSystemUser(Runnable runnable) {
        this.mOnConnectRunnables.add(runnable);
        if (this.mUserToSystemCallbacks == null) {
            Intent intent = new Intent();
            intent.setClass(this.mContext, RecentsSystemUserService.class);
            boolean bindServiceAsUser = this.mContext.bindServiceAsUser(intent, this.mUserToSystemServiceConnection, 1, UserHandleCompat.SYSTEM);
            EventLog.writeEvent(36060, new Object[]{1, Integer.valueOf(sSystemServicesProxy.getProcessUser())});
            if (!bindServiceAsUser) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        LegacyRecentsImpl.this.registerWithSystemUser();
                    }
                }, 5000);
                return;
            }
            return;
        }
        runAndFlushOnConnectRunnables();
    }

    /* access modifiers changed from: private */
    public void runAndFlushOnConnectRunnables() {
        Iterator<Runnable> it = this.mOnConnectRunnables.iterator();
        while (it.hasNext()) {
            it.next().run();
        }
        this.mOnConnectRunnables.clear();
    }

    private boolean proxyToOverridePackage(String str) {
        if (this.mOverrideRecentsPackageName == null) {
            return false;
        }
        Intent intent = new Intent(str);
        intent.setPackage(this.mOverrideRecentsPackageName);
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent);
        return true;
    }

    private <T> T getComponent(Class<T> cls) {
        return this.mSysUiServiceProvider.getComponent(cls);
    }
}
