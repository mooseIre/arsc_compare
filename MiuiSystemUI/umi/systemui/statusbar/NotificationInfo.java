package com.android.systemui.statusbar;

import android.app.INotificationManager;
import android.app.NotificationChannelCompat;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.NotificationInfo;
import java.util.List;
import miui.widget.SlidingButton;

public class NotificationInfo extends LinearLayout implements NotificationGuts.GutsContent {
    private SlidingButton mChannelEnabledSwitch;
    private ClickListener mClickListener;
    private NotificationGuts mGutsContainer;
    private int mIndex;
    private ExpandedNotification mSbn;
    private int mStartingUserImportance;

    public interface ClickListener {
        void onClickCheckSave(Runnable runnable);

        void onClickDone(View view);

        void onClickSettings(View view);
    }

    public View getContentView() {
        return this;
    }

    public boolean isLeavebehind() {
        return false;
    }

    public NotificationInfo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void bindNotification(INotificationManager iNotificationManager, List<NotificationChannelCompat> list, int i, ExpandedNotification expandedNotification, int i2, ClickListener clickListener) {
        this.mStartingUserImportance = i;
        this.mSbn = expandedNotification;
        this.mIndex = i2;
        this.mClickListener = clickListener;
        NotificationUtil.applyAppIcon(getContext(), expandedNotification, (ImageView) findViewById(R.id.pkgicon));
        initSlidingButton();
        initTitle();
        initSettingsButton();
        ((TextView) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                NotificationInfo.ClickListener.this.onClickDone(view);
            }
        });
    }

    private void initTitle() {
        boolean z = !this.mChannelEnabledSwitch.isChecked();
        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(this.mContext.getString(z ? R.string.notification_info_disabled_text : R.string.notification_info_enabled_text, new Object[]{this.mSbn.getAppName()}));
        textView.setTextColor(this.mContext.getColor(z ? R.color.notification_info_warning_color : 17170903));
    }

    private void initSlidingButton() {
        this.mChannelEnabledSwitch = (SlidingButton) findViewById(R.id.channel_enabled_switch);
        int i = 0;
        this.mChannelEnabledSwitch.setChecked(this.mStartingUserImportance != 0);
        SlidingButton slidingButton = this.mChannelEnabledSwitch;
        if (!isBlockable()) {
            i = 8;
        }
        slidingButton.setVisibility(i);
        this.mChannelEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                NotificationInfo.this.lambda$initSlidingButton$1$NotificationInfo(compoundButton, z);
            }
        });
    }

    public /* synthetic */ void lambda$initSlidingButton$1$NotificationInfo(CompoundButton compoundButton, boolean z) {
        initTitle();
        NotificationGuts notificationGuts = this.mGutsContainer;
        if (notificationGuts != null) {
            notificationGuts.resetFalsingCheck();
        }
    }

    private void initSettingsButton() {
        TextView textView = (TextView) findViewById(R.id.button1);
        if (this.mSbn.getAppUid() >= 0) {
            textView.setVisibility(0);
            textView.setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    NotificationInfo.this.lambda$initSettingsButton$2$NotificationInfo(view);
                }
            });
            return;
        }
        textView.setVisibility(8);
    }

    public /* synthetic */ void lambda$initSettingsButton$2$NotificationInfo(View view) {
        this.mClickListener.onClickSettings(view);
    }

    private boolean hasImportanceChanged() {
        SlidingButton slidingButton = this.mChannelEnabledSwitch;
        return slidingButton != null && !slidingButton.isChecked();
    }

    /* access modifiers changed from: private */
    /* renamed from: saveImportance */
    public void lambda$handleCloseControls$3$NotificationInfo() {
        String packageName = this.mSbn.getPackageName();
        NotificationSettingsHelper.setNotificationsEnabledForPackage(this.mContext, packageName, this.mChannelEnabledSwitch.isChecked());
        if (!this.mChannelEnabledSwitch.isChecked()) {
            Intent intent = new Intent("com.miui.app.ExtraStatusBarManager.action_refresh_notification");
            intent.setPackage("com.android.systemui");
            intent.putExtra("com.miui.app.ExtraStatusBarManager.extra_forbid_notification", !this.mChannelEnabledSwitch.isChecked());
            intent.putExtra("app_packageName", packageName);
            String messageId = NotificationUtil.getMessageId(this.mSbn);
            if (!TextUtils.isEmpty(messageId)) {
                intent.putExtra("messageId", messageId);
            }
            this.mContext.sendBroadcast(intent);
            ((NotificationStat) Dependency.get(NotificationStat.class)).onBlock(this.mSbn, (NotificationChannelCompat) null, this.mIndex);
        }
    }

    private boolean isBlockable() {
        if (NotificationSettingsHelper.isNonBlockable(this.mContext, this.mSbn.getPackageName(), "")) {
            return false;
        }
        return !NotificationSettingsHelper.isUidSystem(this.mSbn.getAppUid());
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (this.mGutsContainer != null && accessibilityEvent.getEventType() == 32) {
            accessibilityEvent.getText().add(this.mContext.getString(this.mGutsContainer.isExposed() ? R.string.notification_channel_controls_opened_accessibility : R.string.notification_channel_controls_closed_accessibility, new Object[]{this.mSbn.getAppName()}));
        }
    }

    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    public boolean willBeRemoved() {
        SlidingButton slidingButton = this.mChannelEnabledSwitch;
        return slidingButton != null && !slidingButton.isChecked();
    }

    public boolean handleCloseControls(boolean z, boolean z2) {
        if (!z || !hasImportanceChanged()) {
            return false;
        }
        ClickListener clickListener = this.mClickListener;
        if (clickListener != null) {
            clickListener.onClickCheckSave(new Runnable() {
                public final void run() {
                    NotificationInfo.this.lambda$handleCloseControls$3$NotificationInfo();
                }
            });
            return false;
        }
        lambda$handleCloseControls$3$NotificationInfo();
        return false;
    }

    public int getActualHeight() {
        return getHeight();
    }
}
