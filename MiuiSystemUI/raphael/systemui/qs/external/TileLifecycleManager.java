package com.android.systemui.qs.external;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.quicksettings.IQSService;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.util.ArraySet;
import android.util.Log;
import java.util.Objects;
import java.util.Set;

public class TileLifecycleManager extends BroadcastReceiver implements IQSTileService, ServiceConnection, IBinder.DeathRecipient {
    private int mBindRetryDelay;
    private int mBindTryCount;
    /* access modifiers changed from: private */
    public boolean mBound;
    private TileChangeListener mChangeListener;
    private IBinder mClickBinder;
    private final Context mContext;
    private final Handler mHandler;
    private final Intent mIntent;
    private boolean mIsBound;
    private boolean mListening;
    private final PackageManagerAdapter mPackageManagerAdapter;
    private Set<Integer> mQueuedMessages;
    boolean mReceiverRegistered;
    private final IBinder mToken;
    private boolean mUnbindImmediate;
    private final UserHandle mUser;
    private QSTileServiceWrapper mWrapper;

    public interface TileChangeListener {
        void onTileChanged(ComponentName componentName);
    }

    public TileLifecycleManager(Handler handler, Context context, IQSService iQSService, Tile tile, Intent intent, UserHandle userHandle) {
        this(handler, context, iQSService, tile, intent, userHandle, new PackageManagerAdapter(context));
    }

    TileLifecycleManager(Handler handler, Context context, IQSService iQSService, Tile tile, Intent intent, UserHandle userHandle, PackageManagerAdapter packageManagerAdapter) {
        this.mToken = new Binder();
        this.mQueuedMessages = new ArraySet();
        this.mBindRetryDelay = 1000;
        this.mContext = context;
        this.mHandler = handler;
        this.mIntent = intent;
        this.mIntent.putExtra("service", iQSService.asBinder());
        this.mIntent.putExtra("token", this.mToken);
        this.mUser = userHandle;
        this.mPackageManagerAdapter = packageManagerAdapter;
    }

    public ComponentName getComponent() {
        return this.mIntent.getComponent();
    }

    public boolean hasPendingClick() {
        boolean contains;
        synchronized (this.mQueuedMessages) {
            contains = this.mQueuedMessages.contains(2);
        }
        return contains;
    }

