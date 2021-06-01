package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.res.Resources;
import com.android.systemui.C0019R$plurals;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppAdapter.kt */
public final class FavoritesRenderer {
    private final Function1<ComponentName, Integer> favoriteFunction;
    private final Resources resources;

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.jvm.functions.Function1<? super android.content.ComponentName, java.lang.Integer> */
    /* JADX WARN: Multi-variable type inference failed */
    public FavoritesRenderer(@NotNull Resources resources2, @NotNull Function1<? super ComponentName, Integer> function1) {
        Intrinsics.checkParameterIsNotNull(resources2, "resources");
        Intrinsics.checkParameterIsNotNull(function1, "favoriteFunction");
        this.resources = resources2;
        this.favoriteFunction = function1;
    }

    @Nullable
    public final String renderFavoritesForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        int intValue = this.favoriteFunction.invoke(componentName).intValue();
        if (intValue == 0) {
            return null;
        }
        return this.resources.getQuantityString(C0019R$plurals.controls_number_of_favorites, intValue, Integer.valueOf(intValue));
    }
}
