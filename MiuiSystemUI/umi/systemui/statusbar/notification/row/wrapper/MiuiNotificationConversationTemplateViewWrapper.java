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

    public void setVisible(boolean z) {
        super.setVisible(z);
        View view = this.mView;
        Intrinsics.checkExpressionValueIsNotNull(view, "mView");
        view.setAlpha(z ? 1.0f : 0.0f);
    }

    public void setContentHeight(int i, int i2) {
        super.setContentHeight(i, i2);
        this.mContentHeight = i;
        this.mMinHeightHint = i2;
        updateActionOffset();
    }

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

    public void onContentUpdated(@NotNull ExpandableNotificationRow expandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        resolveViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    @NotNull
    public View getExpandButton() {
        View view = this.expandButtonInnerContainer;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandButtonInnerContainer");
        throw null;
    }

    public void updateExpandability(boolean z, @Nullable View.OnClickListener onClickListener) {
        this.conversationLayout.updateExpandability(z, onClickListener);
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean disallowSingleClick(float r6, float r7) {
        /*
            r5 = this;
            android.view.View r0 = r5.expandButtonContainer
            r1 = 0
            java.lang.String r2 = "expandButtonContainer"
            if (r0 == 0) goto L_0x002b
            int r0 = r0.getVisibility()
            r3 = 1
            r4 = 0
            if (r0 != 0) goto L_0x001f
            android.view.View r0 = r5.expandButtonContainer
            if (r0 == 0) goto L_0x001b
            boolean r0 = r5.isOnView(r0, r6, r7)
            if (r0 == 0) goto L_0x001f
            r0 = r3
            goto L_0x0020
        L_0x001b:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x001f:
            r0 = r4
        L_0x0020:
            if (r0 != 0) goto L_0x002a
            boolean r5 = super.disallowSingleClick(r6, r7)
            if (r5 == 0) goto L_0x0029
            goto L_0x002a
        L_0x0029:
            r3 = r4
        L_0x002a:
            return r3
        L_0x002b:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationConversationTemplateViewWrapper.disallowSingleClick(float, float):boolean");
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
