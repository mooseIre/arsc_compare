package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.view.View;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationBigTextTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private ImageFloatingTextView mBigText;

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
        return true;
    }

    protected NotificationBigTextTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void resolveViews() {
        ImageFloatingTextView findViewById = this.mView.findViewById(16908796);
        this.mBigText = findViewById;
        clearColorSpans(findViewById);
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        Notification notification = this.mRow.getEntry().notification.getNotification();
        return notification.showsTime() || notification.showsChronometer();
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        resolveViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        ImageFloatingTextView imageFloatingTextView;
        super.updateTransformedTypes();
        if (!showMiuiStyle() && (imageFloatingTextView = this.mBigText) != null) {
            this.mTransformationHelper.addTransformedView(2, imageFloatingTextView);
        }
    }
}
