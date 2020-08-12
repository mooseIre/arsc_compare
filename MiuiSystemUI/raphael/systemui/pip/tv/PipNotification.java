package com.android.systemui.pip.tv;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ParceledListSlice;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.plugins.R;
import com.android.systemui.util.NotificationChannels;
import miui.view.MiuiHapticFeedbackConstants;

public class PipNotification {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = PipManager.DEBUG;
    private static final String NOTIFICATION_TAG = "com.android.systemui.pip.tv.PipNotification";
    private Bitmap mArt;
    private int mDefaultIconResId;
    private String mDefaultTitle;
    private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (PipNotification.DEBUG) {
                Log.d("PipNotification", "Received " + intent.getAction() + " from the notification UI");
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
    /* access modifiers changed from: private */
    public MediaController mMediaController;
    /* access modifiers changed from: private */
    public MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            if (PipNotification.this.updateMediaControllerMetadata() && PipNotification.this.mNotified) {
                PipNotification.this.notifyPipNotification();
            }
        }
    };
    private final Notification.Builder mNotificationBuilder;
    private final NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public boolean mNotified;
    private PipManager.Listener mPipListener = new PipManager.Listener() {
        public void onPipMenuActionsChanged(ParceledListSlice parceledListSlice) {
        }

        public void onPipResizeAboutToStart() {
        }

        public void onShowPipMenu() {
        }

        public void onPipEntered() {
            boolean unused = PipNotification.this.updateMediaControllerMetadata();
            PipNotification.this.notifyPipNotification();
        }

        public void onPipActivityClosed() {
            PipNotification.this.dismissPipNotification();
        }

        public void onMoveToFullscreen() {
            PipNotification.this.dismissPipNotification();
        }
    };
    /* access modifiers changed from: private */
    public final PipManager mPipManager = PipManager.getInstance();
    private final PipManager.MediaListener mPipMediaListener = new PipManager.MediaListener() {
        public void onMediaControllerChanged() {
            MediaController mediaController = PipNotification.this.mPipManager.getMediaController();
            if (PipNotification.this.mMediaController != mediaController) {
                if (PipNotification.this.mMediaController != null) {
                    PipNotification.this.mMediaController.unregisterCallback(PipNotification.this.mMediaControllerCallback);
                }
                MediaController unused = PipNotification.this.mMediaController = mediaController;
                if (PipNotification.this.mMediaController != null) {
                    PipNotification.this.mMediaController.registerCallback(PipNotification.this.mMediaControllerCallback);
                }
                if (PipNotification.this.updateMediaControllerMetadata() && PipNotification.this.mNotified) {
                    PipNotification.this.notifyPipNotification();
                }
            }
        }
    };
    private String mTitle;

    public PipNotification(Context context) {
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotificationBuilder = new Notification.Builder(context, NotificationChannels.TVPIP).setLocalOnly(true).setOngoing(false).setCategory("sys").extend(new Notification.TvExtender().setContentIntent(createPendingIntent(context, "PipNotification.menu")).setDeleteIntent(createPendingIntent(context, "PipNotification.close")));
        this.mPipManager.addListener(this.mPipListener);
        this.mPipManager.addMediaListener(this.mPipMediaListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PipNotification.menu");
        intentFilter.addAction("PipNotification.close");
        context.registerReceiver(this.mEventReceiver, intentFilter);
        onConfigurationChanged(context);
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged(Context context) {
        this.mDefaultTitle = context.getResources().getString(R.string.pip_notification_unknown_title);
        this.mDefaultIconResId = R.drawable.pip_icon;
        if (this.mNotified) {
            notifyPipNotification();
        }
    }

    /* access modifiers changed from: private */
    public void notifyPipNotification() {
        this.mNotified = true;
        this.mNotificationBuilder.setShowWhen(true).setWhen(System.currentTimeMillis()).setSmallIcon(this.mDefaultIconResId).setContentTitle(!TextUtils.isEmpty(this.mTitle) ? this.mTitle : this.mDefaultTitle);
        if (this.mArt != null) {
            this.mNotificationBuilder.setStyle(new Notification.BigPictureStyle().bigPicture(this.mArt));
        } else {
            this.mNotificationBuilder.setStyle((Notification.Style) null);
        }
        this.mNotificationManager.notify(NOTIFICATION_TAG, 1100, this.mNotificationBuilder.build());
    }

    /* access modifiers changed from: private */
    public void dismissPipNotification() {
        this.mNotified = false;
        this.mNotificationManager.cancel(NOTIFICATION_TAG, 1100);
    }

    /* access modifiers changed from: private */
    public boolean updateMediaControllerMetadata() {
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
        if (TextUtils.equals(str, this.mTitle) && bitmap == this.mArt) {
            return false;
        }
        this.mTitle = str;
        this.mArt = bitmap;
        return true;
    }

    private static PendingIntent createPendingIntent(Context context, String str) {
        return PendingIntent.getBroadcast(context, 0, new Intent(str), MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
    }
}
