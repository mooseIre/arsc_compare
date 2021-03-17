package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.miui.systemui.animation.AutoCleanFloatTransitionListener;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: RowAnimationUtils.kt */
public final class RowAnimationUtils$startTouchAnimationIfNeed$1 extends AutoCleanFloatTransitionListener {
    final /* synthetic */ ExpandableNotificationRow $row;
    final /* synthetic */ float $scale;
    final /* synthetic */ String $scaleAnimName;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    RowAnimationUtils$startTouchAnimationIfNeed$1(float f, ExpandableNotificationRow expandableNotificationRow, String str, String str2, Object obj) {
        super(obj);
        this.$scale = f;
        this.$row = expandableNotificationRow;
        this.$scaleAnimName = str;
    }

    @Override // com.miui.systemui.animation.AutoCleanFloatTransitionListener
    public void onStart() {
        if (this.$scale != 1.0f) {
            RowAnimationUtils.access$setTouchAnimatingState(RowAnimationUtils.INSTANCE, this.$row, true);
        }
    }

    @Override // com.miui.systemui.animation.AutoCleanFloatTransitionListener
    public void onUpdate(@NotNull Map<String, Float> map) {
        Intrinsics.checkParameterIsNotNull(map, "infos");
        Float f = map.get(this.$scaleAnimName);
        if (f != null) {
            float floatValue = f.floatValue();
            ExpandableViewState viewState = this.$row.getViewState();
            if (viewState != null) {
                viewState.scaleX = floatValue;
                viewState.scaleY = floatValue;
            }
            this.$row.setScaleX(floatValue);
            this.$row.setScaleY(floatValue);
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Override // com.miui.systemui.animation.AutoCleanFloatTransitionListener
    public void onEnd() {
        if (this.$scale == 1.0f) {
            RowAnimationUtils.access$setTouchAnimatingState(RowAnimationUtils.INSTANCE, this.$row, false);
        }
        ExpandableNotificationRow expandableNotificationRow = this.$row;
        if (expandableNotificationRow instanceof MiuiExpandableNotificationRow) {
            ((MiuiExpandableNotificationRow) expandableNotificationRow).updateBackground();
        }
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onCancel(@Nullable Object obj) {
        super.onCancel(obj);
        RowAnimationUtils.access$setTouchAnimatingState(RowAnimationUtils.INSTANCE, this.$row, false);
    }
}
