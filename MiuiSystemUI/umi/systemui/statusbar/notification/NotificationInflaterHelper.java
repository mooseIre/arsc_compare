package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationCompat;
import android.app.PendingIntent;
import android.os.CancellationSignal;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationContentView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.InCallNotificationView;
import com.android.systemui.statusbar.notification.NotificationInflater;
import com.android.systemui.util.Assert;
import java.util.HashMap;

public class NotificationInflaterHelper {
    static {
        new NotificationInflater.InflationExecutor();
    }

    private static void onViewApplied(View view, NotificationInflater.InflationProgress inflationProgress, int i, int i2, ExpandableNotificationRow expandableNotificationRow, boolean z, boolean z2, NotificationInflater.InflationCallback inflationCallback, NotificationViewWrapper notificationViewWrapper, HashMap<Integer, CancellationSignal> hashMap, NotificationInflater.ApplyCallback applyCallback, InCallNotificationView.InCallCallback inCallCallback) {
        if (z2) {
            view.setIsRootNamespace(true);
            applyCallback.setResultView(view);
        } else if (notificationViewWrapper != null) {
            notificationViewWrapper.onReinflated();
        }
        hashMap.remove(Integer.valueOf(i2));
    }

    private static void applyRemoteViewSync(NotificationInflater.InflationProgress inflationProgress, int i, int i2, ExpandableNotificationRow expandableNotificationRow, boolean z, boolean z2, RemoteViews.OnClickHandler onClickHandler, NotificationInflater.InflationCallback inflationCallback, NotificationData.Entry entry, NotificationContentView notificationContentView, View view, NotificationViewWrapper notificationViewWrapper, HashMap<Integer, CancellationSignal> hashMap, NotificationInflater.ApplyCallback applyCallback, InCallNotificationView.InCallCallback inCallCallback, Exception exc) {
        View view2;
        NotificationInflater.InflationProgress inflationProgress2 = inflationProgress;
        RemoteViews.OnClickHandler onClickHandler2 = onClickHandler;
        HashMap<Integer, CancellationSignal> hashMap2 = hashMap;
        Exception exc2 = exc;
        try {
            RemoteViews remoteView = applyCallback.getRemoteView();
            if (z2) {
                view2 = remoteView.apply(inflationProgress2.packageContext, notificationContentView, onClickHandler2);
            } else {
                View view3 = view;
                remoteView.reapply(inflationProgress2.packageContext, view3, onClickHandler2);
                view2 = view3;
            }
            onViewApplied(view2, inflationProgress, i, i2, expandableNotificationRow, z, z2, inflationCallback, notificationViewWrapper, hashMap, applyCallback, inCallCallback);
            if (exc2 != null) {
                Log.wtf("NotificationInflater", "Async Inflation failed but normal inflation finished normally.", exc2);
            }
        } catch (Exception e) {
            hashMap2.put(Integer.valueOf(i2), new CancellationSignal());
            handleInflationError(hashMap2, e, entry.notification, inflationCallback);
        }
    }

    public static void applyRemoteView(NotificationInflater.InflationProgress inflationProgress, int i, int i2, ExpandableNotificationRow expandableNotificationRow, boolean z, boolean z2, RemoteViews.OnClickHandler onClickHandler, NotificationInflater.InflationCallback inflationCallback, NotificationData.Entry entry, NotificationContentView notificationContentView, View view, NotificationViewWrapper notificationViewWrapper, HashMap<Integer, CancellationSignal> hashMap, NotificationInflater.ApplyCallback applyCallback, InCallNotificationView.InCallCallback inCallCallback) {
        applyRemoteViewSync(inflationProgress, i, i2, expandableNotificationRow, z, z2, onClickHandler, inflationCallback, entry, notificationContentView, view, notificationViewWrapper, hashMap, applyCallback, inCallCallback, (Exception) null);
    }

    private static void handleInflationError(HashMap<Integer, CancellationSignal> hashMap, Exception exc, StatusBarNotification statusBarNotification, NotificationInflater.InflationCallback inflationCallback) {
        Assert.isMainThread();
        for (CancellationSignal cancel : hashMap.values()) {
            cancel.cancel();
        }
        if (inflationCallback != null) {
            inflationCallback.handleInflationException(statusBarNotification, exc);
        }
    }

    public static RemoteViews createContentView(Notification.Builder builder, boolean z, boolean z2, ExpandableNotificationRow expandableNotificationRow) {
        RemoteViews createContentView = NotificationCompat.createContentView(builder, z, z2);
        applyMiuiAction(createContentView, expandableNotificationRow);
        return createContentView;
    }

