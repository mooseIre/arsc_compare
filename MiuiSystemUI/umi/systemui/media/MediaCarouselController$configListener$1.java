package com.android.systemui.media;

import android.content.res.Configuration;
import com.android.systemui.statusbar.policy.ConfigurationController;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselController.kt */
public final class MediaCarouselController$configListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ MediaCarouselController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaCarouselController$configListener$1(MediaCarouselController mediaCarouselController) {
        this.this$0 = mediaCarouselController;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        this.this$0.recreatePlayers();
        this.this$0.inflateSettingsButton();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        this.this$0.inflateSettingsButton();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(@Nullable Configuration configuration) {
        if (configuration != null) {
            MediaCarouselController mediaCarouselController = this.this$0;
            boolean z = true;
            if (configuration.getLayoutDirection() != 1) {
                z = false;
            }
            mediaCarouselController.setRtl(z);
        }
    }
}
