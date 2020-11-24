package com.android.systemui.controls.management;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0014R$layout;
import com.android.systemui.controls.ControlInterface;
import java.util.List;
import kotlin.NoWhenBranchMatchedException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
public final class ControlAdapter extends RecyclerView.Adapter<Holder> {
    private final float elevation;
    /* access modifiers changed from: private */
    public ControlsModel model;
    @NotNull
    private final GridLayoutManager.SpanSizeLookup spanSizeLookup = new ControlAdapter$spanSizeLookup$1(this);

    public ControlAdapter(float f) {
        this.elevation = f;
    }

    @NotNull
    public final GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return this.spanSizeLookup;
    }

    @NotNull
    public Holder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        LayoutInflater from = LayoutInflater.from(viewGroup.getContext());
        if (i == 0) {
            View inflate = from.inflate(C0014R$layout.controls_zone_header, viewGroup, false);
            Intrinsics.checkExpressionValueIsNotNull(inflate, "layoutInflater.inflate(R…ne_header, parent, false)");
            return new ZoneHolder(inflate);
        } else if (i == 1) {
            View inflate2 = from.inflate(C0014R$layout.controls_base_item, viewGroup, false);
            inflate2.getLayoutParams().width = -1;
            inflate2.setElevation(this.elevation);
            inflate2.setBackground(viewGroup.getContext().getDrawable(C0010R$drawable.control_background_ripple));
            Intrinsics.checkExpressionValueIsNotNull(inflate2, "layoutInflater.inflate(R…le)\n                    }");
            ControlsModel controlsModel = this.model;
            return new ControlHolder(inflate2, controlsModel != null ? controlsModel.getMoveHelper() : null, new ControlAdapter$onCreateViewHolder$2(this));
        } else if (i == 2) {
            View inflate3 = from.inflate(C0014R$layout.controls_horizontal_divider_with_empty, viewGroup, false);
            Intrinsics.checkExpressionValueIsNotNull(inflate3, "layoutInflater.inflate(\n…ith_empty, parent, false)");
            return new DividerHolder(inflate3);
        } else {
            throw new IllegalStateException("Wrong viewType: " + i);
        }
    }

    public final void changeModel(@NotNull ControlsModel controlsModel) {
        Intrinsics.checkParameterIsNotNull(controlsModel, "model");
        this.model = controlsModel;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        List<ElementWrapper> elements;
        ControlsModel controlsModel = this.model;
        if (controlsModel == null || (elements = controlsModel.getElements()) == null) {
            return 0;
        }
        return elements.size();
    }

    public void onBindViewHolder(@NotNull Holder holder, int i) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            holder.bindData(controlsModel.getElements().get(i));
        }
    }

    public void onBindViewHolder(@NotNull Holder holder, int i, @NotNull List<Object> list) {
        Intrinsics.checkParameterIsNotNull(holder, "holder");
        Intrinsics.checkParameterIsNotNull(list, "payloads");
        if (list.isEmpty()) {
            super.onBindViewHolder(holder, i, list);
            return;
        }
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            ElementWrapper elementWrapper = controlsModel.getElements().get(i);
            if (elementWrapper instanceof ControlInterface) {
                holder.updateFavorite(((ControlInterface) elementWrapper).getFavorite());
            }
        }
    }

    public int getItemViewType(int i) {
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            ElementWrapper elementWrapper = controlsModel.getElements().get(i);
            if (elementWrapper instanceof ZoneNameWrapper) {
                return 0;
            }
            if ((elementWrapper instanceof ControlStatusWrapper) || (elementWrapper instanceof ControlInfoWrapper)) {
                return 1;
            }
            if (elementWrapper instanceof DividerWrapper) {
                return 2;
            }
            throw new NoWhenBranchMatchedException();
        }
        throw new IllegalStateException("Getting item type for null model");
    }
}
