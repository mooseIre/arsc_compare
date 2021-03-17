package com.android.systemui.util.leak;

import android.os.Build;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;

public class LeakDetector implements Dumpable {
    public static final boolean ENABLED = Build.IS_DEBUGGABLE;
    private final TrackedCollections mTrackedCollections;
    private final TrackedGarbage mTrackedGarbage;
    private final TrackedObjects mTrackedObjects;

    @VisibleForTesting
    public LeakDetector(TrackedCollections trackedCollections, TrackedGarbage trackedGarbage, TrackedObjects trackedObjects) {
        this.mTrackedCollections = trackedCollections;
        this.mTrackedGarbage = trackedGarbage;
        this.mTrackedObjects = trackedObjects;
    }

    public <T> void trackInstance(T t) {
        TrackedObjects trackedObjects = this.mTrackedObjects;
        if (trackedObjects != null) {
            trackedObjects.track(t);
        }
    }

    public <T> void trackCollection(Collection<T> collection, String str) {
        TrackedCollections trackedCollections = this.mTrackedCollections;
        if (trackedCollections != null) {
            trackedCollections.track(collection, str);
        }
    }

    public void trackGarbage(Object obj) {
        TrackedGarbage trackedGarbage = this.mTrackedGarbage;
        if (trackedGarbage != null) {
            trackedGarbage.track(obj);
        }
    }

    /* access modifiers changed from: package-private */
    public TrackedGarbage getTrackedGarbage() {
        return this.mTrackedGarbage;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        PrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.println("SYSUI LEAK DETECTOR");
        indentingPrintWriter.increaseIndent();
        if (this.mTrackedCollections == null || this.mTrackedGarbage == null) {
            indentingPrintWriter.println("disabled");
        } else {
            indentingPrintWriter.println("TrackedCollections:");
            indentingPrintWriter.increaseIndent();
            this.mTrackedCollections.dump(indentingPrintWriter, $$Lambda$LeakDetector$pWx7s0HACocvPZyQWmuD0rk2VO8.INSTANCE);
            indentingPrintWriter.decreaseIndent();
            indentingPrintWriter.println();
            indentingPrintWriter.println("TrackedObjects:");
            indentingPrintWriter.increaseIndent();
            this.mTrackedCollections.dump(indentingPrintWriter, $$Lambda$oUbBhMkDSLCrT89WHUZWOlE1TKs.INSTANCE);
            indentingPrintWriter.decreaseIndent();
            indentingPrintWriter.println();
            indentingPrintWriter.print("TrackedGarbage:");
            indentingPrintWriter.increaseIndent();
            this.mTrackedGarbage.dump(indentingPrintWriter);
            indentingPrintWriter.decreaseIndent();
        }
        indentingPrintWriter.decreaseIndent();
        indentingPrintWriter.println();
    }

    static /* synthetic */ boolean lambda$dump$0(Collection collection) {
        return !TrackedObjects.isTrackedObject(collection);
    }

    public static LeakDetector create() {
        if (!ENABLED) {
            return new LeakDetector(null, null, null);
        }
        TrackedCollections trackedCollections = new TrackedCollections();
        return new LeakDetector(trackedCollections, new TrackedGarbage(trackedCollections), new TrackedObjects(trackedCollections));
    }
}
