package com.android.systemui.pip.phone;

import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.IPinnedStackController;
import android.view.InputEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.phone.InputConsumerController;
import com.android.systemui.pip.phone.PipAccessibilityInteractionConnection;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.pip.phone.PipTouchHandler;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.util.DismissCircleView;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.PrintWriter;

public class PipTouchHandler {
    private final AccessibilityManager mAccessibilityManager;
    /* access modifiers changed from: private */
    public final IActivityManager mActivityManager;
    private int mBottomOffsetBufferPx;
    private PipAccessibilityInteractionConnection mConnection;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mDeferResizeToNormalBoundsUntilRotation = -1;
    private int mDismissAreaHeight;
    private int mDisplayRotation;
    /* access modifiers changed from: private */
    public final boolean mEnableDismissDragToEdge;
    private final boolean mEnableResize;
    private Rect mExpandedBounds = new Rect();
    Rect mExpandedMovementBounds = new Rect();
    private int mExpandedShortestEdgeSize;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private PipTouchGesture mGesture;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private boolean mHideMenuAfterShown = false;
    private int mImeHeight;
    private int mImeOffset;
    private Rect mInsetBounds = new Rect();
    private boolean mIsImeShowing;
    private boolean mIsShelfShowing;
    private MagnetizedObject.MagneticTarget mMagneticTarget;
    private PhysicsAnimator mMagneticTargetAnimator;
    private MagnetizedObject mMagnetizedPip;
    /* access modifiers changed from: private */
    public final PipMenuActivityController mMenuController;
    /* access modifiers changed from: private */
    public int mMenuState = 0;
    /* access modifiers changed from: private */
    public PipMotionHelper mMotionHelper;
    /* access modifiers changed from: private */
    public Rect mMovementBounds = new Rect();
    private int mMovementBoundsExtraOffsets;
    /* access modifiers changed from: private */
    public boolean mMovementWithinDismiss;
    private Rect mNormalBounds = new Rect();
    Rect mNormalMovementBounds = new Rect();
    private IPinnedStackController mPinnedStackController;
    private final PipBoundsHandler mPipBoundsHandler;
    private PipResizeGestureHandler mPipResizeGestureHandler;
    Rect mResizedBounds = new Rect();
    /* access modifiers changed from: private */
    public float mSavedSnapFraction = -1.0f;
    private boolean mSendingHoverAccessibilityEvents;
    private int mShelfHeight;
    private boolean mShowPipMenuOnAnimationEnd = false;
    /* access modifiers changed from: private */
    public Runnable mShowTargetAction = new Runnable() {
        public final void run() {
            PipTouchHandler.this.showDismissTargetMaybe();
        }
    };
    private final PipSnapAlgorithm mSnapAlgorithm;
    private final PhysicsAnimator.SpringConfig mTargetSpringConfig = new PhysicsAnimator.SpringConfig(1500.0f, 1.0f);
    private DismissCircleView mTargetView;
    /* access modifiers changed from: private */
    public ViewGroup mTargetViewContainer;
    /* access modifiers changed from: private */
    public final Rect mTmpBounds = new Rect();
    /* access modifiers changed from: private */
    public final PipTouchState mTouchState;
    private final WindowManager mWindowManager;

    private class PipMenuListener implements PipMenuActivityController.Listener {
        private PipMenuListener() {
        }

        public void onPipMenuStateChanged(int i, boolean z, Runnable runnable) {
            PipTouchHandler.this.setMenuState(i, z, runnable);
        }

        public void onPipExpand() {
            PipTouchHandler.this.mMotionHelper.expandPipToFullscreen();
        }

        public void onPipDismiss() {
            Pair<ComponentName, Integer> topPipActivity = PipUtils.getTopPipActivity(PipTouchHandler.this.mContext, PipTouchHandler.this.mActivityManager);
            if (topPipActivity.first != null) {
                MetricsLoggerWrapper.logPictureInPictureDismissByTap(PipTouchHandler.this.mContext, topPipActivity);
            }
            PipTouchHandler.this.mTouchState.removeDoubleTapTimeoutCallback();
            PipTouchHandler.this.mMotionHelper.dismissPip();
        }

        public void onPipShowMenu() {
            PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
        }
    }

