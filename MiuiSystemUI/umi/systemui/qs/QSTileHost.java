package com.android.systemui.qs;

import android.app.MiuiStatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.TileCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Util;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.miui.controlcenter.QSControlTileHost;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.statusbar.policy.OldModeController;
import com.android.systemui.miui.statusbar.policy.SuperSaveModeController;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.miui.qs.MiuiQSTilePlugin;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.proxy.UserManager;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.tileimpl.QSFactoryImpl;
import com.android.systemui.qs.tiles.DriveModeTile;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QSTileHost implements QSHost, TunerService.Tunable, PluginListener<QSFactory>, Dumpable, SuperSaveModeController.SuperSaveModeChangeListener, OldModeController.OldModeChangeListener {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Constants.DEBUG;
    private String MIUI_QS_TILES_EDITED;
    private String MIUI_QS_TILES_SHARED_PREFERENCE;
    private int NORMAL_MIN_SIZE_ONE_SCREEN;
    private int OLD_MODE_MIN_TILE_ONE_SCREEN;
    private int SUPER_SAVE_MIN_TILE_ONE_SCREEN;
    /* access modifiers changed from: private */
    public String TAG;
    private String TILES_OLD_MODE;
    private String TILES_SUPER_SAVE;
    /* access modifiers changed from: private */
    public final List<QSHost.Callback> mCallbacks;
    /* access modifiers changed from: private */
    public boolean mCollpaseAfterClick;
    private ContentObserver mContentObserver;
    /* access modifiers changed from: private */
    public final Context mContext;
    protected List<String> mControlIndependentTiles;
    private final ControlPanelController mControlPanelController;
    protected boolean mControlTileHost;
    private int mCurrentUser;
    private boolean mEdited;
    private boolean mForceTileDestroy;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public volatile boolean mHasDriveApp;
    private final StatusBarIconController mIconController;
    private int mMinTilesInOneScreen;
    /* access modifiers changed from: private */
    public SharedPreferences mMiuiQSTilesSharedPreferences;
    public MiuiQSTilePlugin mMiuiQSTilsplugin;
    private boolean mOldModeOn;
    private BroadcastReceiver mPackageChangeReceiver;
    protected String mPluginDefaultTiles;
    protected String mPluginStockTiles;
    protected String mQsDefaultTiles;
    private final ArrayList<QSFactory> mQsFactories;
    private QSFactoryImpl mQsFactoryImpl;
    protected String mQsStockTiles;
    private PluginListener mQsTilePluginListener;
    private TileServices mServices;
    private final StatusBar mStatusBar;
    private boolean mSuperSaveModeOn;
    private String mTileListKey;
    protected final ArrayList<String> mTileSpecs;
    protected final LinkedHashMap<String, QSTile> mTiles;
    /* access modifiers changed from: private */
    public List<String> mUpdateTiles;
    private BroadcastReceiver mUpdateVersionReceiver;
    private BroadcastReceiver mUserSwitchReceiver;

    public void warn(String str, Throwable th) {
    }

    public QSTileHost(Context context, StatusBar statusBar, StatusBarIconController statusBarIconController) {
        this.TAG = "QSTileHost";
        this.TILES_SUPER_SAVE = "sysui_qs_super_save_tiles";
        this.TILES_OLD_MODE = "sysui_qs_old_mode_tiles";
        this.MIUI_QS_TILES_SHARED_PREFERENCE = "miuiQSTiles";
        this.MIUI_QS_TILES_EDITED = "edited";
        this.NORMAL_MIN_SIZE_ONE_SCREEN = 12;
        this.SUPER_SAVE_MIN_TILE_ONE_SCREEN = 5;
        this.OLD_MODE_MIN_TILE_ONE_SCREEN = 7;
        this.mTiles = new LinkedHashMap<>();
        this.mTileSpecs = new ArrayList<>();
        this.mCallbacks = new ArrayList();
        this.mQsFactories = new ArrayList<>();
        this.mQsDefaultTiles = "";
        this.mQsStockTiles = "";
        this.mPluginStockTiles = "";
        this.mPluginDefaultTiles = "";
        this.mHasDriveApp = false;
        this.mCollpaseAfterClick = false;
        this.mTileListKey = "sysui_qs_tiles";
        this.mMinTilesInOneScreen = this.NORMAL_MIN_SIZE_ONE_SCREEN;
        this.mContentObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                QSTileHost qSTileHost = QSTileHost.this;
                boolean unused = qSTileHost.mCollpaseAfterClick = MiuiStatusBarManager.isCollapseAfterClickedForUser(qSTileHost.mContext, -2);
            }
        };
        this.mPackageChangeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    if (!QSTileHost.this.mHasDriveApp && "com.xiaomi.drivemode".equals(intent.getData().getSchemeSpecificPart())) {
                        boolean unused = QSTileHost.this.mHasDriveApp = true;
                    }
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && QSTileHost.this.mHasDriveApp && intent.getData().getSchemeSpecificPart().equals("com.xiaomi.drivemode")) {
                    boolean unused2 = QSTileHost.this.mHasDriveApp = false;
                    DriveModeTile.leaveDriveMode(QSTileHost.this.getContext());
                }
            }
        };
        this.mUpdateVersionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (QSTileHost.this.needUpdateSharedPreferences() && QSTileHost.this.mUpdateTiles != null && QSTileHost.this.mUpdateTiles.size() != 0) {
                    String stringForUser = Settings.Secure.getStringForUser(QSTileHost.this.mContext.getContentResolver(), "sysui_qs_tiles", KeyguardUpdateMonitor.getCurrentUser());
                    QSTileHost qSTileHost = QSTileHost.this;
                    List<String> loadTileSpecs = qSTileHost.loadTileSpecs(qSTileHost.mContext, stringForUser);
                    SharedPreferences.Editor edit = QSTileHost.this.mMiuiQSTilesSharedPreferences.edit();
                    for (int i = 0; i < QSTileHost.this.mUpdateTiles.size(); i++) {
                        String str = (String) QSTileHost.this.mUpdateTiles.get(i);
                        if (loadTileSpecs.contains(str)) {
                            edit.putBoolean(str, true);
                        } else {
                            loadTileSpecs.add(loadTileSpecs.size() - 1, str);
                            edit.putBoolean(str, true);
                        }
                    }
                    edit.apply();
                    QSTileHost.this.addIndependentTiles("sysui_qs_tiles", loadTileSpecs);
                    Settings.Secure.putStringForUser(QSTileHost.this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", loadTileSpecs), KeyguardUpdateMonitor.getCurrentUser());
                }
            }
        };
        this.mUserSwitchReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean unused = QSTileHost.this.mCollpaseAfterClick = MiuiStatusBarManager.isCollapseAfterClickedForUser(context, -2);
            }
        };
        this.mQsTilePluginListener = new PluginListener<MiuiQSTilePlugin>() {
            public void onPluginConnected(MiuiQSTilePlugin miuiQSTilePlugin, Context context) {
                QSTileHost qSTileHost = QSTileHost.this;
                qSTileHost.mMiuiQSTilsplugin = miuiQSTilePlugin;
                qSTileHost.mPluginStockTiles = miuiQSTilePlugin.getStockTileWithOrder();
                QSTileHost.this.mPluginDefaultTiles = miuiQSTilePlugin.getDefaultTileWithOrder();
                QSTileHost qSTileHost2 = QSTileHost.this;
                qSTileHost2.mQsStockTiles = TextUtils.isEmpty(qSTileHost2.mPluginStockTiles) ? QSTileHost.this.mQsStockTiles : QSTileHost.this.mPluginStockTiles;
                QSTileHost qSTileHost3 = QSTileHost.this;
                qSTileHost3.mQsDefaultTiles = TextUtils.isEmpty(qSTileHost3.mPluginDefaultTiles) ? QSTileHost.this.mQsDefaultTiles : QSTileHost.this.mPluginDefaultTiles;
                QSTileHost.this.filterIndependentTiles();
                QSTileHost.this.onTuningChanged();
            }

            public void onPluginDisconnected(MiuiQSTilePlugin miuiQSTilePlugin) {
                QSTileHost qSTileHost = QSTileHost.this;
                qSTileHost.mMiuiQSTilsplugin = null;
                qSTileHost.initQSTiles(qSTileHost.mContext);
                QSTileHost.this.onTuningChanged();
            }
        };
        this.mIconController = statusBarIconController;
        this.mControlPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        this.mContext = context;
        this.mStatusBar = statusBar;
        this.mControlIndependentTiles = new ArrayList();
    }

    public QSTileHost(Context context, StatusBar statusBar, StatusBarIconController statusBarIconController, boolean z) {
        this(context, statusBar, statusBarIconController);
        this.mControlTileHost = true;
    }

    public void init() {
        Class cls = OldModeController.class;
        Class cls2 = SuperSaveModeController.class;
        Class cls3 = TunerService.class;
        if (this.mControlTileHost) {
            this.NORMAL_MIN_SIZE_ONE_SCREEN = 8;
            this.SUPER_SAVE_MIN_TILE_ONE_SCREEN = 4;
            this.mMinTilesInOneScreen = 8;
        }
        if (Constants.IS_INTERNATIONAL) {
            this.mControlIndependentTiles.addAll(Arrays.asList(this.mContext.getResources().getStringArray(R.array.qs_control_independent_tiles_global)));
        } else {
            this.mControlIndependentTiles.addAll(Arrays.asList(this.mContext.getResources().getStringArray(R.array.qs_control_independent_tiles)));
        }
        this.mMiuiQSTilesSharedPreferences = this.mContext.getSharedPreferences(this.MIUI_QS_TILES_SHARED_PREFERENCE, 0);
        this.mUpdateTiles = new ArrayList(Arrays.asList(this.mContext.getResources().getStringArray(R.array.promote_tiles_on_update_successed)));
        this.mServices = new TileServices(this, (Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mEdited = this.mMiuiQSTilesSharedPreferences.getBoolean(this.MIUI_QS_TILES_EDITED, false);
        this.mQsFactories.add(new QSFactoryImpl(this));
        this.mSuperSaveModeOn = ((SuperSaveModeController) Dependency.get(cls2)).isActive();
        this.mOldModeOn = ((OldModeController) Dependency.get(cls)).isActive();
        changeTileListKey();
        initQSTiles(this.mContext);
        QSFactoryImpl qSFactoryImpl = new QSFactoryImpl(this);
        this.mQsFactoryImpl = qSFactoryImpl;
        this.mQsFactories.add(qSFactoryImpl);
        addPluginListeners();
        ((TunerService) Dependency.get(cls3)).addTunable(this, "sysui_qs_tiles");
        ((TunerService) Dependency.get(cls3)).addTunable(this, this.TILES_SUPER_SAVE);
        ((TunerService) Dependency.get(cls3)).addTunable(this, this.TILES_OLD_MODE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mPackageChangeReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mUserSwitchReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("com.android.updater.action.UPDATE_SUCCESSED");
        this.mContext.registerReceiver(this.mUpdateVersionReceiver, intentFilter3);
        new Thread() {
            public void run() {
                try {
                    QSTileHost qSTileHost = QSTileHost.this;
                    boolean z = false;
                    if (QSTileHost.this.mContext.getPackageManager().getApplicationInfo("com.xiaomi.drivemode", 0) != null) {
                        z = true;
                    }
                    boolean unused = qSTileHost.mHasDriveApp = z;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        this.mHandler = new Handler();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_collapse_after_clicked"), false, this.mContentObserver, -1);
        this.mContentObserver.onChange(false);
        ((SuperSaveModeController) Dependency.get(cls2)).addCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
        ((OldModeController) Dependency.get(cls)).addCallback((OldModeController.OldModeChangeListener) this);
        ((DumpManager) Dependency.get(DumpManager.class)).registerDumpable(this.TAG, this);
    }

    public boolean isDriveModeInstalled() {
        return this.mHasDriveApp;
    }

    public StatusBarIconController getIconController() {
        return this.mIconController;
    }

    public void destroy() {
        for (QSTile destroy : this.mTiles.values()) {
            destroy.destroy();
        }
        this.mTiles.clear();
        this.mTileSpecs.clear();
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        this.mServices.destroy();
        this.mServices = null;
        removePluginListeners();
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).removeCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
        ((OldModeController) Dependency.get(OldModeController.class)).removeCallback((OldModeController.OldModeChangeListener) this);
        ((DumpManager) Dependency.get(DumpManager.class)).unRegisterDumpable(this.TAG);
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        this.mContext.unregisterReceiver(this.mPackageChangeReceiver);
        this.mContext.unregisterReceiver(this.mUpdateVersionReceiver);
        this.mContext.unregisterReceiver(this.mUserSwitchReceiver);
    }

    public void setMiuiQSTilesEdited() {
        if (!this.mEdited) {
            SharedPreferences.Editor edit = this.mMiuiQSTilesSharedPreferences.edit();
            edit.putBoolean(this.MIUI_QS_TILES_EDITED, true);
            edit.apply();
            this.mEdited = true;
        }
    }

    public void onPluginConnected(QSFactory qSFactory, Context context) {
        this.mQsFactories.add(0, qSFactory);
        Log.d(this.TAG, "onPluginConnected: force remove and recreate of all tiles.");
        onTuningChanged();
    }

    public void onPluginDisconnected(QSFactory qSFactory) {
        this.mQsFactories.remove(qSFactory);
        Log.d(this.TAG, "onPluginDisconnected: force remove and recreate of all tiles.");
        onTuningChanged();
    }

    public void addCallback(QSHost.Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(QSHost.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public Collection<QSTile> getTiles() {
        return this.mTiles.values();
    }

    public QSTile getTile(String str) {
        return this.mTiles.get(str);
    }

    public void collapsePanels() {
        if (this.mStatusBar.getBarState() == 1) {
            if (this.mControlTileHost) {
                this.mControlPanelController.collapsePanel(true);
            }
            this.mStatusBar.closeQs();
            return;
        }
        if (this.mControlTileHost) {
            this.mControlPanelController.collapsePanel(true);
        }
        if (!this.mStatusBar.isQSFullyCollapsed()) {
            this.mStatusBar.postAnimateCollapsePanels();
        }
    }

    public boolean isQSFullyCollapsed() {
        return this.mControlTileHost ? this.mControlPanelController.isQSFullyCollapsed() : this.mStatusBar.isQSFullyCollapsed();
    }

    public boolean collapseAfterClick() {
        return Build.VERSION.SDK_INT < 26 && this.mCollpaseAfterClick;
    }

    public void forceCollapsePanels() {
        if (this.mControlTileHost) {
            this.mControlPanelController.collapsePanel(true);
        } else {
            this.mStatusBar.postAnimateForceCollapsePanels();
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public int getBarState() {
        return this.mStatusBar.getBarState();
    }

    public TileServices getTileServices() {
        return this.mServices;
    }

    public int indexOf(String str) {
        return this.mTileSpecs.indexOf(str);
    }

    public void initQSTiles(Context context) {
        this.mQsDefaultTiles = context.getString(R.string.quick_settings_tiles_default);
        this.mQsStockTiles = context.getString(R.string.quick_settings_tiles_stock);
        filterIndependentTiles();
    }

    public void filterIndependentTiles() {
        if (this.mControlTileHost) {
            for (String next : this.mControlIndependentTiles) {
                String str = this.mQsStockTiles;
                this.mQsStockTiles = str.replace(next + ",", "");
                String str2 = this.mQsDefaultTiles;
                this.mQsDefaultTiles = str2.replace(next + ",", "");
                String str3 = this.mPluginStockTiles;
                this.mPluginStockTiles = str3.replace(next + ",", "");
                String str4 = this.mPluginDefaultTiles;
                this.mPluginDefaultTiles = str4.replace(next + ",", "");
            }
        }
    }

    public void filterArrayIndependentTiles(ArrayList<String> arrayList) {
        if (arrayList != null && this.mControlTileHost) {
            for (String remove : this.mControlIndependentTiles) {
                arrayList.remove(remove);
            }
        }
    }

    /* access modifiers changed from: private */
    public void addIndependentTiles(String str, List<String> list) {
        boolean z;
        if (this.mControlTileHost && list != null && list.size() != 0 && str != null && str.equals("sysui_qs_tiles") && this.mControlTileHost) {
            Iterator<String> it = this.mControlIndependentTiles.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (list.contains(it.next())) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            if (!z) {
                for (int size = this.mControlIndependentTiles.size() - 1; size >= 0; size--) {
                    list.add(0, this.mControlIndependentTiles.get(size));
                }
            }
        }
    }

    public String getQsDefaultTiles() {
        if (this.mSuperSaveModeOn) {
            if (this.mControlTileHost) {
                return this.mContext.getResources().getString(R.string.control_quick_settings_tiles_super_save);
            }
            return this.mContext.getResources().getString(R.string.quick_settings_tiles_super_save);
        } else if (this.mOldModeOn && !this.mControlTileHost) {
            return this.mContext.getResources().getString(R.string.quick_settings_tiles_old_mode);
        } else {
            if (this.mMiuiQSTilsplugin == null || TextUtils.isEmpty(this.mPluginDefaultTiles)) {
                return this.mQsDefaultTiles;
            }
            return this.mMiuiQSTilsplugin.getDefaultTileWithOrder();
        }
    }

    public String getQsStockTiles() {
        if (this.mMiuiQSTilsplugin == null || TextUtils.isEmpty(this.mPluginStockTiles)) {
            return this.mQsStockTiles;
        }
        return this.mPluginStockTiles;
    }

    private void changeTileListKey() {
        if (this.mSuperSaveModeOn) {
            this.mTileListKey = this.TILES_SUPER_SAVE;
            this.mMinTilesInOneScreen = this.SUPER_SAVE_MIN_TILE_ONE_SCREEN;
        } else if (!this.mOldModeOn || this.mControlTileHost) {
            this.mTileListKey = "sysui_qs_tiles";
            this.mMinTilesInOneScreen = this.NORMAL_MIN_SIZE_ONE_SCREEN;
        } else {
            this.mTileListKey = this.TILES_OLD_MODE;
            this.mMinTilesInOneScreen = this.OLD_MODE_MIN_TILE_ONE_SCREEN;
        }
    }

    private void updateTileListKey() {
        String str = this.mTileListKey;
        changeTileListKey();
        if (!TextUtils.equals(str, this.mTileListKey)) {
            onTuningChanged();
        }
    }

    private boolean isValidTileListKey(String str) {
        return this.mTileListKey.equals(str);
    }

    private String getTileListValue() {
        Class cls = TunerService.class;
        if (this.mTileListKey.equals(this.TILES_SUPER_SAVE)) {
            if (this.mControlTileHost) {
                return this.mContext.getResources().getString(R.string.control_quick_settings_tiles_super_save);
            }
            return this.mContext.getResources().getString(R.string.quick_settings_tiles_super_save);
        } else if (this.mTileListKey.equals(this.TILES_OLD_MODE) && !this.mControlTileHost) {
            String value = ((TunerService) Dependency.get(cls)).getValue(this.TILES_OLD_MODE);
            return TextUtils.isEmpty(value) ? this.mContext.getResources().getString(R.string.quick_settings_tiles_old_mode) : value;
        } else if (!this.mTileListKey.equals("sysui_qs_tiles")) {
            return this.mQsDefaultTiles;
        } else {
            if (this.mEdited) {
                return ((TunerService) Dependency.get(cls)).getValue("sysui_qs_tiles");
            }
            return this.mQsDefaultTiles;
        }
    }

    /* access modifiers changed from: private */
    public boolean needUpdateSharedPreferences() {
        List<String> list = this.mUpdateTiles;
        if (!(list == null || list.size() == 0)) {
            for (String str : this.mUpdateTiles) {
                if (!this.mMiuiQSTilesSharedPreferences.getBoolean(str, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    public MiuiQSTilePlugin getMiuiQSTilePlugin() {
        return this.mMiuiQSTilsplugin;
    }

    /* access modifiers changed from: protected */
    public void addPluginListeners() {
        Class cls = PluginManager.class;
        ((PluginManager) Dependency.get(cls)).addPluginListener(this, (Class<?>) QSFactory.class, true);
        ((PluginManager) Dependency.get(cls)).addPluginListener(this.mQsTilePluginListener, (Class<?>) MiuiQSTilePlugin.class, true);
    }

    /* access modifiers changed from: protected */
    public void removePluginListeners() {
        Class cls = PluginManager.class;
        ((PluginManager) Dependency.get(cls)).removePluginListener(this);
        ((PluginManager) Dependency.get(cls)).removePluginListener(this.mQsTilePluginListener);
    }

    /* access modifiers changed from: private */
    public void onTuningChanged() {
        onTuningChanged(this.mTileListKey, "");
        onTuningChanged(this.mTileListKey, getTileListValue());
    }

    public void onTuningChanged(String str, String str2) {
        boolean z;
        if (!isValidTileListKey(str)) {
            String str3 = this.TAG;
            Slog.d(str3, "onTuningChanged: other key: " + str);
            return;
        }
        if (str2 == null && UserManager.isDeviceInDemoMode(this.mContext)) {
            str2 = this.mContext.getResources().getString(R.string.quick_settings_tiles_retail_mode);
        }
        final List<String> loadTileSpecs = loadTileSpecs(this.mContext, str2);
        if (DEBUG) {
            String str4 = this.TAG;
            Slog.d(str4, "onTuningChanged: recreating tiles: newValue: " + str2 + ", tileSpecs: " + loadTileSpecs);
        }
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (!loadTileSpecs.equals(this.mTileSpecs) || currentUser != this.mCurrentUser) {
            for (Map.Entry next : this.mTiles.entrySet()) {
                if (this.mForceTileDestroy || !loadTileSpecs.contains(next.getKey())) {
                    if (DEBUG) {
                        String str5 = this.TAG;
                        Log.d(str5, "Destroying tile: " + ((String) next.getKey()));
                    }
                    ((QSTile) next.getValue()).destroy();
                }
            }
            if (this.mForceTileDestroy) {
                this.mTiles.clear();
            }
            int i = 0;
            this.mForceTileDestroy = false;
            final LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (String next2 : loadTileSpecs) {
                QSTile qSTile = this.mTiles.get(next2);
                if (qSTile == null || (z && ((CustomTile) qSTile).getUser() != currentUser)) {
                    if (DEBUG) {
                        String str6 = this.TAG;
                        Log.d(str6, "Creating tile: " + next2);
                    }
                    if (qSTile != null) {
                        qSTile.destroy();
                    }
                    try {
                        QSTile createTile = createTile(next2);
                        if (createTile != null) {
                            if (createTile.isAvailable()) {
                                createTile.setTileSpec(next2);
                                linkedHashMap.put(next2, createTile);
                            } else {
                                String str7 = this.TAG;
                                Slog.d(str7, "onTuningChanged: unavailable custom tile: " + createTile);
                                createTile.destroy();
                            }
                        }
                    } catch (Throwable th) {
                        String str8 = this.TAG;
                        Slog.w(str8, "onTuningChanged: Error creating tile for spec: " + next2, th);
                    }
                } else if (qSTile.isAvailable()) {
                    if (DEBUG) {
                        String str9 = this.TAG;
                        Log.d(str9, "Adding " + qSTile);
                    }
                    qSTile.removeCallbacks();
                    if (!((z = qSTile instanceof CustomTile)) && this.mCurrentUser != currentUser) {
                        qSTile.userSwitch(currentUser);
                    }
                    linkedHashMap.put(next2, qSTile);
                } else {
                    String str10 = this.TAG;
                    Slog.d(str10, "onTuningChanged: unavailable tile: " + qSTile);
                    qSTile.destroy();
                }
            }
            this.mCurrentUser = currentUser;
            final ArrayList arrayList = new ArrayList(this.mTileSpecs);
            this.mTileSpecs.clear();
            this.mTileSpecs.addAll(loadTileSpecs);
            this.mTiles.clear();
            this.mTiles.putAll(linkedHashMap);
            if (Build.VERSION.SDK_INT <= 28) {
                if (Util.isMiuiOptimizationDisabled()) {
                    i = 200;
                }
                if (this.mHandler == null) {
                    this.mHandler = new Handler();
                }
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        for (int i = 0; i < QSTileHost.this.mCallbacks.size(); i++) {
                            ((QSHost.Callback) QSTileHost.this.mCallbacks.get(i)).onTilesChanged();
                        }
                    }
                }, (long) i);
                return;
            }
            if (this.mHandler == null) {
                this.mHandler = new Handler();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (!linkedHashMap.isEmpty() || loadTileSpecs.isEmpty()) {
                        for (int i = 0; i < QSTileHost.this.mCallbacks.size(); i++) {
                            ((QSHost.Callback) QSTileHost.this.mCallbacks.get(i)).onTilesChanged();
                        }
                        return;
                    }
                    if (QSTileHost.DEBUG) {
                        Log.d(QSTileHost.this.TAG, "No valid tiles on tuning changed. Setting to default.");
                    }
                    QSTileHost qSTileHost = QSTileHost.this;
                    qSTileHost.changeTiles(arrayList, qSTileHost.loadTileSpecs(qSTileHost.mContext, ""));
                }
            });
        }
    }

    public void removeTile(String str) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.remove(str);
        addIndependentTiles(this.mTileListKey, arrayList);
        Settings.Secure.putStringForUser(this.mContext.getContentResolver(), this.mTileListKey, TextUtils.join(",", arrayList), KeyguardUpdateMonitor.getCurrentUser());
    }

    public int getMinTiles() {
        return this.mMinTilesInOneScreen;
    }

    public void addTile(ComponentName componentName) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.add(0, CustomTile.toSpec(componentName));
        changeTiles(this.mTileSpecs, arrayList);
    }

    public void removeTile(ComponentName componentName) {
        ArrayList arrayList = new ArrayList(this.mTileSpecs);
        arrayList.remove(CustomTile.toSpec(componentName));
        changeTiles(this.mTileSpecs, arrayList);
    }

    public void changeTiles(List<String> list, List<String> list2) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String str = list.get(i);
            if (str.startsWith("custom(") && !list2.contains(str)) {
                ComponentName componentFromSpec = CustomTile.getComponentFromSpec(str);
                TileLifecycleManager tileLifecycleManager = new TileLifecycleManager(new Handler(), this.mContext, this.mServices, TileCompat.newTile(componentFromSpec), new Intent().setComponent(componentFromSpec), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
                tileLifecycleManager.onStopListening();
                tileLifecycleManager.onTileRemoved();
                TileLifecycleManager.setTileAdded(this.mContext, componentFromSpec, false);
                tileLifecycleManager.flushMessagesAndUnbind();
            }
        }
        if (DEBUG) {
            Log.d(this.TAG, "saveCurrentTiles " + list2);
        }
        addIndependentTiles(this.mTileListKey, list2);
        Settings.Secure.putStringForUser(getContext().getContentResolver(), this.mTileListKey, TextUtils.join(",", list2), KeyguardUpdateMonitor.getCurrentUser());
    }

    public QSTile createTile(String str) {
        if (str.equals("custom(com.miui.securitycenter/com.miui.superpower.notification.SuperPowerTileService)") && (KeyguardUpdateMonitor.getCurrentUser() != 0 || (Constants.IS_INTERNATIONAL && Constants.SUPPORT_EXTREME_BATTERY_SAVER && !Constants.IS_TABLET))) {
            return null;
        }
        if (str.equals("custom(com.miui.screenrecorder/.service.QuickService)") && KeyguardUpdateMonitor.getCurrentUser() != 0) {
            return null;
        }
        if (str.equals("custom(com.google.android.gms/.nearby.sharing.SharingTileService)") && !Constants.IS_INTERNATIONAL) {
            return null;
        }
        for (int i = 0; i < this.mQsFactories.size(); i++) {
            QSTile createTile = this.mQsFactories.get(i).createTile(str, this.mControlTileHost);
            if (createTile != null) {
                return createTile;
            }
        }
        return null;
    }

    public QSTileView createTileView(QSTile qSTile, boolean z) {
        for (int i = 0; i < this.mQsFactories.size(); i++) {
            QSTileView createTileView = this.mQsFactories.get(i).createTileView(qSTile, z);
            if (createTileView != null) {
                return createTileView;
            }
        }
        throw new RuntimeException("Default factory didn't create view for " + qSTile.getTileSpec());
    }

    public QSTileView createControlCenterTileView(QSTile qSTile, boolean z) {
        QSTileView createControlCenterTileView = this.mQsFactoryImpl.createControlCenterTileView(qSTile, z);
        if (createControlCenterTileView != null) {
            return createControlCenterTileView;
        }
        throw new RuntimeException("Default factory didn't create view for " + qSTile.getTileSpec());
    }

    public void updateTilesAvailable() {
        onTuningChanged();
    }

    /* access modifiers changed from: protected */
    public List<String> loadTileSpecs(Context context, String str) {
        Resources resources = context.getResources();
        String qsDefaultTiles = getQsDefaultTiles();
        if (TextUtils.isEmpty(str)) {
            str = resources.getString(R.string.quick_settings_tiles);
            if (DEBUG) {
                Slog.d(this.TAG, "loadTileSpecs: Loaded tile specs from config: " + str);
            }
        } else if (!str.contains("edit")) {
            str = resources.getString(R.string.quick_settings_tiles);
            if (DEBUG) {
                Slog.d(this.TAG, "loadTileSpecs: missing edit, loaded tile specs from config: " + str);
            }
        } else if (DEBUG) {
            Slog.d(this.TAG, "loadTileSpecs: loaded tile specs from setting: " + str);
        }
        ArrayList arrayList = new ArrayList();
        boolean z = false;
        for (String trim : str.split(",")) {
            String trim2 = trim.trim();
            if (!trim2.isEmpty()) {
                if (!trim2.equals("default")) {
                    arrayList.add(trim2);
                } else if (!z) {
                    arrayList.addAll(Arrays.asList(qsDefaultTiles.split(",")));
                    z = true;
                }
            }
        }
        filterArrayIndependentTiles(arrayList);
        filterQSTiles(arrayList);
        return arrayList;
    }

    private void filterQSTiles(List<String> list) {
        QSTile createTile;
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (!next.startsWith("custom(")) {
                QSTile createTile2 = createTile(next);
                if (createTile2 == null) {
                    it.remove();
                } else {
                    if (!createTile2.isAvailable()) {
                        it.remove();
                    }
                    createTile2.destroy();
                }
            }
        }
        int size = list.size();
        int i = this.mMinTilesInOneScreen;
        if (size < i) {
            int size2 = i - list.size();
            String[] split = getQsStockTiles().split(",");
            int i2 = 0;
            for (int i3 = 0; i3 < split.length && i2 < size2; i3++) {
                String str = split[i3];
                if (!list.contains(str) && (createTile = createTile(split[i3])) != null) {
                    if (createTile.isAvailable()) {
                        if (TextUtils.equals(list.get(list.size() - 1), "edit")) {
                            list.add(list.size() - 1, str);
                        } else {
                            list.add(list.size(), str);
                        }
                        i2++;
                    }
                    createTile.destroy();
                }
            }
        }
    }

    public void onSuperSaveModeChange(boolean z) {
        if (this.mSuperSaveModeOn != z) {
            this.mSuperSaveModeOn = z;
            updateTileListKey();
        }
    }

    public void onOldModeChange(boolean z) {
        if (this.mOldModeOn != z) {
            this.mOldModeOn = z;
            updateTileListKey();
        }
    }

    public void resetTiles() {
        String str = this.TAG;
        Log.d(str, "resetTiles " + (this instanceof QSControlTileHost) + "  " + this.mTileListKey);
        if (this.mTileListKey.equals("sysui_qs_tiles")) {
            this.mTileSpecs.clear();
            this.mForceTileDestroy = true;
            onTuningChanged(this.mTileListKey, this.mQsDefaultTiles);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("QSTileHost:");
        this.mTiles.values().stream().filter($$Lambda$QSTileHost$2PpjhdQZsz5HSxLRXZDakLEmIOg.INSTANCE).forEach(new Consumer(fileDescriptor, printWriter, strArr) {
            public final /* synthetic */ FileDescriptor f$0;
            public final /* synthetic */ PrintWriter f$1;
            public final /* synthetic */ String[] f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                ((Dumpable) ((QSTile) obj)).dump(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    static /* synthetic */ boolean lambda$dump$0(QSTile qSTile) {
        return qSTile instanceof Dumpable;
    }
}
