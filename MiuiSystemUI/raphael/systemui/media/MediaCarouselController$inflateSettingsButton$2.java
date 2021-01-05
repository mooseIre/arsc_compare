package com.android.systemui.media;

import android.view.View;

/* compiled from: MediaCarouselController.kt */
final class MediaCarouselController$inflateSettingsButton$2 implements View.OnClickListener {
    final /* synthetic */ MediaCarouselController this$0;

    MediaCarouselController$inflateSettingsButton$2(MediaCarouselController mediaCarouselController) {
        this.this$0 = mediaCarouselController;
    }

    public final void onClick(View view) {
        this.this$0.activityStarter.startActivity(MediaCarouselControllerKt.settingsIntent, true);
    }
}
