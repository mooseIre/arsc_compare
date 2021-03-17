package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationConversationTemplateViewWrapper.kt */
public final class MiuiNotificationConversationTemplateViewWrapper extends NotificationViewWrapper {
    private View actionsContainer;
    private final ConversationLayout conversationLayout;
    private View expandButtonContainer;
    private View expandButtonInnerContainer;
    private int mContentHeight;
    private int mMinHeightHint;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationConversationTemplateViewWrapper(@NotNull Context context, @NotNull View view, @NotNull ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        Intrinsics.checkParameterIsNotNull(context, "ctx");
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        this.conversationLayout = (ConversationLayout) view;
    }

    private final void resolveViews() {
        ConversationLayout conversationLayout2 = this.conversationLayout;
        Intrinsics.checkExpressionValueIsNotNull(conversationLayout2.requireViewById(16908957), "requireViewById(com.andr…ernal.R.id.expand_button)");
        View requireViewById = conversationLayout2.requireViewById(16908959);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(com.andr….expand_button_container)");
        this.expandButtonContainer = requireViewById;
        View requireViewById2 = conversationLayout2.requireViewById(16908960);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(com.andr…d_button_inner_container)");
        this.expandButtonInnerContainer = requireViewById2;
        View findViewById = this.mView.findViewById(16908724);
        this.actionsContainer = findViewById;
        if (findViewById == null) {
            this.actionsContainer = this.mView.findViewById(C0015R$id.actions_container);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        View view = this.mView;
        Intrinsics.checkExpressionValueIsNotNull(view, "mView");
        view.setAlpha(z ? 1.0f : 0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setContentHeight(int i, int i2) {
        super.setContentHeight(i, i2);
        this.mContentHeight = i;
        this.mMinHeightHint = i2;
        updateActionOffset();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean shouldClipToRounding(boolean z, boolean z2) {
        View view;
        if (super.shouldClipToRounding(z, z2)) {
            return true;
        }
        if (!z2 || ((view = this.actionsContainer) != null && view.getVisibility() == 8)) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(@NotNull ExpandableNotificationRow expandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        resolveViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    @NotNull
    public View getExpandButton() {
        View view = this.expandButtonInnerContainer;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandButtonInnerContainer");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void updateExpandability(boolean z, @Nullable View.OnClickListener onClickListener) {
        this.conversationLayout.updateExpandability(z, onClickListener);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean disallowSingleClick(float f, float f2) {
        boolean z;
        View view = this.expandButtonContainer;
        if (view != null) {
            if (view.getVisibility() == 0) {
                View view2 = this.expandButtonContainer;
                if (view2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("expandButtonContainer");
                    throw null;
                } else if (isOnView(view2, f, f2)) {
                    z = true;
                    return z || super.disallowSingleClick(f, f2);
                }
            }
            z = false;
            if (z) {
                return true;
            }
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandButtonContainer");
        throw null;
    }

    private final void updateActionOffset() {
        if (this.actionsContainer != null) {
            int max = Math.max(this.mContentHeight, this.mMinHeightHint);
            View view = this.actionsContainer;
            if (view != null) {
                View view2 = this.mView;
                Intrinsics.checkExpressionValueIsNotNull(view2, "mView");
                view.setTranslationY((float) (max - view2.getHeight()));
            }
        }
    }
}
