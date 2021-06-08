package com.android.systemui.statusbar.phone;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NavigationModeControllerExt.kt */
public final class NavigationModeControllerExt$mHandler$1 extends Handler {
    NavigationModeControllerExt$mHandler$1(Looper looper) {
        super(looper);
    }

    public void handleMessage(@NotNull Message message) {
        NavigationBarView defaultNavigationBarView;
        View findViewById;
        Intrinsics.checkParameterIsNotNull(message, "msg");
        int i = message.what;
        if (i == 0) {
            NavigationModeControllerExt.INSTANCE.getNavigationBarController().addDefaultNavigationBar();
            if (!NavigationModeControllerExt.INSTANCE.getMIsFsgMode() && (defaultNavigationBarView = NavigationModeControllerExt.INSTANCE.getNavigationBarController().getDefaultNavigationBarView()) != null && (findViewById = defaultNavigationBarView.findViewById(C0015R$id.home_handle)) != null) {
                findViewById.setVisibility(8);
            }
        } else if (i == 1) {
            NavigationModeControllerExt.INSTANCE.getNavigationBarController().removeDefaultNavigationBar();
        }
    }
}
