package com.android.systemui.util.leak;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WeakIdentityHashMap<K, V> {
    private final HashMap<WeakReference<K>, V> mMap = new HashMap<>();
    private final ReferenceQueue<Object> mRefQueue = new ReferenceQueue<>();

    private void cleanUp() {
        while (true) {
            Reference<? extends Object> poll = this.mRefQueue.poll();
            if (poll != null) {
                this.mMap.remove(poll);
            } else {
                return;
            }
        }
    }

    public void put(K k, V v) {
        cleanUp();
        this.mMap.put(new CmpWeakReference(k, this.mRefQueue), v);
    }

    public V get(K k) {
        cleanUp();
        return this.mMap.get(new CmpWeakReference(k));
    }

    public Set<Map.Entry<WeakReference<K>, V>> entrySet() {
        return this.mMap.entrySet();
    }

    public int size() {
        cleanUp();
        return this.mMap.size();
    }

    public boolean isEmpty() {
        cleanUp();
        return this.mMap.isEmpty();
    }

    private static class CmpWeakReference<K> extends WeakReference<K> {
        private final int mHashCode;

        public CmpWeakReference(K k) {
            super(k);
            this.mHashCode = System.identityHashCode(k);
        }

        public CmpWeakReference(K k, ReferenceQueue<Object> referenceQueue) {
            super(k, referenceQueue);
            this.mHashCode = System.identityHashCode(k);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            Object obj2 = get();
            if (obj2 == null || !(obj instanceof CmpWeakReference)) {
                return false;
            }
            if (((CmpWeakReference) obj).get() == obj2) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.mHashCode;
        }
    }
}
