package com.android.systemui.statusbar.notification;

import android.app.WindowConfiguration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.RemoteException;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import android.view.SurfaceControlCompat;
import android.view.SurfaceSession;
import android.view.View;
import com.android.internal.policy.ScreenDecorationsUtilsCompat;
import com.android.systemui.miui.statusbar.notification.HeadsUpAnimatedStubView;
import com.android.systemui.plugins.R;
import com.android.systemui.shared.system.SyncRtSurfaceTransactionApplier;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import miui.util.Pools;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;

public class MiuiActivityLaunchAnimator {
    private static final boolean ENABLED = (Build.VERSION.SDK_INT > 28);
    /* access modifiers changed from: private */
    public static final Pools.Pool<Matrix> MATRIX_POOL = Pools.createSimplePool(new Pools.Manager<Matrix>() {
        public Matrix createInstance() {
            return new Matrix();
        }

        public void onAcquire(Matrix matrix) {
            super.onAcquire(matrix);
            matrix.reset();
        }
    }, 5);
    private boolean mAnimationPending;
    /* access modifiers changed from: private */
    public boolean mAnimationRunning;
    /* access modifiers changed from: private */
    public Callback mCallback;
    /* access modifiers changed from: private */
    @WindowConfiguration.ActivityType
    public int mClosingActivityType = 0;
    /* access modifiers changed from: private */
    public final HeadsUpAnimatedStubView mHeadsUpStub;
    private boolean mIsLaunchForActivity;
    /* access modifiers changed from: private */
    public final NotificationPanelView mNotificationPanel;
    /* access modifiers changed from: private */
    public final StatusBarWindowView mStatusBarWindow;
    private final Runnable mTimeoutRunnable = new Runnable() {
        public final void run() {
            MiuiActivityLaunchAnimator.this.lambda$new$0$MiuiActivityLaunchAnimator();
        }
    };
    /* access modifiers changed from: private */
    public final float mWindowCornerRadius;

    public interface Callback {
        boolean areLaunchAnimationsEnabled();

        void onExpandAnimationFinished(boolean z);

        void onExpandAnimationTimedOut();

        void onLaunchAnimationCancelled();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiActivityLaunchAnimator() {
        setAnimationPending(false);
        this.mCallback.onExpandAnimationTimedOut();
    }

    public MiuiActivityLaunchAnimator(StatusBarWindowView statusBarWindowView, Callback callback, NotificationPanelView notificationPanelView, HeadsUpAnimatedStubView headsUpAnimatedStubView) {
        this.mNotificationPanel = notificationPanelView;
        this.mStatusBarWindow = statusBarWindowView;
        this.mCallback = callback;
        this.mHeadsUpStub = headsUpAnimatedStubView;
        this.mWindowCornerRadius = ScreenDecorationsUtilsCompat.getWindowCornerRadius(statusBarWindowView.getResources());
    }

    public RemoteAnimationAdapter getLaunchAnimation(View view, boolean z) {
        if (ENABLED && (view instanceof ExpandableNotificationRow) && this.mCallback.areLaunchAnimationsEnabled() && !z) {
            return new RemoteAnimationAdapter(new AnimationRunner((ExpandableNotificationRow) view), 300, 200);
        }
        return null;
    }

    public boolean isAnimationPending() {
        return this.mAnimationPending;
    }

    public void setLaunchResult(int i, boolean z) {
        this.mIsLaunchForActivity = z;
        setAnimationPending((i == 2 || i == 0) && this.mCallback.areLaunchAnimationsEnabled());
    }

    public boolean isLaunchForActivity() {
        return this.mIsLaunchForActivity;
    }

    /* access modifiers changed from: private */
    public void setAnimationPending(boolean z) {
        this.mAnimationPending = z;
        this.mStatusBarWindow.setExpandAnimationPending(z);
        if (z) {
            this.mStatusBarWindow.postDelayed(this.mTimeoutRunnable, 500);
        } else {
            this.mStatusBarWindow.removeCallbacks(this.mTimeoutRunnable);
        }
    }

