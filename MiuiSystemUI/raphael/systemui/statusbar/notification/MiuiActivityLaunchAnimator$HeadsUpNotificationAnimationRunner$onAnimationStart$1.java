package com.android.systemui.statusbar.notification;

import android.view.IRemoteAnimationFinishedCallback;
import android.view.RemoteAnimationTarget;
import com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator;

/* compiled from: MiuiActivityLaunchAnimator.kt */
final class MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationStart$1 implements Runnable {
    final /* synthetic */ IRemoteAnimationFinishedCallback $finishedCallback;
    final /* synthetic */ RemoteAnimationTarget[] $remoteAnimationTargets;
    final /* synthetic */ MiuiActivityLaunchAnimator.HeadsUpNotificationAnimationRunner this$0;

    MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationStart$1(MiuiActivityLaunchAnimator.HeadsUpNotificationAnimationRunner headsUpNotificationAnimationRunner, RemoteAnimationTarget[] remoteAnimationTargetArr, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
        this.this$0 = headsUpNotificationAnimationRunner;
        this.$remoteAnimationTargets = remoteAnimationTargetArr;
        this.$finishedCallback = iRemoteAnimationFinishedCallback;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x009d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        /*
            r18 = this;
            r0 = r18
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r1 = r0.this$0
            android.view.RemoteAnimationTarget[] r2 = r0.$remoteAnimationTargets
            android.view.RemoteAnimationTarget r1 = r1.getPrimaryRemoteAnimationTarget(r2)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r2 = r0.this$0
            android.view.RemoteAnimationTarget[] r3 = r0.$remoteAnimationTargets
            android.view.RemoteAnimationTarget r2 = r2.getClosingRemoteAnimationTarget(r3)
            r10 = 1
            r11 = 0
            if (r1 == 0) goto L_0x0047
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            android.graphics.Rect r4 = r1.localBounds
            int r4 = r4.top
            if (r4 != 0) goto L_0x0034
            android.graphics.Rect r4 = r1.screenSpaceBounds
            int r4 = r4.height()
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r5 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r5 = r5.this$0
            com.android.systemui.statusbar.phone.NotificationPanelViewController r5 = r5.mNotificationPanel
            int r5 = r5.getHeight()
            if (r4 < r5) goto L_0x0034
            r4 = r10
            goto L_0x0035
        L_0x0034:
            r4 = r11
        L_0x0035:
            r3.mIsFullScreenLaunch = r4
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            boolean r3 = r3.mIsFullScreenLaunch
            if (r3 == 0) goto L_0x0047
            boolean r3 = r1.isTranslucent
            if (r3 == 0) goto L_0x0045
            goto L_0x0047
        L_0x0045:
            r3 = r11
            goto L_0x0048
        L_0x0047:
            r3 = r10
        L_0x0048:
            if (r3 == 0) goto L_0x009d
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "bail, target="
            r2.append(r3)
            r2.append(r1)
            java.lang.String r3 = ", fullscreen="
            r2.append(r3)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            boolean r3 = r3.mIsFullScreenLaunch
            r2.append(r3)
            java.lang.String r3 = ", isTranslucent="
            r2.append(r3)
            if (r1 == 0) goto L_0x0073
            boolean r1 = r1.isTranslucent
            java.lang.Boolean r1 = java.lang.Boolean.valueOf(r1)
            goto L_0x0075
        L_0x0073:
            java.lang.Boolean r1 = java.lang.Boolean.FALSE
        L_0x0075:
            r2.append(r1)
            java.lang.String r1 = r2.toString()
            java.lang.String r2 = "MiuiActivityLaunchAnimator"
            miui.util.Log.i(r2, r1)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r1 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r1 = r1.this$0
            r1.setAnimationPending(r11)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r1 = r0.this$0
            android.view.IRemoteAnimationFinishedCallback r2 = r0.$finishedCallback
            r1.invokeCallback(r2)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r0 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = r0.this$0
            com.android.systemui.statusbar.phone.NotificationPanelViewController r0 = r0.mNotificationPanel
            r1 = 1065353216(0x3f800000, float:1.0)
            r0.collapse(r11, r1)
            return
        L_0x009d:
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            r3.setupDimLayer()
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r3 = r3.this$0
            if (r2 == 0) goto L_0x00b1
            android.app.WindowConfiguration r4 = r2.windowConfiguration
            if (r4 == 0) goto L_0x00b1
            int r4 = r4.getActivityType()
            goto L_0x00b2
        L_0x00b1:
            r4 = r11
        L_0x00b2:
            r3.mClosingActivityType = r4
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpExpandAnimationParameters r3 = r3.mParams
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            int[] r4 = r4.getLocationOnScreen()
            r3.startPosition = r4
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpExpandAnimationParameters r3 = r3.mParams
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            float r4 = r4.getTranslationZ()
            r3.startTranslationZ = r4
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpExpandAnimationParameters r3 = r3.mParams
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            int r4 = r4.getClipTopAmount()
            r3.startClipTopAmount = r4
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = r3.mSourceNotification
            boolean r3 = r3.isChildInGroup()
            r12 = 0
            if (r3 == 0) goto L_0x0133
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = r3.mSourceNotification
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = r3.getNotificationParent()
            java.lang.String r4 = "mSourceNotification\n    … .getNotificationParent()"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r3, r4)
            int r3 = r3.getClipTopAmount()
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpExpandAnimationParameters r4 = r4.mParams
            r4.parentStartClipTopAmount = r3
            if (r3 == 0) goto L_0x0133
            float r3 = (float) r3
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            float r4 = r4.getTranslationY()
            float r3 = r3 - r4
            int r4 = (r3 > r12 ? 1 : (r3 == r12 ? 0 : -1))
            if (r4 <= 0) goto L_0x0133
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpExpandAnimationParameters r4 = r4.mParams
            double r5 = (double) r3
            double r5 = java.lang.Math.ceil(r5)
            int r3 = (int) r5
            r4.startClipTopAmount = r3
        L_0x0133:
            if (r1 == 0) goto L_0x0297
            android.graphics.Rect r3 = r1.screenSpaceBounds
            int r13 = r3.width()
            android.graphics.Rect r3 = r1.screenSpaceBounds
            int r14 = r3.height()
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = r3.mSourceNotification
            int r3 = r3.getActualHeight()
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            int r4 = r4.getClipBottomAmount()
            int r3 = r3 - r4
            float r3 = (float) r3
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            float r4 = r4.getScaleY()
            float r3 = r3 * r4
            int r7 = (int) r3
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = r3.mSourceNotification
            int r3 = r3.getWidth()
            float r3 = (float) r3
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r4 = r0.this$0
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r4.mSourceNotification
            float r4 = r4.getScaleX()
            float r3 = r3 * r4
            int r6 = (int) r3
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpExpandAnimationParameters r3 = r3.mParams
            int[] r3 = r3.startPosition
            r3 = r3[r10]
            int r4 = r7 / 2
            int r5 = r3 + r4
            int r15 = r14 / 2
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r3 = r3.this$0
            int r3 = r3.mClosingActivityType
            r9 = 2
            if (r3 != r9) goto L_0x0198
            r16 = r10
            goto L_0x019a
        L_0x0198:
            r16 = r11
        L_0x019a:
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            r3.setExpandAnimationRunning(r10)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r3 = r0.this$0
            r4 = r13
            r8 = r1
            r17 = r9
            r9 = r2
            miuix.animation.IStateStyle r3 = r3.setupExpandAnimation(r4, r5, r6, r7, r8, r9)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationStart$1$1 r4 = new com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationStart$1$1
            r4.<init>(r0, r13, r1, r2)
            miuix.animation.IStateStyle r1 = r3.addListener(r4)
            r3 = 3
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r5 = "y"
            r4[r11] = r5
            java.lang.Integer r5 = java.lang.Integer.valueOf(r15)
            r4[r10] = r5
            r5 = 1065185444(0x3f7d70a4, float:0.99)
            r6 = 1050253722(0x3e99999a, float:0.3)
            miuix.animation.base.AnimConfig r7 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r6)
            r4[r17] = r7
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r7 = "alpha"
            r4[r11] = r7
            java.lang.Float r7 = java.lang.Float.valueOf(r12)
            r4[r10] = r7
            r7 = 1041865114(0x3e19999a, float:0.15)
            miuix.animation.base.AnimConfig r7 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r7)
            r4[r17] = r7
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r7 = "width"
            r4[r11] = r7
            java.lang.Integer r7 = java.lang.Integer.valueOf(r13)
            r4[r10] = r7
            r7 = 1053609165(0x3ecccccd, float:0.4)
            miuix.animation.base.AnimConfig r8 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r7)
            r4[r17] = r8
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r8 = "height"
            r4[r11] = r8
            java.lang.Integer r8 = java.lang.Integer.valueOf(r14)
            r4[r10] = r8
            r8 = 1051931443(0x3eb33333, float:0.35)
            miuix.animation.base.AnimConfig r8 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r8)
            r4[r17] = r8
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r8 = "corner"
            r4[r11] = r8
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r8 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r8 = r8.this$0
            float r8 = r8.mWindowCornerRadius
            java.lang.Float r8 = java.lang.Float.valueOf(r8)
            r4[r10] = r8
            miuix.animation.base.AnimConfig r8 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r6)
            r4[r17] = r8
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r8 = "dimBehind"
            r4[r11] = r8
            r8 = 1061997773(0x3f4ccccd, float:0.8)
            java.lang.Float r8 = java.lang.Float.valueOf(r8)
            r4[r10] = r8
            miuix.animation.base.AnimConfig r8 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r6)
            r4[r17] = r8
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r4 = new java.lang.Object[r3]
            java.lang.String r8 = "closingX"
            r4[r11] = r8
            if (r2 != 0) goto L_0x025c
            r2 = r11
            goto L_0x0263
        L_0x025c:
            android.graphics.Rect r2 = r2.screenSpaceBounds
            int r2 = r2.width()
            int r2 = -r2
        L_0x0263:
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r4[r10] = r2
            miuix.animation.base.AnimConfig r2 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r7)
            r4[r17] = r2
            miuix.animation.IStateStyle r1 = r1.to(r4)
            java.lang.Object[] r2 = new java.lang.Object[r3]
            java.lang.String r3 = "closingScale"
            r2[r11] = r3
            r3 = 1063675494(0x3f666666, float:0.9)
            java.lang.Float r3 = java.lang.Float.valueOf(r3)
            r2[r10] = r3
            if (r16 == 0) goto L_0x0285
            goto L_0x0286
        L_0x0285:
            r6 = r7
        L_0x0286:
            miuix.animation.base.AnimConfig r3 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimatorKt.springEase(r5, r6)
            r2[r17] = r3
            r1.to(r2)
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner r0 = r0.this$0
            com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = r0.this$0
            r0.setAnimationPending(r11)
            return
        L_0x0297:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            r0 = 0
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationStart$1.run():void");
    }
}
