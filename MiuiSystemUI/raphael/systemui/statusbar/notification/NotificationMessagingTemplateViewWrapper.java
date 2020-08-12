package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.view.View;
import com.android.internal.widget.MessagingLayout;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationMessagingTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private MessagingLayout mMessagingLayout;
    private MessagingLinearLayout mMessagingLinearLayout = this.mMessagingLayout.getMessagingLinearLayout();

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean showProgressBar() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showRightIcon() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showSingleLine() {
        return false;
    }

    protected NotificationMessagingTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mMessagingLayout = (MessagingLayout) view;
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        Notification notification = this.mRow.getEntry().notification.getNotification();
        return notification.showsTime() || notification.showsChronometer();
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        MessagingLinearLayout messagingLinearLayout;
        super.updateTransformedTypes();
        if (!showMiuiStyle() && (messagingLinearLayout = this.mMessagingLinearLayout) != null) {
            this.mTransformationHelper.addTransformedView(messagingLinearLayout.getId(), this.mMessagingLinearLayout);
        }
    }

    public void setRemoteInputVisible(boolean z) {
        super.setRemoteInputVisible(z);
        this.mMessagingLayout.showHistoricMessages(z);
    }
}
