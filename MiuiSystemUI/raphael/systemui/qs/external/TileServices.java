package com.android.systemui.qs.external;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.quicksettings.IQSService;
import android.service.quicksettings.Tile;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Executor;

public class TileServices extends IQSService.Stub {
    private static final Comparator<TileServiceManager> SERVICE_SORT = new Comparator<TileServiceManager>() {
        /* class com.android.systemui.qs.external.TileServices.AnonymousClass3 */

        public int compare(TileServiceManager tileServiceManager, TileServiceManager tileServiceManager2) {
            return -Integer.compare(tileServiceManager.getBindPriority(), tileServiceManager2.getBindPriority());
        }
    };
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final Context mContext;
    private final Executor mExecutor;
    private final Handler mHandler;
    private final QSTileHost mHost;
    private final Handler mMainHandler;
    private int mMaxBound = 10;
    private final BroadcastReceiver mRequestListeningReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.qs.external.TileServices.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if ("android.service.quicksettings.action.REQUEST_LISTENING".equals(intent.getAction())) {
                TileServices.this.requestListening((ComponentName) intent.getParcelableExtra("android.intent.extra.COMPONENT_NAME"));
            }
        }
    };
    private final ArrayMap<CustomTile, TileServiceManager> mServices = new ArrayMap<>();
    private final ArrayMap<ComponentName, CustomTile> mTiles = new ArrayMap<>();
    private final ArrayMap<IBinder, CustomTile> mTokenMap = new ArrayMap<>();

    public TileServices(QSTileHost qSTileHost, Looper looper, BroadcastDispatcher broadcastDispatcher, Executor executor) {
        this.mHost = qSTileHost;
        this.mContext = qSTileHost.getContext();
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mHandler = new Handler(looper);
        this.mExecutor = executor;
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mBroadcastDispatcher.registerReceiver(this.mRequestListeningReceiver, new IntentFilter("android.service.quicksettings.action.REQUEST_LISTENING"), null, UserHandle.ALL);
    }

    public Context getContext() {
        return this.mContext;
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public TileServiceManager getTileWrapper(CustomTile customTile) {
        ComponentName component = customTile.getComponent();
        TileServiceManager onCreateTileService = onCreateTileService(component, customTile.getQsTile(), this.mBroadcastDispatcher);
        synchronized (this.mServices) {
            this.mServices.put(customTile, onCreateTileService);
            this.mTiles.put(component, customTile);
            this.mTokenMap.put(onCreateTileService.getToken(), customTile);
        }
        onCreateTileService.startLifecycleManagerAndAddTile();
        return onCreateTileService;
    }

    /* access modifiers changed from: protected */
    public TileServiceManager onCreateTileService(ComponentName componentName, Tile tile, BroadcastDispatcher broadcastDispatcher) {
        return new TileServiceManager(this, this.mHandler, componentName, tile, broadcastDispatcher);
    }

    public void freeService(CustomTile customTile, TileServiceManager tileServiceManager) {
        this.mExecutor.execute(new Runnable(tileServiceManager, customTile) {
            /* class com.android.systemui.qs.external.$$Lambda$TileServices$ovbFZPyneKkWl1sq50iCLdrLoVg */
            public final /* synthetic */ TileServiceManager f$1;
            public final /* synthetic */ CustomTile f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                TileServices.this.lambda$freeService$1$TileServices(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeService$1 */
    public /* synthetic */ void lambda$freeService$1$TileServices(TileServiceManager tileServiceManager, CustomTile customTile) {
        synchronized (this.mServices) {
            tileServiceManager.setBindAllowed(false);
            tileServiceManager.handleDestroy();
            this.mServices.remove(customTile);
            this.mTokenMap.remove(tileServiceManager.getToken());
            this.mTiles.remove(customTile.getComponent());
            this.mMainHandler.post(new Runnable(customTile.getComponent().getClassName()) {
                /* class com.android.systemui.qs.external.$$Lambda$TileServices$m2qCzd8BVbBUzSnClFn7o_chF7k */
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TileServices.this.lambda$freeService$0$TileServices(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$freeService$0 */
    public /* synthetic */ void lambda$freeService$0$TileServices(String str) {
        this.mHost.getIconController().removeAllIconsForSlot(str);
    }

    public void recalculateBindAllowance() {
        this.mExecutor.execute(new Runnable() {
            /* class com.android.systemui.qs.external.$$Lambda$TileServices$o9aPjmtcX2u4_eRkYTFvDr4_EfQ */

            public final void run() {
                TileServices.this.lambda$recalculateBindAllowance$2$TileServices();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$recalculateBindAllowance$2 */
    public /* synthetic */ void lambda$recalculateBindAllowance$2$TileServices() {
        ArrayList arrayList;
        synchronized (this.mServices) {
            arrayList = new ArrayList(this.mServices.values());
        }
        int size = arrayList.size();
        if (size > this.mMaxBound) {
            long currentTimeMillis = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                ((TileServiceManager) arrayList.get(i)).calculateBindPriority(currentTimeMillis);
            }
            Collections.sort(arrayList, SERVICE_SORT);
        }
        int i2 = 0;
        while (i2 < this.mMaxBound && i2 < size) {
            ((TileServiceManager) arrayList.get(i2)).setBindAllowed(true);
            i2++;
        }
        while (i2 < size) {
            ((TileServiceManager) arrayList.get(i2)).setBindAllowed(false);
            i2++;
        }
    }

    private void verifyCaller(CustomTile customTile) {
        try {
            if (Binder.getCallingUid() != this.mContext.getPackageManager().getPackageUidAsUser(customTile.getComponent().getPackageName(), Binder.getCallingUserHandle().getIdentifier())) {
                throw new SecurityException("Component outside caller's uid");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new SecurityException(e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestListening(ComponentName componentName) {
        this.mExecutor.execute(new Runnable(componentName) {
            /* class com.android.systemui.qs.external.$$Lambda$TileServices$zuaa8Sby4z44oxdYLlY7dlWeT20 */
            public final /* synthetic */ ComponentName f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                TileServices.this.lambda$requestListening$3$TileServices(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$requestListening$3 */
    public /* synthetic */ void lambda$requestListening$3$TileServices(ComponentName componentName) {
        synchronized (this.mServices) {
            CustomTile tileForComponent = getTileForComponent(componentName);
            if (tileForComponent == null) {
                Log.d("TileServices", "Couldn't find tile for " + componentName);
                return;
            }
            TileServiceManager tileServiceManager = this.mServices.get(tileForComponent);
            if (tileServiceManager.isActiveTile()) {
                tileServiceManager.setBindRequested(true);
                try {
                    tileServiceManager.getTileService().onStartListening();
                } catch (RemoteException unused) {
                }
            }
        }
    }

    public void updateQsTile(Tile tile, IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            this.mExecutor.execute(new Runnable(tileForToken, tile) {
                /* class com.android.systemui.qs.external.$$Lambda$TileServices$CzYr9R8a8SrXZhyiFFeFglD619w */
                public final /* synthetic */ CustomTile f$1;
                public final /* synthetic */ Tile f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    TileServices.this.lambda$updateQsTile$4$TileServices(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateQsTile$4 */
    public /* synthetic */ void lambda$updateQsTile$4$TileServices(CustomTile customTile, Tile tile) {
        synchronized (this.mServices) {
            TileServiceManager tileServiceManager = this.mServices.get(customTile);
            if (tileServiceManager != null) {
                if (tileServiceManager.isLifecycleStarted()) {
                    tileServiceManager.clearPendingBind();
                    tileServiceManager.setLastUpdate(System.currentTimeMillis());
                    customTile.updateState(tile);
                    customTile.refreshState();
                    return;
                }
            }
            Log.e("TileServices", "TileServiceManager not started for " + customTile.getComponent(), new IllegalStateException());
        }
    }

    public void onStartSuccessful(IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            this.mExecutor.execute(new Runnable(tileForToken) {
                /* class com.android.systemui.qs.external.$$Lambda$TileServices$9WKgE8lZswiSfTCYjXVfTNVb_4 */
                public final /* synthetic */ CustomTile f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    TileServices.this.lambda$onStartSuccessful$5$TileServices(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onStartSuccessful$5 */
    public /* synthetic */ void lambda$onStartSuccessful$5$TileServices(CustomTile customTile) {
        synchronized (this.mServices) {
            TileServiceManager tileServiceManager = this.mServices.get(customTile);
            if (tileServiceManager != null) {
                if (tileServiceManager.isLifecycleStarted()) {
                    tileServiceManager.clearPendingBind();
                    customTile.refreshState();
                    return;
                }
            }
            Log.e("TileServices", "TileServiceManager not started for " + customTile.getComponent(), new IllegalStateException());
        }
    }

    public void onShowDialog(IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            tileForToken.onDialogShown();
            this.mHost.forceCollapsePanels();
            this.mServices.get(tileForToken).setShowingDialog(true);
        }
    }

    public void onDialogHidden(IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            this.mServices.get(tileForToken).setShowingDialog(false);
            tileForToken.onDialogHidden();
        }
    }

    public void onStartActivity(IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            this.mHost.forceCollapsePanels();
        }
    }

    public void updateStatusIcon(IBinder iBinder, Icon icon, String str) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            this.mExecutor.execute(new Runnable(tileForToken, icon, str) {
                /* class com.android.systemui.qs.external.$$Lambda$TileServices$CIeT2k2nQroVdJV7no79sab8hg */
                public final /* synthetic */ CustomTile f$1;
                public final /* synthetic */ Icon f$2;
                public final /* synthetic */ String f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    TileServices.this.lambda$updateStatusIcon$6$TileServices(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateStatusIcon$6 */
    public /* synthetic */ void lambda$updateStatusIcon$6$TileServices(CustomTile customTile, Icon icon, String str) {
        try {
            final ComponentName component = customTile.getComponent();
            String packageName = component.getPackageName();
            UserHandle callingUserHandle = IQSService.Stub.getCallingUserHandle();
            if (this.mContext.getPackageManager().getPackageInfoAsUser(packageName, 0, callingUserHandle.getIdentifier()).applicationInfo.isSystemApp()) {
                final StatusBarIcon statusBarIcon = icon != null ? new StatusBarIcon(callingUserHandle, packageName, icon, 0, 0, str) : null;
                this.mMainHandler.post(new Runnable() {
                    /* class com.android.systemui.qs.external.TileServices.AnonymousClass1 */

                    public void run() {
                        StatusBarIconController iconController = TileServices.this.mHost.getIconController();
                        iconController.setIcon(component.getClassName(), statusBarIcon);
                        iconController.setExternalIcon(component.getClassName());
                    }
                });
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
    }

    public Tile getTile(IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken == null) {
            return null;
        }
        verifyCaller(tileForToken);
        return tileForToken.getQsTile();
    }

    public void startUnlockAndRun(IBinder iBinder) {
        CustomTile tileForToken = getTileForToken(iBinder);
        if (tileForToken != null) {
            verifyCaller(tileForToken);
            tileForToken.startUnlockAndRun();
        }
    }

    public boolean isLocked() {
        return ((KeyguardStateController) Dependency.get(KeyguardStateController.class)).isShowing();
    }

    public boolean isSecure() {
        KeyguardStateController keyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        return keyguardStateController.isMethodSecure() && keyguardStateController.isShowing();
    }

    private CustomTile getTileForToken(IBinder iBinder) {
        CustomTile customTile;
        synchronized (this.mServices) {
            customTile = this.mTokenMap.get(iBinder);
        }
        return customTile;
    }

    private CustomTile getTileForComponent(ComponentName componentName) {
        CustomTile customTile;
        synchronized (this.mServices) {
            customTile = this.mTiles.get(componentName);
        }
        return customTile;
    }
}
