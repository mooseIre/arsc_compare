package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import java.util.function.Consumer;

public class HybridGroupManager {
    private final Context mContext;
    private float mDarkAmount = 0.0f;
    private final NotificationDozeHelper mDozer;
    private int mGoogleContentMarginEnd;
    private int mGoogleContentMarginStart;
    private int mOverflowNumberColor;
    private int mOverflowNumberColorDark;
    private int mOverflowNumberPadding;
    private final ViewGroup mParent;

    public HybridGroupManager(Context context, ViewGroup viewGroup) {
        this.mContext = context;
        this.mParent = viewGroup;
        this.mDozer = new NotificationDozeHelper();
        Resources resources = this.mContext.getResources();
        this.mOverflowNumberPadding = resources.getDimensionPixelSize(R.dimen.group_overflow_number_padding);
        this.mGoogleContentMarginStart = resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_start);
        this.mGoogleContentMarginEnd = resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_end);
    }

    private HybridNotificationView inflateHybridViewWithStyle(int i) {
        HybridNotificationView hybridNotificationView = (HybridNotificationView) ((LayoutInflater) new ContextThemeWrapper(this.mContext, i).getSystemService(LayoutInflater.class)).inflate(R.layout.hybrid_notification, this.mParent, false);
        if (NotificationUtil.showGoogleStyle()) {
            hybridNotificationView.setPaddingRelative(this.mGoogleContentMarginStart, 0, this.mGoogleContentMarginEnd, 0);
        }
        this.mParent.addView(hybridNotificationView);
        return hybridNotificationView;
    }

    private TextView inflateOverflowNumber() {
        TextView textView = (TextView) ((LayoutInflater) this.mContext.getSystemService(LayoutInflater.class)).inflate(R.layout.hybrid_overflow_number, this.mParent, false);
        this.mParent.addView(textView);
        updateOverFlowNumberColor(textView);
        return textView;
    }

    private void updateOverFlowNumberColor(TextView textView) {
        textView.setTextColor(NotificationUtils.interpolateColors(this.mOverflowNumberColor, this.mOverflowNumberColorDark, this.mDarkAmount));
    }

    public void setOverflowNumberColor(TextView textView, int i, int i2) {
        this.mOverflowNumberColor = i;
        this.mOverflowNumberColorDark = i2;
        if (textView != null) {
            updateOverFlowNumberColor(textView);
        }
    }

    public HybridNotificationView bindFromNotification(HybridNotificationView hybridNotificationView, Notification notification) {
        return bindFromNotificationWithStyle(hybridNotificationView, notification, R.style.HybridNotification);
    }

    public HybridNotificationView bindAmbientFromNotification(HybridNotificationView hybridNotificationView, Notification notification) {
        return bindFromNotificationWithStyle(hybridNotificationView, notification, R.style.HybridNotification_Ambient);
    }

    private HybridNotificationView bindFromNotificationWithStyle(HybridNotificationView hybridNotificationView, Notification notification, int i) {
        if (hybridNotificationView == null) {
            hybridNotificationView = inflateHybridViewWithStyle(i);
        }
        hybridNotificationView.bind(resolveTitle(notification), resolveText(notification));
        return hybridNotificationView;
    }

    private CharSequence resolveText(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.text");
        return charSequence == null ? notification.extras.getCharSequence("android.bigText") : charSequence;
    }

    private CharSequence resolveTitle(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        return charSequence == null ? notification.extras.getCharSequence("android.title.big") : charSequence;
    }

    public TextView bindOverflowNumber(TextView textView, int i) {
        if (textView == null) {
            textView = inflateOverflowNumber();
        }
        String string = this.mContext.getResources().getString(R.string.notification_group_overflow_indicator, new Object[]{Integer.valueOf(i)});
        if (!string.equals(textView.getText())) {
            textView.setText(string);
        }
        textView.setContentDescription(String.format(this.mContext.getResources().getQuantityString(R.plurals.notification_group_overflow_description, i), new Object[]{Integer.valueOf(i)}));
        return textView;
    }

    public void setOverflowNumberDark(TextView textView, boolean z, boolean z2, long j) {
        this.mDozer.setIntensityDark(new Consumer(textView) {
            private final /* synthetic */ TextView f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                HybridGroupManager.this.lambda$setOverflowNumberDark$0$HybridGroupManager(this.f$1, (Float) obj);
            }
        }, z, z2, j);
    }

    public /* synthetic */ void lambda$setOverflowNumberDark$0$HybridGroupManager(TextView textView, Float f) {
        this.mDarkAmount = f.floatValue();
        updateOverFlowNumberColor(textView);
    }

    public int getOverflowNumberPadding() {
        return this.mOverflowNumberPadding;
    }
}
