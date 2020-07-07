package com.android.systemui.qs.tileimpl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.util.ViewAnimUtils;
import miui.animation.ITouchStyle;
import miui.animation.base.AnimConfig;

public class QSTileBaseView extends QSTileView {
    private String mAccessibilityClass;
    private ObjectAnimator mBreathAnimator;
    private boolean mClicked;
    private final H mHandler = new H();
    protected QSIconView mIcon;
    /* access modifiers changed from: private */
    public final FrameLayout mIconFrame;
    /* access modifiers changed from: private */
    public ITouchStyle mIconMouseAnim;
    private boolean mTileState;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public QSTileBaseView(Context context, QSIconView qSIconView, boolean z) {
        super(context);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mIconFrame = frameLayout;
        frameLayout.setForegroundGravity(17);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.qs_tile_icon_bg_size);
        addView(this.mIconFrame, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        this.mIcon = qSIconView;
        this.mIconFrame.addView(qSIconView);
        setImportantForAccessibility(2);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mIcon.getIconView(), "alpha", new float[]{0.5f});
        this.mBreathAnimator = ofFloat;
        ofFloat.setDuration(400);
        this.mBreathAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBreathAnimator.setRepeatMode(2);
        this.mBreathAnimator.setRepeatCount(-1);
        setPadding(0, 0, 0, 0);
        setClipChildren(false);
        setClipToPadding(false);
        setFocusable(true);
    }

    public void init(final QSTile qSTile) {
        init(new View.OnClickListener() {
            public void onClick(View view) {
                qSTile.click();
            }
        }, new View.OnClickListener() {
            public void onClick(View view) {
                qSTile.secondaryClick();
            }
        }, new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                qSTile.longClick();
                return true;
            }
        });
    }

    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        this.mIconFrame.setOnClickListener(onClickListener);
        this.mIconFrame.setOnLongClickListener(onLongClickListener);
        this.mIconFrame.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Class cls = HapticFeedBackImpl.class;
                if (QSTileBaseView.this.mIconMouseAnim == null) {
                    ITouchStyle unused = QSTileBaseView.this.mIconMouseAnim = ViewAnimUtils.createMouseAnim(view);
                }
                QSTileBaseView.this.mIconMouseAnim.onMotionEventEx(view, motionEvent, new AnimConfig[0]);
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked != 0) {
                    boolean z = true;
                    if (actionMasked == 1) {
                        if (motionEvent.getPointerId(0) != 0 || !QSTileBaseView.this.pointInView(motionEvent.getX(), motionEvent.getY(), 0.0f) || !QSTileBaseView.this.mIconFrame.isShown()) {
                            z = false;
                        }
                        if (z) {
                            ((HapticFeedBackImpl) Dependency.get(cls)).flick();
                        }
                    }
                } else {
                    ((HapticFeedBackImpl) Dependency.get(cls)).flick();
                }
                return false;
            }
        });
    }

    public View updateAccessibilityOrder(View view) {
        setAccessibilityTraversalAfter(view.getId());
        return this;
    }

    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        boolean z = true;
        setClickable(state.state != 0);
        this.mIcon.setIcon(state);
        this.mIcon.setContentDescription(state.contentDescription);
        this.mAccessibilityClass = state.expandedAccessibilityClassName;
        if (state instanceof QSTile.BooleanState) {
            boolean z2 = ((QSTile.BooleanState) state).value;
            if (this.mTileState != z2) {
                this.mClicked = false;
                this.mTileState = z2;
            }
        } else {
            this.mClicked = false;
            if (state.state != 2) {
                z = false;
            }
            this.mTileState = z;
        }
        if (state.withAnimation && !this.mBreathAnimator.isStarted()) {
            this.mBreathAnimator.start();
        } else if (!state.withAnimation) {
            this.mBreathAnimator.cancel();
            this.mIcon.getIconView().animate().alpha(1.0f).setDuration(300).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
        }
    }

    public int getDetailY() {
        return getTop() + (getHeight() / 2);
    }

    public QSIconView getIcon() {
        return this.mIcon;
    }

    public View getIconWithBackground() {
        return this.mIconFrame;
    }

    public boolean performClick() {
        this.mClicked = true;
        return super.performClick();
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (!TextUtils.isEmpty(this.mAccessibilityClass)) {
            accessibilityEvent.setClassName(this.mAccessibilityClass);
            if (Switch.class.getName().equals(this.mAccessibilityClass)) {
                boolean z = this.mClicked ? !this.mTileState : this.mTileState;
                accessibilityEvent.setContentDescription(getResources().getString(z ? R.string.switch_bar_on : R.string.switch_bar_off));
                accessibilityEvent.setChecked(z);
            }
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (!TextUtils.isEmpty(this.mAccessibilityClass)) {
            if (!this.mAccessibilityClass.equals(Button.class.getName())) {
                accessibilityNodeInfo.setClassName(this.mAccessibilityClass);
            }
            if (Switch.class.getName().equals(this.mAccessibilityClass)) {
                boolean z = this.mClicked ? !this.mTileState : this.mTileState;
                accessibilityNodeInfo.setText(getResources().getString(z ? R.string.switch_bar_on : R.string.switch_bar_off));
                accessibilityNodeInfo.setChecked(z);
                accessibilityNodeInfo.setCheckable(true);
            }
        }
    }

    private class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                QSTileBaseView.this.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }
}
