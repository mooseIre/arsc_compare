package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.view.View;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class NotificationBigTextTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private ImageFloatingTextView mBigtext;

    protected NotificationBigTextTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    private void resolveViews(StatusBarNotification statusBarNotification) {
        this.mBigtext = this.mView.findViewById(16908794);
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        resolveViews(expandableNotificationRow.getEntry().getSbn());
        super.onContentUpdated(expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        ImageFloatingTextView imageFloatingTextView = this.mBigtext;
        if (imageFloatingTextView != null) {
            this.mTransformationHelper.addTransformedView(2, imageFloatingTextView);
        }
    }
}
