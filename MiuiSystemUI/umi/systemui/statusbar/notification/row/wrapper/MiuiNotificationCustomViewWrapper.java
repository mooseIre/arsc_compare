package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class MiuiNotificationCustomViewWrapper extends NotificationViewWrapper {
    public MiuiNotificationCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        handleCustomView();
    }

    private void handleCustomView() {
        resetChildrenHeightAndMargin();
        setCustomViewMargin();
    }

    private void resetChildrenHeightAndMargin() {
        NotificationHeaderView findViewById = this.mView.findViewById(16909237);
        if (findViewById != null && (findViewById.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) findViewById.getLayoutParams();
            marginLayoutParams.height = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_height);
            findViewById.setLayoutParams(marginLayoutParams);
            findViewById.setPadding(findViewById.getPaddingStart(), 0, findViewById.getPaddingEnd(), 0);
        }
        LinearLayout linearLayout = (LinearLayout) this.mView.findViewById(16909238);
        if (linearLayout != null && (linearLayout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
            marginLayoutParams2.topMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_content_margin_top);
            marginLayoutParams2.bottomMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_content_margin_bottom);
            linearLayout.setLayoutParams(marginLayoutParams2);
        }
        LinearLayout linearLayout2 = (LinearLayout) this.mView.findViewById(16909389);
        if (linearLayout2 != null && (linearLayout2.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) linearLayout2.getLayoutParams();
            marginLayoutParams3.topMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.miui_notification_header_content_margin_top);
            linearLayout2.setLayoutParams(marginLayoutParams3);
        }
    }

    private void setCustomViewMargin() {
        int customViewMargin = getCustomViewMargin();
        if (customViewMargin > 0 && (this.mView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mView.getLayoutParams();
            marginLayoutParams.setMarginStart(customViewMargin);
            marginLayoutParams.setMarginEnd(customViewMargin);
            marginLayoutParams.topMargin = customViewMargin;
            marginLayoutParams.bottomMargin = customViewMargin;
            this.mView.setLayoutParams(marginLayoutParams);
        }
    }

    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        handleCustomView();
    }

    public int getExtraMeasureHeight() {
        return getCustomViewMargin() * 2;
    }

    private int getCustomViewMargin() {
        if (MiuiNotificationCompat.isCustomHideBorder(this.mRow.getEntry().getSbn().getNotification())) {
            return 0;
        }
        return this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.notification_custom_view_margin);
    }

    private static boolean hasExtraMeasureHeight(NotificationViewWrapper notificationViewWrapper) {
        return (notificationViewWrapper instanceof MiuiNotificationCustomViewWrapper) && notificationViewWrapper.getExtraMeasureHeight() > 0;
    }

    public static int getExtraMeasureHeight(NotificationViewWrapper notificationViewWrapper) {
        if (hasExtraMeasureHeight(notificationViewWrapper)) {
            return notificationViewWrapper.getExtraMeasureHeight();
        }
        return 0;
    }
}
