package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationAggregate;
import com.android.systemui.statusbar.NotificationGuts;

public class NotificationAggregate extends LinearLayout implements NotificationGuts.GutsContent {

    public interface ClickListener {
        void onClickCancel(View view);

        void onClickConfirm(View view);
    }

    public View getContentView() {
        return this;
    }

    public boolean handleCloseControls(boolean z, boolean z2) {
        return false;
    }

    public boolean isLeavebehind() {
        return false;
    }

    public void setGutsParent(NotificationGuts notificationGuts) {
    }

    public boolean willBeRemoved() {
        return false;
    }

    public NotificationAggregate(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void bindNotification(ExpandedNotification expandedNotification, ClickListener clickListener) {
        NotificationUtil.applyAppIcon(getContext(), expandedNotification, (ImageView) findViewById(R.id.pkgicon));
        ((TextView) findViewById(R.id.title)).setText(this.mContext.getString(R.string.notification_aggregate_text, new Object[]{expandedNotification.getAppName()}));
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                NotificationAggregate.ClickListener.this.onClickCancel(view);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                NotificationAggregate.ClickListener.this.onClickConfirm(view);
            }
        });
    }

    public int getActualHeight() {
        return getHeight();
    }

    public static boolean canAggregate(Context context, ExpandedNotification expandedNotification) {
        if (!Constants.IS_INTERNATIONAL && expandedNotification != null && expandedNotification.isClearable() && !expandedNotification.getNotification().isGroupSummary() && !NotificationUtil.isMediaNotification(expandedNotification) && !NotificationUtil.isCustomViewNotification(expandedNotification) && NotificationSettingsHelper.isFoldable(context, expandedNotification.getPackageName()) && NotificationUtil.getUserFold(context) >= 0) {
            return true;
        }
        return false;
    }
}
