package com.android.systemui.media;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDeviceManager;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataCombineLatest.kt */
public final class MediaDataCombineLatest {
    private final MediaDataManager dataSource;
    private final MediaDeviceManager deviceSource;
    /* access modifiers changed from: private */
    public final Map<String, Pair<MediaData, MediaDeviceData>> entries = new LinkedHashMap();
    private final Set<MediaDataManager.Listener> listeners = new LinkedHashSet();

    public MediaDataCombineLatest(@NotNull MediaDataManager mediaDataManager, @NotNull MediaDeviceManager mediaDeviceManager) {
        Intrinsics.checkParameterIsNotNull(mediaDataManager, "dataSource");
        Intrinsics.checkParameterIsNotNull(mediaDeviceManager, "deviceSource");
        this.dataSource = mediaDataManager;
        this.deviceSource = mediaDeviceManager;
        this.dataSource.addListener(new MediaDataManager.Listener(this) {
            final /* synthetic */ MediaDataCombineLatest this$0;

            {
                this.this$0 = r1;
            }

            public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Intrinsics.checkParameterIsNotNull(mediaData, "data");
                MediaDeviceData mediaDeviceData = null;
                if (str2 == null || str2.equals(str)) {
                    Map access$getEntries$p = this.this$0.entries;
                    Pair pair = (Pair) this.this$0.entries.get(str);
                    if (pair != null) {
                        mediaDeviceData = (MediaDeviceData) pair.getSecond();
                    }
                    access$getEntries$p.put(str, TuplesKt.to(mediaData, mediaDeviceData));
                } else {
                    Pair pair2 = (Pair) this.this$0.entries.get(str2);
                    if (pair2 != null) {
                        MediaDeviceData mediaDeviceData2 = (MediaDeviceData) pair2.getSecond();
                    }
                    Map access$getEntries$p2 = this.this$0.entries;
                    Pair pair3 = (Pair) this.this$0.entries.get(str2);
                    if (pair3 != null) {
                        mediaDeviceData = (MediaDeviceData) pair3.getSecond();
                    }
                    access$getEntries$p2.put(str, TuplesKt.to(mediaData, mediaDeviceData));
                    this.this$0.entries.remove(str2);
                }
                this.this$0.update(str, str2);
            }

            public void onMediaDataRemoved(@NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                this.this$0.remove(str);
            }
        });
        this.deviceSource.addListener(new MediaDeviceManager.Listener(this) {
            final /* synthetic */ MediaDataCombineLatest this$0;

            {
                this.this$0 = r1;
            }

            public void onMediaDeviceChanged(@NotNull String str, @Nullable MediaDeviceData mediaDeviceData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Map access$getEntries$p = this.this$0.entries;
                Pair pair = (Pair) this.this$0.entries.get(str);
                access$getEntries$p.put(str, TuplesKt.to(pair != null ? (MediaData) pair.getFirst() : null, mediaDeviceData));
                this.this$0.update(str, str);
            }

            public void onKeyRemoved(@NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                this.this$0.remove(str);
            }
        });
    }

    @NotNull
    public final Map<String, MediaData> getData() {
        Map<String, Pair<MediaData, MediaDeviceData>> map = this.entries;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry next : map.entrySet()) {
            String str = (String) next.getKey();
            Pair pair = (Pair) next.getValue();
            if ((pair.getFirst() == null || pair.getSecond() == null) ? false : true) {
                linkedHashMap.put(next.getKey(), next.getValue());
            }
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap(MapsKt__MapsKt.mapCapacity(linkedHashMap.size()));
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            Object key = entry.getKey();
            String str2 = (String) entry.getKey();
            Pair pair2 = (Pair) entry.getValue();
            Object first = pair2.getFirst();
            if (first != null) {
                linkedHashMap2.put(key, MediaData.copy$default((MediaData) first, 0, false, 0, (String) null, (Drawable) null, (CharSequence) null, (CharSequence) null, (Icon) null, (List) null, (List) null, (String) null, (MediaSession.Token) null, (PendingIntent) null, (MediaDeviceData) pair2.getSecond(), false, (Runnable) null, false, (String) null, false, 516095, (Object) null));
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        return linkedHashMap2;
    }

    public final boolean addListener(@NotNull MediaDataManager.Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    /* access modifiers changed from: private */
    public final void update(String str, String str2) {
        String str3 = str;
        Pair pair = this.entries.get(str3);
        if (pair == null) {
            pair = TuplesKt.to(null, null);
        }
        MediaData mediaData = (MediaData) pair.component1();
        MediaDeviceData mediaDeviceData = (MediaDeviceData) pair.component2();
        if (mediaData != null && mediaDeviceData != null) {
            MediaData copy$default = MediaData.copy$default(mediaData, 0, false, 0, (String) null, (Drawable) null, (CharSequence) null, (CharSequence) null, (Icon) null, (List) null, (List) null, (String) null, (MediaSession.Token) null, (PendingIntent) null, mediaDeviceData, false, (Runnable) null, false, (String) null, false, 516095, (Object) null);
            for (T onMediaDataLoaded : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                onMediaDataLoaded.onMediaDataLoaded(str3, str2, copy$default);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void remove(String str) {
        if (this.entries.remove(str) != null) {
            for (T onMediaDataRemoved : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                onMediaDataRemoved.onMediaDataRemoved(str);
            }
        }
    }
}
