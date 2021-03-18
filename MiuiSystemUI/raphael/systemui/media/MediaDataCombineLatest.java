package com.android.systemui.media;

import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDeviceManager;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaDataCombineLatest.kt */
public final class MediaDataCombineLatest {
    private final MediaDataManager dataSource;
    private final MediaDeviceManager deviceSource;
    private final Map<String, Pair<MediaData, MediaDeviceData>> entries = new LinkedHashMap();
    private final Set<MediaDataManager.Listener> listeners = new LinkedHashSet();

    public MediaDataCombineLatest(@NotNull MediaDataManager mediaDataManager, @NotNull MediaDeviceManager mediaDeviceManager) {
        Intrinsics.checkParameterIsNotNull(mediaDataManager, "dataSource");
        Intrinsics.checkParameterIsNotNull(mediaDeviceManager, "deviceSource");
        this.dataSource = mediaDataManager;
        this.deviceSource = mediaDeviceManager;
        this.dataSource.addListener(new MediaDataManager.Listener(this) {
            /* class com.android.systemui.media.MediaDataCombineLatest.AnonymousClass1 */
            final /* synthetic */ MediaDataCombineLatest this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Intrinsics.checkParameterIsNotNull(mediaData, "data");
                MediaDeviceData mediaDeviceData = null;
                if (str2 == null || str2.equals(str)) {
                    Map map = this.this$0.entries;
                    Pair pair = (Pair) this.this$0.entries.get(str);
                    if (pair != null) {
                        mediaDeviceData = (MediaDeviceData) pair.getSecond();
                    }
                    map.put(str, TuplesKt.to(mediaData, mediaDeviceData));
                } else {
                    Pair pair2 = (Pair) this.this$0.entries.get(str2);
                    if (pair2 != null) {
                        MediaDeviceData mediaDeviceData2 = (MediaDeviceData) pair2.getSecond();
                    }
                    Map map2 = this.this$0.entries;
                    Pair pair3 = (Pair) this.this$0.entries.get(str2);
                    if (pair3 != null) {
                        mediaDeviceData = (MediaDeviceData) pair3.getSecond();
                    }
                    map2.put(str, TuplesKt.to(mediaData, mediaDeviceData));
                    this.this$0.entries.remove(str2);
                }
                this.this$0.update(str, str2);
            }

            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataRemoved(@NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                this.this$0.remove(str);
            }
        });
        this.deviceSource.addListener(new MediaDeviceManager.Listener(this) {
            /* class com.android.systemui.media.MediaDataCombineLatest.AnonymousClass2 */
            final /* synthetic */ MediaDataCombineLatest this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.media.MediaDeviceManager.Listener
            public void onMediaDeviceChanged(@NotNull String str, @Nullable MediaDeviceData mediaDeviceData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Map map = this.this$0.entries;
                Pair pair = (Pair) this.this$0.entries.get(str);
                map.put(str, TuplesKt.to(pair != null ? (MediaData) pair.getFirst() : null, mediaDeviceData));
                this.this$0.update(str, str);
            }

            @Override // com.android.systemui.media.MediaDeviceManager.Listener
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
        for (Map.Entry<String, Pair<MediaData, MediaDeviceData>> entry : map.entrySet()) {
            entry.getKey();
            Pair<MediaData, MediaDeviceData> value = entry.getValue();
            if ((value.getFirst() == null || value.getSecond() == null) ? false : true) {
                linkedHashMap.put(entry.getKey(), entry.getValue());
            }
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap(MapsKt__MapsKt.mapCapacity(linkedHashMap.size()));
        for (Map.Entry entry2 : linkedHashMap.entrySet()) {
            Object key = entry2.getKey();
            String str = (String) entry2.getKey();
            Pair pair = (Pair) entry2.getValue();
            Object first = pair.getFirst();
            if (first != null) {
                linkedHashMap2.put(key, MediaData.copy$default((MediaData) first, 0, false, 0, null, null, null, null, null, null, null, null, null, null, (MediaDeviceData) pair.getSecond(), false, null, false, null, false, 516095, null));
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
    /* access modifiers changed from: public */
    private final void update(String str, String str2) {
        Pair<MediaData, MediaDeviceData> pair = this.entries.get(str);
        if (pair == null) {
            pair = TuplesKt.to(null, null);
        }
        MediaData component1 = pair.component1();
        MediaDeviceData component2 = pair.component2();
        if (!(component1 == null || component2 == null)) {
            MediaData copy$default = MediaData.copy$default(component1, 0, false, 0, null, null, null, null, null, null, null, null, null, null, component2, false, null, false, null, false, 516095, null);
            for (MediaDataManager.Listener listener : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                listener.onMediaDataLoaded(str, str2, copy$default);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void remove(String str) {
        if (this.entries.remove(str) != null) {
            for (MediaDataManager.Listener listener : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                listener.onMediaDataRemoved(str);
            }
        }
    }
}
