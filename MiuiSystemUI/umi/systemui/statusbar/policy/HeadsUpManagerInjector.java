package com.android.systemui.statusbar.policy;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.views.ClickableToast;
import miui.app.AlertDialog;

public class HeadsUpManagerInjector {
    private static boolean sSnoozeNotify = false;
    private static long sSnoozeUntil = 0;
    private static boolean sUserSelected = false;

    public static void sendExitFloatingIntent(NotificationEntry notificationEntry) {
        PendingIntent exitFloatingIntent = MiuiNotificationCompat.getExitFloatingIntent(notificationEntry.getSbn().getNotification());
        if (exitFloatingIntent != null) {
            try {
                exitFloatingIntent.send();
            } catch (Exception e) {
                Log.d("HeadsUpManagerInjector", "sendExitFloatingIntent " + notificationEntry.getKey(), e);
            }
        }
    }

    public static boolean isSnoozed(Context context, StatusBarNotification statusBarNotification, boolean z) {
        if (skipSnooze((ExpandedNotification) statusBarNotification)) {
            return false;
        }
        int snoozeStrategy = getSnoozeStrategy(context);
        if (snoozeStrategy == 0) {
            if (SystemClock.elapsedRealtime() <= sSnoozeUntil) {
                return true;
            }
            return false;
        } else if (snoozeStrategy == 1) {
            return z;
        } else {
            return false;
        }
    }

    public static boolean injectSnooze(Context context, NotificationEntry notificationEntry) {
        int snoozeStrategy = getSnoozeStrategy(context);
        if (NotificationUtil.isInCallNotification(notificationEntry.getSbn()) || NotificationUtil.containsVerifyCode(notificationEntry.getSbn())) {
            return true;
        }
        if (sSnoozeNotify) {
            return false;
        }
        showToast(context, snoozeStrategy);
        sSnoozeNotify = true;
        return false;
    }

    public static void setSnoozeUntil(long j) {
        sSnoozeUntil = j;
    }

    public static boolean skipSnooze(ExpandedNotification expandedNotification) {
        if (!NotificationUtil.isInCallNotification(expandedNotification) && !NotificationUtil.containsVerifyCode(expandedNotification)) {
            return false;
        }
        return true;
    }

    private static void showToast(Context context, int i) {
        if (i != 2) {
            StringBuilder sb = new StringBuilder(initSnoozeHint(context, i));
            int length = sb.length();
            sb.append(context.getString(C0021R$string.miui_snooze_to_settings));
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(sb);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#0D84FF")), length, sb.length(), 33);
            ClickableToast.showToast(context).setText(spannableStringBuilder).setClickListener(new View.OnClickListener(context) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$HeadsUpManagerInjector$wHmib0z3DD8B9CgfU74krQYYF9g */
                public final /* synthetic */ Context f$0;

                {
                    this.f$0 = r1;
                }

                public final void onClick(View view) {
                    HeadsUpManagerInjector.lambda$showToast$0(this.f$0, view);
                }
            }).show();
            ((NotificationStat) Dependency.get(NotificationStat.class)).onSnoozeToastVisible();
        }
    }

    static /* synthetic */ void lambda$showToast$0(Context context, View view) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onSnoozeToastClick();
        showDialog(context);
    }

    private static void showDialog(Context context) {
        int i = Settings.Global.getInt(context.getContentResolver(), "miui_float_notification_snooze_strategy", 0);
        AlertDialog create = new AlertDialog.Builder(context, 8).setTitle(context.getString(C0021R$string.float_notification_snooze_strategy)).setSingleChoiceItems(initSnoozeSummary(context), i, new DialogInterface.OnClickListener(context) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$HeadsUpManagerInjector$v1XSqS80ySw2ok_fG0cknhSyWzg */
            public final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                HeadsUpManagerInjector.lambda$showDialog$1(this.f$0, dialogInterface, i);
            }
        }).setNegativeButton(C0021R$string.miui_snooze_cancel, (DialogInterface.OnClickListener) null).setOnDismissListener(new DialogInterface.OnDismissListener(context) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$HeadsUpManagerInjector$KLmL1shOzZ9PNNSlPBgquT3Idg */
            public final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final void onDismiss(DialogInterface dialogInterface) {
                HeadsUpManagerInjector.lambda$showDialog$2(this.f$0, dialogInterface);
            }
        }).create();
        create.getWindow().setType(2003);
        create.show();
    }

    static /* synthetic */ void lambda$showDialog$1(Context context, DialogInterface dialogInterface, int i) {
        updateSnoozeStrategy(i, context);
        dialogInterface.dismiss();
        sUserSelected = true;
        Toast.makeText(context, context.getString(C0021R$string.miui_snooze_user_set_success), 1).show();
        ((NotificationStat) Dependency.get(NotificationStat.class)).onSnoozeDialogClick(i);
    }

    static /* synthetic */ void lambda$showDialog$2(Context context, DialogInterface dialogInterface) {
        if (!sUserSelected) {
            Toast.makeText(context, context.getString(C0021R$string.miui_snooze_user_set_fail), 1).show();
            ((NotificationStat) Dependency.get(NotificationStat.class)).onSnoozeDialogClick(-1);
        }
    }

    private static void updateSnoozeStrategy(int i, Context context) {
        Settings.Global.putInt(context.getContentResolver(), "miui_float_notification_snooze_strategy", i);
    }

    public static int getMiuiFloatTime(NotificationEntry notificationEntry) {
        int floatTime;
        if (notificationEntry == null || (floatTime = notificationEntry.getSbn().getFloatTime()) <= 0) {
            return 0;
        }
        return floatTime;
    }

    public static boolean getSnoozeNotify() {
        return sSnoozeNotify;
    }

    public static int getSnoozeStrategy(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "miui_float_notification_snooze_strategy", 0);
    }

    private static String[] initSnoozeSummary(Context context) {
        return new String[]{context.getResources().getString(C0021R$string.miui_snooze_default_time, 1), context.getResources().getString(C0021R$string.miui_snooze_until_unlock_screen), context.getResources().getString(C0021R$string.miui_snooze_always_float)};
    }

    private static String initSnoozeHint(Context context, int i) {
        if (i != 0) {
            return context.getResources().getString(C0021R$string.miui_snooze_toast_hint_until_unlock_screen);
        }
        return context.getResources().getString(C0021R$string.miui_snooze_toast_hint_default, 1);
    }
}
