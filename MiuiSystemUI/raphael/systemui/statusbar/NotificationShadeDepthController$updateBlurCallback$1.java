package com.android.systemui.statusbar;

import android.view.Choreographer;

/* compiled from: NotificationShadeDepthController.kt */
final class NotificationShadeDepthController$updateBlurCallback$1 implements Choreographer.FrameCallback {
    final /* synthetic */ NotificationShadeDepthController this$0;

    NotificationShadeDepthController$updateBlurCallback$1(NotificationShadeDepthController notificationShadeDepthController) {
        this.this$0 = notificationShadeDepthController;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x010d A[LOOP:0: B:27:0x0107->B:29:0x010d, LOOP_END] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void doFrame(long r4) {
        /*
            r3 = this;
            com.android.systemui.statusbar.NotificationShadeDepthController r4 = r3.this$0
            r5 = 0
            r4.updateScheduled = r5
            com.android.systemui.statusbar.NotificationShadeDepthController r4 = r3.this$0
            com.android.systemui.statusbar.NotificationShadeDepthController$DepthAnimation r4 = r4.getShadeAnimation()
            int r4 = r4.getRadius()
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            com.android.systemui.statusbar.BlurUtils r0 = r0.blurUtils
            int r0 = r0.getMinBlurRadius()
            com.android.systemui.statusbar.NotificationShadeDepthController r1 = r3.this$0
            com.android.systemui.statusbar.BlurUtils r1 = r1.blurUtils
            int r1 = r1.getMaxBlurRadius()
            int r4 = android.util.MathUtils.constrain(r4, r0, r1)
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            com.android.systemui.statusbar.NotificationShadeDepthController$DepthAnimation r0 = r0.getShadeSpring()
            int r0 = r0.getRadius()
            float r0 = (float) r0
            r1 = 1053609165(0x3ecccccd, float:0.4)
            float r0 = r0 * r1
            float r4 = (float) r4
            r1 = 1058642330(0x3f19999a, float:0.6)
            float r4 = r4 * r1
            float r0 = r0 + r4
            int r4 = (int) r0
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            int r0 = r0.wakeAndUnlockBlurRadius
            int r4 = java.lang.Math.max(r4, r0)
            float r4 = (float) r4
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            com.android.systemui.statusbar.NotificationShadeDepthController$DepthAnimation r0 = r0.getBrightnessMirrorSpring()
            float r0 = r0.getRatio()
            r1 = 1065353216(0x3f800000, float:1.0)
            float r0 = r1 - r0
            float r4 = r4 * r0
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            com.android.systemui.statusbar.notification.ActivityLaunchAnimator$ExpandAnimationParameters r0 = r0.getNotificationLaunchAnimationParams()
            r2 = 0
            if (r0 == 0) goto L_0x0064
            float r0 = r0.linearProgress
            goto L_0x0065
        L_0x0064:
            r0 = r2
        L_0x0065:
            float r1 = r1 - r0
            float r1 = r1 * r1
            float r4 = r4 * r1
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            boolean r0 = r0.ignoreShadeBlurUntilHidden
            if (r0 == 0) goto L_0x0079
            int r0 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            if (r0 != 0) goto L_0x007a
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            r0.ignoreShadeBlurUntilHidden = r5
        L_0x0079:
            r2 = r4
        L_0x007a:
            com.android.systemui.statusbar.NotificationShadeDepthController r4 = r3.this$0
            com.android.systemui.statusbar.NotificationShadeDepthController$DepthAnimation r4 = r4.getGlobalActionsSpring()
            int r4 = r4.getRadius()
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            boolean r0 = r0.getShowingHomeControls()
            if (r0 == 0) goto L_0x008d
            r4 = r5
        L_0x008d:
            int r0 = (int) r2
            int r4 = java.lang.Math.max(r0, r4)
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            boolean r0 = r0.scrimsVisible
            if (r0 == 0) goto L_0x009b
            goto L_0x009c
        L_0x009b:
            r5 = r4
        L_0x009c:
            com.android.systemui.statusbar.NotificationShadeDepthController r4 = r3.this$0
            com.android.systemui.statusbar.BlurUtils r4 = r4.blurUtils
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            android.view.View r0 = r0.blurRoot
            if (r0 == 0) goto L_0x00b1
            android.view.ViewRootImpl r0 = r0.getViewRootImpl()
            if (r0 == 0) goto L_0x00b1
            goto L_0x00bb
        L_0x00b1:
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            android.view.View r0 = r0.getRoot()
            android.view.ViewRootImpl r0 = r0.getViewRootImpl()
        L_0x00bb:
            r4.applyBlur(r0, r5)
            com.android.systemui.statusbar.NotificationShadeDepthController r4 = r3.this$0
            com.android.systemui.statusbar.BlurUtils r4 = r4.blurUtils
            float r4 = r4.ratioOfBlurRadius(r5)
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0     // Catch:{ IllegalArgumentException -> 0x00dc }
            android.app.WallpaperManager r0 = r0.wallpaperManager     // Catch:{ IllegalArgumentException -> 0x00dc }
            com.android.systemui.statusbar.NotificationShadeDepthController r1 = r3.this$0     // Catch:{ IllegalArgumentException -> 0x00dc }
            android.view.View r1 = r1.getRoot()     // Catch:{ IllegalArgumentException -> 0x00dc }
            android.os.IBinder r1 = r1.getWindowToken()     // Catch:{ IllegalArgumentException -> 0x00dc }
            r0.setWallpaperZoomOut(r1, r4)     // Catch:{ IllegalArgumentException -> 0x00dc }
            goto L_0x00fd
        L_0x00dc:
            r0 = move-exception
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "Can't set zoom. Window is gone: "
            r1.append(r2)
            com.android.systemui.statusbar.NotificationShadeDepthController r2 = r3.this$0
            android.view.View r2 = r2.getRoot()
            android.os.IBinder r2 = r2.getWindowToken()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "DepthController"
            android.util.Log.w(r2, r1, r0)
        L_0x00fd:
            com.android.systemui.statusbar.NotificationShadeDepthController r0 = r3.this$0
            java.util.List r0 = r0.listeners
            java.util.Iterator r0 = r0.iterator()
        L_0x0107:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0117
            java.lang.Object r1 = r0.next()
            com.android.systemui.statusbar.NotificationShadeDepthController$DepthListener r1 = (com.android.systemui.statusbar.NotificationShadeDepthController.DepthListener) r1
            r1.onWallpaperZoomOutChanged(r4)
            goto L_0x0107
        L_0x0117:
            com.android.systemui.statusbar.NotificationShadeDepthController r3 = r3.this$0
            com.android.systemui.statusbar.phone.NotificationShadeWindowController r3 = r3.notificationShadeWindowController
            r3.setBackgroundBlurRadius(r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.NotificationShadeDepthController$updateBlurCallback$1.doFrame(long):void");
    }
}
