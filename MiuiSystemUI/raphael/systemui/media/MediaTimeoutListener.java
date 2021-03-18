package com.android.systemui.media;

import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.util.Log;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaTimeoutListener.kt */
public final class MediaTimeoutListener implements MediaDataManager.Listener {
    private final DelayableExecutor mainExecutor;
    private final MediaControllerFactory mediaControllerFactory;
    private final Map<String, PlaybackStateListener> mediaListeners = new LinkedHashMap();
    @NotNull
    public Function2<? super String, ? super Boolean, Unit> timeoutCallback;

    public MediaTimeoutListener(@NotNull MediaControllerFactory mediaControllerFactory2, @NotNull DelayableExecutor delayableExecutor) {
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory2, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "mainExecutor");
        this.mediaControllerFactory = mediaControllerFactory2;
        this.mainExecutor = delayableExecutor;
    }

    /* JADX DEBUG: Type inference failed for r0v1. Raw type applied. Possible types: kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Boolean, kotlin.Unit>, kotlin.jvm.functions.Function2<java.lang.String, java.lang.Boolean, kotlin.Unit> */
    @NotNull
    public final Function2<String, Boolean, Unit> getTimeoutCallback() {
        Function2 function2 = this.timeoutCallback;
        if (function2 != null) {
            return function2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("timeoutCallback");
        throw null;
    }

    public final void setTimeoutCallback(@NotNull Function2<? super String, ? super Boolean, Unit> function2) {
        Intrinsics.checkParameterIsNotNull(function2, "<set-?>");
        this.timeoutCallback = function2;
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        Boolean playing;
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        if (!this.mediaListeners.containsKey(str)) {
            boolean z = false;
            boolean z2 = str2 != null && (Intrinsics.areEqual(str, str2) ^ true);
            if (z2) {
                Map<String, PlaybackStateListener> map = this.mediaListeners;
                if (map == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.Map<K, *>");
                } else if (map.containsKey(str2)) {
                    Map<String, PlaybackStateListener> map2 = this.mediaListeners;
                    if (map2 != null) {
                        PlaybackStateListener playbackStateListener = (PlaybackStateListener) TypeIntrinsics.asMutableMap(map2).remove(str2);
                        if (!(playbackStateListener == null || (playing = playbackStateListener.getPlaying()) == null)) {
                            z = playing.booleanValue();
                        }
                        if (playbackStateListener != null) {
                            playbackStateListener.destroy();
                        }
                        Log.d("MediaTimeout", "migrating key " + str2 + " to " + str + ", for resumption");
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.MutableMap<K, V>");
                    }
                } else {
                    Log.w("MediaTimeout", "Old key " + str2 + " for player " + str + " doesn't exist. Continuing...");
                }
            }
            this.mediaListeners.put(str, new PlaybackStateListener(this, str, mediaData));
            if (z2) {
                PlaybackStateListener playbackStateListener2 = this.mediaListeners.get(str);
                if (!Intrinsics.areEqual(playbackStateListener2 != null ? playbackStateListener2.getPlaying() : null, Boolean.valueOf(z))) {
                    this.mainExecutor.execute(new MediaTimeoutListener$onMediaDataLoaded$1(this, str));
                }
            }
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        PlaybackStateListener remove = this.mediaListeners.remove(str);
        if (remove != null) {
            remove.destroy();
        }
    }

    public final boolean hasPlayingMedia() {
        Map<String, PlaybackStateListener> map = this.mediaListeners;
        if (map.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, PlaybackStateListener> entry : map.entrySet()) {
            if (Intrinsics.areEqual(entry.getValue().getPlaying(), Boolean.TRUE)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* compiled from: MediaTimeoutListener.kt */
    public final class PlaybackStateListener extends MediaController.Callback {
        private Runnable cancellation;
        private final String key;
        private final MediaController mediaController;
        @Nullable
        private Boolean playing;
        final /* synthetic */ MediaTimeoutListener this$0;
        private boolean timedOut;

        public PlaybackStateListener(@NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull String str, MediaData mediaData) {
            Intrinsics.checkParameterIsNotNull(str, "key");
            Intrinsics.checkParameterIsNotNull(mediaData, "data");
            this.this$0 = mediaTimeoutListener;
            this.key = str;
            PlaybackState playbackState = null;
            MediaController create = mediaData.getToken() != null ? mediaTimeoutListener.mediaControllerFactory.create(mediaData.getToken()) : null;
            this.mediaController = create;
            if (create != null) {
                create.registerCallback(this);
            }
            MediaController mediaController2 = this.mediaController;
            processState(mediaController2 != null ? mediaController2.getPlaybackState() : playbackState, false);
        }

        public final boolean getTimedOut() {
            return this.timedOut;
        }

        public final void setTimedOut(boolean z) {
            this.timedOut = z;
        }

        @Nullable
        public final Boolean getPlaying() {
            return this.playing;
        }

        public final void destroy() {
            MediaController mediaController2 = this.mediaController;
            if (mediaController2 != null) {
                mediaController2.unregisterCallback(this);
            }
        }

        public void onPlaybackStateChanged(@Nullable PlaybackState playbackState) {
            processState(playbackState, true);
        }

        private final void processState(PlaybackState playbackState, boolean z) {
            Log.v("MediaTimeout", "processState: " + playbackState);
            boolean z2 = playbackState != null && NotificationMediaManager.isPlayingState(playbackState.getState());
            if (!Intrinsics.areEqual(this.playing, Boolean.valueOf(z2)) || this.playing == null) {
                this.playing = Boolean.valueOf(z2);
                if (!z2) {
                    Log.v("MediaTimeout", "schedule timeout for " + this.key);
                    if (this.cancellation != null) {
                        Log.d("MediaTimeout", "cancellation already exists, continuing.");
                        return;
                    }
                    String str = this.key;
                    expireMediaTimeout(str, "PLAYBACK STATE CHANGED - " + playbackState);
                    this.cancellation = this.this$0.mainExecutor.executeDelayed(new MediaTimeoutListener$PlaybackStateListener$processState$1(this, z), MediaTimeoutListenerKt.access$getPAUSED_MEDIA_TIMEOUT$p());
                    return;
                }
                String str2 = this.key;
                expireMediaTimeout(str2, "playback started - " + playbackState + ", " + this.key);
                this.timedOut = false;
                if (z) {
                    this.this$0.getTimeoutCallback().invoke(this.key, Boolean.valueOf(this.timedOut));
                }
            }
        }

        private final void expireMediaTimeout(String str, String str2) {
            Runnable runnable = this.cancellation;
            if (runnable != null) {
                Log.v("MediaTimeout", "media timeout cancelled for  " + str + ", reason: " + str2);
                runnable.run();
            }
            this.cancellation = null;
        }
    }
}
