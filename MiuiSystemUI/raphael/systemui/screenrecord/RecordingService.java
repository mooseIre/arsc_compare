package com.android.systemui.screenrecord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
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

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public int onStartCommand(Intent intent, int i, int i2) {
        boolean z;
        if (intent == null) {
            return 2;
        }
        String action = intent.getAction();
        Log.d("RecordingService", "onStartCommand " + action);
        int userId = this.mUserContextTracker.getCurrentUserContext().getUserId();
        UserHandle userHandle = new UserHandle(userId);
        boolean z2 = false;
        switch (action.hashCode()) {
            case -1688140755:
                if (action.equals("com.android.systemui.screenrecord.SHARE")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -1687783248:
                if (action.equals("com.android.systemui.screenrecord.START")) {
                    z = false;
                    break;
                }
                z = true;
                break;
            case -1224647939:
                if (action.equals("com.android.systemui.screenrecord.DELETE")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -470086188:
                if (action.equals("com.android.systemui.screenrecord.STOP")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case -288359034:
                if (action.equals("com.android.systemui.screenrecord.STOP_FROM_NOTIF")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            this.mAudioSource = ScreenRecordingAudioSource.values()[intent.getIntExtra("extra_useAudio", 0)];
            Log.d("RecordingService", "recording with audio source" + this.mAudioSource);
            this.mShowTaps = intent.getBooleanExtra("extra_showTaps", false);
            if (Settings.System.getInt(getApplicationContext().getContentResolver(), "show_touches", 0) != 0) {
                z2 = true;
            }
            this.mOriginalShowTaps = z2;
            setTapsVisible(this.mShowTaps);
            this.mRecorder = new ScreenMediaRecorder(this.mUserContextTracker.getCurrentUserContext(), userId, this.mAudioSource, this);
            startRecording();
        } else if (z || z) {
            if ("com.android.systemui.screenrecord.STOP_FROM_NOTIF".equals(action)) {
                this.mUiEventLogger.log(Events$ScreenRecordEvent.SCREEN_RECORD_END_NOTIFICATION);
            } else {
                this.mUiEventLogger.log(Events$ScreenRecordEvent.SCREEN_RECORD_END_QS_TILE);
            }
            int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (intExtra == -1) {
                intExtra = this.mUserContextTracker.getCurrentUserContext().getUserId();
            }
            Log.d("RecordingService", "notifying for user " + intExtra);
            stopRecording(intExtra);
            this.mNotificationManager.cancel(4274);
            stopSelf();
        } else if (z) {
            Intent putExtra = new Intent("android.intent.action.SEND").setType("video/mp4").putExtra("android.intent.extra.STREAM", Uri.parse(intent.getStringExtra("extra_path")));
            String string = getResources().getString(C0021R$string.screenrecord_share_label);
            sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
            this.mNotificationManager.cancelAsUser(null, 4273, userHandle);
            startActivity(Intent.createChooser(putExtra, string).setFlags(268435456));
        } else if (z) {
            sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
            ContentResolver contentResolver = getContentResolver();
            Uri parse = Uri.parse(intent.getStringExtra("extra_path"));
            contentResolver.delete(parse, null, null);
            Toast.makeText(this, C0021R$string.screenrecord_delete_description, 1).show();
            this.mNotificationManager.cancelAsUser(null, 4273, userHandle);
            Log.d("RecordingService", "Deleted recording " + parse);
        }
        return 1;
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
        this.mNotificationManager.notifyAsUser(null, 4275, createProcessingNotification(), userHandle);
        this.mLongExecutor.execute(new Runnable(userHandle) {
            /* class com.android.systemui.screenrecord.$$Lambda$RecordingService$VCwgNNzpq2HdyNZyJT8KtYGVm5w */
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
                this.mNotificationManager.notifyAsUser(null, 4273, createSaveNotification, userHandle);
            }
        } catch (IOException e) {
            Log.e("RecordingService", "Error saving screen recording: " + e.getMessage());
            Toast.makeText(this, C0021R$string.screenrecord_delete_error, 1).show();
        } catch (Throwable th) {
            this.mNotificationManager.cancelAsUser(null, 4275, userHandle);
            throw th;
        }
        this.mNotificationManager.cancelAsUser(null, 4275, userHandle);
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
