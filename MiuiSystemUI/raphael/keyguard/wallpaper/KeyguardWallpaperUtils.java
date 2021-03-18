package com.android.keyguard.wallpaper;

import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.Dependency;
import com.miui.systemui.graphics.DrawableUtils;
import java.io.File;
import java.io.IOException;
import miui.content.res.ThemeResources;
import miui.graphics.BitmapFactory;
import miui.util.CustomizeUtil;

public class KeyguardWallpaperUtils {
    private static Pair<File, Drawable> sLockWallpaperCache;
    private static boolean sLockWallpaperChangedForSleep;
    private static long sLockWallpaperModifiedTime;
    private static Object sWallpaperLock = new Object();

    public static boolean isWallpaperShouldBlur() {
        return !MiuiGxzwManager.isGxzwSensor() || !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
    }

    public static WallpaperInfo getWallpaperInfo() {
        try {
            return IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).getWallpaperInfo(KeyguardUpdateMonitor.getCurrentUser());
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void getRealSize(Display display, Point point) {
        CustomizeUtil.getRealSize(display, point);
    }

    public static Drawable getLockWallpaperPreview(Context context) {
        WallpaperInfo wallpaperInfo;
        Drawable loadDrawable;
        Drawable loadDrawable2;
        if (!WallpaperAuthorityUtils.isThemeLockLiveWallpaper() || (wallpaperInfo = getWallpaperInfo()) == null) {
            Pair<File, Drawable> lockWallpaper = getLockWallpaper(context);
            if (lockWallpaper == null) {
                return null;
            }
            return (Drawable) lockWallpaper.second;
        } else if ("com.miui.miwallpaper.MiWallpaper".equals(wallpaperInfo.getServiceName()) && isMiwallpaperPreviewExist() && (loadDrawable2 = loadDrawable(context, "/data/system/theme/miwallpaper_preview")) != null) {
            return loadDrawable2;
        } else {
            if ("com.android.thememanager.service.VideoWallpaperService".equals(wallpaperInfo.getServiceName()) && isVideoWallpaperPreviewExist() && (loadDrawable = loadDrawable(context, "/data/system/theme_magic/vid eo/video_wallpaper_thumbnail.jpg")) != null) {
                return loadDrawable;
            }
            Drawable loadThumbnail = wallpaperInfo.loadThumbnail(context.getPackageManager());
            if (loadThumbnail != null) {
                return new BitmapDrawable((Resources) null, DrawableUtils.drawable2Bitmap(loadThumbnail));
            }
            return null;
        }
    }

    private static boolean isVideoWallpaperPreviewExist() {
        return new File("/data/system/theme_magic/vid eo/video_wallpaper_thumbnail.jpg").exists();
    }

    private static boolean isMiwallpaperPreviewExist() {
        return new File("/data/system/theme/miwallpaper_preview").exists();
    }

    public static final Pair<File, Drawable> getLockWallpaper(Context context) {
        File file;
        File file2;
        if (WallpaperAuthorityUtils.isHomeDefaultWallpaper()) {
            file2 = new File("/system/media/lockscreen/video/video_wallpaper.mp4");
            file = new File("/system/media/lockscreen/video/video_wallpaper_thumbnail.jpg");
        } else if (WallpaperAuthorityUtils.isThemeLockVideoWallpaper()) {
            file2 = new File("/data/system/theme_magic/video/video_wallpaper.mp4");
            file = new File("/data/system/theme_magic/vid eo/video_wallpaper_thumbnail.jpg");
        } else {
            file2 = null;
            file = null;
        }
        if (file2 == null || !file2.exists() || file == null || !file.exists()) {
            file2 = ThemeResources.getSystem().getLockscreenWallpaper();
            file = file2;
        }
        if (file2 != null && file2.exists() && file != null && file.exists()) {
            return getLockWallpaperCache(context, file2, file);
        }
        String str = "null";
        String absolutePath = file2 != null ? file2.getAbsolutePath() : str;
        if (file != null) {
            str = file.getAbsolutePath();
        }
        Log.d("KeyguardWallpaperUtils", "getLockWallpaper return null; filePath = " + absolutePath + " previewPath = " + str);
        return null;
    }

    private static Drawable loadDrawable(Context context, String str) {
        try {
            Bitmap decodeBitmap = BitmapFactory.decodeBitmap(str, false);
            if (decodeBitmap != null) {
                return new BitmapDrawable(context.getResources(), decodeBitmap);
            }
        } catch (IOException unused) {
        }
        return null;
    }

    private static final Pair<File, Drawable> getLockWallpaperCache(Context context, File file, File file2) {
        synchronized (sWallpaperLock) {
            if (sLockWallpaperModifiedTime != file.lastModified() || sLockWallpaperChangedForSleep || sLockWallpaperCache == null || sLockWallpaperCache.first == null || !((File) sLockWallpaperCache.first).exists() || !file.equals(sLockWallpaperCache.first)) {
                sLockWallpaperCache = null;
                try {
                    Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
                    Point point = new Point();
                    getRealSize(defaultDisplay, point);
                    int i = point.x;
                    int i2 = point.y;
                    if (i > i2) {
                        Log.e("LockWallpaper", "Wrong display metrics for width = " + i + " and height = " + i2);
                        i2 = i;
                        i = i2;
                    }
                    Bitmap decodeBitmap = BitmapFactory.decodeBitmap(file2.getAbsolutePath(), i, i2, false);
                    if (decodeBitmap != null) {
                        sLockWallpaperCache = new Pair<>(file, new BitmapDrawable(context.getResources(), decodeBitmap));
                        sLockWallpaperModifiedTime = file.lastModified();
                        sLockWallpaperChangedForSleep = false;
                    }
                } catch (Exception e) {
                    Slog.e("KeyguardWallpaperUtils", "getLockWallpaperCache", e);
                } catch (OutOfMemoryError e2) {
                    Slog.e("KeyguardWallpaperUtils", "getLockWallpaperCache", e2);
                }
                if (sLockWallpaperCache == null) {
                    Slog.i("KeyguardWallpaperUtils", "getLockWallpaperCache empty");
                }
                return sLockWallpaperCache;
            }
            return sLockWallpaperCache;
        }
    }
}
