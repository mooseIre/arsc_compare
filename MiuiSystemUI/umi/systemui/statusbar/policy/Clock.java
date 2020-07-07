package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import miui.date.Calendar;
import miui.date.DateUtils;

public class Clock extends TextView implements DemoMode, DarkIconDispatcher.DarkReceiver {
    private static final ThreadLocal<ReceiverInfo> sReceiverInfo = new ThreadLocal<>();
    private Calendar mCalendar;
    private int mClockMode;
    private DarkIconDispatcher mDarkIconDispatcher;
    private boolean mDemoMode;
    public int mForceHideAmPm;
    private boolean mShowAmPm;
    private LinkedList<ClockVisibilityListener> mVisibilityListeners;

    public interface ClockVisibilityListener {
        void onClockVisibilityChanged(boolean z);
    }

    public Clock(Context context) {
        this(context, (AttributeSet) null);
    }

    public Clock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Clock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mVisibilityListeners = new LinkedList<>();
        this.mShowAmPm = true;
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
    }

    public void setClockMode(int i) {
        if (this.mClockMode != i) {
            this.mClockMode = i;
            updateClock();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ReceiverInfo receiverInfo = sReceiverInfo.get();
        if (receiverInfo == null) {
            receiverInfo = new ReceiverInfo();
            sReceiverInfo.set(receiverInfo);
        }
        receiverInfo.setTimeFormat(DateFormat.is24HourFormat(this.mContext, -2) ? 32 : 16);
        receiverInfo.addView(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ReceiverInfo receiverInfo = sReceiverInfo.get();
        if (receiverInfo != null) {
            receiverInfo.removeView(this);
        }
    }

    public void setShowAmPm(boolean z) {
        this.mShowAmPm = z;
    }

    /* access modifiers changed from: package-private */
    public final void updateClock() {
        if (this.mDemoMode) {
            showDemoModeClock();
            return;
        }
        ReceiverInfo receiverInfo = sReceiverInfo.get();
        int i = this.mClockMode;
        if (i == 2) {
            if (this.mCalendar == null) {
                this.mCalendar = new Calendar();
            }
            this.mCalendar.setTimeZone(TimeZone.getDefault());
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            int i2 = R.string.status_bar_clock_date_time_format;
            if (receiverInfo != null && receiverInfo.getTimeFormat() == 16) {
                i2 = R.string.status_bar_clock_date_time_format_12;
            }
            setText(this.mCalendar.format(this.mContext.getString(i2)));
            return;
        }
        int i3 = R.string.status_bar_clock_date_format_12;
        if (i == 1) {
            if (this.mCalendar == null) {
                this.mCalendar = new Calendar();
            }
            this.mCalendar.setTimeZone(TimeZone.getDefault());
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            if (receiverInfo == null || receiverInfo.getTimeFormat() != 16) {
                i3 = R.string.status_bar_clock_date_format;
            }
            setText(this.mCalendar.format(this.mContext.getString(i3)));
        } else if (i == 3) {
            if (this.mCalendar == null) {
                this.mCalendar = new Calendar();
            }
            this.mCalendar.setTimeZone(TimeZone.getDefault());
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            int i4 = R.string.status_bar_clock_date_weekday_format;
            if (receiverInfo != null && receiverInfo.getTimeFormat() == 16) {
                i4 = R.string.status_bar_clock_date_weekday_format_12;
            }
            setText(this.mCalendar.format(this.mContext.getString(i4)));
            if (receiverInfo == null || receiverInfo.getTimeFormat() != 16) {
                i3 = R.string.status_bar_clock_date_format;
            }
            setContentDescription(this.mCalendar.format(this.mContext.getString(i3)));
        } else if (receiverInfo != null) {
            int timeFormat = receiverInfo.getTimeFormat();
            if (!this.mShowAmPm || this.mForceHideAmPm != 0) {
                setText(DateUtils.formatDateTime(System.currentTimeMillis(), timeFormat | 12 | 64));
            } else {
                setText(DateUtils.formatDateTime(System.currentTimeMillis(), timeFormat | 12));
            }
        }
    }

    public void update() {
        updateClock();
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        Resources resources = getResources();
        boolean showCtsSpecifiedColor = Util.showCtsSpecifiedColor();
        int i2 = R.color.status_bar_textColor;
        if (showCtsSpecifiedColor) {
            if (DarkIconDispatcherHelper.inDarkMode(rect, this, f)) {
                i2 = R.color.status_bar_icon_text_color_dark_mode_cts;
            }
            setTextColor(resources.getColor(i2));
        } else if (this.mDarkIconDispatcher.useTint()) {
            setTextColor(DarkIconDispatcherHelper.getTint(rect, this, i));
        } else {
            if (DarkIconDispatcherHelper.inDarkMode(rect, this, f)) {
                i2 = R.color.status_bar_textColor_darkmode;
            }
            setTextColor(resources.getColor(i2));
        }
    }

    private static class ReceiverInfo {
        private final ArrayList<Clock> mAttachedViews;
        private final BroadcastReceiver mReceiver;
        /* access modifiers changed from: private */
        public int mTimeFormat;

        private ReceiverInfo() {
            this.mAttachedViews = new ArrayList<>();
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.USER_SWITCHED".equals(action)) {
                        int unused = ReceiverInfo.this.mTimeFormat = DateFormat.is24HourFormat(context, -2) ? 32 : 16;
                    }
                    ReceiverInfo.this.updateAll();
                }
            };
            this.mTimeFormat = 16;
        }

        public int getTimeFormat() {
            return this.mTimeFormat;
        }

        public void setTimeFormat(int i) {
            this.mTimeFormat = i;
        }

        public void addView(Clock clock) {
            synchronized (this.mAttachedViews) {
                boolean isEmpty = this.mAttachedViews.isEmpty();
                this.mAttachedViews.add(clock);
                if (isEmpty) {
                    register(clock.getContext().getApplicationContext());
                }
            }
            clock.updateClock();
        }

        public void removeView(Clock clock) {
            synchronized (this.mAttachedViews) {
                this.mAttachedViews.remove(clock);
                if (this.mAttachedViews.isEmpty()) {
                    unregister(clock.getContext().getApplicationContext());
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void updateAll() {
            synchronized (this.mAttachedViews) {
                int size = this.mAttachedViews.size();
                for (int i = 0; i < size; i++) {
                    final Clock clock = this.mAttachedViews.get(i);
                    clock.post(new Runnable(this) {
                        public void run() {
                            clock.updateClock();
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void register(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        }

        /* access modifiers changed from: package-private */
        public void unregister(Context context) {
            context.unregisterReceiver(this.mReceiver);
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        Log.d("demo_mode", "Clock mDemoMode = " + this.mDemoMode + ", command = " + str);
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            showDemoModeClock();
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            updateClock();
        }
    }

    private void showDemoModeClock() {
        if (this.mCalendar == null) {
            this.mCalendar = new Calendar();
        }
        this.mCalendar.setTimeZone(TimeZone.getDefault());
        this.mCalendar.set(18, 2);
        this.mCalendar.set(20, 36);
        int i = this.mClockMode;
        if (i == 2) {
            setText(this.mCalendar.format(this.mContext.getString(R.string.status_bar_clock_date_time_format)));
        } else if (i == 1) {
            setText(this.mCalendar.format(this.mContext.getString(R.string.status_bar_clock_date_format)));
        } else {
            ReceiverInfo receiverInfo = sReceiverInfo.get();
            if (receiverInfo != null) {
                setText(DateUtils.formatDateTime(this.mCalendar.getTimeInMillis(), receiverInfo.getTimeFormat() | 12));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        Iterator it = this.mVisibilityListeners.iterator();
        while (it.hasNext()) {
            ((ClockVisibilityListener) it.next()).onClockVisibilityChanged(isShown());
        }
    }

    public void addVisibilityListener(ClockVisibilityListener clockVisibilityListener) {
        this.mVisibilityListeners.add(clockVisibilityListener);
    }

    public void removeVisibilityListener(ClockVisibilityListener clockVisibilityListener) {
        this.mVisibilityListeners.remove(clockVisibilityListener);
    }
}
