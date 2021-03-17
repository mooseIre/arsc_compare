package com.android.systemui.controls.controller;

import android.content.Context;
import android.os.Environment;
import android.os.UserHandle;
import java.io.File;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class UserStructure {
    private final File auxiliaryFile;
    private final File file;
    private final Context userContext;

    public UserStructure(@NotNull Context context, @NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        Context createContextAsUser = context.createContextAsUser(userHandle, 0);
        this.userContext = createContextAsUser;
        Intrinsics.checkExpressionValueIsNotNull(createContextAsUser, "userContext");
        this.file = Environment.buildPath(createContextAsUser.getFilesDir(), new String[]{"controls_favorites.xml"});
        Context context2 = this.userContext;
        Intrinsics.checkExpressionValueIsNotNull(context2, "userContext");
        this.auxiliaryFile = Environment.buildPath(context2.getFilesDir(), new String[]{"aux_controls_favorites.xml"});
    }

    public final Context getUserContext() {
        return this.userContext;
    }

    public final File getFile() {
        return this.file;
    }

    public final File getAuxiliaryFile() {
        return this.auxiliaryFile;
    }
}
