package com.android.systemui.miui.controlcenter.tileImpl;

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
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.FocusedTextView;
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
    /* access modifiers changed from: private */
    public final FrameLayout mIconFrame;
    /* access modifiers changed from: private */
    public ITouchStyle mIconMouseAnim;
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
        context.getResources().getDimensionPixelSize(R.dimen.qs_control_tile_label_padding_top);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mIconFrame = frameLayout;
        frameLayout.setForegroundGravity(17);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.qs_control_tile_icon_bg_size);
        addView(this.mIconFrame, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        this.mIcon = qSIconView;
        this.mIconFrame.addView(qSIconView, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mIcon.getIconView(), "alpha", new float[]{0.5f});
        this.mBreathAnimator = ofFloat;
        ofFloat.setDuration(400);
        this.mBreathAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBreathAnimator.setRepeatMode(2);
        this.mBreathAnimator.setRepeatCount(-1);
        setPadding(0, 0, 0, 0);
        setFocusable(true);
        createLabel(z);
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

    /* access modifiers changed from: protected */
    public void createLabel(boolean z) {
        this.mLabel = createQSStyleLabel(this.mContext);
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

    private static FocusedTextView createQSStyleLabel(Context context) {
        FocusedTextView focusedTextView = new FocusedTextView(context);
        focusedTextView.setTextAppearance(R.style.TextAppearance_QSControl_CCTileLabel);
        new LinearLayout.LayoutParams(-1, -2).gravity = 17;
        focusedTextView.setPadding(0, context.getResources().getDimensionPixelSize(R.dimen.qs_control_tile_label_padding_top), 0, 0);
        focusedTextView.setGravity(17);
        focusedTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        focusedTextView.setMarqueeRepeatLimit(-1);
        focusedTextView.setSelected(true);
        focusedTextView.setFocusableInTouchMode(true);
        focusedTextView.setSingleLine(true);
        return focusedTextView;
    }

    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        this.mIconFrame.setOnClickListener(onClickListener);
        this.mIconFrame.setOnLongClickListener(onLongClickListener);
        this.mIconFrame.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Class cls = HapticFeedBackImpl.class;
                boolean z = true;
                if (CCQSTileView.this.mIconMouseAnim == null) {
                    CCQSTileView cCQSTileView = CCQSTileView.this;
                    ITouchStyle iTouchStyle = Folme.useAt(view).touch();
                    iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
                    ITouchStyle unused = cCQSTileView.mIconMouseAnim = iTouchStyle;
                }
                CCQSTileView.this.mIconMouseAnim.onMotionEventEx(view, motionEvent, new AnimConfig[0]);
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    ((HapticFeedBackImpl) Dependency.get(cls)).flick();
                } else if (actionMasked == 1) {
                    if (motionEvent.getPointerId(0) != 0 || !CCQSTileView.this.pointInView(motionEvent.getX(), motionEvent.getY(), 0.0f) || !CCQSTileView.this.mIconFrame.isShown()) {
                        z = false;
                    }
                    if (z) {
                        ((HapticFeedBackImpl) Dependency.get(cls)).flick();
                    }
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
        boolean z;
        setClickable(state.state != 0);
        this.mIcon.setIcon(state);
        this.mIcon.setContentDescription(state.contentDescription);
        this.mAccessibilityClass = state.expandedAccessibilityClassName;
        if ((state instanceof QSTile.BooleanState) && this.mTileState != (z = ((QSTile.BooleanState) state).value)) {
            this.mClicked = false;
            this.mTileState = z;
        }
        if (state.withAnimation && !this.mBreathAnimator.isStarted()) {
            this.mBreathAnimator.start();
        } else if (!state.withAnimation) {
            this.mBreathAnimator.cancel();
            this.mIcon.getIconView().animate().alpha(1.0f).setDuration(300).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
        }
        if (!Objects.equals(this.mLabel.getText(), state.label) || this.mState != state.state) {
            if (state.state == 0) {
                state.label = new SpannableStringBuilder().append(state.label, new ForegroundColorSpan(getContext().getColor(R.color.qs_control_tile_text_unavailable_color)), 18);
            }
            this.mState = state.state;
            this.mLabel.setEnabled(true ^ state.disabledByPolicy);
            this.mLabel.setText(state.label);
        }
    }

    public void updateResources() {
        getIcon().updateResources();
        getLabel().setTextAppearance(R.style.TextAppearance_QSControl_CCTileLabel);
    }

    public int getDetailY() {
        return getTop() + (getHeight() / 2);
    }

    public QSIconView getIcon() {
        return this.mIcon;
    }

    public TextView getLabel() {
        return this.mLabel;
    }

    public void setChildsAlpha(float f) {
        this.mIcon.setAlpha(f);
        this.mLabel.setAlpha(f);
        setVisibility(f == 0.0f ? 4 : 0);
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
                CCQSTileView.this.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }
}
