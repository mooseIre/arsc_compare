package com.android.systemui.statusbar.notification.row;

import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import java.util.Objects;

public class ActivatableNotificationViewController {
    /* access modifiers changed from: private */
    public final AccessibilityManager mAccessibilityManager;
    /* access modifiers changed from: private */
    public DoubleTapHelper mDoubleTapHelper;
    private final ExpandableOutlineViewController mExpandableOutlineViewController;
    /* access modifiers changed from: private */
    public final FalsingManager mFalsingManager;
    /* access modifiers changed from: private */
    public boolean mNeedsDimming;
    private TouchHandler mTouchHandler = new TouchHandler();
    /* access modifiers changed from: private */
    public final ActivatableNotificationView mView;

    public ActivatableNotificationViewController(ActivatableNotificationView activatableNotificationView, ExpandableOutlineViewController expandableOutlineViewController, AccessibilityManager accessibilityManager, FalsingManager falsingManager) {
        this.mView = activatableNotificationView;
        this.mExpandableOutlineViewController = expandableOutlineViewController;
        this.mAccessibilityManager = accessibilityManager;
        this.mFalsingManager = falsingManager;
        activatableNotificationView.setOnActivatedListener(new ActivatableNotificationView.OnActivatedListener() {
            public void onActivationReset(ActivatableNotificationView activatableNotificationView) {
            }

            public void onActivated(ActivatableNotificationView activatableNotificationView) {
                ActivatableNotificationViewController.this.mFalsingManager.onNotificationActive();
            }
        });
    }

    public void init() {
        this.mExpandableOutlineViewController.init();
        ActivatableNotificationView activatableNotificationView = this.mView;
        $$Lambda$ActivatableNotificationViewController$r3sTjOy8fdG9h_zptjU2waOJUhM r3 = new DoubleTapHelper.ActivationListener() {
            public final void onActiveChanged(boolean z) {
                ActivatableNotificationViewController.this.lambda$init$0$ActivatableNotificationViewController(z);
            }
        };
        ActivatableNotificationView activatableNotificationView2 = this.mView;
        Objects.requireNonNull(activatableNotificationView2);
        $$Lambda$YDw8IXhiUvHyYCObyXXnYJSdUnc r4 = new DoubleTapHelper.DoubleTapListener() {
            public final boolean onDoubleTap() {
                return ActivatableNotificationView.this.performClick();
            }
        };
        ActivatableNotificationView activatableNotificationView3 = this.mView;
        Objects.requireNonNull(activatableNotificationView3);
        $$Lambda$ELEe9GisA3PeCbD7mpobFwmaM r5 = new DoubleTapHelper.SlideBackListener() {
            public final boolean onSlideBack() {
                return ActivatableNotificationView.this.handleSlideBack();
            }
        };
        FalsingManager falsingManager = this.mFalsingManager;
        Objects.requireNonNull(falsingManager);
        this.mDoubleTapHelper = new DoubleTapHelper(activatableNotificationView, r3, r4, r5, new DoubleTapHelper.DoubleTapLogListener() {
            public final void onDoubleTapLog(boolean z, float f, float f2) {
                FalsingManager.this.onNotificationDoubleTap(z, f, f2);
            }
        });
        this.mView.setOnTouchListener(this.mTouchHandler);
        this.mView.setTouchHandler(this.mTouchHandler);
        this.mView.setOnDimmedListener(new ActivatableNotificationView.OnDimmedListener() {
            public final void onSetDimmed(boolean z) {
                ActivatableNotificationViewController.this.lambda$init$1$ActivatableNotificationViewController(z);
            }
        });
        this.mView.setAccessibilityManager(this.mAccessibilityManager);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$init$0 */
    public /* synthetic */ void lambda$init$0$ActivatableNotificationViewController(boolean z) {
        if (z) {
            this.mView.makeActive();
            this.mFalsingManager.onNotificationActive();
            return;
        }
        this.mView.makeInactive(true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$init$1 */
    public /* synthetic */ void lambda$init$1$ActivatableNotificationViewController(boolean z) {
        this.mNeedsDimming = z;
    }

    class TouchHandler implements Gefingerpoken, View.OnTouchListener {
        private boolean mBlockNextTouch;

        public boolean onTouchEvent(MotionEvent motionEvent) {
            return false;
        }

        TouchHandler() {
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (this.mBlockNextTouch) {
                this.mBlockNextTouch = false;
                return true;
            } else if (!ActivatableNotificationViewController.this.mNeedsDimming || ActivatableNotificationViewController.this.mAccessibilityManager.isTouchExplorationEnabled() || !ActivatableNotificationViewController.this.mView.isInteractive()) {
                return false;
            } else {
                if (!ActivatableNotificationViewController.this.mNeedsDimming || ActivatableNotificationViewController.this.mView.isDimmed()) {
                    return ActivatableNotificationViewController.this.mDoubleTapHelper.onTouchEvent(motionEvent, ActivatableNotificationViewController.this.mView.getActualHeight());
                }
                return false;
            }
        }

        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            if (!ActivatableNotificationViewController.this.mNeedsDimming || motionEvent.getActionMasked() != 0 || !ActivatableNotificationViewController.this.mView.disallowSingleClick(motionEvent) || ActivatableNotificationViewController.this.mAccessibilityManager.isTouchExplorationEnabled()) {
                return false;
            }
            if (!ActivatableNotificationViewController.this.mView.isActive()) {
                return true;
            }
            if (ActivatableNotificationViewController.this.mDoubleTapHelper.isWithinDoubleTapSlop(motionEvent)) {
                return false;
            }
            this.mBlockNextTouch = true;
            ActivatableNotificationViewController.this.mView.makeInactive(true);
            return true;
        }
    }
}
