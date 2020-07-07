package com.android.systemui.recents.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;

/* compiled from: RecentsTaskLoader */
class BackgroundTaskLoader implements Runnable {
    static boolean DEBUG = false;
    static String TAG = "TaskResourceLoader";
    boolean mCancelled;
    Context mContext;
    BitmapDrawable mDefaultIcon;
    Bitmap mDefaultThumbnail;
    TaskKeyLruCache<Drawable> mIconCache;
    TaskResourceLoadQueue mLoadQueue;
    HandlerThread mLoadThread = new HandlerThread("Recents-TaskResourceLoader", 10);
    Handler mLoadThreadHandler;
    Handler mMainThreadHandler = new Handler();
    TaskKeyLruCache<ThumbnailData> mSnapshotCache;
    TaskKeyLruCache<ThumbnailData> mThumbnailCache;
    boolean mWaitingOnLoadQueue;

    public BackgroundTaskLoader(TaskResourceLoadQueue taskResourceLoadQueue, TaskKeyLruCache<Drawable> taskKeyLruCache, TaskKeyLruCache<ThumbnailData> taskKeyLruCache2, TaskKeyLruCache<ThumbnailData> taskKeyLruCache3, Bitmap bitmap, BitmapDrawable bitmapDrawable) {
        this.mLoadQueue = taskResourceLoadQueue;
        this.mIconCache = taskKeyLruCache;
        this.mThumbnailCache = taskKeyLruCache2;
        this.mSnapshotCache = taskKeyLruCache3;
        this.mDefaultThumbnail = bitmap;
        this.mDefaultIcon = bitmapDrawable;
        this.mLoadThread.start();
        this.mLoadThreadHandler = new Handler(this.mLoadThread.getLooper());
        this.mLoadThreadHandler.post(this);
    }

