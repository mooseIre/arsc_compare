package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.InflationTask;
import com.android.systemui.statusbar.NotificationData;

public class RowInflaterTask implements InflationTask, AsyncLayoutInflater.OnInflateFinishedListener {
    private boolean mCancelled;
    private NotificationData.Entry mEntry;
    private RowInflationFinishedListener mListener;

    public interface RowInflationFinishedListener {
        void onInflationFinished(ExpandableNotificationRow expandableNotificationRow);
    }

    public void supersedeTask(InflationTask inflationTask) {
    }

    public void inflate(Context context, ViewGroup viewGroup, NotificationData.Entry entry, RowInflationFinishedListener rowInflationFinishedListener) {
        this.mListener = rowInflationFinishedListener;
        AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(context);
        this.mEntry = entry;
        entry.setInflationTask(this);
        asyncLayoutInflater.inflate(R.layout.status_bar_notification_row, viewGroup, this);
    }

    public void abort() {
        this.mCancelled = true;
    }

    public void onInflateFinished(View view, int i, ViewGroup viewGroup) {
        if (!this.mCancelled) {
            this.mEntry.onInflationTaskFinished();
            this.mListener.onInflationFinished((ExpandableNotificationRow) view);
        }
    }
}