    public boolean isAnimationRunning() {
        return this.mAnimationRunning;
    }

    class AnimationRunner extends IRemoteAnimationRunner.Stub {
        private float mCornerRadius;
        private SurfaceControl mDimLayer;
        private boolean mIsFullScreenLaunch = true;
        private final float mNotificationCornerRadius;
        private final ExpandAnimationParameters mParams;
        private final ExpandableNotificationRow mSourceNotification;
        private final SyncRtSurfaceTransactionApplier mSyncRtTransactionApplier;
        private final Rect mWindowCrop = new Rect();

        public AnimationRunner(ExpandableNotificationRow expandableNotificationRow) {
            this.mSourceNotification = expandableNotificationRow;
            this.mParams = new ExpandAnimationParameters();
            this.mSyncRtTransactionApplier = new SyncRtSurfaceTransactionApplier(this.mSourceNotification);
            this.mNotificationCornerRadius = expandableNotificationRow.getResources().getDimension(R.dimen.notification_heads_up_bg_radius);
        }

        private void setupDimLayer() {
            Point point = new Point();
            MiuiActivityLaunchAnimator.this.mStatusBarWindow.getDisplay().getRealSize(point);
            SurfaceControl.Builder builder = new SurfaceControl.Builder(new SurfaceSession());
            SurfaceControlCompat.setColorLayer(builder, point.x, point.y);
            this.mDimLayer = builder.setName("SystemUI Notification Dim").build();
        }

