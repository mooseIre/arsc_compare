package com.android.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.miui.anim.HideAfterAnimatorListener;
import com.android.systemui.miui.anim.ShowBeforeAnimatorListener;
import com.android.systemui.miui.policy.NotificationsMonitor;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.KeyguardMonitor;

public class NotificationPeekingIconAreaController extends NotificationIconAreaController implements MiuiStatusBarPromptController.OnPromptStateChangedListener, NotificationsMonitor.Callback {
    private View mClockContainer;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 100) {
                NotificationPeekingIconAreaController.this.handlePeeking(true);
            } else if (i == 101) {
                NotificationPeekingIconAreaController.this.handlePeeking(false);
            }
        }
    };
    /* access modifiers changed from: private */
    public ViewGroup mNotificationIcons;
    private int mNotificationIconsCount;
    private boolean mPeeking;
    private int mPeekingDuration;
    private int mPeekingSizeHint;
    private boolean mPeekingWithExtraPadding;
    private boolean mPendingPeeking;
    private boolean mShowNotifications;
    private boolean mShowingMiuiPrompts;
    private UserPresentReceiver mUserPresentReceiver = new UserPresentReceiver();

    public void onNotificationAdded(StatusBarNotification statusBarNotification) {
    }

    public void onNotificationUpdated(StatusBarNotification statusBarNotification) {
    }

    public NotificationPeekingIconAreaController(Context context, StatusBar statusBar) {
        super(context, statusBar);
        this.mPeekingWithExtraPadding = context.getResources().getBoolean(R.bool.status_bar_notification_icons_peeking_extra_padding);
        this.mPeekingSizeHint = context.getResources().getInteger(R.integer.status_bar_notification_icons_size_hint);
        this.mPeekingDuration = context.getResources().getInteger(R.integer.status_bar_notification_icons_peeking_duration);
        context.registerReceiver(this.mUserPresentReceiver, new IntentFilter("android.intent.action.USER_PRESENT"));
        this.mContext = context;
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).addPromptStateChangedListener("NotificationPeekingIcon", this);
        ((NotificationsMonitor) Dependency.get(NotificationsMonitor.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public View inflateIconArea(LayoutInflater layoutInflater) {
        View inflateIconArea = super.inflateIconArea(layoutInflater);
        ViewGroup viewGroup = (ViewGroup) inflateIconArea.findViewById(R.id.notificationIcons);
        this.mNotificationIcons = viewGroup;
        viewGroup.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            public void onChildViewAdded(View view, View view2) {
                NotificationPeekingIconAreaController.this.onIconsChanged();
                NotificationPeekingIconAreaController.this.firePendingPeeking();
            }

            public void onChildViewRemoved(View view, View view2) {
                NotificationPeekingIconAreaController.this.onIconsChanged();
            }
        });
        this.mNotificationIcons.setAlpha(0.0f);
        return inflateIconArea;
    }

    public void setupClockContainer(View view) {
        super.setupClockContainer(view);
        this.mClockContainer = view;
    }

    /* access modifiers changed from: package-private */
    public void setShowNotificationIcon(boolean z) {
        super.setShowNotificationIcon(z);
        boolean z2 = this.mShowNotifications;
        this.mShowNotifications = z;
        if (z && !z2) {
            dispatchPeeking();
        }
        if (z2 && !z && this.mHandler.hasMessages(R.styleable.AppCompatTheme_switchStyle)) {
            this.mHandler.removeMessages(R.styleable.AppCompatTheme_switchStyle);
            this.mHandler.sendEmptyMessageDelayed(R.styleable.AppCompatTheme_switchStyle, 0);
        }
    }

    public void release() {
        super.release();
        this.mContext.unregisterReceiver(this.mUserPresentReceiver);
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).removePromptStateChangedListener("NotificationPeekingIcon");
        ((NotificationsMonitor) Dependency.get(NotificationsMonitor.class)).removeCallback(this);
    }

    private void dispatchPeeking() {
        if (this.mShowNotifications && this.mNotificationIconsCount > 0) {
            if (this.mShowingMiuiPrompts) {
                Log.i("NotificationPeekingIcon", "ignore peeking because of miui prompt showing");
            } else if (((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).isShowing()) {
                Log.v("NotificationPeekingIcon", "pending peeking because of keyguard showing");
                recordPendingPeeking();
            } else {
                this.mHandler.removeMessages(100);
                this.mHandler.sendEmptyMessageDelayed(100, 10);
                this.mHandler.removeMessages(R.styleable.AppCompatTheme_switchStyle);
                this.mHandler.sendEmptyMessageDelayed(R.styleable.AppCompatTheme_switchStyle, (long) this.mPeekingDuration);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handlePeeking(boolean z) {
        if (z && !this.mPeeking) {
            this.mClockContainer.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setListener(new HideAfterAnimatorListener(this.mClockContainer)).setInterpolator(Interpolators.MIUI_ALPHA_OUT).start();
            this.mNotificationIcons.animate().alpha(1.0f).setDuration(160).setStartDelay(160).setInterpolator(Interpolators.MIUI_ALPHA_IN).start();
        } else if (!z) {
            this.mNotificationIcons.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.MIUI_ALPHA_OUT).start();
            this.mClockContainer.animate().alpha(1.0f).setDuration(160).setStartDelay(160).setListener(new ShowBeforeAnimatorListener(this.mClockContainer)).setInterpolator(Interpolators.MIUI_ALPHA_IN).start();
        }
        this.mPeeking = z;
    }

    /* access modifiers changed from: private */
    public void onIconsChanged() {
        this.mNotificationIconsCount = this.mNotificationIcons.getChildCount();
        if (this.mPeekingWithExtraPadding) {
            this.mNotificationIcons.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    NotificationPeekingIconAreaController.this.mNotificationIcons.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    NotificationPeekingIconAreaController.this.handleExtraPadding();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void handleExtraPadding() {
        this.mNotificationIcons.setPaddingRelative((int) ((Math.max(0.0f, (float) (this.mPeekingSizeHint - this.mNotificationIconsCount)) * (this.mNotificationIconsCount > 0 ? (float) this.mNotificationIcons.getChildAt(0).getMeasuredWidth() : 0.0f)) / 2.0f), 0, 0, 0);
    }

    private void recordPendingPeeking() {
        this.mPendingPeeking = true;
    }

    /* access modifiers changed from: private */
    public void firePendingPeeking() {
        if (this.mPendingPeeking) {
            this.mPendingPeeking = false;
            dispatchPeeking();
        }
    }

    public void onPromptStateChanged(boolean z, String str) {
        boolean z2 = !z;
        this.mShowingMiuiPrompts = z2;
        if (z2) {
            this.mHandler.removeMessages(100);
            if (this.mPeeking) {
                this.mHandler.removeMessages(R.styleable.AppCompatTheme_switchStyle);
                this.mHandler.sendEmptyMessage(R.styleable.AppCompatTheme_switchStyle);
                Log.d("NotificationPeekingIcon", "prompt showing, end peeking immediately");
            }
        }
    }

    public void onNotificationArrived(StatusBarNotification statusBarNotification) {
        recordPendingPeeking();
    }

    private class UserPresentReceiver extends BroadcastReceiver {
        private UserPresentReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                NotificationPeekingIconAreaController.this.firePendingPeeking();
            }
        }
    }
}
