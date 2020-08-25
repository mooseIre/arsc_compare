package com.android.systemui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.MiuiConfiguration;
import android.graphics.Outline;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import com.android.systemui.content.pm.PackageManagerCompat;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.miui.ToastOverlayManager;
import java.util.List;

public class Util {
    private static boolean sMiuiOptimizationDisabled;
    private static final ArrayMap<String, Boolean> sSystemApps = new ArrayMap<>();

    public static void setUserExperienceProgramEnabled(boolean z) {
    }

    public static PackageManager getPackageManagerForUser(Context context, int i) {
        if (i >= 0) {
            try {
                context = context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(i));
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        return context.getPackageManager();
    }

    public static String getTopActivityPkg(Context context, boolean z) {
        if (z && ((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked()) {
            return "lockscreen";
        }
        ComponentName topActivity = getTopActivity(context);
        if (topActivity == null) {
            return "";
        }
        return topActivity.getPackageName();
    }

    public static String getTopActivityPkg(Context context) {
        return getTopActivityPkg(context, false);
    }

    public static ComponentName getTopActivity(Context context) {
        if (isMainProcess()) {
            return ((ActivityObserver) Dependency.get(ActivityObserver.class)).getTopActivity();
        }
        return getTopActivityLegacy(context);
    }

    public static ComponentName getLastResumedActivity(Context context) {
        if (isMainProcess()) {
            return ((ActivityObserver) Dependency.get(ActivityObserver.class)).getLastResumedActivity();
        }
        return getTopActivityLegacy(context);
    }

    private static ComponentName getTopActivityLegacy(Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            return null;
        }
        return runningTasks.get(0).topActivity;
    }

    public static void setMiuiOptimizationDisabled(boolean z) {
        sMiuiOptimizationDisabled = z;
    }

    public static boolean isMiuiOptimizationDisabled() {
        return sMiuiOptimizationDisabled;
    }

    public static boolean showCtsSpecifiedColor() {
        return sMiuiOptimizationDisabled && Build.VERSION.SDK_INT > 25;
    }

    public static boolean isGlobalFileExplorerExist(Context context) {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setPackage("com.mi.android.globalFileexplorer");
        return isIntentActivityExist(context, intent);
    }

    public static boolean isCNFileExplorerExist(Context context) {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setPackage("com.android.fileexplorer");
        return isIntentActivityExist(context, intent);
    }

    public static boolean isBrowserSearchExist(Context context) {
        Intent intent = new Intent("com.android.browser.browser_search");
        intent.setPackage(isBrowserGlobalEnabled(context) ? "com.mi.globalbrowser" : "com.android.browser");
        return isIntentActivityExist(context, intent);
    }

    public static boolean isBrowserGlobalEnabled(Context context) {
        if (!Constants.IS_INTERNATIONAL || !isAppInstalledForUser(context, "com.mi.globalbrowser", 0)) {
            return false;
        }
        return true;
    }

    public static boolean isAppInstalledForUser(Context context, String str, int i) {
        try {
            PackageManagerCompat.getPackageInfoAsUser(context.getPackageManager(), str, 1, i);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e("Util", "name not found pkg=" + str);
            return false;
        }
    }

    public static boolean isIntentActivityExist(Context context, Intent intent) {
        if (!(context == null || intent == null)) {
            try {
                List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 786432);
                if (queryIntentActivities == null || queryIntentActivities.size() <= 0) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void hideSystemBars(View view) {
        view.setSystemUiVisibility(12038);
    }

    public static void wholeHideSystemBars(View view) {
        view.setSystemUiVisibility(16134);
    }

    public static void setViewRoundCorner(View view, final float f) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), f);
            }
        });
        view.setClipToOutline(true);
    }

    public static void playRingtoneAsync(final Context context, final Uri uri, final int i) {
        ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).submit(new Runnable() {
            public void run() {
                try {
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    if (ringtone != null) {
                        if (i >= 0) {
                            ringtone.setStreamType(i);
                        }
                        ringtone.play();
                    }
                } catch (Exception e) {
                    Log.e("Util", "error playing ringtone " + uri, e);
                }
            }
        });
    }

    @SuppressLint({"ShowToast"})
    private static Toast makeSystemOverlayToast(Context context, String str, int i) {
        Toast makeText = Toast.makeText(context, str, i);
        makeText.setType(2006);
        if (makeText.getWindowParams() != null) {
            makeText.getWindowParams().privateFlags |= 16;
        }
        return makeText;
    }

    public static Toast showSystemOverlayToast(Context context, int i, int i2) {
        return showSystemOverlayToast(context, context.getString(i), i2);
    }

    public static Toast showSystemOverlayToast(Context context, String str, int i) {
        Toast makeSystemOverlayToast = makeSystemOverlayToast(context, str, i);
        makeSystemOverlayToast.show();
        ((ToastOverlayManager) Dependency.get(ToastOverlayManager.class)).dispatchShowToast(makeSystemOverlayToast);
        return makeSystemOverlayToast;
    }

    public static boolean isThemeResourcesChanged(int i, long j) {
        return ((Integer.MIN_VALUE & i) != 0 && MiuiConfiguration.needRestartStatusBar(j)) || ((i & 512) != 0);
    }

    public static boolean isMainProcess() {
        String currentProcessName = ActivityThread.currentProcessName();
        return !TextUtils.isEmpty(currentProcessName) && TextUtils.indexOf(currentProcessName, ':') < 0;
    }

    public static void runAfterGlobalLayout(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                runnable.run();
            }
        });
    }

    public static void runAfterGlobalLayoutOrNot(View view, Runnable runnable, boolean z) {
        if (z) {
            runAfterGlobalLayout(view, runnable);
        } else {
            runnable.run();
        }
    }

    public static boolean isSystemApp(Context context, String str) {
        Boolean bool;
        synchronized (sSystemApps) {
            bool = sSystemApps.get(str);
        }
        if (bool == null) {
            try {
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(str, 0);
                if (applicationInfo != null) {
                    Boolean valueOf = Boolean.valueOf(applicationInfo.isSystemApp());
                    try {
                        synchronized (sSystemApps) {
                            sSystemApps.put(str, valueOf);
                        }
                    } catch (PackageManager.NameNotFoundException unused) {
                    }
                    bool = valueOf;
                }
            } catch (PackageManager.NameNotFoundException unused2) {
            }
        }
        if (bool == null) {
            return false;
        }
        return bool.booleanValue();
    }

    public static Intent getSilentModeIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(ComponentName.unflattenFromString(Constants.SILENT_MODE_ACTION));
        intent.setFlags(335544320);
        return intent;
    }
}
