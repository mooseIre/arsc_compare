package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationCustomViewWrapper extends NotificationViewWrapper {
    private int mCustomViewMargin;

    /* access modifiers changed from: protected */
    public boolean shouldClearBackgroundOnReapply() {
        return false;
    }

    protected NotificationCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        initResources();
        handleViewMargin();
    }

    private void initResources() {
        this.mBackgroundColor = this.mContext.getResources().getColor(R.color.notification_material_background_color);
        this.mCustomViewMargin = NotificationUtil.getCustomViewMargin(this.mContext);
    }

    private void handleViewMargin() {
        if (this.mView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mView.getLayoutParams();
            int i = this.mCustomViewMargin;
            marginLayoutParams.leftMargin = i;
            marginLayoutParams.rightMargin = i;
            marginLayoutParams.topMargin = i;
            marginLayoutParams.bottomMargin = i;
            this.mView.setLayoutParams(marginLayoutParams);
        }
    }

    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
    }

    public int getCustomBackgroundColor() {
        return this.mBackgroundColor;
    }
}
