package com.android.systemui.recents;

import android.app.ActivityManagerCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.AccessibilityManagerCompat;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.systemui.Application;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.SystemUICompat;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.recents.ISystemUiProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.xiaomi.stat.d;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import miui.securityspace.CrossUserUtils;

public class OverviewProxyService implements CallbackController<OverviewProxyListener>, Dumpable {
    private Region mActiveNavBarRegion;
    /* access modifiers changed from: private */
    public float mBackButtonAlpha;
    private boolean mBound;
    /* access modifiers changed from: private */
    public int mConnectionBackoffAttempts;
    /* access modifiers changed from: private */
    public final List<OverviewProxyListener> mConnectionCallbacks = new ArrayList();
    private final Runnable mConnectionRunnable = new Runnable() {
        public void run() {
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentBoundedUserId = -1;
    /* access modifiers changed from: private */
    public final Runnable mDeferredConnectionCallback = new Runnable() {
        public void run() {
            Log.w("OverviewProxyService", "Binder supposed established connection but actual connection to service timed out, trying again");
            OverviewProxyService.this.retryConnectionWithBackoff();
        }
    };
    private final DeviceProvisionedController.DeviceProvisionedListener mDeviceProvisionedCallback = new DeviceProvisionedController.DeviceProvisionedListener() {
        public void onDeviceProvisionedChanged() {
        }

        public void onUserSetupChanged() {
            if (OverviewProxyService.this.mDeviceProvisionedController.isCurrentUserSetup()) {
                OverviewProxyService.this.internalConnectToCurrentUser();
            }
        }

        public void onUserSwitched() {
            int unused = OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    /* access modifiers changed from: private */
    public final DeviceProvisionedController mDeviceProvisionedController;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsControlCenterMotion;
    private boolean mIsEnabled;
    private final BroadcastReceiver mLauncherStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            OverviewProxyService.this.updateEnabledState();
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    /* access modifiers changed from: private */
    public IOverviewProxy mOverviewProxy;
    private final ServiceConnection mOverviewServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            int unused = OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.mHandler.removeCallbacks(OverviewProxyService.this.mDeferredConnectionCallback);
            try {
                iBinder.linkToDeath(OverviewProxyService.this.mOverviewServiceDeathRcpt, 0);
                OverviewProxyService overviewProxyService = OverviewProxyService.this;
                int unused2 = overviewProxyService.mCurrentBoundedUserId = overviewProxyService.mDeviceProvisionedController.getCurrentUser();
                IOverviewProxy unused3 = OverviewProxyService.this.mOverviewProxy = IOverviewProxy.Stub.asInterface(iBinder);
                Bundle bundle = new Bundle();
                bundle.putBinder("extra_sysui_proxy", OverviewProxyService.this.mSysUiProxy.asBinder());
                bundle.putFloat("extra_window_corner_radius", 20.0f);
                bundle.putBoolean("extra_supports_window_corners", false);
                try {
                    OverviewProxyService.this.mOverviewProxy.onInitialize(bundle);
                } catch (RemoteException e) {
                    int unused4 = OverviewProxyService.this.mCurrentBoundedUserId = -1;
                    Log.e("OverviewProxyService", "Failed to call onInitialize()", e);
                }
                OverviewProxyService.this.dispatchNavButtonBounds();
                OverviewProxyService.this.updateSystemUiStateFlags();
                OverviewProxyService.this.notifyConnectionChanged();
            } catch (RemoteException e2) {
                Log.e("OverviewProxyService", "Lost connection to launcher service", e2);
                OverviewProxyService.this.disconnectFromLauncherService();
                OverviewProxyService.this.retryConnectionWithBackoff();
            }
        }

        public void onNullBinding(ComponentName componentName) {
            Log.w("OverviewProxyService", "Null binding of '" + componentName + "', try reconnecting");
            int unused = OverviewProxyService.this.mCurrentBoundedUserId = -1;
            OverviewProxyService.this.retryConnectionWithBackoff();
        }

        public void onBindingDied(ComponentName componentName) {
            Log.w("OverviewProxyService", "Binding died of '" + componentName + "', try reconnecting");
            int unused = OverviewProxyService.this.mCurrentBoundedUserId = -1;
            OverviewProxyService.this.retryConnectionWithBackoff();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            int unused = OverviewProxyService.this.mCurrentBoundedUserId = -1;
        }
    };
    /* access modifiers changed from: private */
    public final IBinder.DeathRecipient mOverviewServiceDeathRcpt = new IBinder.DeathRecipient() {
        public void binderDied() {
            OverviewProxyService.this.cleanupAfterDeath();
        }
    };
    private final Intent mQuickStepIntent;
    /* access modifiers changed from: private */
    public MotionEvent mStatusBarGestureDownEvent;
    /* access modifiers changed from: private */
    public ISystemUiProxy mSysUiProxy = new ISystemUiProxy.Stub() {
        public void startScreenPinning(final int i) {
            if (verifyCaller("startScreenPinning")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            StatusBar statusBar = (StatusBar) ((Application) OverviewProxyService.this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
                            if (statusBar != null) {
                                statusBar.showScreenPinningRequest(i, false);
                            }
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void stopScreenPinning() {
            if (verifyCaller("stopScreenPinning")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            try {
                                ActivityManagerCompat.stopSystemLockTaskMode();
                            } catch (RemoteException unused) {
                                Log.e("OverviewProxyService", "Failed to stop screen pinning");
                            }
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void onStatusBarMotionEvent(final MotionEvent motionEvent) {
            if (verifyCaller("onStatusBarMotionEvent")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            StatusBar statusBar = (StatusBar) ((Application) OverviewProxyService.this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
                            if (statusBar != null) {
                                int actionMasked = motionEvent.getActionMasked();
                                if (actionMasked == 0) {
                                    MotionEvent unused = OverviewProxyService.this.mStatusBarGestureDownEvent = MotionEvent.obtain(motionEvent);
                                    boolean unused2 = OverviewProxyService.this.mIsControlCenterMotion = ((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() && motionEvent.getX() >= ((float) statusBar.getStatusBarWindow().getWidth()) / 2.0f;
                                }
                                if (OverviewProxyService.this.mIsControlCenterMotion) {
                                    statusBar.dispatchControlPanelTouchEvent(motionEvent);
                                } else {
                                    statusBar.dispatchNotificationsPanelTouchEvent(motionEvent);
                                }
                                if ((actionMasked == 1 || actionMasked == 3) && OverviewProxyService.this.mStatusBarGestureDownEvent != null) {
                                    OverviewProxyService.this.mStatusBarGestureDownEvent.recycle();
                                    MotionEvent unused3 = OverviewProxyService.this.mStatusBarGestureDownEvent = null;
                                }
                                motionEvent.recycle();
                            }
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void onSplitScreenInvoked() {
            if (verifyCaller("onSplitScreenInvoked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    Divider divider = (Divider) ((Application) OverviewProxyService.this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(Divider.class);
                    if (divider != null) {
                        divider.onDockedFirstAnimationFrame();
                    }
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void exitSplitScreen() {
            if (verifyCaller("exitSplitScreen")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    Divider divider = (Divider) ((Application) OverviewProxyService.this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(Divider.class);
                    if (divider != null) {
                        divider.onUndockingTask(true);
                    }
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void onOverviewShown(final boolean z) {
            if (verifyCaller("onOverviewShown")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            for (int size = OverviewProxyService.this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
                                ((OverviewProxyListener) OverviewProxyService.this.mConnectionCallbacks.get(size)).onOverviewShown(z);
                            }
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public Rect getNonMinimizedSplitScreenSecondaryBounds() {
            if (!verifyCaller("getNonMinimizedSplitScreenSecondaryBounds")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Divider divider = (Divider) ((Application) OverviewProxyService.this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(Divider.class);
                if (divider != null) {
                    return divider.getView().getNonMinimizedSplitScreenSecondaryBounds();
                }
                Binder.restoreCallingIdentity(clearCallingIdentity);
                return null;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        public Rect getMiddleSplitScreenSecondaryBounds() {
            if (!verifyCaller("getMiddleSplitScreenSecondaryBounds")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Divider divider = (Divider) ((Application) OverviewProxyService.this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(Divider.class);
                if (divider != null) {
                    return divider.getView().getMiddleSplitScreenSecondaryBounds();
                }
                Binder.restoreCallingIdentity(clearCallingIdentity);
                return null;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        public void setBackButtonAlpha(final float f, final boolean z) {
            if (verifyCaller("setBackButtonAlpha")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    float unused = OverviewProxyService.this.mBackButtonAlpha = f;
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            OverviewProxyService.this.notifyBackButtonAlphaChanged(f, z);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void onAssistantProgress(final float f) {
            if (verifyCaller("onAssistantProgress")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            OverviewProxyService.this.notifyAssistantProgress(f);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void startAssistant(final Bundle bundle) {
            if (verifyCaller("startAssistant")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            OverviewProxyService.this.notifyStartAssistant(bundle);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void onAssistantGestureCompletion() {
            if (verifyCaller("onAssistantProgress")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            OverviewProxyService.this.notifyCompleteAssistant();
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public Bundle monitorGestureInput(String str, int i) {
            if (!verifyCaller("monitorGestureInput")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Bundle inputManagerBundle = SystemUICompat.getInputManagerBundle("extra_input_monitor", str, i);
                if (inputManagerBundle == null) {
                    inputManagerBundle = new Bundle();
                }
                return inputManagerBundle;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        public void notifyAccessibilityButtonClicked(int i) {
            if (verifyCaller("notifyAccessibilityButtonClicked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    AccessibilityManagerCompat.notifyAccessibilityButtonClicked(AccessibilityManager.getInstance(OverviewProxyService.this.mContext), i);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void notifyAccessibilityButtonLongClicked() {
            if (verifyCaller("notifyAccessibilityButtonLongClicked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
                    intent.addFlags(268468224);
                    OverviewProxyService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        public void onGestureLineProgress(final float f) {
            if (verifyCaller("onGestureLineProgress")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable() {
                        public void run() {
                            OverviewProxyService.this.notifyGestureLineProgress(f);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        private boolean verifyCaller(String str) {
            int identifier = Binder.getCallingUserHandle().getIdentifier();
            if (identifier == OverviewProxyService.this.mCurrentBoundedUserId) {
                return true;
            }
            Log.w("OverviewProxyService", "Launcher called sysui with invalid user: " + identifier + ", reason: " + str);
            return false;
        }
    };
    private int mSysUiStateFlags;
    private String pakName = "com.miui.home";

    public interface OverviewProxyListener {
        void completeAssistant();

        void onAssistantProgress(float f);

        void onBackButtonAlphaChanged(float f, boolean z);

        void onConnectionChanged(boolean z);

        void onOverviewShown(boolean z);

        void startAssistant(Bundle bundle);
    }

    public OverviewProxyService(Context context, DeviceProvisionedController deviceProvisionedController) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mConnectionBackoffAttempts = 0;
        this.mQuickStepIntent = new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.pakName);
        this.mBackButtonAlpha = 1.0f;
        if (UserHandle.myUserId() == 0) {
            updateEnabledState();
            this.mDeviceProvisionedController.addCallback(this.mDeviceProvisionedCallback);
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addDataScheme("package");
            intentFilter.addDataSchemeSpecificPart(this.pakName, 0);
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            this.mContext.registerReceiver(this.mLauncherStateChangedReceiver, intentFilter);
        }
    }

    public void setSystemUiStateFlag(int i, boolean z) {
        int i2 = this.mSysUiStateFlags;
        int i3 = z ? i | i2 : (~i) & i2;
        if (this.mSysUiStateFlags != i3) {
            this.mSysUiStateFlags = i3;
            notifySystemUiStateFlags(this.mSysUiStateFlags);
        }
    }

    /* access modifiers changed from: private */
    public void updateSystemUiStateFlags() {
        StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        NavigationBarView navigationBarView = statusBar != null ? statusBar.getNavigationBarView() : null;
        this.mSysUiStateFlags = 0;
        if (navigationBarView != null) {
            navigationBarView.updateSystemUiStateFlags();
        }
        notifySystemUiStateFlags(this.mSysUiStateFlags);
    }

    private void notifySystemUiStateFlags(int i) {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onSystemUiStateChanged(i);
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to notify sysui state change", e);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchNavButtonBounds() {
        Region region;
        IOverviewProxy iOverviewProxy = this.mOverviewProxy;
        if (iOverviewProxy != null && (region = this.mActiveNavBarRegion) != null) {
            try {
                iOverviewProxy.onActiveNavBarRegionChanges(region);
            } catch (RemoteException e) {
                Log.e("OverviewProxyService", "Failed to call onActiveNavBarRegionChanges()", e);
            }
        }
    }

    public void cleanupAfterDeath() {
        startConnectionToCurrentUser();
    }

    public void startConnectionToCurrentUser() {
        Log.e("OverviewProxyService", "startConnectionToCurrentUser");
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mConnectionRunnable);
        } else {
            internalConnectToCurrentUser();
        }
    }

    /* access modifiers changed from: private */
    public void internalConnectToCurrentUser() {
        Log.e("OverviewProxyService", "internalConnectToCurrentUser");
        disconnectFromLauncherService();
        if (!this.mDeviceProvisionedController.isCurrentUserSetup() || !isEnabled()) {
            Log.v("OverviewProxyService", "Cannot attempt connection, is setup " + this.mDeviceProvisionedController.isCurrentUserSetup() + ", is enabled " + isEnabled());
            return;
        }
        this.mHandler.removeCallbacks(this.mConnectionRunnable);
        Intent intent = new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.pakName);
        try {
            Log.e("OverviewProxyService", "internalConnectToCurrentUser  bindServiceAsUser", new Throwable());
            this.mBound = this.mContext.bindServiceAsUser(intent, this.mOverviewServiceConnection, 33554433, UserHandle.of(this.mDeviceProvisionedController.getCurrentUser()));
        } catch (SecurityException e) {
            Log.e("OverviewProxyService", "Unable to bind because of security error", e);
        }
        if (this.mBound) {
            this.mHandler.postDelayed(this.mDeferredConnectionCallback, 5000);
        } else {
            retryConnectionWithBackoff();
        }
    }

    /* access modifiers changed from: private */
    public void retryConnectionWithBackoff() {
        if (!this.mHandler.hasCallbacks(this.mConnectionRunnable)) {
            long min = (long) Math.min(Math.scalb(1000.0f, this.mConnectionBackoffAttempts), 600000.0f);
            this.mHandler.postDelayed(this.mConnectionRunnable, min);
            this.mConnectionBackoffAttempts++;
            Log.w("OverviewProxyService", "Failed to connect on attempt " + this.mConnectionBackoffAttempts + " will try again in " + min + d.H);
        }
    }

    public void addCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.add(overviewProxyListener);
        overviewProxyListener.onConnectionChanged(this.mOverviewProxy != null);
        overviewProxyListener.onBackButtonAlphaChanged(this.mBackButtonAlpha, false);
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public IOverviewProxy getProxy() {
        return this.mOverviewProxy;
    }

    /* access modifiers changed from: private */
    public void disconnectFromLauncherService() {
        if (this.mBound) {
            this.mContext.unbindService(this.mOverviewServiceConnection);
            this.mBound = false;
        }
        IOverviewProxy iOverviewProxy = this.mOverviewProxy;
        if (iOverviewProxy != null) {
            iOverviewProxy.asBinder().unlinkToDeath(this.mOverviewServiceDeathRcpt, 0);
            this.mOverviewProxy = null;
            notifyBackButtonAlphaChanged(1.0f, false);
            notifyConnectionChanged();
        }
    }

    /* access modifiers changed from: private */
    public void notifyBackButtonAlphaChanged(float f, boolean z) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onBackButtonAlphaChanged(f, z);
        }
    }

    /* access modifiers changed from: private */
    public void notifyConnectionChanged() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onConnectionChanged(this.mOverviewProxy != null);
        }
    }

    /* access modifiers changed from: private */
    public void notifyGestureLineProgress(float f) {
        NavigationBarView navigationBarView;
        StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        if (statusBar != null && (navigationBarView = statusBar.getNavigationBarView()) != null) {
            navigationBarView.onGestureLineProgress(f);
        }
    }

    /* access modifiers changed from: private */
    public void notifyAssistantProgress(float f) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onAssistantProgress(f);
        }
    }

    /* access modifiers changed from: private */
    public void notifyStartAssistant(Bundle bundle) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).startAssistant(bundle);
        }
    }

    /* access modifiers changed from: private */
    public void notifyCompleteAssistant() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).completeAssistant();
        }
    }

    /* access modifiers changed from: private */
    public void updateEnabledState() {
        this.mIsEnabled = this.mContext.getPackageManager().resolveServiceAsUser(this.mQuickStepIntent, 1048576, CrossUserUtils.getCurrentUserId()) != null;
        Log.e("OverviewProxyService", "updateEnabledState    mIsEnabled=" + this.mIsEnabled);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("OverviewProxyService state:");
        printWriter.print("  isConnected=");
        printWriter.println(this.mOverviewProxy != null);
        printWriter.print("  isCurrentUserSetup=");
        printWriter.println(this.mDeviceProvisionedController.isCurrentUserSetup());
        printWriter.print("  connectionBackoffAttempts=");
        printWriter.println(this.mConnectionBackoffAttempts);
        printWriter.print("  quickStepIntent=");
        printWriter.println(this.mQuickStepIntent);
        printWriter.print("  quickStepIntentResolved=");
        printWriter.println(isEnabled());
        printWriter.print("  mSysUiStateFlags=");
        printWriter.println(this.mSysUiStateFlags);
    }
}
