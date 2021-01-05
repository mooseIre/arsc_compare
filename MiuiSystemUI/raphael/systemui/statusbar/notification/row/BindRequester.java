package com.android.systemui.statusbar.notification.row;

import androidx.core.os.CancellationSignal;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;

public abstract class BindRequester {
    private BindRequestListener mBindRequestListener;

    public interface BindRequestListener {
        void onBindRequest(NotificationEntry notificationEntry, CancellationSignal cancellationSignal, NotifBindPipeline.BindCallback bindCallback);
    }

    public final CancellationSignal requestRebind(NotificationEntry notificationEntry, NotifBindPipeline.BindCallback bindCallback) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        BindRequestListener bindRequestListener = this.mBindRequestListener;
        if (bindRequestListener != null) {
            bindRequestListener.onBindRequest(notificationEntry, cancellationSignal, bindCallback);
        }
        return cancellationSignal;
    }

    /* access modifiers changed from: package-private */
    public final void setBindRequestListener(BindRequestListener bindRequestListener) {
        this.mBindRequestListener = bindRequestListener;
    }
}