    /* access modifiers changed from: package-private */
    public void start(Context context) {
        this.mContext = context.getApplicationContext();
        this.mCancelled = false;
        synchronized (this.mLoadThread) {
            this.mLoadThread.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        this.mCancelled = true;
        if (this.mWaitingOnLoadQueue) {
            this.mContext = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        r4 = r10.mLoadQueue.nextTask();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
            r10 = this;
        L_0x0000:
            android.os.HandlerThread r0 = r10.mLoadThread
            monitor-enter(r0)
        L_0x0003:
            boolean r1 = r10.mCancelled     // Catch:{ all -> 0x0158 }
            if (r1 == 0) goto L_0x0015
            r1 = 0
            r10.mContext = r1     // Catch:{ all -> 0x0158 }
            android.os.HandlerThread r1 = r10.mLoadThread     // Catch:{ InterruptedException -> 0x0010 }
            r1.wait()     // Catch:{ InterruptedException -> 0x0010 }
            goto L_0x0003
        L_0x0010:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0158 }
            goto L_0x0003
        L_0x0015:
            monitor-exit(r0)     // Catch:{ all -> 0x0158 }
            boolean r0 = r10.mCancelled
            if (r0 != 0) goto L_0x0000
            com.android.systemui.recents.RecentsConfiguration r0 = com.android.systemui.recents.Recents.getConfiguration()
            com.android.systemui.recents.misc.SystemServicesProxy r1 = com.android.systemui.recents.Recents.getSystemServices()
            r2 = 0
            r3 = 1
            if (r1 == 0) goto L_0x0134
            com.android.systemui.recents.model.TaskResourceLoadQueue r4 = r10.mLoadQueue
            com.android.systemui.recents.model.Task r4 = r4.nextTask()
            if (r4 == 0) goto L_0x0134
            com.android.systemui.recents.model.TaskKeyLruCache<android.graphics.drawable.Drawable> r5 = r10.mIconCache
            com.android.systemui.recents.model.Task$TaskKey r6 = r4.key
            java.lang.Object r5 = r5.get(r6)
            android.graphics.drawable.Drawable r5 = (android.graphics.drawable.Drawable) r5
            com.android.systemui.recents.model.TaskKeyLruCache<com.android.systemui.recents.model.ThumbnailData> r6 = r10.mSnapshotCache
            com.android.systemui.recents.model.Task$TaskKey r7 = r4.key
            java.lang.Object r6 = r6.get(r7)
            com.android.systemui.recents.model.ThumbnailData r6 = (com.android.systemui.recents.model.ThumbnailData) r6
            com.android.systemui.recents.model.TaskKeyLruCache<com.android.systemui.recents.model.ThumbnailData> r7 = r10.mSnapshotCache
            com.android.systemui.recents.model.Task$TaskKey r8 = r4.key
            r7.remove(r8)
            if (r6 != 0) goto L_0x0055
            com.android.systemui.recents.model.TaskKeyLruCache<com.android.systemui.recents.model.ThumbnailData> r6 = r10.mThumbnailCache
            com.android.systemui.recents.model.Task$TaskKey r7 = r4.key
            java.lang.Object r6 = r6.get(r7)
            com.android.systemui.recents.model.ThumbnailData r6 = (com.android.systemui.recents.model.ThumbnailData) r6
        L_0x0055:
            if (r5 != 0) goto L_0x00a8
            android.app.ActivityManager$TaskDescription r5 = r4.taskDescription
            com.android.systemui.recents.model.Task$TaskKey r7 = r4.key
            int r7 = r7.userId
            android.content.Context r8 = r10.mContext
            android.content.res.Resources r8 = r8.getResources()
            android.graphics.drawable.Drawable r5 = r1.getBadgedTaskDescriptionIcon(r5, r7, r8)
            if (r5 != 0) goto L_0x009d
            com.android.systemui.recents.model.Task$TaskKey r7 = r4.key
            android.content.ComponentName r7 = r7.getComponent()
            com.android.systemui.recents.model.Task$TaskKey r8 = r4.key
            int r8 = r8.userId
            android.content.pm.ActivityInfo r7 = r1.getActivityInfo(r7, r8)
            if (r7 == 0) goto L_0x009d
            boolean r5 = DEBUG
            if (r5 == 0) goto L_0x0095
            java.lang.String r5 = TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "Loading icon: "
            r8.append(r9)
            com.android.systemui.recents.model.Task$TaskKey r9 = r4.key
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r5, r8)
        L_0x0095:
            com.android.systemui.recents.model.Task$TaskKey r5 = r4.key
            int r5 = r5.userId
            android.graphics.drawable.Drawable r5 = r1.getBadgedActivityIcon(r7, r5)
        L_0x009d:
            if (r5 != 0) goto L_0x00a1
            android.graphics.drawable.BitmapDrawable r5 = r10.mDefaultIcon
        L_0x00a1:
            com.android.systemui.recents.model.TaskKeyLruCache<android.graphics.drawable.Drawable> r7 = r10.mIconCache
            com.android.systemui.recents.model.Task$TaskKey r8 = r4.key
            r7.put(r8, r5)
        L_0x00a8:
            if (r6 != 0) goto L_0x00dc
            int r7 = r0.svelteLevel
            r8 = 3
            if (r7 >= r8) goto L_0x00d2
            boolean r6 = DEBUG
            if (r6 == 0) goto L_0x00cb
            java.lang.String r6 = TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Loading thumbnail: "
            r7.append(r8)
            com.android.systemui.recents.model.Task$TaskKey r8 = r4.key
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.d(r6, r7)
        L_0x00cb:
            com.android.systemui.recents.model.Task$TaskKey r6 = r4.key
            com.android.systemui.recents.model.ThumbnailData r1 = r1.getTaskThumbnail(r6)
            r6 = r1
        L_0x00d2:
            android.graphics.Bitmap r1 = r6.thumbnail
            if (r1 != 0) goto L_0x00da
            android.graphics.Bitmap r1 = r10.mDefaultThumbnail
            r6.thumbnail = r1
        L_0x00da:
            r1 = r3
            goto L_0x00dd
        L_0x00dc:
            r1 = r2
        L_0x00dd:
            int r0 = r0.svelteLevel
            if (r0 >= r3) goto L_0x0126
            boolean r0 = r4.isBlurThumbnail()
            if (r0 == 0) goto L_0x011b
            boolean r0 = r6.isDeterminedWhetherBlur
            if (r0 != 0) goto L_0x011b
            android.graphics.Bitmap r0 = r6.thumbnail     // Catch:{ Exception -> 0x011b }
            android.graphics.Bitmap$Config r7 = android.graphics.Bitmap.Config.ARGB_8888     // Catch:{ Exception -> 0x011b }
            android.graphics.Bitmap r0 = r0.copy(r7, r2)     // Catch:{ Exception -> 0x011b }
            int r7 = r0.getWidth()     // Catch:{ Exception -> 0x011b }
            int r7 = r7 / 4
            int r8 = r0.getHeight()     // Catch:{ Exception -> 0x011b }
            int r8 = r8 / 4
            android.graphics.Bitmap r0 = miui.graphics.BitmapFactory.scaleBitmap(r0, r7, r8)     // Catch:{ Exception -> 0x011b }
            r7 = 24
            android.graphics.Bitmap r0 = miui.graphics.BitmapFactory.fastBlur(r0, r7)     // Catch:{ Exception -> 0x011b }
            android.graphics.Bitmap r7 = r6.thumbnail     // Catch:{ Exception -> 0x011b }
            int r7 = r7.getWidth()     // Catch:{ Exception -> 0x011b }
            android.graphics.Bitmap r8 = r6.thumbnail     // Catch:{ Exception -> 0x011b }
            int r8 = r8.getHeight()     // Catch:{ Exception -> 0x011b }
            android.graphics.Bitmap r0 = miui.graphics.BitmapFactory.scaleBitmap(r0, r7, r8)     // Catch:{ Exception -> 0x011b }
            r6.thumbnail = r0     // Catch:{ Exception -> 0x011b }
        L_0x011b:
            r6.isDeterminedWhetherBlur = r3
            if (r1 == 0) goto L_0x0126
            com.android.systemui.recents.model.TaskKeyLruCache<com.android.systemui.recents.model.ThumbnailData> r0 = r10.mThumbnailCache
            com.android.systemui.recents.model.Task$TaskKey r1 = r4.key
            r0.put(r1, r6)
        L_0x0126:
            boolean r0 = r10.mCancelled
            if (r0 != 0) goto L_0x0134
            android.os.Handler r0 = r10.mMainThreadHandler
            com.android.systemui.recents.model.BackgroundTaskLoader$1 r1 = new com.android.systemui.recents.model.BackgroundTaskLoader$1
            r1.<init>(r4, r6, r5)
            r0.post(r1)
        L_0x0134:
            com.android.systemui.recents.model.TaskResourceLoadQueue r0 = r10.mLoadQueue
            monitor-enter(r0)
        L_0x0137:
            boolean r1 = r10.mCancelled     // Catch:{ all -> 0x0155 }
            if (r1 != 0) goto L_0x0152
            com.android.systemui.recents.model.TaskResourceLoadQueue r1 = r10.mLoadQueue     // Catch:{ all -> 0x0155 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x0155 }
            if (r1 == 0) goto L_0x0152
            r10.mWaitingOnLoadQueue = r3     // Catch:{ InterruptedException -> 0x014d }
            com.android.systemui.recents.model.TaskResourceLoadQueue r1 = r10.mLoadQueue     // Catch:{ InterruptedException -> 0x014d }
            r1.wait()     // Catch:{ InterruptedException -> 0x014d }
            r10.mWaitingOnLoadQueue = r2     // Catch:{ InterruptedException -> 0x014d }
            goto L_0x0137
        L_0x014d:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x0155 }
            goto L_0x0137
        L_0x0152:
            monitor-exit(r0)     // Catch:{ all -> 0x0155 }
            goto L_0x0000
        L_0x0155:
            r10 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0155 }
            throw r10
        L_0x0158:
            r10 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0158 }
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.model.BackgroundTaskLoader.run():void");
    }
}
