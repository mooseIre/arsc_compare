package com.android.keyguard.smartcover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Calendar;
import miui.date.DateUtils;

public class Clock extends TextView {
    /* access modifiers changed from: private */
    public static Calendar sCalendar;
    private static final ThreadLocal<ReceiverInfo> sReceiverInfo = new ThreadLocal<>();
    private boolean mShowDate;
    private boolean mShowHour;
    private boolean mShowMinute;

    public Clock(Context context) {
        this(context, (AttributeSet) null);
    }

    public Clock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Clock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (sCalendar == null) {
            sCalendar = Calendar.getInstance();
        }
    }

    public void setShowDate(boolean z) {
        if (this.mShowDate != z) {
            this.mShowDate = z;
            updateClock();
        }
    }

    public void setShowHour(boolean z) {
        if (this.mShowHour != z) {
            this.mShowHour = z;
            updateClock();
        }
    }

    public void setShowMinute(boolean z) {
        if (this.mShowMinute != z) {
            this.mShowMinute = z;
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

    /* access modifiers changed from: package-private */
    public final void updateClock() {
        sCalendar.setTimeInMillis(System.currentTimeMillis());
        boolean is24HourFormat = DateFormat.is24HourFormat(this.mContext, KeyguardUpdateMonitor.getCurrentUser());
        if (this.mShowDate) {
            setText(DateFormat.format(this.mContext.getString(is24HourFormat ? R.string.lock_screen_date : R.string.lock_screen_date_12), sCalendar));
        } else if (this.mShowHour) {
            int i = sCalendar.get(11);
            if (!is24HourFormat && i > 12) {
                i -= 12;
            }
            if (!is24HourFormat && i == 0) {
                i = 12;
            }
            setText(String.valueOf(i));
        } else if (this.mShowMinute) {
            setText(String.format("%02d", new Object[]{Integer.valueOf(sCalendar.get(12))}));
        } else {
            setText(DateUtils.formatDateTime(System.currentTimeMillis(), (is24HourFormat ? 32 : 16) | 12 | 64));
        }
    }

    private static class ReceiverInfo {
        private final ArrayList<Clock> mAttachedViews;
        /* access modifiers changed from: private */
        public final Handler mHandler;
        private final BroadcastReceiver mReceiver;
        Runnable mUpdateRunnable;

        private ReceiverInfo() {
            this.mAttachedViews = new ArrayList<>();
            this.mHandler = new Handler();
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                        Calendar unused = Clock.sCalendar = Calendar.getInstance();
                    }
                    ReceiverInfo.this.mHandler.post(ReceiverInfo.this.mUpdateRunnable);
                }
            };
            this.mUpdateRunnable = new Runnable() {
                public void run() {
                    ReceiverInfo.this.updateAll();
                }
            };
        }

        public void addView(Clock clock) {
            boolean isEmpty = this.mAttachedViews.isEmpty();
            this.mAttachedViews.add(clock);
            if (isEmpty) {
                register(clock.getContext().getApplicationContext());
            }
            clock.updateClock();
        }

        public void removeView(Clock clock) {
            this.mAttachedViews.remove(clock);
            if (this.mAttachedViews.isEmpty()) {
                unregister(clock.getContext().getApplicationContext());
            }
        }

        /* access modifiers changed from: package-private */
        public void updateAll() {
            int size = this.mAttachedViews.size();
            for (int i = 0; i < size; i++) {
                this.mAttachedViews.get(i).updateClock();
            }
        }

        /* access modifiers changed from: package-private */
        public void register(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.TIME_TICK");
            intentFilter.addAction("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            intentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        }

        /* access modifiers changed from: package-private */
        public void unregister(Context context) {
            context.unregisterReceiver(this.mReceiver);
            this.mHandler.removeCallbacks(this.mUpdateRunnable);
        }
    }
}
