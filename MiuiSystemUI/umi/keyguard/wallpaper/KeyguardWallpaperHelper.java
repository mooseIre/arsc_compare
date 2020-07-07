package com.android.keyguard.wallpaper;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.widget.RemoteViews;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.PreferenceUtils;
import com.android.keyguard.utils.ThemeUtils;
import com.android.keyguard.wallpaper.mode.RequestInfo;
import com.android.keyguard.wallpaper.mode.ResultInfo;
import com.android.keyguard.wallpaper.mode.WallpaperInfo;
import com.google.gson.Gson;
import java.util.List;
import miui.os.Build;
import miui.view.MiuiHapticFeedbackConstants;

public class KeyguardWallpaperHelper {
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            if ("android.miui.UPDATE_LOCKSCREEN_WALLPAPER".equals(intent.getAction())) {
                final String stringExtra = intent.getStringExtra("wallpaperInfo");
                Uri uri = null;
                if (!TextUtils.isEmpty(stringExtra)) {
                    String str = ((WallpaperInfo) KeyguardWallpaperHelper.this.mGson.fromJson(stringExtra, WallpaperInfo.class)).wallpaperUri;
                    if (!TextUtils.isEmpty(str)) {
                        uri = Uri.parse(str);
                    }
                    if (uri != null) {
                        new AsyncTask<Uri, Void, Boolean>() {
                            /* access modifiers changed from: protected */
                            public Boolean doInBackground(Uri... uriArr) {
                                Boolean bool;
                                if (intent.getBooleanExtra("apply", false)) {
                                    bool = Boolean.valueOf(KeyguardWallpaperUtils.setLockWallpaper(context, uriArr[0], true));
                                } else {
                                    bool = true;
                                }
                                if (bool.booleanValue()) {
                                    KeyguardWallpaperUtils.updateCurrentWallpaperInfo(context, stringExtra);
                                }
                                return bool;
                            }

                            /* access modifiers changed from: protected */
                            public void onPostExecute(Boolean bool) {
                                KeyguardWallpaperHelper.this.reportSetLockWallpaperResult(bool.booleanValue());
                            }
                        }.execute(new Uri[]{uri});
                        return;
                    }
                    KeyguardWallpaperHelper.this.reportSetLockWallpaperResult(false);
                } else if (intent.hasExtra("showtime")) {
                    KeyguardWallpaperHelper.this.startLockWallpaperPreviewActivity(intent.getLongExtra("showtime", 0));
                } else {
                    KeyguardWallpaperHelper.this.updateWallpaper(true);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Gson mGson = new Gson();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public boolean mIsChangingLockWallpaper = false;
    /* access modifiers changed from: private */
    public boolean mIsStartingLockWallpaperPreviewActivity = false;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStartedGoingToSleep(int i) {
            boolean unused = KeyguardWallpaperHelper.this.mStartedGoingToSleep = true;
            LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(KeyguardWallpaperHelper.this.mContext, "Screen_OFF");
            KeyguardWallpaperHelper.this.updateWallpaper(false);
        }

        public void onStartedWakingUp() {
            boolean unused = KeyguardWallpaperHelper.this.mStartedGoingToSleep = false;
            LockScreenMagazineUtils.sendLockScreenMagazineScreenOnBroadcast(KeyguardWallpaperHelper.this.mContext);
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            boolean unused = KeyguardWallpaperHelper.this.mKeyguardShowing = z;
        }

        public void onLockScreenMagazineStatusChanged() {
            KeyguardWallpaperHelper.this.updateWallpaper(true);
        }

        public void onUserSwitchComplete(int i) {
            if (KeyguardWallpaperHelper.this.isWallpaperAuthorityChanged()) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    public void run() {
                        KeyguardWallpaperHelper.this.mKeyguardUpdateMonitor.processKeyguardWallpaper();
                    }
                });
            }
        }

        public void onUserUnlocked() {
            super.onUserUnlocked();
            if (KeyguardWallpaperHelper.this.mPendingTellThemeSetSuperWallpaper) {
                Slog.i("KeyguardWallpaperHelper", "onUserUnlocked　notifyThemeSetSuperWallpaper");
                boolean unused = KeyguardWallpaperHelper.this.mPendingTellThemeSetSuperWallpaper = false;
                new AsyncTask<Void, Void, Void>() {
                    /* access modifiers changed from: protected */
                    public Void doInBackground(Void... voidArr) {
                        MiuiKeyguardUtils.notifyThemeSetSuperWallpaper(KeyguardWallpaperHelper.this.mContext);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }
    };
    private ContentObserver mLockWallpaperProviderObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            boolean unused = KeyguardWallpaperHelper.this.isWallpaperAuthorityChanged();
            if ("com.miui.gallery.cloud.baby.wallpaper_provider".equals(KeyguardWallpaperHelper.this.mWallpaperAuthority) || WallpaperAuthorityUtils.APPLY_MAGAZINE_DEFAULT_AUTHORITY.equals(KeyguardWallpaperHelper.this.mWallpaperAuthority)) {
                KeyguardWallpaperHelper.this.updateWallpaper(true);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mPendingTellThemeSetSuperWallpaper;
    /* access modifiers changed from: private */
    public String mPreviewComponent;
    /* access modifiers changed from: private */
    public boolean mStartedGoingToSleep;
    /* access modifiers changed from: private */
    public String mWallpaperAuthority;

    public KeyguardWallpaperHelper(Context context) {
        this.mContext = context;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mWallpaperAuthority = WallpaperAuthorityUtils.getWallpaperAuthority(context);
        registerContentObserver();
        registerBroadcastReceivers();
        KeyguardWallpaperUtils.resetLockWallpaperProviderIfNeeded(context);
        notifyThemeSetSuperWallpaper();
    }

    private void notifyThemeSetSuperWallpaper() {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                try {
                    boolean isDeviceProvisioned = KeyguardUpdateMonitor.getInstance(KeyguardWallpaperHelper.this.mContext).isDeviceProvisioned();
                    String miuiVersionName = MiuiKeyguardUtils.getMiuiVersionName();
                    int intValue = Integer.valueOf(miuiVersionName.substring(1)).intValue();
                    int i = PreferenceUtils.getInt(KeyguardWallpaperHelper.this.mContext, "pref_key_current_miui_version_code", 0);
                    Slog.i("KeyguardWallpaperHelper", "notifyThemeSetSuperWallpaper, isDeviceProvisioned = " + isDeviceProvisioned + "miuiVersionName = " + miuiVersionName + "miuiVersionCode = " + intValue + "oldMiuiVersionCode " + i);
                    if (isDeviceProvisioned && intValue == 12 && i < 12) {
                        boolean isUserUnlocked = KeyguardUpdateMonitor.getInstance(KeyguardWallpaperHelper.this.mContext).isUserUnlocked();
                        Slog.i("KeyguardWallpaperHelper", "notifyThemeSetSuperWallpaper, isUserUnlocked = " + isUserUnlocked);
                        if (isUserUnlocked) {
                            MiuiKeyguardUtils.notifyThemeSetSuperWallpaper(KeyguardWallpaperHelper.this.mContext);
                        } else {
                            boolean unused = KeyguardWallpaperHelper.this.mPendingTellThemeSetSuperWallpaper = true;
                        }
                    }
                    PreferenceUtils.putInt(KeyguardWallpaperHelper.this.mContext, "pref_key_current_miui_version_code", intValue);
                    return null;
                } catch (Exception e) {
                    Log.e("KeyguardWallpaperHelper", "notifyThemeSetSuperWallpaper " + e.getMessage());
                    return null;
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("lock_wallpaper_provider_authority"), false, this.mLockWallpaperProviderObserver, -1);
        this.mLockWallpaperProviderObserver.onChange(false);
    }

    private void registerBroadcastReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.miui.UPDATE_LOCKSCREEN_WALLPAPER");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        LocalBroadcastManager.getInstance(this.mContext).registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public boolean isWallpaperAuthorityChanged() {
        String wallpaperAuthority = WallpaperAuthorityUtils.getWallpaperAuthority(this.mContext);
        Log.d("KeyguardWallpaperHelper", "isWallpaperAuthorityChanged authority = " + wallpaperAuthority);
        if (TextUtils.equals(this.mWallpaperAuthority, wallpaperAuthority)) {
            return false;
        }
        this.mWallpaperAuthority = wallpaperAuthority;
        this.mKeyguardUpdateMonitor.handleLockWallpaperProviderChanged();
        return true;
    }

    /* access modifiers changed from: private */
    public void updateWallpaper(final boolean z) {
        if (KeyguardWallpaperUtils.isDefaultLockStyle(this.mContext) && !this.mIsChangingLockWallpaper) {
            final String wallpaperAuthority = WallpaperAuthorityUtils.getWallpaperAuthority(this.mContext);
            if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) || !this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                if (WallpaperAuthorityUtils.isGalleryCloudBabyWallpaper(this.mContext)) {
                    Context context = this.mContext;
                    if (!ContentProviderUtils.isProviderExists(context, Uri.parse("content://" + wallpaperAuthority))) {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && !z) {
                if (Build.IS_INTERNATIONAL_BUILD) {
                    if (!this.mStartedGoingToSleep) {
                        return;
                    }
                } else if (LockScreenMagazineController.getInstance(this.mContext).isDecoupleHome()) {
                    int lockScreenMagazineWallpaperAutoUpdateMinute = LockScreenMagazineUtils.getLockScreenMagazineWallpaperAutoUpdateMinute(this.mContext);
                    if (lockScreenMagazineWallpaperAutoUpdateMinute != 0 || !this.mKeyguardShowing) {
                        long currentTimeMillis = System.currentTimeMillis();
                        long requestLockScreenMagazineWallpaperTime = LockScreenMagazineUtils.getRequestLockScreenMagazineWallpaperTime(this.mContext);
                        if (currentTimeMillis < requestLockScreenMagazineWallpaperTime || currentTimeMillis - requestLockScreenMagazineWallpaperTime >= ((long) (lockScreenMagazineWallpaperAutoUpdateMinute * 60000))) {
                            LockScreenMagazineUtils.setRequestLockScreenMagazineWallpaperTime(this.mContext, currentTimeMillis);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            if (!WallpaperAuthorityUtils.isGalleryCloudBabyWallpaper(this.mContext) || z || !this.mKeyguardShowing) {
                new AsyncTask<Void, Void, Boolean>() {
                    /* access modifiers changed from: protected */
                    public Boolean doInBackground(Void... voidArr) {
                        boolean unused = KeyguardWallpaperHelper.this.mIsChangingLockWallpaper = true;
                        return Boolean.valueOf(KeyguardWallpaperHelper.this.setLockWallpaperFromProvider(wallpaperAuthority, z, false));
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(Boolean bool) {
                        boolean unused = KeyguardWallpaperHelper.this.mIsChangingLockWallpaper = false;
                        KeyguardWallpaperHelper.this.reportSetLockWallpaperResult(bool.booleanValue());
                        if (bool.booleanValue()) {
                            ThemeUtils.tellThemeLockWallpaperPath(KeyguardWallpaperHelper.this.mContext, "");
                        }
                    }

                    /* access modifiers changed from: protected */
                    public void onCancelled() {
                        boolean unused = KeyguardWallpaperHelper.this.mIsChangingLockWallpaper = false;
                    }
                }.execute(new Void[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean setLockWallpaperFromProvider(String str, boolean z, boolean z2) {
        String str2;
        Uri uri;
        List<WallpaperInfo> list;
        try {
            String currentWallpaperInfo = KeyguardWallpaperUtils.getCurrentWallpaperInfo(this.mContext);
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.mode = 1;
            requestInfo.currentWallpaperInfo = (WallpaperInfo) this.mGson.fromJson(currentWallpaperInfo, WallpaperInfo.class);
            requestInfo.needLast = z2;
            requestInfo.packageName = "com.android.systemui";
            String json = this.mGson.toJson(requestInfo);
            Bundle bundle = new Bundle();
            bundle.putBoolean("force_refresh", z);
            bundle.putString("extra_current_provider", str);
            bundle.putString("request_json", json);
            Context context = this.mContext;
            if (!ContentProviderUtils.isProviderExists(context, Uri.parse("content://" + str))) {
                return false;
            }
            Context context2 = this.mContext;
            Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(context2, "content://" + str, "getNextLockWallpaperUri", (String) null, bundle);
            if (resultFromProvider == null) {
                return false;
            }
            String string = resultFromProvider.getString("result_json");
            String str3 = "";
            if (!TextUtils.isEmpty(string)) {
                ResultInfo resultInfo = (ResultInfo) this.mGson.fromJson(string, ResultInfo.class);
                if (!(resultInfo == null || (list = resultInfo.wallpaperInfos) == null)) {
                    if (list.size() > 0) {
                        WallpaperInfo wallpaperInfo = list.get(0);
                        str2 = wallpaperInfo.wallpaperUri;
                        if (TextUtils.isEmpty(str2)) {
                            return false;
                        }
                        uri = Uri.parse(str2);
                        KeyguardWallpaperUtils.updateCurrentWallpaperInfo(this.mContext, this.mGson.toJson(wallpaperInfo));
                    }
                }
                return false;
            }
            str2 = resultFromProvider.getString("result_string");
            if (TextUtils.isEmpty(str2)) {
                return false;
            }
            uri = Uri.parse(str2);
            KeyguardWallpaperUtils.updateCurrentWallpaperInfo(this.mContext, str3);
            LockScreenMagazineController.getInstance(this.mContext).updateRemoteView((RemoteViews) resultFromProvider.getParcelable("remoteMain"), (RemoteViews) resultFromProvider.getParcelable("remoteFullScreen"));
            StringBuilder sb = new StringBuilder();
            sb.append("setLockWallpaperFromProvider requestWallpaperUri = ");
            if (requestInfo.currentWallpaperInfo != null) {
                str3 = requestInfo.currentWallpaperInfo.wallpaperUri;
            }
            sb.append(str3);
            sb.append(" resultWallpaperUri = ");
            sb.append(str2);
            Log.i("KeyguardWallpaperHelper", sb.toString());
            return KeyguardWallpaperUtils.setLockWallpaper(this.mContext, uri, true);
        } catch (Exception e) {
            Log.e("KeyguardWallpaperHelper", "setLockWallpaperFromProvider" + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void startLockWallpaperPreviewActivity(final long j) {
        if (!Build.IS_TABLET && !this.mIsStartingLockWallpaperPreviewActivity) {
            new AsyncTask<Void, Void, Bundle>() {
                /* access modifiers changed from: protected */
                public Bundle doInBackground(Void... voidArr) {
                    boolean unused = KeyguardWallpaperHelper.this.mIsStartingLockWallpaperPreviewActivity = true;
                    return KeyguardWallpaperHelper.this.getPreviewActivityExtras(j);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Bundle bundle) {
                    boolean unused = KeyguardWallpaperHelper.this.mIsStartingLockWallpaperPreviewActivity = false;
                    if (bundle == null) {
                        KeyguardWallpaperHelper.this.reportSetLockWallpaperResult(false);
                        return;
                    }
                    ComponentName componentName = null;
                    if (KeyguardWallpaperHelper.this.mPreviewComponent != null) {
                        componentName = ComponentName.unflattenFromString(KeyguardWallpaperHelper.this.mPreviewComponent);
                    }
                    if (componentName == null) {
                        KeyguardWallpaperHelper.this.reportSetLockWallpaperResult(false);
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setComponent(componentName);
                    intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                    intent.putExtras(bundle);
                    try {
                        KeyguardWallpaperHelper.this.mContext.startActivity(intent, KeyguardWallpaperHelper.makeCustomAnimation(KeyguardWallpaperHelper.this.mContext, 0, 0, new Handler()).toBundle());
                    } catch (Exception e) {
                        Log.e("KeyguardWallpaperHelper", "start activity failed.", e);
                    }
                }
            }.execute(new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00bf  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.os.Bundle getPreviewActivityExtras(long r8) {
        /*
            r7 = this;
            r0 = 0
            r7.mPreviewComponent = r0
            android.content.Context r1 = r7.mContext
            java.lang.String r1 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.getWallpaperAuthority(r1)
            android.content.Context r2 = r7.mContext
            boolean r2 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.isValidAuthority(r2)
            if (r2 != 0) goto L_0x0013
            java.lang.String r1 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.APPLY_MAGAZINE_DEFAULT_AUTHORITY
        L_0x0013:
            android.content.Context r2 = r7.mContext
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "content://"
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.net.Uri r3 = android.net.Uri.parse(r3)
            boolean r2 = com.android.keyguard.utils.ContentProviderUtils.isProviderExists(r2, r3)
            if (r2 != 0) goto L_0x0031
            return r0
        L_0x0031:
            android.content.Context r2 = r7.mContext
            java.lang.String r2 = com.android.keyguard.wallpaper.KeyguardWallpaperUtils.getCurrentWallpaperInfo(r2)
            com.android.keyguard.wallpaper.mode.RequestInfo r3 = new com.android.keyguard.wallpaper.mode.RequestInfo     // Catch:{ Exception -> 0x0083 }
            r3.<init>()     // Catch:{ Exception -> 0x0083 }
            r5 = 2
            r3.mode = r5     // Catch:{ Exception -> 0x0083 }
            com.google.gson.Gson r5 = r7.mGson     // Catch:{ Exception -> 0x0083 }
            java.lang.Class<com.android.keyguard.wallpaper.mode.WallpaperInfo> r6 = com.android.keyguard.wallpaper.mode.WallpaperInfo.class
            java.lang.Object r5 = r5.fromJson(r2, r6)     // Catch:{ Exception -> 0x0083 }
            com.android.keyguard.wallpaper.mode.WallpaperInfo r5 = (com.android.keyguard.wallpaper.mode.WallpaperInfo) r5     // Catch:{ Exception -> 0x0083 }
            r3.currentWallpaperInfo = r5     // Catch:{ Exception -> 0x0083 }
            com.google.gson.Gson r5 = r7.mGson     // Catch:{ Exception -> 0x0083 }
            java.lang.String r3 = r5.toJson(r3)     // Catch:{ Exception -> 0x0083 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0083 }
            r5.<init>()     // Catch:{ Exception -> 0x0083 }
            r5.append(r4)     // Catch:{ Exception -> 0x0083 }
            r5.append(r1)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r1 = r5.toString()     // Catch:{ Exception -> 0x0083 }
            java.lang.String r1 = r7.getLockWallpaperListFromProvider(r1, r3)     // Catch:{ Exception -> 0x0083 }
            com.google.gson.Gson r3 = r7.mGson     // Catch:{ Exception -> 0x0083 }
            java.lang.Class<com.android.keyguard.wallpaper.mode.ResultInfo> r4 = com.android.keyguard.wallpaper.mode.ResultInfo.class
            java.lang.Object r1 = r3.fromJson(r1, r4)     // Catch:{ Exception -> 0x0083 }
            com.android.keyguard.wallpaper.mode.ResultInfo r1 = (com.android.keyguard.wallpaper.mode.ResultInfo) r1     // Catch:{ Exception -> 0x0083 }
            if (r1 == 0) goto L_0x0081
            java.lang.String r3 = r1.previewComponent     // Catch:{ Exception -> 0x0083 }
            r7.mPreviewComponent = r3     // Catch:{ Exception -> 0x0083 }
            java.lang.String r3 = r1.dialogComponent     // Catch:{ Exception -> 0x0083 }
            com.google.gson.Gson r7 = r7.mGson     // Catch:{ Exception -> 0x007f }
            java.util.List<com.android.keyguard.wallpaper.mode.WallpaperInfo> r1 = r1.wallpaperInfos     // Catch:{ Exception -> 0x007f }
            java.lang.String r7 = r7.toJson(r1)     // Catch:{ Exception -> 0x007f }
            goto L_0x00a0
        L_0x007f:
            r7 = move-exception
            goto L_0x0085
        L_0x0081:
            r7 = r0
            goto L_0x00a1
        L_0x0083:
            r7 = move-exception
            r3 = r0
        L_0x0085:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "getPreviewActivityExtras"
            r1.append(r4)
            java.lang.String r7 = r7.getMessage()
            r1.append(r7)
            java.lang.String r7 = r1.toString()
            java.lang.String r1 = "KeyguardWallpaperHelper"
            android.util.Log.e(r1, r7)
            r7 = r0
        L_0x00a0:
            r0 = r3
        L_0x00a1:
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.lang.String r3 = "showtime"
            r1.putLong(r3, r8)
            java.lang.String r8 = "currentWallpaperInfo"
            r1.putString(r8, r2)
            java.lang.String r8 = "wallpaperInfos"
            r1.putString(r8, r7)
            java.lang.String r7 = "dialogComponent"
            r1.putString(r7, r0)
            boolean r7 = miui.os.Build.IS_INTERNATIONAL_BUILD
            if (r7 == 0) goto L_0x00c6
            java.lang.String r7 = "entry_source"
            java.lang.String r8 = "cta"
            r1.putString(r7, r8)
        L_0x00c6:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.KeyguardWallpaperHelper.getPreviewActivityExtras(long):android.os.Bundle");
    }

    private String getLockWallpaperListFromProvider(String str, String str2) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("request_json", str2);
            Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(this.mContext, str, "getNextLockWallpaperUri", (String) null, bundle);
            if (resultFromProvider == null) {
                return null;
            }
            return resultFromProvider.getString("result_json");
        } catch (Exception e) {
            Log.e("KeyguardWallpaperHelper", "getLockWallpaperListFromProvider failed." + e.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void reportSetLockWallpaperResult(boolean z) {
        Intent intent = new Intent("com.miui.keyguard.setwallpaper");
        intent.putExtra("set_lock_wallpaper_result", z);
        this.mContext.sendBroadcast(intent);
    }

    public static ActivityOptions makeCustomAnimation(Context context, int i, int i2, Handler handler) {
        return ActivityOptions.makeCustomAnimation(context, i, i2, handler, (ActivityOptions.OnAnimationStartedListener) null);
    }
}
