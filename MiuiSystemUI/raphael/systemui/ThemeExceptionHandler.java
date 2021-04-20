package com.android.systemui;

import android.content.Context;
import android.miui.Shell;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.statusbar.IStatusBarService;
import com.miui.systemui.util.MiuiThemeUtils;
import java.io.File;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExceptionHandler.kt */
public final class ThemeExceptionHandler {
    public static final ThemeExceptionHandler INSTANCE = new ThemeExceptionHandler();

    private ThemeExceptionHandler() {
    }

    public final void tryFixCrash(@NotNull Context context, @Nullable String str) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (CodeBlueConfig.Companion.isFirstTimeFixCrash(context)) {
            MiuiThemeUtils.forceUseDefaultTheme();
            return;
        }
        boolean delete = delete("/data/system/theme/com.android.systemui");
        boolean delete2 = delete("/data/system/theme/lockscreen") | delete("/data/system/theme/splockscreen");
        if (delete || delete2) {
            reboot();
        }
    }

    private final boolean delete(String str) {
        File file = new File(str);
        if (!file.exists()) {
            return false;
        }
        Shell.chown(file.getAbsolutePath(), 1000, 1000);
        return file.delete();
    }

    private final void reboot() {
        try {
            IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar")).reboot(false);
        } catch (RemoteException unused) {
        }
    }
}
