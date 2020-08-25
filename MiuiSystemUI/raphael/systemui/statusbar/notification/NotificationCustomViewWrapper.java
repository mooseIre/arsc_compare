package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationCustomViewWrapper extends NotificationViewWrapper {
    private int mCornerRadius;
    private int mCustomViewMargin;

    public void onReinflated() {
    }

    /* access modifiers changed from: protected */
    public boolean shouldClearBackgroundOnReapply() {
        return false;
    }

    protected NotificationCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        initResources();
        handleViewMargin();
        updateRoundCorner();
    }

    private void initResources() {
        Resources resources = this.mContext.getResources();
        this.mBackgroundColor = resources.getColor(R.color.notification_material_background_color);
        this.mCornerRadius = resources.getDimensionPixelSize(R.dimen.notification_custom_view_corner_radius);
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

    private void updateRoundCorner() {
        Util.setViewRoundCorner(this.mView, (float) this.mCornerRadius);
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
