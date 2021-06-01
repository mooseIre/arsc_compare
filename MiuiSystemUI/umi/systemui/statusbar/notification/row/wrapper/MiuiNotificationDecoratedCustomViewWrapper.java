package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.internal.widget.CachingIconView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class MiuiNotificationDecoratedCustomViewWrapper extends MiuiNotificationCustomViewWrapper {
    public MiuiNotificationDecoratedCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        handleCustomView();
    }

    private void handleCustomView() {
        changeChildrenHeightAndMargin();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationCustomViewWrapper
    public void setCustomViewMargin() {
        super.setCustomViewMargin();
        if (this.mView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mView.getLayoutParams();
            marginLayoutParams.setMarginStart(0);
            marginLayoutParams.setMarginEnd(0);
            this.mView.setLayoutParams(marginLayoutParams);
        }
    }

    private void changeChildrenHeightAndMargin() {
        NotificationHeaderView findViewById = this.mView.findViewById(16909237);
        if (findViewById != null && (findViewById.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) findViewById.getLayoutParams();
            marginLayoutParams.height = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_height);
            findViewById.setLayoutParams(marginLayoutParams);
            findViewById.setPadding(findViewById.getPaddingStart(), 0, findViewById.getPaddingEnd(), 0);
            CachingIconView icon = findViewById.getIcon();
            if (icon != null) {
                NotificationUtil.applyAppIconAllowCustom(this.mContext, this.mRow.getEntry().getSbn(), icon);
            }
            FrameLayout frameLayout = (FrameLayout) findViewById.findViewById(16909040);
            if (frameLayout != null && (frameLayout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
                ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();
                marginLayoutParams2.width = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_icon_container_width);
                frameLayout.setLayoutParams(marginLayoutParams2);
            }
        }
        LinearLayout linearLayout = (LinearLayout) this.mView.findViewById(16909238);
        if (linearLayout != null && (linearLayout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
            marginLayoutParams3.topMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_content_margin_top);
            marginLayoutParams3.bottomMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_content_margin_bottom);
            linearLayout.setLayoutParams(marginLayoutParams3);
        }
        LinearLayout linearLayout2 = (LinearLayout) this.mView.findViewById(16909389);
        if (linearLayout2 != null && (linearLayout2.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) linearLayout2.getLayoutParams();
            marginLayoutParams4.topMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_content_margin_top);
            linearLayout2.setLayoutParams(marginLayoutParams4);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationCustomViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationCustomViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        handleCustomView();
    }
}
