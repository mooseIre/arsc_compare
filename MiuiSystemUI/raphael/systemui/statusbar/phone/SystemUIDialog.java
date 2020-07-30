package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;
import miui.app.AlertDialog;

public class SystemUIDialog extends AlertDialog {
    private final Context mContext;

    public SystemUIDialog(Context context) {
        this(context, 8);
    }

    public SystemUIDialog(Context context, int i) {
        super(context, i);
        this.mContext = context;
        applyFlags(this);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.setTitle(getClass().getSimpleName());
        getWindow().setAttributes(attributes);
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

    public static void setShowForAllUsers(AlertDialog alertDialog, boolean z) {
        if (z) {
            alertDialog.getWindow().getAttributes().privateFlags |= 16;
            return;
        }
        alertDialog.getWindow().getAttributes().privateFlags &= -17;
    }

    public static void applyFlags(AlertDialog alertDialog) {
        alertDialog.getWindow().setType(2014);
        alertDialog.getWindow().addFlags(655360);
    }
}
