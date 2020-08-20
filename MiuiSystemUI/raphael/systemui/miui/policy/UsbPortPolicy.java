package com.android.systemui.miui.policy;

import android.app.Notification;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import com.android.systemui.util.NotificationChannels;

public class UsbPortPolicy extends SystemUI {
    private boolean mIsUsbShortCircuit;
    private NotificationManager mNotificationManager;
    private Notification.Builder mUsbPortOverheatedBuilder;
    private Notification.Builder mUsbPortShortCircuitBuilder;

    public void start() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        new UsbReceiver() {
            /* access modifiers changed from: protected */
            public void onUsbOverheatedStateChanged(boolean z) {
                UsbPortPolicy.this.onUsbOverheatedStateChanged(z);
            }

            /* access modifiers changed from: protected */
            public void onUsbShortCircuitChanged(boolean z) {
                UsbPortPolicy.this.onUsbShortCircuitChanged(z);
            }
        }.register(this.mContext);
    }

    private void ensureOverheatNotificationBuilder() {
        if (this.mUsbPortOverheatedBuilder == null) {
            Context context = this.mContext;
            PendingIntent activityAsUser = PendingIntent.getActivityAsUser(context, 0, new Intent(context, UsbPortOverheatActivity.class), 134217728, (Bundle) null, UserHandle.CURRENT);
            this.mUsbPortOverheatedBuilder = NotificationCompat.newBuilder(this.mContext, NotificationChannels.ALERTS).setContentTitle(this.mContext.getString(R.string.usb_port_overheated_title)).setContentText(this.mContext.getString(R.string.usb_port_overheated_notification_content)).setSmallIcon(R.drawable.ic_usb_port_warning).setLargeIcon(Icon.createWithResource(this.mContext, R.drawable.ic_usb_port_warning_large)).setContentIntent(activityAsUser).setFullScreenIntent(activityAsUser, true).setOngoing(true);
        }
    }

    private void ensureShortCircuitNotificationBuilder() {
        if (this.mUsbPortShortCircuitBuilder == null) {
            Context context = this.mContext;
            PendingIntent activityAsUser = PendingIntent.getActivityAsUser(context, 0, new Intent(context, UsbPortShortCircuitActivity.class), 0, (Bundle) null, UserHandle.CURRENT);
            this.mUsbPortShortCircuitBuilder = NotificationCompat.newBuilder(this.mContext, NotificationChannels.ALERTS).setContentTitle(this.mContext.getString(R.string.usb_port_short_circuit_title)).setContentText(this.mContext.getString(R.string.usb_port_short_circuit_notification_content)).setSmallIcon(R.drawable.ic_usb_port_warning).setLargeIcon(Icon.createWithResource(this.mContext, R.drawable.ic_usb_port_warning_large)).setContentIntent(activityAsUser).setFullScreenIntent(activityAsUser, true).setOngoing(true);
        }
    }

    /* access modifiers changed from: private */
    public void onUsbOverheatedStateChanged(boolean z) {
        if (z) {
            ensureOverheatNotificationBuilder();
            this.mNotificationManager.notify("USB_PORT", 1, this.mUsbPortOverheatedBuilder.build());
            return;
        }
        this.mNotificationManager.cancel("USB_PORT", 1);
    }

    /* access modifiers changed from: private */
    public void onUsbShortCircuitChanged(boolean z) {
        if (z) {
            ensureShortCircuitNotificationBuilder();
            this.mNotificationManager.notify("USB_PORT", 0, this.mUsbPortShortCircuitBuilder.build());
        } else {
            this.mNotificationManager.cancel("USB_PORT", 0);
            if (this.mIsUsbShortCircuit) {
                Intent intent = new Intent(this.mContext, UsbPortRecoveredActivity.class);
                intent.setFlags(268435456);
                this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            }
        }
        this.mIsUsbShortCircuit = z;
    }

    static abstract class UsbReceiver extends BroadcastReceiver {
        /* access modifiers changed from: protected */
        public abstract void onUsbOverheatedStateChanged(boolean z);

        /* access modifiers changed from: protected */
        public abstract void onUsbShortCircuitChanged(boolean z);

        UsbReceiver() {
        }

        public final void onReceive(Context context, Intent intent) {
            boolean z = true;
            if ("miui.intent.action.ACTION_CONNECTOR_HEALTH_TRIGGER".equals(intent.getAction())) {
                if (intent.getIntExtra("miui.intent.extra.USBCONNECTOR_HEALTH", 0) == 0) {
                    z = false;
                }
                onUsbOverheatedStateChanged(z);
            } else if ("miui.intent.action.ACTION_LIQUID_DETECTION_TRIGGER".equals(intent.getAction())) {
                if (intent.getIntExtra("miui.intent.extra.USB_LIQUID_DETECTION", 0) == 0) {
                    z = false;
                }
                onUsbShortCircuitChanged(z);
            }
        }

        public void register(Context context) {
            register(context, 0);
        }

        public void register(Context context, int i) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("miui.intent.action.ACTION_CONNECTOR_HEALTH_TRIGGER");
            intentFilter.addAction("miui.intent.action.ACTION_LIQUID_DETECTION_TRIGGER");
            intentFilter.setPriority(i);
            context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        }

        public void unregister(Context context) {
            context.unregisterReceiver(this);
        }
    }
}
