package com.android.systemui.statusbar.notification;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.android.systemui.Dependency;

public class ExpandedNotification extends StatusBarNotification {
    private Drawable mAppIcon;
    private String mAppName;
    private int mAppUid;
    private boolean mCanFloat;
    private boolean mCanShowOnKeyguard;
    private boolean mFullscreen;
    private boolean mHasShownAfterUnlock;
    private boolean mIsPrioritizedApp;
    private boolean mIsSystemApp;
    private boolean mPeek;
    private String mPkgName;
    private int mTargetSdk;
    public long seeTime;

    public ExpandedNotification(StatusBarNotification statusBarNotification) {
        super(statusBarNotification.getPackageName(), statusBarNotification.getOpPkg(), statusBarNotification.getId(), statusBarNotification.getTag(), statusBarNotification.getUid(), statusBarNotification.getInitialPid(), statusBarNotification.getNotification(), statusBarNotification.getUser(), statusBarNotification.getOverrideGroupKey(), statusBarNotification.getPostTime());
        CharSequence targetPkg = MiuiNotificationCompat.getTargetPkg(getNotification());
        String packageName = (!NotificationSettingsHelper.canSendSubstituteNotification(statusBarNotification.getPackageName()) || TextUtils.isEmpty(targetPkg)) ? statusBarNotification.getPackageName() : targetPkg.toString();
        this.mPkgName = packageName;
        this.mIsSystemApp = NotificationSettingsHelper.isSystemApp(packageName);
        this.mIsPrioritizedApp = NotificationSettingsHelper.isPrioritizedApp(this.mPkgName);
        this.mCanFloat = NotificationSettingsHelper.checkFloat(this.mPkgName, getNotification().getChannelId());
        this.mCanShowOnKeyguard = NotificationSettingsHelper.checkKeyguard(this.mPkgName, getNotification().getChannelId());
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

    public String getOpPkg() {
        return TextUtils.isEmpty(super.getOpPkg()) ? super.getPackageName() : super.getOpPkg();
    }

    public boolean isSubstituteNotification() {
        return !TextUtils.equals(this.mPkgName, getOpPkg());
    }

    public void setAppIcon(Drawable drawable) {
        this.mAppIcon = drawable;
    }

    public Drawable getAppIcon() {
        return this.mAppIcon;
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

    public boolean canFloat() {
        return this.mCanFloat && isEnableFloat();
    }

    public boolean canShowOnKeyguard() {
        return this.mCanShowOnKeyguard && isEnableKeyguard();
    }

    public boolean hasShownAfterUnlock() {
        if (isOnlyShowKeyguard()) {
            return false;
        }
        return this.mHasShownAfterUnlock;
    }

    public void setHasShownAfterUnlock(boolean z) {
        if (!isOnlyShowKeyguard()) {
            if (isClearable() || getNotification().isGroupSummary()) {
                this.mHasShownAfterUnlock = z;
            }
        }
    }

    public boolean isShowMiuiAction() {
        return NotificationSettingsHelper.showMiuiStyle() && MiuiNotificationCompat.isShowMiuiAction(getNotification());
    }

    public boolean isEnableFloat() {
        return MiuiNotificationCompat.isEnableFloat(getNotification());
    }

    public boolean isFloatWhenDnd() {
        return this.mIsSystemApp && MiuiNotificationCompat.isFloatWhenDnd(getNotification());
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

    public boolean isCustomHeight() {
        return MiuiNotificationCompat.isCustomHeight(getNotification());
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

    public PendingIntent getLongPressIntent() {
        if (this.mIsSystemApp || ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canSendSubstituteNotification(getOpPkg())) {
            return MiuiNotificationCompat.getLongPressIntent(getNotification());
        }
        return null;
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
        sb.append(" floatWhenDnd=");
        if (isFloatWhenDnd()) {
            str9 = str12;
        } else {
            str9 = "F";
        }
        sb.append(str9);
        sb.append(" enableKeyguard=");
        if (isEnableKeyguard()) {
            str10 = str12;
        } else {
            str10 = "F";
        }
        sb.append(str10);
        sb.append(" floatTime=");
        sb.append(getFloatTime());
        sb.append(" messageCount=");
        sb.append(getMessageCount());
        sb.append(" persistent=");
        if (isPersistent()) {
            str11 = str12;
        } else {
            str11 = "F";
        }
        sb.append(str11);
        sb.append(" customHeight=");
        if (!isCustomHeight()) {
            str12 = "F";
        }
        sb.append(str12);
        return sb.toString();
    }
}
