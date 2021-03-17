package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.util.ConvenienceExtensionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationStackScrollLayoutExt.kt */
public final class NotificationStackScrollLayoutExtKt {
    public static final void setPanelStretching(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, boolean z) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$setPanelStretching");
        if (isPanelStretching(notificationStackScrollLayout) && !z) {
            notificationStackScrollLayout.getAnimationEvents().add(new SpringAnimationEvent(getVisibleChildCount(notificationStackScrollLayout)));
            notificationStackScrollLayout.requestAnimation();
            notificationStackScrollLayout.requestChildrenUpdate();
        }
        if (!isPanelStretching(notificationStackScrollLayout) && z) {
            for (View view : ConvenienceExtensionsKt.getChildren(notificationStackScrollLayout)) {
                MiuiNotificationAnimations miuiNotificationAnimations = MiuiNotificationAnimations.INSTANCE;
                Intrinsics.checkExpressionValueIsNotNull(view, "it");
                miuiNotificationAnimations.cancelSpringAnimations(view);
            }
        }
        notificationStackScrollLayout.getAmbientState().setPanelStretching(z);
    }

    public static final boolean isPanelStretching(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$isPanelStretching");
        return notificationStackScrollLayout.getAmbientState().getPanelStretching();
    }

    public static final void generateHeadsUpChildrenPositionAnimation(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$generateHeadsUpChildrenPositionAnimation");
        for (ExpandableView expandableView : SequencesKt___SequencesKt.map(SequencesKt___SequencesKt.filter(ConvenienceExtensionsKt.getChildren(notificationStackScrollLayout), NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$1.INSTANCE), NotificationStackScrollLayoutExtKt$generateHeadsUpChildrenPositionAnimation$2.INSTANCE)) {
            notificationStackScrollLayout.getAnimationEvents().add(new HeadsUpPositionEvent(expandableView));
        }
    }

    public static final void setPanelAppeared(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, boolean z, boolean z2) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$setPanelAppeared");
        if (notificationStackScrollLayout.getAmbientState().getPanelAppeared() != z) {
            if (!z2) {
                notificationStackScrollLayout.getAnimationEvents().add(new PanelAppearDisappearEvent());
                notificationStackScrollLayout.requestAnimation();
            }
            notificationStackScrollLayout.requestChildrenUpdate();
            for (ExpandableNotificationRow expandableNotificationRow : SequencesKt___SequencesKt.map(SequencesKt___SequencesKt.filter(ConvenienceExtensionsKt.getChildren(notificationStackScrollLayout), NotificationStackScrollLayoutExtKt$setPanelAppeared$1.INSTANCE), NotificationStackScrollLayoutExtKt$setPanelAppeared$2.INSTANCE)) {
                expandableNotificationRow.cancelAppearDrawing();
            }
        }
        notificationStackScrollLayout.getAmbientState().setPanelAppeared(z);
    }

    public static final boolean isPanelAppeared(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$isPanelAppeared");
        return notificationStackScrollLayout.getAmbientState().getPanelAppeared();
    }

    public static final void setPanelStretchingFromHeadsUp(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, boolean z) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$setPanelStretchingFromHeadsUp");
        notificationStackScrollLayout.getAmbientState().setPanelStretchingFromHeadsUp(z);
    }

    public static final void onSpringLengthUpdated(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, float f) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$onSpringLengthUpdated");
        if (notificationStackScrollLayout.getAmbientState().getPanelStretching()) {
            notificationStackScrollLayout.getAmbientState().setSpringLength(f);
            notificationStackScrollLayout.requestChildrenUpdate();
        }
    }

    public static final void updateStackScrollLayoutHeight(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$updateStackScrollLayoutHeight");
        notificationStackScrollLayout.getAmbientState().setStackScrollLayoutHeight(notificationStackScrollLayout.getMeasuredHeight());
    }

    private static final int getVisibleChildCount(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        StackScrollAlgorithm stackScrollAlgorithm = notificationStackScrollLayout.mStackScrollAlgorithm;
        if (stackScrollAlgorithm instanceof MiuiStackScrollAlgorithm) {
            return ((MiuiStackScrollAlgorithm) stackScrollAlgorithm).getLatestVisibleChildCount();
        }
        return 0;
    }

    public static final void setQsExpansionEnabled(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, boolean z) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$setQsExpansionEnabled");
        notificationStackScrollLayout.getAmbientState().setQsExpansionEnabled(z);
    }

    public static final void setNCSwitching(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, boolean z) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$setNCSwitching");
        notificationStackScrollLayout.getAmbientState().setNCSwitching(z);
    }

    public static final void setStaticTopPadding(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, int i) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "$this$setStaticTopPadding");
        notificationStackScrollLayout.getAmbientState().setStaticTopPadding(i);
    }
}
