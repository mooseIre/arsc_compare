package com.android.systemui.controlcenter.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.policy.BoostHelper;
import com.android.systemui.controlcenter.policy.NCSwitchController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.util.AccessibilityUtils;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;

public class ControlPanelWindowView extends FrameLayout {
    private boolean mAnimating;
    private boolean mAttached;
    private IStateStyle mBlurAmin;
    private float mBlurRatio;
    private TransitionListener mBlurRatioListener;
    private View mBottomArea;
    private AnimatorListenerAdapter mCollapseListener;
    private boolean mCollapsingAnim;
    private ControlPanelContentView mContent;
    private boolean mContentShowing;
    private ControlCenter mControlCenter;
    private ControlCenterPanelView mControlCenterPanel;
    private QSControlCenterTileLayout mControlCenterTileLayout;
    private ControlPanelController mControlPanelController;
    private ControlPanelWindowManager mControlPanelWindowManager;
    private float mDownExpandHeight;
    private float mDownX;
    private float mDownY;
    private float mExpandHeight;
    private AnimatorListenerAdapter mExpandListener;
    private int mExpandState;
    private ValueAnimator mHeightChangeAnimator;
    private boolean mInterceptTouchEvent;
    private boolean mIsCancel;
    private boolean mIsDown;
    private boolean mIsGetSelfEvent;
    private boolean mIsIntercept;
    private boolean mIsMove;
    private boolean mIsMoveY;
    private boolean mIsUp;
    private int[] mLocation;
    private boolean mNCAnimSwitched;
    private boolean mNCBlurSwitched;

    public ControlPanelWindowView(Context context) {
        this(context, null);
    }

