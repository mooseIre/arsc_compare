package com.android.systemui.util;

import android.os.FileObserver;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class FixedFileObserver {
    /* access modifiers changed from: private */
    public static final HashMap<File, Set<FixedFileObserver>> sObserverLists = new HashMap<>();
    /* access modifiers changed from: private */
    public final int mMask;
    private FileObserver mObserver;
    private final File mRootPath;

    public abstract void onEvent(int i, String str);

    public FixedFileObserver(String str, int i) {
        this.mRootPath = new File(str);
        this.mMask = i;
    }

    public void startWatching() {
        synchronized (sObserverLists) {
            if (!sObserverLists.containsKey(this.mRootPath)) {
                sObserverLists.put(this.mRootPath, new HashSet());
            }
            final Set set = sObserverLists.get(this.mRootPath);
            this.mObserver = set.size() > 0 ? ((FixedFileObserver) set.iterator().next()).mObserver : new FileObserver(this.mRootPath.getPath()) {
                public void onEvent(int i, String str) {
                    synchronized (FixedFileObserver.sObserverLists) {
                        for (FixedFileObserver fixedFileObserver : set) {
                            if ((fixedFileObserver.mMask & i) != 0) {
                                fixedFileObserver.onEvent(i, str);
                            }
                        }
                    }
                }
            };
            this.mObserver.startWatching();
            set.add(this);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stopWatching() {
        /*
            r3 = this;
            java.util.HashMap<java.io.File, java.util.Set<com.android.systemui.util.FixedFileObserver>> r0 = sObserverLists
            monitor-enter(r0)
            java.util.HashMap<java.io.File, java.util.Set<com.android.systemui.util.FixedFileObserver>> r1 = sObserverLists     // Catch:{ all -> 0x0029 }
            java.io.File r2 = r3.mRootPath     // Catch:{ all -> 0x0029 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x0029 }
            java.util.Set r1 = (java.util.Set) r1     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x0027
            android.os.FileObserver r2 = r3.mObserver     // Catch:{ all -> 0x0029 }
            if (r2 != 0) goto L_0x0014
            goto L_0x0027
        L_0x0014:
            r1.remove(r3)     // Catch:{ all -> 0x0029 }
            int r1 = r1.size()     // Catch:{ all -> 0x0029 }
            if (r1 != 0) goto L_0x0022
            android.os.FileObserver r1 = r3.mObserver     // Catch:{ all -> 0x0029 }
            r1.stopWatching()     // Catch:{ all -> 0x0029 }
        L_0x0022:
            r1 = 0
            r3.mObserver = r1     // Catch:{ all -> 0x0029 }
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0027:
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return
        L_0x0029:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.FixedFileObserver.stopWatching():void");
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        stopWatching();
    }
}
