package com.android.systemui.pip.phone;

import android.annotation.SuppressLint;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.IPinnedStackController;
import android.view.InputEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.phone.PipAccessibilityInteractionConnection;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.pip.phone.PipTouchHandler;
import com.android.systemui.shared.system.InputConsumerController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.DismissCircleView;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Function;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function5;

public class PipTouchHandler {
    private final AccessibilityManager mAccessibilityManager;
    private int mBottomOffsetBufferPx;
    private PipAccessibilityInteractionConnection mConnection;
    private final Context mContext;
    private int mDeferResizeToNormalBoundsUntilRotation = -1;
    private int mDismissAreaHeight;
    private int mDisplayRotation;
    private final boolean mEnableDismissDragToEdge;
    private final boolean mEnableResize;
    private Rect mExpandedBounds = new Rect();
    @VisibleForTesting
    Rect mExpandedMovementBounds = new Rect();
    private int mExpandedShortestEdgeSize;
    private final FloatingContentCoordinator mFloatingContentCoordinator;
    private PipTouchGesture mGesture;
    private Handler mHandler = new Handler();
    private int mImeHeight;
    private int mImeOffset;
    private Rect mInsetBounds = new Rect();
    private boolean mIsImeShowing;
    private boolean mIsShelfShowing;
    private MagnetizedObject.MagneticTarget mMagneticTarget;
    private PhysicsAnimator<View> mMagneticTargetAnimator;
    private MagnetizedObject<Rect> mMagnetizedPip;
    private final PipMenuActivityController mMenuController;
    private int mMenuState = 0;
    private PipMotionHelper mMotionHelper;
    private Rect mMovementBounds = new Rect();
    private int mMovementBoundsExtraOffsets;
    private boolean mMovementWithinDismiss;
    private Rect mNormalBounds = new Rect();
    @VisibleForTesting
    Rect mNormalMovementBounds = new Rect();
    private IPinnedStackController mPinnedStackController;
    private final PipBoundsHandler mPipBoundsHandler;
    private PipResizeGestureHandler mPipResizeGestureHandler;
    private final PipUiEventLogger mPipUiEventLogger;
    @VisibleForTesting
    Rect mResizedBounds = new Rect();
    private float mSavedSnapFraction = -1.0f;
    private boolean mSendingHoverAccessibilityEvents;
    private int mShelfHeight;
    private boolean mShowPipMenuOnAnimationEnd = false;
    private Runnable mShowTargetAction = new Runnable() {
        /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$bnz9PC9JAAj_rxnZq96LLBoKnqw */

        public final void run() {
            PipTouchHandler.this.showDismissTargetMaybe();
        }
    };
    private final PipSnapAlgorithm mSnapAlgorithm;
    private final PhysicsAnimator.SpringConfig mTargetSpringConfig = new PhysicsAnimator.SpringConfig(200.0f, 0.75f);
    private DismissCircleView mTargetView;
    private ViewGroup mTargetViewContainer;
    private final Rect mTmpBounds = new Rect();
    private final PipTouchState mTouchState;
    private final WindowManager mWindowManager;

