package com.android.systemui.media;

import com.android.systemui.media.MediaDeviceManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.BiConsumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaDeviceManager.kt */
final class MediaDeviceManager$dump$$inlined$with$lambda$1<T, U> implements BiConsumer<String, MediaDeviceManager.Token> {
    final /* synthetic */ String[] $args$inlined;
    final /* synthetic */ FileDescriptor $fd$inlined;
    final /* synthetic */ PrintWriter $pw$inlined;
    final /* synthetic */ PrintWriter $this_with;

    MediaDeviceManager$dump$$inlined$with$lambda$1(PrintWriter printWriter, MediaDeviceManager mediaDeviceManager, FileDescriptor fileDescriptor, PrintWriter printWriter2, String[] strArr) {
        this.$this_with = printWriter;
        this.$fd$inlined = fileDescriptor;
        this.$pw$inlined = printWriter2;
        this.$args$inlined = strArr;
    }

    public final void accept(@NotNull String str, @NotNull MediaDeviceManager.Token token) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(token, "entry");
        PrintWriter printWriter = this.$this_with;
        printWriter.println("  key=" + str);
        token.dump(this.$fd$inlined, this.$pw$inlined, this.$args$inlined);
    }
}
