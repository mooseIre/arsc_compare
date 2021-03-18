package com.android.systemui.statusbar.phone;

import android.view.View;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public class LightsOutNotifController {
    @VisibleForTesting
    int mAppearance;
    private final CommandQueue.Callbacks mCallback = new CommandQueue.Callbacks() {
        /* class com.android.systemui.statusbar.phone.LightsOutNotifController.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
        public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
            if (i == LightsOutNotifController.this.mDisplayId) {
                LightsOutNotifController lightsOutNotifController = LightsOutNotifController.this;
                lightsOutNotifController.mAppearance = i2;
                lightsOutNotifController.updateLightsOutView();
            }
        }
    };
    private final CommandQueue mCommandQueue;
    private int mDisplayId;
    private final NotificationEntryListener mEntryListener = new NotificationEntryListener() {
        /* class com.android.systemui.statusbar.phone.LightsOutNotifController.AnonymousClass3 */

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onNotificationAdded(NotificationEntry notificationEntry) {
            LightsOutNotifController.this.updateLightsOutView();
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onPostEntryUpdated(NotificationEntry notificationEntry) {
            LightsOutNotifController.this.updateLightsOutView();
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
            LightsOutNotifController.this.updateLightsOutView();
        }
    };
    private final NotificationEntryManager mEntryManager;
    private View mLightsOutNotifView;
    private final WindowManager mWindowManager;

    LightsOutNotifController(WindowManager windowManager, NotificationEntryManager notificationEntryManager, CommandQueue commandQueue) {
        this.mWindowManager = windowManager;
        this.mEntryManager = notificationEntryManager;
        this.mCommandQueue = commandQueue;
    }

    /* access modifiers changed from: package-private */
    public void setLightsOutNotifView(View view) {
        destroy();
        this.mLightsOutNotifView = view;
        if (view != null) {
            view.setVisibility(8);
            this.mLightsOutNotifView.setAlpha(0.0f);
            init();
        }
    }

    private void destroy() {
        this.mEntryManager.removeNotificationEntryListener(this.mEntryListener);
        this.mCommandQueue.removeCallback(this.mCallback);
    }

    private void init() {
        this.mDisplayId = this.mWindowManager.getDefaultDisplay().getDisplayId();
        this.mEntryManager.addNotificationEntryListener(this.mEntryListener);
        this.mCommandQueue.addCallback(this.mCallback);
        updateLightsOutView();
    }

    private boolean hasActiveNotifications() {
        return this.mEntryManager.hasActiveNotifications();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0005, code lost:
        r0 = shouldShowDot();
     */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateLightsOutView() {
        /*
            r4 = this;
            android.view.View r0 = r4.mLightsOutNotifView
            if (r0 != 0) goto L_0x0005
            return
        L_0x0005:
            boolean r0 = r4.shouldShowDot()
            boolean r1 = r4.isShowingDot()
            if (r0 == r1) goto L_0x004d
            r1 = 0
            if (r0 == 0) goto L_0x001d
            android.view.View r2 = r4.mLightsOutNotifView
            r2.setAlpha(r1)
            android.view.View r2 = r4.mLightsOutNotifView
            r3 = 0
            r2.setVisibility(r3)
        L_0x001d:
            android.view.View r2 = r4.mLightsOutNotifView
            android.view.ViewPropertyAnimator r2 = r2.animate()
            if (r0 == 0) goto L_0x0027
            r1 = 1065353216(0x3f800000, float:1.0)
        L_0x0027:
            android.view.ViewPropertyAnimator r1 = r2.alpha(r1)
            if (r0 == 0) goto L_0x0030
            r2 = 750(0x2ee, double:3.705E-321)
            goto L_0x0032
        L_0x0030:
            r2 = 250(0xfa, double:1.235E-321)
        L_0x0032:
            android.view.ViewPropertyAnimator r1 = r1.setDuration(r2)
            android.view.animation.AccelerateInterpolator r2 = new android.view.animation.AccelerateInterpolator
            r3 = 1073741824(0x40000000, float:2.0)
            r2.<init>(r3)
            android.view.ViewPropertyAnimator r1 = r1.setInterpolator(r2)
            com.android.systemui.statusbar.phone.LightsOutNotifController$1 r2 = new com.android.systemui.statusbar.phone.LightsOutNotifController$1
            r2.<init>(r0)
            android.view.ViewPropertyAnimator r4 = r1.setListener(r2)
            r4.start()
        L_0x004d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.LightsOutNotifController.updateLightsOutView():void");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isShowingDot() {
        return this.mLightsOutNotifView.getVisibility() == 0 && this.mLightsOutNotifView.getAlpha() == 1.0f;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldShowDot() {
        return hasActiveNotifications() && areLightsOut();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean areLightsOut() {
        return (this.mAppearance & 4) != 0;
    }
}
