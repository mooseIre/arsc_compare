package com.android.systemui.screenshot;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/* compiled from: GlobalScreenshot */
class SaveImageInBackgroundTask extends AsyncTask<SaveImageInBackgroundData, Void, SaveImageInBackgroundData> {
    private static boolean mTickerAddSpace;
    private String mImageFileName;
    private String mImageFilePath;
    private int mImageHeight;
    private long mImageTime;
    private int mImageWidth;
    private NotificationManager mNotificationManager;
    public NotifyMediaStoreData mNotifyMediaStoreData;
    OutputStream mOutputStream;
    private File mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");

    SaveImageInBackgroundTask(Context context, SaveImageInBackgroundData saveImageInBackgroundData, NotificationManager notificationManager) {
        context.getResources();
        if (!this.mScreenshotDir.exists()) {
            this.mScreenshotDir.mkdirs();
        }
        long currentTimeMillis = System.currentTimeMillis();
        this.mImageTime = currentTimeMillis;
        boolean z = false;
        while (!z && this.mImageTime - currentTimeMillis < 200) {
            try {
                this.mImageTime = System.currentTimeMillis();
                this.mImageFileName = String.format("Screenshot_%s_%s.jpg", new Object[]{new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date(this.mImageTime)), Util.getTopActivityPkg(context, true)});
                this.mImageFilePath = String.format("%s/%s", new Object[]{this.mScreenshotDir, this.mImageFileName});
                this.mOutputStream = new FileOutputStream(this.mImageFilePath);
                try {
                    Log.e("GlobalScreenshot", "Create outputStream success, mImageFilePath = " + this.mImageFilePath);
                    z = true;
                } catch (Exception e) {
                    e = e;
                    z = true;
                    Log.e("GlobalScreenshot", "Create outputStream success, mImageFilePath = " + this.mImageFilePath);
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                e = e2;
                Log.e("GlobalScreenshot", "Create outputStream success, mImageFilePath = " + this.mImageFilePath);
                e.printStackTrace();
            }
        }
        this.mImageWidth = saveImageInBackgroundData.image.getWidth();
        this.mImageHeight = saveImageInBackgroundData.image.getHeight();
        mTickerAddSpace = !mTickerAddSpace;
        this.mNotificationManager = notificationManager;
        this.mNotifyMediaStoreData = new NotifyMediaStoreData();
        NotifyMediaStoreData notifyMediaStoreData = this.mNotifyMediaStoreData;
        notifyMediaStoreData.imageFilePath = this.mImageFilePath;
        notifyMediaStoreData.imageFileName = this.mImageFileName;
        notifyMediaStoreData.width = this.mImageWidth;
        notifyMediaStoreData.height = this.mImageHeight;
        notifyMediaStoreData.takenTime = this.mImageTime;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0123 A[LOOP:0: B:19:0x0121->B:20:0x0123, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0157  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.screenshot.SaveImageInBackgroundData doInBackground(com.android.systemui.screenshot.SaveImageInBackgroundData... r13) {
        /*
            r12 = this;
            java.lang.String r0 = ", mImageFilePath = "
            java.lang.String r1 = "GlobalScreenshot"
            int r2 = r13.length
            r3 = 0
            r4 = 1
            if (r2 == r4) goto L_0x000a
            return r3
        L_0x000a:
            r2 = 0
            r5 = r13[r2]
            boolean r5 = r5.orientationLandscape
            if (r5 != 0) goto L_0x001d
            r5 = r13[r2]
            r6 = r13[r2]
            com.android.systemui.screenshot.GlobalScreenshotDisplay r6 = r6.screenshotDisplay
            boolean r6 = r6.canLongScreenshot()
            r5.canLongScreenshot = r6
        L_0x001d:
            r5 = r13[r2]
            android.content.Context r5 = r5.context
            r6 = r13[r2]
            android.graphics.Bitmap r6 = r6.image
            android.graphics.Bitmap$CompressFormat r7 = android.graphics.Bitmap.CompressFormat.JPEG     // Catch:{ Exception -> 0x00fb }
            r8 = 100
            java.io.OutputStream r9 = r12.mOutputStream     // Catch:{ Exception -> 0x00fb }
            r6.compress(r7, r8, r9)     // Catch:{ Exception -> 0x00fb }
            java.io.OutputStream r6 = r12.mOutputStream     // Catch:{ Exception -> 0x00fb }
            r6.flush()     // Catch:{ Exception -> 0x00fb }
            java.io.OutputStream r6 = r12.mOutputStream     // Catch:{ Exception -> 0x00fb }
            r6.close()     // Catch:{ Exception -> 0x00fb }
            com.android.systemui.screenshot.NotifyMediaStoreData r6 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            long r6 = r6.takenTime     // Catch:{ Exception -> 0x00f8 }
            r8 = 1000(0x3e8, double:4.94E-321)
            long r6 = r6 / r8
            com.android.systemui.screenshot.NotifyMediaStoreData r8 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = r8.imageFileName     // Catch:{ Exception -> 0x00f8 }
            r9 = 46
            int r9 = r8.lastIndexOf(r9)     // Catch:{ Exception -> 0x00f8 }
            if (r9 < 0) goto L_0x004f
            java.lang.String r8 = r8.substring(r2, r9)     // Catch:{ Exception -> 0x00f8 }
        L_0x004f:
            android.content.ContentValues r9 = new android.content.ContentValues     // Catch:{ Exception -> 0x00f8 }
            r9.<init>()     // Catch:{ Exception -> 0x00f8 }
            android.content.ContentResolver r5 = r5.getContentResolver()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "_data"
            com.android.systemui.screenshot.NotifyMediaStoreData r11 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = r11.imageFilePath     // Catch:{ Exception -> 0x00f8 }
            r9.put(r10, r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "title"
            r9.put(r10, r8)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "_display_name"
            com.android.systemui.screenshot.NotifyMediaStoreData r10 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.imageFileName     // Catch:{ Exception -> 0x00f8 }
            r9.put(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "width"
            com.android.systemui.screenshot.NotifyMediaStoreData r10 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            int r10 = r10.width     // Catch:{ Exception -> 0x00f8 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ Exception -> 0x00f8 }
            r9.put(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "height"
            com.android.systemui.screenshot.NotifyMediaStoreData r10 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            int r10 = r10.height     // Catch:{ Exception -> 0x00f8 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ Exception -> 0x00f8 }
            r9.put(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "datetaken"
            com.android.systemui.screenshot.NotifyMediaStoreData r10 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            long r10 = r10.takenTime     // Catch:{ Exception -> 0x00f8 }
            java.lang.Long r10 = java.lang.Long.valueOf(r10)     // Catch:{ Exception -> 0x00f8 }
            r9.put(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "date_added"
            java.lang.Long r10 = java.lang.Long.valueOf(r6)     // Catch:{ Exception -> 0x00f8 }
            r9.put(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "date_modified"
            java.lang.Long r6 = java.lang.Long.valueOf(r6)     // Catch:{ Exception -> 0x00f8 }
            r9.put(r8, r6)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r6 = "mime_type"
            java.lang.String r7 = "image/jpeg"
            r9.put(r6, r7)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r6 = "_size"
            java.io.File r7 = new java.io.File     // Catch:{ Exception -> 0x00f8 }
            com.android.systemui.screenshot.NotifyMediaStoreData r8 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = r8.imageFilePath     // Catch:{ Exception -> 0x00f8 }
            r7.<init>(r8)     // Catch:{ Exception -> 0x00f8 }
            long r7 = r7.length()     // Catch:{ Exception -> 0x00f8 }
            java.lang.Long r7 = java.lang.Long.valueOf(r7)     // Catch:{ Exception -> 0x00f8 }
            r9.put(r6, r7)     // Catch:{ Exception -> 0x00f8 }
            com.android.systemui.screenshot.NotifyMediaStoreData r6 = r12.mNotifyMediaStoreData     // Catch:{ Exception -> 0x00f8 }
            android.net.Uri r7 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x00f8 }
            android.net.Uri r5 = r5.insert(r7, r9)     // Catch:{ Exception -> 0x00f8 }
            r6.outUri = r5     // Catch:{ Exception -> 0x00f8 }
            r5 = r13[r2]     // Catch:{ Exception -> 0x00f8 }
            r5.result = r2     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            r5.<init>()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r6 = "Save success, mScreenshotDir.getAbsolutePath() = "
            r5.append(r6)     // Catch:{ Exception -> 0x00f8 }
            java.io.File r6 = r12.mScreenshotDir     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r6 = r6.getAbsolutePath()     // Catch:{ Exception -> 0x00f8 }
            r5.append(r6)     // Catch:{ Exception -> 0x00f8 }
            r5.append(r0)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r6 = r12.mImageFilePath     // Catch:{ Exception -> 0x00f8 }
            r5.append(r6)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x00f8 }
            android.util.Log.e(r1, r5)     // Catch:{ Exception -> 0x00f8 }
            goto L_0x0168
        L_0x00f8:
            r5 = move-exception
            r6 = r4
            goto L_0x00fd
        L_0x00fb:
            r5 = move-exception
            r6 = r2
        L_0x00fd:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Save fail, mScreenshotDir.getAbsolutePath() = "
            r7.append(r8)
            java.io.File r8 = r12.mScreenshotDir
            java.lang.String r8 = r8.getAbsolutePath()
            r7.append(r8)
            r7.append(r0)
            java.lang.String r0 = r12.mImageFilePath
            r7.append(r0)
            java.lang.String r0 = r7.toString()
            android.util.Log.e(r1, r0)
            java.io.File r0 = r12.mScreenshotDir
        L_0x0121:
            if (r0 == 0) goto L_0x0151
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Save fail, path = "
            r7.append(r8)
            java.lang.String r8 = r0.getAbsolutePath()
            r7.append(r8)
            java.lang.String r8 = ", exists = "
            r7.append(r8)
            boolean r8 = r0.exists()
            r7.append(r8)
            java.lang.String r8 = "\n"
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            android.util.Log.e(r1, r7)
            java.io.File r0 = r0.getParentFile()
            goto L_0x0121
        L_0x0151:
            r0 = r13[r2]
            r0.result = r4
            if (r6 == 0) goto L_0x0161
            java.io.File r0 = new java.io.File
            java.lang.String r1 = r12.mImageFilePath
            r0.<init>(r1)
            r0.deleteOnExit()
        L_0x0161:
            com.android.systemui.screenshot.NotifyMediaStoreData r12 = r12.mNotifyMediaStoreData
            r12.outUri = r3
            r5.printStackTrace()
        L_0x0168:
            r12 = r13[r2]
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenshot.SaveImageInBackgroundTask.doInBackground(com.android.systemui.screenshot.SaveImageInBackgroundData[]):com.android.systemui.screenshot.SaveImageInBackgroundData");
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(SaveImageInBackgroundData saveImageInBackgroundData) {
        this.mNotifyMediaStoreData.saveFinished = true;
        saveImageInBackgroundData.screenLongShotView.setSelected(saveImageInBackgroundData.canLongScreenshot);
        NotifyMediaStoreData notifyMediaStoreData = this.mNotifyMediaStoreData;
        if (notifyMediaStoreData.isPending) {
            GlobalScreenshot.notifyMediaAndFinish(saveImageInBackgroundData.context, notifyMediaStoreData);
        }
        if (saveImageInBackgroundData.result > 0) {
            GlobalScreenshot.notifyScreenshotError(saveImageInBackgroundData.context, this.mNotificationManager, R.string.screenshot_failed_to_save_title);
        }
        Runnable runnable = saveImageInBackgroundData.finisher;
        if (runnable != null) {
            runnable.run();
        }
    }
}
