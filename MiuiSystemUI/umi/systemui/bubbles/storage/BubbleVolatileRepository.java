package com.android.systemui.bubbles.storage;

import android.content.pm.LauncherApps;
import android.os.UserHandle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.bubbles.ShortcutKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubbleVolatileRepository.kt */
public final class BubbleVolatileRepository {
    private int capacity = 16;
    private Set<BubbleEntity> entities = new LinkedHashSet();
    private final LauncherApps launcherApps;

    @VisibleForTesting
    public static /* synthetic */ void capacity$annotations() {
    }

    public BubbleVolatileRepository(@NotNull LauncherApps launcherApps2) {
        Intrinsics.checkParameterIsNotNull(launcherApps2, "launcherApps");
        this.launcherApps = launcherApps2;
    }

    @NotNull
    public final synchronized List<BubbleEntity> getBubbles() {
        return CollectionsKt___CollectionsKt.toList(this.entities);
    }

    public final synchronized void addBubbles(@NotNull List<BubbleEntity> list) {
        Intrinsics.checkParameterIsNotNull(list, "bubbles");
        if (!list.isEmpty()) {
            List<T> takeLast = CollectionsKt___CollectionsKt.takeLast(list, this.capacity);
            ArrayList arrayList = new ArrayList();
            for (T next : takeLast) {
                if (!this.entities.removeIf(new BubbleVolatileRepository$addBubbles$uniqueBubbles$1$1((BubbleEntity) next))) {
                    arrayList.add(next);
                }
            }
            int size = (this.entities.size() + takeLast.size()) - this.capacity;
            if (size > 0) {
                uncache(CollectionsKt___CollectionsKt.take(this.entities, size));
                this.entities = CollectionsKt___CollectionsKt.toMutableSet(CollectionsKt___CollectionsKt.drop(this.entities, size));
            }
            this.entities.addAll(takeLast);
            cache(arrayList);
        }
    }

    public final synchronized void removeBubbles(@NotNull List<BubbleEntity> list) {
        Intrinsics.checkParameterIsNotNull(list, "bubbles");
        ArrayList arrayList = new ArrayList();
        for (T next : list) {
            if (this.entities.removeIf(new BubbleVolatileRepository$removeBubbles$1$1((BubbleEntity) next))) {
                arrayList.add(next);
            }
        }
        uncache(arrayList);
    }

    private final void cache(List<BubbleEntity> list) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (T next : list) {
            BubbleEntity bubbleEntity = (BubbleEntity) next;
            ShortcutKey shortcutKey = new ShortcutKey(bubbleEntity.getUserId(), bubbleEntity.getPackageName());
            Object obj = linkedHashMap.get(shortcutKey);
            if (obj == null) {
                obj = new ArrayList();
                linkedHashMap.put(shortcutKey, obj);
            }
            ((List) obj).add(next);
        }
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            ShortcutKey shortcutKey2 = (ShortcutKey) entry.getKey();
            List<BubbleEntity> list2 = (List) entry.getValue();
            LauncherApps launcherApps2 = this.launcherApps;
            String pkg = shortcutKey2.getPkg();
            ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
            for (BubbleEntity shortcutId : list2) {
                arrayList.add(shortcutId.getShortcutId());
            }
            launcherApps2.cacheShortcuts(pkg, arrayList, UserHandle.of(shortcutKey2.getUserId()), 1);
        }
    }

    private final void uncache(List<BubbleEntity> list) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (T next : list) {
            BubbleEntity bubbleEntity = (BubbleEntity) next;
            ShortcutKey shortcutKey = new ShortcutKey(bubbleEntity.getUserId(), bubbleEntity.getPackageName());
            Object obj = linkedHashMap.get(shortcutKey);
            if (obj == null) {
                obj = new ArrayList();
                linkedHashMap.put(shortcutKey, obj);
            }
            ((List) obj).add(next);
        }
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            ShortcutKey shortcutKey2 = (ShortcutKey) entry.getKey();
            List<BubbleEntity> list2 = (List) entry.getValue();
            LauncherApps launcherApps2 = this.launcherApps;
            String pkg = shortcutKey2.getPkg();
            ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10));
            for (BubbleEntity shortcutId : list2) {
                arrayList.add(shortcutId.getShortcutId());
            }
            launcherApps2.uncacheShortcuts(pkg, arrayList, UserHandle.of(shortcutKey2.getUserId()), 1);
        }
    }
}
