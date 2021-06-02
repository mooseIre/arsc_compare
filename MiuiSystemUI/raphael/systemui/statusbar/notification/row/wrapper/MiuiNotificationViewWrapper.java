package com.android.systemui.statusbar.notification.row.wrapper;

import android.app.Notification;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class MiuiNotificationViewWrapper extends NotificationViewWrapper {
    protected ImageView mAppIcon;

    protected MiuiNotificationViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        resolveViews();
        handleViews();
    }

    private void resolveViews() {
        this.mAppIcon = (ImageView) this.mView.findViewById(C0015R$id.app_icon);
    }

    private void handleViews() {
        handleAppIcon();
    }

    /* access modifiers changed from: protected */
    public void handleAppIcon() {
        NotificationUtil.applyAppIconAllowCustom(this.mContext, this.mRow.getEntry().getSbn(), this.mAppIcon);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        resolveViews();
        handleViews();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        if (z && this.mView.getAlpha() != 1.0f) {
            this.mView.setAlpha(1.0f);
        }
        super.setVisible(z);
    }

    /* access modifiers changed from: protected */
    public boolean showRightIcon() {
        Notification notification = this.mRow.getEntry().getSbn().getNotification();
        return (notification.getLargeIcon() == null && notification.largeIcon == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    public boolean showProgressBar() {
        Notification notification = this.mRow.getEntry().getSbn().getNotification();
        int i = notification.extras.getInt("android.progressMax", 0);
        boolean z = notification.extras.getBoolean("android.progressIndeterminate");
        if (i != 0 || z) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        Notification notification = this.mRow.getEntry().getSbn().getNotification();
        return notification.showsTime() || notification.showsChronometer();
    }

    /* access modifiers changed from: protected */
    public void setViewMarginEnd(View view, int i) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginLayoutParams.setMarginEnd(i);
        view.setLayoutParams(marginLayoutParams);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView) {
        this.mView.setAlpha(0.0f);
        super.transformFrom(transformableView);
    }

    /* access modifiers changed from: protected */
    public int getDimensionPixelSize(int i) {
        return this.mContext.getResources().getDimensionPixelSize(i);
    }
}
