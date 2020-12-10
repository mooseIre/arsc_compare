package com.android.systemui.util.leak;

import android.app.NotificationChannelCompat;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.google.android.collect.Lists;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class LeakReporter {
    private final Context mContext;
    private final LeakDetector mLeakDetector;
    private final String mLeakReportEmail;

    public LeakReporter(Context context, LeakDetector leakDetector, String str) {
        this.mContext = context;
        this.mLeakDetector = leakDetector;
        this.mLeakReportEmail = str;
    }

    public void dumpLeak(int i) {
        Throwable th;
        try {
            File file = new File(this.mContext.getCacheDir(), "leak");
            file.mkdir();
            File file2 = new File(file, "leak.hprof");
            Debug.dumpHprofData(file2.getAbsolutePath());
            File file3 = new File(file, "leak.dump");
            FileOutputStream fileOutputStream = new FileOutputStream(file3);
            try {
                PrintWriter printWriter = new PrintWriter(fileOutputStream);
                printWriter.print("Build: ");
                printWriter.println(SystemProperties.get("ro.build.description"));
                printWriter.println();
                printWriter.flush();
                this.mLeakDetector.dump(fileOutputStream.getFD(), printWriter, new String[0]);
                printWriter.close();
                fileOutputStream.close();
                NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
                NotificationChannelCompat notificationChannelCompat = new NotificationChannelCompat("leak", "Leak Alerts", 4);
                notificationChannelCompat.enableVibration(true);
                NotificationChannelCompat.createNotificationChannel(notificationManager, notificationChannelCompat);
                notificationManager.notify("LeakReporter", 0, NotificationCompat.newBuilder(this.mContext, "leak").setAutoCancel(true).setShowWhen(true).setContentTitle("Memory Leak Detected").setContentText(String.format("SystemUI has detected %d leaked objects. Tap to send", new Object[]{Integer.valueOf(i)})).setSmallIcon(17303619).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, getIntent(file2, file3), 134217728, (Bundle) null, UserHandle.CURRENT)).build());
            } catch (Throwable th2) {
                Throwable th3 = th2;
                fileOutputStream.close();
                throw th3;
            }
        } catch (IOException e) {
            Log.e("LeakReporter", "Couldn't dump heap for leak", e);
        } catch (Throwable th4) {
            th.addSuppressed(th4);
        }
    }

    private Intent getIntent(File file, File file2) {
        Uri uriForFile = FileProvider.getUriForFile(this.mContext, "com.android.systemui.fileprovider", file2);
        Uri uriForFile2 = FileProvider.getUriForFile(this.mContext, "com.android.systemui.fileprovider", file);
        Intent intent = new Intent("android.intent.action.SEND_MULTIPLE");
        intent.addFlags(1);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("application/vnd.android.leakreport");
        intent.putExtra("android.intent.extra.SUBJECT", "SystemUI leak report");
        intent.putExtra("android.intent.extra.TEXT", "Build info: " + SystemProperties.get("ro.build.description"));
        ClipData clipData = new ClipData((CharSequence) null, new String[]{"application/vnd.android.leakreport"}, new ClipData.Item((CharSequence) null, (String) null, (Intent) null, uriForFile));
        ArrayList newArrayList = Lists.newArrayList(new Uri[]{uriForFile});
        clipData.addItem(new ClipData.Item((CharSequence) null, (String) null, (Intent) null, uriForFile2));
        newArrayList.add(uriForFile2);
        intent.setClipData(clipData);
        intent.putParcelableArrayListExtra("android.intent.extra.STREAM", newArrayList);
        String str = this.mLeakReportEmail;
        if (str != null) {
            intent.putExtra("android.intent.extra.EMAIL", new String[]{str});
        }
        return intent;
    }
}
