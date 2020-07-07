package com.android.systemui.pip.phone;

import android.app.IActivityManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PipMediaController {
    private final IActivityManager mActivityManager;
    private final Context mContext;
    private ArrayList<ActionListener> mListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public MediaController mMediaController;
    private final MediaSessionManager mMediaSessionManager;
    private RemoteAction mNextAction;
    private RemoteAction mPauseAction;
    private RemoteAction mPlayAction;
    private BroadcastReceiver mPlayPauseActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.android.systemui.pip.phone.PLAY")) {
                PipMediaController.this.mMediaController.getTransportControls().play();
            } else if (action.equals("com.android.systemui.pip.phone.PAUSE")) {
                PipMediaController.this.mMediaController.getTransportControls().pause();
            } else if (action.equals("com.android.systemui.pip.phone.NEXT")) {
                PipMediaController.this.mMediaController.getTransportControls().skipToNext();
            } else if (action.equals("com.android.systemui.pip.phone.PREV")) {
                PipMediaController.this.mMediaController.getTransportControls().skipToPrevious();
            }
        }
    };
    private MediaController.Callback mPlaybackChangedListener = new MediaController.Callback() {
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            PipMediaController.this.notifyActionsChanged();
        }
    };
    private RemoteAction mPrevAction;

    public interface ActionListener {
        void onMediaActionsChanged(List<RemoteAction> list);
    }

    public PipMediaController(Context context, IActivityManager iActivityManager) {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.pip.phone.PLAY");
        intentFilter.addAction("com.android.systemui.pip.phone.PAUSE");
        intentFilter.addAction("com.android.systemui.pip.phone.NEXT");
        intentFilter.addAction("com.android.systemui.pip.phone.PREV");
        this.mContext.registerReceiver(this.mPlayPauseActionReceiver, intentFilter);
        createMediaActions();
        this.mMediaSessionManager = (MediaSessionManager) context.getSystemService("media_session");
        this.mMediaSessionManager.addOnActiveSessionsChangedListener(new MediaSessionManager.OnActiveSessionsChangedListener() {
            public final void onActiveSessionsChanged(List list) {
                PipMediaController.this.lambda$new$0$PipMediaController(list);
            }
        }, (ComponentName) null);
    }

    public void onActivityPinned() {
        lambda$new$0$PipMediaController(this.mMediaSessionManager.getActiveSessions((ComponentName) null));
    }

    public void addListener(ActionListener actionListener) {
        if (!this.mListeners.contains(actionListener)) {
            this.mListeners.add(actionListener);
            actionListener.onMediaActionsChanged(getMediaActions());
        }
    }

    public void removeListener(ActionListener actionListener) {
        actionListener.onMediaActionsChanged(Collections.EMPTY_LIST);
        this.mListeners.remove(actionListener);
    }

    private List<RemoteAction> getMediaActions() {
        MediaController mediaController = this.mMediaController;
        if (mediaController == null || mediaController.getPlaybackState() == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList arrayList = new ArrayList();
        boolean isActiveState = MediaSession.isActiveState(this.mMediaController.getPlaybackState().getState());
        long actions = this.mMediaController.getPlaybackState().getActions();
        boolean z = true;
        this.mPrevAction.setEnabled((16 & actions) != 0);
        arrayList.add(this.mPrevAction);
        if (!isActiveState && (4 & actions) != 0) {
            arrayList.add(this.mPlayAction);
        } else if (isActiveState && (2 & actions) != 0) {
            arrayList.add(this.mPauseAction);
        }
        RemoteAction remoteAction = this.mNextAction;
        if ((actions & 32) == 0) {
            z = false;
        }
        remoteAction.setEnabled(z);
        arrayList.add(this.mNextAction);
        return arrayList;
    }

    private void createMediaActions() {
        String string = this.mContext.getString(R.string.pip_pause);
        this.mPauseAction = new RemoteAction(Icon.createWithResource(this.mContext, R.drawable.ic_pause_white), string, string, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.PAUSE"), 134217728));
        String string2 = this.mContext.getString(R.string.pip_play);
        this.mPlayAction = new RemoteAction(Icon.createWithResource(this.mContext, R.drawable.ic_play_arrow_white), string2, string2, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.PLAY"), 134217728));
        String string3 = this.mContext.getString(R.string.pip_skip_to_next);
        this.mNextAction = new RemoteAction(Icon.createWithResource(this.mContext, R.drawable.ic_skip_next_white), string3, string3, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.NEXT"), 134217728));
        String string4 = this.mContext.getString(R.string.pip_skip_to_prev);
        this.mPrevAction = new RemoteAction(Icon.createWithResource(this.mContext, R.drawable.ic_skip_previous_white), string4, string4, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.PREV"), 134217728));
    }

    /* access modifiers changed from: private */
    /* renamed from: resolveActiveMediaController */
    public void lambda$new$0$PipMediaController(List<MediaController> list) {
        ComponentName topPinnedActivity;
        if (!(list == null || (topPinnedActivity = PipUtils.getTopPinnedActivity(this.mContext, this.mActivityManager)) == null)) {
            for (int i = 0; i < list.size(); i++) {
                MediaController mediaController = list.get(i);
                if (mediaController.getPackageName().equals(topPinnedActivity.getPackageName())) {
                    setActiveMediaController(mediaController);
                    return;
                }
            }
        }
        setActiveMediaController((MediaController) null);
    }

    private void setActiveMediaController(MediaController mediaController) {
        MediaController mediaController2 = this.mMediaController;
        if (mediaController != mediaController2) {
            if (mediaController2 != null) {
                mediaController2.unregisterCallback(this.mPlaybackChangedListener);
            }
            this.mMediaController = mediaController;
            if (mediaController != null) {
                mediaController.registerCallback(this.mPlaybackChangedListener);
            }
            notifyActionsChanged();
        }
    }

    /* access modifiers changed from: private */
    public void notifyActionsChanged() {
        if (!this.mListeners.isEmpty()) {
            List<RemoteAction> mediaActions = getMediaActions();
            Iterator<ActionListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onMediaActionsChanged(mediaActions);
            }
        }
    }
}
