package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.view.View;
import com.android.internal.widget.ImageFloatingTextView;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationBigTextTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private ImageFloatingTextView mBigText;

    protected NotificationBigTextTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void resolveViews() {
        this.mBigText = this.mView.findViewById(16908767);
        clearColorSpans(this.mBigText);
        this.mBigText.post(new Runnable() {
            public final void run() {
                NotificationBigTextTemplateViewWrapper.this.lambda$resolveViews$0$NotificationBigTextTemplateViewWrapper();
            }
        });
    }

    public /* synthetic */ void lambda$resolveViews$0$NotificationBigTextTemplateViewWrapper() {
        if (showOneLine()) {
            handleTemplateViews();
        }
    }

    private int getTextLineCount() {
        ImageFloatingTextView imageFloatingTextView = this.mBigText;
        if (imageFloatingTextView != null) {
            return imageFloatingTextView.getLineCount();
        }
        return 0;
    }

    private boolean showOneLine() {
        return getTextLineCount() == 1;
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        if (showRightIcon() && showOneLine()) {
            return false;
        }
        Notification notification = this.mRow.getEntry().notification.getNotification();
        if (notification.showsTime() || notification.showsChronometer()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
        if (!showRightIcon() && !showOneLine()) {
            return true;
        }
        return false;
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