        public void onAnimationStart(RemoteAnimationTarget[] remoteAnimationTargetArr, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) throws RemoteException {
            this.mSourceNotification.post(new Runnable(remoteAnimationTargetArr, iRemoteAnimationFinishedCallback) {
                public final /* synthetic */ RemoteAnimationTarget[] f$1;
                public final /* synthetic */ IRemoteAnimationFinishedCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MiuiActivityLaunchAnimator.AnimationRunner.this.lambda$onAnimationStart$0$MiuiActivityLaunchAnimator$AnimationRunner(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0037  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x007b  */
        /* renamed from: lambda$onAnimationStart$0 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public /* synthetic */ void lambda$onAnimationStart$0$MiuiActivityLaunchAnimator$AnimationRunner(android.view.RemoteAnimationTarget[] r20, android.view.IRemoteAnimationFinishedCallback r21) {
            /*
                r19 = this;
                r7 = r19
                android.view.RemoteAnimationTarget r8 = r19.getPrimaryRemoteAnimationTarget(r20)
                android.view.RemoteAnimationTarget r9 = r19.getClosingRemoteAnimationTarget(r20)
                r10 = 1
                r11 = 0
                if (r8 == 0) goto L_0x0034
                android.graphics.Point r0 = r8.position
                int r0 = r0.y
                if (r0 != 0) goto L_0x0028
                android.graphics.Rect r0 = r8.sourceContainerBounds
                int r0 = r0.height()
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r1 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                com.android.systemui.statusbar.phone.NotificationPanelView r1 = r1.mNotificationPanel
                int r1 = r1.getHeight()
                if (r0 < r1) goto L_0x0028
                r0 = r10
                goto L_0x0029
            L_0x0028:
                r0 = r11
            L_0x0029:
                r7.mIsFullScreenLaunch = r0
                if (r0 == 0) goto L_0x0034
                boolean r0 = r8.isTranslucent
                if (r0 == 0) goto L_0x0032
                goto L_0x0034
            L_0x0032:
                r0 = r11
                goto L_0x0035
            L_0x0034:
                r0 = r10
            L_0x0035:
                if (r0 == 0) goto L_0x007b
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "bail, target="
                r0.append(r1)
                r0.append(r8)
                java.lang.String r1 = ", fullscreen="
                r0.append(r1)
                boolean r1 = r7.mIsFullScreenLaunch
                r0.append(r1)
                java.lang.String r1 = ", isTranslucent="
                r0.append(r1)
                if (r8 == 0) goto L_0x0058
                boolean r1 = r8.isTranslucent
                goto L_0x0059
            L_0x0058:
                r1 = r11
            L_0x0059:
                r0.append(r1)
                java.lang.String r0 = r0.toString()
                java.lang.String r1 = "MiuiActivityLaunchAnimator"
                android.util.Log.i(r1, r0)
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                r0.setAnimationPending(r11)
                r12 = r21
                r7.invokeCallback(r12)
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                com.android.systemui.statusbar.phone.NotificationPanelView r0 = r0.mNotificationPanel
                r1 = 1065353216(0x3f800000, float:1.0)
                r0.collapse(r11, r1)
                return
            L_0x007b:
                r12 = r21
                r19.setupDimLayer()
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                if (r9 != 0) goto L_0x0086
                r1 = r11
                goto L_0x008c
            L_0x0086:
                android.app.WindowConfiguration r1 = r9.windowConfiguration
                int r1 = r1.getActivityType()
            L_0x008c:
                int unused = r0.mClosingActivityType = r1
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$ExpandAnimationParameters r0 = r7.mParams
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                int[] r1 = r1.getLocationOnScreen()
                r0.startPosition = r1
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$ExpandAnimationParameters r0 = r7.mParams
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                float r1 = r1.getTranslationZ()
                r0.startTranslationZ = r1
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$ExpandAnimationParameters r0 = r7.mParams
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                int r1 = r1.getClipTopAmount()
                r0.startClipTopAmount = r1
                com.android.systemui.statusbar.ExpandableNotificationRow r0 = r7.mSourceNotification
                boolean r0 = r0.isChildInGroup()
                r13 = 0
                if (r0 == 0) goto L_0x00dc
                com.android.systemui.statusbar.ExpandableNotificationRow r0 = r7.mSourceNotification
                com.android.systemui.statusbar.ExpandableNotificationRow r0 = r0.getNotificationParent()
                int r0 = r0.getClipTopAmount()
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$ExpandAnimationParameters r1 = r7.mParams
                r1.parentStartClipTopAmount = r0
                if (r0 == 0) goto L_0x00dc
                float r0 = (float) r0
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                float r1 = r1.getTranslationY()
                float r0 = r0 - r1
                int r1 = (r0 > r13 ? 1 : (r0 == r13 ? 0 : -1))
                if (r1 <= 0) goto L_0x00dc
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$ExpandAnimationParameters r1 = r7.mParams
                double r2 = (double) r0
                double r2 = java.lang.Math.ceil(r2)
                int r0 = (int) r2
                r1.startClipTopAmount = r0
            L_0x00dc:
                android.graphics.Rect r0 = r8.sourceContainerBounds
                int r14 = r0.width()
                android.graphics.Rect r0 = r8.sourceContainerBounds
                int r15 = r0.height()
                com.android.systemui.statusbar.ExpandableNotificationRow r0 = r7.mSourceNotification
                int r0 = r0.getActualHeight()
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                int r1 = r1.getClipBottomAmount()
                int r0 = r0 - r1
                float r0 = (float) r0
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                float r1 = r1.getScaleY()
                float r0 = r0 * r1
                int r4 = (int) r0
                com.android.systemui.statusbar.ExpandableNotificationRow r0 = r7.mSourceNotification
                int r0 = r0.getWidth()
                float r0 = (float) r0
                com.android.systemui.statusbar.ExpandableNotificationRow r1 = r7.mSourceNotification
                float r1 = r1.getScaleX()
                float r0 = r0 * r1
                int r3 = (int) r0
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$ExpandAnimationParameters r0 = r7.mParams
                int[] r0 = r0.startPosition
                r0 = r0[r10]
                int r1 = r4 / 2
                int r2 = r0 + r1
                int r16 = r15 / 2
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                int r0 = r0.mClosingActivityType
                r6 = 2
                if (r0 != r6) goto L_0x0125
                r17 = r10
                goto L_0x0127
            L_0x0125:
                r17 = r11
            L_0x0127:
                r7.setExpandAnimationRunning(r10)
                r0 = r19
                r1 = r14
                r5 = r8
                r18 = r6
                r6 = r9
                miuix.animation.IStateStyle r6 = r0.setupExpandAnimation(r1, r2, r3, r4, r5, r6)
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$AnimationRunner$1 r5 = new com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator$AnimationRunner$1
                r0 = r5
                r1 = r19
                r2 = r14
                r3 = r8
                r4 = r9
                r8 = r5
                r5 = r21
                r0.<init>(r2, r3, r4, r5)
                miuix.animation.IStateStyle r0 = r6.addListener(r8)
                r1 = 3
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r3 = "y"
                r2[r11] = r3
                java.lang.Integer r3 = java.lang.Integer.valueOf(r16)
                r2[r10] = r3
                r3 = 1065185444(0x3f7d70a4, float:0.99)
                r4 = 1050253722(0x3e99999a, float:0.3)
                miuix.animation.base.AnimConfig r5 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r4)
                r2[r18] = r5
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r5 = "alpha"
                r2[r11] = r5
                java.lang.Float r5 = java.lang.Float.valueOf(r13)
                r2[r10] = r5
                r5 = 1041865114(0x3e19999a, float:0.15)
                miuix.animation.base.AnimConfig r5 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r5)
                r2[r18] = r5
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r5 = "width"
                r2[r11] = r5
                java.lang.Integer r5 = java.lang.Integer.valueOf(r14)
                r2[r10] = r5
                r5 = 1053609165(0x3ecccccd, float:0.4)
                miuix.animation.base.AnimConfig r6 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r5)
                r2[r18] = r6
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r6 = "height"
                r2[r11] = r6
                java.lang.Integer r6 = java.lang.Integer.valueOf(r15)
                r2[r10] = r6
                r6 = 1051931443(0x3eb33333, float:0.35)
                miuix.animation.base.AnimConfig r6 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r6)
                r2[r18] = r6
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r6 = "corner"
                r2[r11] = r6
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r6 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                float r6 = r6.mWindowCornerRadius
                java.lang.Float r6 = java.lang.Float.valueOf(r6)
                r2[r10] = r6
                miuix.animation.base.AnimConfig r6 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r4)
                r2[r18] = r6
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r6 = "dimBehind"
                r2[r11] = r6
                r6 = 1061997773(0x3f4ccccd, float:0.8)
                java.lang.Float r6 = java.lang.Float.valueOf(r6)
                r2[r10] = r6
                miuix.animation.base.AnimConfig r6 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r4)
                r2[r18] = r6
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r2 = new java.lang.Object[r1]
                java.lang.String r6 = "closingX"
                r2[r11] = r6
                if (r9 != 0) goto L_0x01f0
                r6 = r11
                goto L_0x01f7
            L_0x01f0:
                android.graphics.Rect r6 = r9.sourceContainerBounds
                int r6 = r6.width()
                int r6 = -r6
            L_0x01f7:
                java.lang.Integer r6 = java.lang.Integer.valueOf(r6)
                r2[r10] = r6
                miuix.animation.base.AnimConfig r6 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r5)
                r2[r18] = r6
                miuix.animation.IStateStyle r0 = r0.to(r2)
                java.lang.Object[] r1 = new java.lang.Object[r1]
                java.lang.String r2 = "closingScale"
                r1[r11] = r2
                r2 = 1063675494(0x3f666666, float:0.9)
                java.lang.Float r2 = java.lang.Float.valueOf(r2)
                r1[r10] = r2
                if (r17 == 0) goto L_0x0219
                goto L_0x021a
            L_0x0219:
                r4 = r5
            L_0x021a:
                miuix.animation.base.AnimConfig r2 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.springEase(r3, r4)
                r1[r18] = r2
                r0.to(r1)
                com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator r0 = com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.this
                r0.setAnimationPending(r11)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator.AnimationRunner.lambda$onAnimationStart$0$MiuiActivityLaunchAnimator$AnimationRunner(android.view.RemoteAnimationTarget[], android.view.IRemoteAnimationFinishedCallback):void");
        }

        public void onAnimationStart(RemoteAnimationTarget[] remoteAnimationTargetArr, RemoteAnimationTarget[] remoteAnimationTargetArr2, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) throws RemoteException {
            onAnimationStart(remoteAnimationTargetArr, iRemoteAnimationFinishedCallback);
        }

        private IStateStyle setupExpandAnimation(int i, int i2, int i3, int i4, RemoteAnimationTarget remoteAnimationTarget, RemoteAnimationTarget remoteAnimationTarget2) {
            float f = this.mNotificationCornerRadius;
            updateAndApplyParams(i, i2, i3, i4, f, 0.0f, 1.0f, 0, 1.0f, remoteAnimationTarget, remoteAnimationTarget2);
            IStateStyle useValue = Folme.useValue(new Object[0]);
            Float valueOf = Float.valueOf(1.0f);
            return useValue.setTo("y", Integer.valueOf(i2), "alpha", valueOf, "width", Integer.valueOf(i3), "height", Integer.valueOf(i4), "dimBehind", Float.valueOf(0.0f), "corner", Float.valueOf(f), "closingX", 0, "closingScale", valueOf);
        }

        /* access modifiers changed from: private */
        public void updateAndApplyParams(int i, int i2, int i3, int i4, float f, float f2, float f3, int i5, float f4, RemoteAnimationTarget remoteAnimationTarget, RemoteAnimationTarget remoteAnimationTarget2) {
            ExpandAnimationParameters expandAnimationParameters = this.mParams;
            expandAnimationParameters.left = (i - i3) / 2;
            int i6 = i4 / 2;
            expandAnimationParameters.top = i2 - i6;
            expandAnimationParameters.right = (i + i3) / 2;
            expandAnimationParameters.bottom = i2 + i6;
            expandAnimationParameters.closingX = i5;
            expandAnimationParameters.closingScale = f4;
            expandAnimationParameters.dimBehind = f2;
            this.mCornerRadius = f;
            applyParamsToWindow(remoteAnimationTarget, remoteAnimationTarget2);
            applyParamsToHeadsUpStub(this.mParams, f3);
        }

        /* access modifiers changed from: private */
        public void invokeCallback(IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            try {
                iRemoteAnimationFinishedCallback.onAnimationFinished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private RemoteAnimationTarget getPrimaryRemoteAnimationTarget(RemoteAnimationTarget[] remoteAnimationTargetArr) {
            for (RemoteAnimationTarget remoteAnimationTarget : remoteAnimationTargetArr) {
                if (remoteAnimationTarget.mode == 0) {
                    return remoteAnimationTarget;
                }
            }
            return null;
        }

        private RemoteAnimationTarget getClosingRemoteAnimationTarget(RemoteAnimationTarget[] remoteAnimationTargetArr) {
            for (RemoteAnimationTarget remoteAnimationTarget : remoteAnimationTargetArr) {
                if (remoteAnimationTarget.mode == 1) {
                    return remoteAnimationTarget;
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        public void setExpandAnimationRunning(boolean z) {
            MiuiActivityLaunchAnimator.this.mNotificationPanel.setLaunchingNotification(z);
            MiuiActivityLaunchAnimator.this.mStatusBarWindow.setExpandAnimationRunning(z);
            MiuiActivityLaunchAnimator.this.mHeadsUpStub.setAnimationRunning(z);
            boolean unused = MiuiActivityLaunchAnimator.this.mAnimationRunning = z;
            if (!z) {
                MiuiActivityLaunchAnimator.this.mCallback.onExpandAnimationFinished(this.mIsFullScreenLaunch);
                MiuiActivityLaunchAnimator.this.mHeadsUpStub.setAlpha(1.0f);
            }
            if (z) {
                this.mSourceNotification.getViewState().cancelAnimations(this.mSourceNotification);
            }
        }

        private void applyParamsToHeadsUpStub(ExpandAnimationParameters expandAnimationParameters, float f) {
            MiuiActivityLaunchAnimator.this.mHeadsUpStub.applyStubBounds(expandAnimationParameters.left, expandAnimationParameters.top, expandAnimationParameters.right, expandAnimationParameters.bottom);
            MiuiActivityLaunchAnimator.this.mHeadsUpStub.setAlpha(f);
        }

        /* access modifiers changed from: private */
        public void applyParamsToWindow(RemoteAnimationTarget remoteAnimationTarget, RemoteAnimationTarget remoteAnimationTarget2) {
            RemoteAnimationTarget remoteAnimationTarget3 = remoteAnimationTarget;
            RemoteAnimationTarget remoteAnimationTarget4 = remoteAnimationTarget2;
            Matrix matrix = (Matrix) MiuiActivityLaunchAnimator.MATRIX_POOL.acquire();
            float width = ((float) this.mParams.getWidth()) / ((float) remoteAnimationTarget3.sourceContainerBounds.width());
            matrix.setScale(width, width, (float) remoteAnimationTarget3.sourceContainerBounds.centerX(), 0.0f);
            matrix.postTranslate(0.0f, (float) (this.mParams.top - remoteAnimationTarget3.position.y));
            this.mWindowCrop.set(0, 0, remoteAnimationTarget3.sourceContainerBounds.width(), (int) (((float) this.mParams.getHeight()) / width));
            SyncRtSurfaceTransactionApplier.SurfaceParams surfaceParams = new SyncRtSurfaceTransactionApplier.SurfaceParams(new com.android.systemui.shared.system.SurfaceControlCompat(remoteAnimationTarget3.leash), 1.0f, matrix, this.mWindowCrop, remoteAnimationTarget3.prefixOrderIndex, this.mCornerRadius, true);
            MiuiActivityLaunchAnimator.MATRIX_POOL.release(matrix);
            if (remoteAnimationTarget4 == null || !(MiuiActivityLaunchAnimator.this.mClosingActivityType == 2 || MiuiActivityLaunchAnimator.this.mClosingActivityType == 1)) {
                this.mSyncRtTransactionApplier.scheduleApply(surfaceParams);
                return;
            }
            Matrix matrix2 = (Matrix) MiuiActivityLaunchAnimator.MATRIX_POOL.acquire();
            float f = this.mParams.closingScale;
            matrix2.setScale(f, f, (float) remoteAnimationTarget4.sourceContainerBounds.centerX(), (float) remoteAnimationTarget4.sourceContainerBounds.centerY());
            if (MiuiActivityLaunchAnimator.this.mClosingActivityType == 1) {
                matrix2.postTranslate((float) this.mParams.closingX, 0.0f);
            }
            com.android.systemui.shared.system.SurfaceControlCompat surfaceControlCompat = new com.android.systemui.shared.system.SurfaceControlCompat(remoteAnimationTarget4.leash);
            SyncRtSurfaceTransactionApplier.SurfaceParams surfaceParams2 = new SyncRtSurfaceTransactionApplier.SurfaceParams(surfaceControlCompat, 1.0f, matrix2, remoteAnimationTarget4.sourceContainerBounds, remoteAnimationTarget3.prefixOrderIndex - 2, MiuiActivityLaunchAnimator.this.mWindowCornerRadius, MiuiActivityLaunchAnimator.this.mAnimationRunning);
            MiuiActivityLaunchAnimator.MATRIX_POOL.release(matrix2);
            if (!MiuiActivityLaunchAnimator.this.mAnimationRunning || this.mDimLayer == null) {
                this.mSyncRtTransactionApplier.scheduleApply(surfaceParams, surfaceParams2);
                if (this.mDimLayer != null) {
                    SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                    SurfaceControlCompat.remove(transaction, this.mDimLayer);
                    transaction.apply();
                    this.mDimLayer = null;
                    return;
                }
                return;
            }
            Matrix matrix3 = (Matrix) MiuiActivityLaunchAnimator.MATRIX_POOL.acquire();
            com.android.systemui.shared.system.SurfaceControlCompat surfaceControlCompat2 = new com.android.systemui.shared.system.SurfaceControlCompat(this.mDimLayer);
            float f2 = this.mParams.dimBehind;
            SyncRtSurfaceTransactionApplier.SurfaceParams surfaceParams3 = new SyncRtSurfaceTransactionApplier.SurfaceParams(surfaceControlCompat2, f2, matrix3, remoteAnimationTarget3.sourceContainerBounds, remoteAnimationTarget3.prefixOrderIndex - 1, MiuiActivityLaunchAnimator.this.mWindowCornerRadius, MiuiActivityLaunchAnimator.this.mAnimationRunning);
            surfaceParams3.setLayerAbove(new com.android.systemui.shared.system.SurfaceControlCompat(remoteAnimationTarget3.leash));
            MiuiActivityLaunchAnimator.MATRIX_POOL.release(matrix3);
            this.mSyncRtTransactionApplier.scheduleApply(surfaceParams, surfaceParams2, surfaceParams3);
        }

        public void onAnimationCancelled() throws RemoteException {
            this.mSourceNotification.post(new Runnable() {
                public final void run() {
                    MiuiActivityLaunchAnimator.AnimationRunner.this.lambda$onAnimationCancelled$1$MiuiActivityLaunchAnimator$AnimationRunner();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAnimationCancelled$1 */
        public /* synthetic */ void lambda$onAnimationCancelled$1$MiuiActivityLaunchAnimator$AnimationRunner() {
            MiuiActivityLaunchAnimator.this.setAnimationPending(false);
            MiuiActivityLaunchAnimator.this.mCallback.onLaunchAnimationCancelled();
        }
    }

    public static class ExpandAnimationParameters {
        int bottom;
        float closingScale;
        int closingX;
        float dimBehind;
        int left;
        int parentStartClipTopAmount;
        int right;
        int startClipTopAmount;
        int[] startPosition;
        float startTranslationZ;
        int top;

        public int getWidth() {
            return this.right - this.left;
        }

        public int getHeight() {
            return this.bottom - this.top;
        }
    }

    /* access modifiers changed from: private */
    public static AnimConfig springEase(float f, float f2) {
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, f, f2);
        return animConfig;
    }

    private static abstract class MultiFloatTransitionListener extends TransitionListener {
        private final Map<String, Float> mCurrentInfo;

        /* access modifiers changed from: package-private */
        public abstract void onUpdate(Map<String, Float> map);

        private MultiFloatTransitionListener() {
            this.mCurrentInfo = new HashMap();
        }

        public final void onUpdate(Object obj, Collection<UpdateInfo> collection) {
            super.onUpdate(obj, collection);
            collection.forEach(new Consumer() {
                public final void accept(Object obj) {
                    MiuiActivityLaunchAnimator.MultiFloatTransitionListener.this.lambda$onUpdate$0$MiuiActivityLaunchAnimator$MultiFloatTransitionListener((UpdateInfo) obj);
                }
            });
            onUpdate(this.mCurrentInfo);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onUpdate$0 */
        public /* synthetic */ void lambda$onUpdate$0$MiuiActivityLaunchAnimator$MultiFloatTransitionListener(UpdateInfo updateInfo) {
            this.mCurrentInfo.put(updateInfo.property.getName(), Float.valueOf(updateInfo.getFloatValue()));
        }
    }
}
