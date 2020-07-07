package com.android.systemui.miui;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import miui.content.res.IconCustomizer;
import miui.maml.FancyDrawable;
import miui.maml.util.AppIconsHelper;

public class AppIconsManager implements Dumpable, ConfigurationController.ConfigurationListener, PackageEventReceiver {
    private final SparseArray<WeakHashMap<Bitmap, WeakReference<Bitmap>>> mIconStyledCache = new SparseArray<>();
    private final SparseArray<ConcurrentHashMap<String, WeakReference<Bitmap>>> mIconsCache = new SparseArray<>();
    private final SparseArray<ConcurrentHashMap<String, WeakReference<Bitmap>>> mQuietFancyIconsCache = new SparseArray<>();

    public void onConfigChanged(Configuration configuration) {
    }

    public void onPackageChanged(int i, String str) {
    }

    public AppIconsManager() {
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public Drawable getAppIcon(Context context, ApplicationInfo applicationInfo, PackageManager packageManager, int i) {
        return getAppIconInner(context, applicationInfo.packageName, i, applicationInfo, packageManager);
    }

    public Drawable getAppIcon(Context context, String str, int i) {
        return getAppIconInner(context, str, i, (ApplicationInfo) null, (PackageManager) null);
    }

    private Drawable getAppIconInner(Context context, String str, int i, ApplicationInfo applicationInfo, PackageManager packageManager) {
        Bitmap appIconBitmapCache = getAppIconBitmapCache(str, i, false);
        if (appIconBitmapCache != null) {
            return new BitmapDrawable(Resources.getSystem(), appIconBitmapCache);
        }
        Drawable loadAppIcon = loadAppIcon(context, str, i, applicationInfo, packageManager);
        if (loadAppIcon instanceof BitmapDrawable) {
            cacheWeakRef(this.mIconsCache.get(i), str, ((BitmapDrawable) loadAppIcon).getBitmap());
            log("icon cache missed for " + str + ", load and put bitmap cache, userId: " + i);
        } else {
            log("don't store cache for non-BitmapDrawable: " + str + ", " + loadAppIcon);
        }
        return loadAppIcon;
    }

    public Bitmap getAppIconBitmap(Context context, String str) {
        return getAppIconBitmap(context, str, UserHandle.myUserId());
    }

    public Bitmap getAppIconBitmap(Context context, String str, int i) {
        boolean z = true;
        Bitmap appIconBitmapCache = getAppIconBitmapCache(str, i, true);
        if (appIconBitmapCache == null) {
            FancyDrawable loadAppIcon = loadAppIcon(context, str, i);
            boolean z2 = false;
            if (loadAppIcon instanceof FancyDrawable) {
                Drawable quietDrawable = loadAppIcon.getQuietDrawable();
                if (quietDrawable != null) {
                    z2 = true;
                }
                if (!z2) {
                    try {
                        quietDrawable = context.getPackageManager().getApplicationIcon(str);
                    } catch (PackageManager.NameNotFoundException unused) {
                    }
                }
                if (quietDrawable != null) {
                    loadAppIcon = quietDrawable;
                }
            } else {
                z = false;
            }
            appIconBitmapCache = DrawableUtils.drawable2Bitmap(loadAppIcon);
            if (!z) {
                cacheWeakRef(this.mIconsCache.get(i), str, appIconBitmapCache);
            } else if (z2) {
                cacheWeakRef(this.mQuietFancyIconsCache.get(i), str, appIconBitmapCache);
            }
            log("bitmap cache missed for " + str + ", load and put cache " + appIconBitmapCache + ", userId: " + i + ", isFancyDrawable: " + z + ", isQuietDrawable: " + z2);
        }
        return appIconBitmapCache;
    }

    private Bitmap getAppIconBitmapCache(String str, int i, boolean z) {
        synchronized (this.mIconsCache) {
            if (this.mIconsCache.get(i) == null) {
                this.mIconsCache.put(i, new ConcurrentHashMap());
            }
            if (this.mQuietFancyIconsCache.get(i) == null) {
                this.mQuietFancyIconsCache.put(i, new ConcurrentHashMap());
            }
        }
        Bitmap appIconBitmapCacheForUser = getAppIconBitmapCacheForUser(str, this.mIconsCache.get(i));
        if (appIconBitmapCacheForUser == null && z) {
            log("query quiet drawable cache for " + str);
            appIconBitmapCacheForUser = getAppIconBitmapCacheForUser(str, this.mQuietFancyIconsCache.get(i));
        }
        if (appIconBitmapCacheForUser != null) {
            log("bitmap cache found for " + str + ", userId: " + i);
        }
        return appIconBitmapCacheForUser;
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [java.util.Map<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>, java.util.Map] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.Bitmap getAppIconBitmapCacheForUser(java.lang.String r1, java.util.Map<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>> r2) {
        /*
            r0 = this;
            java.lang.Object r0 = getCachedValue(r2, r1)
            android.graphics.Bitmap r0 = (android.graphics.Bitmap) r0
            if (r0 == 0) goto L_0x000e
            boolean r1 = r0.isRecycled()
            if (r1 == 0) goto L_0x000f
        L_0x000e:
            r0 = 0
        L_0x000f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.AppIconsManager.getAppIconBitmapCacheForUser(java.lang.String, java.util.Map):android.graphics.Bitmap");
    }

    private Drawable loadAppIcon(Context context, String str, int i) {
        return loadAppIcon(context, str, i, (ApplicationInfo) null, (PackageManager) null);
    }

    private Drawable loadAppIcon(Context context, String str, int i, ApplicationInfo applicationInfo, PackageManager packageManager) {
        if (applicationInfo == null) {
            try {
                applicationInfo = ActivityThread.getPackageManager().getApplicationInfo(str, 0, i);
            } catch (Exception unused) {
                return null;
            }
        }
        if (packageManager == null) {
            packageManager = context.getPackageManager();
        }
        if (applicationInfo != null) {
            return AppIconsHelper.getIconDrawable(context, applicationInfo, packageManager);
        }
        return null;
    }

    public Drawable getIconStyleDrawable(Drawable drawable, boolean z) {
        boolean z2 = !z;
        synchronized (this.mIconStyledCache) {
            if (this.mIconStyledCache.get(z2 ? 1 : 0) == null) {
                this.mIconStyledCache.put(z2, new WeakHashMap());
            }
        }
        WeakHashMap weakHashMap = this.mIconStyledCache.get(z2);
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap2 = null;
            if (weakHashMap.containsKey(bitmap)) {
                bitmap2 = (Bitmap) ((WeakReference) weakHashMap.get(bitmap)).get();
            }
            if (bitmap2 == null || bitmap2.isRecycled()) {
                bitmap2 = DrawableUtils.drawable2Bitmap(IconCustomizer.generateIconStyleDrawable(drawable, z));
                weakHashMap.put(bitmap, new WeakReference(bitmap2));
                log("icon style cache missing for request: " + drawable);
            } else {
                log("icon style cache found for request: " + drawable);
            }
            return new BitmapDrawable(Resources.getSystem(), bitmap2);
        }
        log("don't store cache for non-BitmapDrawable in getIconStyleDrawable " + drawable);
        return IconCustomizer.generateIconStyleDrawable(drawable, z);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00ae, code lost:
        r0 = r10.mIconStyledCache;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b0, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00b1, code lost:
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00b8, code lost:
        if (r11 >= r10.mIconStyledCache.size()) goto L_0x0101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ba, code lost:
        r1 = r10.mIconStyledCache.get(r10.mIconStyledCache.keyAt(r11));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c8, code lost:
        if (r1 != null) goto L_0x00cc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ca, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00cb, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00cc, code lost:
        r5 = validCount(r1);
        r6 = java.util.Locale.getDefault();
        r8 = new java.lang.Object[3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00de, code lost:
        if (r10.mIconStyledCache.keyAt(r11) != 0) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00e0, code lost:
        r9 = "crop";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00e3, code lost:
        r9 = "non-crop";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00e5, code lost:
        r8[0] = r9;
        r8[1] = java.lang.Integer.valueOf(r1.size());
        r8[2] = java.lang.Integer.valueOf(r5);
        r12.println(java.lang.String.format(r6, "icon-styled cache for %s, count: %d, valid: %d", r8));
        r11 = r11 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0101, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0102, code lost:
        r12.println();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0105, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(java.io.FileDescriptor r11, java.io.PrintWriter r12, java.lang.String[] r13) {
        /*
            r10 = this;
            java.lang.String r11 = "AppIconsManager:"
            r12.println(r11)
            java.lang.String r11 = "AppIcons:"
            r12.println(r11)
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r11 = r10.mIconsCache
            monitor-enter(r11)
            r13 = 0
            r0 = r13
        L_0x000f:
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r1 = r10.mIconsCache     // Catch:{ all -> 0x0109 }
            int r1 = r1.size()     // Catch:{ all -> 0x0109 }
            r2 = 2
            r3 = 3
            r4 = 1
            if (r0 >= r1) goto L_0x005f
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r1 = r10.mIconsCache     // Catch:{ all -> 0x0109 }
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r5 = r10.mIconsCache     // Catch:{ all -> 0x0109 }
            int r5 = r5.keyAt(r0)     // Catch:{ all -> 0x0109 }
            java.lang.Object r1 = r1.get(r5)     // Catch:{ all -> 0x0109 }
            java.util.Map r1 = (java.util.Map) r1     // Catch:{ all -> 0x0109 }
            if (r1 != 0) goto L_0x002c
            monitor-exit(r11)     // Catch:{ all -> 0x0109 }
            return
        L_0x002c:
            int r5 = validCount(r1)     // Catch:{ all -> 0x0109 }
            java.util.Locale r6 = java.util.Locale.getDefault()     // Catch:{ all -> 0x0109 }
            java.lang.String r7 = "userId: %d, cache size: %d, valid bitmaps: %d"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ all -> 0x0109 }
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r8 = r10.mIconsCache     // Catch:{ all -> 0x0109 }
            int r8 = r8.keyAt(r0)     // Catch:{ all -> 0x0109 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x0109 }
            r3[r13] = r8     // Catch:{ all -> 0x0109 }
            int r1 = r1.size()     // Catch:{ all -> 0x0109 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0109 }
            r3[r4] = r1     // Catch:{ all -> 0x0109 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0109 }
            r3[r2] = r1     // Catch:{ all -> 0x0109 }
            java.lang.String r1 = java.lang.String.format(r6, r7, r3)     // Catch:{ all -> 0x0109 }
            r12.println(r1)     // Catch:{ all -> 0x0109 }
            int r0 = r0 + 1
            goto L_0x000f
        L_0x005f:
            r0 = r13
        L_0x0060:
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r1 = r10.mQuietFancyIconsCache     // Catch:{ all -> 0x0109 }
            int r1 = r1.size()     // Catch:{ all -> 0x0109 }
            if (r0 >= r1) goto L_0x00ad
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r1 = r10.mQuietFancyIconsCache     // Catch:{ all -> 0x0109 }
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r5 = r10.mQuietFancyIconsCache     // Catch:{ all -> 0x0109 }
            int r5 = r5.keyAt(r0)     // Catch:{ all -> 0x0109 }
            java.lang.Object r1 = r1.get(r5)     // Catch:{ all -> 0x0109 }
            java.util.Map r1 = (java.util.Map) r1     // Catch:{ all -> 0x0109 }
            if (r1 != 0) goto L_0x007a
            monitor-exit(r11)     // Catch:{ all -> 0x0109 }
            return
        L_0x007a:
            int r5 = validCount(r1)     // Catch:{ all -> 0x0109 }
            java.util.Locale r6 = java.util.Locale.getDefault()     // Catch:{ all -> 0x0109 }
            java.lang.String r7 = "userId: %d, quiet drawable cache size: %d, valid bitmaps: %d"
            java.lang.Object[] r8 = new java.lang.Object[r3]     // Catch:{ all -> 0x0109 }
            android.util.SparseArray<java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r9 = r10.mQuietFancyIconsCache     // Catch:{ all -> 0x0109 }
            int r9 = r9.keyAt(r0)     // Catch:{ all -> 0x0109 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x0109 }
            r8[r13] = r9     // Catch:{ all -> 0x0109 }
            int r1 = r1.size()     // Catch:{ all -> 0x0109 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0109 }
            r8[r4] = r1     // Catch:{ all -> 0x0109 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0109 }
            r8[r2] = r1     // Catch:{ all -> 0x0109 }
            java.lang.String r1 = java.lang.String.format(r6, r7, r8)     // Catch:{ all -> 0x0109 }
            r12.println(r1)     // Catch:{ all -> 0x0109 }
            int r0 = r0 + 1
            goto L_0x0060
        L_0x00ad:
            monitor-exit(r11)     // Catch:{ all -> 0x0109 }
            android.util.SparseArray<java.util.WeakHashMap<android.graphics.Bitmap, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r0 = r10.mIconStyledCache
            monitor-enter(r0)
            r11 = r13
        L_0x00b2:
            android.util.SparseArray<java.util.WeakHashMap<android.graphics.Bitmap, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r1 = r10.mIconStyledCache     // Catch:{ all -> 0x0106 }
            int r1 = r1.size()     // Catch:{ all -> 0x0106 }
            if (r11 >= r1) goto L_0x0101
            android.util.SparseArray<java.util.WeakHashMap<android.graphics.Bitmap, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r1 = r10.mIconStyledCache     // Catch:{ all -> 0x0106 }
            android.util.SparseArray<java.util.WeakHashMap<android.graphics.Bitmap, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r5 = r10.mIconStyledCache     // Catch:{ all -> 0x0106 }
            int r5 = r5.keyAt(r11)     // Catch:{ all -> 0x0106 }
            java.lang.Object r1 = r1.get(r5)     // Catch:{ all -> 0x0106 }
            java.util.Map r1 = (java.util.Map) r1     // Catch:{ all -> 0x0106 }
            if (r1 != 0) goto L_0x00cc
            monitor-exit(r0)     // Catch:{ all -> 0x0106 }
            return
        L_0x00cc:
            int r5 = validCount(r1)     // Catch:{ all -> 0x0106 }
            java.util.Locale r6 = java.util.Locale.getDefault()     // Catch:{ all -> 0x0106 }
            java.lang.String r7 = "icon-styled cache for %s, count: %d, valid: %d"
            java.lang.Object[] r8 = new java.lang.Object[r3]     // Catch:{ all -> 0x0106 }
            android.util.SparseArray<java.util.WeakHashMap<android.graphics.Bitmap, java.lang.ref.WeakReference<android.graphics.Bitmap>>> r9 = r10.mIconStyledCache     // Catch:{ all -> 0x0106 }
            int r9 = r9.keyAt(r11)     // Catch:{ all -> 0x0106 }
            if (r9 != 0) goto L_0x00e3
            java.lang.String r9 = "crop"
            goto L_0x00e5
        L_0x00e3:
            java.lang.String r9 = "non-crop"
        L_0x00e5:
            r8[r13] = r9     // Catch:{ all -> 0x0106 }
            int r1 = r1.size()     // Catch:{ all -> 0x0106 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0106 }
            r8[r4] = r1     // Catch:{ all -> 0x0106 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x0106 }
            r8[r2] = r1     // Catch:{ all -> 0x0106 }
            java.lang.String r1 = java.lang.String.format(r6, r7, r8)     // Catch:{ all -> 0x0106 }
            r12.println(r1)     // Catch:{ all -> 0x0106 }
            int r11 = r11 + 1
            goto L_0x00b2
        L_0x0101:
            monitor-exit(r0)     // Catch:{ all -> 0x0106 }
            r12.println()
            return
        L_0x0106:
            r10 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0106 }
            throw r10
        L_0x0109:
            r10 = move-exception
            monitor-exit(r11)     // Catch:{ all -> 0x0109 }
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.AppIconsManager.dump(java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
    }

    public void onDensityOrFontScaleChanged() {
        synchronized (this.mIconsCache) {
            this.mIconsCache.clear();
            this.mQuietFancyIconsCache.clear();
        }
        synchronized (this.mIconStyledCache) {
            this.mIconStyledCache.clear();
        }
        log("clear all caches");
    }

    public void onPackageAdded(int i, String str, boolean z) {
        if (!z) {
            removeCachesForPackage(i, str);
        }
    }

    public void onPackageRemoved(int i, String str, boolean z, boolean z2) {
        if (z2) {
            removeCachesForPackage(i, str);
        }
    }

    private void removeCachesForPackage(int i, String str) {
        int userId = UserHandle.getUserId(i);
        synchronized (this.mIconsCache) {
            if (!(this.mIconsCache.get(userId) == null || this.mIconsCache.get(userId).remove(str) == null)) {
                log("user " + userId + ", cache for " + str + " removed");
            }
            if (!(this.mQuietFancyIconsCache.get(userId) == null || this.mQuietFancyIconsCache.get(userId).remove(str) == null)) {
                log("user " + userId + ", quiet drawable cache for " + str + " removed");
            }
        }
    }

    private static void log(String str) {
        if (Constants.DEBUG) {
            Log.i("AppIconsManager", str);
        }
    }

    private static int validCount(Map<?, WeakReference<Bitmap>> map) {
        int i = 0;
        for (Map.Entry<?, WeakReference<Bitmap>> value : map.entrySet()) {
            if (isWeakBitmapValid((WeakReference) value.getValue())) {
                i++;
            }
        }
        return i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:1:0x0002, code lost:
        r0 = (android.graphics.Bitmap) r0.get();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isWeakBitmapValid(java.lang.ref.WeakReference<android.graphics.Bitmap> r0) {
        /*
            if (r0 == 0) goto L_0x0012
            java.lang.Object r0 = r0.get()
            android.graphics.Bitmap r0 = (android.graphics.Bitmap) r0
            if (r0 == 0) goto L_0x0012
            boolean r0 = r0.isRecycled()
            if (r0 != 0) goto L_0x0012
            r0 = 1
            goto L_0x0013
        L_0x0012:
            r0 = 0
        L_0x0013:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.AppIconsManager.isWeakBitmapValid(java.lang.ref.WeakReference):boolean");
    }

    private static <K, V> V getCachedValue(Map<K, WeakReference<V>> map, K k) {
        WeakReference weakReference;
        if (map == null || (weakReference = map.get(k)) == null) {
            return null;
        }
        return weakReference.get();
    }

    private static <K, V> void cacheWeakRef(Map<K, WeakReference<V>> map, K k, V v) {
        if (map != null) {
            map.put(k, new WeakReference(v));
        }
    }
}