    public static RemoteViews createExpandedView(Notification.Builder builder, boolean z, ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.getEntry().notification.isShowMiuiAction() && !expandableNotificationRow.getEntry().notification.isMiuiActionExpandable()) {
            return null;
        }
        RemoteViews createBigContentView = builder.createBigContentView();
        if (createBigContentView != null) {
            return createBigContentView;
        }
        if (!z) {
            return null;
        }
        RemoteViews createContentView = builder.createContentView();
        NotificationCompat.makeHeaderExpanded(createContentView);
        return createContentView;
    }

    public static RemoteViews createHeadsUpView(Notification.Builder builder, boolean z, ExpandableNotificationRow expandableNotificationRow) {
        if (!expandableNotificationRow.getEntry().notification.isShowMiuiAction()) {
            return NotificationCompat.createHeadsUpContentView(builder, z);
        }
        RemoteViews createContentView = builder.createContentView();
        applyMiuiAction(createContentView, expandableNotificationRow);
        return createContentView;
    }

    public static RemoteViews createPublicContentView(Notification.Builder builder, ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.getEntry().notification.getNotification().publicVersion != null) {
            return makePublicContentView(builder);
        }
        return createMiuiPublicView(expandableNotificationRow);
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(3:4|5|6) */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0031, code lost:
        return (android.widget.RemoteViews) r6.getClass().getDeclaredMethod("makePublicContentView", new java.lang.Class[]{java.lang.Boolean.TYPE}).invoke(r6, new java.lang.Object[]{java.lang.Boolean.FALSE});
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0032, code lost:
        return null;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:4:0x0016 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static android.widget.RemoteViews makePublicContentView(android.app.Notification.Builder r6) {
        /*
            java.lang.String r0 = "makePublicContentView"
            r1 = 0
            java.lang.Class r2 = r6.getClass()     // Catch:{ Exception -> 0x0016 }
            java.lang.Class[] r3 = new java.lang.Class[r1]     // Catch:{ Exception -> 0x0016 }
            java.lang.reflect.Method r2 = r2.getDeclaredMethod(r0, r3)     // Catch:{ Exception -> 0x0016 }
            java.lang.Object[] r3 = new java.lang.Object[r1]     // Catch:{ Exception -> 0x0016 }
            java.lang.Object r2 = r2.invoke(r6, r3)     // Catch:{ Exception -> 0x0016 }
            android.widget.RemoteViews r2 = (android.widget.RemoteViews) r2     // Catch:{ Exception -> 0x0016 }
            return r2
        L_0x0016:
            java.lang.Class r2 = r6.getClass()     // Catch:{ Exception -> 0x0032 }
            r3 = 1
            java.lang.Class[] r4 = new java.lang.Class[r3]     // Catch:{ Exception -> 0x0032 }
            java.lang.Class r5 = java.lang.Boolean.TYPE     // Catch:{ Exception -> 0x0032 }
            r4[r1] = r5     // Catch:{ Exception -> 0x0032 }
            java.lang.reflect.Method r0 = r2.getDeclaredMethod(r0, r4)     // Catch:{ Exception -> 0x0032 }
            java.lang.Object[] r2 = new java.lang.Object[r3]     // Catch:{ Exception -> 0x0032 }
            java.lang.Boolean r3 = java.lang.Boolean.FALSE     // Catch:{ Exception -> 0x0032 }
            r2[r1] = r3     // Catch:{ Exception -> 0x0032 }
            java.lang.Object r6 = r0.invoke(r6, r2)     // Catch:{ Exception -> 0x0032 }
            android.widget.RemoteViews r6 = (android.widget.RemoteViews) r6     // Catch:{ Exception -> 0x0032 }
            return r6
        L_0x0032:
            r6 = 0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.NotificationInflaterHelper.makePublicContentView(android.app.Notification$Builder):android.widget.RemoteViews");
    }

    public static RemoteViews createAmbientView(Notification.Builder builder, boolean z) {
        return NotificationCompat.makeAmbientNotification(builder, z);
    }

    private static void applyMiuiAction(RemoteViews remoteViews, ExpandableNotificationRow expandableNotificationRow) {
        Notification.Action[] actionArr;
        if (remoteViews != null && MiuiNotificationCompat.isShowMiuiAction(expandableNotificationRow.getEntry().notification.getNotification()) && (actionArr = expandableNotificationRow.getEntry().notification.getNotification().actions) != null && actionArr.length > 0) {
            Notification.Action action = actionArr[0];
            PendingIntent pendingIntent = action.actionIntent;
            if (pendingIntent != null) {
                remoteViews.setOnClickPendingIntent(16909189, pendingIntent);
            }
            if (action.getRemoteInputs() != null) {
                remoteViews.setRemoteInputs(16909189, action.getRemoteInputs());
            }
        }
    }

    private static RemoteViews createMiuiPublicView(ExpandableNotificationRow expandableNotificationRow) {
        ExpandedNotification expandedNotification = expandableNotificationRow.getEntry().notification;
        int notificationChildCount = expandableNotificationRow.getChildrenContainer() != null ? expandableNotificationRow.getChildrenContainer().getNotificationChildCount() : 1;
        Notification.Builder contentText = new Notification.Builder(expandableNotificationRow.getContext()).setWhen(expandedNotification.getNotification().when).setShowWhen(true).setSmallIcon(expandedNotification.getNotification().getSmallIcon()).setContentTitle(expandedNotification.getAppName()).setContentText(expandableNotificationRow.getContext().getResources().getQuantityString(R.plurals.new_notifications_msg, notificationChildCount, new Object[]{Integer.valueOf(notificationChildCount)}));
        contentText.build().extras.putString("android.substName", expandedNotification.getAppName());
        return contentText.createContentView();
    }
}
