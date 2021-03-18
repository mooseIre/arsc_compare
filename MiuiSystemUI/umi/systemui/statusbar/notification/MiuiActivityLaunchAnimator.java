package com.android.systemui.statusbar.notification;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.RemoteException;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;

public final class MiuiActivityLaunchAnimator extends ActivityLaunchAnimator {
    private final ActivityLaunchAnimator.Callback mCallback;
    private int mClosingActivityType;
    private final NotificationShadeDepthController mDepthController;
    private final NotificationListContainer mNotificationContainer;
    private final NotificationPanelViewController mNotificationPanel;
    private final NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    private final float mWindowCornerRadius;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiActivityLaunchAnimator(NotificationShadeWindowViewController notificationShadeWindowViewController, ActivityLaunchAnimator.Callback callback, NotificationPanelViewController notificationPanelViewController, NotificationShadeDepthController notificationShadeDepthController, NotificationListContainer notificationListContainer, Executor executor) {
        super(notificationShadeWindowViewController, callback, notificationPanelViewController, notificationShadeDepthController, notificationListContainer, executor);
        Intrinsics.checkParameterIsNotNull(notificationShadeWindowViewController, "notificationShadeWindowViewController");
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        Intrinsics.checkParameterIsNotNull(notificationPanelViewController, "notificationPanel");
        Intrinsics.checkParameterIsNotNull(notificationShadeDepthController, "depthController");
        Intrinsics.checkParameterIsNotNull(notificationListContainer, "container");
        this.mNotificationShadeWindowViewController = notificationShadeWindowViewController;
        this.mNotificationContainer = notificationListContainer;
        this.mNotificationPanel = notificationPanelViewController;
        this.mDepthController = notificationShadeDepthController;
        this.mCallback = callback;
        ViewGroup view = notificationPanelViewController.getView();
        Intrinsics.checkExpressionValueIsNotNull(view, "mNotificationPanel.view");
        this.mWindowCornerRadius = ScreenDecorationsUtils.getWindowCornerRadius(view.getResources());
    }

