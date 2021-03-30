package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.systemui.C0008R$array;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Prefs;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.CloudDataListener;
import com.miui.systemui.CloudDataManager;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.NotificationCloudData;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import miui.util.NotificationFilterHelper;

public class NotificationSettingsManager implements Dumpable {
    private static final boolean DEBUG = DebugConfig.DEBUG_NOTIFICATION;
    private static final boolean USE_WHITE_LISTS = (!BuildConfig.IS_INTERNATIONAL);
    private List<String> mAllowFloatPackages;
    private List<String> mAllowKeyguardPackages;
    private List<String> mAllowNotificationSlide;
    private List<String> mAvoidDisturbPackages;
    private Handler mBgHandler;
    private List<String> mBlockFloatPackages;
    private List<String> mBlockKeyguardPackages;
    private List<String> mCanShowBadgePackages;
    private Context mContext;
    private List<String> mCustomAppIconPackages;
    private List<String> mDisableAutoGroupSummaryPackages;
    private List<String> mHideAlertWindowWhitelist;
    private List<String> mHideForegroundWhitelist;
    private List<String> mImportantWhitelist;
    private List<String> mPreInstallPackages;
    private List<String> mPrioritizedPackages;
    private List<String> mSubstitutePackages;
    private ArrayMap<String, Boolean> mSystemApps = new ArrayMap<>();

