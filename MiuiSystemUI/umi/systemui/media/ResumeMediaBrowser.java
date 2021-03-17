package com.android.systemui.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;

public class ResumeMediaBrowser {
    private final Callback mCallback;
    private ComponentName mComponentName;
    private final MediaBrowser.ConnectionCallback mConnectionCallback = new MediaBrowser.ConnectionCallback() {
        /* class com.android.systemui.media.ResumeMediaBrowser.AnonymousClass2 */

        public void onConnected() {
            Log.d("ResumeMediaBrowser", "Service connected for " + ResumeMediaBrowser.this.mComponentName);
            if (ResumeMediaBrowser.this.mMediaBrowser != null && ResumeMediaBrowser.this.mMediaBrowser.isConnected()) {
                String root = ResumeMediaBrowser.this.mMediaBrowser.getRoot();
                if (!TextUtils.isEmpty(root)) {
                    ResumeMediaBrowser.this.mCallback.onConnected();
                    ResumeMediaBrowser.this.mMediaBrowser.subscribe(root, ResumeMediaBrowser.this.mSubscriptionCallback);
                    return;
                }
            }
            ResumeMediaBrowser.this.mCallback.onError();
        }

        public void onConnectionSuspended() {
            Log.d("ResumeMediaBrowser", "Connection suspended for " + ResumeMediaBrowser.this.mComponentName);
            ResumeMediaBrowser.this.mCallback.onError();
            ResumeMediaBrowser.this.disconnect();
        }

        public void onConnectionFailed() {
            Log.d("ResumeMediaBrowser", "Connection failed for " + ResumeMediaBrowser.this.mComponentName);
            ResumeMediaBrowser.this.mCallback.onError();
            ResumeMediaBrowser.this.disconnect();
        }
    };
    private final Context mContext;
    private MediaBrowser mMediaBrowser;
    private final MediaBrowser.SubscriptionCallback mSubscriptionCallback = new MediaBrowser.SubscriptionCallback() {
        /* class com.android.systemui.media.ResumeMediaBrowser.AnonymousClass1 */

        @Override // android.media.browse.MediaBrowser.SubscriptionCallback
        public void onChildrenLoaded(String str, List<MediaBrowser.MediaItem> list) {
            if (list.size() == 0) {
                Log.d("ResumeMediaBrowser", "No children found for " + ResumeMediaBrowser.this.mComponentName);
                return;
            }
            MediaBrowser.MediaItem mediaItem = list.get(0);
            MediaDescription description = mediaItem.getDescription();
            if (!mediaItem.isPlayable() || ResumeMediaBrowser.this.mMediaBrowser == null) {
                Log.d("ResumeMediaBrowser", "Child found but not playable for " + ResumeMediaBrowser.this.mComponentName);
            } else {
                ResumeMediaBrowser.this.mCallback.addTrack(description, ResumeMediaBrowser.this.mMediaBrowser.getServiceComponent(), ResumeMediaBrowser.this);
            }
            ResumeMediaBrowser.this.disconnect();
        }

        public void onError(String str) {
            Log.d("ResumeMediaBrowser", "Subscribe error for " + ResumeMediaBrowser.this.mComponentName + ": " + str);
            ResumeMediaBrowser.this.mCallback.onError();
            ResumeMediaBrowser.this.disconnect();
        }

        public void onError(String str, Bundle bundle) {
            Log.d("ResumeMediaBrowser", "Subscribe error for " + ResumeMediaBrowser.this.mComponentName + ": " + str + ", options: " + bundle);
            ResumeMediaBrowser.this.mCallback.onError();
            ResumeMediaBrowser.this.disconnect();
        }
    };

    public static class Callback {
        public void addTrack(MediaDescription mediaDescription, ComponentName componentName, ResumeMediaBrowser resumeMediaBrowser) {
        }

        public void onConnected() {
        }

        public void onError() {
        }
    }

    public ResumeMediaBrowser(Context context, Callback callback, ComponentName componentName) {
        this.mContext = context;
        this.mCallback = callback;
        this.mComponentName = componentName;
    }

