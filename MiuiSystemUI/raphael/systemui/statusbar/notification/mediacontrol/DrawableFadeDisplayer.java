package com.android.systemui.statusbar.notification.mediacontrol;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DrawableFadeDisplayer.kt */
public final class DrawableFadeDisplayer {
    public static final Companion Companion = new Companion(null);

    /* compiled from: DrawableFadeDisplayer.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final void display(@NotNull Bitmap bitmap, @NotNull ImageView imageView, boolean z) {
            Intrinsics.checkParameterIsNotNull(bitmap, "bitmap");
            Intrinsics.checkParameterIsNotNull(imageView, "imageView");
            Drawable drawable = imageView.getDrawable();
            if (drawable == null) {
                drawable = new ColorDrawable(0);
            } else if (drawable instanceof TransitionDrawable) {
                TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
                drawable = transitionDrawable.getDrawable(transitionDrawable.getNumberOfLayers() - 1);
            }
            TransitionDrawable transitionDrawable2 = new TransitionDrawable(new Drawable[]{drawable, new BitmapDrawable(bitmap)});
            imageView.setImageDrawable(transitionDrawable2);
            transitionDrawable2.setCrossFadeEnabled(z);
            transitionDrawable2.startTransition(370);
        }
    }
}
