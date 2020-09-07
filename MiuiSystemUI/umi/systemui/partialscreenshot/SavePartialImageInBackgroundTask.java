package com.android.systemui.partialscreenshot;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import com.android.systemui.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavePartialImageInBackgroundTask extends AsyncTask<SavePartialImageInBackgroundData, Void, SavePartialImageInBackgroundData> {
    private String mImageFileName;
    private String mImageFilePath;
    private int mImageHeight;
    private long mImageTime;
    private int mImageWidth;
    private NotificationManager mNotificationManager;
    public PartialNotifyMediaStoreData mNotifyMediaStoreData;
    private OutputStream mOutputStream;
    private File mScreenshotDir;

    SavePartialImageInBackgroundTask(Context context, SavePartialImageInBackgroundData savePartialImageInBackgroundData, NotificationManager notificationManager) {
        context.getResources();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
        this.mScreenshotDir = file;
        if (!file.exists()) {
            this.mScreenshotDir.mkdirs();
        }
        try {
            this.mImageTime = System.currentTimeMillis();
            String format = String.format("Screenshot_%s_%s.png", new Object[]{new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date(this.mImageTime)), Util.getTopActivityPkg(context, true)});
            this.mImageFileName = format;
            this.mImageFilePath = String.format("%s/%s", new Object[]{this.mScreenshotDir, format});
            this.mOutputStream = new FileOutputStream(this.mImageFilePath);
            Log.d("PartialScreenshot", "Create outputStream success,, mImageFilePath = " + this.mImageFilePath);
        } catch (Exception e) {
            SystemProperties.set("sys.miui.screenshot.partial", "false");
            Log.e("PartialScreenshot", "Create outputStream fail,, mImageFilePath = " + this.mImageFilePath);
            e.printStackTrace();
        }
        this.mImageWidth = savePartialImageInBackgroundData.image.getWidth();
        this.mImageHeight = savePartialImageInBackgroundData.image.getHeight();
        this.mNotificationManager = notificationManager;
        PartialNotifyMediaStoreData partialNotifyMediaStoreData = new PartialNotifyMediaStoreData();
        this.mNotifyMediaStoreData = partialNotifyMediaStoreData;
        partialNotifyMediaStoreData.imageFilePath = this.mImageFilePath;
        partialNotifyMediaStoreData.imageFileName = this.mImageFileName;
        partialNotifyMediaStoreData.width = this.mImageWidth;
        partialNotifyMediaStoreData.height = this.mImageHeight;
        partialNotifyMediaStoreData.takenTime = this.mImageTime;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x01ce  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.partialscreenshot.SavePartialImageInBackgroundData doInBackground(com.android.systemui.partialscreenshot.SavePartialImageInBackgroundData... r21) {
        /*
            r20 = this;
            r1 = r20
            r2 = r21
            java.lang.String r3 = ", mImageFilePath = "
            java.lang.String r4 = "PartialScreenshot"
            int r0 = android.os.Process.myTid()
            int r5 = android.os.Process.getThreadPriority(r0)
            r0 = -19
            android.os.Process.setThreadPriority(r0)
            int r0 = r2.length
            r6 = 0
            r7 = 1
            if (r0 == r7) goto L_0x001b
            return r6
        L_0x001b:
            r8 = 0
            r0 = r2[r8]
            android.content.Context r0 = r0.context
            r9 = r2[r8]
            android.graphics.Bitmap r9 = r9.image
            java.lang.String r10 = "Start Save"
            android.util.Log.d(r4, r10)     // Catch:{ Exception -> 0x0171 }
            android.graphics.Bitmap$CompressFormat r10 = android.graphics.Bitmap.CompressFormat.PNG     // Catch:{ Exception -> 0x0171 }
            r11 = 100
            java.io.OutputStream r12 = r1.mOutputStream     // Catch:{ Exception -> 0x0171 }
            r9.compress(r10, r11, r12)     // Catch:{ Exception -> 0x0171 }
            java.lang.String r9 = "Compress End"
            android.util.Log.d(r4, r9)     // Catch:{ Exception -> 0x0171 }
            java.io.OutputStream r9 = r1.mOutputStream     // Catch:{ Exception -> 0x0171 }
            r9.flush()     // Catch:{ Exception -> 0x0171 }
            java.io.OutputStream r9 = r1.mOutputStream     // Catch:{ Exception -> 0x0171 }
            r9.close()     // Catch:{ Exception -> 0x0171 }
            java.lang.String r9 = "End Save"
            android.util.Log.d(r4, r9)     // Catch:{ Exception -> 0x0171 }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r9 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            long r9 = r9.takenTime     // Catch:{ Exception -> 0x016e }
            r11 = 1000(0x3e8, double:4.94E-321)
            long r9 = r9 / r11
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r11 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = r11.imageFileName     // Catch:{ Exception -> 0x016e }
            r12 = 46
            int r12 = r11.lastIndexOf(r12)     // Catch:{ Exception -> 0x016e }
            if (r12 < 0) goto L_0x005d
            java.lang.String r11 = r11.substring(r8, r12)     // Catch:{ Exception -> 0x016e }
        L_0x005d:
            android.content.ContentValues r12 = new android.content.ContentValues     // Catch:{ Exception -> 0x016e }
            r12.<init>()     // Catch:{ Exception -> 0x016e }
            android.content.ContentResolver r13 = r0.getContentResolver()     // Catch:{ Exception -> 0x016e }
            java.lang.String r14 = "title"
            r12.put(r14, r11)     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = "width"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r14 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            int r14 = r14.width     // Catch:{ Exception -> 0x016e }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ Exception -> 0x016e }
            r12.put(r11, r14)     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = "height"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r14 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            int r14 = r14.height     // Catch:{ Exception -> 0x016e }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ Exception -> 0x016e }
            r12.put(r11, r14)     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = "datetaken"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r14 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            long r14 = r14.takenTime     // Catch:{ Exception -> 0x016e }
            java.lang.Long r14 = java.lang.Long.valueOf(r14)     // Catch:{ Exception -> 0x016e }
            r12.put(r11, r14)     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = "date_added"
            java.lang.Long r14 = java.lang.Long.valueOf(r9)     // Catch:{ Exception -> 0x016e }
            r12.put(r11, r14)     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = "date_modified"
            java.lang.Long r9 = java.lang.Long.valueOf(r9)     // Catch:{ Exception -> 0x016e }
            r12.put(r11, r9)     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = "mime_type"
            java.lang.String r10 = "image/png"
            r12.put(r9, r10)     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = "_size"
            java.io.File r10 = new java.io.File     // Catch:{ Exception -> 0x016e }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r11 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            java.lang.String r11 = r11.imageFilePath     // Catch:{ Exception -> 0x016e }
            r10.<init>(r11)     // Catch:{ Exception -> 0x016e }
            long r10 = r10.length()     // Catch:{ Exception -> 0x016e }
            java.lang.Long r10 = java.lang.Long.valueOf(r10)     // Catch:{ Exception -> 0x016e }
            r12.put(r9, r10)     // Catch:{ Exception -> 0x016e }
            int r9 = android.os.Build.VERSION.SDK_INT     // Catch:{ Exception -> 0x016e }
            r10 = 30
            if (r9 < r10) goto L_0x012b
            java.lang.String r17 = "_display_name=?"
            java.lang.String[] r9 = new java.lang.String[r7]     // Catch:{ Exception -> 0x016e }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r10 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            java.lang.String r10 = r10.imageFileName     // Catch:{ Exception -> 0x016e }
            r9[r8] = r10     // Catch:{ Exception -> 0x016e }
            android.content.ContentResolver r14 = r0.getContentResolver()     // Catch:{ Exception -> 0x016e }
            android.net.Uri r15 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x016e }
            r16 = 0
            r19 = 0
            r18 = r9
            android.database.Cursor r0 = r14.query(r15, r16, r17, r18, r19)     // Catch:{ Exception -> 0x016e }
            if (r0 == 0) goto L_0x0147
            boolean r9 = r0.moveToFirst()     // Catch:{ Exception -> 0x016e }
            if (r9 == 0) goto L_0x0147
            java.lang.String r9 = "_id"
            int r9 = r0.getColumnIndexOrThrow(r9)     // Catch:{ Exception -> 0x016e }
            long r9 = r0.getLong(r9)     // Catch:{ Exception -> 0x016e }
            r0.close()     // Catch:{ Exception -> 0x016e }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            android.net.Uri r11 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x016e }
            android.net.Uri r9 = android.content.ContentUris.withAppendedId(r11, r9)     // Catch:{ Exception -> 0x016e }
            r0.outUri = r9     // Catch:{ Exception -> 0x016e }
            java.lang.String r0 = "is_pending"
            java.lang.Integer r9 = java.lang.Integer.valueOf(r8)     // Catch:{ Exception -> 0x016e }
            r12.put(r0, r9)     // Catch:{ Exception -> 0x016e }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            android.net.Uri r0 = r0.outUri     // Catch:{ Exception -> 0x016e }
            int r0 = r13.update(r0, r12, r6, r6)     // Catch:{ Exception -> 0x016e }
            if (r0 == r7) goto L_0x0147
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x016e }
            r9.<init>()     // Catch:{ Exception -> 0x016e }
            java.lang.String r10 = "update media store abnormal : "
            r9.append(r10)     // Catch:{ Exception -> 0x016e }
            r9.append(r0)     // Catch:{ Exception -> 0x016e }
            java.lang.String r0 = r9.toString()     // Catch:{ Exception -> 0x016e }
            android.util.Log.d(r4, r0)     // Catch:{ Exception -> 0x016e }
            goto L_0x0147
        L_0x012b:
            java.lang.String r0 = "_display_name"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r9 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = r9.imageFileName     // Catch:{ Exception -> 0x016e }
            r12.put(r0, r9)     // Catch:{ Exception -> 0x016e }
            java.lang.String r0 = "_data"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r9 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = r9.imageFilePath     // Catch:{ Exception -> 0x016e }
            r12.put(r0, r9)     // Catch:{ Exception -> 0x016e }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r0 = r1.mNotifyMediaStoreData     // Catch:{ Exception -> 0x016e }
            android.net.Uri r9 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x016e }
            android.net.Uri r9 = r13.insert(r9, r12)     // Catch:{ Exception -> 0x016e }
            r0.outUri = r9     // Catch:{ Exception -> 0x016e }
        L_0x0147:
            r0 = r2[r8]     // Catch:{ Exception -> 0x016e }
            r0.result = r8     // Catch:{ Exception -> 0x016e }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x016e }
            r0.<init>()     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = "Save success, mScreenshotDir.getAbsolutePath() = "
            r0.append(r9)     // Catch:{ Exception -> 0x016e }
            java.io.File r9 = r1.mScreenshotDir     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = r9.getAbsolutePath()     // Catch:{ Exception -> 0x016e }
            r0.append(r9)     // Catch:{ Exception -> 0x016e }
            r0.append(r3)     // Catch:{ Exception -> 0x016e }
            java.lang.String r9 = r1.mImageFilePath     // Catch:{ Exception -> 0x016e }
            r0.append(r9)     // Catch:{ Exception -> 0x016e }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x016e }
            android.util.Log.d(r4, r0)     // Catch:{ Exception -> 0x016e }
            goto L_0x01df
        L_0x016e:
            r0 = move-exception
            r9 = r7
            goto L_0x0173
        L_0x0171:
            r0 = move-exception
            r9 = r8
        L_0x0173:
            java.lang.String r10 = "sys.miui.screenshot.partial"
            java.lang.String r11 = "false"
            android.os.SystemProperties.set(r10, r11)
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "Save fail, mScreenshotDir.getAbsolutePath() = "
            r10.append(r11)
            java.io.File r11 = r1.mScreenshotDir
            java.lang.String r11 = r11.getAbsolutePath()
            r10.append(r11)
            r10.append(r3)
            java.lang.String r3 = r1.mImageFilePath
            r10.append(r3)
            java.lang.String r3 = r10.toString()
            android.util.Log.e(r4, r3)
            java.io.File r3 = r1.mScreenshotDir
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "Save fail, path = "
            r10.append(r11)
            java.lang.String r11 = r3.getAbsolutePath()
            r10.append(r11)
            java.lang.String r11 = ", exists = "
            r10.append(r11)
            boolean r3 = r3.exists()
            r10.append(r3)
            java.lang.String r3 = "\n"
            r10.append(r3)
            java.lang.String r3 = r10.toString()
            android.util.Log.e(r4, r3)
            r3 = r2[r8]
            r3.result = r7
            if (r9 == 0) goto L_0x01d8
            java.io.File r3 = new java.io.File
            java.lang.String r4 = r1.mImageFilePath
            r3.<init>(r4)
            r3.deleteOnExit()
        L_0x01d8:
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r1 = r1.mNotifyMediaStoreData
            r1.outUri = r6
            r0.printStackTrace()
        L_0x01df:
            android.os.Process.setThreadPriority(r5)
            r0 = r2[r8]
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.partialscreenshot.SavePartialImageInBackgroundTask.doInBackground(com.android.systemui.partialscreenshot.SavePartialImageInBackgroundData[]):com.android.systemui.partialscreenshot.SavePartialImageInBackgroundData");
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(SavePartialImageInBackgroundData savePartialImageInBackgroundData) {
        PartialNotifyMediaStoreData partialNotifyMediaStoreData = this.mNotifyMediaStoreData;
        partialNotifyMediaStoreData.saveFinished = true;
        if (partialNotifyMediaStoreData.isPending) {
            PartialScreenshot.notifyPartialMediaAndFinish(savePartialImageInBackgroundData.context, partialNotifyMediaStoreData);
        }
        if (savePartialImageInBackgroundData.result > 0) {
            PartialScreenshot.notifyPartialScreenshotError(savePartialImageInBackgroundData.context, this.mNotificationManager, R$string.screenshot_failed_to_save_title);
        }
        Runnable runnable = savePartialImageInBackgroundData.finisher;
        if (runnable != null) {
            runnable.run();
        }
    }
}