    private class PipMenuListener implements PipMenuActivityController.Listener {
        private PipMenuListener() {
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipMenuStateChanged(int i, boolean z, Runnable runnable) {
            PipTouchHandler.this.setMenuState(i, z, runnable);
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipExpand() {
            PipTouchHandler.this.mMotionHelper.expandPipToFullscreen();
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipDismiss() {
            PipTouchHandler.this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_TAP_TO_REMOVE);
            PipTouchHandler.this.mTouchState.removeDoubleTapTimeoutCallback();
            PipTouchHandler.this.mMotionHelper.dismissPip();
        }

        @Override // com.android.systemui.pip.phone.PipMenuActivityController.Listener
        public void onPipShowMenu() {
            PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
        }
    }

    @SuppressLint({"InflateParams"})
    public PipTouchHandler(Context context, IActivityManager iActivityManager, PipMenuActivityController pipMenuActivityController, InputConsumerController inputConsumerController, PipBoundsHandler pipBoundsHandler, PipTaskOrganizer pipTaskOrganizer, FloatingContentCoordinator floatingContentCoordinator, DeviceConfigProxy deviceConfigProxy, PipSnapAlgorithm pipSnapAlgorithm, SysUiState sysUiState, PipUiEventLogger pipUiEventLogger) {
        this.mContext = context;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mMenuController = pipMenuActivityController;
        pipMenuActivityController.addListener(new PipMenuListener());
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mGesture = new DefaultPipTouchGesture();
        PipMotionHelper pipMotionHelper = new PipMotionHelper(this.mContext, pipTaskOrganizer, this.mMenuController, this.mSnapAlgorithm, floatingContentCoordinator);
        this.mMotionHelper = pipMotionHelper;
        this.mPipResizeGestureHandler = new PipResizeGestureHandler(context, pipBoundsHandler, pipMotionHelper, deviceConfigProxy, pipTaskOrganizer, new Function() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$Pinp5dDEZz4g_gFarHF_EBKOZzg */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return PipTouchHandler.this.getMovementBounds((Rect) obj);
            }
        }, new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$uINUOEMRLade2qxAeU4HH41XrU */

            public final void run() {
                PipTouchHandler.this.updateMovementBounds();
            }
        }, sysUiState, pipUiEventLogger);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        Handler handler = this.mHandler;
        $$Lambda$PipTouchHandler$Uq5M9Md512Sfgd22VAeFpot25E0 r4 = new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$Uq5M9Md512Sfgd22VAeFpot25E0 */

