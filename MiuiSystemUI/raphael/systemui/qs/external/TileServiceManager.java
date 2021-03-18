package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.external.TileLifecycleManager;
import java.util.Objects;

public class TileServiceManager {
    static final String PREFS_FILE = "CustomTileModes";
    private boolean mBindAllowed;
    private boolean mBindRequested;
    private boolean mBound;
    private final Handler mHandler;
    private boolean mJustBound;
    final Runnable mJustBoundOver;
    private long mLastUpdate;
    private boolean mPendingBind;
    private int mPriority;
    private final TileServices mServices;
    private boolean mShowingDialog;
    private boolean mStarted;
    private final TileLifecycleManager mStateManager;
    private final Runnable mUnbind;
    private final BroadcastReceiver mUninstallReceiver;

    TileServiceManager(TileServices tileServices, Handler handler, ComponentName componentName, Tile tile, BroadcastDispatcher broadcastDispatcher) {
        this(tileServices, handler, new TileLifecycleManager(handler, tileServices.getContext(), tileServices, tile, new Intent().setComponent(componentName), new UserHandle(ActivityManager.getCurrentUser()), broadcastDispatcher));
    }

    TileServiceManager(TileServices tileServices, Handler handler, TileLifecycleManager tileLifecycleManager) {
        this.mPendingBind = true;
        this.mStarted = false;
        this.mUnbind = new Runnable() {
            /* class com.android.systemui.qs.external.TileServiceManager.AnonymousClass1 */

            public void run() {
                if (TileServiceManager.this.mBound && !TileServiceManager.this.mBindRequested) {
                    TileServiceManager.this.unbindService();
                }
            }
        };
        this.mJustBoundOver = new Runnable() {
            /* class com.android.systemui.qs.external.TileServiceManager.AnonymousClass2 */

            public void run() {
                TileServiceManager.this.mJustBound = false;
                TileServiceManager.this.mServices.recalculateBindAllowance();
            }
        };
        this.mUninstallReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.qs.external.TileServiceManager.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                    String encodedSchemeSpecificPart = intent.getData().getEncodedSchemeSpecificPart();
                    ComponentName component = TileServiceManager.this.mStateManager.getComponent();
                    if (Objects.equals(encodedSchemeSpecificPart, component.getPackageName())) {
                        if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                            Intent intent2 = new Intent("android.service.quicksettings.action.QS_TILE");
                            intent2.setPackage(encodedSchemeSpecificPart);
                            for (ResolveInfo resolveInfo : context.getPackageManager().queryIntentServicesAsUser(intent2, 0, ActivityManager.getCurrentUser())) {
                                if (Objects.equals(resolveInfo.serviceInfo.packageName, component.getPackageName()) && Objects.equals(resolveInfo.serviceInfo.name, component.getClassName())) {
                                    return;
                                }
                            }
                        }
                        TileServiceManager.this.mServices.getHost().removeTile(component);
                    }
                }
            }
        };
        this.mServices = tileServices;
        this.mHandler = handler;
        this.mStateManager = tileLifecycleManager;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mServices.getContext().registerReceiverAsUser(this.mUninstallReceiver, new UserHandle(ActivityManager.getCurrentUser()), intentFilter, null, this.mHandler);
    }

    /* access modifiers changed from: package-private */
    public boolean isLifecycleStarted() {
        return this.mStarted;
    }

    /* access modifiers changed from: package-private */
    public void startLifecycleManagerAndAddTile() {
        this.mStarted = true;
        ComponentName component = this.mStateManager.getComponent();
        Context context = this.mServices.getContext();
        if (!TileLifecycleManager.isTileAdded(context, component)) {
            TileLifecycleManager.setTileAdded(context, component, true);
            this.mStateManager.onTileAdded();
            this.mStateManager.flushMessagesAndUnbind();
        }
    }

    public void setTileChangeListener(TileLifecycleManager.TileChangeListener tileChangeListener) {
        this.mStateManager.setTileChangeListener(tileChangeListener);
    }

    public boolean isActiveTile() {
        return this.mStateManager.isActiveTile();
    }

    public boolean isToggleableTile() {
        return this.mStateManager.isToggleableTile();
    }

    public void setShowingDialog(boolean z) {
        this.mShowingDialog = z;
    }

    public IQSTileService getTileService() {
        return this.mStateManager;
    }

    public IBinder getToken() {
        return this.mStateManager.getToken();
    }

    public void setBindRequested(boolean z) {
        if (this.mBindRequested != z) {
            this.mBindRequested = z;
            if (!this.mBindAllowed || !z || this.mBound) {
                this.mServices.recalculateBindAllowance();
            } else {
                this.mHandler.removeCallbacks(this.mUnbind);
                bindService();
            }
            if (this.mBound && !this.mBindRequested) {
                this.mHandler.postDelayed(this.mUnbind, 30000);
            }
        }
    }

    public void setLastUpdate(long j) {
        this.mLastUpdate = j;
        if (this.mBound && isActiveTile()) {
            this.mStateManager.onStopListening();
            setBindRequested(false);
        }
        this.mServices.recalculateBindAllowance();
    }

    public void handleDestroy() {
        setBindAllowed(false);
        this.mServices.getContext().unregisterReceiver(this.mUninstallReceiver);
        this.mStateManager.handleDestroy();
    }

    public void setBindAllowed(boolean z) {
        if (this.mBindAllowed != z) {
            this.mBindAllowed = z;
            if (!z && this.mBound) {
                unbindService();
            } else if (this.mBindAllowed && this.mBindRequested && !this.mBound) {
                bindService();
            }
        }
    }

    public boolean hasPendingBind() {
        return this.mPendingBind;
    }

    public void clearPendingBind() {
        this.mPendingBind = false;
    }

    private void bindService() {
        if (this.mBound) {
            Log.e("TileServiceManager", "Service already bound");
            return;
        }
        this.mPendingBind = true;
        this.mBound = true;
        this.mJustBound = true;
        this.mHandler.postDelayed(this.mJustBoundOver, 5000);
        this.mStateManager.setBindService(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unbindService() {
        if (!this.mBound) {
            Log.e("TileServiceManager", "Service not bound");
            return;
        }
        this.mBound = false;
        this.mJustBound = false;
        this.mStateManager.setBindService(false);
    }

    public void calculateBindPriority(long j) {
        if (this.mStateManager.hasPendingClick()) {
            this.mPriority = Integer.MAX_VALUE;
        } else if (this.mShowingDialog) {
            this.mPriority = 2147483646;
        } else if (this.mJustBound) {
            this.mPriority = 2147483645;
        } else if (!this.mBindRequested) {
            this.mPriority = Integer.MIN_VALUE;
        } else {
            long j2 = j - this.mLastUpdate;
            if (j2 > 2147483644) {
                this.mPriority = 2147483644;
            } else {
                this.mPriority = (int) j2;
            }
        }
    }

    public int getBindPriority() {
        return this.mPriority;
    }
}
