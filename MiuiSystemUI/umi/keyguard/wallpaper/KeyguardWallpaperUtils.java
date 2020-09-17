package com.android.keyguard.wallpaper;

import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.common.Utilities;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.utils.HomeUtils;
import com.android.keyguard.utils.MiuiSettingsUtils;
import com.android.keyguard.utils.PreferenceUtils;
import com.android.keyguard.utils.ThemeUtils;
import com.android.systemui.miui.DrawableUtils;
import com.android.systemui.plugins.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import miui.content.res.ThemeResources;
import miui.graphics.BitmapFactory;
import miui.theme.ThemeFileUtils;
import miui.util.CustomizeUtil;
import miui.util.IOUtils;
import miui.util.InputStreamLoader;

public class KeyguardWallpaperUtils {
    private static Point mTmpPoint = new Point();
    private static boolean sIsSupportWallpaperBlur;
    private static Pair<File, Drawable> sLockWallpaperCache;
    private static boolean sLockWallpaperChangedForSleep;
    private static long sLockWallpaperModifiedTime;
    private static Object sWallpaperLock = new Object();

    public static void resetLockWallpaperProviderIfNeeded(Context context) {
    }

    static {
        Class<WallpaperService.Engine> cls = WallpaperService.Engine.class;
        try {
            cls.getDeclaredMethod("setBlurCurrent", new Class[]{Float.TYPE});
            sIsSupportWallpaperBlur = true;
        } catch (Exception unused) {
            sIsSupportWallpaperBlur = false;
        }
    }

    public static Drawable getLockWallpaperPreview(Context context) {
        WallpaperInfo wallpaperInfo;
        Drawable loadDrawable;
        Drawable loadAssetsDrawable;
        Drawable loadDrawable2;
        if (!WallpaperAuthorityUtils.isThemeLockLiveWallpaper(context) || (wallpaperInfo = getWallpaperInfo()) == null) {
            Pair<File, Drawable> lockWallpaper = getLockWallpaper(context);
            if (lockWallpaper == null) {
                return null;
            }
            return (Drawable) lockWallpaper.second;
        } else if ("com.miui.miwallpaper.MiWallpaper".equals(wallpaperInfo.getServiceName()) && isMiwallpaperPreviewExist() && (loadDrawable2 = loadDrawable(context, "/data/system/theme/miwallpaper_preview")) != null) {
            return loadDrawable2;
        } else {
            String video24WallpaperThumnailName = KeyguardUpdateMonitor.getVideo24WallpaperThumnailName();
            if ("com.android.systemui.wallpaper.Video24WallpaperService".equals(wallpaperInfo.getServiceName()) && !TextUtils.isEmpty(video24WallpaperThumnailName) && (loadAssetsDrawable = loadAssetsDrawable(context, video24WallpaperThumnailName)) != null) {
                return loadAssetsDrawable;
            }
            if ("com.android.thememanager.service.VideoWallpaperService".equals(wallpaperInfo.getServiceName()) && isVideoWallpaperPreviewExist() && (loadDrawable = loadDrawable(context, "/data/system/theme_magic/video/video_wallpaper_thumbnail.jpg")) != null) {
                return loadDrawable;
            }
            Drawable loadThumbnail = wallpaperInfo.loadThumbnail(context.getPackageManager());
            if (loadThumbnail == null) {
                return null;
            }
            if (loadThumbnail instanceof BitmapDrawable) {
                return loadThumbnail;
            }
            return new BitmapDrawable((Resources) null, DrawableUtils.drawable2Bitmap(loadThumbnail));
        }
    }

