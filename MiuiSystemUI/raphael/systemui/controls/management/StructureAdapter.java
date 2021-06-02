package com.android.systemui.controls.management;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StructureAdapter.kt */
public final class StructureAdapter extends RecyclerView.Adapter<StructureHolder> {
    private final List<StructureContainer> models;

    public StructureAdapter(@NotNull List<StructureContainer> list) {
        Intrinsics.checkParameterIsNotNull(list, "models");
        this.models = list;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @NotNull
    public StructureHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(C0017R$layout.controls_structure_page, viewGroup, false);
        Intrinsics.checkExpressionValueIsNotNull(inflate, "layoutInflater.inflate(R…ture_page, parent, false)");
        return new StructureHolder(inflate);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.models.size();
    }

    public void onBindViewHolder(@NotNull StructureHolder structureHolder, int i) {
        Intrinsics.checkParameterIsNotNull(structureHolder, "holder");
        structureHolder.bind(this.models.get(i).getModel());
    }

    /* compiled from: StructureAdapter.kt */
    public static final class StructureHolder extends RecyclerView.ViewHolder {
        private final ControlAdapter controlAdapter;
        private final RecyclerView recyclerView;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public StructureHolder(@NotNull View view) {
            super(view);
            Intrinsics.checkParameterIsNotNull(view, "view");
            View requireViewById = this.itemView.requireViewById(C0015R$id.listAll);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById, "itemView.requireViewById…cyclerView>(R.id.listAll)");
            this.recyclerView = (RecyclerView) requireViewById;
            View view2 = this.itemView;
            Intrinsics.checkExpressionValueIsNotNull(view2, "itemView");
            Context context = view2.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "itemView.context");
            this.controlAdapter = new ControlAdapter(context.getResources().getFloat(C0012R$dimen.control_card_elevation));
            setUpRecyclerView();
        }

        public final void bind(@NotNull ControlsModel controlsModel) {
            Intrinsics.checkParameterIsNotNull(controlsModel, "model");
            this.controlAdapter.changeModel(controlsModel);
        }

        private final void setUpRecyclerView() {
            View view = this.itemView;
            Intrinsics.checkExpressionValueIsNotNull(view, "itemView");
            Context context = view.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "itemView.context");
            int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0012R$dimen.controls_card_margin);
            MarginItemDecorator marginItemDecorator = new MarginItemDecorator(dimensionPixelSize, dimensionPixelSize);
            RecyclerView recyclerView2 = this.recyclerView;
            recyclerView2.setAdapter(this.controlAdapter);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this.recyclerView.getContext(), 2);
            gridLayoutManager.setSpanSizeLookup(this.controlAdapter.getSpanSizeLookup());
            recyclerView2.setLayoutManager(gridLayoutManager);
            recyclerView2.addItemDecoration(marginItemDecorator);
        }
    }
}
