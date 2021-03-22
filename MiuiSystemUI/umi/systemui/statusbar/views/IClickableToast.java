package com.android.systemui.statusbar.views;

import android.view.View;
import org.jetbrains.annotations.NotNull;

/* compiled from: ClickableToast.kt */
public interface IClickableToast {
    @NotNull
    IClickableToast setClickListener(@NotNull View.OnClickListener onClickListener);

    @NotNull
    IClickableToast setText(@NotNull CharSequence charSequence);

    void show();
}
