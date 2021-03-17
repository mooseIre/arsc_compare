package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import libcore.io.IoUtils;

public class LockscreenWallpaper extends IWallpaperManagerCallback.Stub implements Runnable, Dumpable {
    private Bitmap mCache;
    private boolean mCached;
    private int mCurrentUserId = ActivityManager.getCurrentUser();
    private final Handler mH;
    private AsyncTask<Void, Void, LoaderResult> mLoader;
    private final NotificationMediaManager mMediaManager;
    private UserHandle mSelectedUser;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final WallpaperManager mWallpaperManager;

    public void onWallpaperColorsChanged(WallpaperColors wallpaperColors, int i, int i2) {
    }

    public LockscreenWallpaper(WallpaperManager wallpaperManager, IWallpaperManager iWallpaperManager, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, NotificationMediaManager notificationMediaManager, Handler handler) {
        dumpManager.registerDumpable(LockscreenWallpaper.class.getSimpleName(), this);
        this.mWallpaperManager = wallpaperManager;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mMediaManager = notificationMediaManager;
        this.mH = handler;
        if (iWallpaperManager != null) {
            try {
                iWallpaperManager.setLockWallpaperCallback(this);
            } catch (RemoteException e) {
                Log.e("LockscreenWallpaper", "System dead?" + e);
            }
        }
    }

    public LoaderResult loadBitmap(int i, UserHandle userHandle) {
        if (!this.mWallpaperManager.isWallpaperSupported()) {
            return LoaderResult.success(null);
        }
        if (userHandle != null) {
            i = userHandle.getIdentifier();
        }
        ParcelFileDescriptor wallpaperFile = this.mWallpaperManager.getWallpaperFile(2, i);
        if (wallpaperFile != null) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.HARDWARE;
                return LoaderResult.success(BitmapFactory.decodeFileDescriptor(wallpaperFile.getFileDescriptor(), null, options));
            } catch (OutOfMemoryError e) {
                Log.w("LockscreenWallpaper", "Can't decode file", e);
                return LoaderResult.fail();
            } finally {
                IoUtils.closeQuietly(wallpaperFile);
            }
        } else if (userHandle != null) {
            return LoaderResult.success(this.mWallpaperManager.getBitmapAsUser(userHandle.getIdentifier(), true));
        } else {
            return LoaderResult.success(null);
        }
    }

    public void setCurrentUser(int i) {
        if (i != this.mCurrentUserId) {
            UserHandle userHandle = this.mSelectedUser;
            if (userHandle == null || i != userHandle.getIdentifier()) {
                this.mCached = false;
            }
            this.mCurrentUserId = i;
        }
    }

    public void onWallpaperChanged() {
        postUpdateWallpaper();
    }

    private void postUpdateWallpaper() {
        Handler handler = this.mH;
        if (handler == null) {
            Log.wtfStack("LockscreenWallpaper", "Trying to use LockscreenWallpaper before initialization.");
            return;
        }
        handler.removeCallbacks(this);
        this.mH.post(this);
    }

    public void run() {
        AsyncTask<Void, Void, LoaderResult> asyncTask = this.mLoader;
        if (asyncTask != null) {
            asyncTask.cancel(false);
        }
        final int i = this.mCurrentUserId;
        final UserHandle userHandle = this.mSelectedUser;
        this.mLoader = new AsyncTask<Void, Void, LoaderResult>() {
            /* class com.android.systemui.statusbar.phone.LockscreenWallpaper.AnonymousClass1 */

            /* access modifiers changed from: protected */
            public LoaderResult doInBackground(Void... voidArr) {
                return LockscreenWallpaper.this.loadBitmap(i, userHandle);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(LoaderResult loaderResult) {
                super.onPostExecute((Object) loaderResult);
                if (!isCancelled()) {
                    if (loaderResult.success) {
                        LockscreenWallpaper.this.mCached = true;
                        LockscreenWallpaper.this.mCache = loaderResult.bitmap;
                        LockscreenWallpaper.this.mUpdateMonitor.setHasLockscreenWallpaper(loaderResult.bitmap != null);
                        LockscreenWallpaper.this.mMediaManager.updateMediaMetaData(true, true);
                    }
                    LockscreenWallpaper.this.mLoader = null;
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(LockscreenWallpaper.class.getSimpleName() + ":");
        IndentingPrintWriter increaseIndent = new IndentingPrintWriter(printWriter, "  ").increaseIndent();
        increaseIndent.println("mCached=" + this.mCached);
        increaseIndent.println("mCache=" + this.mCache);
        increaseIndent.println("mCurrentUserId=" + this.mCurrentUserId);
        increaseIndent.println("mSelectedUser=" + this.mSelectedUser);
    }

    /* access modifiers changed from: private */
    public static class LoaderResult {
        public final Bitmap bitmap;
        public final boolean success;

        LoaderResult(boolean z, Bitmap bitmap2) {
            this.success = z;
            this.bitmap = bitmap2;
        }

        static LoaderResult success(Bitmap bitmap2) {
            return new LoaderResult(true, bitmap2);
        }

        static LoaderResult fail() {
            return new LoaderResult(false, null);
        }
    }
}
