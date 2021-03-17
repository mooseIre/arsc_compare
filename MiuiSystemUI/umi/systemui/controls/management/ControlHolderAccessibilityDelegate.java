package com.android.systemui.controls.management;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.controls.management.ControlsModel;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ControlAdapter.kt */
public final class ControlHolderAccessibilityDelegate extends AccessibilityDelegateCompat {
    private static final int MOVE_AFTER_ID = C0015R$id.accessibility_action_controls_move_after;
    private static final int MOVE_BEFORE_ID = C0015R$id.accessibility_action_controls_move_before;
    private boolean isFavorite;
    @Nullable
    private final ControlsModel.MoveHelper moveHelper;
    @NotNull
    private final Function0<Integer> positionRetriever;
    @NotNull
    private final Function1<Boolean, CharSequence> stateRetriever;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: kotlin.jvm.functions.Function1<? super java.lang.Boolean, ? extends java.lang.CharSequence> */
    /* JADX WARN: Multi-variable type inference failed */
    public ControlHolderAccessibilityDelegate(@NotNull Function1<? super Boolean, ? extends CharSequence> function1, @NotNull Function0<Integer> function0, @Nullable ControlsModel.MoveHelper moveHelper2) {
        Intrinsics.checkParameterIsNotNull(function1, "stateRetriever");
        Intrinsics.checkParameterIsNotNull(function0, "positionRetriever");
        this.stateRetriever = function1;
        this.positionRetriever = function0;
        this.moveHelper = moveHelper2;
    }

    public final void setFavorite(boolean z) {
        this.isFavorite = z;
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public void onInitializeAccessibilityNodeInfo(@NotNull View view, @NotNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        Intrinsics.checkParameterIsNotNull(view, "host");
        Intrinsics.checkParameterIsNotNull(accessibilityNodeInfoCompat, "info");
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
        accessibilityNodeInfoCompat.setContextClickable(false);
        addClickAction(view, accessibilityNodeInfoCompat);
        maybeAddMoveBeforeAction(view, accessibilityNodeInfoCompat);
        maybeAddMoveAfterAction(view, accessibilityNodeInfoCompat);
        accessibilityNodeInfoCompat.setStateDescription(this.stateRetriever.invoke(Boolean.valueOf(this.isFavorite)));
        accessibilityNodeInfoCompat.setCollectionItemInfo(null);
        accessibilityNodeInfoCompat.setClassName(Switch.class.getName());
    }

    @Override // androidx.core.view.AccessibilityDelegateCompat
    public boolean performAccessibilityAction(@Nullable View view, int i, @Nullable Bundle bundle) {
        if (super.performAccessibilityAction(view, i, bundle)) {
            return true;
        }
        if (i == MOVE_BEFORE_ID) {
            ControlsModel.MoveHelper moveHelper2 = this.moveHelper;
            if (moveHelper2 == null) {
                return true;
            }
            moveHelper2.moveBefore(this.positionRetriever.invoke().intValue());
            return true;
        } else if (i != MOVE_AFTER_ID) {
            return false;
        } else {
            ControlsModel.MoveHelper moveHelper3 = this.moveHelper;
            if (moveHelper3 == null) {
                return true;
            }
            moveHelper3.moveAfter(this.positionRetriever.invoke().intValue());
            return true;
        }
    }

    private final void addClickAction(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        String str;
        if (this.isFavorite) {
            str = view.getContext().getString(C0021R$string.accessibility_control_change_unfavorite);
        } else {
            str = view.getContext().getString(C0021R$string.accessibility_control_change_favorite);
        }
        accessibilityNodeInfoCompat.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(16, str));
    }

    private final void maybeAddMoveBeforeAction(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        ControlsModel.MoveHelper moveHelper2 = this.moveHelper;
        if (moveHelper2 != null ? moveHelper2.canMoveBefore(this.positionRetriever.invoke().intValue()) : false) {
            accessibilityNodeInfoCompat.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(MOVE_BEFORE_ID, view.getContext().getString(C0021R$string.accessibility_control_move, Integer.valueOf((this.positionRetriever.invoke().intValue() + 1) - 1))));
            accessibilityNodeInfoCompat.setContextClickable(true);
        }
    }

    private final void maybeAddMoveAfterAction(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        ControlsModel.MoveHelper moveHelper2 = this.moveHelper;
        if (moveHelper2 != null ? moveHelper2.canMoveAfter(this.positionRetriever.invoke().intValue()) : false) {
            accessibilityNodeInfoCompat.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(MOVE_AFTER_ID, view.getContext().getString(C0021R$string.accessibility_control_move, Integer.valueOf(this.positionRetriever.invoke().intValue() + 1 + 1))));
            accessibilityNodeInfoCompat.setContextClickable(true);
        }
    }
}
