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
    private File mScreenshotDir;

    SaveImageInBackgroundTask(Context context, SaveImageInBackgroundData saveImageInBackgroundData, NotificationManager notificationManager) {
        boolean z;
        Exception e;
        context.getResources();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
        this.mScreenshotDir = file;
        if (!file.exists()) {
            this.mScreenshotDir.mkdirs();
        }
        long currentTimeMillis = System.currentTimeMillis();
        this.mImageTime = currentTimeMillis;
        boolean z2 = false;
        while (!z2 && this.mImageTime - currentTimeMillis < 200) {
            try {
                this.mImageTime = System.currentTimeMillis();
                String format = String.format("Screenshot_%s_%s.jpg", new Object[]{new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date(this.mImageTime)), Util.getTopActivityPkg(context, true)});
                this.mImageFileName = format;
                this.mImageFilePath = String.format("%s/%s", new Object[]{this.mScreenshotDir, format});
                this.mOutputStream = new FileOutputStream(this.mImageFilePath);
                try {
                    Log.e("GlobalScreenshot", "Create outputStream success, mImageFilePath = " + this.mImageFilePath);
                    z2 = true;
                } catch (Exception e2) {
                    e = e2;
                    z = true;
                    Log.e("GlobalScreenshot", "Create outputStream fail, mImageFilePath = " + this.mImageFilePath);
                    e.printStackTrace();
                    z2 = z;
                }
            } catch (Exception e3) {
                Exception exc = e3;
                z = z2;
                e = exc;
                Log.e("GlobalScreenshot", "Create outputStream fail, mImageFilePath = " + this.mImageFilePath);
                e.printStackTrace();
                z2 = z;
            }
        }
        this.mImageWidth = saveImageInBackgroundData.image.getWidth();
        this.mImageHeight = saveImageInBackgroundData.image.getHeight();
        mTickerAddSpace = !mTickerAddSpace;
        this.mNotificationManager = notificationManager;
        NotifyMediaStoreData notifyMediaStoreData = new NotifyMediaStoreData();
        this.mNotifyMediaStoreData = notifyMediaStoreData;
        notifyMediaStoreData.imageFilePath = this.mImageFilePath;
        notifyMediaStoreData.imageFileName = this.mImageFileName;
        notifyMediaStoreData.width = this.mImageWidth;
        notifyMediaStoreData.height = this.mImageHeight;
        notifyMediaStoreData.takenTime = this.mImageTime;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x01ca A[LOOP:0: B:33:0x01c8->B:34:0x01ca, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x01fe  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.screenshot.SaveImageInBackgroundData doInBackground(com.android.systemui.screenshot.SaveImageInBackgroundData... r21) {
        /*
            r20 = this;
            r1 = r20
            r2 = r21
            java.lang.String r3 = ", mImageFilePath = "
            java.lang.String r4 = "GlobalScreenshot"
            int r0 = r2.length
            r5 = 0
            r6 = 1
            if (r0 == r6) goto L_0x000e
            return r5
        L_0x000e:
            r7 = 0
            r0 = r2[r7]
            boolean r0 = r0.orientationLandscape
            if (r0 != 0) goto L_0x0021
            r0 = r2[r7]
            r8 = r2[r7]
            com.android.systemui.screenshot.GlobalScreenshotDisplay r8 = r8.screenshotDisplay
            boolean r8 = r8.canLongScreenshot()
            r0.canLongScreenshot = r8
        L_0x0021:
            r0 = r2[r7]
            android.content.Context r0 = r0.context
            r8 = r2[r7]
            android.graphics.Bitmap r8 = r8.image
            android.graphics.Bitmap$CompressFormat r9 = android.graphics.Bitmap.CompressFormat.JPEG     // Catch:{ Exception -> 0x01a2 }
            r10 = 100
            java.io.OutputStream r11 = r1.mOutputStream     // Catch:{ Exception -> 0x01a2 }
            r8.compress(r9, r10, r11)     // Catch:{ Exception -> 0x01a2 }
            java.io.OutputStream r8 = r1.mOutputStream     // Catch:{ Exception -> 0x01a2 }
            r8.flush()     // Catch:{ Exception -> 0x01a2 }
            java.io.OutputStream r8 = r1.mOutputStream     // Catch:{ Exception -> 0x01a2 }
            r8.close()     // Catch:{ Exception -> 0x01a2 }
            com.android.systemui.screenshot.NotifyMediaStoreData r8 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            long r8 = r8.takenTime     // Catch:{ Exception -> 0x019f }
            r10 = 1000(0x3e8, double:4.94E-321)
            long r8 = r8 / r10
            com.android.systemui.screenshot.NotifyMediaStoreData r10 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = r10.imageFileName     // Catch:{ Exception -> 0x019f }
            r11 = 46
            int r11 = r10.lastIndexOf(r11)     // Catch:{ Exception -> 0x019f }
            if (r11 < 0) goto L_0x0053
            java.lang.String r10 = r10.substring(r7, r11)     // Catch:{ Exception -> 0x019f }
        L_0x0053:
            android.content.ContentValues r11 = new android.content.ContentValues     // Catch:{ Exception -> 0x019f }
            r11.<init>()     // Catch:{ Exception -> 0x019f }
            android.content.ContentResolver r12 = r0.getContentResolver()     // Catch:{ Exception -> 0x019f }
            java.lang.String r13 = "title"
            r11.put(r13, r10)     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = "width"
            com.android.systemui.screenshot.NotifyMediaStoreData r13 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            int r13 = r13.width     // Catch:{ Exception -> 0x019f }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r13)     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = "height"
            com.android.systemui.screenshot.NotifyMediaStoreData r13 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            int r13 = r13.height     // Catch:{ Exception -> 0x019f }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r13)     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = "datetaken"
            com.android.systemui.screenshot.NotifyMediaStoreData r13 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            long r13 = r13.takenTime     // Catch:{ Exception -> 0x019f }
            java.lang.Long r13 = java.lang.Long.valueOf(r13)     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r13)     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = "date_added"
            java.lang.Long r13 = java.lang.Long.valueOf(r8)     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r13)     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = "date_modified"
            java.lang.Long r8 = java.lang.Long.valueOf(r8)     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r8)     // Catch:{ Exception -> 0x019f }
            java.lang.String r8 = "mime_type"
            java.lang.String r9 = "image/jpeg"
            r11.put(r8, r9)     // Catch:{ Exception -> 0x019f }
            java.lang.String r8 = "_size"
            java.io.File r9 = new java.io.File     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r10 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r10 = r10.imageFilePath     // Catch:{ Exception -> 0x019f }
            r9.<init>(r10)     // Catch:{ Exception -> 0x019f }
            long r9 = r9.length()     // Catch:{ Exception -> 0x019f }
            java.lang.Long r9 = java.lang.Long.valueOf(r9)     // Catch:{ Exception -> 0x019f }
            r11.put(r8, r9)     // Catch:{ Exception -> 0x019f }
            int r8 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x019f }
            r9 = 30
            java.lang.String r10 = "_data"
            java.lang.String r13 = "_display_name"
            if (r8 < r9) goto L_0x0160
            java.lang.String r17 = "_display_name=?"
            java.lang.String[] r8 = new java.lang.String[r6]     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r9 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r9 = r9.imageFileName     // Catch:{ Exception -> 0x019f }
            r8[r7] = r9     // Catch:{ Exception -> 0x019f }
            android.content.ContentResolver r14 = r0.getContentResolver()     // Catch:{ Exception -> 0x019f }
            android.net.Uri r15 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x019f }
            r16 = 0
            r19 = 0
            r18 = r8
            android.database.Cursor r0 = r14.query(r15, r16, r17, r18, r19)     // Catch:{ Exception -> 0x019f }
            if (r0 == 0) goto L_0x0125
            boolean r8 = r0.moveToFirst()     // Catch:{ Exception -> 0x019f }
            if (r8 == 0) goto L_0x0125
            java.lang.String r8 = "_id"
            int r8 = r0.getColumnIndexOrThrow(r8)     // Catch:{ Exception -> 0x019f }
            long r8 = r0.getLong(r8)     // Catch:{ Exception -> 0x019f }
            r0.close()     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            android.net.Uri r10 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x019f }
            android.net.Uri r8 = android.content.ContentUris.withAppendedId(r10, r8)     // Catch:{ Exception -> 0x019f }
            r0.outUri = r8     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = "is_pending"
            java.lang.Integer r8 = java.lang.Integer.valueOf(r7)     // Catch:{ Exception -> 0x019f }
            r11.put(r0, r8)     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            android.net.Uri r0 = r0.outUri     // Catch:{ Exception -> 0x019f }
            int r0 = r12.update(r0, r11, r5, r5)     // Catch:{ Exception -> 0x019f }
            if (r0 == r6) goto L_0x0178
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x019f }
            r8.<init>()     // Catch:{ Exception -> 0x019f }
            java.lang.String r9 = "update media store abnormal : "
            r8.append(r9)     // Catch:{ Exception -> 0x019f }
            r8.append(r0)     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = r8.toString()     // Catch:{ Exception -> 0x019f }
            android.util.Log.d(r4, r0)     // Catch:{ Exception -> 0x019f }
            goto L_0x0178
        L_0x0125:
            java.lang.String r0 = "Query record is wrong, insert a new record"
            android.util.Log.d(r4, r0)     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = r0.imageFileName     // Catch:{ Exception -> 0x019f }
            r11.put(r13, r0)     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = r0.imageFilePath     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r0)     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = "relative_path"
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x019f }
            r8.<init>()     // Catch:{ Exception -> 0x019f }
            java.lang.String r9 = android.os.Environment.DIRECTORY_DCIM     // Catch:{ Exception -> 0x019f }
            r8.append(r9)     // Catch:{ Exception -> 0x019f }
            java.lang.String r9 = java.io.File.separator     // Catch:{ Exception -> 0x019f }
            r8.append(r9)     // Catch:{ Exception -> 0x019f }
            java.lang.String r9 = "Screenshots"
            r8.append(r9)     // Catch:{ Exception -> 0x019f }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x019f }
            r11.put(r0, r8)     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            android.net.Uri r8 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x019f }
            android.net.Uri r8 = r12.insert(r8, r11)     // Catch:{ Exception -> 0x019f }
            r0.outUri = r8     // Catch:{ Exception -> 0x019f }
            goto L_0x0178
        L_0x0160:
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = r0.imageFileName     // Catch:{ Exception -> 0x019f }
            r11.put(r13, r0)     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = r0.imageFilePath     // Catch:{ Exception -> 0x019f }
            r11.put(r10, r0)     // Catch:{ Exception -> 0x019f }
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x019f }
            android.net.Uri r8 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x019f }
            android.net.Uri r8 = r12.insert(r8, r11)     // Catch:{ Exception -> 0x019f }
            r0.outUri = r8     // Catch:{ Exception -> 0x019f }
        L_0x0178:
            r0 = r2[r7]     // Catch:{ Exception -> 0x019f }
            r0.result = r7     // Catch:{ Exception -> 0x019f }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x019f }
            r0.<init>()     // Catch:{ Exception -> 0x019f }
            java.lang.String r8 = "Save success, mScreenshotDir.getAbsolutePath() = "
            r0.append(r8)     // Catch:{ Exception -> 0x019f }
            java.io.File r8 = r1.mScreenshotDir     // Catch:{ Exception -> 0x019f }
            java.lang.String r8 = r8.getAbsolutePath()     // Catch:{ Exception -> 0x019f }
            r0.append(r8)     // Catch:{ Exception -> 0x019f }
            r0.append(r3)     // Catch:{ Exception -> 0x019f }
            java.lang.String r8 = r1.mImageFilePath     // Catch:{ Exception -> 0x019f }
            r0.append(r8)     // Catch:{ Exception -> 0x019f }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x019f }
            android.util.Log.e(r4, r0)     // Catch:{ Exception -> 0x019f }
            goto L_0x020f
        L_0x019f:
            r0 = move-exception
            r8 = r6
            goto L_0x01a4
        L_0x01a2:
            r0 = move-exception
            r8 = r7
        L_0x01a4:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "Save fail, mScreenshotDir.getAbsolutePath() = "
            r9.append(r10)
            java.io.File r10 = r1.mScreenshotDir
            java.lang.String r10 = r10.getAbsolutePath()
            r9.append(r10)
            r9.append(r3)
            java.lang.String r3 = r1.mImageFilePath
            r9.append(r3)
            java.lang.String r3 = r9.toString()
            android.util.Log.e(r4, r3)
            java.io.File r3 = r1.mScreenshotDir
        L_0x01c8:
            if (r3 == 0) goto L_0x01f8
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "Save fail, path = "
            r9.append(r10)
            java.lang.String r10 = r3.getAbsolutePath()
            r9.append(r10)
            java.lang.String r10 = ", exists = "
            r9.append(r10)
            boolean r10 = r3.exists()
            r9.append(r10)
            java.lang.String r10 = "\n"
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            android.util.Log.e(r4, r9)
            java.io.File r3 = r3.getParentFile()
            goto L_0x01c8
        L_0x01f8:
            r3 = r2[r7]
            r3.result = r6
            if (r8 == 0) goto L_0x0208
            java.io.File r3 = new java.io.File
            java.lang.String r4 = r1.mImageFilePath
            r3.<init>(r4)
            r3.deleteOnExit()
        L_0x0208:
            com.android.systemui.screenshot.NotifyMediaStoreData r1 = r1.mNotifyMediaStoreData
            r1.outUri = r5
            r0.printStackTrace()
        L_0x020f:
            r0 = r2[r7]
            return r0
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
