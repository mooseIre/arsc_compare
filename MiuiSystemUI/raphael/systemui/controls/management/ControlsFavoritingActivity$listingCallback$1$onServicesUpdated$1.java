package com.android.systemui.controls.management;

/* compiled from: ControlsFavoritingActivity.kt */
final class ControlsFavoritingActivity$listingCallback$1$onServicesUpdated$1 implements Runnable {
    final /* synthetic */ ControlsFavoritingActivity$listingCallback$1 this$0;

    ControlsFavoritingActivity$listingCallback$1$onServicesUpdated$1(ControlsFavoritingActivity$listingCallback$1 controlsFavoritingActivity$listingCallback$1) {
        this.this$0 = controlsFavoritingActivity$listingCallback$1;
    }

    public final void run() {
        ControlsFavoritingActivity.access$getOtherAppsButton$p(this.this$0.this$0).setVisibility(0);
    }
}
