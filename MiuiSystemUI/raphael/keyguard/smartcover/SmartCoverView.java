package com.android.keyguard.smartcover;

import android.content.Context;
import android.net.Uri;
import android.provider.CallLog;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.keyguard.smartcover.ContentProviderBinder;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class SmartCoverView extends RelativeLayout implements ContentProviderBinder.QueryCompleteListener {
    private Uri SMS_INBOX_URI = Uri.parse("content://sms");
    private ArrayList<ContentProviderBinder> mBinders = new ArrayList<>();
    protected boolean mCharging;
    protected boolean mFull;
    protected int mLevel;
    protected int mLowBatteryWarningLevel;
    protected int mMissCallNum;
    protected boolean mShowMissCall;
    protected boolean mShowSms;
    protected int mSmsNum;

    /* access modifiers changed from: protected */
    public abstract void refresh();

    public SmartCoverView(Context context) {
        super(context);
    }

    public SmartCoverView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLowBatteryWarningLevel = this.mContext.getResources().getInteger(285868035);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        fillMissedCall();
        fillUnreadSms();
        Iterator<ContentProviderBinder> it = this.mBinders.iterator();
        while (it.hasNext()) {
            it.next().init();
        }
    }

    private void fillMissedCall() {
        ContentProviderBinder.Builder addContentProviderBinder = addContentProviderBinder(CallLog.Calls.CONTENT_URI);
        addContentProviderBinder.setColumns(new String[]{"number"});
        addContentProviderBinder.setWhere("type=3 AND new=1");
        addContentProviderBinder.setCountName("call_missed_count");
    }

    private void fillUnreadSms() {
        ContentProviderBinder.Builder addContentProviderBinder = addContentProviderBinder(this.SMS_INBOX_URI);
        addContentProviderBinder.setColumns((String[]) null);
        addContentProviderBinder.setWhere("seen=0 AND read=0");
        addContentProviderBinder.setCountName("sms_unread_count");
    }

    public ContentProviderBinder.Builder addContentProviderBinder(Uri uri) {
        ContentProviderBinder contentProviderBinder = new ContentProviderBinder(this.mContext);
        contentProviderBinder.setQueryCompleteListener(this);
        contentProviderBinder.setUri(uri);
        this.mBinders.add(contentProviderBinder);
        return new ContentProviderBinder.Builder(contentProviderBinder);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Iterator<ContentProviderBinder> it = this.mBinders.iterator();
        while (it.hasNext()) {
            it.next().finish();
        }
    }

    public void onQueryCompleted(Uri uri, int i) {
        boolean z = true;
        if (CallLog.Calls.CONTENT_URI.equals(uri)) {
            if (i <= 0) {
                z = false;
            }
            this.mShowMissCall = z;
            this.mMissCallNum = i;
        } else if (this.SMS_INBOX_URI.equals(uri)) {
            if (i <= 0) {
                z = false;
            }
            this.mShowSms = z;
            this.mSmsNum = i;
        }
        refresh();
    }

    public void onBatteryInfoRefresh(boolean z, boolean z2, int i) {
        this.mCharging = z;
        this.mFull = z2;
        this.mLevel = i;
        refresh();
    }
}
