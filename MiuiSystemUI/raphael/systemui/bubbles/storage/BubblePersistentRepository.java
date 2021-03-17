package com.android.systemui.bubbles.storage;

import android.content.Context;
import android.util.AtomicFile;
import java.io.File;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubblePersistentRepository.kt */
public final class BubblePersistentRepository {
    private final AtomicFile bubbleFile;

    public BubblePersistentRepository(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.bubbleFile = new AtomicFile(new File(context.getFilesDir(), "overflow_bubbles.xml"), "overflow-bubbles");
    }

    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean persistsToDisk(@org.jetbrains.annotations.NotNull java.util.List<com.android.systemui.bubbles.storage.BubbleEntity> r6) {
        /*
            r5 = this;
            java.lang.String r0 = "bubbles"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r6, r0)
            android.util.AtomicFile r0 = r5.bubbleFile
            monitor-enter(r0)
            r1 = 0
            android.util.AtomicFile r2 = r5.bubbleFile     // Catch:{ IOException -> 0x0032 }
            java.io.FileOutputStream r2 = r2.startWrite()     // Catch:{ IOException -> 0x0032 }
            java.lang.String r3 = "bubbleFile.startWrite()"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r2, r3)     // Catch:{ IOException -> 0x0032 }
            com.android.systemui.bubbles.storage.BubbleXmlHelperKt.writeXml(r2, r6)     // Catch:{  }
            android.util.AtomicFile r6 = r5.bubbleFile     // Catch:{  }
            r6.finishWrite(r2)     // Catch:{  }
            r6 = 1
            monitor-exit(r0)     // Catch:{ Exception -> 0x001f }
            return r6
        L_0x001f:
            r6 = move-exception
            java.lang.String r3 = "BubblePersistentRepository"
            java.lang.String r4 = "Failed to save bubble file, restoring backup"
            android.util.Log.e(r3, r4, r6)     // Catch:{ all -> 0x0030 }
            android.util.AtomicFile r5 = r5.bubbleFile     // Catch:{ all -> 0x0030 }
            r5.failWrite(r2)     // Catch:{ all -> 0x0030 }
            kotlin.Unit r5 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x0030 }
            monitor-exit(r0)
            return r1
        L_0x0030:
            r5 = move-exception
            goto L_0x003c
        L_0x0032:
            r5 = move-exception
            java.lang.String r6 = "BubblePersistentRepository"
            java.lang.String r2 = "Failed to save bubble file"
            android.util.Log.e(r6, r2, r5)     // Catch:{ all -> 0x0030 }
            monitor-exit(r0)
            return r1
        L_0x003c:
            monitor-exit(r0)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.storage.BubblePersistentRepository.persistsToDisk(java.util.List):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0023, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        kotlin.io.CloseableKt.closeFinally(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0027, code lost:
        throw r2;
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.util.List<com.android.systemui.bubbles.storage.BubbleEntity> readFromDisk() {
        /*
            r3 = this;
            android.util.AtomicFile r0 = r3.bubbleFile
            monitor-enter(r0)
            android.util.AtomicFile r1 = r3.bubbleFile     // Catch:{ all -> 0x0036 }
            boolean r1 = r1.exists()     // Catch:{ all -> 0x0036 }
            if (r1 != 0) goto L_0x0011
            java.util.List r3 = kotlin.collections.CollectionsKt__CollectionsKt.emptyList()     // Catch:{ all -> 0x0036 }
            monitor-exit(r0)
            return r3
        L_0x0011:
            android.util.AtomicFile r3 = r3.bubbleFile     // Catch:{ all -> 0x0028 }
            java.io.FileInputStream r3 = r3.openRead()     // Catch:{ all -> 0x0028 }
            r1 = 0
            java.util.List r2 = com.android.systemui.bubbles.storage.BubbleXmlHelperKt.readXml(r3)     // Catch:{ all -> 0x0021 }
            kotlin.io.CloseableKt.closeFinally(r3, r1)     // Catch:{ all -> 0x0028 }
            monitor-exit(r0)     // Catch:{ all -> 0x0028 }
            return r2
        L_0x0021:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0023 }
        L_0x0023:
            r2 = move-exception
            kotlin.io.CloseableKt.closeFinally(r3, r1)     // Catch:{ all -> 0x0028 }
            throw r2     // Catch:{ all -> 0x0028 }
        L_0x0028:
            r3 = move-exception
            java.lang.String r1 = "BubblePersistentRepository"
            java.lang.String r2 = "Failed to open bubble file"
            android.util.Log.e(r1, r2, r3)     // Catch:{ all -> 0x0036 }
            java.util.List r3 = kotlin.collections.CollectionsKt__CollectionsKt.emptyList()     // Catch:{ all -> 0x0036 }
            monitor-exit(r0)
            return r3
        L_0x0036:
            r3 = move-exception
            monitor-exit(r0)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.storage.BubblePersistentRepository.readFromDisk():java.util.List");
    }
}