    public NotificationSettingsManager(Context context, CloudDataManager cloudDataManager) {
        this.mContext = context;
        this.mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mPrioritizedPackages = getStringArray(C0008R$array.config_prioritizedPackages);
        this.mSubstitutePackages = getStringArray(C0008R$array.config_canSendSubstituteNotificationPackages);
        this.mCustomAppIconPackages = getStringArray(C0008R$array.config_canCustomNotificationAppIcon);
        this.mDisableAutoGroupSummaryPackages = getStringArray(C0008R$array.config_disableAutoGroupSummaryPackages);
        this.mHideForegroundWhitelist = getStringArray(C0008R$array.system_foreground_notification_whitelist);
        this.mHideAlertWindowWhitelist = getStringArray(C0008R$array.system_alert_window_notification_whitelist);
        this.mAvoidDisturbPackages = getStringArray(C0008R$array.avoid_disturb_app_whitelist);
        this.mPreInstallPackages = getStringArray(C0008R$array.config_preInstalledPackages);
        this.mCanShowBadgePackages = getStringArray(C0008R$array.config_canShowBadgePackages);
        this.mAllowFloatPackages = getStringArray(C0008R$array.config_allowFloatPackages);
        this.mAllowKeyguardPackages = getStringArray(C0008R$array.config_allowKeyguardPackages);
        this.mBlockFloatPackages = getStringArray(C0008R$array.config_blockFloatPackages);
        this.mBlockKeyguardPackages = getStringArray(C0008R$array.config_blockKeyguardPackages);
        this.mAllowNotificationSlide = getStringArray(C0008R$array.config_allowNotificationSlide);
        this.mImportantWhitelist = getStringArray(C0008R$array.important_section_whitelist);
        cloudDataManager.registerListener(new CloudDataListener() {
            /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationSettingsManager$d8gb7CE_AenvkjHLK6pCP4Go */

            @Override // com.miui.systemui.CloudDataListener
            public final void onCloudDataUpdate(boolean z) {
                NotificationSettingsManager.this.lambda$new$0$NotificationSettingsManager(z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NotificationSettingsManager(boolean z) {
        onCloudDataUpdated();
    }

    private List<String> getStringArray(int i) {
        return Arrays.asList(this.mContext.getResources().getStringArray(i));
    }

    public void onCloudDataUpdated() {
        List<String> floatWhitelist = NotificationCloudData.Companion.getFloatWhitelist(this.mContext);
        if (floatWhitelist != null && !floatWhitelist.isEmpty()) {
            this.mAllowFloatPackages = floatWhitelist;
        }
        if (NotificationCloudData.Companion.isFloatDataUpdated(this.mContext, this.mAllowFloatPackages)) {
            this.mBgHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationSettingsManager$yxjwWiXuYbIej07h3m_Ynt7n8c0 */

                public final void run() {
                    NotificationSettingsManager.this.lambda$onCloudDataUpdated$1$NotificationSettingsManager();
                }
            });
        }
        List<String> keyguardWhitelist = NotificationCloudData.Companion.getKeyguardWhitelist(this.mContext);
        if (keyguardWhitelist != null && !keyguardWhitelist.isEmpty()) {
            this.mAllowKeyguardPackages = keyguardWhitelist;
        }
        if (NotificationCloudData.Companion.isKeyguardDataUpdated(this.mContext, this.mAllowKeyguardPackages)) {
            this.mBgHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationSettingsManager$TBAtro6UZEXCqz3ZylABms_UzP8 */

                public final void run() {
                    NotificationSettingsManager.this.lambda$onCloudDataUpdated$2$NotificationSettingsManager();
                }
            });
        }
        List<String> floatBlacklist = NotificationCloudData.Companion.getFloatBlacklist(this.mContext);
        if (floatBlacklist != null && !floatBlacklist.isEmpty()) {
            this.mBlockFloatPackages = floatBlacklist;
        }
        List<String> keyguardBlacklist = NotificationCloudData.Companion.getKeyguardBlacklist(this.mContext);
        if (keyguardBlacklist != null && !keyguardBlacklist.isEmpty()) {
            this.mBlockKeyguardPackages = keyguardBlacklist;
        }
        List<String> badgeWhitelist = NotificationCloudData.Companion.getBadgeWhitelist(this.mContext);
        if (badgeWhitelist != null && !badgeWhitelist.isEmpty()) {
            this.mCanShowBadgePackages = badgeWhitelist;
        }
        List<String> slideWhiteList = NotificationCloudData.Companion.getSlideWhiteList(this.mContext);
        if (slideWhiteList != null && !slideWhiteList.isEmpty()) {
            this.mAllowNotificationSlide = slideWhiteList;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCloudDataUpdated$1 */
    public /* synthetic */ void lambda$onCloudDataUpdated$1$NotificationSettingsManager() {
        NotificationFilterHelper.updateFloatWhiteList(this.mContext, this.mAllowFloatPackages);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCloudDataUpdated$2 */
    public /* synthetic */ void lambda$onCloudDataUpdated$2$NotificationSettingsManager() {
        NotificationFilterHelper.updateKeyguardWhitelist(this.mContext, this.mAllowKeyguardPackages);
    }

    public String getString(int i) {
        return this.mContext.getString(i);
    }

    public boolean getBoolean(int i) {
        return this.mContext.getResources().getBoolean(i);
    }

    public boolean isPrioritizedApp(String str) {
        return this.mPrioritizedPackages.contains(str);
    }

    public boolean canSendSubstituteNotification(String str) {
        return DEBUG || this.mSubstitutePackages.contains(str);
    }

    public boolean canCustomAppIcon(String str) {
        return DEBUG || this.mCustomAppIconPackages.contains(str);
    }

    public boolean disableAutoGroupSummary(String str) {
        return this.mDisableAutoGroupSummaryPackages.contains(str);
    }

    public boolean hideForegroundNotification(String str, String str2) {
        if (!this.mHideForegroundWhitelist.contains(str)) {
            List<String> list = this.mHideForegroundWhitelist;
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(":");
            sb.append(str2);
            return list.contains(sb.toString());
        }
    }

    public boolean hideAlertWindowNotification(String str) {
        if (!TextUtils.isEmpty(str) && str.startsWith("com.android.server.wm.AlertWindowNotification - ")) {
            return this.mHideAlertWindowWhitelist.contains(str.split(" - ", 2)[1]);
        }
        return false;
    }

    public boolean shouldPeekWhenAppShowing(String str) {
        return this.mAvoidDisturbPackages.contains(str);
    }

    public int getFoldImportance(Context context, String str) {
        String foldImportanceKey = FilterHelperCompat.getFoldImportanceKey(str);
        SharedPreferences notif = Prefs.getNotif(context);
        if (notif.contains(foldImportanceKey)) {
            return notif.getInt(foldImportanceKey, 0);
        }
        return 0;
    }

    public void setFoldImportance(Context context, String str, int i) {
        Prefs.getNotif(context).edit().putInt(FilterHelperCompat.getFoldImportanceKey(str), i).apply();
    }

    public boolean canShowBadge(Context context, String str) {
        String badgeKey = FilterHelperCompat.getBadgeKey(str);
        SharedPreferences notif = Prefs.getNotif(context);
        if (notif.contains(badgeKey)) {
            return notif.getBoolean(badgeKey, false);
        }
        if (this.mCanShowBadgePackages.contains(str) || this.mPreInstallPackages.contains(str) || isSystemApp(str)) {
            return true;
        }
        return false;
    }

    public boolean canSlide(String str) {
        return this.mAllowNotificationSlide.contains(str) || DEBUG;
    }

    public void setShowBadge(Context context, String str, boolean z) {
        String badgeKey = FilterHelperCompat.getBadgeKey(str);
        if (DEBUG) {
            Log.d("NotifiSettingsManager", String.format("setShowBadge key=%s enabled=%s", badgeKey, Boolean.valueOf(z)));
        }
        Prefs.getNotif(context).edit().putBoolean(badgeKey, z).apply();
    }

    public boolean canFloat(Context context, String str, String str2) {
        boolean canFloat = canFloat(context, str);
        if (!canFloat || TextUtils.isEmpty(str2)) {
            return canFloat;
        }
        String floatKey = FilterHelperCompat.getFloatKey(str, str2);
        SharedPreferences notif = Prefs.getNotif(context);
        if (notif.contains(floatKey)) {
            return notif.getInt(floatKey, 1) == 2;
        }
        boolean isXmsfChannel = NotificationUtil.isXmsfChannel(str, str2);
        if (USE_WHITE_LISTS) {
            if (!isXmsfChannel) {
                return this.mAllowFloatPackages.contains(str);
            }
        } else if (this.mBlockFloatPackages.contains(str)) {
            return false;
        }
        return true;
    }

    private boolean canFloat(Context context, String str) {
        String floatKey = FilterHelperCompat.getFloatKey(str, null);
        SharedPreferences notif = Prefs.getNotif(context);
        return notif.contains(floatKey) ? notif.getInt(floatKey, 1) == 2 : USE_WHITE_LISTS || !this.mBlockFloatPackages.contains(str);
    }

    public void setFloat(Context context, String str, String str2, boolean z) {
        String floatKey = FilterHelperCompat.getFloatKey(str, str2);
        int i = 1;
        if (DEBUG) {
            Log.d("NotifiSettingsManager", String.format("setFloat key=%s enabled=%s", floatKey, Boolean.valueOf(z)));
        }
        if (z) {
            i = 2;
        }
        Prefs.getNotif(context).edit().putInt(floatKey, i).apply();
    }

    public boolean canShowOnKeyguard(Context context, String str, String str2) {
        boolean canShowOnKeyguard = canShowOnKeyguard(context, str);
        if (!canShowOnKeyguard || TextUtils.isEmpty(str2)) {
            return canShowOnKeyguard;
        }
        String keyguardKey = FilterHelperCompat.getKeyguardKey(str, str2);
        SharedPreferences notif = Prefs.getNotif(context);
        if (notif.contains(keyguardKey)) {
            return notif.getBoolean(keyguardKey, false);
        }
        boolean isXmsfChannel = NotificationUtil.isXmsfChannel(str, str2);
        if (USE_WHITE_LISTS) {
            if (!isXmsfChannel) {
                return this.mAllowKeyguardPackages.contains(str);
            }
        } else if (this.mBlockKeyguardPackages.contains(str)) {
            return false;
        }
        return true;
    }

    private boolean canShowOnKeyguard(Context context, String str) {
        String keyguardKey = FilterHelperCompat.getKeyguardKey(str, null);
        SharedPreferences notif = Prefs.getNotif(context);
        if (notif.contains(keyguardKey)) {
            return notif.getBoolean(keyguardKey, false);
        }
        if (!USE_WHITE_LISTS && this.mBlockKeyguardPackages.contains(str)) {
            return false;
        }
        return true;
    }

    public void setShowOnKeyguard(Context context, String str, String str2, boolean z) {
        String keyguardKey = FilterHelperCompat.getKeyguardKey(str, str2);
        if (DEBUG) {
            Log.d("NotifiSettingsManager", String.format("setShowOnKeyguard key=%s enabled=%s", keyguardKey, Boolean.valueOf(z)));
        }
        Prefs.getNotif(context).edit().putBoolean(keyguardKey, z).apply();
    }

    public boolean canSound(Context context, String str) {
        String soundKey = FilterHelperCompat.getSoundKey(str);
        SharedPreferences notif = Prefs.getNotif(context);
        return notif.contains(soundKey) ? notif.getBoolean(soundKey, false) : this.mAllowFloatPackages.contains(str);
    }

    public void setSound(Context context, String str, boolean z) {
        Prefs.getNotif(context).edit().putBoolean(FilterHelperCompat.getSoundKey(str), z).apply();
    }

    public boolean canVibrate(Context context, String str) {
        String vibrateKey = FilterHelperCompat.getVibrateKey(str);
        SharedPreferences notif = Prefs.getNotif(context);
        return notif.contains(vibrateKey) ? notif.getBoolean(vibrateKey, false) : this.mAllowFloatPackages.contains(str);
    }

    public void setVibrate(Context context, String str, boolean z) {
        Prefs.getNotif(context).edit().putBoolean(FilterHelperCompat.getVibrateKey(str), z).apply();
    }

    public boolean canLights(Context context, String str) {
        String lightsKey = FilterHelperCompat.getLightsKey(str);
        SharedPreferences notif = Prefs.getNotif(context);
        return notif.contains(lightsKey) ? notif.getBoolean(lightsKey, false) : this.mAllowFloatPackages.contains(str);
    }

    public void setLights(Context context, String str, boolean z) {
        Prefs.getNotif(context).edit().putBoolean(FilterHelperCompat.getLightsKey(str), z).apply();
    }

    public boolean isSystemApp(String str) {
        Boolean bool = this.mSystemApps.get(str);
        if (bool == null) {
            try {
                bool = Boolean.valueOf(this.mContext.getPackageManager().getApplicationInfo(str, 0).isSystemApp());
                this.mSystemApps.put(str, bool);
            } catch (Exception unused) {
            }
        }
        if (bool == null) {
            return false;
        }
        return bool.booleanValue();
    }

    public boolean isImportantWhitelist(String str) {
        return this.mImportantWhitelist.contains(str);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("mAllowFloatPackages: " + this.mAllowFloatPackages);
        printWriter.println("mAllowKeyguardPackages: " + this.mAllowKeyguardPackages);
        printWriter.println("mCanShowBadgePackages: " + this.mCanShowBadgePackages);
        printWriter.println("AppNotificationSettings:");
        for (Map.Entry<String, ?> entry : Prefs.getNotif(this.mContext).getAll().entrySet()) {
            printWriter.print("  ");
            printWriter.print(entry.getKey());
            printWriter.print("=");
            printWriter.println(entry.getValue());
        }
    }

    /* access modifiers changed from: private */
    public static class FilterHelperCompat {
        public static String getFoldImportanceKey(String str) {
            return String.format("%s_%s", str, "importance");
        }

        public static String getBadgeKey(String str) {
            return String.format("%s_%s", str, "message");
        }

        public static String getFloatKey(String str, String str2) {
            if (TextUtils.isEmpty(str2) || "miscellaneous".equals(str2)) {
                return str;
            }
            return String.format("%s_%s_channel_flag", str, str2);
        }

        public static String getKeyguardKey(String str, String str2) {
            if (TextUtils.isEmpty(str2) || "miscellaneous".equals(str2)) {
                return String.format("%s_%s", str, "keyguard");
            }
            return String.format("%s_%s_%s", str, str2, "keyguard");
        }

        public static String getSoundKey(String str) {
            return String.format("%s_%s", str, "sound");
        }

        public static String getVibrateKey(String str) {
            return String.format("%s_%s", str, "vibrate");
        }

        public static String getLightsKey(String str) {
            return String.format("%s_%s", str, "led");
        }
    }
}