            public final void run() {
                PipTouchHandler.this.lambda$new$0$PipTouchHandler();
            }
        };
        Objects.requireNonNull(pipMenuActivityController);
        this.mTouchState = new PipTouchState(viewConfiguration, handler, r4, new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$QWy27z4N9eSKLQk7iOWRu3Ei38 */

            public final void run() {
                PipMenuActivityController.this.hideMenu();
            }
        });
        Resources resources = context.getResources();
        this.mEnableDismissDragToEdge = resources.getBoolean(C0010R$bool.config_pipEnableDismissDragToEdge);
        this.mEnableResize = resources.getBoolean(C0010R$bool.config_pipEnableResizeForMenu);
        reloadResources();
        inputConsumerController.setInputListener(new InputConsumerController.InputListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$A78OVgVs8H_2SG6WUxzMSclOdX0 */

            @Override // com.android.systemui.shared.system.InputConsumerController.InputListener
            public final boolean onInputEvent(InputEvent inputEvent) {
                return PipTouchHandler.this.handleTouchEvent(inputEvent);
            }
        });
        inputConsumerController.setRegistrationListener(new InputConsumerController.RegistrationListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$NVpciZTELeGnxXPZeY5rYMmqJQ */

            @Override // com.android.systemui.shared.system.InputConsumerController.RegistrationListener
            public final void onRegistrationChanged(boolean z) {
                PipTouchHandler.this.onRegistrationChanged(z);
            }
        });
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        this.mConnection = new PipAccessibilityInteractionConnection(this.mContext, this.mMotionHelper, pipTaskOrganizer, pipSnapAlgorithm, new PipAccessibilityInteractionConnection.AccessibilityCallbacks() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$1nY3kLe318Fm3UtIAbDmSK80h7w */

            @Override // com.android.systemui.pip.phone.PipAccessibilityInteractionConnection.AccessibilityCallbacks
            public final void onAccessibilityShowMenu() {
                PipTouchHandler.this.onAccessibilityShowMenu();
            }
        }, new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$uINUOEMRLade2qxAeU4HH41XrU */

            public final void run() {
                PipTouchHandler.this.updateMovementBounds();
            }
        }, this.mHandler);
        this.mPipUiEventLogger = pipUiEventLogger;
        this.mTargetView = new DismissCircleView(context);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mTargetViewContainer = frameLayout;
        frameLayout.setBackgroundDrawable(context.getDrawable(C0013R$drawable.floating_dismiss_gradient_transition));
        this.mTargetViewContainer.setClipChildren(false);
        this.mTargetViewContainer.addView(this.mTargetView);
        MagnetizedObject<Rect> magnetizedPip = this.mMotionHelper.getMagnetizedPip();
        this.mMagnetizedPip = magnetizedPip;
        this.mMagneticTarget = magnetizedPip.addTarget(this.mTargetView, 0);
        updateMagneticTargetSize();
        this.mMagnetizedPip.setAnimateStuckToTarget(new Function5() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$Nekx4ZO_bAe0QnJLdZ92hnlTRtE */

            @Override // kotlin.jvm.functions.Function5
            public final Object invoke(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
                return PipTouchHandler.this.lambda$new$1$PipTouchHandler((MagnetizedObject.MagneticTarget) obj, (Float) obj2, (Float) obj3, (Boolean) obj4, (Function0) obj5);
            }
        });
        this.mMagnetizedPip.setMagnetListener(new MagnetizedObject.MagnetListener() {
            /* class com.android.systemui.pip.phone.PipTouchHandler.AnonymousClass1 */

            @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
            public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
                PipTouchHandler.this.showDismissTargetMaybe();
            }

            @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
            public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
                if (z) {
                    PipTouchHandler.this.mMotionHelper.flingToSnapTarget(f, f2, null, null);
                    PipTouchHandler.this.hideDismissTarget();
                    return;
                }
                PipTouchHandler.this.mMotionHelper.setSpringingToTouch(true);
            }

            @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
            public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
                PipTouchHandler.this.mMotionHelper.notifyDismissalPending();
                PipTouchHandler.this.mHandler.post(new Runnable() {
                    /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$1$zJ5cwW9_qQ4umngtgHurxl3qHI */

                    public final void run() {
                        PipTouchHandler.AnonymousClass1.this.lambda$onReleasedInTarget$0$PipTouchHandler$1();
                    }
                });
                PipTouchHandler.this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_DRAG_TO_REMOVE);
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onReleasedInTarget$0 */
            public /* synthetic */ void lambda$onReleasedInTarget$0$PipTouchHandler$1() {
                PipTouchHandler.this.mMotionHelper.animateDismiss();
                PipTouchHandler.this.hideDismissTarget();
            }
        });
        this.mMagneticTargetAnimator = PhysicsAnimator.getInstance(this.mTargetView);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipTouchHandler() {
        this.mMenuController.showMenuWithDelay(2, this.mMotionHelper.getBounds(), true, willResizeMenu(), shouldShowResizeHandle());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ Unit lambda$new$1$PipTouchHandler(MagnetizedObject.MagneticTarget magneticTarget, Float f, Float f2, Boolean bool, Function0 function0) {
        this.mMotionHelper.animateIntoDismissTarget(magneticTarget, f.floatValue(), f2.floatValue(), bool.booleanValue(), function0);
        return Unit.INSTANCE;
    }

    private void reloadResources() {
        Resources resources = this.mContext.getResources();
        this.mBottomOffsetBufferPx = resources.getDimensionPixelSize(C0012R$dimen.pip_bottom_offset_buffer);
        this.mExpandedShortestEdgeSize = resources.getDimensionPixelSize(C0012R$dimen.pip_expanded_shortest_edge_size);
        this.mImeOffset = resources.getDimensionPixelSize(C0012R$dimen.pip_ime_offset);
        this.mDismissAreaHeight = resources.getDimensionPixelSize(C0012R$dimen.floating_dismiss_gradient_height);
        updateMagneticTargetSize();
    }

    private void updateMagneticTargetSize() {
        if (this.mTargetView != null) {
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.dismiss_circle_size);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
            layoutParams.gravity = 81;
            layoutParams.bottomMargin = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.floating_dismiss_bottom_margin);
            this.mTargetView.setLayoutParams(layoutParams);
            this.mMagneticTarget.setMagneticFieldRadiusPx((int) (((float) dimensionPixelSize) * 1.25f));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldShowResizeHandle() {
        return !this.mPipBoundsHandler.hasSaveReentryBounds();
    }

    public void setTouchEnabled(boolean z) {
        this.mTouchState.setAllowTouches(z);
    }

    public void showPictureInPictureMenu() {
        if (!this.mTouchState.isUserInteracting()) {
            this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), false, willResizeMenu(), shouldShowResizeHandle());
        }
    }

    public void onActivityPinned() {
        createOrUpdateDismissTarget();
        this.mShowPipMenuOnAnimationEnd = true;
        this.mPipResizeGestureHandler.onActivityPinned();
        this.mFloatingContentCoordinator.onContentAdded(this.mMotionHelper);
    }

    public void onActivityUnpinned(ComponentName componentName) {
        if (componentName == null) {
            cleanUpDismissTarget();
            this.mFloatingContentCoordinator.onContentRemoved(this.mMotionHelper);
        }
        this.mResizedBounds.setEmpty();
        this.mPipResizeGestureHandler.onActivityUnpinned();
    }

    public void onPinnedStackAnimationEnded(int i) {
        this.mMotionHelper.synchronizePinnedStackBounds();
        updateMovementBounds();
        if (i == 2) {
            this.mResizedBounds.set(this.mMotionHelper.getBounds());
        }
        if (this.mShowPipMenuOnAnimationEnd) {
            this.mMenuController.showMenu(1, this.mMotionHelper.getBounds(), true, false, shouldShowResizeHandle());
            this.mShowPipMenuOnAnimationEnd = false;
        }
    }

    public void onConfigurationChanged() {
        this.mPipResizeGestureHandler.onConfigurationChanged();
        this.mMotionHelper.synchronizePinnedStackBounds();
        reloadResources();
        createOrUpdateDismissTarget();
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mIsImeShowing = z;
        this.mImeHeight = i;
    }

    public void onShelfVisibilityChanged(boolean z, int i) {
        this.mIsShelfShowing = z;
        this.mShelfHeight = i;
    }

    public void adjustBoundsForRotation(Rect rect, Rect rect2, Rect rect3) {
        Rect rect4 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(rect, rect3, rect4, 0);
        if ((this.mMovementBounds.bottom - this.mMovementBoundsExtraOffsets) - this.mBottomOffsetBufferPx <= rect2.top) {
            rect.offsetTo(rect.left, rect4.bottom);
        }
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) {
        int i2;
        int i3 = 0;
        int i4 = this.mIsImeShowing ? this.mImeHeight : 0;
        boolean z3 = true;
        if (this.mDisplayRotation != i) {
            this.mTouchState.reset();
        }
        this.mNormalBounds.set(rect2);
        Rect rect4 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mNormalBounds, rect, rect4, i4);
        if (this.mMovementBounds.isEmpty()) {
            this.mSnapAlgorithm.getMovementBounds(rect3, rect, this.mMovementBounds, 0);
        }
        float width = ((float) rect2.width()) / ((float) rect2.height());
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        Size sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(width, (float) this.mExpandedShortestEdgeSize, point.x, point.y);
        this.mExpandedBounds.set(0, 0, sizeForAspectRatio.getWidth(), sizeForAspectRatio.getHeight());
        Rect rect5 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mExpandedBounds, rect, rect5, i4);
        this.mPipResizeGestureHandler.updateMinSize(this.mNormalBounds.width(), this.mNormalBounds.height());
        this.mPipResizeGestureHandler.updateMaxSize(this.mExpandedBounds.width(), this.mExpandedBounds.height());
        int max = Math.max(this.mIsImeShowing ? this.mImeOffset : 0, (this.mIsImeShowing || !this.mIsShelfShowing) ? 0 : this.mShelfHeight);
        if ((z || z2) && !this.mTouchState.isUserInteracting()) {
            if (this.mMenuState != 2 || !willResizeMenu()) {
                z3 = false;
            }
            Rect rect6 = new Rect();
            PipSnapAlgorithm pipSnapAlgorithm = this.mSnapAlgorithm;
            if (this.mIsImeShowing) {
                i3 = this.mImeHeight;
            }
            pipSnapAlgorithm.getMovementBounds(rect3, rect, rect6, i3);
            int i5 = this.mMovementBounds.bottom - this.mMovementBoundsExtraOffsets;
            int i6 = rect6.bottom;
            if (i6 >= rect6.top) {
                i6 -= max;
            }
            if (z3) {
                rect3.set(this.mExpandedBounds);
                this.mSnapAlgorithm.applySnapFraction(rect3, rect6, this.mSavedSnapFraction);
            }
            if (i5 < i6) {
                int i7 = rect3.top;
                if (i7 > i5 - this.mBottomOffsetBufferPx) {
                    this.mMotionHelper.animateToOffset(rect3, i6 - i7);
                }
            } else if (i5 > i6 && (i2 = rect3.top) > i6 - this.mBottomOffsetBufferPx) {
                this.mMotionHelper.animateToOffset(rect3, i6 - i2);
            }
        }
        this.mNormalMovementBounds.set(rect4);
        this.mExpandedMovementBounds.set(rect5);
        this.mDisplayRotation = i;
        this.mInsetBounds.set(rect);
        updateMovementBounds();
        this.mMovementBoundsExtraOffsets = max;
        this.mConnection.onMovementBoundsChanged(this.mNormalBounds, this.mExpandedBounds, this.mNormalMovementBounds, this.mExpandedMovementBounds);
        if (this.mDeferResizeToNormalBoundsUntilRotation == i) {
            this.mMotionHelper.animateToUnexpandedState(rect2, this.mSavedSnapFraction, this.mNormalMovementBounds, this.mMovementBounds, true);
            this.mSavedSnapFraction = -1.0f;
            this.mDeferResizeToNormalBoundsUntilRotation = -1;
        }
    }

    private void createOrUpdateDismissTarget() {
        if (!this.mTargetViewContainer.isAttachedToWindow()) {
            this.mHandler.removeCallbacks(this.mShowTargetAction);
            this.mMagneticTargetAnimator.cancel();
            this.mTargetViewContainer.setVisibility(4);
            try {
                this.mWindowManager.addView(this.mTargetViewContainer, getDismissTargetLayoutParams());
            } catch (IllegalStateException unused) {
                this.mWindowManager.updateViewLayout(this.mTargetViewContainer, getDismissTargetLayoutParams());
            }
        } else {
            this.mWindowManager.updateViewLayout(this.mTargetViewContainer, getDismissTargetLayoutParams());
        }
    }

    private WindowManager.LayoutParams getDismissTargetLayoutParams() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        int i = this.mDismissAreaHeight;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, i, 0, point.y - i, 2024, 280, -3);
        layoutParams.setTitle("pip-dismiss-overlay");
        layoutParams.privateFlags |= 16;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void showDismissTargetMaybe() {
        createOrUpdateDismissTarget();
        if (this.mTargetViewContainer.getVisibility() != 0) {
            this.mTargetView.setTranslationY((float) this.mTargetViewContainer.getHeight());
            this.mTargetViewContainer.setVisibility(0);
            this.mMagneticTargetAnimator.cancel();
            PhysicsAnimator<View> physicsAnimator = this.mMagneticTargetAnimator;
            physicsAnimator.spring(DynamicAnimation.TRANSLATION_Y, 0.0f, this.mTargetSpringConfig);
            physicsAnimator.start();
            ((TransitionDrawable) this.mTargetViewContainer.getBackground()).startTransition(200);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowTargetAction);
        PhysicsAnimator<View> physicsAnimator = this.mMagneticTargetAnimator;
        physicsAnimator.spring(DynamicAnimation.TRANSLATION_Y, (float) this.mTargetViewContainer.getHeight(), this.mTargetSpringConfig);
        physicsAnimator.withEndActions(new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$JjtgIlfsvLfISVWRI9f7tSgS_AA */

            public final void run() {
                PipTouchHandler.this.lambda$hideDismissTarget$2$PipTouchHandler();
            }
        });
        physicsAnimator.start();
        ((TransitionDrawable) this.mTargetViewContainer.getBackground()).reverseTransition(200);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideDismissTarget$2 */
    public /* synthetic */ void lambda$hideDismissTarget$2$PipTouchHandler() {
        this.mTargetViewContainer.setVisibility(8);
    }

    private void cleanUpDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowTargetAction);
        if (this.mTargetViewContainer.isAttachedToWindow()) {
            this.mWindowManager.removeViewImmediate(this.mTargetViewContainer);
        }
    }

    /* access modifiers changed from: private */
    public void onRegistrationChanged(boolean z) {
        this.mAccessibilityManager.setPictureInPictureActionReplacingConnection(z ? this.mConnection : null);
        if (!z && this.mTouchState.isUserInteracting()) {
            cleanUpDismissTarget();
        }
    }

    /* access modifiers changed from: private */
    public void onAccessibilityShowMenu() {
        this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), true, willResizeMenu(), shouldShowResizeHandle());
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00dc, code lost:
        if (r11.mGesture.onUp(r11.mTouchState) != false) goto L_0x00fe;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handleTouchEvent(android.view.InputEvent r12) {
        /*
        // Method dump skipped, instructions count: 282
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.pip.phone.PipTouchHandler.handleTouchEvent(android.view.InputEvent):boolean");
    }

    private void sendAccessibilityHoverEvent(int i) {
        if (this.mAccessibilityManager.isEnabled()) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(i);
            obtain.setImportantForAccessibility(true);
            obtain.setSourceNodeId(AccessibilityNodeInfo.ROOT_NODE_ID);
            obtain.setWindowId(-3);
            this.mAccessibilityManager.sendAccessibilityEvent(obtain);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void updateDismissFraction() {
        if (this.mMenuController != null && !this.mIsImeShowing) {
            Rect bounds = this.mMotionHelper.getBounds();
            float f = (float) this.mInsetBounds.bottom;
            int i = bounds.bottom;
            float min = ((float) i) > f ? Math.min((((float) i) - f) / ((float) bounds.height()), 1.0f) : 0.0f;
            if (Float.compare(min, 0.0f) != 0 || this.mMenuController.isMenuActivityVisible()) {
                this.mMenuController.setDismissFraction(min);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPinnedStackController(IPinnedStackController iPinnedStackController) {
        this.mPinnedStackController = iPinnedStackController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMenuState(int i, boolean z, Runnable runnable) {
        if (this.mMenuState != i || z) {
            boolean z2 = false;
            if (i != 2 || this.mMenuState == 2) {
                if (i == 0 && this.mMenuState == 2) {
                    if (z) {
                        if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                            try {
                                int displayRotation = this.mPinnedStackController.getDisplayRotation();
                                if (this.mDisplayRotation != displayRotation) {
                                    this.mDeferResizeToNormalBoundsUntilRotation = displayRotation;
                                }
                            } catch (RemoteException unused) {
                                Log.e("PipTouchHandler", "Could not get display rotation from controller");
                            }
                        }
                        if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                            Rect rect = new Rect(this.mResizedBounds);
                            Rect rect2 = new Rect();
                            this.mSnapAlgorithm.getMovementBounds(rect, this.mInsetBounds, rect2, this.mIsImeShowing ? this.mImeHeight : 0);
                            this.mMotionHelper.animateToUnexpandedState(rect, this.mSavedSnapFraction, rect2, this.mMovementBounds, false);
                            this.mSavedSnapFraction = -1.0f;
                        }
                    } else {
                        this.mSavedSnapFraction = -1.0f;
                    }
                }
            } else if (z) {
                this.mResizedBounds.set(this.mMotionHelper.getBounds());
                this.mSavedSnapFraction = this.mMotionHelper.animateToExpandedState(new Rect(this.mExpandedBounds), this.mMovementBounds, this.mExpandedMovementBounds, runnable);
            }
            this.mMenuState = i;
            updateMovementBounds();
            if (i == 0) {
                z2 = true;
            }
            onRegistrationChanged(z2);
            if (i == 0) {
                this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_HIDE_MENU);
            } else if (i == 2) {
                this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_SHOW_MENU);
            }
        }
    }

    public PipMotionHelper getMotionHelper() {
        return this.mMotionHelper;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public PipResizeGestureHandler getPipResizeGestureHandler() {
        return this.mPipResizeGestureHandler;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setPipResizeGestureHandler(PipResizeGestureHandler pipResizeGestureHandler) {
        this.mPipResizeGestureHandler = pipResizeGestureHandler;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setPipMotionHelper(PipMotionHelper pipMotionHelper) {
        this.mMotionHelper = pipMotionHelper;
    }

    public Rect getNormalBounds() {
        return this.mNormalBounds;
    }

    /* access modifiers changed from: private */
    public class DefaultPipTouchGesture extends PipTouchGesture {
        private final PointF mDelta;
        private boolean mShouldHideMenuAfterFling;
        private final Point mStartPosition;

        private DefaultPipTouchGesture() {
            this.mStartPosition = new Point();
            this.mDelta = new PointF();
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public void onDown(PipTouchState pipTouchState) {
            if (pipTouchState.isUserInteracting()) {
                Rect possiblyAnimatingBounds = PipTouchHandler.this.mMotionHelper.getPossiblyAnimatingBounds();
                this.mDelta.set(0.0f, 0.0f);
                this.mStartPosition.set(possiblyAnimatingBounds.left, possiblyAnimatingBounds.top);
                PipTouchHandler.this.mMovementWithinDismiss = pipTouchState.getDownTouchPosition().y >= ((float) PipTouchHandler.this.mMovementBounds.bottom);
                PipTouchHandler.this.mMotionHelper.setSpringingToTouch(false);
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.pokeMenu();
                }
            }
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public boolean onMove(PipTouchState pipTouchState) {
            boolean z = false;
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            if (pipTouchState.startedDragging()) {
                PipTouchHandler.this.mSavedSnapFraction = -1.0f;
                if (PipTouchHandler.this.mEnableDismissDragToEdge && PipTouchHandler.this.mTargetViewContainer.getVisibility() != 0) {
                    PipTouchHandler.this.mHandler.removeCallbacks(PipTouchHandler.this.mShowTargetAction);
                    PipTouchHandler.this.showDismissTargetMaybe();
                }
            }
            if (!pipTouchState.isDragging()) {
                return false;
            }
            PointF lastTouchDelta = pipTouchState.getLastTouchDelta();
            Point point = this.mStartPosition;
            PointF pointF = this.mDelta;
            float f = pointF.x;
            float f2 = ((float) point.x) + f;
            float f3 = pointF.y;
            float f4 = ((float) point.y) + f3;
            float f5 = lastTouchDelta.x + f2;
            float f6 = lastTouchDelta.y + f4;
            pointF.x = f + (f5 - f2);
            pointF.y = f3 + (f6 - f4);
            PipTouchHandler.this.mTmpBounds.set(PipTouchHandler.this.mMotionHelper.getPossiblyAnimatingBounds());
            PipTouchHandler.this.mTmpBounds.offsetTo((int) f5, (int) f6);
            PipTouchHandler.this.mMotionHelper.movePip(PipTouchHandler.this.mTmpBounds, true);
            PointF lastTouchPosition = pipTouchState.getLastTouchPosition();
            if (PipTouchHandler.this.mMovementWithinDismiss) {
                PipTouchHandler pipTouchHandler = PipTouchHandler.this;
                if (lastTouchPosition.y >= ((float) pipTouchHandler.mMovementBounds.bottom)) {
                    z = true;
                }
                pipTouchHandler.mMovementWithinDismiss = z;
            }
            return true;
        }

        @Override // com.android.systemui.pip.phone.PipTouchGesture
        public boolean onUp(PipTouchState pipTouchState) {
            if (PipTouchHandler.this.mEnableDismissDragToEdge) {
                PipTouchHandler.this.hideDismissTarget();
            }
            boolean z = false;
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            PointF velocity = pipTouchState.getVelocity();
            if (pipTouchState.isDragging()) {
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.showMenu(PipTouchHandler.this.mMenuState, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
                }
                if (PipTouchHandler.this.mMenuState == 0) {
                    z = true;
                }
                this.mShouldHideMenuAfterFling = z;
                PipTouchHandler.this.mTouchState.reset();
                PipTouchHandler.this.mMotionHelper.flingToSnapTarget(velocity.x, velocity.y, new Runnable() {
                    /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$DefaultPipTouchGesture$K8tFYcJKtB3Bkuu5piDq01YhA */

                    public final void run() {
                        PipTouchHandler.this.updateDismissFraction();
                    }
                }, new Runnable() {
                    /* class com.android.systemui.pip.phone.$$Lambda$PipTouchHandler$DefaultPipTouchGesture$c8YgJLEypMoVYe3YjylatK650zk */

                    public final void run() {
                        PipTouchHandler.DefaultPipTouchGesture.this.flingEndAction();
                    }
                });
            } else if (PipTouchHandler.this.mTouchState.isDoubleTap()) {
                PipTouchHandler.this.setTouchEnabled(false);
                PipTouchHandler.this.mMotionHelper.expandPipToFullscreen();
            } else if (PipTouchHandler.this.mMenuState != 2) {
                if (!PipTouchHandler.this.mTouchState.isWaitingForDoubleTap()) {
                    PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
                } else {
                    PipTouchHandler.this.mTouchState.scheduleDoubleTapTimeoutCallback();
                }
            }
            return true;
        }

        /* access modifiers changed from: private */
        public void flingEndAction() {
            if (this.mShouldHideMenuAfterFling) {
                PipTouchHandler.this.mMenuController.hideMenu();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateMovementBounds() {
        int i = 0;
        this.mSnapAlgorithm.getMovementBounds(this.mMotionHelper.getBounds(), this.mInsetBounds, this.mMovementBounds, this.mIsImeShowing ? this.mImeHeight : 0);
        this.mMotionHelper.setCurrentMovementBounds(this.mMovementBounds);
        boolean z = this.mMenuState == 2;
        PipBoundsHandler pipBoundsHandler = this.mPipBoundsHandler;
        if (z && willResizeMenu()) {
            i = this.mExpandedShortestEdgeSize;
        }
        pipBoundsHandler.setMinEdgeSize(i);
    }

    /* access modifiers changed from: private */
    public Rect getMovementBounds(Rect rect) {
        Rect rect2 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(rect, this.mInsetBounds, rect2, this.mIsImeShowing ? this.mImeHeight : 0);
        return rect2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean willResizeMenu() {
        if (!this.mEnableResize) {
            return false;
        }
        if (this.mExpandedBounds.width() == this.mNormalBounds.width() && this.mExpandedBounds.height() == this.mNormalBounds.height()) {
            return false;
        }
        return true;
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipTouchHandler");
        printWriter.println(str2 + "mMovementBounds=" + this.mMovementBounds);
        printWriter.println(str2 + "mNormalBounds=" + this.mNormalBounds);
        printWriter.println(str2 + "mNormalMovementBounds=" + this.mNormalMovementBounds);
        printWriter.println(str2 + "mExpandedBounds=" + this.mExpandedBounds);
        printWriter.println(str2 + "mExpandedMovementBounds=" + this.mExpandedMovementBounds);
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mIsImeShowing=" + this.mIsImeShowing);
        printWriter.println(str2 + "mImeHeight=" + this.mImeHeight);
        printWriter.println(str2 + "mIsShelfShowing=" + this.mIsShelfShowing);
        printWriter.println(str2 + "mShelfHeight=" + this.mShelfHeight);
        printWriter.println(str2 + "mSavedSnapFraction=" + this.mSavedSnapFraction);
        printWriter.println(str2 + "mEnableDragToEdgeDismiss=" + this.mEnableDismissDragToEdge);
        printWriter.println(str2 + "mMovementBoundsExtraOffsets=" + this.mMovementBoundsExtraOffsets);
        this.mTouchState.dump(printWriter, str2);
        this.mMotionHelper.dump(printWriter, str2);
        PipResizeGestureHandler pipResizeGestureHandler = this.mPipResizeGestureHandler;
        if (pipResizeGestureHandler != null) {
            pipResizeGestureHandler.dump(printWriter, str2);
        }
    }
}
