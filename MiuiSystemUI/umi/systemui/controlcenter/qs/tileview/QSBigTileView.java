package com.android.systemui.controlcenter.qs.tileview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiTileRecord;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.BtExpandEvent;
import com.miui.systemui.events.DataExpandEvent;
import com.miui.systemui.events.WifiExpandEvent;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class QSBigTileView extends QSTileView {
    private String mActiveString;
    private BigQSTileAnimationController mAnimatorController;
    private ObjectAnimator mBreathAnimator;
    private boolean mClicked;
    private String mClosingString;
    private String mConnectedString;
    private Context mContext;
    private ControlCenterPanelView mControlCenterPanelView;
    private ImageView mExpandIndicator;
    private final H mHandler;
    private QSTileHost mHost;
    private String mInActiveString;
    private int mLayoutDirection;
    private String mOpeningString;
    private ControlPanelController mPanelController;
    private QSTile mQSTile;
    private QSTile.State mQSTileState;
    private int mState;
    private QSIconView mStatusIconView;
    private TextView mStatusView;
    protected String mTag;
    private final QSTile.Callback mTileCallBack;
    private MiuiQSPanel$MiuiTileRecord mTileRecord;
    private boolean mTileState;
    private TextView mTitleView;
    private String mUnavailableString;

    @Override // com.android.systemui.plugins.qs.QSTileView
    public int getDetailY() {
        return 0;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public QSIconView getIcon() {
        return null;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View getIconWithBackground() {
        return null;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View updateAccessibilityOrder(View view) {
        return null;
    }

    public QSBigTileView(Context context) {
        this(context, null);
    }

    public QSBigTileView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public QSBigTileView(Context context, AttributeSet attributeSet, int i) {
        super(context);
        this.mHandler = new H();
        this.mActiveString = "";
        this.mInActiveString = "";
        this.mUnavailableString = "";
        this.mTileCallBack = new QSTileCallback();
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        context.getResources().getColor(C0011R$color.qs_control_tile_icon_disabled_color);
        context.getResources().getColor(C0011R$color.qs_control_tile_icon_unavailable_color);
        this.mConnectedString = this.mContext.getString(C0021R$string.qs_control_big_tile_state_connected);
        this.mActiveString = this.mContext.getString(C0021R$string.qs_control_big_tile_state_opened);
        this.mInActiveString = this.mContext.getString(C0021R$string.qs_control_big_tile_state_closed);
        this.mUnavailableString = this.mContext.getString(C0021R$string.qs_control_big_tile_state_unavailable);
        this.mOpeningString = this.mContext.getString(C0021R$string.qs_control_big_tile_state_opening);
        this.mClosingString = this.mContext.getString(C0021R$string.qs_control_big_tile_state_closing);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mStatusIconView, "alpha", 0.5f);
        this.mBreathAnimator = ofFloat;
        ofFloat.setDuration(400L);
        this.mBreathAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBreathAnimator.setRepeatMode(2);
        this.mBreathAnimator.setRepeatCount(-1);
        setFocusable(true);
        setClipChildren(false);
        setClipToPadding(false);
        setOutlineProvider(new ViewOutlineProvider(this) {
            /* class com.android.systemui.controlcenter.qs.tileview.QSBigTileView.AnonymousClass1 */

            public void getOutline(View view, Outline outline) {
                outline.setAlpha(0.0f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        TextView textView = (TextView) findViewById(C0015R$id.title);
        this.mTitleView = textView;
        textView.setImportantForAccessibility(2);
        this.mStatusView = (TextView) findViewById(C0015R$id.status);
        this.mStatusIconView = (QSIconView) findViewById(C0015R$id.status_icon);
        ControlCenterUtils.createCardFolmeTouchStyle(this);
        ImageView imageView = (ImageView) findViewById(C0015R$id.indicator);
        this.mExpandIndicator = imageView;
        imageView.setContentDescription(this.mContext.getResources().getString(C0021R$string.accessibility_expand_button));
        updateIndicatorTouch();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.orientation;
        if (this.mLayoutDirection != i) {
            this.mLayoutDirection = i;
            updateIndicatorTouch();
        }
    }

    public void updateIndicatorTouch() {
        this.mExpandIndicator.post(new Runnable() {
            /* class com.android.systemui.controlcenter.qs.tileview.QSBigTileView.AnonymousClass2 */

            public void run() {
                int dimensionPixelSize = QSBigTileView.this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_indicator_touch_h);
                int dimensionPixelSize2 = QSBigTileView.this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_indicator_touch_v);
                Rect rect = new Rect();
                QSBigTileView.this.mExpandIndicator.getHitRect(rect);
                rect.top -= dimensionPixelSize2;
                rect.bottom += dimensionPixelSize2;
                rect.left -= dimensionPixelSize;
                rect.right += dimensionPixelSize;
                TouchDelegate touchDelegate = new TouchDelegate(rect, QSBigTileView.this.mExpandIndicator);
                if (View.class.isInstance(QSBigTileView.this.mExpandIndicator.getParent())) {
                    ((View) QSBigTileView.this.mExpandIndicator.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    public void onUserSwitched(int i) {
        this.mQSTile.userSwitch(i);
    }

    public void updateResources() {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_width);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        if (layoutParams.width != dimensionPixelSize) {
            layoutParams.width = dimensionPixelSize;
            setLayoutParams(layoutParams);
        }
        this.mTitleView.setTextAppearance(C0022R$style.TextAppearance_QSControl_BigTileTitle);
        this.mStatusView.setTextAppearance(C0022R$style.TextAppearance_QSControl_BigTileSubTitle);
        this.mExpandIndicator.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.qs_big_tile_expand_indicator));
        updateBackground();
        QSTile.State state = this.mQSTileState;
        if (state != null) {
            handleStateChanged(state);
        }
    }

    public void init(ControlCenterPanelView controlCenterPanelView, String str, int i) {
        this.mControlCenterPanelView = controlCenterPanelView;
        this.mTag = str;
        updateBackground();
        QSTile.State state = this.mQSTileState;
        if (state != null) {
            handleStateChanged(state);
        }
    }

    private void updateBackground() {
        if (this.mTag.equals("cell")) {
            int i = this.mState;
            if (i == 2) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_1);
            } else if (i == 1) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_inactive_1);
            } else if (i == 0) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_unavailable_1);
            }
        } else if (this.mTag.equals("wifi")) {
            int i2 = this.mState;
            if (i2 == 2) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_2);
            } else if (i2 == 1) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_inactive_2);
            } else if (i2 == 0) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_unavailable_2);
            }
        } else if (this.mTag.equals("bt")) {
            int i3 = this.mState;
            if (i3 == 2) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_3);
            } else if (i3 == 1) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_inactive_3);
            } else if (i3 == 0) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_unavailable_3);
            }
        } else if (this.mTag.equals("flashlight")) {
            int i4 = this.mState;
            if (i4 == 2) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_4);
            } else if (i4 == 1) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_inactive_4);
            } else if (i4 == 0) {
                setSmoothBackground(C0013R$drawable.ic_qs_big_tile_bg_unavailable_4);
            }
        }
    }

    private void setSmoothBackground(int i) {
        Drawable smoothRoundDrawable = ControlCenterUtils.getSmoothRoundDrawable(this.mContext, i);
        if (smoothRoundDrawable != null) {
            setBackground(smoothRoundDrawable);
        }
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mTileRecord = new MiuiQSPanel$MiuiTileRecord();
        QSTile createTile = this.mHost.createTile(this.mTag);
        this.mQSTile = createTile;
        createTile.setTileSpec(this.mTag);
        this.mQSTile.userSwitch(KeyguardUpdateMonitor.getCurrentUser());
        this.mTileRecord.callback = this.mTileCallBack;
        init(this.mQSTile);
        this.mQSTile.refreshState();
        MiuiQSPanel$MiuiTileRecord miuiQSPanel$MiuiTileRecord = this.mTileRecord;
        miuiQSPanel$MiuiTileRecord.tile = this.mQSTile;
        miuiQSPanel$MiuiTileRecord.tileView = this;
        miuiQSPanel$MiuiTileRecord.expandIndicator = this.mExpandIndicator;
    }

    public void handleSetListening(boolean z) {
        QSTile qSTile = this.mQSTile;
        qSTile.setListening(qSTile, z);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        QSTile qSTile = this.mQSTile;
        if (qSTile != null) {
            qSTile.addCallback(this.mTileCallBack);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        QSTile qSTile = this.mQSTile;
        if (qSTile != null) {
            qSTile.removeCallbacks();
        }
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        SpannableStringBuilder spannableStringBuilder;
        int i;
        int i2;
        boolean z;
        setClickable(state.state != 0);
        this.mExpandIndicator.setVisibility((this.mPanelController.isSuperPowerMode() || !state.dualTarget) ? 8 : 0);
        this.mStatusIconView.setIcon(state, false);
        this.mStatusIconView.setContentDescription(state.contentDescription);
        String str = state.expandedAccessibilityClassName;
        if ((state instanceof QSTile.BooleanState) && this.mTileState != (z = ((QSTile.BooleanState) state).value)) {
            this.mTileState = z;
            announceForAccessibility(z ? this.mActiveString : this.mInActiveString);
        }
        Log.d("QSBigTileView" + this.mTag, "start state.state:" + state.state + " mState:" + this.mState);
        if (state.withAnimation && !this.mBreathAnimator.isStarted()) {
            this.mBreathAnimator.start();
        } else if (!state.withAnimation) {
            this.mBreathAnimator.cancel();
            this.mStatusIconView.animate().alpha(1.0f).setDuration(300).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
        }
        int i3 = state.state;
        if (i3 == 2) {
            spannableStringBuilder = this.mActiveString;
        } else {
            spannableStringBuilder = i3 == 1 ? this.mInActiveString : this.mUnavailableString;
        }
        this.mState = state.state;
        if (this.mTag.equals("wifi") || this.mTag.equals("bt")) {
            boolean isConnected = this.mTag.equals("wifi") ? ((WifiTile) this.mQSTile).isConnected() : ((BluetoothTile) this.mQSTile).isConnected();
            if (state.isTransient) {
                int i4 = state.state;
                if (i4 == 2) {
                    spannableStringBuilder = this.mOpeningString;
                    this.mState = 2;
                } else if (i4 == 1) {
                    spannableStringBuilder = this.mClosingString;
                    this.mState = 1;
                }
            }
            if (state.state == 2 && isConnected) {
                spannableStringBuilder = this.mConnectedString;
            }
        }
        int i5 = this.mState;
        if (i5 == 0) {
            int color = getContext().getColor(C0011R$color.qs_control_tile_text_unavailable_color);
            state.label = new SpannableStringBuilder().append(state.label, new ForegroundColorSpan(color), 18);
            spannableStringBuilder = new SpannableStringBuilder().append(spannableStringBuilder, new ForegroundColorSpan(color), 18);
        } else {
            TextView textView = this.mTitleView;
            if (i5 == 2) {
                i = this.mContext.getColor(C0011R$color.qs_control_big_tile_title_color);
            } else {
                i = this.mContext.getColor(C0011R$color.qs_control_big_tile_title_color_off);
            }
            textView.setTextColor(i);
            TextView textView2 = this.mStatusView;
            if (this.mState == 2) {
                i2 = this.mContext.getColor(C0011R$color.qs_control_big_tile_sub_title_color);
            } else {
                i2 = this.mContext.getColor(C0011R$color.qs_control_big_tile_sub_title_color_off);
            }
            textView2.setTextColor(i2);
        }
        this.mTitleView.setEnabled(true ^ state.disabledByPolicy);
        this.mTitleView.setText(state.label);
        this.mStatusView.setText(spannableStringBuilder);
        this.mQSTileState = state;
        updateBackground();
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void init(final QSTile qSTile) {
        AnonymousClass3 r0 = new View.OnClickListener(this) {
            /* class com.android.systemui.controlcenter.qs.tileview.QSBigTileView.AnonymousClass3 */

            public void onClick(View view) {
                qSTile.click();
            }
        };
        AnonymousClass4 r1 = new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.qs.tileview.QSBigTileView.AnonymousClass4 */

            public void onClick(View view) {
                Object obj;
                if (QSBigTileView.this.mTag.equals("cell")) {
                    obj = new DataExpandEvent();
                } else if (QSBigTileView.this.mTag.equals("bt")) {
                    obj = new BtExpandEvent();
                } else {
                    obj = QSBigTileView.this.mTag.equals("wifi") ? new WifiExpandEvent() : null;
                }
                if (obj != null) {
                    ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(obj);
                }
                qSTile.secondaryClick();
            }
        };
        AnonymousClass5 r2 = new View.OnLongClickListener(this) {
            /* class com.android.systemui.controlcenter.qs.tileview.QSBigTileView.AnonymousClass5 */

            public boolean onLongClick(View view) {
                qSTile.longClick();
                return true;
            }
        };
        setOnClickListener(r0);
        setOnLongClickListener(r2);
        this.mExpandIndicator.setOnClickListener(r1);
        this.mAnimatorController = new BigQSTileAnimationController(this, this.mExpandIndicator, 0.7f, 1.0f);
        this.mExpandIndicator.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.controlcenter.qs.tileview.QSBigTileView.AnonymousClass6 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    QSBigTileView.this.mClicked = true;
                }
                QSBigTileView.this.mAnimatorController.onTouchEvent(motionEvent);
                if (actionMasked == 1) {
                    view.callOnClick();
                }
                if (actionMasked == 3 || actionMasked == 1) {
                    QSBigTileView.this.mClicked = false;
                }
                return true;
            }
        });
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        AnimConfig animConfig = new AnimConfig();
        ITouchStyle iTouchStyle = Folme.useAt(this).touch();
        iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
        iTouchStyle.onMotionEventEx(this, motionEvent, animConfig);
        return super.onTouchEvent(motionEvent);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return super.dispatchTouchEvent(motionEvent);
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                QSBigTileView.this.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }

    public static class BigQSTileAnimationController {
        protected int[] location = new int[2];
        private View mBigQSTile;
        private AnimConfig mBigQSTileAnimConfig;
        private View mExpandIndicator;
        private IStateStyle mExpandIndicatorAnim;
        private AnimConfig mExpandIndicatorAnimConfig;
        private AnimState mExpandIndicatorClickState;
        private float mInitAlpha;

        public BigQSTileAnimationController(View view, View view2, float f, float f2) {
            this.mBigQSTile = view;
            this.mExpandIndicator = view2;
            this.mInitAlpha = f;
            this.mExpandIndicatorAnim = Folme.useAt(view2).state();
            AnimState animState = new AnimState("clicked state");
            this.mExpandIndicatorClickState = animState;
            animState.add(ViewProperty.ALPHA, f2, new long[0]);
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
            this.mExpandIndicatorAnimConfig = animConfig;
            Folme.clean(this.mBigQSTile);
            AnimConfig animConfig2 = new AnimConfig();
            animConfig2.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
            this.mBigQSTileAnimConfig = animConfig2;
        }

        public void onTouchEvent(MotionEvent motionEvent) {
            Folme.clean(this.mBigQSTile);
            IStateStyle state = Folme.useAt(this.mBigQSTile).state();
            int action = motionEvent.getAction();
            if (action != 0) {
                if (action != 1) {
                    if (action != 2) {
                        if (action != 3) {
                            return;
                        }
                    }
                }
                this.mExpandIndicator.setAlpha(this.mInitAlpha);
                AnimState animState = new AnimState("qs big tile up");
                animState.add(ViewProperty.ROTATION_X, 0, new long[0]);
                animState.add(ViewProperty.ROTATION_Y, 0, new long[0]);
                animState.add(ViewProperty.TRANSLATION_Z, 0, new long[0]);
                state.to(animState, this.mBigQSTileAnimConfig);
                return;
            }
            if (motionEvent.getAction() == 0) {
                this.mExpandIndicatorAnim.cancel();
                this.mExpandIndicatorAnim.to(this.mExpandIndicatorClickState, this.mExpandIndicatorAnimConfig);
            }
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            this.mBigQSTile.getLocationOnScreen(this.location);
            float width = ((float) this.location[0]) + (((float) this.mBigQSTile.getWidth()) * 0.5f);
            float height = ((float) this.location[1]) + (((float) this.mBigQSTile.getHeight()) * 0.5f);
            if (rawX > width + 213.0f) {
                rawX = (((rawX - width) - 213.0f) * 0.05f) + width + 213.0f;
            } else {
                float f = width - 213.0f;
                if (rawX < f) {
                    rawX = f - ((f - rawX) * 0.05f);
                }
            }
            if (rawY > height + 101.0f) {
                rawY = (((rawY - height) - 101.0f) * 0.05f) + height + 101.0f;
            } else {
                float f2 = height - 101.0f;
                if (rawY < f2) {
                    rawY = f2 - ((f2 - rawY) * 0.05f);
                }
            }
            float f3 = rawX - width;
            float f4 = rawY - height;
            float f5 = (f4 / 101.0f) * -9.0f;
            float f6 = (f3 / 213.0f) * 5.0f;
            float sqrt = (float) ((Math.sqrt((double) ((f3 * f3) + (f4 * f4))) / 211.0d) * 25.0d);
            if (motionEvent.getAction() == 0) {
                AnimState animState2 = new AnimState("qs big tile down");
                animState2.add(ViewProperty.ROTATION_X, f5, new long[0]);
                animState2.add(ViewProperty.ROTATION_Y, f6, new long[0]);
                animState2.add(ViewProperty.TRANSLATION_Z, sqrt, new long[0]);
                state.to(animState2, this.mBigQSTileAnimConfig);
                return;
            }
            state.clean();
            this.mBigQSTile.setRotationX(f5);
            this.mBigQSTile.setRotationY(f6);
            this.mBigQSTile.setTranslationZ(sqrt);
        }
    }

    private class QSTileCallback implements QSTile.Callback {
        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onAnnouncementRequested(CharSequence charSequence) {
        }

        private QSTileCallback() {
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onStateChanged(QSTile.State state) {
            QSBigTileView.this.onStateChanged(state);
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onShowDetail(boolean z) {
            QSBigTileView.this.mControlCenterPanelView.showDetail(z, QSBigTileView.this.mTileRecord);
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onToggleStateChanged(boolean z) {
            QSBigTileView.this.mControlCenterPanelView.fireToggleStateChanged(z);
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onScanStateChanged(boolean z) {
            QSBigTileView.this.mControlCenterPanelView.fireScanStateChanged(z);
        }
    }
}
