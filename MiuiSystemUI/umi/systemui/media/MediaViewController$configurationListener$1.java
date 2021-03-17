package com.android.systemui.media;

import android.content.res.Configuration;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.animation.TransitionLayout;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaViewController.kt */
public final class MediaViewController$configurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ MediaViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaViewController$configurationListener$1(MediaViewController mediaViewController) {
        this.this$0 = mediaViewController;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(@Nullable Configuration configuration) {
        if (configuration != null) {
            TransitionLayout transitionLayout = this.this$0.transitionLayout;
            if (transitionLayout == null || transitionLayout.getRawLayoutDirection() != configuration.getLayoutDirection()) {
                TransitionLayout transitionLayout2 = this.this$0.transitionLayout;
                if (transitionLayout2 != null) {
                    transitionLayout2.setLayoutDirection(configuration.getLayoutDirection());
                }
                this.this$0.refreshState();
            }
        }
    }
}
