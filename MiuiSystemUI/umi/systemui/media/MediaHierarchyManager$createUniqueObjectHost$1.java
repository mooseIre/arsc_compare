package com.android.systemui.media;

import android.view.View;
import android.view.ViewGroupOverlay;
import android.view.ViewOverlay;
import android.view.ViewRootImpl;
import com.android.systemui.util.animation.UniqueObjectHostView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHierarchyManager.kt */
public final class MediaHierarchyManager$createUniqueObjectHost$1 implements View.OnAttachStateChangeListener {
    final /* synthetic */ UniqueObjectHostView $viewHost;
    final /* synthetic */ MediaHierarchyManager this$0;

    public void onViewDetachedFromWindow(@Nullable View view) {
    }

    MediaHierarchyManager$createUniqueObjectHost$1(MediaHierarchyManager mediaHierarchyManager, UniqueObjectHostView uniqueObjectHostView) {
        this.this$0 = mediaHierarchyManager;
        this.$viewHost = uniqueObjectHostView;
    }

    public void onViewAttachedToWindow(@Nullable View view) {
        if (this.this$0.rootOverlay == null) {
            MediaHierarchyManager mediaHierarchyManager = this.this$0;
            ViewRootImpl viewRootImpl = this.$viewHost.getViewRootImpl();
            Intrinsics.checkExpressionValueIsNotNull(viewRootImpl, "viewHost.viewRootImpl");
            mediaHierarchyManager.rootView = viewRootImpl.getView();
            MediaHierarchyManager mediaHierarchyManager2 = this.this$0;
            View view2 = mediaHierarchyManager2.rootView;
            if (view2 != null) {
                ViewOverlay overlay = view2.getOverlay();
                if (overlay != null) {
                    mediaHierarchyManager2.rootOverlay = (ViewGroupOverlay) overlay;
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroupOverlay");
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        this.$viewHost.removeOnAttachStateChangeListener(this);
    }
}
