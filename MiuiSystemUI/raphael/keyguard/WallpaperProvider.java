package com.android.keyguard;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.miui.systemui.util.CommonUtil;
import miui.util.Log;

public class WallpaperProvider extends ContentProvider {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        return null;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }

    public Bundle call(String str, String str2, Bundle bundle) {
        Intent intent;
        Log.d("WallpaperProvider", "call method = " + str);
        Bundle bundle2 = new Bundle();
        if (str.equals("GET_ELECTRIC_TORCH_STATUS")) {
            bundle2.putBoolean("electric_torch_status", ((FlashlightController) Dependency.get(FlashlightController.class)).isEnabled());
        } else if (str.equals("SET_ELECTRIC_TORCH_STATUS")) {
            try {
                CommonUtil.toggleTorch();
            } catch (Exception e) {
                Log.e("WallpaperProvider", "call METHOD_SET_ELECTRIC_TORCH_STATUS" + e.getMessage());
            }
        } else if (str.equals("CHECK_TSM_CLIENT_STATUS")) {
            try {
                bundle2.putBoolean("TSM_client_status", PackageUtils.supportTSMClient(getContext()));
            } catch (Exception e2) {
                Log.e("WallpaperProvider", "call METHOD_CHECK_TSM_CLIENT_STATUS" + e2.getMessage());
            }
        } else if (str.equals("OPEN_TSM_CLIENT")) {
            try {
                getContext().startActivityAsUser(PackageUtils.getTSMClientIntent(), UserHandle.CURRENT);
            } catch (Exception e3) {
                Log.e("WallpaperProvider", "call METHOD_OPEN_TSM_CLIENT" + e3.getMessage());
            }
        } else {
            boolean z = true;
            if (str.equals("CHECK_SMART_HOME_STATUS")) {
                String str3 = "";
                if (!PackageUtils.isAppInstalledForUser(getContext(), "com.xiaomi.smarthome", KeyguardUpdateMonitor.getCurrentUser()) || !MiuiKeyguardUtils.isRegionSupportMiHome(getContext())) {
                    z = false;
                } else {
                    Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(getContext(), MiuiKeyguardUtils.maybeAddUserId(MiuiKeyguardMoveLeftControlCenterView.KEYGUARD_SMART_HOME, KeyguardUpdateMonitor.getCurrentUser()), "online_devices_count", (String) null, (Bundle) null);
                    if (resultFromProvider != null) {
                        str3 = resultFromProvider.getString("count", str3);
                    }
                }
                bundle2.putBoolean("smart_home_status", z);
                if (z) {
                    bundle2.putString("smart_home_online_devices_count", str3);
                }
            } else if (str.equals("OPEN_SMART_HOME")) {
                try {
                    if (PackageUtils.isAppInstalledForUser(getContext(), "com.xiaomi.smarthome", KeyguardUpdateMonitor.getCurrentUser())) {
                        intent = PackageUtils.getSmartHomeMainIntent();
                    } else {
                        intent = PackageUtils.getMarketDownloadIntent("com.xiaomi.smarthome");
                    }
                    StatusBar statusBar = (StatusBar) Dependency.get(StatusBar.class);
                    if (statusBar != null) {
                        this.mHandler.post(new Runnable(intent) {
                            /* class com.android.keyguard.$$Lambda$WallpaperProvider$JpV_wqCsi6DqWAHoUV3crosEfIk */
                            public final /* synthetic */ Intent f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                WallpaperProvider.lambda$call$0(StatusBar.this, this.f$1);
                            }
                        });
                    }
                } catch (Exception e4) {
                    Log.e("WallpaperProvider", "call METHOD_OPEN_SMART_HOME" + e4.getMessage());
                }
            } else if ("SET_SUPPORT_LOCK_SCREEN_LEFT_OVERLAY".equals(str)) {
                try {
                    ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).setSupportLockScreenMagazineOverlay(bundle.getBoolean("support_overlay"));
                    bundle2.putBoolean("result_boolean", true);
                } catch (Exception e5) {
                    Log.e("WallpaperProvider", "call METHOD_SET_SUPPORT_LOCK_SCREEN_LEFT_OVERLAY" + e5.getMessage());
                }
            } else if ("getGxzwAnimStyle".equals(str)) {
                bundle2.putParcelable("thumbnail", ((MiuiGxzwManager) Dependency.get(MiuiGxzwManager.class)).getGxzwAnimBitmap());
            }
        }
        return bundle2;
    }

    static /* synthetic */ void lambda$call$0(StatusBar statusBar, Intent intent) {
        try {
            statusBar.startActivity(intent, true);
        } catch (Exception e) {
            Log.e("WallpaperProvider", "call METHOD_OPEN_SMART_HOME" + e.getMessage());
        }
    }
}
