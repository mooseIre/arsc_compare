package com.android.systemui.pip.phone;

import android.app.IActivityManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.pip.phone.PipMediaController;
import com.android.systemui.statusbar.policy.UserInfoController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PipMediaController {
    private final IActivityManager mActivityManager;
    private final Context mContext;
    private ArrayList<ActionListener> mListeners = new ArrayList<>();
    private MediaController mMediaController;
    private final MediaSessionManager mMediaSessionManager;
    private RemoteAction mNextAction;
    private RemoteAction mPauseAction;
    private RemoteAction mPlayAction;
    private BroadcastReceiver mPlayPauseActionReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.pip.phone.PipMediaController.AnonymousClass1 */

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
    private final MediaController.Callback mPlaybackChangedListener = new MediaController.Callback() {
        /* class com.android.systemui.pip.phone.PipMediaController.AnonymousClass2 */

        public void onPlaybackStateChanged(PlaybackState playbackState) {
            PipMediaController.this.notifyActionsChanged();
        }
    };
    private RemoteAction mPrevAction;
    private final MediaSessionManager.OnActiveSessionsChangedListener mSessionsChangedListener = new MediaSessionManager.OnActiveSessionsChangedListener() {
        /* class com.android.systemui.pip.phone.PipMediaController.AnonymousClass3 */

        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> list) {
            PipMediaController.this.resolveActiveMediaController(list);
        }
    };

    public interface ActionListener {
        void onMediaActionsChanged(List<RemoteAction> list);
    }

    public PipMediaController(Context context, IActivityManager iActivityManager, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.pip.phone.PLAY");
        intentFilter.addAction("com.android.systemui.pip.phone.PAUSE");
        intentFilter.addAction("com.android.systemui.pip.phone.NEXT");
        intentFilter.addAction("com.android.systemui.pip.phone.PREV");
        broadcastDispatcher.registerReceiver(this.mPlayPauseActionReceiver, intentFilter);
        createMediaActions();
        this.mMediaSessionManager = (MediaSessionManager) context.getSystemService("media_session");
        ((UserInfoController) Dependency.get(UserInfoController.class)).addCallback(new UserInfoController.OnUserInfoChangedListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMediaController$neOVZxIcmRkhimcM6huwsIEiXEw */

            @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
            public final void onUserInfoChanged(String str, Drawable drawable, String str2) {
                PipMediaController.this.lambda$new$0$PipMediaController(str, drawable, str2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipMediaController(String str, Drawable drawable, String str2) {
        registerSessionListenerForCurrentUser();
    }

    public void onActivityPinned() {
        resolveActiveMediaController(this.mMediaSessionManager.getActiveSessionsForUser(null, -2));
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
        String string = this.mContext.getString(C0021R$string.pip_pause);
        this.mPauseAction = new RemoteAction(Icon.createWithResource(this.mContext, C0013R$drawable.ic_pause_white), string, string, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.PAUSE"), 134217728));
        String string2 = this.mContext.getString(C0021R$string.pip_play);
        this.mPlayAction = new RemoteAction(Icon.createWithResource(this.mContext, C0013R$drawable.ic_play_arrow_white), string2, string2, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.PLAY"), 134217728));
        String string3 = this.mContext.getString(C0021R$string.pip_skip_to_next);
        this.mNextAction = new RemoteAction(Icon.createWithResource(this.mContext, C0013R$drawable.ic_skip_next_white), string3, string3, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.NEXT"), 134217728));
        String string4 = this.mContext.getString(C0021R$string.pip_skip_to_prev);
        this.mPrevAction = new RemoteAction(Icon.createWithResource(this.mContext, C0013R$drawable.ic_skip_previous_white), string4, string4, PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.android.systemui.pip.phone.PREV"), 134217728));
    }

    private void registerSessionListenerForCurrentUser() {
        this.mMediaSessionManager.removeOnActiveSessionsChangedListener(this.mSessionsChangedListener);
        this.mMediaSessionManager.addOnActiveSessionsChangedListener(this.mSessionsChangedListener, null, -2, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resolveActiveMediaController(List<MediaController> list) {
        ComponentName componentName;
        if (!(list == null || (componentName = (ComponentName) PipUtils.getTopPipActivity(this.mContext, this.mActivityManager).first) == null)) {
            for (int i = 0; i < list.size(); i++) {
                MediaController mediaController = list.get(i);
                if (mediaController.getPackageName().equals(componentName.getPackageName())) {
                    setActiveMediaController(mediaController);
                    return;
                }
            }
        }
        setActiveMediaController(null);
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
    /* access modifiers changed from: public */
    private void notifyActionsChanged() {
        if (!this.mListeners.isEmpty()) {
            this.mListeners.forEach(new Consumer(getMediaActions()) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipMediaController$PGZH9Rcf3EMC5cibv13aaStfc2E */
                public final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    PipMediaController.lambda$notifyActionsChanged$1(this.f$0, (PipMediaController.ActionListener) obj);
                }
            });
        }
    }
}
