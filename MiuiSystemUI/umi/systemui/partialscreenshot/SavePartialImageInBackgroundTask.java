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
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0162  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.partialscreenshot.SavePartialImageInBackgroundData doInBackground(com.android.systemui.partialscreenshot.SavePartialImageInBackgroundData... r14) {
        /*
            r13 = this;
            java.lang.String r0 = ", mImageFilePath = "
            java.lang.String r1 = "PartialScreenshot"
            int r2 = android.os.Process.myTid()
            int r2 = android.os.Process.getThreadPriority(r2)
            r3 = -19
            android.os.Process.setThreadPriority(r3)
            int r3 = r14.length
            r4 = 0
            r5 = 1
            if (r3 == r5) goto L_0x0017
            return r4
        L_0x0017:
            r3 = 0
            r6 = r14[r3]
            android.content.Context r6 = r6.context
            r7 = r14[r3]
            android.graphics.Bitmap r7 = r7.image
            java.lang.String r8 = "Start Save"
            android.util.Log.d(r1, r8)     // Catch:{ Exception -> 0x0105 }
            android.graphics.Bitmap$CompressFormat r8 = android.graphics.Bitmap.CompressFormat.PNG     // Catch:{ Exception -> 0x0105 }
            r9 = 100
            java.io.OutputStream r10 = r13.mOutputStream     // Catch:{ Exception -> 0x0105 }
            r7.compress(r8, r9, r10)     // Catch:{ Exception -> 0x0105 }
            java.lang.String r7 = "Compress End"
            android.util.Log.d(r1, r7)     // Catch:{ Exception -> 0x0105 }
            java.io.OutputStream r7 = r13.mOutputStream     // Catch:{ Exception -> 0x0105 }
            r7.flush()     // Catch:{ Exception -> 0x0105 }
            java.io.OutputStream r7 = r13.mOutputStream     // Catch:{ Exception -> 0x0105 }
            r7.close()     // Catch:{ Exception -> 0x0105 }
            java.lang.String r7 = "End Save"
            android.util.Log.d(r1, r7)     // Catch:{ Exception -> 0x0105 }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r7 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            long r7 = r7.takenTime     // Catch:{ Exception -> 0x0102 }
            r9 = 1000(0x3e8, double:4.94E-321)
            long r7 = r7 / r9
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r9 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = r9.imageFileName     // Catch:{ Exception -> 0x0102 }
            r10 = 46
            int r10 = r9.lastIndexOf(r10)     // Catch:{ Exception -> 0x0102 }
            if (r10 < 0) goto L_0x0059
            java.lang.String r9 = r9.substring(r3, r10)     // Catch:{ Exception -> 0x0102 }
        L_0x0059:
            android.content.ContentValues r10 = new android.content.ContentValues     // Catch:{ Exception -> 0x0102 }
            r10.<init>()     // Catch:{ Exception -> 0x0102 }
            android.content.ContentResolver r6 = r6.getContentResolver()     // Catch:{ Exception -> 0x0102 }
            java.lang.String r11 = "_data"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r12 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            java.lang.String r12 = r12.imageFilePath     // Catch:{ Exception -> 0x0102 }
            r10.put(r11, r12)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r11 = "title"
            r10.put(r11, r9)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = "_display_name"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r11 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            java.lang.String r11 = r11.imageFileName     // Catch:{ Exception -> 0x0102 }
            r10.put(r9, r11)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = "width"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r11 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            int r11 = r11.width     // Catch:{ Exception -> 0x0102 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ Exception -> 0x0102 }
            r10.put(r9, r11)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = "height"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r11 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            int r11 = r11.height     // Catch:{ Exception -> 0x0102 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ Exception -> 0x0102 }
            r10.put(r9, r11)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = "datetaken"
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r11 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            long r11 = r11.takenTime     // Catch:{ Exception -> 0x0102 }
            java.lang.Long r11 = java.lang.Long.valueOf(r11)     // Catch:{ Exception -> 0x0102 }
            r10.put(r9, r11)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = "date_added"
            java.lang.Long r11 = java.lang.Long.valueOf(r7)     // Catch:{ Exception -> 0x0102 }
            r10.put(r9, r11)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = "date_modified"
            java.lang.Long r7 = java.lang.Long.valueOf(r7)     // Catch:{ Exception -> 0x0102 }
            r10.put(r9, r7)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r7 = "mime_type"
            java.lang.String r8 = "image/png"
            r10.put(r7, r8)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r7 = "_size"
            java.io.File r8 = new java.io.File     // Catch:{ Exception -> 0x0102 }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r9 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            java.lang.String r9 = r9.imageFilePath     // Catch:{ Exception -> 0x0102 }
            r8.<init>(r9)     // Catch:{ Exception -> 0x0102 }
            long r8 = r8.length()     // Catch:{ Exception -> 0x0102 }
            java.lang.Long r8 = java.lang.Long.valueOf(r8)     // Catch:{ Exception -> 0x0102 }
            r10.put(r7, r8)     // Catch:{ Exception -> 0x0102 }
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r7 = r13.mNotifyMediaStoreData     // Catch:{ Exception -> 0x0102 }
            android.net.Uri r8 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI     // Catch:{ Exception -> 0x0102 }
            android.net.Uri r6 = r6.insert(r8, r10)     // Catch:{ Exception -> 0x0102 }
            r7.outUri = r6     // Catch:{ Exception -> 0x0102 }
            r6 = r14[r3]     // Catch:{ Exception -> 0x0102 }
            r6.result = r3     // Catch:{ Exception -> 0x0102 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0102 }
            r6.<init>()     // Catch:{ Exception -> 0x0102 }
            java.lang.String r7 = "Save success, mScreenshotDir.getAbsolutePath() = "
            r6.append(r7)     // Catch:{ Exception -> 0x0102 }
            java.io.File r7 = r13.mScreenshotDir     // Catch:{ Exception -> 0x0102 }
            java.lang.String r7 = r7.getAbsolutePath()     // Catch:{ Exception -> 0x0102 }
            r6.append(r7)     // Catch:{ Exception -> 0x0102 }
            r6.append(r0)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r7 = r13.mImageFilePath     // Catch:{ Exception -> 0x0102 }
            r6.append(r7)     // Catch:{ Exception -> 0x0102 }
            java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x0102 }
            android.util.Log.d(r1, r6)     // Catch:{ Exception -> 0x0102 }
            goto L_0x0173
        L_0x0102:
            r6 = move-exception
            r7 = r5
            goto L_0x0107
        L_0x0105:
            r6 = move-exception
            r7 = r3
        L_0x0107:
            java.lang.String r8 = "sys.miui.screenshot.partial"
            java.lang.String r9 = "false"
            android.os.SystemProperties.set(r8, r9)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "Save fail, mScreenshotDir.getAbsolutePath() = "
            r8.append(r9)
            java.io.File r9 = r13.mScreenshotDir
            java.lang.String r9 = r9.getAbsolutePath()
            r8.append(r9)
            r8.append(r0)
            java.lang.String r0 = r13.mImageFilePath
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            android.util.Log.e(r1, r0)
            java.io.File r0 = r13.mScreenshotDir
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "Save fail, path = "
            r8.append(r9)
            java.lang.String r9 = r0.getAbsolutePath()
            r8.append(r9)
            java.lang.String r9 = ", exists = "
            r8.append(r9)
            boolean r0 = r0.exists()
            r8.append(r0)
            java.lang.String r0 = "\n"
            r8.append(r0)
            java.lang.String r0 = r8.toString()
            android.util.Log.e(r1, r0)
            r0 = r14[r3]
            r0.result = r5
            if (r7 == 0) goto L_0x016c
            java.io.File r0 = new java.io.File
            java.lang.String r1 = r13.mImageFilePath
            r0.<init>(r1)
            r0.deleteOnExit()
        L_0x016c:
            com.android.systemui.partialscreenshot.PartialNotifyMediaStoreData r13 = r13.mNotifyMediaStoreData
            r13.outUri = r4
            r6.printStackTrace()
        L_0x0173:
            android.os.Process.setThreadPriority(r2)
            r13 = r14[r3]
            return r13
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
