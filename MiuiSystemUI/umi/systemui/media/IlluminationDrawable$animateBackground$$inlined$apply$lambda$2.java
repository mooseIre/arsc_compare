package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import org.jetbrains.annotations.Nullable;

/* compiled from: IlluminationDrawable.kt */
public final class IlluminationDrawable$animateBackground$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ IlluminationDrawable this$0;

    IlluminationDrawable$animateBackground$$inlined$apply$lambda$2(IlluminationDrawable illuminationDrawable, int i, int i2, int i3) {
        this.this$0 = illuminationDrawable;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        IlluminationDrawable.access$setBackgroundAnimation$p(this.this$0, null);
    }
}
