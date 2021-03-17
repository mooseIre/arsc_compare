package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.view.View;
import com.android.systemui.controls.ControlsServiceInfo;

/* compiled from: AppAdapter.kt */
final class AppAdapter$onBindViewHolder$1 implements View.OnClickListener {
    final /* synthetic */ int $index;
    final /* synthetic */ AppAdapter this$0;

    AppAdapter$onBindViewHolder$1(AppAdapter appAdapter, int i) {
        this.this$0 = appAdapter;
        this.$index = i;
    }

    public final void onClick(View view) {
        this.this$0.onAppSelected.invoke(ComponentName.unflattenFromString(((ControlsServiceInfo) this.this$0.listOfServices.get(this.$index)).getKey()));
    }
}
