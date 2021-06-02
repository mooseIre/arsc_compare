package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.policy.DateView;
import java.util.Date;
import java.util.Locale;

public class DateView extends TextView {
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final Date mCurrentTime = new Date();
    private DateFormat mDateFormat;
    private String mDatePattern;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.DateView.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            Handler handler = DateView.this.getHandler();
            if (handler != null) {
                String action = intent.getAction();
                if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.LOCALE_CHANGED".equals(action)) {
                    if ("android.intent.action.LOCALE_CHANGED".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                        handler.post(new Runnable() {
                            /* class com.android.systemui.statusbar.policy.$$Lambda$DateView$1$I3qZI4QmwEIAfQqo2b2oUNiPII */

                            public final void run() {
                                DateView.AnonymousClass1.this.lambda$onReceive$0$DateView$1();
                            }
                        });
                    }
                    handler.post(new Runnable() {
                        /* class com.android.systemui.statusbar.policy.$$Lambda$DateView$1$v1y3JoGtv68dyea2Bk7AdwrkpMI */

                        public final void run() {
                            DateView.AnonymousClass1.this.lambda$onReceive$1$DateView$1();
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$0 */
        public /* synthetic */ void lambda$onReceive$0$DateView$1() {
            DateView.this.mDateFormat = null;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$1 */
        public /* synthetic */ void lambda$onReceive$1$DateView$1() {
            DateView.this.updateClock();
        }
    };
    private String mLastText;

    /* JADX INFO: finally extract failed */
    public DateView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.DateView, 0, 0);
        try {
            this.mDatePattern = obtainStyledAttributes.getString(R$styleable.DateView_datePattern);
            obtainStyledAttributes.recycle();
            if (this.mDatePattern == null) {
                this.mDatePattern = getContext().getString(C0021R$string.system_ui_date_pattern);
            }
            this.mBroadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        updateClock();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mDateFormat = null;
        this.mBroadcastDispatcher.unregisterReceiver(this.mIntentReceiver);
    }

    /* access modifiers changed from: protected */
    public void updateClock() {
        if (this.mDateFormat == null) {
            DateFormat instanceForSkeleton = DateFormat.getInstanceForSkeleton(this.mDatePattern, Locale.getDefault());
            instanceForSkeleton.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            this.mDateFormat = instanceForSkeleton;
        }
        this.mCurrentTime.setTime(System.currentTimeMillis());
        String format = this.mDateFormat.format(this.mCurrentTime);
        if (!format.equals(this.mLastText)) {
            setText(format);
            this.mLastText = format;
        }
    }

    public void setDatePattern(String str) {
        if (!TextUtils.equals(str, this.mDatePattern)) {
            this.mDatePattern = str;
            this.mDateFormat = null;
            if (isAttachedToWindow()) {
                updateClock();
            }
        }
    }
}
