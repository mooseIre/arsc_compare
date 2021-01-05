package com.android.systemui;

import com.android.internal.annotations.GuardedBy;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BootCompleteCacheImpl.kt */
public final class BootCompleteCacheImpl implements BootCompleteCache, Dumpable {
    private final AtomicBoolean bootComplete = new AtomicBoolean(false);
    @GuardedBy({"listeners"})
    private final List<WeakReference<BootCompleteCache.BootCompleteListener>> listeners = new ArrayList();

    public BootCompleteCacheImpl(@NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        dumpManager.registerDumpable("BootCompleteCacheImpl", this);
    }

    public boolean isBootComplete() {
        return this.bootComplete.get();
    }

    public final void setBootComplete() {
        if (this.bootComplete.compareAndSet(false, true)) {
            synchronized (this.listeners) {
                for (WeakReference weakReference : this.listeners) {
                    BootCompleteCache.BootCompleteListener bootCompleteListener = (BootCompleteCache.BootCompleteListener) weakReference.get();
                    if (bootCompleteListener != null) {
                        bootCompleteListener.onBootComplete();
                    }
                }
                this.listeners.clear();
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    public boolean addListener(@NotNull BootCompleteCache.BootCompleteListener bootCompleteListener) {
        Intrinsics.checkParameterIsNotNull(bootCompleteListener, "listener");
        if (this.bootComplete.get()) {
            return true;
        }
        synchronized (this.listeners) {
            if (this.bootComplete.get()) {
                return true;
            }
            this.listeners.add(new WeakReference(bootCompleteListener));
            return false;
        }
    }

    public void removeListener(@NotNull BootCompleteCache.BootCompleteListener bootCompleteListener) {
        Intrinsics.checkParameterIsNotNull(bootCompleteListener, "listener");
        if (!this.bootComplete.get()) {
            synchronized (this.listeners) {
                this.listeners.removeIf(new BootCompleteCacheImpl$removeListener$$inlined$synchronized$lambda$1(this, bootCompleteListener));
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("BootCompleteCache state:");
        printWriter.println("  boot complete: " + isBootComplete());
        if (!isBootComplete()) {
            printWriter.println("  listeners:");
            synchronized (this.listeners) {
                for (WeakReference weakReference : this.listeners) {
                    printWriter.println("    " + weakReference);
                }
                Unit unit = Unit.INSTANCE;
            }
        }
    }
}
