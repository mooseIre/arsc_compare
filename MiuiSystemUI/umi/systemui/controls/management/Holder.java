package com.android.systemui.controls.management;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
public abstract class Holder extends RecyclerView.ViewHolder {
    public abstract void bindData(@NotNull ElementWrapper elementWrapper);

    public void updateFavorite(boolean z) {
    }

    private Holder(View view) {
        super(view);
    }

    public /* synthetic */ Holder(View view, DefaultConstructorMarker defaultConstructorMarker) {
        this(view);
    }
}
