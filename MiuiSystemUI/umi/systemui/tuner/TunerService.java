package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.SystemUIDialog;

public abstract class TunerService {

    public interface Tunable {
        void onTuningChanged(String str, String str2);
    }

    public abstract void addTunable(Tunable tunable, String... strArr);

    public abstract void clearAll();

    public abstract int getValue(String str, int i);

    public abstract String getValue(String str);

    public abstract void removeTunable(Tunable tunable);

    public abstract void setValue(String str, int i);

    public abstract void setValue(String str, String str2);

    private static Context userContext(Context context) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(ActivityManager.getCurrentUser()));
        } catch (PackageManager.NameNotFoundException unused) {
            return context;
        }
    }

    public static final void setTunerEnabled(Context context, boolean z) {
        userContext(context).getPackageManager().setComponentEnabledSetting(new ComponentName(context, TunerActivity.class), z ? 1 : 2, 1);
    }

    public static final boolean isTunerEnabled(Context context) {
        return userContext(context).getPackageManager().getComponentEnabledSetting(new ComponentName(context, TunerActivity.class)) == 1;
    }

    public static class ClearReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.action.CLEAR_TUNER".equals(intent.getAction())) {
                ((TunerService) Dependency.get(TunerService.class)).clearAll();
            }
        }
    }

    public static final void showResetRequest(final Context context, final Runnable runnable) {
        SystemUIDialog systemUIDialog = new SystemUIDialog(context);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setMessage(C0021R$string.remove_from_settings_prompt);
        systemUIDialog.setButton(-2, context.getString(C0021R$string.cancel), (DialogInterface.OnClickListener) null);
        systemUIDialog.setButton(-1, context.getString(C0021R$string.guest_exit_guest_dialog_remove), new DialogInterface.OnClickListener() {
            /* class com.android.systemui.tuner.TunerService.AnonymousClass1 */

            public void onClick(DialogInterface dialogInterface, int i) {
                context.sendBroadcast(new Intent("com.android.systemui.action.CLEAR_TUNER"));
                TunerService.setTunerEnabled(context, false);
                Settings.Secure.putInt(context.getContentResolver(), "seen_tuner_warning", 0);
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        systemUIDialog.show();
    }

    public static boolean parseIntegerSwitch(String str, boolean z) {
        if (str == null) {
            return z;
        }
        try {
            return Integer.parseInt(str) != 0;
        } catch (NumberFormatException unused) {
            return z;
        }
    }
}
