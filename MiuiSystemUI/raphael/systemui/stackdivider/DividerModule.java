package com.android.systemui.stackdivider;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.TransactionPool;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.SystemWindows;
import dagger.Lazy;
import java.util.Optional;

public class DividerModule {
    static Divider provideDivider(Context context, Optional<Lazy<Recents>> optional, DisplayController displayController, SystemWindows systemWindows, DisplayImeController displayImeController, Handler handler, KeyguardStateController keyguardStateController, TransactionPool transactionPool) {
        return new Divider(context, optional, displayController, systemWindows, displayImeController, handler, keyguardStateController, transactionPool);
    }
}
