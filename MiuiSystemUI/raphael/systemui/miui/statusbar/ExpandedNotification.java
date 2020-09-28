package com.android.systemui.miui.statusbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.xiaomi.stat.c.c;

public class ExpandedNotification extends StatusBarNotification {
    private Drawable mAppIcon;
    private String mAppName;
    private int mAppUid;
    private boolean mCanFloat;
    private boolean mCanShowOnKeyguard;
    private boolean mFullscreen;
    private boolean mHasShownAfterUnlock;
    private int mImportance = -1000;
    private boolean mIsPrioritizedApp;
    private boolean mIsSystemApp;
    private boolean mPeek;
    private String mPkgName;
    private Drawable mRowIcon;
    private int mTargetSdk;

    public ExpandedNotification(Context context, StatusBarNotification statusBarNotification) {
        super(statusBarNotification.getPackageName(), statusBarNotification.getOpPkg(), statusBarNotification.getId(), statusBarNotification.getTag(), statusBarNotification.getUid(), statusBarNotification.getInitialPid(), statusBarNotification.getNotification(), statusBarNotification.getUser(), statusBarNotification.getOverrideGroupKey(), statusBarNotification.getPostTime());
        NotificationSettingsManager notificationSettingsManager = (NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class);
        CharSequence targetPkg = MiuiNotificationCompat.getTargetPkg(getNotification());
        this.mPkgName = (!notificationSettingsManager.canSendSubstituteNotification(statusBarNotification.getPackageName()) || TextUtils.isEmpty(targetPkg)) ? statusBarNotification.getPackageName() : targetPkg.toString();
        this.mIsSystemApp = Util.isSystemApp(context, this.mPkgName);
        this.mIsPrioritizedApp = notificationSettingsManager.isPrioritizedApp(this.mPkgName);
        this.mCanFloat = NotificationSettingsHelper.checkFloat(context, this.mPkgName, getNotification().getChannelId());
        this.mCanShowOnKeyguard = NotificationSettingsHelper.checkKeyguard(context, this.mPkgName, getNotification().getChannelId());
    }

    public void setAppName(String str) {
        this.mAppName = str;
    }

    public String getAppName() {
        if (NotificationUtil.isHybrid(this)) {
            String hybridAppName = NotificationUtil.getHybridAppName(this);
            if (!TextUtils.isEmpty(hybridAppName)) {
                return hybridAppName;
            }
        }
        return this.mAppName;
    }

