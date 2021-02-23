package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0008R$array;
import com.android.systemui.qs.AutoAddTracker;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.AutoTileManager;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.util.UserAwareController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import org.apache.miui.commons.lang3.ArrayUtils;

public class AutoTileManager implements UserAwareController {
    private final ArrayList<AutoAddSetting> mAutoAddSettingList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final AutoAddTracker mAutoTracker;
    @VisibleForTesting
    final CastController.Callback mCastCallback = new CastController.Callback() {
        public void onCastDevicesChanged() {
            if (!AutoTileManager.this.mAutoTracker.isAdded("cast")) {
                boolean z = false;
                Iterator<CastController.CastDevice> it = AutoTileManager.this.mCastController.getCastDevices().iterator();
                while (true) {
                    if (it.hasNext()) {
                        int i = it.next().state;
                        if (i != 2) {
                            if (i == 1) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                z = true;
                if (z) {
                    AutoTileManager.this.mHost.addTile("cast");
                    AutoTileManager.this.mAutoTracker.setTileAdded("cast");
                    AutoTileManager.this.mHandler.post(new Runnable() {
                        public final void run() {
                            AutoTileManager.AnonymousClass5.this.lambda$onCastDevicesChanged$0$AutoTileManager$5();
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onCastDevicesChanged$0 */
        public /* synthetic */ void lambda$onCastDevicesChanged$0$AutoTileManager$5() {
            AutoTileManager.this.mCastController.removeCallback(AutoTileManager.this.mCastCallback);
        }
    };
    /* access modifiers changed from: private */
    public final CastController mCastController;
    private final Context mContext;
    private UserHandle mCurrentUser;
    /* access modifiers changed from: private */
    public final DataSaverController mDataSaverController;
    /* access modifiers changed from: private */
    public final DataSaverController.Listener mDataSaverListener = new DataSaverController.Listener() {
        public void onDataSaverChanged(boolean z) {
            if (!AutoTileManager.this.mAutoTracker.isAdded("saver") && z) {
                AutoTileManager.this.mHost.addTile("saver");
                AutoTileManager.this.mAutoTracker.setTileAdded("saver");
                AutoTileManager.this.mHandler.post(new Runnable() {
                    public final void run() {
                        AutoTileManager.AnonymousClass2.this.lambda$onDataSaverChanged$0$AutoTileManager$2();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDataSaverChanged$0 */
        public /* synthetic */ void lambda$onDataSaverChanged$0$AutoTileManager$2() {
            AutoTileManager.this.mDataSaverController.removeCallback(AutoTileManager.this.mDataSaverListener);
        }
    };
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public final QSTileHost mHost;
    /* access modifiers changed from: private */
    public final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() {
        public void onHotspotChanged(boolean z, int i) {
            if (!AutoTileManager.this.mAutoTracker.isAdded("hotspot") && z) {
                AutoTileManager.this.mHost.addTile("hotspot");
                AutoTileManager.this.mAutoTracker.setTileAdded("hotspot");
                AutoTileManager.this.mHandler.post(new Runnable() {
                    public final void run() {
                        AutoTileManager.AnonymousClass3.this.lambda$onHotspotChanged$0$AutoTileManager$3();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onHotspotChanged$0 */
        public /* synthetic */ void lambda$onHotspotChanged$0$AutoTileManager$3() {
            AutoTileManager.this.mHotspotController.removeCallback(AutoTileManager.this.mHotspotCallback);
        }
    };
    /* access modifiers changed from: private */
    public final HotspotController mHotspotController;
    /* access modifiers changed from: private */
    public final ManagedProfileController mManagedProfileController;
    @VisibleForTesting
    final NightDisplayListener.Callback mNightDisplayCallback = new NightDisplayListener.Callback() {
        public void onActivated(boolean z) {
            if (z) {
                addNightTile();
            }
        }

        public void onAutoModeChanged(int i) {
            if (i == 1 || i == 2) {
                addNightTile();
            }
        }

        private void addNightTile() {
            if (!AutoTileManager.this.mAutoTracker.isAdded("night")) {
                AutoTileManager.this.mHost.addTile("night");
                AutoTileManager.this.mAutoTracker.setTileAdded("night");
                AutoTileManager.this.mHandler.post(new Runnable() {
                    public final void run() {
                        AutoTileManager.AnonymousClass4.this.lambda$addNightTile$0$AutoTileManager$4();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$addNightTile$0 */
        public /* synthetic */ void lambda$addNightTile$0$AutoTileManager$4() {
            AutoTileManager.this.mNightDisplayListener.setCallback((NightDisplayListener.Callback) null);
        }
    };
    /* access modifiers changed from: private */
    public final NightDisplayListener mNightDisplayListener;
    private final ManagedProfileController.Callback mProfileCallback = new ManagedProfileController.Callback() {
        public void onManagedProfileRemoved() {
        }

        public void onManagedProfileChanged() {
            if (!AutoTileManager.this.mAutoTracker.isAdded("work") && AutoTileManager.this.mManagedProfileController.hasActiveProfile()) {
                AutoTileManager.this.mHost.addTile("work");
                AutoTileManager.this.mAutoTracker.setTileAdded("work");
            }
        }
    };

    public AutoTileManager(Context context, AutoAddTracker.Builder builder, QSTileHost qSTileHost, Handler handler, HotspotController hotspotController, DataSaverController dataSaverController, ManagedProfileController managedProfileController, NightDisplayListener nightDisplayListener, CastController castController) {
        this.mContext = context;
        this.mHost = qSTileHost;
        UserHandle user = qSTileHost.getUserContext().getUser();
        this.mCurrentUser = user;
        builder.setUserId(user.getIdentifier());
        this.mAutoTracker = builder.build();
        this.mHandler = handler;
        this.mHotspotController = hotspotController;
        this.mDataSaverController = dataSaverController;
        this.mManagedProfileController = managedProfileController;
        this.mNightDisplayListener = nightDisplayListener;
        this.mCastController = castController;
        populateSettingsList();
        startControllersAndSettingsListeners();
    }

    /* access modifiers changed from: protected */
    public void startControllersAndSettingsListeners() {
        if (!this.mAutoTracker.isAdded("hotspot")) {
            this.mHotspotController.addCallback(this.mHotspotCallback);
        }
        if (!this.mAutoTracker.isAdded("saver")) {
            this.mDataSaverController.addCallback(this.mDataSaverListener);
        }
        if (!this.mAutoTracker.isAdded("work")) {
            this.mManagedProfileController.addCallback(this.mProfileCallback);
        }
        if (!this.mAutoTracker.isAdded("night") && ColorDisplayManager.isNightDisplayAvailable(this.mContext)) {
            this.mNightDisplayListener.setCallback(this.mNightDisplayCallback);
        }
        if (!this.mAutoTracker.isAdded("cast")) {
            this.mCastController.addCallback(this.mCastCallback);
        }
        int size = this.mAutoAddSettingList.size();
        for (int i = 0; i < size; i++) {
            if (!this.mAutoTracker.isAdded(this.mAutoAddSettingList.get(i).mSpec)) {
                this.mAutoAddSettingList.get(i).setListening(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopListening() {
        this.mHotspotController.removeCallback(this.mHotspotCallback);
        this.mDataSaverController.removeCallback(this.mDataSaverListener);
        this.mManagedProfileController.removeCallback(this.mProfileCallback);
        if (ColorDisplayManager.isNightDisplayAvailable(this.mContext)) {
            this.mNightDisplayListener.setCallback((NightDisplayListener.Callback) null);
        }
        this.mCastController.removeCallback(this.mCastCallback);
        int size = this.mAutoAddSettingList.size();
        for (int i = 0; i < size; i++) {
            this.mAutoAddSettingList.get(i).setListening(false);
        }
    }

    private void populateSettingsList() {
        try {
            for (String str : addMiuiSettings(this.mContext.getResources().getStringArray(C0008R$array.config_quickSettingsAutoAdd))) {
                String[] split = str.split(":");
                if (split.length == 2) {
                    this.mAutoAddSettingList.add(new AutoAddSetting(this.mContext, this.mHandler, split[0], split[1]));
                } else {
                    Log.w("AutoTileManager", "Malformed item in array: " + str);
                }
            }
        } catch (Resources.NotFoundException unused) {
            Log.w("AutoTileManager", "Missing config resource");
        }
    }

    private String[] addMiuiSettings(String[] strArr) {
        try {
            return (String[]) ArrayUtils.addAll(strArr, this.mContext.getResources().getStringArray(C0008R$array.miui_config_quickSettingsAutoAdd));
        } catch (Resources.NotFoundException unused) {
            Log.w("AutoTileManager", "Missing MIUI config resource");
            return strArr;
        }
    }

    /* renamed from: changeUser */
    public void lambda$changeUser$0(UserHandle userHandle) {
        if (!Thread.currentThread().equals(this.mHandler.getLooper().getThread())) {
            this.mHandler.post(new Runnable(userHandle) {
                public final /* synthetic */ UserHandle f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AutoTileManager.this.lambda$changeUser$0$AutoTileManager(this.f$1);
                }
            });
        } else if (userHandle.getIdentifier() != this.mCurrentUser.getIdentifier()) {
            stopListening();
            this.mCurrentUser = userHandle;
            int size = this.mAutoAddSettingList.size();
            for (int i = 0; i < size; i++) {
                this.mAutoAddSettingList.get(i).setUserId(userHandle.getIdentifier());
            }
            this.mAutoTracker.changeUser(userHandle);
            startControllersAndSettingsListeners();
        }
    }

    public int getCurrentUserId() {
        return this.mCurrentUser.getIdentifier();
    }

    public void unmarkTileAsAutoAdded(String str) {
        this.mAutoTracker.setTileRemoved(str);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public SecureSetting getSecureSettingForKey(String str) {
        Iterator<AutoAddSetting> it = this.mAutoAddSettingList.iterator();
        while (it.hasNext()) {
            SecureSetting next = it.next();
            if (Objects.equals(str, next.getKey())) {
                return next;
            }
        }
        return null;
    }

    private class AutoAddSetting extends SecureSetting {
        /* access modifiers changed from: private */
        public final String mSpec;

        AutoAddSetting(Context context, Handler handler, String str, String str2) {
            super(context, handler, str);
            this.mSpec = str2;
        }

        /* access modifiers changed from: protected */
        public void handleValueChanged(int i, boolean z) {
            if (AutoTileManager.this.mAutoTracker.isAdded(this.mSpec)) {
                AutoTileManager.this.mHandler.post(new Runnable() {
                    public final void run() {
                        AutoTileManager.AutoAddSetting.this.lambda$handleValueChanged$0$AutoTileManager$AutoAddSetting();
                    }
                });
            } else if (i != 0) {
                if (this.mSpec.startsWith("custom(")) {
                    AutoTileManager.this.mHost.addTile(CustomTile.getComponentFromSpec(this.mSpec), true);
                } else {
                    AutoTileManager.this.mHost.addTile(this.mSpec);
                }
                AutoTileManager.this.mAutoTracker.setTileAdded(this.mSpec);
                AutoTileManager.this.mHandler.post(new Runnable() {
                    public final void run() {
                        AutoTileManager.AutoAddSetting.this.lambda$handleValueChanged$1$AutoTileManager$AutoAddSetting();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$handleValueChanged$0 */
        public /* synthetic */ void lambda$handleValueChanged$0$AutoTileManager$AutoAddSetting() {
            setListening(false);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$handleValueChanged$1 */
        public /* synthetic */ void lambda$handleValueChanged$1$AutoTileManager$AutoAddSetting() {
            setListening(false);
        }
    }
}
