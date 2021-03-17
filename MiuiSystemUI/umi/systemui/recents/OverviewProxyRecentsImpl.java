package com.android.systemui.recents;

import android.app.ActivityManager;
import android.app.trust.TrustManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.Optional;

public class OverviewProxyRecentsImpl implements RecentsImplementation {
    private Context mContext;
    private final Optional<Divider> mDividerOptional;
    private Handler mHandler;
    private OverviewProxyService mOverviewProxyService;
    private final Lazy<StatusBar> mStatusBarLazy;
    private TrustManager mTrustManager;

    public OverviewProxyRecentsImpl(Optional<Lazy<StatusBar>> optional, Optional<Divider> optional2) {
        this.mStatusBarLazy = optional.orElse(null);
        this.mDividerOptional = optional2;
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public void onStart(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mTrustManager = (TrustManager) context.getSystemService("trust");
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public void showRecentApps(boolean z) {
        IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
        if (proxy != null) {
            try {
                proxy.onOverviewShown(z);
            } catch (RemoteException e) {
                Log.e("OverviewProxyRecentsImpl", "Failed to send overview show event to launcher.", e);
            }
        }
    }

    @Override // com.android.systemui.recents.RecentsImplementation
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

    @Override // com.android.systemui.recents.RecentsImplementation
    public void toggleRecentApps() {
        if (this.mOverviewProxyService.getProxy() != null) {
            $$Lambda$OverviewProxyRecentsImpl$ZzsBj6p_GVl3rLvpPgWKT0NW9E r0 = new Runnable() {
                /* class com.android.systemui.recents.$$Lambda$OverviewProxyRecentsImpl$ZzsBj6p_GVl3rLvpPgWKT0NW9E */

                public final void run() {
                    OverviewProxyRecentsImpl.this.lambda$toggleRecentApps$0$OverviewProxyRecentsImpl();
                }
            };
            Lazy<StatusBar> lazy = this.mStatusBarLazy;
            if (lazy == null || !lazy.get().isKeyguardShowing()) {
                r0.run();
            } else {
                this.mStatusBarLazy.get().executeRunnableDismissingKeyguard(new Runnable(r0) {
                    /* class com.android.systemui.recents.$$Lambda$OverviewProxyRecentsImpl$PUSBynP3ZsSZrPqXO1jJqSKnayU */
                    public final /* synthetic */ Runnable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        OverviewProxyRecentsImpl.this.lambda$toggleRecentApps$1$OverviewProxyRecentsImpl(this.f$1);
                    }
                }, null, true, false, true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$toggleRecentApps$0 */
    public /* synthetic */ void lambda$toggleRecentApps$0$OverviewProxyRecentsImpl() {
        try {
            if (this.mOverviewProxyService.getProxy() != null) {
                this.mOverviewProxyService.getProxy().onOverviewToggle();
                this.mOverviewProxyService.notifyToggleRecentApps();
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyRecentsImpl", "Cannot send toggle recents through proxy service.", e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$toggleRecentApps$1 */
    public /* synthetic */ void lambda$toggleRecentApps$1$OverviewProxyRecentsImpl(Runnable runnable) {
        this.mTrustManager.reportKeyguardShowingChanged();
        this.mHandler.post(runnable);
    }

    @Override // com.android.systemui.recents.RecentsImplementation
    public boolean splitPrimaryTask(int i, Rect rect, int i2) {
        Point point = new Point();
        if (rect == null) {
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(point);
            rect = new Rect(0, 0, point.x, point.y);
        }
        ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
        int activityType = runningTask != null ? runningTask.configuration.windowConfiguration.getActivityType() : 0;
        boolean isScreenPinningActive = ActivityManagerWrapper.getInstance().isScreenPinningActive();
        boolean z = activityType == 2 || activityType == 3;
        if (runningTask != null && !z && !isScreenPinningActive) {
            if (!runningTask.supportsSplitScreenMultiWindow) {
                Toast.makeText(this.mContext, C0021R$string.dock_non_resizeble_failed_to_dock_text, 0).show();
            } else if (ActivityManagerWrapper.getInstance().setTaskWindowingModeSplitScreenPrimary(runningTask.id, i, rect)) {
                this.mDividerOptional.ifPresent($$Lambda$fHPOCVoTSvBox_jGWtU7jxIAav4.INSTANCE);
                this.mDividerOptional.ifPresent($$Lambda$SmHdjDaQkSsbiXXCyerAyvUNnY.INSTANCE);
                return true;
            }
        }
        return false;
    }
}
