package com.android.systemui.recents.model;

import android.util.Log;
import android.util.LruCache;
import com.android.systemui.recents.model.Task;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskKeyLruCache<V> {
    private final LruCache<Integer, V> mCache;
    /* access modifiers changed from: private */
    public final EvictionCallback mEvictionCallback;
    /* access modifiers changed from: private */
    public final Map<Integer, Task.TaskKey> mKeys;

    public interface EvictionCallback {
        void onEntryEvicted(Task.TaskKey taskKey);
    }

    public TaskKeyLruCache(int i) {
        this(i, (EvictionCallback) null);
    }

    public TaskKeyLruCache(int i, EvictionCallback evictionCallback) {
        this.mKeys = new ConcurrentHashMap();
        this.mEvictionCallback = evictionCallback;
        this.mCache = new LruCache<Integer, V>(i) {
            /* access modifiers changed from: protected */
            public void entryRemoved(boolean z, Integer num, V v, V v2) {
                if (TaskKeyLruCache.this.mEvictionCallback != null) {
                    TaskKeyLruCache.this.mEvictionCallback.onEntryEvicted((Task.TaskKey) TaskKeyLruCache.this.mKeys.get(num));
                }
                TaskKeyLruCache.this.mKeys.remove(num);
            }
        };
    }

    /* access modifiers changed from: package-private */
    public final V get(Task.TaskKey taskKey) {
        return this.mCache.get(Integer.valueOf(taskKey.id));
    }

    /* access modifiers changed from: package-private */
    public final V getAndInvalidateIfModified(Task.TaskKey taskKey) {
        Task.TaskKey taskKey2 = this.mKeys.get(Integer.valueOf(taskKey.id));
        if (taskKey2 == null || (taskKey2.stackId == taskKey.stackId && taskKey2.lastActiveTime == taskKey.lastActiveTime && taskKey2.isThumbnailBlur == taskKey.isThumbnailBlur && taskKey2.isScreening == taskKey.isScreening)) {
            return this.mCache.get(Integer.valueOf(taskKey.id));
        }
        remove(taskKey);
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void put(Task.TaskKey taskKey, V v) {
        if (taskKey == null || v == null) {
            Log.e("TaskKeyLruCache", "Unexpected null key or value: " + taskKey + ", " + v);
            return;
        }
        this.mKeys.put(Integer.valueOf(taskKey.id), taskKey);
        this.mCache.put(Integer.valueOf(taskKey.id), v);
    }

    /* access modifiers changed from: package-private */
    public final void remove(Task.TaskKey taskKey) {
        this.mCache.remove(Integer.valueOf(taskKey.id));
        this.mKeys.remove(Integer.valueOf(taskKey.id));
    }

    /* access modifiers changed from: package-private */
    public final void evictAll() {
        this.mCache.evictAll();
        this.mKeys.clear();
    }

    /* access modifiers changed from: package-private */
    public final void trimToSize(int i) {
        this.mCache.trimToSize(i);
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.print("TaskKeyLruCache");
        printWriter.print(" numEntries=");
        printWriter.print(this.mKeys.size());
        printWriter.println();
        for (Integer num : this.mKeys.keySet()) {
            printWriter.print(str2);
            printWriter.println(this.mKeys.get(num));
        }
    }
}
