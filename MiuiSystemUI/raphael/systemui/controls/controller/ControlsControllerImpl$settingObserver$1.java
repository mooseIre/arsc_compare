package com.android.systemui.controls.controller;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
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
        if (!this.this$0.userChanging && i2 == this.this$0.getCurrentUserId()) {
            ControlsControllerImpl controlsControllerImpl = this.this$0;
            controlsControllerImpl.available = ControlsControllerImpl.Companion.isAvailable(controlsControllerImpl.getCurrentUserId(), this.this$0.getContentResolver());
            ControlsControllerImpl controlsControllerImpl2 = this.this$0;
            controlsControllerImpl2.resetFavorites(controlsControllerImpl2.getAvailable());
        }
    }
}
