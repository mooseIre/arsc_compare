package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentCompat;

public class KeyboardShortcutsReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (IntentCompat.ACTION_SHOW_KEYBOARD_SHORTCUTS.equals(intent.getAction())) {
            KeyboardShortcuts.show(context, -1);
        } else if (IntentCompat.ACTION_DISMISS_KEYBOARD_SHORTCUTS.equals(intent.getAction())) {
            KeyboardShortcuts.dismiss();
        }
    }
}