    public PipTouchHandler(Context context, IActivityManager iActivityManager, IActivityTaskManager iActivityTaskManager, PipMenuActivityController pipMenuActivityController, InputConsumerController inputConsumerController, PipBoundsHandler pipBoundsHandler, PipTaskOrganizer pipTaskOrganizer, PipSnapAlgorithm pipSnapAlgorithm) {
        Context context2 = context;
        PipMenuActivityController pipMenuActivityController2 = pipMenuActivityController;
        InputConsumerController inputConsumerController2 = inputConsumerController;
        PipBoundsHandler pipBoundsHandler2 = pipBoundsHandler;
        this.mContext = context2;
        this.mActivityManager = iActivityManager;
        this.mAccessibilityManager = (AccessibilityManager) context2.getSystemService(AccessibilityManager.class);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mMenuController = pipMenuActivityController2;
        pipMenuActivityController2.addListener(new PipMenuListener());
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mFlingAnimationUtils = new FlingAnimationUtils(context2, 2.5f);
        this.mGesture = new DefaultPipTouchGesture();
        PipMotionHelper pipMotionHelper = new PipMotionHelper(this.mContext, iActivityTaskManager, pipTaskOrganizer, this.mMenuController, this.mSnapAlgorithm, this.mFlingAnimationUtils);
        this.mMotionHelper = pipMotionHelper;
        this.mPipResizeGestureHandler = new PipResizeGestureHandler(context2, pipBoundsHandler2, pipMotionHelper, pipTaskOrganizer);
        this.mTouchState = new PipTouchState(ViewConfiguration.get(context), this.mHandler, new Runnable() {
            public final void run() {
                PipTouchHandler.this.lambda$new$0$PipTouchHandler();
            }
        });
        Resources resources = context.getResources();
        this.mEnableDismissDragToEdge = resources.getBoolean(R.bool.config_pipEnableDismissDragToEdge);
        this.mEnableResize = resources.getBoolean(R.bool.config_pipEnableResizeForMenu);
        reloadResources();
        inputConsumerController2.setInputListener(new InputConsumerController.InputListener() {
            public final boolean onInputEvent(InputEvent inputEvent) {
                return PipTouchHandler.this.handleTouchEvent(inputEvent);
            }
        });
        inputConsumerController2.setRegistrationListener(new InputConsumerController.RegistrationListener() {
            public final void onRegistrationChanged(boolean z) {
                PipTouchHandler.this.onRegistrationChanged(z);
            }
        });
        this.mPipBoundsHandler = pipBoundsHandler2;
        this.mConnection = new PipAccessibilityInteractionConnection(this.mMotionHelper, new PipAccessibilityInteractionConnection.AccessibilityCallbacks() {
            public final void onAccessibilityShowMenu() {
                PipTouchHandler.this.onAccessibilityShowMenu();
            }
        }, this.mHandler);
        resources.getDimensionPixelSize(R.dimen.dismiss_circle_size);
        this.mTargetView = new DismissCircleView(context2);
        FrameLayout frameLayout = new FrameLayout(context2);
        this.mTargetViewContainer = frameLayout;
        frameLayout.setClipChildren(false);
        this.mTargetViewContainer.addView(this.mTargetView);
        MagnetizedObject magnetizedPip = this.mMotionHelper.getMagnetizedPip();
        this.mMagnetizedPip = magnetizedPip;
        this.mMagneticTarget = magnetizedPip.addTarget(this.mTargetView, 0);
        updateMagneticTargetSize();
        this.mMagnetizedPip.setPhysicsAnimatorUpdateListener(this.mMotionHelper.mResizePipUpdateListener);
        this.mMagnetizedPip.setMagnetListener(new MagnetizedObject.MagnetListener() {
            public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
                PipTouchHandler.this.mMotionHelper.prepareForAnimation();
                PipTouchHandler.this.showDismissTargetMaybe();
            }

            public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
                if (z) {
                    PipTouchHandler.this.mMotionHelper.flingToSnapTarget(f, f2, (Runnable) null, (Runnable) null);
                    PipTouchHandler.this.hideDismissTarget();
                    return;
                }
                PipTouchHandler.this.mMotionHelper.setSpringingToTouch(true);
            }

            public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
                PipTouchHandler.this.mHandler.post(new Runnable() {
                    public final void run() {
                        PipTouchHandler.AnonymousClass1.this.lambda$onReleasedInTarget$0$PipTouchHandler$1();
                    }
                });
                MetricsLoggerWrapper.logPictureInPictureDismissByDrag(PipTouchHandler.this.mContext, PipUtils.getTopPipActivity(PipTouchHandler.this.mContext, PipTouchHandler.this.mActivityManager));
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

    private void reloadResources() {
        Resources resources = this.mContext.getResources();
        this.mBottomOffsetBufferPx = 3;
        this.mExpandedShortestEdgeSize = resources.getDimensionPixelSize(R.dimen.pip_expanded_shortest_edge_size);
        this.mImeOffset = resources.getDimensionPixelSize(R.dimen.pip_ime_offset);
        this.mDismissAreaHeight = resources.getDimensionPixelSize(R.dimen.floating_dismiss_gradient_height);
        updateMagneticTargetSize();
    }

    private void updateMagneticTargetSize() {
        if (this.mTargetView != null) {
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.dismiss_circle_size);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
            layoutParams.gravity = 17;
            this.mTargetView.setLayoutParams(layoutParams);
            this.mMagneticTarget.setMagneticFieldRadiusPx(dimensionPixelSize * 2);
        }
    }

