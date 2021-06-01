package com.android.systemui.pip.tv;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.util.NotificationChannels;

public class PipNotification {
    private static final boolean DEBUG = PipManager.DEBUG;
    private static final String NOTIFICATION_TAG = "PipNotification";
    private Bitmap mArt;
    private int mDefaultIconResId;
    private String mDefaultTitle;
    private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.pip.tv.PipNotification.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if (PipNotification.DEBUG) {
                Log.d(PipNotification.NOTIFICATION_TAG, "Received " + intent.getAction() + " from the notification UI");
            }
            String action = intent.getAction();
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -1402086132) {
                if (hashCode == 1201988555 && action.equals("PipNotification.menu")) {
                    c = 0;
                }
            } else if (action.equals("PipNotification.close")) {
                c = 1;
            }
            if (c == 0) {
                PipNotification.this.mPipManager.showPictureInPictureMenu();
            } else if (c == 1) {
                PipNotification.this.mPipManager.closePip();
            }
        }
    };
    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        /* class com.android.systemui.pip.tv.PipNotification.AnonymousClass2 */

        public void onPlaybackStateChanged(PlaybackState playbackState) {
            if (PipNotification.this.updateMediaControllerMetadata() && PipNotification.this.mNotified) {
                PipNotification.this.notifyPipNotification();
            }
        }
    };
    private String mMediaTitle;
    private final Notification.Builder mNotificationBuilder;
    private final NotificationManager mNotificationManager;
    private boolean mNotified;
    private final PackageManager mPackageManager;
    private String mPackageName;
    private PipManager.Listener mPipListener = new PipManager.Listener() {
        /* class com.android.systemui.pip.tv.PipNotification.AnonymousClass1 */

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipMenuActionsChanged(ParceledListSlice parceledListSlice) {
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipResizeAboutToStart() {
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onShowPipMenu() {
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipEntered(String str) {
            PipNotification.this.mPackageName = str;
            PipNotification.this.updateMediaControllerMetadata();
            PipNotification.this.notifyPipNotification();
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onPipActivityClosed() {
            PipNotification.this.dismissPipNotification();
            PipNotification.this.mPackageName = null;
        }

        @Override // com.android.systemui.pip.tv.PipManager.Listener
        public void onMoveToFullscreen() {
            PipNotification.this.dismissPipNotification();
            PipNotification.this.mPackageName = null;
        }
    };
    private final PipManager mPipManager;
    private final PipManager.MediaListener mPipMediaListener = new PipManager.MediaListener() {
        /* class com.android.systemui.pip.tv.PipNotification.AnonymousClass3 */

        @Override // com.android.systemui.pip.tv.PipManager.MediaListener
        public void onMediaControllerChanged() {
            MediaController mediaController = PipNotification.this.mPipManager.getMediaController();
            if (PipNotification.this.mMediaController != mediaController) {
                if (PipNotification.this.mMediaController != null) {
                    PipNotification.this.mMediaController.unregisterCallback(PipNotification.this.mMediaControllerCallback);
                }
                PipNotification.this.mMediaController = mediaController;
                if (PipNotification.this.mMediaController != null) {
                    PipNotification.this.mMediaController.registerCallback(PipNotification.this.mMediaControllerCallback);
                }
                if (PipNotification.this.updateMediaControllerMetadata() && PipNotification.this.mNotified) {
                    PipNotification.this.notifyPipNotification();
                }
            }
        }
    };

    public PipNotification(Context context, BroadcastDispatcher broadcastDispatcher, PipManager pipManager) {
        this.mPackageManager = context.getPackageManager();
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotificationBuilder = new Notification.Builder(context, NotificationChannels.TVPIP).setLocalOnly(true).setOngoing(DEBUG).setCategory("sys").extend(new Notification.TvExtender().setContentIntent(createPendingIntent(context, "PipNotification.menu")).setDeleteIntent(createPendingIntent(context, "PipNotification.close")));
        this.mPipManager = pipManager;
        pipManager.addListener(this.mPipListener);
        this.mPipManager.addMediaListener(this.mPipMediaListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PipNotification.menu");
        intentFilter.addAction("PipNotification.close");
        broadcastDispatcher.registerReceiver(this.mEventReceiver, intentFilter);
        onConfigurationChanged(context);
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged(Context context) {
        this.mDefaultTitle = context.getResources().getString(C0021R$string.pip_notification_unknown_title);
        this.mDefaultIconResId = C0013R$drawable.pip_icon;
        if (this.mNotified) {
            notifyPipNotification();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyPipNotification() {
        this.mNotified = true;
        this.mNotificationBuilder.setShowWhen(true).setWhen(System.currentTimeMillis()).setSmallIcon(this.mDefaultIconResId).setContentTitle(getNotificationTitle());
        if (this.mArt != null) {
            this.mNotificationBuilder.setStyle(new Notification.BigPictureStyle().bigPicture(this.mArt));
        } else {
            this.mNotificationBuilder.setStyle(null);
        }
        this.mNotificationManager.notify(NOTIFICATION_TAG, 1100, this.mNotificationBuilder.build());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissPipNotification() {
        this.mNotified = DEBUG;
        this.mNotificationManager.cancel(NOTIFICATION_TAG, 1100);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateMediaControllerMetadata() {
        Bitmap bitmap;
        MediaMetadata metadata;
        String str = null;
        if (this.mPipManager.getMediaController() == null || (metadata = this.mPipManager.getMediaController().getMetadata()) == null) {
            bitmap = null;
        } else {
            str = metadata.getString("android.media.metadata.DISPLAY_TITLE");
            if (TextUtils.isEmpty(str)) {
                str = metadata.getString("android.media.metadata.TITLE");
            }
            Bitmap bitmap2 = metadata.getBitmap("android.media.metadata.ALBUM_ART");
            bitmap = bitmap2 == null ? metadata.getBitmap("android.media.metadata.ART") : bitmap2;
        }
        if (TextUtils.equals(str, this.mMediaTitle) && bitmap == this.mArt) {
            return DEBUG;
        }
        this.mMediaTitle = str;
        this.mArt = bitmap;
        return true;
    }

    private String getNotificationTitle() {
        if (!TextUtils.isEmpty(this.mMediaTitle)) {
            return this.mMediaTitle;
        }
        String applicationLabel = getApplicationLabel(this.mPackageName);
        if (!TextUtils.isEmpty(applicationLabel)) {
            return applicationLabel;
        }
        return this.mDefaultTitle;
    }

    private String getApplicationLabel(String str) {
        try {
            return this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(str, 0)).toString();
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    private static PendingIntent createPendingIntent(Context context, String str) {
        return PendingIntent.getBroadcast(context, 0, new Intent(str), 268435456);
    }
}
