package com.android.systemui.controlcenter.qs.tileview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.widget.VisibleFocusedTextView;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.Objects;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class CCQSTileView extends QSTileView {
    private String mAccessibilityClass;
    private ObjectAnimator mBreathAnimator;
    private boolean mClicked;
    private final H mHandler;
    protected QSIconView mIcon;
    private final FrameLayout mIconFrame;
    private ITouchStyle mIconMouseAnim;
    protected TextView mLabel;
    private int mState;
    private boolean mTileState;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public CCQSTileView(Context context, QSIconView qSIconView) {
        this(context, qSIconView, false);
    }

    public CCQSTileView(Context context, QSIconView qSIconView, boolean z) {
        super(context);
        this.mHandler = new H();
        setOrientation(1);
        context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_tile_label_padding_top);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mIconFrame = frameLayout;
        frameLayout.setForegroundGravity(17);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_tile_icon_bg_size);
        addView(this.mIconFrame, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        this.mIcon = qSIconView;
        this.mIconFrame.addView(qSIconView, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mIcon.getIconView(), "alpha", 0.5f);
        this.mBreathAnimator = ofFloat;
        ofFloat.setDuration(400L);
        this.mBreathAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBreathAnimator.setRepeatMode(2);
        this.mBreathAnimator.setRepeatCount(-1);
        setPadding(0, 0, 0, 0);
        setFocusable(true);
        createLabel(z);
        setImportantForAccessibility(2);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void init(final QSTile qSTile) {
        init(new View.OnClickListener(this) {
            /* class com.android.systemui.controlcenter.qs.tileview.CCQSTileView.AnonymousClass1 */

            public void onClick(View view) {
                qSTile.click();
            }
        }, new View.OnClickListener(this) {
            /* class com.android.systemui.controlcenter.qs.tileview.CCQSTileView.AnonymousClass2 */

            public void onClick(View view) {
                qSTile.secondaryClick();
            }
        }, new View.OnLongClickListener(this) {
            /* class com.android.systemui.controlcenter.qs.tileview.CCQSTileView.AnonymousClass3 */

            public boolean onLongClick(View view) {
                qSTile.longClick();
                return true;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void createLabel(boolean z) {
        this.mLabel = createQSStyleLabel(((LinearLayout) this).mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.gravity = 17;
        addView(this.mLabel, layoutParams);
        this.mLabel.setImportantForAccessibility(2);
        if (z) {
            this.mLabel.setAlpha(0.0f);
        }
    }

    public static int getTextHeight(Context context) {
        Paint.FontMetrics fontMetrics = createQSStyleLabel(context).getPaint().getFontMetrics();
        return (int) (fontMetrics.descent - fontMetrics.ascent);
    }

    private static VisibleFocusedTextView createQSStyleLabel(Context context) {
        VisibleFocusedTextView visibleFocusedTextView = new VisibleFocusedTextView(context);
        visibleFocusedTextView.setTextAppearance(C0022R$style.TextAppearance_QSControl_CCTileLabel);
        new LinearLayout.LayoutParams(-1, -2).gravity = 17;
        visibleFocusedTextView.setPadding(0, context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_tile_label_padding_top), 0, 0);
        visibleFocusedTextView.setGravity(17);
        visibleFocusedTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        visibleFocusedTextView.setMarqueeRepeatLimit(2);
        visibleFocusedTextView.setFocusable(true);
        visibleFocusedTextView.setFocusableInTouchMode(true);
        visibleFocusedTextView.setSingleLine(true);
        return visibleFocusedTextView;
    }

    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        this.mIconFrame.setOnClickListener(onClickListener);
        this.mIconFrame.setOnLongClickListener(onLongClickListener);
        this.mIconFrame.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.controlcenter.qs.tileview.CCQSTileView.AnonymousClass4 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean z = true;
                if (CCQSTileView.this.mIconMouseAnim == null) {
                    CCQSTileView cCQSTileView = CCQSTileView.this;
                    ITouchStyle iTouchStyle = Folme.useAt(view).touch();
                    iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
                    cCQSTileView.mIconMouseAnim = iTouchStyle;
                }
                CCQSTileView.this.mIconMouseAnim.onMotionEventEx(view, motionEvent, new AnimConfig[0]);
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    CCQSTileView.this.mHandler.sendEmptyMessageDelayed(2, 80);
                } else if (actionMasked == 1) {
                    if (motionEvent.getPointerId(0) != 0 || !CCQSTileView.this.pointInView(motionEvent.getX(), motionEvent.getY(), 0.0f) || !CCQSTileView.this.mIconFrame.isShown()) {
                        z = false;
                    }
                    if (z) {
                        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).flick();
                    }
                } else if (actionMasked == 3) {
                    CCQSTileView.this.mHandler.removeMessages(2);
                }
                return false;
            }
        });
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View updateAccessibilityOrder(View view) {
        setAccessibilityTraversalAfter(view.getId());
        return this;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        boolean z;
        setClickable(state.state != 0);
        this.mIcon.setIcon(state, state.withAnimation);
        this.mIcon.setContentDescription(state.contentDescription);
        this.mAccessibilityClass = state.expandedAccessibilityClassName;
        if ((state instanceof QSTile.BooleanState) && this.mTileState != (z = ((QSTile.BooleanState) state).value)) {
            this.mClicked = false;
            this.mTileState = z;
        }
        if (state.withAnimation && !this.mBreathAnimator.isStarted()) {
            this.mBreathAnimator.start();
        } else if (!state.withAnimation && this.mBreathAnimator.isStarted()) {
            this.mBreathAnimator.cancel();
        }
        if (!Objects.equals(this.mLabel.getText(), state.label) || this.mState != state.state) {
            if (state.state == 0) {
                state.label = new SpannableStringBuilder().append(state.label, new ForegroundColorSpan(getContext().getColor(C0011R$color.qs_control_tile_text_unavailable_color)), 18);
            }
            this.mState = state.state;
            this.mLabel.setEnabled(true ^ state.disabledByPolicy);
            this.mLabel.setText(state.label);
        }
    }

    public void updateResources() {
        getIcon().updateResources();
        getLabel().setTextAppearance(C0022R$style.TextAppearance_QSControl_CCTileLabel);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public int getDetailY() {
        return getTop() + (getHeight() / 2);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public QSIconView getIcon() {
        return this.mIcon;
    }

    public TextView getLabel() {
        return this.mLabel;
    }

    public void setLabelAlpha(float f) {
        this.mLabel.setAlpha(f);
        labelRequestFocus();
    }

    public void setChildsAlpha(float f) {
        this.mIcon.setAlpha(f);
        this.mLabel.setAlpha(f);
        setVisibility(f == 0.0f ? 4 : 0);
        labelRequestFocus();
    }

    private void labelRequestFocus() {
        if (this.mLabel.getAlpha() == 1.0f && !this.mLabel.isSelected()) {
            this.mLabel.setSelected(true);
        } else if (this.mLabel.getAlpha() < 1.0f && this.mLabel.isSelected()) {
            this.mLabel.setSelected(false);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
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
                accessibilityEvent.setContentDescription(getResources().getString(z ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
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
                accessibilityNodeInfo.setText(getResources().getString(z ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
                accessibilityNodeInfo.setChecked(z);
                accessibilityNodeInfo.setCheckable(true);
            }
        }
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                CCQSTileView.this.handleStateChanged((QSTile.State) message.obj);
            } else if (i == 2) {
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).flick();
            }
        }
    }
}
