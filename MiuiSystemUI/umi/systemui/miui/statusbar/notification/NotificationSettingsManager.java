package com.android.systemui.miui.statusbar.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.Dumpable;
import com.android.systemui.Util;
import com.android.systemui.miui.PackageEventReceiver;
import com.android.systemui.miui.statusbar.CloudDataHelper;
import com.android.systemui.plugins.R;
import com.miui.systemui.annotation.Inject;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import miui.util.NotificationFilterHelper;

public class NotificationSettingsManager implements Dumpable, PackageEventReceiver {
    private static final boolean DEBUG = Constants.DEBUG;
    private static final boolean USE_WHITE_LISTS = (!Constants.IS_INTERNATIONAL);
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
    private List<String> mForcedEnabledPackages;
    private List<String> mHideAlertWindowWhitelist;
    private List<String> mHideForegroundWhitelist;
    private List<String> mPreInstallPackages;
    private List<String> mPrioritizedPackages;
    private List<String> mSubstitutePackages;

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public void onPackageAdded(int i, String str, boolean z) {
    }

    public void onPackageChanged(int i, String str) {
    }

    public void onPackageRemoved(int i, String str, boolean z, boolean z2) {
    }

    public NotificationSettingsManager(@Inject Context context, @Inject(tag = "SysUiBg") Looper looper) {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mBgHandler = new Handler(looper);
        this.mPrioritizedPackages = Arrays.asList(resources.getStringArray(R.array.config_prioritizedPackages));
        this.mSubstitutePackages = Arrays.asList(resources.getStringArray(R.array.config_canSendSubstituteNotificationPackages));
        this.mCustomAppIconPackages = Arrays.asList(resources.getStringArray(R.array.config_canCustomNotificationAppIcon));
        this.mDisableAutoGroupSummaryPackages = Arrays.asList(resources.getStringArray(R.array.config_disableAutoGroupSummaryPackages));
        this.mHideForegroundWhitelist = Arrays.asList(resources.getStringArray(R.array.system_foreground_notification_whitelist));
        this.mHideAlertWindowWhitelist = Arrays.asList(resources.getStringArray(R.array.system_alert_window_notification_whitelist));
        this.mAvoidDisturbPackages = Arrays.asList(resources.getStringArray(R.array.avoid_disturb_app_whitelist));
        this.mPreInstallPackages = Arrays.asList(resources.getStringArray(R.array.config_preInstalledPackages));
        this.mCanShowBadgePackages = Arrays.asList(resources.getStringArray(R.array.config_canShowBadgePackages));
        this.mAllowFloatPackages = Arrays.asList(resources.getStringArray(R.array.config_allowFloatPackages));
        this.mAllowKeyguardPackages = Arrays.asList(resources.getStringArray(R.array.config_allowKeyguardPackages));
        this.mBlockFloatPackages = Arrays.asList(resources.getStringArray(R.array.config_blockFloatPackages));
        this.mBlockKeyguardPackages = Arrays.asList(resources.getStringArray(R.array.config_blockKeyguardPackages));
        this.mForcedEnabledPackages = Arrays.asList(resources.getStringArray(17236056));
        this.mAllowNotificationSlide = Arrays.asList(resources.getStringArray(R.array.config_allowNotificationSlide));
        this.mBgHandler.post(new Runnable() {
            public void run() {
                NotificationSettingsManager.this.onCloudDataUpdated();
                NotificationSettingsManager.this.updateWhiteListIfNeeded();
                NotificationSettingsManager.this.enableNotificationIfNeeded();
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateWhiteListIfNeeded() {
        NotificationFilterHelper.updateFloatWhiteList(this.mContext, this.mAllowFloatPackages);
        NotificationFilterHelper.updateKeyguardWhitelist(this.mContext, this.mAllowKeyguardPackages);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000c, code lost:
        r1 = r0.next();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void enableNotificationIfNeeded() {
        /*
            r5 = this;
            java.util.List<java.lang.String> r0 = r5.mForcedEnabledPackages
            java.util.Iterator r0 = r0.iterator()
        L_0x0006:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x002d
            java.lang.Object r1 = r0.next()
            java.lang.String r1 = (java.lang.String) r1
            android.content.Context r2 = r5.mContext
            int r2 = com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper.getPackageUid(r2, r1)
            if (r2 > 0) goto L_0x001b
            return
        L_0x001b:
            boolean r3 = com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper.isNotificationsBanned((java.lang.String) r1, (int) r2)
            r4 = 1
            if (r3 == 0) goto L_0x0027
            android.content.Context r3 = r5.mContext
            com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper.setNotificationsEnabledForPackage(r3, r1, r2, r4)
        L_0x0027:
            android.content.Context r2 = r5.mContext
            com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper.setFoldImportance(r2, r1, r4)
            goto L_0x0006
        L_0x002d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.statusbar.notification.NotificationSettingsManager.enableNotificationIfNeeded():void");
    }

    public void onCloudDataUpdated() {
        List<String> floatWhitelist = CloudDataHelper.getFloatWhitelist(this.mContext);
        if (floatWhitelist != null && !floatWhitelist.isEmpty()) {
            this.mAllowFloatPackages = floatWhitelist;
        }
        List<String> keyguardWhitelist = CloudDataHelper.getKeyguardWhitelist(this.mContext);
        if (keyguardWhitelist != null && !keyguardWhitelist.isEmpty()) {
            this.mAllowKeyguardPackages = keyguardWhitelist;
        }
        List<String> floatBlacklist = CloudDataHelper.getFloatBlacklist(this.mContext);
        if (floatBlacklist != null && !floatBlacklist.isEmpty()) {
            this.mBlockFloatPackages = floatBlacklist;
        }
        List<String> keyguardBlacklist = CloudDataHelper.getKeyguardBlacklist(this.mContext);
        if (keyguardBlacklist != null && !keyguardBlacklist.isEmpty()) {
            this.mBlockKeyguardPackages = keyguardBlacklist;
        }
        List<String> badgeWhitelist = CloudDataHelper.getBadgeWhitelist(this.mContext);
        if (badgeWhitelist != null && !badgeWhitelist.isEmpty()) {
            this.mCanShowBadgePackages = badgeWhitelist;
        }
        List<String> slideWhiteList = CloudDataHelper.getSlideWhiteList(this.mContext);
        if (slideWhiteList != null && !slideWhiteList.isEmpty()) {
            this.mAllowNotificationSlide = slideWhiteList;
        }
    }

    public boolean isPrioritizedApp(String str) {
        return this.mPrioritizedPackages.contains(str);
    }

    public boolean canSendSubstituteNotification(String str) {
        return Constants.DEBUG || this.mSubstitutePackages.contains(str);
    }

    public boolean canCustomAppIcon(String str) {
        return Constants.DEBUG || this.mCustomAppIconPackages.contains(str);
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
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(foldImportanceKey)) {
            return sharedPreferences.getInt(foldImportanceKey, 0);
        }
        return 0;
    }

    public void setFoldImportance(Context context, String str, int i) {
        getSharedPreferences(context).edit().putInt(FilterHelperCompat.getFoldImportanceKey(str), i).apply();
    }

    public boolean canShowBadge(Context context, String str) {
        String badgeKey = FilterHelperCompat.getBadgeKey(str);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(badgeKey)) {
            return sharedPreferences.getBoolean(badgeKey, false);
        }
        if (this.mCanShowBadgePackages.contains(str) || this.mPreInstallPackages.contains(str) || Util.isSystemApp(context, str)) {
            return true;
        }
        return false;
    }

    public boolean isInNotificationSlideWhiteList(String str) {
        return this.mAllowNotificationSlide.contains(str);
    }

    public void setShowBadge(Context context, String str, boolean z) {
        String badgeKey = FilterHelperCompat.getBadgeKey(str);
        if (DEBUG) {
            Log.d("NotifiSettingsManager", String.format("setShowBadge key=%s enabled=%s", new Object[]{badgeKey, Boolean.valueOf(z)}));
        }
        getSharedPreferences(context).edit().putBoolean(badgeKey, z).apply();
    }

    public boolean canFloat(Context context, String str, String str2) {
        boolean canFloat = canFloat(context, str);
        if (!canFloat || TextUtils.isEmpty(str2)) {
            return canFloat;
        }
        String floatKey = FilterHelperCompat.getFloatKey(str, str2);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (!sharedPreferences.contains(floatKey)) {
            boolean isXmsfChannel = NotificationUtil.isXmsfChannel(str, str2);
            if (USE_WHITE_LISTS) {
                if (!isXmsfChannel) {
                    return this.mAllowFloatPackages.contains(str);
                }
            } else if (this.mBlockFloatPackages.contains(str)) {
                return false;
            }
            return true;
        } else if (sharedPreferences.getInt(floatKey, 1) == 2) {
            return true;
        } else {
            return false;
        }
    }

    private boolean canFloat(Context context, String str) {
        String floatKey = FilterHelperCompat.getFloatKey(str, (String) null);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(floatKey)) {
            if (sharedPreferences.getInt(floatKey, 1) == 2) {
                return true;
            }
            return false;
        } else if (!USE_WHITE_LISTS && this.mBlockFloatPackages.contains(str)) {
            return false;
        } else {
            return true;
        }
    }

    public void setFloat(Context context, String str, String str2, boolean z) {
        String floatKey = FilterHelperCompat.getFloatKey(str, str2);
        int i = 1;
        if (DEBUG) {
            Log.d("NotifiSettingsManager", String.format("setFloat key=%s enabled=%s", new Object[]{floatKey, Boolean.valueOf(z)}));
        }
        if (z) {
            i = 2;
        }
        getSharedPreferences(context).edit().putInt(floatKey, i).apply();
    }

    public boolean canShowOnKeyguard(Context context, String str, String str2) {
        boolean canShowOnKeyguard = canShowOnKeyguard(context, str);
        if (!canShowOnKeyguard || TextUtils.isEmpty(str2)) {
            return canShowOnKeyguard;
        }
        String keyguardKey = FilterHelperCompat.getKeyguardKey(str, str2);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(keyguardKey)) {
            return sharedPreferences.getBoolean(keyguardKey, false);
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
        String keyguardKey = FilterHelperCompat.getKeyguardKey(str, (String) null);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(keyguardKey)) {
            return sharedPreferences.getBoolean(keyguardKey, false);
        }
        if (!USE_WHITE_LISTS && this.mBlockKeyguardPackages.contains(str)) {
            return false;
        }
        return true;
    }

    public void setShowOnKeyguard(Context context, String str, String str2, boolean z) {
        String keyguardKey = FilterHelperCompat.getKeyguardKey(str, str2);
        if (DEBUG) {
            Log.d("NotifiSettingsManager", String.format("setShowOnKeyguard key=%s enabled=%s", new Object[]{keyguardKey, Boolean.valueOf(z)}));
        }
        getSharedPreferences(context).edit().putBoolean(keyguardKey, z).apply();
    }

    public boolean canSound(Context context, String str) {
        String soundKey = FilterHelperCompat.getSoundKey(str);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.contains(soundKey) ? sharedPreferences.getBoolean(soundKey, false) : this.mAllowKeyguardPackages.contains(str);
    }

    public void setSound(Context context, String str, boolean z) {
        getSharedPreferences(context).edit().putBoolean(FilterHelperCompat.getSoundKey(str), z).apply();
    }

    public boolean canVibrate(Context context, String str) {
        String vibrateKey = FilterHelperCompat.getVibrateKey(str);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(vibrateKey)) {
            return sharedPreferences.getBoolean(vibrateKey, true);
        }
        return true;
    }

    public void setVibrate(Context context, String str, boolean z) {
        getSharedPreferences(context).edit().putBoolean(FilterHelperCompat.getVibrateKey(str), z).apply();
    }

    public boolean canLights(Context context, String str) {
        String lightsKey = FilterHelperCompat.getLightsKey(str);
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.contains(lightsKey) ? sharedPreferences.getBoolean(lightsKey, false) : this.mAllowFloatPackages.contains(str);
    }

    public void setLights(Context context, String str, boolean z) {
        getSharedPreferences(context).edit().putBoolean(FilterHelperCompat.getLightsKey(str), z).apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("app_notification", 4);
    }

    private static class FilterHelperCompat {
        public static String getFoldImportanceKey(String str) {
            return String.format("%s_%s", new Object[]{str, "importance"});
        }

        public static String getBadgeKey(String str) {
            return String.format("%s_%s", new Object[]{str, "message"});
        }

        public static String getFloatKey(String str, String str2) {
            if (TextUtils.isEmpty(str2) || "miscellaneous".equals(str2)) {
                return str;
            }
            return String.format("%s_%s_channel_flag", new Object[]{str, str2});
        }

        public static String getKeyguardKey(String str, String str2) {
            if (TextUtils.isEmpty(str2) || "miscellaneous".equals(str2)) {
                return String.format("%s_%s", new Object[]{str, "keyguard"});
            }
            return String.format("%s_%s_%s", new Object[]{str, str2, "keyguard"});
        }

        public static String getSoundKey(String str) {
            return String.format("%s_%s", new Object[]{str, "sound"});
        }

        public static String getVibrateKey(String str) {
            return String.format("%s_%s", new Object[]{str, "vibrate"});
        }

        public static String getLightsKey(String str) {
            return String.format("%s_%s", new Object[]{str, "led"});
        }
    }
}
