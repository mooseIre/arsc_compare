package com.android.systemui.globalactions;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListAdapter;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0017R$layout;

public class GlobalActionsPowerDialog {
    public static Dialog create(Context context, ListAdapter listAdapter) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(C0017R$layout.global_actions_power_dialog, (ViewGroup) null);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            viewGroup.addView(listAdapter.getView(i, null, viewGroup));
        }
        Resources resources = context.getResources();
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(1);
        dialog.setContentView(viewGroup);
        Window window = dialog.getWindow();
        window.setType(2020);
        window.setTitle("");
        window.setBackgroundDrawable(resources.getDrawable(C0013R$drawable.control_background, context.getTheme()));
        window.addFlags(131072);
        return dialog;
    }
}
