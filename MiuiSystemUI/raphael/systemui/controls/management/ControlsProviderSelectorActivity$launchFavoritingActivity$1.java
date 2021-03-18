package com.android.systemui.controls.management;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Pair;

/* access modifiers changed from: package-private */
/* compiled from: ControlsProviderSelectorActivity.kt */
public final class ControlsProviderSelectorActivity$launchFavoritingActivity$1 implements Runnable {
    final /* synthetic */ ComponentName $component;
    final /* synthetic */ ControlsProviderSelectorActivity this$0;

    ControlsProviderSelectorActivity$launchFavoritingActivity$1(ControlsProviderSelectorActivity controlsProviderSelectorActivity, ComponentName componentName) {
        this.this$0 = controlsProviderSelectorActivity;
        this.$component = componentName;
    }

    public final void run() {
        ComponentName componentName = this.$component;
        if (componentName != null) {
            Intent intent = new Intent(this.this$0.getApplicationContext(), ControlsFavoritingActivity.class);
            intent.putExtra("extra_app_label", this.this$0.listingController.getAppLabel(componentName));
            intent.putExtra("android.intent.extra.COMPONENT_NAME", componentName);
            intent.putExtra("extra_from_provider_selector", true);
            ControlsProviderSelectorActivity controlsProviderSelectorActivity = this.this$0;
            controlsProviderSelectorActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(controlsProviderSelectorActivity, new Pair[0]).toBundle());
        }
    }
}
