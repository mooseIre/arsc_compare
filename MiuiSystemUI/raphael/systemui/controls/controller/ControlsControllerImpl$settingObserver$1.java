package com.android.systemui.controls.controller;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import java.util.Collection;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$settingObserver$1 extends ContentObserver {
    final /* synthetic */ ControlsControllerImpl this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlsControllerImpl$settingObserver$1(ControlsControllerImpl controlsControllerImpl, Handler handler) {
        super(handler);
        this.this$0 = controlsControllerImpl;
    }

    public void onChange(boolean z, @NotNull Collection<? extends Uri> collection, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(collection, "uris");
        if (!ControlsControllerImpl.access$getUserChanging$p(this.this$0) && i2 == this.this$0.getCurrentUserId()) {
            ControlsControllerImpl controlsControllerImpl = this.this$0;
            ControlsControllerImpl.access$setAvailable$p(controlsControllerImpl, ControlsControllerImpl.Companion.access$isAvailable(ControlsControllerImpl.Companion, controlsControllerImpl.getCurrentUserId(), ControlsControllerImpl.access$getContentResolver$p(this.this$0)));
            ControlsControllerImpl controlsControllerImpl2 = this.this$0;
            ControlsControllerImpl.access$resetFavorites(controlsControllerImpl2, controlsControllerImpl2.getAvailable());
        }
    }
}