    @Override // com.android.systemui.statusbar.notification.ActivityLaunchAnimator
    public RemoteAnimationAdapter getLaunchAnimation(View view, boolean z) {
        if (!(view instanceof ExpandableNotificationRow) || !this.mCallback.areLaunchAnimationsEnabled() || z) {
            return null;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        if (expandableNotificationRow.isHeadsUpState()) {
            return new RemoteAnimationAdapter(new HeadsUpNotificationAnimationRunner(this, expandableNotificationRow), (long) 300, (long) 200);
        }
        return null;
    }

    public static final class HeadsUpExpandAnimationParameters extends ActivityLaunchAnimator.ExpandAnimationParameters {
        private float closingScale;
        private int closingX;
        private float cornerRadius;
        private float dimBehind;

        public final int getClosingX() {
            return this.closingX;
        }

        public final void setClosingX(int i) {
            this.closingX = i;
        }

        public final float getClosingScale() {
            return this.closingScale;
        }

        public final void setClosingScale(float f) {
            this.closingScale = f;
        }

        public final float getDimBehind() {
            return this.dimBehind;
        }

        public final void setDimBehind(float f) {
            this.dimBehind = f;
        }

        public final float getCornerRadius() {
            return this.cornerRadius;
        }

        public final void setCornerRadius(float f) {
            this.cornerRadius = f;
        }
    }

    public final class HeadsUpNotificationAnimationRunner extends IRemoteAnimationRunner.Stub {
        private SurfaceControl mDimLayer;
        private boolean mIsFullScreenLaunch;
        private final float mNotificationCornerRadius;
        private final HeadsUpExpandAnimationParameters mParams = new HeadsUpExpandAnimationParameters();
        private final ExpandableNotificationRow mSourceNotification;
        private final SyncRtSurfaceTransactionApplierCompat mSyncRtTransactionApplier;
        private final Rect mWindowCrop;
        final /* synthetic */ MiuiActivityLaunchAnimator this$0;

        public HeadsUpNotificationAnimationRunner(MiuiActivityLaunchAnimator miuiActivityLaunchAnimator, ExpandableNotificationRow expandableNotificationRow) {
            Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "sourceNotification");
            this.this$0 = miuiActivityLaunchAnimator;
            this.mSourceNotification = expandableNotificationRow;
            this.mSyncRtTransactionApplier = new SyncRtSurfaceTransactionApplierCompat(expandableNotificationRow);
            this.mNotificationCornerRadius = Math.max(expandableNotificationRow.getCurrentTopRoundness(), expandableNotificationRow.getCurrentBottomRoundness());
            this.mWindowCrop = new Rect();
            this.mIsFullScreenLaunch = true;
        }

        private final void setupDimLayer() {
            ExpandableNotificationRow expandableNotificationRow = this.mSourceNotification;
            expandableNotificationRow.getView();
            ViewRootImpl viewRootImpl = expandableNotificationRow.getViewRootImpl();
            this.mDimLayer = new SurfaceControl.Builder(new SurfaceSession()).setColorLayer().setParent(viewRootImpl != null ? viewRootImpl.getSurfaceControl() : null).setName("SystemUI Notification Dim").build();
        }

        public void onAnimationStart(RemoteAnimationTarget[] remoteAnimationTargetArr, RemoteAnimationTarget[] remoteAnimationTargetArr2, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            Intrinsics.checkParameterIsNotNull(remoteAnimationTargetArr, "remoteAnimationTargets");
            Intrinsics.checkParameterIsNotNull(iRemoteAnimationFinishedCallback, "finishedCallback");
            this.mSourceNotification.post(new MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationStart$1(this, remoteAnimationTargetArr, iRemoteAnimationFinishedCallback));
        }

        private final IStateStyle setupExpandAnimation(int i, int i2, int i3, int i4, RemoteAnimationTarget remoteAnimationTarget, RemoteAnimationTarget remoteAnimationTarget2) {
            float f = this.mNotificationCornerRadius;
            updateAndApplyParams(i, i2, i3, i4, f, 0.0f, 1.0f, 0, 1.0f, remoteAnimationTarget, remoteAnimationTarget2);
            IStateStyle useValue = Folme.useValue(new Object[0]);
            Float valueOf = Float.valueOf(1.0f);
            IStateStyle to = useValue.setTo("y", Integer.valueOf(i2), "alpha", valueOf, "width", Integer.valueOf(i3), "height", Integer.valueOf(i4), "dimBehind", Float.valueOf(0.0f), "corner", Float.valueOf(f), "closingX", 0, "closingScale", valueOf);
            Intrinsics.checkExpressionValueIsNotNull(to, "Folme.useValue().setTo(\"…singScale\", closingScale)");
            return to;
        }

        private final void updateAndApplyParams(int i, int i2, int i3, int i4, float f, float f2, float f3, int i5, float f4, RemoteAnimationTarget remoteAnimationTarget, RemoteAnimationTarget remoteAnimationTarget2) {
            HeadsUpExpandAnimationParameters headsUpExpandAnimationParameters = this.mParams;
            headsUpExpandAnimationParameters.left = (i - i3) / 2;
            int i6 = i4 / 2;
            headsUpExpandAnimationParameters.top = i2 - i6;
            headsUpExpandAnimationParameters.right = (i + i3) / 2;
            headsUpExpandAnimationParameters.bottom = i2 + i6;
            headsUpExpandAnimationParameters.setClosingX(i5);
            this.mParams.setClosingScale(f4);
            this.mParams.setDimBehind(f2);
            this.mParams.setCornerRadius(f);
            applyParamsToWindow(remoteAnimationTarget, remoteAnimationTarget2);
            applyParamsToNotification(this.mParams);
            applyParamsToNotificationShade(this.mParams);
        }

        private final void invokeCallback(IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            try {
                iRemoteAnimationFinishedCallback.onAnimationFinished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private final RemoteAnimationTarget getPrimaryRemoteAnimationTarget(RemoteAnimationTarget[] remoteAnimationTargetArr) {
            for (RemoteAnimationTarget remoteAnimationTarget : remoteAnimationTargetArr) {
                if (remoteAnimationTarget.mode == 0) {
                    return remoteAnimationTarget;
                }
            }
            return null;
        }

        private final RemoteAnimationTarget getClosingRemoteAnimationTarget(RemoteAnimationTarget[] remoteAnimationTargetArr) {
            for (RemoteAnimationTarget remoteAnimationTarget : remoteAnimationTargetArr) {
                if (remoteAnimationTarget.mode == 1) {
                    return remoteAnimationTarget;
                }
            }
            return null;
        }

        private final void setExpandAnimationRunning(boolean z) {
            this.this$0.mNotificationPanel.setLaunchingNotification(z);
            this.mSourceNotification.setExpandAnimationRunning(z);
            this.this$0.mNotificationShadeWindowViewController.setExpandAnimationRunning(z);
            this.this$0.mNotificationContainer.setExpandingNotification(z ? this.mSourceNotification : null);
            MiuiActivityLaunchAnimator miuiActivityLaunchAnimator = this.this$0;
            miuiActivityLaunchAnimator.mAnimationRunning = z;
            if (!z) {
                miuiActivityLaunchAnimator.mCallback.onExpandAnimationFinished(this.mIsFullScreenLaunch);
                applyParamsToNotification(null);
                applyParamsToNotificationShade(null);
            }
            if (z) {
                ExpandableViewState viewState = this.mSourceNotification.getViewState();
                if (viewState != null) {
                    viewState.cancelAnimations(this.mSourceNotification);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }

        private final void applyParamsToNotificationShade(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
            this.this$0.mNotificationContainer.applyExpandAnimationParams(expandAnimationParameters);
            this.this$0.mNotificationPanel.applyExpandAnimationParams(expandAnimationParameters);
            this.this$0.mDepthController.setNotificationLaunchAnimationParams(expandAnimationParameters);
        }

        private final void applyParamsToNotification(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
            this.mSourceNotification.applyExpandAnimationParams(expandAnimationParameters);
        }

        private final void applyParamsToWindow(RemoteAnimationTarget remoteAnimationTarget, RemoteAnimationTarget remoteAnimationTarget2) {
            Object acquire = MiuiActivityLaunchAnimatorKt.MATRIX_POOL.acquire();
            Intrinsics.checkExpressionValueIsNotNull(acquire, "MATRIX_POOL.acquire()");
            Matrix matrix = (Matrix) acquire;
            float width = ((float) this.mParams.getWidth()) / ((float) remoteAnimationTarget.screenSpaceBounds.width());
            matrix.setScale(width, width, (float) remoteAnimationTarget.screenSpaceBounds.centerX(), 0.0f);
            matrix.postTranslate(0.0f, (float) (this.mParams.top - remoteAnimationTarget.localBounds.top));
            this.mWindowCrop.set(0, 0, remoteAnimationTarget.screenSpaceBounds.width(), (int) (((float) this.mParams.getHeight()) / width));
            SyncRtSurfaceTransactionApplierCompat.SurfaceParams.Builder builder = new SyncRtSurfaceTransactionApplierCompat.SurfaceParams.Builder(remoteAnimationTarget.leash);
            builder.withAlpha(1.0f);
            builder.withMatrix(matrix);
            builder.withWindowCrop(this.mWindowCrop);
            builder.withLayer(remoteAnimationTarget.prefixOrderIndex);
            builder.withCornerRadius(this.mParams.getCornerRadius());
            builder.withVisibility(true);
            SyncRtSurfaceTransactionApplierCompat.SurfaceParams build = builder.build();
            MiuiActivityLaunchAnimatorKt.MATRIX_POOL.release(matrix);
            if (remoteAnimationTarget2 == null || !(this.this$0.mClosingActivityType == 2 || this.this$0.mClosingActivityType == 1)) {
                this.mSyncRtTransactionApplier.scheduleApply(build);
                return;
            }
            Object acquire2 = MiuiActivityLaunchAnimatorKt.MATRIX_POOL.acquire();
            Intrinsics.checkExpressionValueIsNotNull(acquire2, "MATRIX_POOL.acquire()");
            Matrix matrix2 = (Matrix) acquire2;
            matrix2.setScale(this.mParams.getClosingScale(), this.mParams.getClosingScale(), (float) remoteAnimationTarget2.screenSpaceBounds.centerX(), (float) remoteAnimationTarget2.screenSpaceBounds.centerY());
            if (this.this$0.mClosingActivityType == 1) {
                matrix2.postTranslate((float) this.mParams.getClosingX(), (float) 0);
            }
            SyncRtSurfaceTransactionApplierCompat.SurfaceParams.Builder builder2 = new SyncRtSurfaceTransactionApplierCompat.SurfaceParams.Builder(remoteAnimationTarget2.leash);
            builder2.withAlpha(1.0f);
            builder2.withMatrix(matrix2);
            builder2.withWindowCrop(remoteAnimationTarget2.screenSpaceBounds);
            builder2.withLayer(remoteAnimationTarget.prefixOrderIndex - 2);
            builder2.withCornerRadius(this.this$0.mWindowCornerRadius);
            builder2.withVisibility(this.this$0.isAnimationRunning());
            SyncRtSurfaceTransactionApplierCompat.SurfaceParams build2 = builder2.build();
            MiuiActivityLaunchAnimatorKt.MATRIX_POOL.release(matrix2);
            if (!this.this$0.isAnimationRunning() || this.mDimLayer == null) {
                this.mSyncRtTransactionApplier.scheduleApply(build, build2);
                if (this.mDimLayer != null) {
                    SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                    transaction.remove(this.mDimLayer);
                    transaction.apply();
                    this.mDimLayer = null;
                    return;
                }
                return;
            }
            Object acquire3 = MiuiActivityLaunchAnimatorKt.MATRIX_POOL.acquire();
            Intrinsics.checkExpressionValueIsNotNull(acquire3, "MATRIX_POOL.acquire()");
            Matrix matrix3 = (Matrix) acquire3;
            SyncRtSurfaceTransactionApplierCompat.SurfaceParams.Builder builder3 = new SyncRtSurfaceTransactionApplierCompat.SurfaceParams.Builder(this.mDimLayer);
            builder3.withAlpha(this.mParams.getDimBehind());
            builder3.withMatrix(matrix3);
            builder3.withWindowCrop(remoteAnimationTarget.screenSpaceBounds);
            builder3.withRelativeLayer(-1, remoteAnimationTarget.leash);
            builder3.withVisibility(this.this$0.isAnimationRunning());
            SyncRtSurfaceTransactionApplierCompat.SurfaceParams build3 = builder3.build();
            MiuiActivityLaunchAnimatorKt.MATRIX_POOL.release(matrix3);
            this.mSyncRtTransactionApplier.scheduleApply(build, build2, build3);
        }

        public void onAnimationCancelled() {
            this.mSourceNotification.post(new MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationCancelled$1(this));
        }
    }
}
