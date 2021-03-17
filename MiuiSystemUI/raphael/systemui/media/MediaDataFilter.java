package com.android.systemui.media;

import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataFilter.kt */
public final class MediaDataFilter implements MediaDataManager.Listener {
    private final BroadcastDispatcher broadcastDispatcher;
    private final MediaDataCombineLatest dataSource;
    private final NotificationEntryManager entryManager;
    /* access modifiers changed from: private */
    public final Executor executor;
    private final Set<MediaDataManager.Listener> listeners = new LinkedHashSet();
    private final NotificationLockscreenUserManager lockscreenUserManager;
    private final MediaDataManager mediaDataManager;
    private final LinkedHashMap<String, MediaData> mediaEntries = new LinkedHashMap<>();
    private final MediaResumeListener mediaResumeListener;
    private final CurrentUserTracker userTracker;

    public MediaDataFilter(@NotNull MediaDataCombineLatest mediaDataCombineLatest, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull MediaResumeListener mediaResumeListener2, @NotNull MediaDataManager mediaDataManager2, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull Executor executor2, @NotNull NotificationEntryManager notificationEntryManager) {
        Intrinsics.checkParameterIsNotNull(mediaDataCombineLatest, "dataSource");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener2, "mediaResumeListener");
        Intrinsics.checkParameterIsNotNull(mediaDataManager2, "mediaDataManager");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "lockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        this.dataSource = mediaDataCombineLatest;
        this.broadcastDispatcher = broadcastDispatcher2;
        this.mediaResumeListener = mediaResumeListener2;
        this.mediaDataManager = mediaDataManager2;
        this.lockscreenUserManager = notificationLockscreenUserManager;
        this.executor = executor2;
        this.entryManager = notificationEntryManager;
        AnonymousClass1 r2 = new CurrentUserTracker(this, this.broadcastDispatcher) {
            final /* synthetic */ MediaDataFilter this$0;

            {
                this.this$0 = r1;
            }

            public void onUserSwitched(int i) {
                this.this$0.executor.execute(new MediaDataFilter$1$onUserSwitched$1(this, i));
            }
        };
        this.userTracker = r2;
        r2.startTracking();
        this.dataSource.addListener(this);
    }

    public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        if (this.lockscreenUserManager.isCurrentProfile(mediaData.getUserId())) {
            if (str2 != null) {
                this.mediaEntries.remove(str2);
            }
            this.mediaEntries.put(str, mediaData);
            for (T onMediaDataLoaded : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                onMediaDataLoaded.onMediaDataLoaded(str, str2, mediaData);
            }
        }
    }

    public void onMediaDataRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        if (((MediaData) this.mediaEntries.remove(str)) != null) {
            for (T onMediaDataRemoved : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                onMediaDataRemoved.onMediaDataRemoved(str);
            }
        }
    }

    @VisibleForTesting
    public final void handleUserSwitched$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(int i) {
        Set<T> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
        Set<String> keySet = this.mediaEntries.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet, "mediaEntries.keys");
        List<T> mutableList = CollectionsKt___CollectionsKt.toMutableList(keySet);
        this.mediaEntries.clear();
        for (T t : mutableList) {
            Log.d("MediaDataFilter", "Removing " + t + " after user change");
            for (T onMediaDataRemoved : set) {
                Intrinsics.checkExpressionValueIsNotNull(t, "it");
                onMediaDataRemoved.onMediaDataRemoved(t);
            }
        }
        for (Map.Entry next : this.dataSource.getData().entrySet()) {
            String str = (String) next.getKey();
            MediaData mediaData = (MediaData) next.getValue();
            if (this.lockscreenUserManager.isCurrentProfile(mediaData.getUserId())) {
                Log.d("MediaDataFilter", "Re-adding " + str + " after user change");
                this.mediaEntries.put(str, mediaData);
                for (T onMediaDataLoaded : set) {
                    onMediaDataLoaded.onMediaDataLoaded(str, (String) null, mediaData);
                }
            }
        }
    }

    public final void onSwipeToDismiss() {
        Set<String> keySet = this.mediaEntries.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet, "mediaEntries.keys");
        for (T t : CollectionsKt___CollectionsKt.toSet(keySet)) {
            MediaDataManager mediaDataManager2 = this.mediaDataManager;
            Intrinsics.checkExpressionValueIsNotNull(t, "it");
            mediaDataManager2.setTimedOut$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(t, true);
            NotificationEntry activeNotificationUnfiltered = this.entryManager.getActiveNotificationUnfiltered(t);
            if (activeNotificationUnfiltered != null) {
                this.entryManager.performRemoveNotification(activeNotificationUnfiltered.getSbn(), 2);
            }
        }
    }

    public final boolean hasActiveMedia() {
        LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
        if (linkedHashMap.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, MediaData> value : linkedHashMap.entrySet()) {
            if (((MediaData) value.getValue()).getActive()) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasAnyMedia() {
        if (this.mediaResumeListener.isResumptionEnabled()) {
            return !this.mediaEntries.isEmpty();
        }
        return hasActiveMedia();
    }

    public final boolean addListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    public final boolean removeListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.remove(listener);
    }
}
