package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class KeyboardShortcutsReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("com.android.intent.action.SHOW_KEYBOARD_SHORTCUTS".equals(intent.getAction())) {
            KeyboardShortcuts.show(context, -1);
        } else if ("com.android.intent.action.DISMISS_KEYBOARD_SHORTCUTS".equals(intent.getAction())) {
            KeyboardShortcuts.dismiss();
        }
    }
}
