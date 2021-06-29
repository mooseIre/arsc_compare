package com.android.systemui.statusbar.notification.unimportant;

import android.os.Handler;
import android.os.Message;
import codeinjection.CodeInjection;

/* compiled from: FoldManager.kt */
final class FoldManager$Companion$handler$1 implements Handler.Callback {
    public static final FoldManager$Companion$handler$1 INSTANCE = new FoldManager$Companion$handler$1();

    FoldManager$Companion$handler$1() {
    }

    public final boolean handleMessage(Message message) {
        int i = message.what;
        String str = (String) message.obj;
        if (str == null) {
            str = CodeInjection.MD5;
        }
        FoldManager.Companion.notifyListenersCore1(i, str);
        return false;
    }
}
