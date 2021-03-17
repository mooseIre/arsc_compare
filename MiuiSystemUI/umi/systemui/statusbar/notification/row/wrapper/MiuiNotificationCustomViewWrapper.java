package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class MiuiNotificationCustomViewWrapper extends NotificationViewWrapper {
    public MiuiNotificationCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        handleCustomView();
    }

    private void handleCustomView() {
        setCustomViewMargin();
    }

    /* access modifiers changed from: protected */
    public void setCustomViewMargin() {
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

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        handleCustomView();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
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