    public static final Pair<File, Drawable> getLockWallpaper(Context context) {
        File file;
        File file2;
        if (WallpaperAuthorityUtils.isHomeDefaultWallpaper(context)) {
            file2 = new File("/system/media/lockscreen/video/video_wallpaper.mp4");
            file = new File("/system/media/lockscreen/video/video_wallpaper_thumbnail.jpg");
        } else if (WallpaperAuthorityUtils.isThemeLockVideoWallpaper(context)) {
            file2 = new File("/data/system/theme_magic/video/video_wallpaper.mp4");
            file = new File("/data/system/theme_magic/video/video_wallpaper_thumbnail.jpg");
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
                        int i3 = i2;
                        i2 = i;
                        i = i3;
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
                Pair<File, Drawable> pair = sLockWallpaperCache;
                return pair;
            }
            Pair<File, Drawable> pair2 = sLockWallpaperCache;
            return pair2;
        }
    }

    public static boolean checkNeedDarkenWallpaper(Context context) {
        if (!(!WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(context) || KeyguardUpdateMonitor.getInstance(context).getLockScreenMagazineWallpaperInfo().imgLevel != 1) || !KeyguardUpdateMonitor.getInstance(context).needDarkenWallpaper()) {
            return false;
        }
        return true;
    }

    private static boolean isMiwallpaperPreviewExist() {
        return new File("/data/system/theme/miwallpaper_preview").exists();
    }

    private static boolean isVideoWallpaperPreviewExist() {
        return new File("/data/system/theme_magic/video/video_wallpaper_thumbnail.jpg").exists();
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x002e A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static android.graphics.drawable.Drawable loadAssetsDrawable(android.content.Context r2, java.lang.String r3) {
        /*
            r0 = 0
            android.content.res.AssetManager r1 = r2.getAssets()     // Catch:{ IOException -> 0x0019, all -> 0x0017 }
            java.io.InputStream r3 = r1.open(r3)     // Catch:{ IOException -> 0x0019, all -> 0x0017 }
            if (r3 == 0) goto L_0x0012
            android.graphics.Bitmap r1 = android.graphics.BitmapFactory.decodeStream(r3)     // Catch:{ IOException -> 0x0010 }
            goto L_0x0013
        L_0x0010:
            r1 = move-exception
            goto L_0x001b
        L_0x0012:
            r1 = r0
        L_0x0013:
            miui.util.IOUtils.closeQuietly((java.io.InputStream) r3)
            goto L_0x0022
        L_0x0017:
            r2 = move-exception
            goto L_0x0031
        L_0x0019:
            r1 = move-exception
            r3 = r0
        L_0x001b:
            r1.printStackTrace()     // Catch:{ all -> 0x002f }
            miui.util.IOUtils.closeQuietly((java.io.InputStream) r3)
            r1 = r0
        L_0x0022:
            if (r1 == 0) goto L_0x002e
            android.graphics.drawable.BitmapDrawable r3 = new android.graphics.drawable.BitmapDrawable
            android.content.res.Resources r2 = r2.getResources()
            r3.<init>(r2, r1)
            return r3
        L_0x002e:
            return r0
        L_0x002f:
            r2 = move-exception
            r0 = r3
        L_0x0031:
            miui.util.IOUtils.closeQuietly((java.io.InputStream) r0)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.KeyguardWallpaperUtils.loadAssetsDrawable(android.content.Context, java.lang.String):android.graphics.drawable.Drawable");
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

    public static boolean setLockWallpaper(Context context, Uri uri, boolean z) {
        synchronized (sWallpaperLock) {
            if (!Utilities.isUriFileExists(context, uri)) {
                return false;
            }
            Point screenSize = getScreenSize(context);
            Bitmap rotatedBitmap = getRotatedBitmap(context, uri);
            if (rotatedBitmap == null) {
                return false;
            }
            if (((float) rotatedBitmap.getWidth()) / ((float) rotatedBitmap.getHeight()) == ((float) screenSize.x) / ((float) screenSize.y)) {
                boolean lockWallpaperWithoutCrop = setLockWallpaperWithoutCrop(context, uri, z);
                return lockWallpaperWithoutCrop;
            }
            boolean lockWallpaper = setLockWallpaper(context, autoCropWallpaper(context, rotatedBitmap, screenSize), z, uri.toString());
            return lockWallpaper;
        }
    }

    private static boolean setLockWallpaperWithoutCrop(Context context, String str, String str2, boolean z) {
        setWallpaperSourceUri(context, "pref_key_lock_wallpaper_path", str2);
        return setLockWallpaperWithoutCrop(context, str, z);
    }

    private static boolean setLockWallpaperWithoutCrop(Context context, Uri uri, boolean z) {
        FileOutputStream fileOutputStream;
        if (uri == null || !Utilities.isUriFileExists(context, uri)) {
            return false;
        }
        InputStream inputStream = null;
        try {
            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            try {
                File tmpLockScreenFile = getTmpLockScreenFile(context);
                fileOutputStream = new FileOutputStream(tmpLockScreenFile);
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = openInputStream.read(bArr);
                        if (read != -1) {
                            fileOutputStream.write(bArr, 0, read);
                        } else {
                            boolean lockWallpaperWithoutCrop = setLockWallpaperWithoutCrop(context, tmpLockScreenFile.getPath(), uri.toString(), z);
                            IOUtils.closeQuietly(openInputStream);
                            IOUtils.closeQuietly((OutputStream) fileOutputStream);
                            return lockWallpaperWithoutCrop;
                        }
                    }
                } catch (Exception e) {
                    e = e;
                    inputStream = openInputStream;
                    try {
                        Log.e("KeyguardWallpaperUtils", "setLockWallpaperWithoutCrop", e);
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly((OutputStream) fileOutputStream);
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly((OutputStream) fileOutputStream);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = openInputStream;
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly((OutputStream) fileOutputStream);
                    throw th;
                }
            } catch (Exception e2) {
                e = e2;
                fileOutputStream = null;
                inputStream = openInputStream;
                Log.e("KeyguardWallpaperUtils", "setLockWallpaperWithoutCrop", e);
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly((OutputStream) fileOutputStream);
                return false;
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = null;
                inputStream = openInputStream;
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly((OutputStream) fileOutputStream);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            fileOutputStream = null;
            Log.e("KeyguardWallpaperUtils", "setLockWallpaperWithoutCrop", e);
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly((OutputStream) fileOutputStream);
            return false;
        } catch (Throwable th4) {
            th = th4;
            fileOutputStream = null;
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly((OutputStream) fileOutputStream);
            throw th;
        }
    }

    private static File getTmpLockScreenFile(Context context) throws IOException {
        File file;
        String absolutePath = context.getExternalCacheDir().getAbsolutePath();
        if (absolutePath != null) {
            file = new File(absolutePath + File.separator + "lock_wallpaper");
        } else {
            file = new File("/sdcard/android/data/lock_wallpaper");
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private static boolean setLockWallpaperWithoutCrop(Context context, String str, boolean z) {
        synchronized (sWallpaperLock) {
            new File("/data/system/theme/").mkdirs();
            new File("/data/system/theme/lock_wallpaper").delete();
            ThemeFileUtils.copy(str, "/data/system/theme/lock_wallpaper");
            ThemeFileUtils.remove(str);
            sLockWallpaperChangedForSleep = true;
            Log.d("KeyguardWallpaperUtils", "setLockWallpaperWithoutCrop copy src = " + str);
            ThemeUtils.updateFilePermissionWithThemeContext("/data/system/theme/lock_wallpaper");
            onLockWallpaperChanged(context, z);
        }
        return true;
    }

    private static boolean onLockWallpaperChanged(Context context, boolean z) {
        if (!z) {
            PreferenceUtils.removeKey(context, "pref_key_current_lock_screen_wallpaper_info");
            MiuiSettingsUtils.putStringToSystem(context.getContentResolver(), "lock_wallpaper_provider_authority", "com.miui.home.none_provider");
            return true;
        }
        setLockScreenShowLiveWallpaper(context, false);
        return true;
    }

    private static void setLockScreenShowLiveWallpaper(Context context, boolean z) {
        PreferenceUtils.putBoolean(context, "pref_key_lock_screen_show_live_wallpaper", z);
        if (z) {
            MiuiSettingsUtils.putStringToSystem(context.getContentResolver(), "lock_wallpaper_provider_authority", "com.miui.home.none_provider");
        }
    }

    private static void setWallpaperSourceUri(Context context, String str, String str2) {
        PreferenceUtils.putString(context, str, str2);
    }

    private static boolean setLockWallpaper(Context context, Bitmap bitmap, boolean z, String str) {
        synchronized (sWallpaperLock) {
            try {
                File tmpLockScreenFile = getTmpLockScreenFile(context);
                if (bitmap != null) {
                    if (!saveToJPG(bitmap, tmpLockScreenFile.getAbsolutePath())) {
                        return false;
                    }
                    setLockWallpaperWithoutCrop(context, tmpLockScreenFile.getAbsolutePath(), str, z);
                    tmpLockScreenFile.delete();
                }
                if (!new File("/data/system/theme/lock_wallpaper").exists()) {
                    return false;
                }
                boolean onLockWallpaperChanged = onLockWallpaperChanged(context, z);
                return onLockWallpaperChanged;
            } catch (Exception e) {
                Log.e("KeyguardWallpaperUtils", "setLockWallpaper ", e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private static boolean saveToJPG(Bitmap bitmap, String str) {
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(str);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream2);
                IOUtils.closeQuietly((OutputStream) fileOutputStream2);
                return true;
            } catch (Exception e) {
                e = e;
                fileOutputStream = fileOutputStream2;
                try {
                    Log.e("KeyguardWallpaperUtils", "saveToJPG ", e);
                    IOUtils.closeQuietly((OutputStream) fileOutputStream);
                    return false;
                } catch (Throwable th) {
                    th = th;
                    IOUtils.closeQuietly((OutputStream) fileOutputStream);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fileOutputStream2;
                IOUtils.closeQuietly((OutputStream) fileOutputStream);
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
            Log.e("KeyguardWallpaperUtils", "saveToJPG ", e);
            IOUtils.closeQuietly((OutputStream) fileOutputStream);
            return false;
        }
    }

    private static Bitmap autoCropWallpaper(Context context, Bitmap bitmap, Point point) {
        Bitmap bitmap2 = null;
        if (bitmap == null) {
            return null;
        }
        try {
            float min = Math.min((((float) bitmap.getWidth()) * 1.0f) / ((float) point.x), (((float) bitmap.getHeight()) * 1.0f) / ((float) point.y));
            int width = (int) ((((float) bitmap.getWidth()) - (((float) point.x) * min)) / 2.0f);
            int height = (int) ((((float) bitmap.getHeight()) - (((float) point.y) * min)) / 2.0f);
            BitmapFactory.CropOption cropOption = new BitmapFactory.CropOption();
            Rect rect = new Rect(width, height, bitmap.getWidth() - width, bitmap.getHeight() - height);
            cropOption.srcBmpDrawingArea = rect;
            bitmap2 = Utilities.createBitmapSafely(rect.width(), cropOption.srcBmpDrawingArea.height(), bitmap.getConfig());
            BitmapFactory.cropBitmap(bitmap, bitmap2, cropOption);
        } catch (OutOfMemoryError e) {
            Log.e("KeyguardWallpaperUtils", "autoCropWallpaper", e);
        } catch (Throwable th) {
            bitmap.recycle();
            throw th;
        }
        bitmap.recycle();
        return bitmap2;
    }

    private static Point getScreenSize(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        int rotation = defaultDisplay.getRotation();
        boolean z = rotation == 0 || rotation == 2;
        getRealSize(defaultDisplay, mTmpPoint);
        Point point2 = mTmpPoint;
        point.x = z ? point2.x : point2.y;
        Point point3 = mTmpPoint;
        point.y = z ? point3.y : point3.x;
        return point;
    }

    private static Bitmap getRotatedBitmap(Context context, Uri uri) {
        if (!Utilities.isUriFileExists(context, uri)) {
            return null;
        }
        try {
            BitmapFactory.Options bitmapSize = miui.graphics.BitmapFactory.getBitmapSize(context, uri);
            Rect rect = new Rect(0, 0, bitmapSize.outWidth, bitmapSize.outHeight);
            InputStreamLoader inputStreamLoader = new InputStreamLoader(context, uri);
            int imageRotation = Utilities.getImageRotation(inputStreamLoader.get());
            inputStreamLoader.close();
            return decodeRegion(context, uri, rect, (imageRotation == 90 || imageRotation == 270) ? bitmapSize.outHeight : bitmapSize.outWidth, (imageRotation == 90 || imageRotation == 270) ? bitmapSize.outWidth : bitmapSize.outHeight, imageRotation);
        } catch (IOException e) {
            Log.e("KeyguardWallpaperUtils", "getRotatedBitmap", e);
            return null;
        }
    }

    private static int computeSampleSizeLarger(float f) {
        int floor = (int) Math.floor((double) (1.0f / f));
        if (floor <= 1) {
            return 1;
        }
        if (floor <= 8) {
            return Integer.highestOneBit(floor);
        }
        return (floor / 8) * 8;
    }

    private static void drawInTiles(Canvas canvas, int i, BitmapRegionDecoder bitmapRegionDecoder, Rect rect, int i2, int i3, int i4) {
        int i5;
        int i6;
        Bitmap decodeRegion;
        Canvas canvas2 = canvas;
        int i7 = i;
        BitmapRegionDecoder bitmapRegionDecoder2 = bitmapRegionDecoder;
        int i8 = i2;
        int i9 = i3;
        int i10 = i4;
        int i11 = i10 * 512;
        Rect rect2 = new Rect();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = i10;
        if (i7 == 90 || i7 == 270) {
            float f = (float) i10;
            canvas2.scale((((float) i8) * f) / ((float) rect.height()), (f * ((float) i9)) / ((float) rect.width()));
        } else {
            float f2 = (float) i10;
            canvas2.scale((((float) i8) * f2) / ((float) rect.width()), (f2 * ((float) i9)) / ((float) rect.height()));
        }
        Paint paint = new Paint(2);
        int height = ((i7 == 90 || i7 == 270) ? rect.height() : rect.width()) / i11;
        int width = ((i7 == 90 || i7 == 270) ? rect.width() : rect.height()) / i11;
        int i12 = 0;
        while (i12 <= height) {
            int i13 = 0;
            while (i13 <= width) {
                int i14 = i13;
                int i15 = i12;
                calcTileRect(rect2, rect, i, i12, i13, i11);
                if (rect2.intersect(rect)) {
                    synchronized (bitmapRegionDecoder) {
                        decodeRegion = bitmapRegionDecoder2.decodeRegion(rect2, options);
                    }
                    if (decodeRegion != null && !rect2.isEmpty()) {
                        if (i7 != 0) {
                            Matrix matrix = new Matrix();
                            matrix.setRotate((float) i7, (float) (decodeRegion.getWidth() / 2), (float) (decodeRegion.getHeight() / 2));
                            Bitmap createBitmap = Bitmap.createBitmap(decodeRegion, 0, 0, decodeRegion.getWidth(), decodeRegion.getHeight(), matrix, false);
                            decodeRegion.recycle();
                            decodeRegion = createBitmap;
                        }
                        i6 = i15;
                        i5 = i14;
                        canvas2.drawBitmap(decodeRegion, (float) (i6 * 512), (float) (i5 * 512), paint);
                        decodeRegion.recycle();
                        i13 = i5 + 1;
                        i12 = i6;
                    }
                }
                i5 = i14;
                i6 = i15;
                i13 = i5 + 1;
                i12 = i6;
            }
            Rect rect3 = rect;
            i12++;
        }
    }

    private static void calcTileRect(Rect rect, Rect rect2, int i, int i2, int i3, int i4) {
        if (i == 90) {
            rect.left = rect2.left + (i3 * i4);
            rect.top = rect2.bottom - ((i2 + 1) * i4);
        } else if (i == 180) {
            rect.left = rect2.right - ((i2 + 1) * i4);
            rect.top = rect2.bottom - ((i3 + 1) * i4);
        } else if (i == 270) {
            rect.left = rect2.right - ((i3 + 1) * i4);
            rect.top = rect2.top + (i2 * i4);
        } else {
            rect.left = rect2.left + (i2 * i4);
            rect.top = rect2.top + (i3 * i4);
        }
        rect.right = rect.left + i4;
        rect.bottom = rect.top + i4;
    }

    public static Bitmap decodeRegion(Context context, Uri uri, Rect rect, int i, int i2, int i3) {
        InputStreamLoader inputStreamLoader = new InputStreamLoader(context, uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (i3 == 90 || i3 == 270) {
            options.inSampleSize = computeSampleSizeLarger(Math.max(((float) i) / ((float) rect.height()), ((float) i2) / ((float) rect.width())));
        } else {
            options.inSampleSize = computeSampleSizeLarger(Math.max(((float) i) / ((float) rect.width()), ((float) i2) / ((float) rect.height())));
        }
        try {
            if (inputStreamLoader.get() == null) {
                return null;
            }
            BitmapRegionDecoder newInstance = BitmapRegionDecoder.newInstance(inputStreamLoader.get(), true);
            inputStreamLoader.close();
            Bitmap createBitmapSafely = Utilities.createBitmapSafely(i, i2, Bitmap.Config.ARGB_8888);
            if (createBitmapSafely != null) {
                drawInTiles(new Canvas(createBitmapSafely), i3, newInstance, rect, i, i2, options.inSampleSize);
            }
            return createBitmapSafely;
        } catch (IOException e) {
            Log.e("KeyguardWallpaperUtils", "decodeRegion", e);
            return null;
        } finally {
            inputStreamLoader.close();
        }
    }

    public static void getRealSize(Display display, Point point) {
        CustomizeUtil.getRealSize(display, point);
    }

    public static boolean isDefaultLockStyle(Context context) {
        return !new File("/data/system/theme//lockscreen").exists() && !isKeyguardShowLiveWallpaper(context);
    }

    private static boolean isKeyguardShowLiveWallpaper(Context context) {
        return getWorldReadableSharedPreference(context).getBoolean("keyguard_show_livewallpaper", false);
    }

    private static SharedPreferences getWorldReadableSharedPreference(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_world_readable_preferences", 0);
    }

    public static void clearWallpaperSrc(Context context) {
        setWallpaperSourceUri(context, "pref_key_lock_wallpaper_path", (String) null);
    }

    public static String getCurrentWallpaperInfo(Context context) {
        String str;
        if (LockScreenMagazineController.getInstance(context).isDecoupleHome()) {
            str = PreferenceUtils.getString(context, "pref_key_current_lock_screen_wallpaper_info", "");
        } else {
            str = HomeUtils.getCurrentWallpaperInfo(context);
        }
        Log.d("KeyguardWallpaperUtils", "getCurrentWallpaperInfo infoString=" + str);
        return str;
    }

    public static void updateCurrentWallpaperInfo(Context context, String str) {
        if (LockScreenMagazineController.getInstance(context).isDecoupleHome()) {
            PreferenceUtils.putString(context, "pref_key_current_lock_screen_wallpaper_info", str);
        } else {
            HomeUtils.updateCurrentWallpaperInfo(context, str);
        }
    }

    public static void setProviderClosedByUser(Context context, boolean z) {
        PreferenceUtils.putBoolean(context, "pref_key_provider_closed", z);
    }

    public static WallpaperInfo getWallpaperInfo() {
        try {
            return IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).getWallpaperInfo(KeyguardUpdateMonitor.getCurrentUser());
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isSupportWallpaperBlur() {
        return sIsSupportWallpaperBlur;
    }

    public static boolean isWallpaperShouldBlur(Context context) {
        return !MiuiGxzwManager.isGxzwSensor() || !KeyguardUpdateMonitor.getInstance(context).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
    }

    public static boolean hasKeyguardWallpaperEffects(Context context) {
        return context.getResources().getBoolean(R.bool.miui_config_hasKeyguardWallpaperEffects);
    }
}
