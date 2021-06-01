package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;

public class HybridConversationNotificationView extends HybridNotificationView {
    private View mConversationFacePile;
    private int mConversationIconSize;
    private ImageView mConversationIconView;
    private TextView mConversationSenderName;
    private int mFacePileProtectionWidth;
    private int mFacePileSize;

    public HybridConversationNotificationView(Context context) {
        this(context, null);
    }

    public HybridConversationNotificationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HybridConversationNotificationView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public HybridConversationNotificationView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.HybridNotificationView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mConversationIconView = (ImageView) requireViewById(16908892);
        this.mConversationFacePile = requireViewById(16908887);
        this.mConversationSenderName = (TextView) requireViewById(C0015R$id.conversation_notification_sender);
        this.mFacePileSize = getResources().getDimensionPixelSize(C0012R$dimen.conversation_single_line_face_pile_size);
        this.mConversationIconSize = getResources().getDimensionPixelSize(C0012R$dimen.conversation_single_line_avatar_size);
        this.mFacePileProtectionWidth = getResources().getDimensionPixelSize(C0012R$dimen.conversation_single_line_face_pile_protection_width);
        this.mTransformationHelper.addViewTransformingToSimilar(this.mConversationIconView);
        this.mTransformationHelper.addTransformedView(this.mConversationSenderName);
        MiuiStyleInjector.INSTANCE.alignConversationIcon(this.mConversationIconView, getResources());
    }

    @Override // com.android.systemui.statusbar.notification.row.HybridNotificationView
    public void bind(CharSequence charSequence, CharSequence charSequence2, View view) {
        if (!(view instanceof ConversationLayout)) {
            super.bind(charSequence, charSequence2, view);
            return;
        }
        ConversationLayout conversationLayout = (ConversationLayout) view;
        Icon conversationIcon = conversationLayout.getConversationIcon();
        if (conversationIcon != null) {
            this.mConversationFacePile.setVisibility(8);
            this.mConversationIconView.setVisibility(0);
            this.mConversationIconView.setImageIcon(conversationIcon);
        } else {
            this.mConversationIconView.setVisibility(8);
            this.mConversationFacePile.setVisibility(0);
            View requireViewById = requireViewById(16908887);
            this.mConversationFacePile = requireViewById;
            ImageView imageView = (ImageView) requireViewById.requireViewById(16908889);
            ImageView imageView2 = (ImageView) this.mConversationFacePile.requireViewById(16908888);
            ImageView imageView3 = (ImageView) this.mConversationFacePile.requireViewById(16908890);
            conversationLayout.bindFacePile(imageView, imageView2, imageView3);
            setSize(this.mConversationFacePile, this.mFacePileSize);
            setSize(imageView2, this.mConversationIconSize);
            setSize(imageView3, this.mConversationIconSize);
            setSize(imageView, this.mConversationIconSize + (this.mFacePileProtectionWidth * 2));
            this.mTransformationHelper.addViewTransformingToSimilar(imageView3);
            this.mTransformationHelper.addViewTransformingToSimilar(imageView2);
            this.mTransformationHelper.addViewTransformingToSimilar(imageView);
        }
        CharSequence conversationTitle = conversationLayout.getConversationTitle();
        if (!TextUtils.isEmpty(conversationTitle)) {
            charSequence = conversationTitle;
        }
        if (conversationLayout.isOneToOne()) {
            this.mConversationSenderName.setVisibility(8);
        } else {
            this.mConversationSenderName.setVisibility(0);
            this.mConversationSenderName.setText(conversationLayout.getConversationSenderName());
        }
        CharSequence conversationText = conversationLayout.getConversationText();
        if (!TextUtils.isEmpty(conversationText)) {
            charSequence2 = conversationText;
        }
        super.bind(charSequence, charSequence2, conversationLayout);
    }

    private static void setSize(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i;
        view.setLayoutParams(layoutParams);
    }
}
