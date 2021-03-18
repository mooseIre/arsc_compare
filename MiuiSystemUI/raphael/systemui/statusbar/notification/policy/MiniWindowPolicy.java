package com.android.systemui.statusbar.notification.policy;

import android.content.ComponentName;
import android.content.Intent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class MiniWindowPolicy {
    public static final MiniWindowPolicy INSTANCE = new MiniWindowPolicy();

    private MiniWindowPolicy() {
    }

    public final void initializeMiniWindowIntent(@NotNull String str, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (!Intrinsics.areEqual("com.tencent.tim", str)) {
            intent.addFlags(134217728);
            intent.addFlags(268435456);
            intent.addFlags(8388608);
        }
    }

    public final boolean canSlidePackage(@Nullable String str, @Nullable Intent intent, @Nullable String str2, @Nullable ComponentName componentName, boolean z) {
        Object obj = null;
        if (Intrinsics.areEqual("com.tencent.mm", str)) {
            if (intent != null) {
                obj = intent.getComponent();
            }
            if (obj == null || isTopSameClass(intent, componentName)) {
                return false;
            }
            if (!isTopSamePackage(intent, componentName) || !z) {
                return true;
            }
            return false;
        } else if (Intrinsics.areEqual(str, str2)) {
            return false;
        } else {
            if (componentName != null) {
                obj = componentName.getPackageName();
            }
            if (Intrinsics.areEqual(str, obj)) {
                return false;
            }
            return true;
        }
    }

    private final boolean isTopSameClass(Intent intent, ComponentName componentName) {
        if (intent == null || intent.getComponent() == null || componentName == null) {
            return false;
        }
        ComponentName component = intent.getComponent();
        if (component != null) {
            return Intrinsics.areEqual(component.getClassName(), componentName.getClassName());
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final boolean isTopSamePackage(Intent intent, ComponentName componentName) {
        if (intent == null || intent.getComponent() == null || componentName == null) {
            return false;
        }
        ComponentName component = intent.getComponent();
        if (component != null) {
            return Intrinsics.areEqual(component.getPackageName(), componentName.getPackageName());
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