    public boolean isActiveTile() {
        try {
            ServiceInfo serviceInfo = this.mPackageManagerAdapter.getServiceInfo(this.mIntent.getComponent(), 8320);
            if (serviceInfo.metaData == null || !serviceInfo.metaData.getBoolean("android.service.quicksettings.ACTIVE_TILE", false)) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public boolean isToggleableTile() {
        try {
            ServiceInfo serviceInfo = this.mPackageManagerAdapter.getServiceInfo(this.mIntent.getComponent(), 8320);
            if (serviceInfo.metaData == null || !serviceInfo.metaData.getBoolean("android.service.quicksettings.TOGGLEABLE_TILE", false)) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public void flushMessagesAndUnbind() {
        this.mUnbindImmediate = true;
        setBindService(true);
    }

    public void setBindService(boolean z) {
        if (!this.mBound || !this.mUnbindImmediate) {
            this.mBound = z;
            if (!z) {
                this.mBindTryCount = 0;
                this.mWrapper = null;
                if (this.mIsBound) {
                    this.mContext.unbindService(this);
                    this.mIsBound = false;
                }
            } else if (this.mBindTryCount == 5) {
                startPackageListening();
            } else if (checkComponentState()) {
                this.mBindTryCount++;
                try {
                    this.mIsBound = this.mContext.bindServiceAsUser(this.mIntent, this, 34603009, this.mUser);
                } catch (SecurityException e) {
                    Log.e("TileLifecycleManager", "Failed to bind to service", e);
                    this.mIsBound = false;
                }
            }
        } else {
            this.mUnbindImmediate = false;
        }
    }

    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.mBindTryCount = 0;
        QSTileServiceWrapper qSTileServiceWrapper = new QSTileServiceWrapper(IQSTileService.Stub.asInterface(iBinder));
        try {
            iBinder.linkToDeath(this, 0);
        } catch (RemoteException unused) {
        }
        this.mWrapper = qSTileServiceWrapper;
        handlePendingMessages();
    }

    public void onServiceDisconnected(ComponentName componentName) {
        handleDeath();
    }

    private void handlePendingMessages() {
        ArraySet arraySet;
        synchronized (this.mQueuedMessages) {
            arraySet = new ArraySet(this.mQueuedMessages);
            this.mQueuedMessages.clear();
        }
        if (arraySet.contains(0)) {
            onTileAdded();
        }
        if (this.mListening) {
            onStartListening();
        }
        if (arraySet.contains(2)) {
            if (!this.mListening) {
                Log.w("TileLifecycleManager", "Managed to get click on non-listening state...");
            } else {
                onClick(this.mClickBinder);
            }
        }
        if (arraySet.contains(3)) {
            if (!this.mListening) {
                Log.w("TileLifecycleManager", "Managed to get unlock on non-listening state...");
            } else {
                onUnlockComplete();
            }
        }
        if (arraySet.contains(1)) {
            if (this.mListening) {
                Log.w("TileLifecycleManager", "Managed to get remove in listening state...");
                onStopListening();
            }
            onTileRemoved();
        }
        if (this.mUnbindImmediate) {
            this.mUnbindImmediate = false;
            setBindService(false);
        }
    }

    public void handleDestroy() {
        if (this.mReceiverRegistered) {
            stopPackageListening();
        }
    }

    private void handleDeath() {
        if (this.mWrapper != null) {
            this.mWrapper = null;
            if (this.mBound && checkComponentState()) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (TileLifecycleManager.this.mBound) {
                            TileLifecycleManager.this.setBindService(true);
                        }
                    }
                }, (long) this.mBindRetryDelay);
            }
        }
    }

    private boolean checkComponentState() {
        if (isPackageAvailable() && isComponentAvailable()) {
            return true;
        }
        startPackageListening();
        return false;
    }

    private void startPackageListening() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this, this.mUser, intentFilter, (String) null, this.mHandler);
        this.mContext.registerReceiverAsUser(this, this.mUser, new IntentFilter("android.intent.action.USER_UNLOCKED"), (String) null, this.mHandler);
        this.mReceiverRegistered = true;
    }

    private void stopPackageListening() {
        this.mContext.unregisterReceiver(this);
        this.mReceiverRegistered = false;
    }

    public void setTileChangeListener(TileChangeListener tileChangeListener) {
        this.mChangeListener = tileChangeListener;
    }

    public void onReceive(Context context, Intent intent) {
        TileChangeListener tileChangeListener;
        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) || Objects.equals(intent.getData().getEncodedSchemeSpecificPart(), this.mIntent.getComponent().getPackageName())) {
            if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction()) && (tileChangeListener = this.mChangeListener) != null) {
                tileChangeListener.onTileChanged(this.mIntent.getComponent());
            }
            stopPackageListening();
            if (this.mBound) {
                setBindService(true);
            }
        }
    }

    private boolean isComponentAvailable() {
        this.mIntent.getComponent().getPackageName();
        try {
            if (this.mPackageManagerAdapter.getServiceInfo(this.mIntent.getComponent(), 0, this.mUser.getIdentifier()) != null) {
                return true;
            }
            return false;
        } catch (RemoteException unused) {
            return false;
        }
    }

    private boolean isPackageAvailable() {
        String packageName = this.mIntent.getComponent().getPackageName();
        try {
            this.mPackageManagerAdapter.getPackageInfoAsUser(packageName, 0, this.mUser.getIdentifier());
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.d("TileLifecycleManager", "Package not available: " + packageName);
            return false;
        }
    }

    private void queueMessage(int i) {
        synchronized (this.mQueuedMessages) {
            this.mQueuedMessages.add(Integer.valueOf(i));
        }
    }

    public void onTileAdded() {
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper == null || !qSTileServiceWrapper.onTileAdded()) {
            queueMessage(0);
            handleDeath();
        }
    }

    public void onTileRemoved() {
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper == null || !qSTileServiceWrapper.onTileRemoved()) {
            queueMessage(1);
            handleDeath();
        }
    }

    public void onStartListening() {
        this.mListening = true;
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper != null && !qSTileServiceWrapper.onStartListening()) {
            handleDeath();
        }
    }

    public void onStopListening() {
        this.mListening = false;
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper != null && !qSTileServiceWrapper.onStopListening()) {
            handleDeath();
        }
    }

    public void onClick(IBinder iBinder) {
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper == null || !qSTileServiceWrapper.onClick(iBinder)) {
            this.mClickBinder = iBinder;
            queueMessage(2);
            handleDeath();
        }
    }

    public void onUnlockComplete() {
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper == null || !qSTileServiceWrapper.onUnlockComplete()) {
            queueMessage(3);
            handleDeath();
        }
    }

    public IBinder asBinder() {
        QSTileServiceWrapper qSTileServiceWrapper = this.mWrapper;
        if (qSTileServiceWrapper != null) {
            return qSTileServiceWrapper.asBinder();
        }
        return null;
    }

    public void binderDied() {
        handleDeath();
    }

    public IBinder getToken() {
        return this.mToken;
    }

    public static boolean isTileAdded(Context context, ComponentName componentName) {
        return context.getSharedPreferences("tiles_prefs", 0).getBoolean(componentName.flattenToString(), false);
    }

    public static void setTileAdded(Context context, ComponentName componentName, boolean z) {
        context.getSharedPreferences("tiles_prefs", 0).edit().putBoolean(componentName.flattenToString(), z).commit();
    }
}
