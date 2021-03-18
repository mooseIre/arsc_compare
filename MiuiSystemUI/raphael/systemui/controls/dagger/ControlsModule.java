package com.android.systemui.controls.dagger;

import android.content.pm.PackageManager;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsModule.kt */
public abstract class ControlsModule {
    public static final Companion Companion = new Companion(null);

    public static final boolean providesControlsFeatureEnabled(@NotNull PackageManager packageManager) {
        return Companion.providesControlsFeatureEnabled(packageManager);
    }

    /* compiled from: ControlsModule.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final boolean providesControlsFeatureEnabled(@NotNull PackageManager packageManager) {
            Intrinsics.checkParameterIsNotNull(packageManager, "pm");
            return packageManager.hasSystemFeature("android.software.controls");
        }
    }
}
