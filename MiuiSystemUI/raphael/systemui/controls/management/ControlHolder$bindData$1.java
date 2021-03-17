package com.android.systemui.controls.management;

import android.view.View;
import com.android.systemui.controls.ControlInterface;

/* compiled from: ControlAdapter.kt */
final class ControlHolder$bindData$1 implements View.OnClickListener {
    final /* synthetic */ ElementWrapper $wrapper;
    final /* synthetic */ ControlHolder this$0;

    ControlHolder$bindData$1(ControlHolder controlHolder, ElementWrapper elementWrapper) {
        this.this$0 = controlHolder;
        this.$wrapper = elementWrapper;
    }

    public final void onClick(View view) {
        ControlHolder controlHolder = this.this$0;
        controlHolder.updateFavorite(!controlHolder.favorite.isChecked());
        this.this$0.getFavoriteCallback().invoke(((ControlInterface) this.$wrapper).getControlId(), Boolean.valueOf(this.this$0.favorite.isChecked()));
    }
}
