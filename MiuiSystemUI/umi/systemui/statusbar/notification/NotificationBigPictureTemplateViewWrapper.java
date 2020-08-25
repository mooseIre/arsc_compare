package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationBigPictureTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private int mBigPictureCornerRadius;
    private int mBigPictureMaxHeight;
    private ImageView mBigPictureView;

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
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

    protected NotificationBigPictureTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        initResources();
    }

    private void initResources() {
        this.mBigPictureMaxHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_big_picture_max_height);
        this.mBigPictureCornerRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_big_picture_corner_radius);
    }

    private void resolveViews() {
        ImageView imageView = (ImageView) this.mView.findViewById(16908793);
        this.mBigPictureView = imageView;
        imageView.setMaxHeight(this.mBigPictureMaxHeight);
        Util.setViewRoundCorner(this.mBigPictureView, (float) this.mBigPictureCornerRadius);
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        Notification notification = this.mRow.getEntry().notification.getNotification();
        return notification.showsTime() || notification.showsChronometer();
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        resolveViews();
        updateImageTag(expandableNotificationRow.getStatusBarNotification());
    }

    private void updateImageTag(StatusBarNotification statusBarNotification) {
        Icon icon = (Icon) statusBarNotification.getNotification().extras.getParcelable("android.largeIcon.big");
        if (icon != null) {
            this.mPicture.setTag(R.id.image_icon_tag, icon);
        }
    }
}
