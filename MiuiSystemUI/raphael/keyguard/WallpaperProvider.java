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
import android.provider.Settings;
import android.text.TextUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.utils.PreferenceUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Application;
import com.android.systemui.miui.DrawableUtils;
import com.android.systemui.statusbar.phone.StatusBar;
import com.xiaomi.stat.MiStat;
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
        boolean z = false;
        if (str.equals("SET_LOCK_SCREEN_MAGAZINE_STATUS")) {
            LockScreenMagazineUtils.setLockScreenMagazineStatus(getContext(), bundle.getBoolean(MiStat.Param.STATUS, false));
        } else if (str.equals("GET_ELECTRIC_TORCH_STATUS")) {
            if (Settings.Global.getInt(getContext().getContentResolver(), "torch_state", 0) != 0) {
                z = true;
            }
            bundle2.putBoolean("electric_torch_status", z);
        } else if (str.equals("SET_ELECTRIC_TORCH_STATUS")) {
            try {
                getContext().sendBroadcast(PackageUtils.getToggleTorchIntent(bundle.getBoolean(MiStat.Param.STATUS, false)));
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
            String str3 = "";
            if (str.equals("CHECK_SMART_HOME_STATUS")) {
                if (PackageUtils.isAppInstalledForUser(getContext(), "com.xiaomi.smarthome", KeyguardUpdateMonitor.getCurrentUser()) && MiuiKeyguardUtils.isRegionSupportMiHome(getContext())) {
                    Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(getContext(), MiuiKeyguardUtils.maybeAddUserId(MiuiKeyguardMoveLeftControlCenterView.KEYGUARD_SMART_HOME, KeyguardUpdateMonitor.getCurrentUser()), "online_devices_count", (String) null, (Bundle) null);
                    if (resultFromProvider != null) {
                        str3 = resultFromProvider.getString(MiStat.Param.COUNT, str3);
                    }
                    z = true;
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
                    StatusBar statusBar = (StatusBar) ((Application) getContext().getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
                    if (statusBar != null) {
                        this.mHandler.post(new Runnable(intent) {
                            private final /* synthetic */ Intent f$1;

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
            } else if (str.equals("setLockWallpaperAuthority")) {
                boolean wallpaperAuthoritySystemSetting = WallpaperAuthorityUtils.setWallpaperAuthoritySystemSetting(getContext(), str2);
                String callingPackage = getCallingPackage();
                Log.d("WallpaperProvider", "call METHOD_SET_LOCK_WALLPAPER_PROVIDER_AUTHORITY" + wallpaperAuthoritySystemSetting + " by " + callingPackage);
                if (WallpaperAuthorityUtils.APPLY_MAGAZINE_DEFAULT_AUTHORITY.equals(callingPackage)) {
                    KeyguardWallpaperUtils.setProviderClosedByUser(getContext().getApplicationContext(), TextUtils.isEmpty(str2));
                }
                bundle2.putBoolean("result_boolean", wallpaperAuthoritySystemSetting);
            } else if ("setLockWallpaperUpdateMinute".equals(str)) {
                try {
                    bundle2.putBoolean("result_boolean", LockScreenMagazineUtils.setLockScreenMagazineWallpaperAutoUpdateMinute(getContext(), Integer.parseInt(str2)));
                } catch (Exception e5) {
                    Log.e("WallpaperProvider", "call METHOD_OPEN_SMART_HOME" + e5.getMessage());
                }
            } else if ("getLockScreenPath".equals(str)) {
                String string = PreferenceUtils.getString(getContext(), "pref_key_lock_wallpaper_path", str3);
                if (!TextUtils.isEmpty(string)) {
                    bundle2.putString("result_string", string);
                }
            } else if ("getLockWallpaperInfo".equals(str)) {
                try {
                    String currentWallpaperInfo = KeyguardWallpaperUtils.getCurrentWallpaperInfo(getContext());
                    if (!TextUtils.isEmpty(currentWallpaperInfo)) {
                        bundle2.putString("result_json", currentWallpaperInfo);
                    }
                } catch (Exception e6) {
                    Log.e("WallpaperProvider", "call METHOD_GET_LOCK_WALLPAPER_INFO" + e6.getMessage());
                }
            } else if ("SET_SUPPORT_LOCK_SCREEN_LEFT_OVERLAY".equals(str)) {
                try {
                    KeyguardUpdateMonitor.getInstance(getContext()).setSupportLockScreenMagazineOverlay(bundle.getBoolean("support_overlay"));
                    bundle2.putBoolean("result_boolean", true);
                } catch (Exception e7) {
                    Log.e("WallpaperProvider", "call METHOD_SET_SUPPORT_LOCK_SCREEN_LEFT_OVERLAY" + e7.getMessage());
                }
            } else if ("getLockWallpaper".equals(str)) {
                try {
                    bundle2.putParcelable("wallpaper", DrawableUtils.drawable2Bitmap(KeyguardWallpaperUtils.getLockWallpaperPreview(getContext())));
                } catch (Exception e8) {
                    Log.e("WallpaperProvider", "call METHOD_GET_LOCK_WALLPAPER " + e8.getMessage());
                }
            } else if ("SET_KEYGUARD_CLOCK_POSITION".equals(str)) {
                try {
                    bundle2.putBoolean("result_boolean", Settings.System.putIntForUser(getContext().getContentResolver(), "selected_keyguard_clock_position", bundle.getInt("position", 0), KeyguardUpdateMonitor.getCurrentUser()));
                } catch (Exception e9) {
                    Log.e("WallpaperProvider", "call METHOD_SET_KEYGUARD_CLOCK_POSITION" + e9.getMessage());
                }
            } else if ("getGxzwAnimStyle".equals(str)) {
                bundle2.putParcelable("thumbnail", MiuiGxzwManager.getInstance().getGxzwAnimBitmap());
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
