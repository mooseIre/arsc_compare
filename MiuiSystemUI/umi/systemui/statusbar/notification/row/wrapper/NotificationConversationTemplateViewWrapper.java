package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.ConversationLayout;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationConversationTemplateViewWrapper.kt */
public final class NotificationConversationTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View appName;
    private View conversationBadgeBg;
    private CachingIconView conversationIconView;
    private final ConversationLayout conversationLayout;
    private View conversationTitleView;
    private View expandButton;
    private View expandButtonContainer;
    private View expandButtonInnerContainer;
    private View facePileBottom;
    private View facePileBottomBg;
    private View facePileTop;
    private ViewGroup imageMessageContainer;
    private View importanceRing;
    private MessagingLinearLayout messagingLinearLayout;
    private final int minHeightWithActions;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NotificationConversationTemplateViewWrapper(@NotNull Context context, @NotNull View view, @NotNull ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        Intrinsics.checkParameterIsNotNull(context, "ctx");
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        this.minHeightWithActions = NotificationUtils.getFontScaledHeight(context, C0012R$dimen.notification_messaging_actions_min_height);
        this.conversationLayout = (ConversationLayout) view;
    }

    private final void resolveViews() {
        MessagingLinearLayout messagingLinearLayout2 = this.conversationLayout.getMessagingLinearLayout();
        Intrinsics.checkExpressionValueIsNotNull(messagingLinearLayout2, "conversationLayout.messagingLinearLayout");
        this.messagingLinearLayout = messagingLinearLayout2;
        ViewGroup imageMessageContainer2 = this.conversationLayout.getImageMessageContainer();
        Intrinsics.checkExpressionValueIsNotNull(imageMessageContainer2, "conversationLayout.imageMessageContainer");
        this.imageMessageContainer = imageMessageContainer2;
        ConversationLayout conversationLayout2 = this.conversationLayout;
        CachingIconView requireViewById = conversationLayout2.requireViewById(16908892);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(com.andr…l.R.id.conversation_icon)");
        this.conversationIconView = requireViewById;
        View requireViewById2 = conversationLayout2.requireViewById(16908894);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(com.andr…nversation_icon_badge_bg)");
        this.conversationBadgeBg = requireViewById2;
        View requireViewById3 = conversationLayout2.requireViewById(16908957);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById(com.andr…ernal.R.id.expand_button)");
        this.expandButton = requireViewById3;
        View requireViewById4 = conversationLayout2.requireViewById(16908959);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "requireViewById(com.andr….expand_button_container)");
        this.expandButtonContainer = requireViewById4;
        View requireViewById5 = conversationLayout2.requireViewById(16908960);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById5, "requireViewById(com.andr…d_button_inner_container)");
        this.expandButtonInnerContainer = requireViewById5;
        View requireViewById6 = conversationLayout2.requireViewById(16908895);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById6, "requireViewById(com.andr…ersation_icon_badge_ring)");
        this.importanceRing = requireViewById6;
        View requireViewById7 = conversationLayout2.requireViewById(16908763);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById7, "requireViewById(com.andr…ernal.R.id.app_name_text)");
        this.appName = requireViewById7;
        View requireViewById8 = conversationLayout2.requireViewById(16908898);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById8, "requireViewById(com.andr…l.R.id.conversation_text)");
        this.conversationTitleView = requireViewById8;
        this.facePileTop = conversationLayout2.findViewById(16908890);
        this.facePileBottom = conversationLayout2.findViewById(16908888);
        this.facePileBottomBg = conversationLayout2.findViewById(16908889);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void onContentUpdated(@NotNull ExpandableNotificationRow expandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        resolveViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        View[] viewArr = new View[3];
        MessagingLinearLayout messagingLinearLayout2 = this.messagingLinearLayout;
        if (messagingLinearLayout2 != null) {
            viewArr[0] = messagingLinearLayout2;
            View view = this.appName;
            if (view != null) {
                viewArr[1] = view;
                View view2 = this.conversationTitleView;
                if (view2 != null) {
                    viewArr[2] = view2;
                    addTransformedViews(viewArr);
                    ViewTransformationHelper viewTransformationHelper = this.mTransformationHelper;
                    NotificationConversationTemplateViewWrapper$updateTransformedTypes$1 notificationConversationTemplateViewWrapper$updateTransformedTypes$1 = new NotificationConversationTemplateViewWrapper$updateTransformedTypes$1();
                    ViewGroup viewGroup = this.imageMessageContainer;
                    if (viewGroup != null) {
                        viewTransformationHelper.setCustomTransformation(notificationConversationTemplateViewWrapper$updateTransformedTypes$1, viewGroup.getId());
                        View[] viewArr2 = new View[7];
                        CachingIconView cachingIconView = this.conversationIconView;
                        if (cachingIconView != null) {
                            viewArr2[0] = cachingIconView;
                            View view3 = this.conversationBadgeBg;
                            if (view3 != null) {
                                viewArr2[1] = view3;
                                View view4 = this.expandButton;
                                if (view4 != null) {
                                    viewArr2[2] = view4;
                                    View view5 = this.importanceRing;
                                    if (view5 != null) {
                                        viewArr2[3] = view5;
                                        viewArr2[4] = this.facePileTop;
                                        viewArr2[5] = this.facePileBottom;
                                        viewArr2[6] = this.facePileBottomBg;
                                        addViewsTransformingToSimilar(viewArr2);
                                        return;
                                    }
                                    Intrinsics.throwUninitializedPropertyAccessException("importanceRing");
                                    throw null;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("expandButton");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("conversationBadgeBg");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("imageMessageContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("conversationTitleView");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("appName");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("messagingLinearLayout");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    @NotNull
    public View getExpandButton() {
        View view = this.expandButtonInnerContainer;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandButtonInnerContainer");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void setShelfIconVisible(boolean z) {
        if (this.conversationLayout.isImportantConversation()) {
            CachingIconView cachingIconView = this.conversationIconView;
            if (cachingIconView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                throw null;
            } else if (cachingIconView.getVisibility() != 8) {
                CachingIconView cachingIconView2 = this.conversationIconView;
                if (cachingIconView2 != null) {
                    cachingIconView2.setForceHidden(z);
                    return;
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
                    throw null;
                }
            }
        }
        super.setShelfIconVisible(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    @Nullable
    public View getShelfTransformationTarget() {
        if (!this.conversationLayout.isImportantConversation()) {
            return super.getShelfTransformationTarget();
        }
        CachingIconView cachingIconView = this.conversationIconView;
        if (cachingIconView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
            throw null;
        } else if (cachingIconView.getVisibility() == 8) {
            return super.getShelfTransformationTarget();
        } else {
            CachingIconView cachingIconView2 = this.conversationIconView;
            if (cachingIconView2 != null) {
                return cachingIconView2;
            }
            Intrinsics.throwUninitializedPropertyAccessException("conversationIconView");
            throw null;
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setRemoteInputVisible(boolean z) {
        this.conversationLayout.showHistoricMessages(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void updateExpandability(boolean z, @Nullable View.OnClickListener onClickListener) {
        this.conversationLayout.updateExpandability(z, onClickListener);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
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

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getMinLayoutHeight() {
        View view = this.mActionsContainer;
        if (view != null) {
            Intrinsics.checkExpressionValueIsNotNull(view, "mActionsContainer");
            if (view.getVisibility() != 8) {
                return this.minHeightWithActions;
            }
        }
        return super.getMinLayoutHeight();
    }

    private final void addTransformedViews(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                this.mTransformationHelper.addTransformedView(view);
            }
        }
    }

    private final void addViewsTransformingToSimilar(View... viewArr) {
        for (View view : viewArr) {
            if (view != null) {
                this.mTransformationHelper.addViewTransformingToSimilar(view);
            }
        }
    }
}
