package com.android.systemui.controlcenter.qs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import codeinjection.CodeInjection;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0021R$string;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.policy.OldModeController;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.controlcenter.qs.tileview.CCQSTileView;
import com.android.systemui.controlcenter.utils.Constants;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.miui.qs.MiuiQSTilePlugin;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class MiuiQSTileHostInjector implements SuperSaveModeController.SuperSaveModeChangeListener, OldModeController.OldModeChangeListener {
    private String MIUI_QS_TILES_EDITED = "edited";
    private int NORMAL_MIN_SIZE_ONE_SCREEN = 12;
    private int OLD_MODE_MIN_TILE_ONE_SCREEN = 7;
    private int SUPER_SAVE_MIN_TILE_ONE_SCREEN = 5;
    private Handler mBgHandler;
    private Context mContext;
    protected List<String> mControlIndependentTiles;
    private ControlPanelController mControlPanelController;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private final DeviceProvisionedController.DeviceProvisionedListener mDeviceProvisionedListener = new DeviceProvisionedController.DeviceProvisionedListener() {
        /* class com.android.systemui.controlcenter.qs.MiuiQSTileHostInjector.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
        public void onDeviceProvisionedChanged() {
            if (MiuiQSTileHostInjector.this.mDeviceProvisionedController.isDeviceProvisioned() && !MiuiQSTileHostInjector.this.mMiuiUpdateVersionSharedPreferences.getBoolean("deviceProvisionUpdateTiles", false)) {
                if (MiuiQSTileHostInjector.this.checkHuanjiFinish()) {
                    MiuiQSTileHostInjector.this.setMiuiQSTilesEdited();
                    MiuiQSTileHostInjector.this.onTuningChanged();
                }
                SharedPreferences.Editor edit = MiuiQSTileHostInjector.this.mMiuiUpdateVersionSharedPreferences.edit();
                edit.putBoolean("deviceProvisionUpdateTiles", true);
                edit.apply();
            }
        }
    };
    private boolean mEdited = false;
    private boolean mForceRefreshTiles = false;
    private QSTileHost mHost;
    private SharedPreferences mMiuiQSTilesSharedPreferences;
    private MiuiQSTilePlugin mMiuiQSTilsplugin;
    private PluginListener mMiuiTilePluginListener = new PluginListener<MiuiQSTilePlugin>() {
        /* class com.android.systemui.controlcenter.qs.MiuiQSTileHostInjector.AnonymousClass1 */

        public void onPluginConnected(MiuiQSTilePlugin miuiQSTilePlugin, Context context) {
            MiuiQSTileHostInjector.this.mMiuiQSTilsplugin = miuiQSTilePlugin;
            MiuiQSTileHostInjector.this.mPluginStockTiles = miuiQSTilePlugin.getStockTileWithOrder();
            MiuiQSTileHostInjector.this.mPluginDefaultTiles = miuiQSTilePlugin.getDefaultTileWithOrder();
            MiuiQSTileHostInjector miuiQSTileHostInjector = MiuiQSTileHostInjector.this;
            miuiQSTileHostInjector.mQsStockTiles = TextUtils.isEmpty(miuiQSTileHostInjector.mPluginStockTiles) ? MiuiQSTileHostInjector.this.mQsStockTiles : MiuiQSTileHostInjector.this.mPluginStockTiles;
            MiuiQSTileHostInjector miuiQSTileHostInjector2 = MiuiQSTileHostInjector.this;
            miuiQSTileHostInjector2.mQsDefaultTiles = TextUtils.isEmpty(miuiQSTileHostInjector2.mPluginDefaultTiles) ? MiuiQSTileHostInjector.this.mQsDefaultTiles : MiuiQSTileHostInjector.this.mPluginDefaultTiles;
            MiuiQSTileHostInjector.this.filterIndependentTiles();
            MiuiQSTileHostInjector.this.mForceRefreshTiles = true;
            MiuiQSTileHostInjector.this.onTuningChanged();
            MiuiQSTileHostInjector.this.mForceRefreshTiles = false;
        }

        public void onPluginDisconnected(MiuiQSTilePlugin miuiQSTilePlugin) {
            MiuiQSTileHostInjector.this.mMiuiQSTilsplugin = null;
            MiuiQSTileHostInjector miuiQSTileHostInjector = MiuiQSTileHostInjector.this;
            miuiQSTileHostInjector.initQSTiles(miuiQSTileHostInjector.mContext);
            MiuiQSTileHostInjector.this.onTuningChanged();
        }
    };
    private SharedPreferences mMiuiUpdateVersionSharedPreferences;
    private OldModeController mOldModeController;
    private boolean mOldModeOn = false;
    protected String mPluginDefaultTiles = CodeInjection.MD5;
    private PluginManager mPluginManager;
    protected String mPluginStockTiles = CodeInjection.MD5;
    protected String mQsDefaultTiles = CodeInjection.MD5;
    private ArrayList<QSFactory> mQsFactories;
    protected String mQsStockTiles = CodeInjection.MD5;
    private SuperSaveModeController mSuperSaveModeController;
    private boolean mSuperSaveModeOn = false;
    private String mTileListKey = "sysui_qs_tiles";
    private LinkedHashMap<String, QSTile> mTiles;
    private TunerService mTunerService;
    private boolean mUseControlCenter = false;

    public MiuiQSTileHostInjector(Context context, PluginManager pluginManager, TunerService tunerService, ControlPanelController controlPanelController, SuperSaveModeController superSaveModeController, OldModeController oldModeController, DeviceProvisionedController deviceProvisionedController, Handler handler) {
        this.mContext = context;
        this.mPluginManager = pluginManager;
        this.mTunerService = tunerService;
        this.mControlPanelController = controlPanelController;
        this.mSuperSaveModeController = superSaveModeController;
        this.mOldModeController = oldModeController;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mBgHandler = handler;
    }

    public void MiuiInit(QSTileHost qSTileHost, ArrayList<QSFactory> arrayList, LinkedHashMap<String, QSTile> linkedHashMap, ArrayList<String> arrayList2) {
        this.mHost = qSTileHost;
        this.mQsFactories = arrayList;
        this.mTiles = linkedHashMap;
        this.mUseControlCenter = this.mControlPanelController.useControlPanel();
        ArrayList arrayList3 = new ArrayList();
        this.mControlIndependentTiles = arrayList3;
        if (Constants.IS_INTERNATIONAL) {
            arrayList3.addAll(Arrays.asList(this.mContext.getResources().getStringArray(C0008R$array.qs_control_independent_tiles_global)));
        } else {
            arrayList3.addAll(Arrays.asList(this.mContext.getResources().getStringArray(C0008R$array.qs_control_independent_tiles)));
        }
        initQSTiles(this.mContext);
        this.mPluginManager.addPluginListener(this.mMiuiTilePluginListener, MiuiQSTilePlugin.class, true);
        if (this.mUseControlCenter) {
            this.NORMAL_MIN_SIZE_ONE_SCREEN = 8;
            this.SUPER_SAVE_MIN_TILE_ONE_SCREEN = 4;
        }
        this.mMiuiQSTilesSharedPreferences = this.mContext.getSharedPreferences("miuiQSTiles", 0);
        this.mMiuiUpdateVersionSharedPreferences = this.mContext.getSharedPreferences("deviceProvisionUpdateTiles", 0);
        this.mEdited = this.mMiuiQSTilesSharedPreferences.getBoolean(this.MIUI_QS_TILES_EDITED, false);
        this.mSuperSaveModeOn = this.mSuperSaveModeController.isActive();
        this.mOldModeOn = this.mOldModeController.isActive();
        this.mSuperSaveModeController.addCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
        this.mOldModeController.addCallback((OldModeController.OldModeChangeListener) this);
        changeTileListKey();
        this.mDeviceProvisionedController.addCallback(this.mDeviceProvisionedListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initQSTiles(Context context) {
        this.mQsDefaultTiles = context.getString(C0021R$string.miui_quick_settings_tiles_default);
        this.mQsStockTiles = context.getString(C0021R$string.miui_quick_settings_tiles_stock);
        filterIndependentTiles();
    }

    public void filterIndependentTiles() {
        if (this.mUseControlCenter) {
            for (String str : this.mControlIndependentTiles) {
                String str2 = this.mQsStockTiles;
                this.mQsStockTiles = str2.replace(str + ",", CodeInjection.MD5);
                String str3 = this.mQsDefaultTiles;
                this.mQsDefaultTiles = str3.replace(str + ",", CodeInjection.MD5);
                String str4 = this.mPluginStockTiles;
                this.mPluginStockTiles = str4.replace(str + ",", CodeInjection.MD5);
                String str5 = this.mPluginDefaultTiles;
                this.mPluginDefaultTiles = str5.replace(str + ",", CodeInjection.MD5);
            }
        }
    }

    public void filterArrayIndependentTiles(ArrayList<String> arrayList) {
        if (arrayList != null && this.mUseControlCenter) {
            for (String str : this.mControlIndependentTiles) {
                arrayList.remove(str);
            }
        }
    }

    public void addIndependentTiles(List<String> list) {
        String str;
        boolean z;
        if (this.mUseControlCenter && list != null && list.size() != 0 && (str = this.mTileListKey) != null && str.equals("sysui_qs_tiles") && this.mUseControlCenter) {
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

    public void setMiuiQSTilesEdited() {
        if (!this.mEdited) {
            this.mBgHandler.post(new Runnable() {
                /* class com.android.systemui.controlcenter.qs.$$Lambda$MiuiQSTileHostInjector$69KKihE6uGwnN7X1WKnC1f6g6Z8 */

                public final void run() {
                    MiuiQSTileHostInjector.this.lambda$setMiuiQSTilesEdited$1$MiuiQSTileHostInjector();
                }
            });
            this.mEdited = true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setMiuiQSTilesEdited$1 */
    public /* synthetic */ void lambda$setMiuiQSTilesEdited$1$MiuiQSTileHostInjector() {
        SharedPreferences.Editor edit = this.mMiuiQSTilesSharedPreferences.edit();
        edit.putBoolean(this.MIUI_QS_TILES_EDITED, true);
        edit.apply();
    }

    public QSTileView createControlCenterTileView(QSTile qSTile, boolean z) {
        return new CCQSTileView(this.mContext, qSTile.createControlCenterTileView(this.mContext), z);
    }

    public List<String> getMiuiDefaultTiles(Context context) {
        String str;
        ArrayList arrayList = new ArrayList();
        Resources resources = context.getResources();
        if (this.mSuperSaveModeOn) {
            if (this.mUseControlCenter) {
                str = resources.getString(C0021R$string.control_quick_settings_tiles_super_save);
            } else {
                str = resources.getString(C0021R$string.quick_settings_tiles_super_save);
            }
        } else if (this.mOldModeOn && !this.mUseControlCenter) {
            str = resources.getString(C0021R$string.quick_settings_tiles_old_mode);
        } else if (this.mMiuiQSTilsplugin == null || TextUtils.isEmpty(this.mPluginDefaultTiles)) {
            str = this.mQsDefaultTiles;
        } else {
            str = this.mMiuiQSTilsplugin.getDefaultTileWithOrder();
        }
        arrayList.addAll(Arrays.asList(str.split(",")));
        if (Build.IS_DEBUGGABLE) {
            arrayList.add("dbg:mem");
        }
        return arrayList;
    }

    private String getTileListValue() {
        if (this.mTileListKey.equals("sysui_qs_super_save_tiles")) {
            return getUltraBatteryList();
        }
        if (this.mTileListKey.equals("sysui_qs_old_mode_tiles") && !this.mUseControlCenter) {
            String value = this.mTunerService.getValue("sysui_qs_old_mode_tiles");
            if (TextUtils.isEmpty(value)) {
                return this.mContext.getResources().getString(C0021R$string.quick_settings_tiles_old_mode);
            }
            return value;
        } else if (!this.mTileListKey.equals("sysui_qs_tiles")) {
            return this.mQsDefaultTiles;
        } else {
            if (this.mEdited) {
                return this.mTunerService.getValue("sysui_qs_tiles");
            }
            return this.mQsDefaultTiles;
        }
    }

    private String getUltraBatteryList() {
        if (!this.mUseControlCenter) {
            return this.mContext.getResources().getString(C0021R$string.quick_settings_tiles_super_save);
        }
        String string = this.mContext.getResources().getString(C0021R$string.control_quick_settings_tiles_super_save);
        return !this.mContext.getPackageManager().hasSystemFeature("android.hardware.nfc") ? string.replace("nfc", "mute") : string;
    }

    public String getQsStockTiles() {
        if (this.mMiuiQSTilsplugin == null || TextUtils.isEmpty(this.mPluginStockTiles)) {
            return this.mQsStockTiles;
        }
        return this.mPluginStockTiles;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTuningChanged() {
        this.mHost.onTuningChanged("sysui_qs_tiles", CodeInjection.MD5);
        this.mHost.onTuningChanged("sysui_qs_tiles", getTileListValue());
    }

    public QSTile createMiuiTile(String str) {
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
            QSTile createTile = this.mQsFactories.get(i).createTile(str, this.mUseControlCenter);
            if (createTile != null) {
                return createTile;
            }
        }
        return null;
    }

    public void collapsePanels() {
        if (this.mUseControlCenter) {
            this.mControlPanelController.collapsePanel(true);
        }
    }

    public boolean isForceRefreshTiles() {
        return this.mForceRefreshTiles;
    }

    public void switchControlCenter(boolean z) {
        this.mUseControlCenter = z;
        this.mTiles.values().forEach($$Lambda$MiuiQSTileHostInjector$Wzjacdu73PD1RWbTfY_QUtlIIQ.INSTANCE);
        initQSTiles(this.mContext);
        MiuiQSTilePlugin miuiQSTilePlugin = this.mMiuiQSTilsplugin;
        if (miuiQSTilePlugin != null) {
            String defaultTileWithOrder = miuiQSTilePlugin.getDefaultTileWithOrder();
            this.mPluginDefaultTiles = defaultTileWithOrder;
            this.mQsDefaultTiles = TextUtils.isEmpty(defaultTileWithOrder) ? this.mQsDefaultTiles : this.mPluginDefaultTiles;
        }
        onTuningChanged();
    }

    public MiuiQSTilePlugin getMiuiQSTilePlugin() {
        return this.mMiuiQSTilsplugin;
    }

    public boolean isSuperSaveMode() {
        return this.mSuperSaveModeOn;
    }

    @Override // com.android.systemui.controlcenter.policy.SuperSaveModeController.SuperSaveModeChangeListener
    public void onSuperSaveModeChange(boolean z) {
        if (this.mSuperSaveModeOn != z) {
            this.mSuperSaveModeOn = z;
            updateTileListKey();
        }
    }

    @Override // com.android.systemui.controlcenter.policy.OldModeController.OldModeChangeListener
    public void onOldModeChange(boolean z) {
        if (this.mOldModeOn != z) {
            this.mOldModeOn = z;
            updateTileListKey();
        }
    }

    private void changeTileListKey() {
        if (this.mSuperSaveModeOn) {
            this.mTileListKey = "sysui_qs_super_save_tiles";
        } else if (!this.mOldModeOn || this.mUseControlCenter) {
            this.mTileListKey = "sysui_qs_tiles";
        } else {
            this.mTileListKey = "sysui_qs_old_mode_tiles";
        }
    }

    private void updateTileListKey() {
        String str = this.mTileListKey;
        changeTileListKey();
        if (!TextUtils.equals(str, this.mTileListKey)) {
            onTuningChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkHuanjiFinish() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "huanji_finished", 0) == 1;
    }
}
