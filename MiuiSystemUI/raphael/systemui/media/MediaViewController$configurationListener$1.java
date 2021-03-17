package com.android.systemui.media;

import android.content.res.Configuration;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.animation.TransitionLayout;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaViewController.kt */
public final class MediaViewController$configurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ MediaViewController this$0;

    MediaViewController$configurationListener$1(MediaViewController mediaViewController) {
        this.this$0 = mediaViewController;
    }

    public void onConfigChanged(@Nullable Configuration configuration) {
        if (configuration != null) {
            TransitionLayout access$getTransitionLayout$p = this.this$0.transitionLayout;
            if (access$getTransitionLayout$p == null || access$getTransitionLayout$p.getRawLayoutDirection() != configuration.getLayoutDirection()) {
                TransitionLayout access$getTransitionLayout$p2 = this.this$0.transitionLayout;
                if (access$getTransitionLayout$p2 != null) {
                    access$getTransitionLayout$p2.setLayoutDirection(configuration.getLayoutDirection());
                }
                this.this$0.refreshState();
            }
        }
    }
}
