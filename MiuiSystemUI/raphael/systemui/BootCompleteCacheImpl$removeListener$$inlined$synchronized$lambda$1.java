package com.android.systemui;

import com.android.systemui.BootCompleteCache;
import java.lang.ref.WeakReference;
import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BootCompleteCacheImpl.kt */
final class BootCompleteCacheImpl$removeListener$$inlined$synchronized$lambda$1<T> implements Predicate<WeakReference<BootCompleteCache.BootCompleteListener>> {
    final /* synthetic */ BootCompleteCache.BootCompleteListener $listener$inlined;

    BootCompleteCacheImpl$removeListener$$inlined$synchronized$lambda$1(BootCompleteCacheImpl bootCompleteCacheImpl, BootCompleteCache.BootCompleteListener bootCompleteListener) {
        this.$listener$inlined = bootCompleteListener;
    }

    public final boolean test(@NotNull WeakReference<BootCompleteCache.BootCompleteListener> weakReference) {
        Intrinsics.checkParameterIsNotNull(weakReference, "it");
        return weakReference.get() == null || weakReference.get() == this.$listener$inlined;
    }
}