    public void findRecentMedia() {
        Log.d("ResumeMediaBrowser", "Connecting to " + this.mComponentName);
        disconnect();
        Bundle bundle = new Bundle();
        bundle.putBoolean("android.service.media.extra.RECENT", true);
        MediaBrowser mediaBrowser = new MediaBrowser(this.mContext, this.mComponentName, this.mConnectionCallback, bundle);
        this.mMediaBrowser = mediaBrowser;
        mediaBrowser.connect();
    }

    public void disconnect() {
        MediaBrowser mediaBrowser = this.mMediaBrowser;
        if (mediaBrowser != null) {
            mediaBrowser.disconnect();
        }
        this.mMediaBrowser = null;
    }

    public void restart() {
        disconnect();
        Bundle bundle = new Bundle();
        bundle.putBoolean("android.service.media.extra.RECENT", true);
        MediaBrowser mediaBrowser = new MediaBrowser(this.mContext, this.mComponentName, new MediaBrowser.ConnectionCallback() {
            /* class com.android.systemui.media.ResumeMediaBrowser.AnonymousClass3 */

            public void onConnected() {
                Log.d("ResumeMediaBrowser", "Connected for restart " + ResumeMediaBrowser.this.mMediaBrowser.isConnected());
                if (ResumeMediaBrowser.this.mMediaBrowser == null || !ResumeMediaBrowser.this.mMediaBrowser.isConnected()) {
                    ResumeMediaBrowser.this.mCallback.onError();
                    return;
                }
                MediaController mediaController = new MediaController(ResumeMediaBrowser.this.mContext, ResumeMediaBrowser.this.mMediaBrowser.getSessionToken());
                mediaController.getTransportControls();
                mediaController.getTransportControls().prepare();
                mediaController.getTransportControls().play();
                ResumeMediaBrowser.this.mCallback.onConnected();
            }

            public void onConnectionFailed() {
                ResumeMediaBrowser.this.mCallback.onError();
            }

            public void onConnectionSuspended() {
                ResumeMediaBrowser.this.mCallback.onError();
            }
        }, bundle);
        this.mMediaBrowser = mediaBrowser;
        mediaBrowser.connect();
    }

    public MediaSession.Token getToken() {
        MediaBrowser mediaBrowser = this.mMediaBrowser;
        if (mediaBrowser == null || !mediaBrowser.isConnected()) {
            return null;
        }
        return this.mMediaBrowser.getSessionToken();
    }

    public PendingIntent getAppIntent() {
        return PendingIntent.getActivity(this.mContext, 0, this.mContext.getPackageManager().getLaunchIntentForPackage(this.mComponentName.getPackageName()), 0);
    }

    public void testConnection() {
        disconnect();
        AnonymousClass4 r0 = new MediaBrowser.ConnectionCallback() {
            /* class com.android.systemui.media.ResumeMediaBrowser.AnonymousClass4 */

            public void onConnected() {
                Log.d("ResumeMediaBrowser", "connected");
                if (ResumeMediaBrowser.this.mMediaBrowser == null || !ResumeMediaBrowser.this.mMediaBrowser.isConnected() || TextUtils.isEmpty(ResumeMediaBrowser.this.mMediaBrowser.getRoot())) {
                    ResumeMediaBrowser.this.mCallback.onError();
                } else {
                    ResumeMediaBrowser.this.mCallback.onConnected();
                }
            }

            public void onConnectionSuspended() {
                Log.d("ResumeMediaBrowser", "suspended");
                ResumeMediaBrowser.this.mCallback.onError();
            }

            public void onConnectionFailed() {
                Log.d("ResumeMediaBrowser", "failed");
                ResumeMediaBrowser.this.mCallback.onError();
            }
        };
        Bundle bundle = new Bundle();
        bundle.putBoolean("android.service.media.extra.RECENT", true);
        MediaBrowser mediaBrowser = new MediaBrowser(this.mContext, this.mComponentName, r0, bundle);
        this.mMediaBrowser = mediaBrowser;
        mediaBrowser.connect();
    }
}
