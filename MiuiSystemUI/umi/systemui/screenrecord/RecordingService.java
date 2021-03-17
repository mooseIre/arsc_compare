package com.android.systemui.screenrecord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.screenrecord.ScreenMediaRecorder;
import com.android.systemui.settings.CurrentUserContextTracker;
import java.io.IOException;
import java.util.concurrent.Executor;

public class RecordingService extends Service implements MediaRecorder.OnInfoListener {
    private ScreenRecordingAudioSource mAudioSource;
    private final RecordingController mController;
    private final Executor mLongExecutor;
    private final NotificationManager mNotificationManager;
    private boolean mOriginalShowTaps;
    private ScreenMediaRecorder mRecorder;
    private boolean mShowTaps;
    private final UiEventLogger mUiEventLogger;
    private final CurrentUserContextTracker mUserContextTracker;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public RecordingService(RecordingController recordingController, Executor executor, UiEventLogger uiEventLogger, NotificationManager notificationManager, CurrentUserContextTracker currentUserContextTracker) {
        this.mController = recordingController;
        this.mLongExecutor = executor;
        this.mUiEventLogger = uiEventLogger;
        this.mNotificationManager = notificationManager;
        this.mUserContextTracker = currentUserContextTracker;
    }

    public static Intent getStartIntent(Context context, int i, int i2, boolean z) {
        return new Intent(context, RecordingService.class).setAction("com.android.systemui.screenrecord.START").putExtra("extra_resultCode", i).putExtra("extra_useAudio", i2).putExtra("extra_showTaps", z);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int onStartCommand(android.content.Intent r11, int r12, int r13) {
        /*
            r10 = this;
            r12 = 2
            if (r11 != 0) goto L_0x0004
            return r12
        L_0x0004:
            java.lang.String r13 = r11.getAction()
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onStartCommand "
            r0.append(r1)
            r0.append(r13)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "RecordingService"
            android.util.Log.d(r1, r0)
            com.android.systemui.settings.CurrentUserContextTracker r0 = r10.mUserContextTracker
            android.content.Context r0 = r0.getCurrentUserContext()
            int r0 = r0.getUserId()
            android.os.UserHandle r2 = new android.os.UserHandle
            r2.<init>(r0)
            int r3 = r13.hashCode()
            java.lang.String r4 = "com.android.systemui.screenrecord.STOP_FROM_NOTIF"
            r5 = 4
            r6 = 3
            r7 = -1
            r8 = 0
            r9 = 1
            switch(r3) {
                case -1688140755: goto L_0x0062;
                case -1687783248: goto L_0x0058;
                case -1224647939: goto L_0x004e;
                case -470086188: goto L_0x0044;
                case -288359034: goto L_0x003c;
                default: goto L_0x003b;
            }
        L_0x003b:
            goto L_0x006c
        L_0x003c:
            boolean r3 = r13.equals(r4)
            if (r3 == 0) goto L_0x006c
            r3 = r9
            goto L_0x006d
        L_0x0044:
            java.lang.String r3 = "com.android.systemui.screenrecord.STOP"
            boolean r3 = r13.equals(r3)
            if (r3 == 0) goto L_0x006c
            r3 = r12
            goto L_0x006d
        L_0x004e:
            java.lang.String r3 = "com.android.systemui.screenrecord.DELETE"
            boolean r3 = r13.equals(r3)
            if (r3 == 0) goto L_0x006c
            r3 = r5
            goto L_0x006d
        L_0x0058:
            java.lang.String r3 = "com.android.systemui.screenrecord.START"
            boolean r3 = r13.equals(r3)
            if (r3 == 0) goto L_0x006c
            r3 = r8
            goto L_0x006d
        L_0x0062:
            java.lang.String r3 = "com.android.systemui.screenrecord.SHARE"
            boolean r3 = r13.equals(r3)
            if (r3 == 0) goto L_0x006c
            r3 = r6
            goto L_0x006d
        L_0x006c:
            r3 = r7
        L_0x006d:
            if (r3 == 0) goto L_0x0145
            if (r3 == r9) goto L_0x00fc
            if (r3 == r12) goto L_0x00fc
            r12 = 4273(0x10b1, float:5.988E-42)
            java.lang.String r13 = "android.intent.action.CLOSE_SYSTEM_DIALOGS"
            java.lang.String r0 = "extra_path"
            r4 = 0
            if (r3 == r6) goto L_0x00bb
            if (r3 == r5) goto L_0x0080
            goto L_0x019b
        L_0x0080:
            android.content.Intent r3 = new android.content.Intent
            r3.<init>(r13)
            r10.sendBroadcast(r3)
            android.content.ContentResolver r13 = r10.getContentResolver()
            java.lang.String r11 = r11.getStringExtra(r0)
            android.net.Uri r11 = android.net.Uri.parse(r11)
            r13.delete(r11, r4, r4)
            int r13 = com.android.systemui.C0021R$string.screenrecord_delete_description
            android.widget.Toast r13 = android.widget.Toast.makeText(r10, r13, r9)
            r13.show()
            android.app.NotificationManager r10 = r10.mNotificationManager
            r10.cancelAsUser(r4, r12, r2)
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r12 = "Deleted recording "
            r10.append(r12)
            r10.append(r11)
            java.lang.String r10 = r10.toString()
            android.util.Log.d(r1, r10)
            goto L_0x019b
        L_0x00bb:
            java.lang.String r11 = r11.getStringExtra(r0)
            android.net.Uri r11 = android.net.Uri.parse(r11)
            android.content.Intent r0 = new android.content.Intent
            java.lang.String r1 = "android.intent.action.SEND"
            r0.<init>(r1)
            java.lang.String r1 = "video/mp4"
            android.content.Intent r0 = r0.setType(r1)
            java.lang.String r1 = "android.intent.extra.STREAM"
            android.content.Intent r11 = r0.putExtra(r1, r11)
            android.content.res.Resources r0 = r10.getResources()
            int r1 = com.android.systemui.C0021R$string.screenrecord_share_label
            java.lang.String r0 = r0.getString(r1)
            android.content.Intent r1 = new android.content.Intent
            r1.<init>(r13)
            r10.sendBroadcast(r1)
            android.app.NotificationManager r13 = r10.mNotificationManager
            r13.cancelAsUser(r4, r12, r2)
            android.content.Intent r11 = android.content.Intent.createChooser(r11, r0)
            r12 = 268435456(0x10000000, float:2.5243549E-29)
            android.content.Intent r11 = r11.setFlags(r12)
            r10.startActivity(r11)
            goto L_0x019b
        L_0x00fc:
            boolean r12 = r4.equals(r13)
            if (r12 == 0) goto L_0x010a
            com.android.internal.logging.UiEventLogger r12 = r10.mUiEventLogger
            com.android.systemui.screenrecord.Events$ScreenRecordEvent r13 = com.android.systemui.screenrecord.Events$ScreenRecordEvent.SCREEN_RECORD_END_NOTIFICATION
            r12.log(r13)
            goto L_0x0111
        L_0x010a:
            com.android.internal.logging.UiEventLogger r12 = r10.mUiEventLogger
            com.android.systemui.screenrecord.Events$ScreenRecordEvent r13 = com.android.systemui.screenrecord.Events$ScreenRecordEvent.SCREEN_RECORD_END_QS_TILE
            r12.log(r13)
        L_0x0111:
            java.lang.String r12 = "android.intent.extra.user_handle"
            int r11 = r11.getIntExtra(r12, r7)
            if (r11 != r7) goto L_0x0123
            com.android.systemui.settings.CurrentUserContextTracker r11 = r10.mUserContextTracker
            android.content.Context r11 = r11.getCurrentUserContext()
            int r11 = r11.getUserId()
        L_0x0123:
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "notifying for user "
            r12.append(r13)
            r12.append(r11)
            java.lang.String r12 = r12.toString()
            android.util.Log.d(r1, r12)
            r10.stopRecording(r11)
            android.app.NotificationManager r11 = r10.mNotificationManager
            r12 = 4274(0x10b2, float:5.989E-42)
            r11.cancel(r12)
            r10.stopSelf()
            goto L_0x019b
        L_0x0145:
            com.android.systemui.screenrecord.ScreenRecordingAudioSource[] r12 = com.android.systemui.screenrecord.ScreenRecordingAudioSource.values()
            java.lang.String r13 = "extra_useAudio"
            int r13 = r11.getIntExtra(r13, r8)
            r12 = r12[r13]
            r10.mAudioSource = r12
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "recording with audio source"
            r12.append(r13)
            com.android.systemui.screenrecord.ScreenRecordingAudioSource r13 = r10.mAudioSource
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            android.util.Log.d(r1, r12)
            java.lang.String r12 = "extra_showTaps"
            boolean r11 = r11.getBooleanExtra(r12, r8)
            r10.mShowTaps = r11
            android.content.Context r11 = r10.getApplicationContext()
            android.content.ContentResolver r11 = r11.getContentResolver()
            java.lang.String r12 = "show_touches"
            int r11 = android.provider.Settings.System.getInt(r11, r12, r8)
            if (r11 == 0) goto L_0x0182
            r8 = r9
        L_0x0182:
            r10.mOriginalShowTaps = r8
            boolean r11 = r10.mShowTaps
            r10.setTapsVisible(r11)
            com.android.systemui.screenrecord.ScreenMediaRecorder r11 = new com.android.systemui.screenrecord.ScreenMediaRecorder
            com.android.systemui.settings.CurrentUserContextTracker r12 = r10.mUserContextTracker
            android.content.Context r12 = r12.getCurrentUserContext()
            com.android.systemui.screenrecord.ScreenRecordingAudioSource r13 = r10.mAudioSource
            r11.<init>(r12, r0, r13, r10)
            r10.mRecorder = r11
            r10.startRecording()
        L_0x019b:
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenrecord.RecordingService.onStartCommand(android.content.Intent, int, int):int");
    }

    public void onCreate() {
        super.onCreate();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ScreenMediaRecorder getRecorder() {
        return this.mRecorder;
    }

    private void startRecording() {
        try {
            getRecorder().start();
            this.mController.updateState(true);
            createRecordingNotification();
            this.mUiEventLogger.log(Events$ScreenRecordEvent.SCREEN_RECORD_START);
        } catch (RemoteException | IOException | IllegalStateException e) {
            Toast.makeText(this, C0021R$string.screenrecord_start_error, 1).show();
            e.printStackTrace();
            this.mController.updateState(false);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void createRecordingNotification() {
        String str;
        Resources resources = getResources();
        NotificationChannel notificationChannel = new NotificationChannel("screen_record", getString(C0021R$string.screenrecord_name), 3);
        notificationChannel.setDescription(getString(C0021R$string.screenrecord_channel_description));
        notificationChannel.enableVibration(true);
        this.mNotificationManager.createNotificationChannel(notificationChannel);
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", resources.getString(C0021R$string.screenrecord_name));
        if (this.mAudioSource == ScreenRecordingAudioSource.NONE) {
            str = resources.getString(C0021R$string.screenrecord_ongoing_screen_only);
        } else {
            str = resources.getString(C0021R$string.screenrecord_ongoing_screen_and_audio);
        }
        startForeground(4274, new Notification.Builder(this, "screen_record").setSmallIcon(C0013R$drawable.ic_screenrecord).setContentTitle(str).setContentText(getResources().getString(C0021R$string.screenrecord_stop_text)).setUsesChronometer(true).setColorized(true).setColor(getResources().getColor(C0011R$color.GM2_red_700)).setOngoing(true).setContentIntent(PendingIntent.getService(this, 2, getNotificationIntent(this), 201326592)).addExtras(bundle).build());
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Notification createProcessingNotification() {
        String str;
        Resources resources = getApplicationContext().getResources();
        if (this.mAudioSource == ScreenRecordingAudioSource.NONE) {
            str = resources.getString(C0021R$string.screenrecord_ongoing_screen_only);
        } else {
            str = resources.getString(C0021R$string.screenrecord_ongoing_screen_and_audio);
        }
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", resources.getString(C0021R$string.screenrecord_name));
        return new Notification.Builder(getApplicationContext(), "screen_record").setContentTitle(str).setContentText(getResources().getString(C0021R$string.screenrecord_background_processing_label)).setSmallIcon(C0013R$drawable.ic_screenrecord).addExtras(bundle).build();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Notification createSaveNotification(ScreenMediaRecorder.SavedRecording savedRecording) {
        Uri uri = savedRecording.getUri();
        Intent dataAndType = new Intent("android.intent.action.VIEW").setFlags(268435457).setDataAndType(uri, "video/mp4");
        Notification.Action build = new Notification.Action.Builder(Icon.createWithResource(this, C0013R$drawable.ic_screenrecord), getResources().getString(C0021R$string.screenrecord_share_label), PendingIntent.getService(this, 2, getShareIntent(this, uri.toString()), 201326592)).build();
        Notification.Action build2 = new Notification.Action.Builder(Icon.createWithResource(this, C0013R$drawable.ic_screenrecord), getResources().getString(C0021R$string.screenrecord_delete_label), PendingIntent.getService(this, 2, getDeleteIntent(this, uri.toString()), 201326592)).build();
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", getResources().getString(C0021R$string.screenrecord_name));
        Notification.Builder addExtras = new Notification.Builder(this, "screen_record").setSmallIcon(C0013R$drawable.ic_screenrecord).setContentTitle(getResources().getString(C0021R$string.screenrecord_save_message)).setContentIntent(PendingIntent.getActivity(this, 2, dataAndType, 67108864)).addAction(build).addAction(build2).setAutoCancel(true).addExtras(bundle);
        Bitmap thumbnail = savedRecording.getThumbnail();
        if (thumbnail != null) {
            addExtras.setLargeIcon(thumbnail).setStyle(new Notification.BigPictureStyle().bigPicture(thumbnail).bigLargeIcon((Bitmap) null));
        }
        return addExtras.build();
    }

    private void stopRecording(int i) {
        setTapsVisible(this.mOriginalShowTaps);
        if (getRecorder() != null) {
            getRecorder().end();
            saveRecording(i);
        } else {
            Log.e("RecordingService", "stopRecording called, but recorder was null");
        }
        this.mController.updateState(false);
    }

    private void saveRecording(int i) {
        UserHandle userHandle = new UserHandle(i);
        this.mNotificationManager.notifyAsUser((String) null, 4275, createProcessingNotification(), userHandle);
        this.mLongExecutor.execute(new Runnable(userHandle) {
            public final /* synthetic */ UserHandle f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                RecordingService.this.lambda$saveRecording$0$RecordingService(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$saveRecording$0 */
    public /* synthetic */ void lambda$saveRecording$0$RecordingService(UserHandle userHandle) {
        try {
            Log.d("RecordingService", "saving recording");
            Notification createSaveNotification = createSaveNotification(getRecorder().save());
            if (!this.mController.isRecording()) {
                this.mNotificationManager.notifyAsUser((String) null, 4273, createSaveNotification, userHandle);
            }
        } catch (IOException e) {
            Log.e("RecordingService", "Error saving screen recording: " + e.getMessage());
            Toast.makeText(this, C0021R$string.screenrecord_delete_error, 1).show();
        } catch (Throwable th) {
            this.mNotificationManager.cancelAsUser((String) null, 4275, userHandle);
            throw th;
        }
        this.mNotificationManager.cancelAsUser((String) null, 4275, userHandle);
    }

    private void setTapsVisible(boolean z) {
        Settings.System.putInt(getContentResolver(), "show_touches", z ? 1 : 0);
    }

    public static Intent getStopIntent(Context context) {
        return new Intent(context, RecordingService.class).setAction("com.android.systemui.screenrecord.STOP").putExtra("android.intent.extra.user_handle", context.getUserId());
    }

    protected static Intent getNotificationIntent(Context context) {
        return new Intent(context, RecordingService.class).setAction("com.android.systemui.screenrecord.STOP_FROM_NOTIF");
    }

    private static Intent getShareIntent(Context context, String str) {
        return new Intent(context, RecordingService.class).setAction("com.android.systemui.screenrecord.SHARE").putExtra("extra_path", str);
    }

    private static Intent getDeleteIntent(Context context, String str) {
        return new Intent(context, RecordingService.class).setAction("com.android.systemui.screenrecord.DELETE").putExtra("extra_path", str);
    }

    public void onInfo(MediaRecorder mediaRecorder, int i, int i2) {
        Log.d("RecordingService", "Media recorder info: " + i);
        onStartCommand(getStopIntent(this), 0, 0);
    }
}
