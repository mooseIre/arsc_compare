package com.android.systemui.statusbar.phone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardStateController;

public class SystemUIDialog extends AlertDialog {
    private final Context mContext;
    private final DismissReceiver mDismissReceiver;

    public SystemUIDialog(Context context) {
        this(context, C0022R$style.Theme_SystemUI_Dialog);
    }

    public SystemUIDialog(Context context, int i) {
        super(context, i);
        this.mContext = context;
        applyFlags(this);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.setTitle(getClass().getSimpleName());
        getWindow().setAttributes(attributes);
        this.mDismissReceiver = new DismissReceiver(this);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        this.mDismissReceiver.register();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        this.mDismissReceiver.unregister();
    }

    public void setShowForAllUsers(boolean z) {
        setShowForAllUsers(this, z);
    }

    public void setMessage(int i) {
        setMessage(this.mContext.getString(i));
    }

    public void setPositiveButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-1, this.mContext.getString(i), onClickListener);
    }

    public void setNegativeButton(int i, DialogInterface.OnClickListener onClickListener) {
        setButton(-2, this.mContext.getString(i), onClickListener);
    }

    public static void setShowForAllUsers(Dialog dialog, boolean z) {
        if (z) {
            dialog.getWindow().getAttributes().privateFlags |= 16;
            return;
        }
        dialog.getWindow().getAttributes().privateFlags &= -17;
    }

    public static void setWindowOnTop(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setType(2017);
        if (((KeyguardStateController) Dependency.get(KeyguardStateController.class)).isShowing()) {
            window.getAttributes().setFitInsetsTypes(window.getAttributes().getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
        }
    }

    public static AlertDialog applyFlags(AlertDialog alertDialog) {
        Window window = alertDialog.getWindow();
        window.setType(2017);
        window.addFlags(655360);
        window.getAttributes().setFitInsetsTypes(window.getAttributes().getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
        return alertDialog;
    }

    public static void registerDismissListener(Dialog dialog) {
        DismissReceiver dismissReceiver = new DismissReceiver(dialog);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$SystemUIDialog$aJwQFxZ3HhCkUHQluqH61p2yCMc */

            public final void onDismiss(DialogInterface dialogInterface) {
                SystemUIDialog.DismissReceiver.this.unregister();
            }
        });
        dismissReceiver.register();
    }

    /* access modifiers changed from: private */
    public static class DismissReceiver extends BroadcastReceiver {
        private static final IntentFilter INTENT_FILTER;
        private final BroadcastDispatcher mBroadcastDispatcher = ((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));
        private final Dialog mDialog;
        private boolean mRegistered;

        static {
            IntentFilter intentFilter = new IntentFilter();
            INTENT_FILTER = intentFilter;
            intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            INTENT_FILTER.addAction("android.intent.action.SCREEN_OFF");
        }

        DismissReceiver(Dialog dialog) {
            this.mDialog = dialog;
        }

        /* access modifiers changed from: package-private */
        public void register() {
            this.mBroadcastDispatcher.registerReceiver(this, INTENT_FILTER, null, UserHandle.CURRENT);
            this.mRegistered = true;
        }

        /* access modifiers changed from: package-private */
        public void unregister() {
            if (this.mRegistered) {
                this.mBroadcastDispatcher.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            this.mDialog.dismiss();
        }
    }
}