    /* access modifiers changed from: private */
    public boolean shouldShowResizeHandle() {
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
    }

    public void onActivityUnpinned(ComponentName componentName) {
        if (componentName == null) {
            cleanUpDismissTarget();
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
        this.mMotionHelper.onConfigurationChanged();
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
        Rect rect4;
        boolean z3 = false;
        int i2 = this.mIsImeShowing ? this.mImeHeight : 0;
        if (this.mDisplayRotation != i) {
            this.mTouchState.reset();
        }
        this.mNormalBounds.set(rect2);
        Rect rect5 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mNormalBounds, rect, rect5, i2);
        if (this.mMovementBounds.isEmpty()) {
            this.mSnapAlgorithm.getMovementBounds(rect3, rect, this.mMovementBounds, 0);
        }
        float width = ((float) rect2.width()) / ((float) rect2.height());
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        Size sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(width, (float) this.mExpandedShortestEdgeSize, point.x, point.y);
        this.mExpandedBounds.set(0, 0, sizeForAspectRatio.getWidth(), sizeForAspectRatio.getHeight());
        Rect rect6 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mExpandedBounds, rect, rect6, i2);
        this.mPipResizeGestureHandler.updateMinSize(this.mNormalBounds.width(), this.mNormalBounds.height());
        this.mPipResizeGestureHandler.updateMaxSize(this.mExpandedBounds.width(), this.mExpandedBounds.height());
        int max = Math.max(this.mIsImeShowing ? this.mImeOffset : 0, (this.mIsImeShowing || !this.mIsShelfShowing) ? 0 : this.mShelfHeight);
        if ((z || z2) && !this.mTouchState.isUserInteracting()) {
            if (this.mMenuState == 2 && willResizeMenu()) {
                z3 = true;
            }
            if (z3) {
                rect4 = new Rect(rect6);
            } else {
                rect4 = new Rect(rect5);
            }
            int i3 = this.mMovementBounds.bottom - this.mMovementBoundsExtraOffsets;
            int i4 = rect4.bottom;
            if (i4 >= rect4.top) {
                i4 -= max;
            }
            if (z3) {
                rect3.set(this.mExpandedBounds);
                this.mSnapAlgorithm.applySnapFraction(rect3, rect4, this.mSavedSnapFraction);
            }
            int min = Math.min(i3, i4) - this.mBottomOffsetBufferPx;
            int i5 = rect3.top;
            if (min <= i5 && i5 <= Math.max(i3, i4) + this.mBottomOffsetBufferPx) {
                this.mMotionHelper.animateToOffset(rect3, i4 - rect3.top);
            }
        }
        this.mNormalMovementBounds.set(rect5);
        this.mExpandedMovementBounds.set(rect6);
        this.mDisplayRotation = i;
        this.mInsetBounds.set(rect);
        updateMovementBounds();
        this.mMovementBoundsExtraOffsets = max;
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
    public void showDismissTargetMaybe() {
        createOrUpdateDismissTarget();
        if (this.mTargetViewContainer.getVisibility() != 0) {
            this.mTargetView.setTranslationY((float) this.mTargetViewContainer.getHeight());
            this.mTargetViewContainer.setVisibility(0);
            this.mMagneticTargetAnimator.cancel();
            PhysicsAnimator physicsAnimator = this.mMagneticTargetAnimator;
            physicsAnimator.spring(DynamicAnimation.TRANSLATION_Y, 0.0f, this.mTargetSpringConfig);
            physicsAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    public void hideDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowTargetAction);
        PhysicsAnimator physicsAnimator = this.mMagneticTargetAnimator;
        physicsAnimator.spring(DynamicAnimation.TRANSLATION_Y, (float) this.mTargetViewContainer.getHeight(), this.mTargetSpringConfig);
        physicsAnimator.withEndActions(new Runnable() {
            public final void run() {
                PipTouchHandler.this.lambda$hideDismissTarget$1$PipTouchHandler();
            }
        });
        physicsAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideDismissTarget$1 */
    public /* synthetic */ void lambda$hideDismissTarget$1$PipTouchHandler() {
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
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e4, code lost:
        if (r11.mGesture.onUp(r11.mTouchState) != false) goto L_0x010b;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handleTouchEvent(android.view.InputEvent r12) {
        /*
            r11 = this;
            java.lang.String r0 = "pip_test"
            java.lang.String r1 = "handleTouchEvent111"
            android.util.Log.e(r0, r1)
            boolean r1 = r12 instanceof android.view.MotionEvent
            r2 = 1
            if (r1 != 0) goto L_0x000d
            return r2
        L_0x000d:
            android.view.IPinnedStackController r1 = r11.mPinnedStackController
            if (r1 != 0) goto L_0x0012
            return r2
        L_0x0012:
            android.view.MotionEvent r12 = (android.view.MotionEvent) r12
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "handleTouchEvent2222  action="
            r1.append(r3)
            int r3 = r12.getAction()
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            android.util.Log.e(r0, r1)
            com.android.systemui.pip.phone.PipResizeGestureHandler r0 = r11.mPipResizeGestureHandler
            float r1 = r12.getRawX()
            int r1 = (int) r1
            float r3 = r12.getRawY()
            int r3 = (int) r3
            boolean r0 = r0.isWithinTouchRegion(r1, r3)
            if (r0 == 0) goto L_0x003f
            return r2
        L_0x003f:
            com.android.systemui.util.magnetictarget.MagnetizedObject r0 = r11.mMagnetizedPip
            boolean r0 = r0.maybeConsumeMotionEvent(r12)
            if (r0 == 0) goto L_0x0058
            int r0 = r12.getAction()
            if (r0 != 0) goto L_0x0052
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            r0.onTouchEvent(r12)
        L_0x0052:
            com.android.systemui.pip.phone.PipTouchState r11 = r11.mTouchState
            r11.addMovementToVelocityTracker(r12)
            return r2
        L_0x0058:
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            r0.onTouchEvent(r12)
            int r0 = r11.mMenuState
            r1 = 0
            if (r0 == 0) goto L_0x0064
            r0 = r2
            goto L_0x0065
        L_0x0064:
            r0 = r1
        L_0x0065:
            int r3 = r12.getAction()
            r4 = 3
            if (r3 == 0) goto L_0x00ff
            if (r3 == r2) goto L_0x00d9
            r5 = 2
            if (r3 == r5) goto L_0x00c6
            if (r3 == r4) goto L_0x00e7
            r5 = 7
            if (r3 == r5) goto L_0x00b8
            r5 = 9
            if (r3 == r5) goto L_0x009e
            r5 = 10
            if (r3 == r5) goto L_0x0080
            goto L_0x010b
        L_0x0080:
            r11.mHideMenuAfterShown = r2
            android.view.accessibility.AccessibilityManager r3 = r11.mAccessibilityManager
            boolean r3 = r3.isTouchExplorationEnabled()
            if (r3 != 0) goto L_0x008f
            com.android.systemui.pip.phone.PipMenuActivityController r3 = r11.mMenuController
            r3.hideMenu()
        L_0x008f:
            if (r0 != 0) goto L_0x010b
            boolean r3 = r11.mSendingHoverAccessibilityEvents
            if (r3 == 0) goto L_0x010b
            r3 = 256(0x100, float:3.59E-43)
            r11.sendAccessibilityHoverEvent(r3)
            r11.mSendingHoverAccessibilityEvents = r1
            goto L_0x010b
        L_0x009e:
            android.view.accessibility.AccessibilityManager r1 = r11.mAccessibilityManager
            boolean r1 = r1.isTouchExplorationEnabled()
            if (r1 != 0) goto L_0x00b8
            com.android.systemui.pip.phone.PipMenuActivityController r5 = r11.mMenuController
            r6 = 2
            com.android.systemui.pip.phone.PipMotionHelper r1 = r11.mMotionHelper
            android.graphics.Rect r7 = r1.getBounds()
            r8 = 0
            r9 = 0
            boolean r10 = r11.shouldShowResizeHandle()
            r5.showMenu(r6, r7, r8, r9, r10)
        L_0x00b8:
            if (r0 != 0) goto L_0x010b
            boolean r1 = r11.mSendingHoverAccessibilityEvents
            if (r1 != 0) goto L_0x010b
            r1 = 128(0x80, float:1.794E-43)
            r11.sendAccessibilityHoverEvent(r1)
            r11.mSendingHoverAccessibilityEvents = r2
            goto L_0x010b
        L_0x00c6:
            com.android.systemui.pip.phone.PipTouchGesture r1 = r11.mGesture
            com.android.systemui.pip.phone.PipTouchState r3 = r11.mTouchState
            boolean r1 = r1.onMove(r3)
            if (r1 == 0) goto L_0x00d1
            goto L_0x010b
        L_0x00d1:
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            boolean r0 = r0.isDragging()
            r0 = r0 ^ r2
            goto L_0x010b
        L_0x00d9:
            r11.updateMovementBounds()
            com.android.systemui.pip.phone.PipTouchGesture r3 = r11.mGesture
            com.android.systemui.pip.phone.PipTouchState r5 = r11.mTouchState
            boolean r3 = r3.onUp(r5)
            if (r3 == 0) goto L_0x00e7
            goto L_0x010b
        L_0x00e7:
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            boolean r0 = r0.startedDragging()
            if (r0 != 0) goto L_0x00f8
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            boolean r0 = r0.isDragging()
            if (r0 != 0) goto L_0x00f8
            r1 = r2
        L_0x00f8:
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            r0.reset()
            r0 = r1
            goto L_0x010b
        L_0x00ff:
            com.android.systemui.pip.phone.PipMotionHelper r1 = r11.mMotionHelper
            r1.synchronizePinnedStackBoundsForTouchGesture()
            com.android.systemui.pip.phone.PipTouchGesture r1 = r11.mGesture
            com.android.systemui.pip.phone.PipTouchState r3 = r11.mTouchState
            r1.onDown(r3)
        L_0x010b:
            if (r0 == 0) goto L_0x0126
            android.view.MotionEvent r12 = android.view.MotionEvent.obtain(r12)
            com.android.systemui.pip.phone.PipTouchState r0 = r11.mTouchState
            boolean r0 = r0.startedDragging()
            if (r0 == 0) goto L_0x0121
            r12.setAction(r4)
            com.android.systemui.pip.phone.PipMenuActivityController r0 = r11.mMenuController
            r0.pokeMenu()
        L_0x0121:
            com.android.systemui.pip.phone.PipMenuActivityController r11 = r11.mMenuController
            r11.handlePointerEvent(r12)
        L_0x0126:
            return r2
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
    public void setMenuState(int i, boolean z, Runnable runnable) {
        if (this.mMenuState != i || z) {
            boolean z2 = false;
            if (i == 2 && this.mMenuState != 2) {
                if (z) {
                    this.mResizedBounds.set(this.mMotionHelper.getBounds());
                    this.mSavedSnapFraction = this.mMotionHelper.animateToExpandedState(new Rect(this.mExpandedBounds), this.mMovementBounds, this.mExpandedMovementBounds, runnable);
                }
                if (this.mHideMenuAfterShown) {
                    this.mMenuController.hideMenu();
                }
            } else if (i == 0 && this.mMenuState == 2) {
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
            this.mMenuState = i;
            this.mHideMenuAfterShown = false;
            updateMovementBounds();
            onRegistrationChanged(i == 0);
            if (i != 1) {
                Context context = this.mContext;
                if (i == 2) {
                    z2 = true;
                }
                MetricsLoggerWrapper.logPictureInPictureMenuVisible(context, z2);
            }
        }
    }

    public PipMotionHelper getMotionHelper() {
        return this.mMotionHelper;
    }

    public Rect getNormalBounds() {
        return this.mNormalBounds;
    }

    private class DefaultPipTouchGesture extends PipTouchGesture {
        private final PointF mDelta;
        private boolean mShouldHideMenuAfterFling;
        private final Point mStartPosition;

        private DefaultPipTouchGesture() {
            this.mStartPosition = new Point();
            this.mDelta = new PointF();
        }

        public void onDown(PipTouchState pipTouchState) {
            if (pipTouchState.isUserInteracting()) {
                Rect bounds = PipTouchHandler.this.mMotionHelper.getBounds();
                this.mDelta.set(0.0f, 0.0f);
                this.mStartPosition.set(bounds.left, bounds.top);
                boolean unused = PipTouchHandler.this.mMovementWithinDismiss = pipTouchState.getDownTouchPosition().y >= ((float) PipTouchHandler.this.mMovementBounds.bottom);
                PipTouchHandler.this.mMotionHelper.setSpringingToTouch(false);
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.pokeMenu();
                }
            }
        }

        public boolean onMove(PipTouchState pipTouchState) {
            boolean z = false;
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            if (pipTouchState.startedDragging()) {
                float unused = PipTouchHandler.this.mSavedSnapFraction = -1.0f;
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
            PipTouchHandler.this.mTmpBounds.set(PipTouchHandler.this.mMotionHelper.getBounds());
            PipTouchHandler.this.mTmpBounds.offsetTo((int) f5, (int) f6);
            PipTouchHandler.this.mMotionHelper.movePip(PipTouchHandler.this.mTmpBounds, true);
            PointF lastTouchPosition = pipTouchState.getLastTouchPosition();
            if (PipTouchHandler.this.mMovementWithinDismiss) {
                PipTouchHandler pipTouchHandler = PipTouchHandler.this;
                if (lastTouchPosition.y >= ((float) pipTouchHandler.mMovementBounds.bottom)) {
                    z = true;
                }
                boolean unused2 = pipTouchHandler.mMovementWithinDismiss = z;
            }
            return true;
        }

        public boolean onUp(PipTouchState pipTouchState) {
            if (PipTouchHandler.this.mEnableDismissDragToEdge) {
                PipTouchHandler.this.hideDismissTarget();
            }
            boolean z = false;
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            PointF velocity = pipTouchState.getVelocity();
            PointF.length(velocity.x, velocity.y);
            if (pipTouchState.isDragging()) {
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.showMenu(PipTouchHandler.this.mMenuState, PipTouchHandler.this.mMotionHelper.getBounds(), true, PipTouchHandler.this.willResizeMenu(), PipTouchHandler.this.shouldShowResizeHandle());
                }
                if (PipTouchHandler.this.mMenuState == 0) {
                    z = true;
                }
                this.mShouldHideMenuAfterFling = z;
                PipTouchHandler.this.mMotionHelper.flingToSnapTarget(velocity.x, velocity.y, new Runnable() {
                    public final void run() {
                        PipTouchHandler.this.updateDismissFraction();
                    }
                }, new Runnable() {
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
            PipTouchHandler.this.mTouchState.setAllowTouches(true);
            if (this.mShouldHideMenuAfterFling) {
                PipTouchHandler.this.mMenuController.hideMenu();
            }
        }
    }

    private void updateMovementBounds() {
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
    public boolean willResizeMenu() {
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
        this.mTouchState.dump(printWriter, str2);
        this.mMotionHelper.dump(printWriter, str2);
    }
}
