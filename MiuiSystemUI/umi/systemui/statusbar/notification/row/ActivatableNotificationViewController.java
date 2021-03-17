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
    private final AccessibilityManager mAccessibilityManager;
    private DoubleTapHelper mDoubleTapHelper;
    private final ExpandableOutlineViewController mExpandableOutlineViewController;
    private final FalsingManager mFalsingManager;
    private boolean mNeedsDimming;
    private TouchHandler mTouchHandler = new TouchHandler();
    private final ActivatableNotificationView mView;

    public ActivatableNotificationViewController(ActivatableNotificationView activatableNotificationView, ExpandableOutlineViewController expandableOutlineViewController, AccessibilityManager accessibilityManager, FalsingManager falsingManager) {
        this.mView = activatableNotificationView;
        this.mExpandableOutlineViewController = expandableOutlineViewController;
        this.mAccessibilityManager = accessibilityManager;
        this.mFalsingManager = falsingManager;
        activatableNotificationView.setOnActivatedListener(new ActivatableNotificationView.OnActivatedListener() {
            /* class com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnActivatedListener
            public void onActivationReset(ActivatableNotificationView activatableNotificationView) {
            }

            @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnActivatedListener
            public void onActivated(ActivatableNotificationView activatableNotificationView) {
                ActivatableNotificationViewController.this.mFalsingManager.onNotificationActive();
            }
        });
    }

    public void init() {
        this.mExpandableOutlineViewController.init();
        ActivatableNotificationView activatableNotificationView = this.mView;
        $$Lambda$ActivatableNotificationViewController$r3sTjOy8fdG9h_zptjU2waOJUhM r3 = new DoubleTapHelper.ActivationListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$ActivatableNotificationViewController$r3sTjOy8fdG9h_zptjU2waOJUhM */

            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.ActivationListener
            public final void onActiveChanged(boolean z) {
                ActivatableNotificationViewController.this.lambda$init$0$ActivatableNotificationViewController(z);
            }
        };
        ActivatableNotificationView activatableNotificationView2 = this.mView;
        Objects.requireNonNull(activatableNotificationView2);
        $$Lambda$YDw8IXhiUvHyYCObyXXnYJSdUnc r4 = new DoubleTapHelper.DoubleTapListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$YDw8IXhiUvHyYCObyXXnYJSdUnc */

            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.DoubleTapListener
            public final boolean onDoubleTap() {
                return ActivatableNotificationView.this.performClick();
            }
        };
        ActivatableNotificationView activatableNotificationView3 = this.mView;
        Objects.requireNonNull(activatableNotificationView3);
        $$Lambda$ELEe9GisA3PeCbD7mpobFwmaM r5 = new DoubleTapHelper.SlideBackListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$ELEe9GisA3PeCbD7mpobFwmaM */

            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.SlideBackListener
            public final boolean onSlideBack() {
                return ActivatableNotificationView.this.handleSlideBack();
            }
        };
        FalsingManager falsingManager = this.mFalsingManager;
        Objects.requireNonNull(falsingManager);
        this.mDoubleTapHelper = new DoubleTapHelper(activatableNotificationView, r3, r4, r5, new DoubleTapHelper.DoubleTapLogListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$PkPBcaaRR8KHImTlnKW995Xmvx8 */

            @Override // com.android.systemui.statusbar.phone.DoubleTapHelper.DoubleTapLogListener
            public final void onDoubleTapLog(boolean z, float f, float f2) {
                FalsingManager.this.onNotificationDoubleTap(z, f, f2);
            }
        });
        this.mView.setOnTouchListener(this.mTouchHandler);
        this.mView.setTouchHandler(this.mTouchHandler);
        this.mView.setOnDimmedListener(new ActivatableNotificationView.OnDimmedListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$ActivatableNotificationViewController$tnb8yJViiBqHZ1MPl8MWWadMlQ4 */

            @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnDimmedListener
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

    /* access modifiers changed from: package-private */
    public class TouchHandler implements Gefingerpoken, View.OnTouchListener {
        private boolean mBlockNextTouch;

        @Override // com.android.systemui.Gefingerpoken
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

        @Override // com.android.systemui.Gefingerpoken
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