    public ControlPanelWindowView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ControlPanelWindowView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIsGetSelfEvent = false;
        this.mInterceptTouchEvent = false;
        this.mCollapsingAnim = false;
        this.mExpandState = 0;
        this.mAttached = false;
        this.mDownY = 0.0f;
        this.mDownX = 0.0f;
        this.mDownExpandHeight = 0.0f;
        this.mLocation = new int[2];
        this.mNCBlurSwitched = false;
        this.mNCAnimSwitched = false;
        this.mBlurRatioListener = new TransitionListener() {
            /* class com.android.systemui.controlcenter.phone.ControlPanelWindowView.AnonymousClass1 */

            @Override // miuix.animation.listener.TransitionListener
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                ControlPanelWindowView.this.mBlurRatio = f;
                ControlPanelWindowView.this.setAlpha(f);
                if (ControlPanelWindowView.this.mControlPanelWindowManager != null && !ControlPanelWindowView.this.mControlPanelController.isNCSwitching()) {
                    ControlPanelWindowView.this.mControlPanelWindowManager.setBlurRatio(ControlPanelWindowView.this.mBlurRatio);
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                if (ControlCenter.DEBUG) {
                    Log.d("ControllerPanelWindowView", "onComplete mBlurRatio:" + ControlPanelWindowView.this.mBlurRatio + " state:" + ControlPanelWindowView.this.mExpandState);
                }
                if (ControlPanelWindowView.this.mBlurRatio == 0.0f || (ControlPanelWindowView.this.mBlurRatio == 1.0f && !ControlPanelWindowView.this.isCollapsed())) {
                    ControlPanelWindowView.this.mControlPanelController.requestNCSwitching(false);
                }
            }
        };
        this.mCollapseListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.controlcenter.phone.ControlPanelWindowView.AnonymousClass3 */

            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                ControlPanelWindowView.this.mCollapsingAnim = true;
            }

            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                ControlPanelWindowView.this.mAnimating = false;
                ControlPanelWindowView.this.mCollapsingAnim = false;
                Log.d("ControllerPanelWindowView", "onAnimationCancel");
                ControlPanelWindowView.this.hideControlCenterWindow();
                ControlPanelWindowView.this.mControlCenterPanel.finishCollapse();
                ControlPanelWindowView.this.mControlPanelWindowManager.trimMemory();
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                ControlPanelWindowView.this.mAnimating = false;
                ControlPanelWindowView.this.hideControlCenterWindow();
                ControlPanelWindowView.this.mControlCenterPanel.finishCollapse();
                ControlPanelWindowView.this.mControlPanelWindowManager.trimMemory();
                ControlPanelWindowView.this.onControlPanelHide();
                ControlPanelWindowView.this.mCollapsingAnim = false;
            }
        };
        this.mExpandListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.controlcenter.phone.ControlPanelWindowView.AnonymousClass4 */

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                ControlPanelWindowView.this.mAnimating = false;
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContent = (ControlPanelContentView) findViewById(C0015R$id.control_panel_content);
        ControlCenterPanelView controlCenterPanelView = (ControlCenterPanelView) findViewById(C0015R$id.control_center_panel);
        this.mControlCenterPanel = controlCenterPanelView;
        controlCenterPanelView.setControlPanelWindowView(this);
        this.mControlCenterTileLayout = (QSControlCenterTileLayout) findViewById(C0015R$id.tile_layout);
        this.mBottomArea = findViewById(C0015R$id.control_center_bottom_area);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Folme.getValueTarget("ControlPanelViewBlur").setMinVisibleChange(0.01f, "blurRatio");
        this.mBlurAmin = Folme.useValue("ControlPanelViewBlur").addListener(this.mBlurRatioListener);
        this.mControlCenterPanel.setListening(false);
        this.mAttached = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBlurAmin.removeListener(this.mBlurRatioListener);
        this.mBlurAmin.clean();
        this.mAttached = false;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.orientation;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.mContent == null || !this.mControlCenter.panelEnabled()) {
            return false;
        }
        if (this.mCollapsingAnim) {
            return true;
        }
        if (motionEvent.getActionMasked() == 0) {
            verifyState();
        }
        this.mIsGetSelfEvent = true;
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return !isCollapsed();
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return !isCollapsed();
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (super.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        if (keyEvent.getAction() == 1 && !isCollapsed()) {
            if (keyEvent.getKeyCode() == 4) {
                if (this.mContent.isDetailShowing()) {
                    this.mControlCenterPanel.closeDetail(false);
                    return true;
                } else if (this.mContent.isEditShowing()) {
                    this.mContent.hideEdit();
                    return true;
                } else if (this.mContent.isControlEditShowing()) {
                    this.mContent.hideControlEdit();
                    return true;
                }
            }
            if (keyEvent.getKeyCode() == 4 || keyEvent.getKeyCode() == 3 || keyEvent.getKeyCode() == 82) {
                if (this.mContent.isDetailShowing()) {
                    this.mControlCenterPanel.closeDetail(false);
                }
                if (this.mContent.isEditShowing()) {
                    this.mContent.hideEdit();
                }
                if (this.mContent.isControlEditShowing()) {
                    this.mContent.hideControlEdit();
                }
                collapsePanel();
                return true;
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        this.mIsIntercept = this.mInterceptTouchEvent || !isExpanded() || super.onInterceptTouchEvent(motionEvent);
        this.mIsDown = motionEvent.getAction() == 0;
        this.mIsMove = motionEvent.getAction() == 2;
        this.mIsMoveY = Math.abs(motionEvent.getRawY() - this.mDownY) > Math.abs(motionEvent.getRawX() - this.mDownX);
        ((NCSwitchController) Dependency.get(NCSwitchController.class)).handleCNSwitchTouch(motionEvent, false);
        if (this.mIsDown) {
            this.mDownY = motionEvent.getRawY();
            this.mDownX = motionEvent.getRawX();
            this.mDownExpandHeight = this.mExpandHeight;
        } else if (this.mContent.isDetailShowing() || this.mContent.isEditShowing() || this.mContent.isControlEditShowing()) {
            return this.mIsIntercept;
        } else {
            if (this.mIsMove && isBottomAreaTouchDown(this.mDownY) && motionEvent.getRawY() < this.mDownY && this.mIsMoveY && this.mControlCenterPanel.shouldCollapseByBottomTouch()) {
                return true;
            }
        }
        return this.mIsIntercept;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return handleMotionEvent(motionEvent, false);
    }

    public void setControlCenter(ControlCenter controlCenter) {
        this.mControlCenter = controlCenter;
    }

    public void onUserSwitched(int i) {
        this.mControlCenterPanel.onUserSwitched(i);
    }

    private void verifyState() {
        if (this.mContent.getHeight() == 0) {
            this.mExpandState = 0;
            this.mIsGetSelfEvent = false;
        }
    }

    public boolean handleMotionEvent(MotionEvent motionEvent, boolean z) {
        return handleMotionEvent(motionEvent, z, true);
    }

    public boolean handleMotionEvent(MotionEvent motionEvent, boolean z, boolean z2) {
        if (this.mCollapsingAnim) {
            return true;
        }
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null && !controlCenter.panelEnabled()) {
            return false;
        }
        if (((NCSwitchController) Dependency.get(NCSwitchController.class)).handleCNSwitchTouch(motionEvent, this.mExpandState == 2 && !this.mControlPanelController.isNCSwitching())) {
            return true;
        }
        this.mIsDown = motionEvent.getAction() == 0;
        this.mIsMove = motionEvent.getAction() == 2;
        this.mIsUp = motionEvent.getAction() == 1;
        this.mIsCancel = motionEvent.getAction() == 3;
        this.mIsMoveY = Math.abs(motionEvent.getRawY() - this.mDownY) > Math.abs(motionEvent.getRawX() - this.mDownX);
        boolean z3 = !z || !this.mIsGetSelfEvent;
        if (this.mIsDown) {
            verifyState();
            this.mDownY = motionEvent.getRawY();
            this.mDownX = motionEvent.getRawX();
            if (!isExpanded()) {
                this.mContent.setVisibility(4);
                showControlCenterWindow();
                this.mDownExpandHeight = this.mExpandHeight;
            }
            if (z2) {
                this.mInterceptTouchEvent = true;
            }
        } else if (this.mContent.isDetailShowing() || this.mContent.isEditShowing() || this.mContent.isControlEditShowing()) {
            return false;
        } else {
            if (this.mIsMove && z3 && this.mIsMoveY) {
                float rawY = motionEvent.getRawY();
                float f = this.mDownY;
                if (rawY >= f) {
                    updateExpandHeight(Math.min((this.mDownExpandHeight + motionEvent.getRawY()) - this.mDownY, 80.0f));
                } else {
                    float f2 = this.mDownExpandHeight;
                    if (f2 < 80.0f) {
                        updateExpandHeight(Math.max(0.0f, (f2 + motionEvent.getRawY()) - this.mDownY));
                    } else if (f - motionEvent.getRawY() >= 80.0f) {
                        updateExpandHeight(Math.max(0.0f, 80.0f - ((this.mDownY - motionEvent.getRawY()) - 80.0f)));
                    }
                }
                updateTransHeight(motionEvent.getRawY() - this.mDownY);
            } else if (this.mIsUp || this.mIsCancel) {
                updateTransHeight(0.0f);
                if (this.mExpandState != 2) {
                    endWithCurrentExpandHeight();
                }
                this.mInterceptTouchEvent = false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateExpandHeight(float f) {
        if (!this.mAttached) {
            Log.d("ControllerPanelWindowView", "updateExpandHeight: not attached");
        } else if ((this.mControlCenter.isExpandable() || f == 0.0f) && this.mExpandHeight != f) {
            float max = Math.max(Math.min(1.0f, f / 80.0f), 0.0f);
            float f2 = this.mBlurRatio;
            if (f2 != max) {
                this.mBlurAmin.to("blurRatio", Float.valueOf(max));
            } else {
                this.mBlurAmin.setTo("blurRatio", Float.valueOf(f2));
                this.mControlPanelWindowManager.setBlurRatio(max);
            }
            float f3 = this.mExpandHeight;
            this.mExpandHeight = f;
            if (f3 < 80.0f || f >= 80.0f) {
                if (f3 < 80.0f && f >= 80.0f && !this.mContentShowing) {
                    this.mContent.showContent();
                    this.mContentShowing = true;
                    onControlPanelFinishExpand();
                    cancelHeightChangeAnimator();
                    Log.d("ControllerPanelWindowView", "showContent:" + this.mContent.getHeight());
                }
            } else if (this.mContentShowing) {
                this.mContent.hideContent();
                this.mContentShowing = false;
                AccessibilityUtils.hapticAccessibilityTransitionIfNeeded(getContext(), 191);
                onControlPanelHide();
                Log.d("ControllerPanelWindowView", "hideContent");
            }
        }
    }

    private void createAndStartAnimator(int i, AnimatorListenerAdapter animatorListenerAdapter) {
        cancelHeightChangeAnimator();
        createHeightChangeAnimator(i, animatorListenerAdapter);
        startHeightChangeAnimator();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchNotification(ValueAnimator valueAnimator, int i) {
        if (!this.mNCAnimSwitched && valueAnimator.getAnimatedFraction() > 0.3f) {
            if (i == 0) {
                if (ControlCenter.DEBUG) {
                    Log.d("ControllerPanelWindowView", "switch anim to notification");
                }
                ((StatusBar) Dependency.get(StatusBar.class)).animateExpandNotificationsPanel();
            }
            this.mNCAnimSwitched = true;
        }
        if (!this.mNCBlurSwitched && valueAnimator.getAnimatedFraction() > 0.5f) {
            if (i == 0) {
                if (ControlCenter.DEBUG) {
                    Log.d("ControllerPanelWindowView", "switch blur to notification");
                }
                ((NCSwitchController) Dependency.get(NCSwitchController.class)).switchBlur(false);
            } else if (((float) i) == 80.0f) {
                ((NCSwitchController) Dependency.get(NCSwitchController.class)).switchBlur(true);
            }
            this.mNCBlurSwitched = true;
        }
    }

    private void createHeightChangeAnimator(final int i, AnimatorListenerAdapter animatorListenerAdapter) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mExpandHeight, (float) i);
        this.mHeightChangeAnimator = ofFloat;
        this.mNCBlurSwitched = false;
        this.mNCAnimSwitched = false;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.controlcenter.phone.ControlPanelWindowView.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (ControlPanelWindowView.this.mControlPanelController.isNCSwitching()) {
                    ControlPanelWindowView.this.handleSwitchNotification(valueAnimator, i);
                }
                ControlPanelWindowView.this.updateExpandHeight(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        if (animatorListenerAdapter != null) {
            this.mHeightChangeAnimator.addListener(animatorListenerAdapter);
        }
        this.mHeightChangeAnimator.setDuration(250L);
    }

    private void updateTransHeight(float f) {
        this.mContent.updateTransHeight(f);
    }

    private void endWithCurrentExpandHeight() {
        Log.d("ControllerPanelWindowView", "endWithCurrentExpandHeight");
        if (!this.mCollapsingAnim) {
            this.mControlPanelController.requestNCSwitching(false);
            createAndStartAnimator(0, this.mCollapseListener);
        }
    }

    public void setControlPanelWindowManager(ControlPanelWindowManager controlPanelWindowManager) {
        this.mControlPanelWindowManager = controlPanelWindowManager;
        this.mContent.setControlPanelWindowManager(controlPanelWindowManager);
    }

    public void setControlPanelController(ControlPanelController controlPanelController) {
        this.mControlPanelController = controlPanelController;
    }

    public boolean isCollapsed() {
        return this.mExpandState == 0;
    }

    public boolean isExpanded() {
        return this.mExpandState == 2;
    }

    public void showControlCenterWindow() {
        BoostHelper.getInstance().boostSystemUI(this, true);
        this.mExpandState = 1;
        this.mControlPanelWindowManager.onExpandChange(true);
    }

    public void hideControlCenterWindow() {
        this.mControlPanelWindowManager.onExpandChange(false);
        BoostHelper.getInstance().boostSystemUI(this, false);
        onControlPanelHide();
    }

    public void onControlPanelFinishExpand() {
        this.mExpandState = 2;
        AccessibilityUtils.hapticAccessibilityTransitionIfNeeded(getContext(), 191);
        this.mControlPanelWindowManager.updateNavigationBarSlippery();
    }

    public void onControlPanelHide() {
        this.mExpandState = 0;
        this.mIsGetSelfEvent = false;
        this.mControlPanelWindowManager.updateNavigationBarSlippery();
    }

    public void collapsePanel() {
        collapsePanel(true);
    }

    public void collapsePanel(boolean z) {
        collapsePanel(z, false);
    }

    public void collapsePanel(boolean z, boolean z2) {
        if (!z2) {
            this.mControlPanelController.requestNCSwitching(false);
        }
        if (this.mContent.isDetailShowing()) {
            this.mControlCenterPanel.closeDetail(false);
        }
        if (this.mContent.isEditShowing()) {
            this.mContent.hideEdit();
        }
        if (this.mContent.isControlEditShowing()) {
            this.mContent.hideControlEdit();
        }
        if (z) {
            createAndStartAnimator(0, this.mCollapseListener);
        } else {
            collapsePanelImmediately();
        }
    }

    private void collapsePanelImmediately() {
        updateTransHeight(0.0f);
        updateExpandHeight(0.0f);
        this.mCollapseListener.onAnimationEnd(null);
        onControlPanelHide();
    }

    public void expandPanel() {
        showControlCenterWindow();
        createAndStartAnimator(80, this.mExpandListener);
    }

    private void startHeightChangeAnimator() {
        if (!this.mAnimating) {
            this.mAnimating = true;
            this.mHeightChangeAnimator.start();
        }
    }

    private void cancelHeightChangeAnimator() {
        if (this.mAnimating) {
            this.mHeightChangeAnimator.cancel();
            this.mHeightChangeAnimator.removeAllListeners();
            this.mHeightChangeAnimator.removeAllUpdateListeners();
            this.mAnimating = false;
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        windowInsets.consumeSystemWindowInsets();
        windowInsets.consumeDisplayCutout();
        Log.d("ControllerPanelWindowView", "onApplyWindowInsets: ");
        return windowInsets.consumeDisplayCutout();
    }

    public void onDescendantInvalidated(View view, View view2) {
        super.onDescendantInvalidated(view, view2);
    }

    private boolean isBottomAreaTouchDown(float f) {
        this.mBottomArea.getLocationOnScreen(this.mLocation);
        int[] iArr = this.mLocation;
        return f >= ((float) iArr[1]) && f <= ((float) (iArr[1] + this.mBottomArea.getHeight()));
    }

    public void refreshAllTiles() {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.mControlCenterTileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.refreshAllTiles();
        }
    }
}
