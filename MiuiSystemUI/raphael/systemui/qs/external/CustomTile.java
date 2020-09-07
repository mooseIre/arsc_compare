package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileCompat;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.WindowManagerGlobal;
import android.widget.Switch;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSIconViewImpl;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.tileimpl.QSIconViewImpl;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Objects;

public class CustomTile extends QSTileImpl<QSTile.State> implements TileLifecycleManager.TileChangeListener {
    private final ComponentName mComponent;
    private Icon mDefaultIcon;
    private boolean mIsShowingDialog;
    private boolean mIsTokenGranted;
    private boolean mListening;
    /* access modifiers changed from: private */
    public final IQSTileService mService;
    private final TileServiceManager mServiceManager;
    private final Tile mTile;
    private final IBinder mToken = new Binder();
    private final int mUser;
    private final IWindowManager mWindowManager = WindowManagerGlobal.getWindowManagerService();

    public int getMetricsCategory() {
        return 268;
    }

    private CustomTile(QSTileHost qSTileHost, String str) {
        super(qSTileHost);
        this.mComponent = ComponentName.unflattenFromString(str);
        this.mTile = TileCompat.newTile(this.mComponent);
        this.mTile.setState(1);
        setTileIcon();
        this.mServiceManager = qSTileHost.getTileServices().getTileWrapper(this);
        this.mService = this.mServiceManager.getTileService();
        this.mServiceManager.setTileChangeListener(this);
        this.mUser = ActivityManager.getCurrentUser();
    }

