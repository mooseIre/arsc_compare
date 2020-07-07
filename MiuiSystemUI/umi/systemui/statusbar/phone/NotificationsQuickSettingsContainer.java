package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import com.android.systemui.SystemUI;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;

public class NotificationsQuickSettingsContainer extends FrameLayout implements ViewStub.OnInflateListener, FragmentHostManager.FragmentListener, OnHeadsUpChangedListener {
    private boolean mHeadsUp;
    private HeadsUpManager mHeadsUpManager;
    private boolean mInflated;
    private View mKeyguardStatusBar;
    private QS mQS;
    private boolean mQsExpanded;
    private FrameLayout mQsFrame;
    private View mStackScroller;
    private View mUserSwitcher;

    public void onFragmentViewDestroyed(String str, Fragment fragment) {
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public void onHeadsUpPinnedModeChanged(boolean z) {
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public NotificationsQuickSettingsContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setClipChildren(false);
        this.mQsFrame = (FrameLayout) findViewById(R.id.qs_frame);
        this.mStackScroller = findViewById(R.id.notification_stack_scroller);
        this.mKeyguardStatusBar = findViewById(R.id.keyguard_header);
        ViewStub viewStub = (ViewStub) findViewById(R.id.keyguard_user_switcher);
        viewStub.setOnInflateListener(this);
        this.mUserSwitcher = viewStub;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        FragmentHostManager.get(this).addTagListener(QS.TAG, this);
        this.mHeadsUpManager = ((StatusBar) SystemUI.getComponent(getContext(), StatusBar.class)).mHeadsUpManager;
        this.mHeadsUpManager.addListener(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        this.mHeadsUpManager.removeListener(this);
        FragmentHostManager.get(this).removeTagListener(QS.TAG, this);
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        boolean z = true;
        boolean z2 = this.mInflated && this.mUserSwitcher.getVisibility() == 0;
        if (this.mKeyguardStatusBar.getVisibility() != 0) {
            z = false;
        }
        boolean z3 = this.mHeadsUp;
        View view2 = z3 ? this.mStackScroller : this.mQsFrame;
        View view3 = !z3 ? this.mStackScroller : this.mQsFrame;
        if (view == this.mQsFrame) {
            if (z2 && z) {
                view3 = this.mUserSwitcher;
            } else if (z) {
                view3 = this.mKeyguardStatusBar;
            } else if (z2) {
                view3 = this.mUserSwitcher;
            }
            return super.drawChild(canvas, view3, j);
        } else if (view == this.mStackScroller) {
            if (z2 && z) {
                view2 = this.mKeyguardStatusBar;
            } else if (z || z2) {
                view2 = view3;
            }
            return super.drawChild(canvas, view2, j);
        } else if (view == this.mUserSwitcher) {
            if (!z2 || !z) {
                view3 = view2;
            }
            return super.drawChild(canvas, view3, j);
        } else if (view == this.mKeyguardStatusBar) {
            return super.drawChild(canvas, view2, j);
        } else {
            return super.drawChild(canvas, view, j);
        }
    }

    public void onInflate(ViewStub viewStub, View view) {
        if (viewStub == this.mUserSwitcher) {
            this.mUserSwitcher = view;
            this.mInflated = true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mQS.isShowingDetail()) {
            return super.onTouchEvent(motionEvent);
        }
        if (motionEvent.getAction() == 1) {
            this.mQS.closeDetail();
        }
        return true;
    }

    public void onFragmentViewCreated(String str, Fragment fragment) {
        this.mQS = (QS) fragment;
        this.mQS.setContainer(this);
        this.mQS.setDetailAnimatedViews(this.mStackScroller);
    }

    public void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            invalidate();
        }
    }

    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        boolean z2 = this.mHeadsUpManager.getAllEntries().size() != 0;
        if (this.mHeadsUp != z2) {
            this.mHeadsUp = z2;
            invalidate();
        }
    }
}
