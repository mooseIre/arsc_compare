package com.android.systemui.bubbles.storage;

import android.content.Context;
import android.util.AtomicFile;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubblePersistentRepository.kt */
public final class BubblePersistentRepository {
    private final AtomicFile bubbleFile;

    public BubblePersistentRepository(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.bubbleFile = new AtomicFile(new File(context.getFilesDir(), "overflow_bubbles.xml"), "overflow-bubbles");
    }

    public final boolean persistsToDisk(@NotNull List<BubbleEntity> list) {
        Intrinsics.checkParameterIsNotNull(list, "bubbles");
        synchronized (this.bubbleFile) {
            try {
                FileOutputStream startWrite = this.bubbleFile.startWrite();
                Intrinsics.checkExpressionValueIsNotNull(startWrite, "bubbleFile.startWrite()");
                BubbleXmlHelperKt.writeXml(startWrite, list);
                this.bubbleFile.finishWrite(startWrite);
                try {
                } catch (Exception e) {
                    Log.e("BubblePersistentRepository", "Failed to save bubble file, restoring backup", e);
                    this.bubbleFile.failWrite(startWrite);
                    Unit unit = Unit.INSTANCE;
                    return false;
                }
            } catch (IOException e2) {
                Log.e("BubblePersistentRepository", "Failed to save bubble file", e2);
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0023, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0024, code lost:
        kotlin.io.CloseableKt.closeFinally(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0027, code lost:
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
            java.util.List r3 = kotlin.collections.CollectionsKt.emptyList()     // Catch:{ all -> 0x0036 }
            monitor-exit(r0)
            return r3
        L_0x0011:
            android.util.AtomicFile r3 = r3.bubbleFile     // Catch:{ all -> 0x0028 }
            java.io.FileInputStream r3 = r3.openRead()     // Catch:{ all -> 0x0028 }
            r1 = 0
            java.util.List r2 = com.android.systemui.bubbles.storage.BubbleXmlHelperKt.readXml(r3)     // Catch:{ all -> 0x0021 }
            kotlin.io.CloseableKt.closeFinally(r3, r1)
            monitor-exit(r0)
            return r2
        L_0x0021:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0023 }
        L_0x0023:
            r2 = move-exception
            kotlin.io.CloseableKt.closeFinally(r3, r1)
            throw r2
        L_0x0028:
            r3 = move-exception
            java.lang.String r1 = "BubblePersistentRepository"
            java.lang.String r2 = "Failed to open bubble file"
            android.util.Log.e(r1, r2, r3)
            java.util.List r3 = kotlin.collections.CollectionsKt.emptyList()
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
