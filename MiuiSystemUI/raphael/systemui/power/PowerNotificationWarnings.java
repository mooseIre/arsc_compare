package com.android.systemui.power;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import com.android.settingslib.Utils;
import com.android.settingslib.fuelgauge.BatterySaverUtils;
import com.android.settingslib.utils.PowerUtil;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.power.PowerUI;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.util.NotificationChannels;
import com.android.systemui.volume.Events;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class PowerNotificationWarnings implements PowerUI.WarningsUI {
    private static final boolean DEBUG = PowerUI.DEBUG;
    private static final String[] SHOWING_STRINGS = {"SHOWING_NOTHING", "SHOWING_WARNING", "SHOWING_SAVER", "SHOWING_INVALID_CHARGER", "SHOWING_AUTO_SAVER_SUGGESTION"};
    private ActivityStarter mActivityStarter;
    private int mBatteryLevel;
    private int mBucket;
    private final Context mContext;
    private BatteryStateSnapshot mCurrentBatterySnapshot;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private SystemUIDialog mHighTempDialog;
    private boolean mHighTempWarning;
    private boolean mInvalidCharger;
    private final KeyguardManager mKeyguard;
    private final NotificationManager mNoMan;
    private final Intent mOpenBatterySettings = settings("android.intent.action.POWER_USAGE_SUMMARY");
    private boolean mPlaySound;
    private final PowerManager mPowerMan;
    private final Receiver mReceiver = new Receiver();
    private SystemUIDialog mSaverConfirmation;
    private boolean mShowAutoSaverSuggestion;
    private int mShowing;
    private SystemUIDialog mThermalShutdownDialog;
    SystemUIDialog mUsbHighTempDialog;
    private boolean mWarning;
    private long mWarningTriggerTimeMs;

    static {
        new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    }

    public PowerNotificationWarnings(Context context, ActivityStarter activityStarter) {
        this.mContext = context;
        this.mNoMan = (NotificationManager) context.getSystemService(NotificationManager.class);
        this.mPowerMan = (PowerManager) context.getSystemService("power");
        this.mKeyguard = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
        this.mReceiver.init();
        this.mActivityStarter = activityStarter;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dump(PrintWriter printWriter) {
        printWriter.print("mWarning=");
        printWriter.println(this.mWarning);
        printWriter.print("mPlaySound=");
        printWriter.println(this.mPlaySound);
        printWriter.print("mInvalidCharger=");
        printWriter.println(this.mInvalidCharger);
        printWriter.print("mShowing=");
        printWriter.println(SHOWING_STRINGS[this.mShowing]);
        printWriter.print("mSaverConfirmation=");
        String str = "not null";
        printWriter.println(this.mSaverConfirmation != null ? str : null);
        printWriter.print("mSaverEnabledConfirmation=");
        printWriter.print("mHighTempWarning=");
        printWriter.println(this.mHighTempWarning);
        printWriter.print("mHighTempDialog=");
        printWriter.println(this.mHighTempDialog != null ? str : null);
        printWriter.print("mThermalShutdownDialog=");
        printWriter.println(this.mThermalShutdownDialog != null ? str : null);
        printWriter.print("mUsbHighTempDialog=");
        if (this.mUsbHighTempDialog == null) {
            str = null;
        }
        printWriter.println(str);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void update(int i, int i2, long j) {
        this.mBatteryLevel = i;
        if (i2 >= 0) {
            this.mWarningTriggerTimeMs = 0;
        } else if (i2 < this.mBucket) {
            this.mWarningTriggerTimeMs = System.currentTimeMillis();
        }
        this.mBucket = i2;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void updateSnapshot(BatteryStateSnapshot batteryStateSnapshot) {
        this.mCurrentBatterySnapshot = batteryStateSnapshot;
    }

    private void updateNotification() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "updateNotification mWarning=" + this.mWarning + " mPlaySound=" + this.mPlaySound + " mInvalidCharger=" + this.mInvalidCharger);
        }
        if (this.mInvalidCharger) {
            showInvalidChargerNotification();
            this.mShowing = 3;
        } else if (this.mWarning) {
            showWarningNotification();
            this.mShowing = 1;
        } else if (this.mShowAutoSaverSuggestion) {
            if (this.mShowing != 4) {
                showAutoSaverSuggestionNotification();
            }
            this.mShowing = 4;
        } else {
            this.mNoMan.cancelAsUser("low_battery", 2, UserHandle.ALL);
            this.mNoMan.cancelAsUser("low_battery", 3, UserHandle.ALL);
            this.mNoMan.cancelAsUser("auto_saver", 49, UserHandle.ALL);
            this.mShowing = 0;
        }
    }

    private void showInvalidChargerNotification() {
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(C0013R$drawable.ic_power_low).setWhen(0).setShowWhen(false).setOngoing(true).setContentTitle(this.mContext.getString(C0021R$string.invalid_charger_title)).setContentText(this.mContext.getString(C0021R$string.invalid_charger_text)).setColor(this.mContext.getColor(17170460));
        SystemUI.overrideNotificationAppName(this.mContext, color, false);
        Notification build = color.build();
        this.mNoMan.cancelAsUser("low_battery", 3, UserHandle.ALL);
        this.mNoMan.notifyAsUser("low_battery", 2, build, UserHandle.ALL);
    }

    /* access modifiers changed from: protected */
    public void showWarningNotification() {
        String str;
        String format = NumberFormat.getPercentInstance().format(((double) this.mCurrentBatterySnapshot.getBatteryLevel()) / 100.0d);
        String string = this.mContext.getString(C0021R$string.battery_low_title);
        if (this.mCurrentBatterySnapshot.isHybrid()) {
            str = getHybridContentString(format);
        } else {
            str = this.mContext.getString(C0021R$string.battery_low_percent_format, format);
        }
        Notification.Builder visibility = new Notification.Builder(this.mContext, NotificationChannels.BATTERY).setSmallIcon(C0013R$drawable.ic_power_low).setWhen(this.mWarningTriggerTimeMs).setShowWhen(false).setContentText(str).setContentTitle(string).setOnlyAlertOnce(true).setDeleteIntent(pendingBroadcast("PNW.dismissedWarning")).setStyle(new Notification.BigTextStyle().bigText(str)).setVisibility(1);
        if (hasBatterySettings()) {
            visibility.setContentIntent(pendingBroadcast("PNW.batterySettings"));
        }
        if (!this.mCurrentBatterySnapshot.isHybrid() || this.mBucket < 0 || this.mCurrentBatterySnapshot.getTimeRemainingMillis() < this.mCurrentBatterySnapshot.getSevereThresholdMillis()) {
            visibility.setColor(Utils.getColorAttrDefaultColor(this.mContext, 16844099));
        }
        if (!this.mPowerMan.isPowerSaveMode()) {
            visibility.addAction(0, this.mContext.getString(C0021R$string.battery_saver_start_action), pendingBroadcast("PNW.startSaver"));
        }
        visibility.setOnlyAlertOnce(!this.mPlaySound);
        this.mPlaySound = false;
        SystemUI.overrideNotificationAppName(this.mContext, visibility, false);
        Notification build = visibility.build();
        this.mNoMan.cancelAsUser("low_battery", 2, UserHandle.ALL);
        this.mNoMan.notifyAsUser("low_battery", 3, build, UserHandle.ALL);
    }

    private void showAutoSaverSuggestionNotification() {
        String string = this.mContext.getString(C0021R$string.auto_saver_text);
        Notification.Builder contentText = new Notification.Builder(this.mContext, NotificationChannels.HINTS).setSmallIcon(C0013R$drawable.ic_power_saver).setWhen(0).setShowWhen(false).setContentTitle(this.mContext.getString(C0021R$string.auto_saver_title)).setStyle(new Notification.BigTextStyle().bigText(string)).setContentText(string);
        contentText.setContentIntent(pendingBroadcast("PNW.enableAutoSaver"));
        contentText.setDeleteIntent(pendingBroadcast("PNW.dismissAutoSaverSuggestion"));
        contentText.addAction(0, this.mContext.getString(C0021R$string.no_auto_saver_action), pendingBroadcast("PNW.autoSaverNoThanks"));
        SystemUI.overrideNotificationAppName(this.mContext, contentText, false);
        this.mNoMan.notifyAsUser("auto_saver", 49, contentText.build(), UserHandle.ALL);
    }

    private String getHybridContentString(String str) {
        return PowerUtil.getBatteryRemainingStringFormatted(this.mContext, this.mCurrentBatterySnapshot.getTimeRemainingMillis(), str, this.mCurrentBatterySnapshot.isBasedOnUsage());
    }

    private PendingIntent pendingBroadcast(String str) {
        return PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(str).setPackage(this.mContext.getPackageName()).setFlags(268435456), 0, UserHandle.CURRENT);
    }

    private static Intent settings(String str) {
        return new Intent(str).setFlags(1551892480);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public boolean isInvalidChargerWarningShowing() {
        return this.mInvalidCharger;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissHighTemperatureWarning() {
        if (this.mHighTempWarning) {
            dismissHighTemperatureWarningInternal();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissHighTemperatureWarningInternal() {
        this.mNoMan.cancelAsUser("high_temp", 4, UserHandle.ALL);
        this.mHighTempWarning = false;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showHighTemperatureWarning() {
        if (!this.mHighTempWarning) {
            this.mHighTempWarning = true;
            Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(C0013R$drawable.ic_device_thermostat_24).setWhen(0).setShowWhen(false).setContentTitle(this.mContext.getString(C0021R$string.high_temp_title)).setContentText(this.mContext.getString(C0021R$string.high_temp_notif_message)).setVisibility(1).setContentIntent(pendingBroadcast("PNW.clickedTempWarning")).setDeleteIntent(pendingBroadcast("PNW.dismissedTempWarning")).setColor(Utils.getColorAttrDefaultColor(this.mContext, 16844099));
            SystemUI.overrideNotificationAppName(this.mContext, color, false);
            this.mNoMan.notifyAsUser("high_temp", 4, color.build(), UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showHighTemperatureDialog() {
        if (this.mHighTempDialog == null) {
            SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
            systemUIDialog.setIconAttribute(16843605);
            systemUIDialog.setTitle(C0021R$string.high_temp_title);
            systemUIDialog.setMessage(C0021R$string.high_temp_dialog_message);
            systemUIDialog.setPositiveButton(17039370, null);
            systemUIDialog.setShowForAllUsers(true);
            systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$PU_JpsxNcz7jXGNa_DRkuMbEWwU */

                public final void onDismiss(DialogInterface dialogInterface) {
                    PowerNotificationWarnings.this.lambda$showHighTemperatureDialog$0$PowerNotificationWarnings(dialogInterface);
                }
            });
            systemUIDialog.show();
            this.mHighTempDialog = systemUIDialog;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showHighTemperatureDialog$0 */
    public /* synthetic */ void lambda$showHighTemperatureDialog$0$PowerNotificationWarnings(DialogInterface dialogInterface) {
        this.mHighTempDialog = null;
    }

    /* access modifiers changed from: package-private */
    public void dismissThermalShutdownWarning() {
        this.mNoMan.cancelAsUser("high_temp", 39, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showThermalShutdownDialog() {
        if (this.mThermalShutdownDialog == null) {
            SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
            systemUIDialog.setIconAttribute(16843605);
            systemUIDialog.setTitle(C0021R$string.thermal_shutdown_title);
            systemUIDialog.setMessage(C0021R$string.thermal_shutdown_dialog_message);
            systemUIDialog.setPositiveButton(17039370, null);
            systemUIDialog.setShowForAllUsers(true);
            systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$O5nkGS5PG2ihQrXqunpOO_aZDms */

                public final void onDismiss(DialogInterface dialogInterface) {
                    PowerNotificationWarnings.this.lambda$showThermalShutdownDialog$1$PowerNotificationWarnings(dialogInterface);
                }
            });
            systemUIDialog.show();
            this.mThermalShutdownDialog = systemUIDialog;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showThermalShutdownDialog$1 */
    public /* synthetic */ void lambda$showThermalShutdownDialog$1$PowerNotificationWarnings(DialogInterface dialogInterface) {
        this.mThermalShutdownDialog = null;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showThermalShutdownWarning() {
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(C0013R$drawable.ic_device_thermostat_24).setWhen(0).setShowWhen(false).setContentTitle(this.mContext.getString(C0021R$string.thermal_shutdown_title)).setContentText(this.mContext.getString(C0021R$string.thermal_shutdown_message)).setVisibility(1).setContentIntent(pendingBroadcast("PNW.clickedThermalShutdownWarning")).setDeleteIntent(pendingBroadcast("PNW.dismissedThermalShutdownWarning")).setColor(Utils.getColorAttrDefaultColor(this.mContext, 16844099));
        SystemUI.overrideNotificationAppName(this.mContext, color, false);
        this.mNoMan.notifyAsUser("high_temp", 39, color.build(), UserHandle.ALL);
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showUsbHighTemperatureAlarm() {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$BgW0sVGH4tN6GoBK_M1noXhk8wA */

            public final void run() {
                PowerNotificationWarnings.this.lambda$showUsbHighTemperatureAlarm$2$PowerNotificationWarnings();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: showUsbHighTemperatureAlarmInternal */
    public void lambda$showUsbHighTemperatureAlarm$2() {
        if (this.mUsbHighTempDialog == null) {
            SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext, C0022R$style.Theme_SystemUI_Dialog_Alert);
            systemUIDialog.setCancelable(false);
            systemUIDialog.setIconAttribute(16843605);
            systemUIDialog.setTitle(C0021R$string.high_temp_alarm_title);
            systemUIDialog.setShowForAllUsers(true);
            systemUIDialog.setMessage(this.mContext.getString(C0021R$string.high_temp_alarm_notify_message, ""));
            systemUIDialog.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$wL6F1WmvK9p9dyYXQnu9ScZBxSA */

                public final void onClick(DialogInterface dialogInterface, int i) {
                    PowerNotificationWarnings.this.lambda$showUsbHighTemperatureAlarmInternal$3$PowerNotificationWarnings(dialogInterface, i);
                }
            });
            systemUIDialog.setNegativeButton(C0021R$string.high_temp_alarm_help_care_steps, new DialogInterface.OnClickListener() {
                /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$dkzsXROJlAvy2zSj_OYfkxpKFc */

                public final void onClick(DialogInterface dialogInterface, int i) {
                    PowerNotificationWarnings.this.lambda$showUsbHighTemperatureAlarmInternal$5$PowerNotificationWarnings(dialogInterface, i);
                }
            });
            systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$_C7Tc72CcSASuMrkeFnJC4Oj07o */

                public final void onDismiss(DialogInterface dialogInterface) {
                    PowerNotificationWarnings.this.lambda$showUsbHighTemperatureAlarmInternal$6$PowerNotificationWarnings(dialogInterface);
                }
            });
            systemUIDialog.getWindow().addFlags(2097280);
            systemUIDialog.show();
            this.mUsbHighTempDialog = systemUIDialog;
            Events.writeEvent(19, 3, Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showUsbHighTemperatureAlarmInternal$3 */
    public /* synthetic */ void lambda$showUsbHighTemperatureAlarmInternal$3$PowerNotificationWarnings(DialogInterface dialogInterface, int i) {
        this.mUsbHighTempDialog = null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showUsbHighTemperatureAlarmInternal$5 */
    public /* synthetic */ void lambda$showUsbHighTemperatureAlarmInternal$5$PowerNotificationWarnings(DialogInterface dialogInterface, int i) {
        String string = this.mContext.getString(C0021R$string.high_temp_alarm_help_url);
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.HelpTrampoline");
        intent.putExtra("android.intent.extra.TEXT", string);
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(intent, true, (ActivityStarter.Callback) new ActivityStarter.Callback() {
            /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$aDUeHG2fyaQA2OArgzN2VFmIKQ */

            @Override // com.android.systemui.plugins.ActivityStarter.Callback
            public final void onActivityStarted(int i) {
                PowerNotificationWarnings.this.lambda$showUsbHighTemperatureAlarmInternal$4$PowerNotificationWarnings(i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showUsbHighTemperatureAlarmInternal$4 */
    public /* synthetic */ void lambda$showUsbHighTemperatureAlarmInternal$4$PowerNotificationWarnings(int i) {
        this.mUsbHighTempDialog = null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showUsbHighTemperatureAlarmInternal$6 */
    public /* synthetic */ void lambda$showUsbHighTemperatureAlarmInternal$6$PowerNotificationWarnings(DialogInterface dialogInterface) {
        this.mUsbHighTempDialog = null;
        Events.writeEvent(20, 9, Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void updateLowBatteryWarning() {
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissLowBatteryWarning() {
        if (DEBUG) {
            Slog.d("PowerUI.Notification", "dismissing low battery warning: level=" + this.mBatteryLevel);
        }
        dismissLowBatteryNotification();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissLowBatteryNotification() {
        if (this.mWarning) {
            Slog.i("PowerUI.Notification", "dismissing low battery notification");
        }
        this.mWarning = false;
        updateNotification();
    }

    private boolean hasBatterySettings() {
        return this.mOpenBatterySettings.resolveActivity(this.mContext.getPackageManager()) != null;
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showLowBatteryWarning(boolean z) {
        Slog.i("PowerUI.Notification", "show low battery warning: level=" + this.mBatteryLevel + " [" + this.mBucket + "] playSound=" + z);
        this.mPlaySound = z;
        this.mWarning = true;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void dismissInvalidChargerWarning() {
        dismissInvalidChargerNotification();
    }

    private void dismissInvalidChargerNotification() {
        if (this.mInvalidCharger) {
            Slog.i("PowerUI.Notification", "dismissing invalid charger notification");
        }
        this.mInvalidCharger = false;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void showInvalidChargerWarning() {
        this.mInvalidCharger = true;
        updateNotification();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showAutoSaverSuggestion() {
        this.mShowAutoSaverSuggestion = true;
        updateNotification();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissAutoSaverSuggestion() {
        this.mShowAutoSaverSuggestion = false;
        updateNotification();
    }

    @Override // com.android.systemui.power.PowerUI.WarningsUI
    public void userSwitched() {
        updateNotification();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showStartSaverConfirmation(Bundle bundle) {
        if (this.mSaverConfirmation == null) {
            SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
            boolean z = bundle.getBoolean("extra_confirm_only");
            int i = bundle.getInt("extra_power_save_mode_trigger", 0);
            int i2 = bundle.getInt("extra_power_save_mode_trigger_level", 0);
            systemUIDialog.setMessage(getBatterySaverDescription());
            if (isEnglishLocale()) {
                systemUIDialog.setMessageHyphenationFrequency(0);
            }
            systemUIDialog.setMessageMovementMethod(LinkMovementMethod.getInstance());
            if (z) {
                systemUIDialog.setTitle(C0021R$string.battery_saver_confirmation_title_generic);
                systemUIDialog.setPositiveButton(17040005, new DialogInterface.OnClickListener(i, i2) {
                    /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$i9YMNbne4kaewl8DwiUWlEIhHLU */
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onClick(DialogInterface dialogInterface, int i) {
                        PowerNotificationWarnings.this.lambda$showStartSaverConfirmation$7$PowerNotificationWarnings(this.f$1, this.f$2, dialogInterface, i);
                    }
                });
            } else {
                systemUIDialog.setTitle(C0021R$string.battery_saver_confirmation_title);
                systemUIDialog.setPositiveButton(C0021R$string.battery_saver_confirmation_ok, new DialogInterface.OnClickListener() {
                    /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$UffCz3D5JaMRKgj_soLcPpUL04 */

                    public final void onClick(DialogInterface dialogInterface, int i) {
                        PowerNotificationWarnings.this.lambda$showStartSaverConfirmation$8$PowerNotificationWarnings(dialogInterface, i);
                    }
                });
                systemUIDialog.setNegativeButton(17039360, null);
            }
            systemUIDialog.setShowForAllUsers(true);
            systemUIDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.android.systemui.power.$$Lambda$PowerNotificationWarnings$AE5LLn9E8Dx1b7_xgN4SxgDN7R4 */

                public final void onDismiss(DialogInterface dialogInterface) {
                    PowerNotificationWarnings.this.lambda$showStartSaverConfirmation$9$PowerNotificationWarnings(dialogInterface);
                }
            });
            systemUIDialog.show();
            this.mSaverConfirmation = systemUIDialog;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showStartSaverConfirmation$7 */
    public /* synthetic */ void lambda$showStartSaverConfirmation$7$PowerNotificationWarnings(int i, int i2, DialogInterface dialogInterface, int i3) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, "automatic_power_save_mode", i);
        Settings.Global.putInt(contentResolver, "low_power_trigger_level", i2);
        Settings.Secure.putIntForUser(contentResolver, "low_power_warning_acknowledged", 1, -2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showStartSaverConfirmation$8 */
    public /* synthetic */ void lambda$showStartSaverConfirmation$8$PowerNotificationWarnings(DialogInterface dialogInterface, int i) {
        setSaverMode(true, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showStartSaverConfirmation$9 */
    public /* synthetic */ void lambda$showStartSaverConfirmation$9$PowerNotificationWarnings(DialogInterface dialogInterface) {
        this.mSaverConfirmation = null;
    }

    private boolean isEnglishLocale() {
        return Objects.equals(Locale.getDefault().getLanguage(), Locale.ENGLISH.getLanguage());
    }

    private CharSequence getBatterySaverDescription() {
        String charSequence = this.mContext.getText(C0021R$string.help_uri_battery_saver_learn_more_link_target).toString();
        if (TextUtils.isEmpty(charSequence)) {
            return this.mContext.getText(17039770);
        }
        SpannableString spannableString = new SpannableString(this.mContext.getText(17039771));
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spannableString);
        Annotation[] annotationArr = (Annotation[]) spannableString.getSpans(0, spannableString.length(), Annotation.class);
        for (Annotation annotation : annotationArr) {
            if ("url".equals(annotation.getValue())) {
                int spanStart = spannableString.getSpanStart(annotation);
                int spanEnd = spannableString.getSpanEnd(annotation);
                AnonymousClass1 r8 = new URLSpan(charSequence) {
                    /* class com.android.systemui.power.PowerNotificationWarnings.AnonymousClass1 */

                    public void updateDrawState(TextPaint textPaint) {
                        super.updateDrawState(textPaint);
                        textPaint.setUnderlineText(false);
                    }

                    public void onClick(View view) {
                        if (PowerNotificationWarnings.this.mSaverConfirmation != null) {
                            PowerNotificationWarnings.this.mSaverConfirmation.dismiss();
                        }
                        PowerNotificationWarnings.this.mContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS").setFlags(268435456));
                        Uri parse = Uri.parse(getURL());
                        Context context = view.getContext();
                        Intent flags = new Intent("android.intent.action.VIEW", parse).setFlags(268435456);
                        try {
                            context.startActivity(flags);
                        } catch (ActivityNotFoundException unused) {
                            Log.w("PowerUI.Notification", "Activity was not found for intent, " + flags.toString());
                        }
                    }
                };
                spannableStringBuilder.setSpan(r8, spanStart, spanEnd, spannableString.getSpanFlags(r8));
            }
        }
        return spannableStringBuilder;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSaverMode(boolean z, boolean z2) {
        BatterySaverUtils.setPowerSaveMode(this.mContext, z, z2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startBatterySaverSchedulePage() {
        Intent intent = new Intent("com.android.settings.BATTERY_SAVER_SCHEDULE_SETTINGS");
        intent.setFlags(268468224);
        this.mActivityStarter.startActivity(intent, true);
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("PNW.batterySettings");
            intentFilter.addAction("PNW.startSaver");
            intentFilter.addAction("PNW.dismissedWarning");
            intentFilter.addAction("PNW.clickedTempWarning");
            intentFilter.addAction("PNW.dismissedTempWarning");
            intentFilter.addAction("PNW.clickedThermalShutdownWarning");
            intentFilter.addAction("PNW.dismissedThermalShutdownWarning");
            intentFilter.addAction("PNW.startSaverConfirmation");
            intentFilter.addAction("PNW.autoSaverSuggestion");
            intentFilter.addAction("PNW.enableAutoSaver");
            intentFilter.addAction("PNW.autoSaverNoThanks");
            intentFilter.addAction("PNW.dismissAutoSaverSuggestion");
            PowerNotificationWarnings.this.mContext.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, "android.permission.DEVICE_POWER", PowerNotificationWarnings.this.mHandler);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.i("PowerUI.Notification", "Received " + action);
            if (action.equals("PNW.batterySettings")) {
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
                PowerNotificationWarnings.this.mContext.startActivityAsUser(PowerNotificationWarnings.this.mOpenBatterySettings, UserHandle.CURRENT);
            } else if (action.equals("PNW.startSaver")) {
                PowerNotificationWarnings.this.setSaverMode(true, true);
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
            } else if (action.equals("PNW.startSaverConfirmation")) {
                PowerNotificationWarnings.this.dismissLowBatteryNotification();
                PowerNotificationWarnings.this.showStartSaverConfirmation(intent.getExtras());
            } else if (action.equals("PNW.dismissedWarning")) {
                PowerNotificationWarnings.this.dismissLowBatteryWarning();
            } else if ("PNW.clickedTempWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissHighTemperatureWarningInternal();
                PowerNotificationWarnings.this.showHighTemperatureDialog();
            } else if ("PNW.dismissedTempWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissHighTemperatureWarningInternal();
            } else if ("PNW.clickedThermalShutdownWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissThermalShutdownWarning();
                PowerNotificationWarnings.this.showThermalShutdownDialog();
            } else if ("PNW.dismissedThermalShutdownWarning".equals(action)) {
                PowerNotificationWarnings.this.dismissThermalShutdownWarning();
            } else if ("PNW.autoSaverSuggestion".equals(action)) {
                PowerNotificationWarnings.this.showAutoSaverSuggestion();
            } else if ("PNW.dismissAutoSaverSuggestion".equals(action)) {
                PowerNotificationWarnings.this.dismissAutoSaverSuggestion();
            } else if ("PNW.enableAutoSaver".equals(action)) {
                PowerNotificationWarnings.this.dismissAutoSaverSuggestion();
                PowerNotificationWarnings.this.startBatterySaverSchedulePage();
            } else if ("PNW.autoSaverNoThanks".equals(action)) {
                PowerNotificationWarnings.this.dismissAutoSaverSuggestion();
                BatterySaverUtils.suppressAutoBatterySaver(context);
            }
        }
    }
}
