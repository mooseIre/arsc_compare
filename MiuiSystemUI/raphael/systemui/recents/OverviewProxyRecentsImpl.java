package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.trust.TrustManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.android.systemui.Dependency;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.StatusBar;

public class OverviewProxyRecentsImpl implements RecentsImplementation {
    private Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private OverviewProxyService mOverviewProxyService;
    private SysUiServiceProvider mSysUiServiceProvider;
    /* access modifiers changed from: private */
    public TrustManager mTrustManager;

    public void cancelPreloadRecentApps() {
    }

    public void onBootCompleted() {
    }

    public void onConfigurationChanged(Configuration configuration) {
    }

    public void preloadRecentApps() {
    }

    public void onStart(Context context, SysUiServiceProvider sysUiServiceProvider) {
        this.mContext = context;
        this.mSysUiServiceProvider = sysUiServiceProvider;
        this.mHandler = new Handler();
        this.mTrustManager = (TrustManager) context.getSystemService("trust");
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        Recents.getSystemServices().registerMiuiTaskResizeList(this.mContext);
        RecentsEventBus.getDefault().register(this, 1);
    }

    public void release() {
        try {
            RecentsEventBus.getDefault().unregister(this);
        } catch (Exception unused) {
            Log.e("OverviewProxyRecentsImpl", "release error");
        }
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        Recents.getSystemServices().setRecentsVisibility(recentsVisibilityChangedEvent.applicationContext, recentsVisibilityChangedEvent.visible);
    }

    public void showRecentApps(boolean z, boolean z2) {
        IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
        if (proxy != null) {
            try {
                proxy.onOverviewShown(z);
            } catch (RemoteException e) {
                Log.e("OverviewProxyRecentsImpl", "Failed to send overview show event to launcher.", e);
            }
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
        if (proxy != null) {
            try {
                proxy.onOverviewHidden(z, z2);
            } catch (RemoteException e) {
                Log.e("OverviewProxyRecentsImpl", "Failed to send overview hide event to launcher.", e);
            }
        }
    }

    public void toggleRecentApps() {
        if (this.mOverviewProxyService.getProxy() != null) {
            final $$Lambda$OverviewProxyRecentsImpl$ZzsBj6p_GVl3rLvpPgWKT0NW9E r0 = new Runnable() {
                public final void run() {
                    OverviewProxyRecentsImpl.this.lambda$toggleRecentApps$0$OverviewProxyRecentsImpl();
                }
            };
            StatusBar statusBar = (StatusBar) this.mSysUiServiceProvider.getComponent(StatusBar.class);
            if (statusBar == null || !statusBar.isKeyguardShowing()) {
                r0.run();
            } else {
                statusBar.executeRunnableDismissingKeyguard(new Runnable() {
                    public void run() {
                        OverviewProxyRecentsImpl.this.mTrustManager.reportKeyguardShowingChanged();
                        OverviewProxyRecentsImpl.this.mHandler.post(r0);
                    }
                }, (Runnable) null, true, false, true);
            }
        }
    }

    public /* synthetic */ void lambda$toggleRecentApps$0$OverviewProxyRecentsImpl() {
        try {
            if (this.mOverviewProxyService.getProxy() != null) {
                this.mOverviewProxyService.getProxy().onOverviewToggle();
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyRecentsImpl", "Cannot send toggle recents through proxy service.", e);
        }
    }

    public boolean dockTopTask(int i, int i2, Rect rect, int i3) {
        if (BaseRecentsImpl.toastForbidDockedWhenScreening(this.mContext)) {
            return false;
        }
        if (!Utilities.supportsMultiWindow()) {
            Toast.makeText(this.mContext, R.string.recent_cannot_dock, 1).show();
            return false;
        } else if (Utilities.isInSmallWindowMode(this.mContext)) {
            return false;
        } else {
            Point point = new Point();
            if (rect == null) {
                ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(point);
                rect = new Rect(0, 0, point.x, point.y);
            }
            SystemServicesProxy systemServices = Recents.getSystemServices();
            ActivityManager.RunningTaskInfo runningTask = systemServices.getRunningTask();
            boolean isScreenPinningActive = systemServices.isScreenPinningActive();
            boolean z = runningTask != null && SystemServicesProxy.isHomeOrRecentsStack(ActivityManagerCompat.getRunningTaskStackId(runningTask), runningTask);
            if (runningTask != null && !z && !isScreenPinningActive) {
                if (!ActivityManagerCompat.isRunningTaskDockable(runningTask)) {
                    Toast.makeText(this.mContext, R.string.recents_incompatible_app_message, 0).show();
                } else if (!systemServices.moveTaskToDockedStack(runningTask.id, i2, rect)) {
                    return false;
                } else {
                    Divider divider = (Divider) this.mSysUiServiceProvider.getComponent(Divider.class);
                    if (divider != null) {
                        divider.onRecentsDrawn();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