    /* access modifiers changed from: protected */
    public long getStaleTimeout() {
        return (((long) this.mHost.indexOf(getTileSpec())) * 60000) + 3600000;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x004d A[Catch:{ Exception -> 0x0076 }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0058 A[Catch:{ Exception -> 0x0076 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005d A[Catch:{ Exception -> 0x0076 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x006c A[Catch:{ Exception -> 0x0076 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setTileIcon() {
        /*
            r6 = this;
            r0 = 0
            android.content.Context r1 = r6.mContext     // Catch:{ Exception -> 0x0076 }
            android.content.pm.PackageManager r1 = r1.getPackageManager()     // Catch:{ Exception -> 0x0076 }
            r2 = 786432(0xc0000, float:1.102026E-39)
            boolean r3 = r6.isSystemApp(r1)     // Catch:{ Exception -> 0x0076 }
            if (r3 == 0) goto L_0x0020
            android.content.ComponentName r3 = r6.mComponent     // Catch:{ Exception -> 0x0076 }
            java.lang.String r3 = r3.flattenToShortString()     // Catch:{ Exception -> 0x0076 }
            java.lang.String r4 = "com.google.android.gms/.nearby.sharing.SharingTileService"
            boolean r3 = r3.equals(r4)     // Catch:{ Exception -> 0x0076 }
            if (r3 != 0) goto L_0x0020
            r2 = 786944(0xc0200, float:1.102743E-39)
        L_0x0020:
            android.content.ComponentName r3 = r6.mComponent     // Catch:{ Exception -> 0x0076 }
            android.content.pm.ServiceInfo r2 = r1.getServiceInfo(r3, r2)     // Catch:{ Exception -> 0x0076 }
            int r3 = r2.icon     // Catch:{ Exception -> 0x0076 }
            if (r3 == 0) goto L_0x002d
            int r3 = r2.icon     // Catch:{ Exception -> 0x0076 }
            goto L_0x0031
        L_0x002d:
            android.content.pm.ApplicationInfo r3 = r2.applicationInfo     // Catch:{ Exception -> 0x0076 }
            int r3 = r3.icon     // Catch:{ Exception -> 0x0076 }
        L_0x0031:
            android.service.quicksettings.Tile r4 = r6.mTile     // Catch:{ Exception -> 0x0076 }
            android.graphics.drawable.Icon r4 = r4.getIcon()     // Catch:{ Exception -> 0x0076 }
            if (r4 == 0) goto L_0x004a
            android.service.quicksettings.Tile r4 = r6.mTile     // Catch:{ Exception -> 0x0076 }
            android.graphics.drawable.Icon r4 = r4.getIcon()     // Catch:{ Exception -> 0x0076 }
            android.graphics.drawable.Icon r5 = r6.mDefaultIcon     // Catch:{ Exception -> 0x0076 }
            boolean r4 = r6.iconEquals(r4, r5)     // Catch:{ Exception -> 0x0076 }
            if (r4 == 0) goto L_0x0048
            goto L_0x004a
        L_0x0048:
            r4 = 0
            goto L_0x004b
        L_0x004a:
            r4 = 1
        L_0x004b:
            if (r3 == 0) goto L_0x0058
            android.content.ComponentName r5 = r6.mComponent     // Catch:{ Exception -> 0x0076 }
            java.lang.String r5 = r5.getPackageName()     // Catch:{ Exception -> 0x0076 }
            android.graphics.drawable.Icon r3 = android.graphics.drawable.Icon.createWithResource(r5, r3)     // Catch:{ Exception -> 0x0076 }
            goto L_0x0059
        L_0x0058:
            r3 = r0
        L_0x0059:
            r6.mDefaultIcon = r3     // Catch:{ Exception -> 0x0076 }
            if (r4 == 0) goto L_0x0064
            android.service.quicksettings.Tile r3 = r6.mTile     // Catch:{ Exception -> 0x0076 }
            android.graphics.drawable.Icon r4 = r6.mDefaultIcon     // Catch:{ Exception -> 0x0076 }
            r3.setIcon(r4)     // Catch:{ Exception -> 0x0076 }
        L_0x0064:
            android.service.quicksettings.Tile r3 = r6.mTile     // Catch:{ Exception -> 0x0076 }
            java.lang.CharSequence r3 = r3.getLabel()     // Catch:{ Exception -> 0x0076 }
            if (r3 != 0) goto L_0x0078
            android.service.quicksettings.Tile r3 = r6.mTile     // Catch:{ Exception -> 0x0076 }
            java.lang.CharSequence r1 = r2.loadLabel(r1)     // Catch:{ Exception -> 0x0076 }
            r3.setLabel(r1)     // Catch:{ Exception -> 0x0076 }
            goto L_0x0078
        L_0x0076:
            r6.mDefaultIcon = r0
        L_0x0078:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.external.CustomTile.setTileIcon():void");
    }

    private boolean isSystemApp(PackageManager packageManager) throws PackageManager.NameNotFoundException {
        return packageManager.getApplicationInfo(this.mComponent.getPackageName(), 0).isSystemApp();
    }

    private boolean iconEquals(Icon icon, Icon icon2) {
        if (icon == icon2) {
            return true;
        }
        return icon != null && icon2 != null && icon.getType() == 2 && icon2.getType() == 2 && icon.getResId() == icon2.getResId() && Objects.equals(icon.getResPackage(), icon2.getResPackage());
    }

    public void onTileChanged(ComponentName componentName) {
        setTileIcon();
    }

    public boolean isAvailable() {
        return this.mDefaultIcon != null;
    }

    public int getUser() {
        return this.mUser;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).setComponentName(this.mComponent);
    }

    public Tile getQsTile() {
        return this.mTile;
    }

    public void updateState(Tile tile) {
        this.mTile.setIcon(tile.getIcon());
        this.mTile.setLabel(tile.getLabel());
        this.mTile.setContentDescription(tile.getContentDescription());
        this.mTile.setState(tile.getState());
    }

    public void onDialogShown() {
        this.mIsShowingDialog = true;
    }

    public void onDialogHidden() {
        this.mIsShowingDialog = false;
        try {
            IWindowManagerCompat.removeWindowToken(this.mWindowManager, this.mToken, 0);
        } catch (RemoteException unused) {
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(4:13|14|15|16) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0038 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleSetListening(boolean r3) {
        /*
            r2 = this;
            boolean r0 = r2.mListening
            if (r0 != r3) goto L_0x0005
            return
        L_0x0005:
            r2.mListening = r3
            if (r3 == 0) goto L_0x0023
            r2.setTileIcon()     // Catch:{ RemoteException -> 0x0041 }
            r2.refreshState()     // Catch:{ RemoteException -> 0x0041 }
            com.android.systemui.qs.external.TileServiceManager r3 = r2.mServiceManager     // Catch:{ RemoteException -> 0x0041 }
            boolean r3 = r3.isActiveTile()     // Catch:{ RemoteException -> 0x0041 }
            if (r3 != 0) goto L_0x0041
            com.android.systemui.qs.external.TileServiceManager r3 = r2.mServiceManager     // Catch:{ RemoteException -> 0x0041 }
            r0 = 1
            r3.setBindRequested(r0)     // Catch:{ RemoteException -> 0x0041 }
            android.service.quicksettings.IQSTileService r2 = r2.mService     // Catch:{ RemoteException -> 0x0041 }
            r2.onStartListening()     // Catch:{ RemoteException -> 0x0041 }
            goto L_0x0041
        L_0x0023:
            android.service.quicksettings.IQSTileService r3 = r2.mService     // Catch:{ RemoteException -> 0x0041 }
            r3.onStopListening()     // Catch:{ RemoteException -> 0x0041 }
            boolean r3 = r2.mIsTokenGranted     // Catch:{ RemoteException -> 0x0041 }
            r0 = 0
            if (r3 == 0) goto L_0x003a
            boolean r3 = r2.mIsShowingDialog     // Catch:{ RemoteException -> 0x0041 }
            if (r3 != 0) goto L_0x003a
            android.view.IWindowManager r3 = r2.mWindowManager     // Catch:{ RemoteException -> 0x0038 }
            android.os.IBinder r1 = r2.mToken     // Catch:{ RemoteException -> 0x0038 }
            android.view.IWindowManagerCompat.removeWindowToken(r3, r1, r0)     // Catch:{ RemoteException -> 0x0038 }
        L_0x0038:
            r2.mIsTokenGranted = r0     // Catch:{ RemoteException -> 0x0041 }
        L_0x003a:
            r2.mIsShowingDialog = r0     // Catch:{ RemoteException -> 0x0041 }
            com.android.systemui.qs.external.TileServiceManager r2 = r2.mServiceManager     // Catch:{ RemoteException -> 0x0041 }
            r2.setBindRequested(r0)     // Catch:{ RemoteException -> 0x0041 }
        L_0x0041:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.external.CustomTile.handleSetListening(boolean):void");
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mIsTokenGranted) {
            try {
                IWindowManagerCompat.removeWindowToken(this.mWindowManager, this.mToken, 0);
            } catch (RemoteException unused) {
            }
        }
        this.mHost.getTileServices().freeService(this, this.mServiceManager);
    }

    public QSTile.State newTileState() {
        TileServiceManager tileServiceManager = this.mServiceManager;
        if (tileServiceManager == null || !tileServiceManager.isToggleableTile()) {
            return new QSTile.State();
        }
        return new QSTile.BooleanState();
    }

    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        intent.setPackage(this.mComponent.getPackageName());
        Intent resolveIntent = resolveIntent(intent);
        if (resolveIntent == null) {
            return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", this.mComponent.getPackageName(), (String) null));
        }
        resolveIntent.putExtra("android.intent.extra.COMPONENT_NAME", this.mComponent);
        resolveIntent.putExtra("state", this.mTile.getState());
        return resolveIntent;
    }

    private Intent resolveIntent(Intent intent) {
        ResolveInfo resolveActivityAsUser = this.mContext.getPackageManager().resolveActivityAsUser(intent, 0, KeyguardUpdateMonitor.getCurrentUser());
        if (resolveActivityAsUser != null) {
            return new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES").setClassName(resolveActivityAsUser.activityInfo.packageName, resolveActivityAsUser.activityInfo.name);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't wrap try/catch for region: R(10:3|4|5|6|7|(1:9)|10|(2:12|13)|17|19) */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:6:0x0016 */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c A[SYNTHETIC, Splitter:B:12:0x002c] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x001e A[Catch:{ RemoteException -> 0x0061 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleClick() {
        /*
            r7 = this;
            android.service.quicksettings.Tile r0 = r7.mTile
            int r0 = r0.getState()
            if (r0 != 0) goto L_0x0009
            return
        L_0x0009:
            r0 = 0
            r1 = 1
            android.view.IWindowManager r2 = r7.mWindowManager     // Catch:{ RemoteException -> 0x0016 }
            android.os.IBinder r3 = r7.mToken     // Catch:{ RemoteException -> 0x0016 }
            r4 = 2035(0x7f3, float:2.852E-42)
            android.view.IWindowManagerCompat.addWindowToken(r2, r3, r4, r0)     // Catch:{ RemoteException -> 0x0016 }
            r7.mIsTokenGranted = r1     // Catch:{ RemoteException -> 0x0016 }
        L_0x0016:
            com.android.systemui.qs.external.TileServiceManager r2 = r7.mServiceManager     // Catch:{ RemoteException -> 0x0061 }
            boolean r2 = r2.isActiveTile()     // Catch:{ RemoteException -> 0x0061 }
            if (r2 == 0) goto L_0x0028
            com.android.systemui.qs.external.TileServiceManager r2 = r7.mServiceManager     // Catch:{ RemoteException -> 0x0061 }
            r2.setBindRequested(r1)     // Catch:{ RemoteException -> 0x0061 }
            android.service.quicksettings.IQSTileService r2 = r7.mService     // Catch:{ RemoteException -> 0x0061 }
            r2.onStartListening()     // Catch:{ RemoteException -> 0x0061 }
        L_0x0028:
            android.content.ComponentName r2 = r7.mComponent     // Catch:{ RemoteException -> 0x0061 }
            if (r2 == 0) goto L_0x005a
            android.content.Context r2 = r7.mContext     // Catch:{ Exception -> 0x0052 }
            java.lang.String r3 = "security"
            java.lang.Object r2 = r2.getSystemService(r3)     // Catch:{ Exception -> 0x0052 }
            java.lang.Class r3 = r2.getClass()     // Catch:{ Exception -> 0x0052 }
            java.lang.String r4 = "exemptTemporarily"
            java.lang.Class[] r5 = new java.lang.Class[r1]     // Catch:{ Exception -> 0x0052 }
            java.lang.Class<java.lang.String> r6 = java.lang.String.class
            r5[r0] = r6     // Catch:{ Exception -> 0x0052 }
            java.lang.reflect.Method r3 = r3.getDeclaredMethod(r4, r5)     // Catch:{ Exception -> 0x0052 }
            java.lang.Object[] r1 = new java.lang.Object[r1]     // Catch:{ Exception -> 0x0052 }
            android.content.ComponentName r4 = r7.mComponent     // Catch:{ Exception -> 0x0052 }
            java.lang.String r4 = r4.getPackageName()     // Catch:{ Exception -> 0x0052 }
            r1[r0] = r4     // Catch:{ Exception -> 0x0052 }
            r3.invoke(r2, r1)     // Catch:{ Exception -> 0x0052 }
            goto L_0x005a
        L_0x0052:
            r0 = move-exception
            java.lang.String r1 = r7.TAG     // Catch:{ RemoteException -> 0x0061 }
            java.lang.String r2 = "reflect exempt background start exception!"
            android.util.Log.e(r1, r2, r0)     // Catch:{ RemoteException -> 0x0061 }
        L_0x005a:
            android.service.quicksettings.IQSTileService r0 = r7.mService     // Catch:{ RemoteException -> 0x0061 }
            android.os.IBinder r7 = r7.mToken     // Catch:{ RemoteException -> 0x0061 }
            r0.onClick(r7)     // Catch:{ RemoteException -> 0x0061 }
        L_0x0061:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.external.CustomTile.handleClick():void");
    }

    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.State state, Object obj) {
        Drawable drawable;
        int state2 = this.mTile.getState();
        if (this.mServiceManager.hasPendingBind()) {
            state2 = 0;
        }
        state.state = state2;
        try {
            drawable = this.mTile.getIcon().loadDrawable(this.mContext);
        } catch (Exception unused) {
            Log.w(this.TAG, "Invalid icon, forcing into unavailable state");
            state.state = 0;
            drawable = this.mDefaultIcon.loadDrawable(this.mContext);
        }
        state.icon = new QSTileImpl.DrawableIcon(drawable);
        state.label = this.mTile.getLabel();
        if (this.mTile.getContentDescription() != null) {
            state.contentDescription = this.mTile.getContentDescription();
        } else {
            state.contentDescription = state.label;
        }
        state.expandedAccessibilityClassName = Switch.class.getName();
    }

    public void startUnlockAndRun() {
        if (this.mInControlCenter) {
            ((ControlPanelController) Dependency.get(ControlPanelController.class)).collapsePanel(true);
        }
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() {
            public void run() {
                try {
                    CustomTile.this.mService.onUnlockComplete();
                } catch (RemoteException unused) {
                }
            }
        });
    }

    public static String toSpec(ComponentName componentName) {
        return "custom(" + componentName.flattenToShortString() + ")";
    }

    public static ComponentName getComponentFromSpec(String str) {
        String substring = str.substring(7, str.length() - 1);
        if (!substring.isEmpty()) {
            return ComponentName.unflattenFromString(substring);
        }
        throw new IllegalArgumentException("Empty custom tile spec action");
    }

    public static CustomTile create(QSTileHost qSTileHost, String str) {
        if (str == null || !str.startsWith("custom(") || !str.endsWith(")")) {
            throw new IllegalArgumentException("Bad custom tile spec: " + str);
        }
        String substring = str.substring(7, str.length() - 1);
        if (!substring.isEmpty()) {
            return new CustomTile(qSTileHost, substring);
        }
        throw new IllegalArgumentException("Empty custom tile spec action");
    }

    public QSIconView createTileView(Context context) {
        QSIconViewImpl qSIconViewImpl = new QSIconViewImpl(context);
        qSIconViewImpl.setIsCustomTile(true);
        return qSIconViewImpl;
    }

    public QSIconView createControlCenterTileView(Context context) {
        CCQSIconViewImpl cCQSIconViewImpl = new CCQSIconViewImpl(context);
        cCQSIconViewImpl.setIsCustomTile(true);
        return cCQSIconViewImpl;
    }
}