    public String getTargetPackageName() {
        if (NotificationUtil.isHybrid(this)) {
            String category = NotificationUtil.getCategory(this);
            if (!TextUtils.isEmpty(category)) {
                return category;
            }
        }
        return getPackageName();
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public String getBasePkg() {
        return super.getPackageName();
    }

    public boolean isSubstituteNotification() {
        return !TextUtils.equals(this.mPkgName, getBasePkg()) || !TextUtils.equals(this.mPkgName, getOpPkg());
    }

    public void setAppIcon(Drawable drawable) {
        this.mAppIcon = drawable;
    }

    public Drawable getAppIcon() {
        return this.mAppIcon;
    }

    public void setRowIcon(Drawable drawable) {
        this.mRowIcon = drawable;
    }

    public void setAppUid(int i) {
        this.mAppUid = i;
    }

    public int getAppUid() {
        return this.mAppUid;
    }

    public void setTargetSdk(int i) {
        this.mTargetSdk = i;
    }

    public int getTargetSdk() {
        return this.mTargetSdk;
    }

    public boolean isPrioritizedApp() {
        return this.mIsPrioritizedApp;
    }

    public void setImportance(int i) {
        this.mImportance = i;
    }

    public int getImportance() {
        return this.mImportance;
    }

    public boolean canFloat() {
        return this.mCanFloat && isEnableFloat();
    }

    public boolean canShowOnKeyguard() {
        return this.mCanShowOnKeyguard && isEnableKeyguard();
    }

    public void notePeek(boolean z) {
        this.mPeek = z;
    }

    public void noteFullscreen(boolean z) {
        this.mFullscreen = z;
    }

    public boolean hasShownAfterUnlock() {
        return !isKeptOnKeyguard() && this.mHasShownAfterUnlock;
    }

    public void setHasShownAfterUnlock(boolean z) {
        this.mHasShownAfterUnlock = isClearable() && !isOnlyShowKeyguard() && z;
    }

    public boolean isShowMiuiAction() {
        return NotificationUtil.showMiuiStyle() && MiuiNotificationCompat.isShowMiuiAction(getNotification());
    }

    public CharSequence getMiuiActionTitle() {
        return MiuiNotificationCompat.isShowMiuiAction(getNotification()) ? getNotification().actions[0].title : "";
    }

    public boolean isEnableFloat() {
        return MiuiNotificationCompat.isEnableFloat(getNotification());
    }

    public boolean isEnableKeyguard() {
        return MiuiNotificationCompat.isEnableKeyguard(getNotification());
    }

    public int getFloatTime() {
        return MiuiNotificationCompat.getFloatTime(getNotification());
    }

    public int getMessageCount() {
        return MiuiNotificationCompat.getMessageCount(getNotification());
    }

    public CharSequence getMessageClassName() {
        return MiuiNotificationCompat.getMessageClassName(getNotification());
    }

    public boolean isOnlyShowKeyguard() {
        return MiuiNotificationCompat.isOnlyShowKeyguard(getNotification());
    }

    public boolean isKeptOnKeyguard() {
        return MiuiNotificationCompat.isKeptOnKeyguard(getNotification());
    }

    public boolean isCustomHeight() {
        return MiuiNotificationCompat.isCustomHeight(getNotification());
    }

    public boolean isCustomHideBorder() {
        String opPkg = getOpPkg();
        if (TextUtils.isEmpty(opPkg) || !TextUtils.equals(opPkg, c.a)) {
            return false;
        }
        return MiuiNotificationCompat.isCustomHideBorder(getNotification());
    }

    public boolean isSystemWarnings() {
        return this.mIsSystemApp && MiuiNotificationCompat.isSystemWarnings(getNotification());
    }

    public boolean isShowingAtTail() {
        return MiuiNotificationCompat.isShowingAtTail(getNotification());
    }

    public boolean isPersistent() {
        return this.mIsSystemApp && MiuiNotificationCompat.isPersistent(getNotification());
    }

    public boolean isMiuiActionExpandable() {
        return getNotification().extras.getBoolean("miui.actionExpandable");
    }

    public boolean isExpandableOnKeyguard() {
        return getNotification().extras.getBoolean("miui.expandableOnKeyguard");
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        String str10;
        String str11;
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\n   ");
        sb.append(" pkgName=");
        sb.append(this.mPkgName);
        sb.append(" appUid=");
        sb.append(this.mAppUid);
        sb.append(" sdk=");
        sb.append(this.mTargetSdk);
        sb.append(" imp=");
        sb.append(this.mImportance);
        sb.append(" sysApp=");
        String str12 = "T";
        sb.append(this.mIsSystemApp ? str12 : "F");
        sb.append(" priApp=");
        if (this.mIsPrioritizedApp) {
            str = str12;
        } else {
            str = "F";
        }
        sb.append(str);
        sb.append(" hasShown=");
        if (this.mHasShownAfterUnlock) {
            str2 = str12;
        } else {
            str2 = "F";
        }
        sb.append(str2);
        sb.append(" float=");
        if (this.mCanFloat) {
            str3 = str12;
        } else {
            str3 = "F";
        }
        sb.append(str3);
        sb.append(" keyguard=");
        if (this.mCanShowOnKeyguard) {
            str4 = str12;
        } else {
            str4 = "F";
        }
        sb.append(str4);
        sb.append(" peek=");
        if (this.mPeek) {
            str5 = str12;
        } else {
            str5 = "F";
        }
        sb.append(str5);
        sb.append(" fullscreen=");
        if (this.mFullscreen) {
            str6 = str12;
        } else {
            str6 = "F";
        }
        sb.append(str6);
        sb.append("\n   ");
        sb.append(" showMiuiAction=");
        if (isShowMiuiAction()) {
            str7 = str12;
        } else {
            str7 = "F";
        }
        sb.append(str7);
        sb.append(" enableFloat=");
        if (isEnableFloat()) {
            str8 = str12;
        } else {
            str8 = "F";
        }
        sb.append(str8);
        sb.append(" enableKeyguard=");
        if (isEnableKeyguard()) {
            str9 = str12;
        } else {
            str9 = "F";
        }
        sb.append(str9);
        sb.append(" floatTime=");
        sb.append(getFloatTime());
        sb.append(" messageCount=");
        sb.append(getMessageCount());
        sb.append(" persistent=");
        if (isPersistent()) {
            str10 = str12;
        } else {
            str10 = "F";
        }
        sb.append(str10);
        sb.append(" customHeight=");
        if (isCustomHeight()) {
            str11 = str12;
        } else {
            str11 = "F";
        }
        sb.append(str11);
        sb.append(" customHideBorder=");
        if (!isCustomHideBorder()) {
            str12 = "F";
        }
        sb.append(str12);
        return sb.toString();
    }
}
