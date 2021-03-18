package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.customize.CCQSIconViewImpl;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Objects;
import java.util.function.Supplier;

public class CustomTile extends QSTileImpl<QSTile.State> implements TileLifecycleManager.TileChangeListener {
    private final ComponentName mComponent;
    private Icon mDefaultIcon;
    private CharSequence mDefaultLabel;
    private boolean mIsShowingDialog;
    private boolean mIsTokenGranted;
    private boolean mListening;
    private final IQSTileService mService;
    private final TileServiceManager mServiceManager;
    private final Tile mTile;
    private final IBinder mToken = new Binder();
    private final int mUser;
    private final Context mUserContext;
    private final IWindowManager mWindowManager = WindowManagerGlobal.getWindowManagerService();

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 268;
    }

    private CustomTile(QSHost qSHost, String str, Context context) {
        super(qSHost);
        this.mComponent = ComponentName.unflattenFromString(str);
        this.mTile = new Tile();
        this.mUserContext = context;
        this.mUser = context.getUserId();
        updateDefaultTileAndIcon();
        TileServiceManager tileWrapper = qSHost.getTileServices().getTileWrapper(this);
        this.mServiceManager = tileWrapper;
        if (tileWrapper.isToggleableTile()) {
            resetStates();
        }
        this.mService = this.mServiceManager.getTileService();
        this.mServiceManager.setTileChangeListener(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public long getStaleTimeout() {
        return (((long) this.mHost.indexOf(getTileSpec())) * 60000) + 3600000;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0041 A[Catch:{ NameNotFoundException -> 0x007b }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c A[Catch:{ NameNotFoundException -> 0x007b }] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0051 A[Catch:{ NameNotFoundException -> 0x007b }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0075 A[Catch:{ NameNotFoundException -> 0x007b }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateDefaultTileAndIcon() {
        /*
        // Method dump skipped, instructions count: 128
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.external.CustomTile.updateDefaultTileAndIcon():void");
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

    @Override // com.android.systemui.qs.external.TileLifecycleManager.TileChangeListener
    public void onTileChanged(ComponentName componentName) {
        updateDefaultTileAndIcon();
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mDefaultIcon != null;
    }

    public int getUser() {
        return this.mUser;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).setComponentName(this.mComponent);
    }

    public Tile getQsTile() {
        updateDefaultTileAndIcon();
        return this.mTile;
    }

    public void updateState(Tile tile) {
        this.mTile.setIcon(tile.getIcon());
        this.mTile.setLabel(tile.getLabel());
        this.mTile.setSubtitle(tile.getSubtitle());
        this.mTile.setContentDescription(tile.getContentDescription());
        this.mTile.setStateDescription(tile.getStateDescription());
        this.mTile.setState(tile.getState());
    }

    public void onDialogShown() {
        this.mIsShowingDialog = true;
    }

    public void onDialogHidden() {
        this.mIsShowingDialog = false;
        try {
            this.mWindowManager.removeWindowToken(this.mToken, 0);
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                try {
                    updateDefaultTileAndIcon();
                    refreshState();
                    if (!this.mServiceManager.isActiveTile()) {
                        this.mServiceManager.setBindRequested(true);
                        this.mService.onStartListening();
                    }
                } catch (RemoteException unused) {
                }
            } else {
                this.mService.onStopListening();
                if (this.mIsTokenGranted && !this.mIsShowingDialog) {
                    try {
                        this.mWindowManager.removeWindowToken(this.mToken, 0);
                    } catch (RemoteException unused2) {
                    }
                    this.mIsTokenGranted = false;
                }
                this.mIsShowingDialog = false;
                this.mServiceManager.setBindRequested(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mIsTokenGranted) {
            try {
                this.mWindowManager.removeWindowToken(this.mToken, 0);
            } catch (RemoteException unused) {
            }
        }
        this.mHost.getTileServices().freeService(this, this.mServiceManager);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        TileServiceManager tileServiceManager = this.mServiceManager;
        if (tileServiceManager == null || !tileServiceManager.isToggleableTile()) {
            return new QSTile.State();
        }
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        intent.setPackage(this.mComponent.getPackageName());
        Intent resolveIntent = resolveIntent(intent);
        if (resolveIntent == null) {
            return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", this.mComponent.getPackageName(), null));
        }
        resolveIntent.putExtra("android.intent.extra.COMPONENT_NAME", this.mComponent);
        resolveIntent.putExtra("state", this.mTile.getState());
        return resolveIntent;
    }

    private Intent resolveIntent(Intent intent) {
        ResolveInfo resolveActivityAsUser = this.mContext.getPackageManager().resolveActivityAsUser(intent, 0, ActivityManager.getCurrentUser());
        if (resolveActivityAsUser == null) {
            return null;
        }
        Intent intent2 = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        ActivityInfo activityInfo = resolveActivityAsUser.activityInfo;
        return intent2.setClassName(activityInfo.packageName, activityInfo.name);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't wrap try/catch for region: R(10:3|4|5|6|7|(1:9)|10|(2:12|13)|16|18) */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:6:0x0016 */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c A[SYNTHETIC, Splitter:B:12:0x002c] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x001e A[Catch:{ RemoteException -> 0x0061 }] */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
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
            r2.addWindowToken(r3, r4, r0)     // Catch:{ RemoteException -> 0x0016 }
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
            java.lang.String r1 = r7.TAG
            java.lang.String r2 = "reflect exempt background start exception!"
            android.util.Log.e(r1, r2, r0)
        L_0x005a:
            android.service.quicksettings.IQSTileService r0 = r7.mService
            android.os.IBinder r7 = r7.mToken
            r0.onClick(r7)
        L_0x0061:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.external.CustomTile.handleClick():void");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.State state, Object obj) {
        Drawable drawable;
        int state2 = this.mTile.getState();
        boolean z = false;
        if (this.mServiceManager.hasPendingBind()) {
            state2 = 0;
        }
        state.state = state2;
        try {
            drawable = this.mTile.getIcon().loadDrawable(this.mUserContext);
        } catch (Exception unused) {
            Log.w(this.TAG, "Invalid icon, forcing into unavailable state");
            state.state = 0;
            drawable = this.mDefaultIcon.loadDrawable(this.mUserContext);
        }
        state.iconSupplier = new Supplier(drawable) {
            /* class com.android.systemui.qs.external.$$Lambda$CustomTile$OhNzDEMM2yCWnVYbU2_DKTzaqo */
            public final /* synthetic */ Drawable f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return CustomTile.lambda$handleUpdateState$0(this.f$0);
            }
        };
        state.label = this.mTile.getLabel();
        CharSequence subtitle = this.mTile.getSubtitle();
        if (subtitle == null || subtitle.length() <= 0) {
            state.secondaryLabel = null;
        } else {
            state.secondaryLabel = subtitle;
        }
        if (this.mTile.getContentDescription() != null) {
            state.contentDescription = this.mTile.getContentDescription();
        } else {
            state.contentDescription = state.label;
        }
        if (this.mTile.getStateDescription() != null) {
            state.stateDescription = this.mTile.getStateDescription();
        } else {
            state.stateDescription = null;
        }
        if (state instanceof QSTile.BooleanState) {
            state.expandedAccessibilityClassName = Switch.class.getName();
            QSTile.BooleanState booleanState = (QSTile.BooleanState) state;
            if (state.state == 2) {
                z = true;
            }
            booleanState.value = z;
        }
    }

    static /* synthetic */ QSTile.Icon lambda$handleUpdateState$0(Drawable drawable) {
        Drawable.ConstantState constantState;
        if (drawable == null || (constantState = drawable.getConstantState()) == null) {
            return null;
        }
        return new QSTileImpl.DrawableIcon(constantState.newDrawable());
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public final String getMetricsSpec() {
        return this.mComponent.getPackageName();
    }

    public void startUnlockAndRun() {
        if (QSTileImpl.mInControlCenter) {
            ((ControlPanelController) Dependency.get(ControlPanelController.class)).collapsePanel(true);
        }
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postQSRunnableDismissingKeyguard(new Runnable() {
            /* class com.android.systemui.qs.external.$$Lambda$CustomTile$q1MKWZaaapZOjYFe9CyeyabLR0Q */

            public final void run() {
                CustomTile.this.lambda$startUnlockAndRun$1$CustomTile();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUnlockAndRun$1 */
    public /* synthetic */ void lambda$startUnlockAndRun$1$CustomTile() {
        try {
            this.mService.onUnlockComplete();
        } catch (RemoteException unused) {
        }
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

    public static CustomTile create(QSHost qSHost, String str, Context context) {
        if (str == null || !str.startsWith("custom(") || !str.endsWith(")")) {
            throw new IllegalArgumentException("Bad custom tile spec: " + str);
        }
        String substring = str.substring(7, str.length() - 1);
        if (!substring.isEmpty()) {
            return new CustomTile(qSHost, substring, context);
        }
        throw new IllegalArgumentException("Empty custom tile spec action");
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public QSIconView createControlCenterTileView(Context context) {
        CCQSIconViewImpl cCQSIconViewImpl = new CCQSIconViewImpl(context);
        cCQSIconViewImpl.setIsCustomTile(true);
        return cCQSIconViewImpl;
    }
}
