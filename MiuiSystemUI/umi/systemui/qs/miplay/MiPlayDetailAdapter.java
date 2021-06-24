package com.android.systemui.qs.miplay;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.controls.MiPlayPluginManager;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.MiuiQSDetailItems;

public class MiPlayDetailAdapter implements DetailAdapter, MiuiQSDetailItems.Callback {
    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public int getContainerHeight() {
        return -2;
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public int getMetricsCategory() {
        return 1168;
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public Intent getSettingsIntent() {
        return null;
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public CharSequence getTitle() {
        return "MiPlay";
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public boolean getToggleEnabled() {
        return true;
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public boolean hasHeader() {
        return false;
    }

    @Override // com.android.systemui.qs.MiuiQSDetailItems.Callback
    public void onDetailItemClick(MiuiQSDetailItems.Item item) {
    }

    @Override // com.android.systemui.qs.MiuiQSDetailItems.Callback
    public void onDetailItemDisconnect(MiuiQSDetailItems.Item item) {
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public void setToggleState(boolean z) {
    }

    public View createContentView() {
        return ((MiPlayPluginManager) Dependency.get(MiPlayPluginManager.class)).getMiPlayDetailView();
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public Boolean getToggleState() {
        return Boolean.FALSE;
    }

    @Override // com.android.systemui.plugins.qs.DetailAdapter
    public View createDetailView(Context context, View view, ViewGroup viewGroup) {
        return view != null ? view : createContentView();
    }
}
