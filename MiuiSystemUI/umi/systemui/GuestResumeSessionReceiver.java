package com.android.systemui;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.SystemUIDialog;

public class GuestResumeSessionReceiver extends BroadcastReceiver {
    private Dialog mNewSessionDialog;

    public void register(BroadcastDispatcher broadcastDispatcher) {
        broadcastDispatcher.registerReceiver(this, new IntentFilter("android.intent.action.USER_SWITCHED"), null, UserHandle.SYSTEM);
    }

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
            cancelDialog();
            int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if (intExtra == -10000) {
                Log.e("GuestResumeSessionReceiver", intent + " sent to " + "GuestResumeSessionReceiver" + " without EXTRA_USER_HANDLE");
                return;
            }
            try {
                if (ActivityManager.getService().getCurrentUser().isGuest()) {
                    ContentResolver contentResolver = context.getContentResolver();
                    if (Settings.System.getIntForUser(contentResolver, "systemui.guest_has_logged_in", 0, intExtra) != 0) {
                        ResetSessionDialog resetSessionDialog = new ResetSessionDialog(context, intExtra);
                        this.mNewSessionDialog = resetSessionDialog;
                        resetSessionDialog.show();
                        return;
                    }
                    Settings.System.putIntForUser(contentResolver, "systemui.guest_has_logged_in", 1, intExtra);
                }
            } catch (RemoteException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static void wipeGuestSession(Context context, int i) {
        UserManager userManager = (UserManager) context.getSystemService("user");
        try {
            UserInfo currentUser = ActivityManager.getService().getCurrentUser();
            if (currentUser.id != i) {
                Log.w("GuestResumeSessionReceiver", "User requesting to start a new session (" + i + ") is not current user (" + currentUser.id + ")");
            } else if (!currentUser.isGuest()) {
                Log.w("GuestResumeSessionReceiver", "User requesting to start a new session (" + i + ") is not a guest");
            } else if (!userManager.markGuestForDeletion(currentUser.id)) {
                Log.w("GuestResumeSessionReceiver", "Couldn't mark the guest for deletion for user " + i);
            } else {
                UserInfo createGuest = userManager.createGuest(context, currentUser.name);
                if (createGuest == null) {
                    try {
                        Log.e("GuestResumeSessionReceiver", "Could not create new guest, switching back to system user");
                        ActivityManager.getService().switchUser(0);
                        userManager.removeUser(currentUser.id);
                        WindowManagerGlobal.getWindowManagerService().lockNow((Bundle) null);
                    } catch (RemoteException unused) {
                        Log.e("GuestResumeSessionReceiver", "Couldn't wipe session because ActivityManager or WindowManager is dead");
                    }
                } else {
                    ActivityManager.getService().switchUser(createGuest.id);
                    userManager.removeUser(currentUser.id);
                }
            }
        } catch (RemoteException unused2) {
            Log.e("GuestResumeSessionReceiver", "Couldn't wipe session because ActivityManager is dead");
        }
    }

    private void cancelDialog() {
        Dialog dialog = this.mNewSessionDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mNewSessionDialog.cancel();
            this.mNewSessionDialog = null;
        }
    }

    private static class ResetSessionDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        private final int mUserId;

        public ResetSessionDialog(Context context, int i) {
            super(context);
            setTitle(context.getString(C0021R$string.guest_wipe_session_title));
            setMessage(context.getString(C0021R$string.guest_wipe_session_message));
            setCanceledOnTouchOutside(false);
            setButton(-2, context.getString(C0021R$string.guest_wipe_session_wipe), this);
            setButton(-1, context.getString(C0021R$string.guest_wipe_session_dontwipe), this);
            this.mUserId = i;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                GuestResumeSessionReceiver.wipeGuestSession(getContext(), this.mUserId);
                dismiss();
            } else if (i == -1) {
                cancel();
            }
        }
    }
}
