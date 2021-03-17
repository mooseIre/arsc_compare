package com.android.systemui.util;

import android.os.UserHandle;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: UserAwareController.kt */
public interface UserAwareController {
    default void changeUser(@NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(userHandle, "newUser");
    }

    int getCurrentUserId();
}
