package com.android.systemui.miui.controlcenter;

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
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.util.Utils;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class QSBigTileView extends QSTileView {
    private String mAccessibilityClass;
    private String mActiveString;
    /* access modifiers changed from: private */
    public BigQSTileAnimationController mAnimatorController;
    private ObjectAnimator mBreathAnimator;
    /* access modifiers changed from: private */
    public boolean mClicked;
    private String mClosingString;
    private String mConnectedString;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public ImageView mExpandIndicator;
    private final H mHandler;
    private QSControlTileHost mHost;
    private int mIconColorDisabled;
    private int mIconColorUnavailable;
    private String mInActiveString;
    private int mLayoutDirection;
    private String mOpeningString;
    private ControlPanelController mPanelController;
    /* access modifiers changed from: private */
    public QSControlCenterPanel mQSControlCenterPanel;
    private QSTile mQSTile;
    private QSTile.State mQSTileState;
    private int mState;
    private QSIconView mStatusIconView;
    private TextView mStatusView;
    protected String mTag;
    /* access modifiers changed from: private */
    public QSPanel.TileRecord mTileRecord;
    private boolean mTileState;
    private TextView mTitleView;
    private String mUnavailableString;

    public int getDetailY() {
        return 0;
    }

    public QSIconView getIcon() {
        return null;
    }

    public View getIconWithBackground() {
        return null;
    }

    public View updateAccessibilityOrder(View view) {
        return null;
    }

    public QSBigTileView(Context context) {
        this(context, (AttributeSet) null);
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
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        this.mIconColorDisabled = context.getResources().getColor(R.color.qs_control_tile_icon_disabled_color);
        this.mIconColorUnavailable = context.getResources().getColor(R.color.qs_control_tile_icon_unavailable_color);
        this.mConnectedString = this.mContext.getString(R.string.qs_control_big_tile_state_connected);
        this.mActiveString = this.mContext.getString(R.string.qs_control_big_tile_state_opened);
        this.mInActiveString = this.mContext.getString(R.string.qs_control_big_tile_state_closed);
        this.mUnavailableString = this.mContext.getString(R.string.qs_control_big_tile_state_unavailable);
        this.mOpeningString = this.mContext.getString(R.string.qs_control_big_tile_state_opening);
        this.mClosingString = this.mContext.getString(R.string.qs_control_big_tile_state_closing);
        this.mBreathAnimator = ObjectAnimator.ofFloat(this.mStatusIconView, "alpha", new float[]{0.5f});
        this.mBreathAnimator.setDuration(400);
        this.mBreathAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBreathAnimator.setRepeatMode(2);
        this.mBreathAnimator.setRepeatCount(-1);
        setFocusable(true);
        setClipChildren(false);
        setClipToPadding(false);
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setAlpha(0.0f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleView = (TextView) findViewById(R.id.title);
        this.mTitleView.setImportantForAccessibility(2);
        this.mStatusView = (TextView) findViewById(R.id.status);
        this.mStatusIconView = (QSIconView) findViewById(R.id.status_icon);
        Utils.createCardFolmeTouchStyle(this);
        this.mExpandIndicator = (ImageView) findViewById(R.id.indicator);
        this.mExpandIndicator.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_expand_button));
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

    private void updateIndicatorTouch() {
        this.mExpandIndicator.post(new Runnable() {
            public void run() {
                int dimensionPixelSize = QSBigTileView.this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_big_tile_indicator_touch_h);
                int dimensionPixelSize2 = QSBigTileView.this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_big_tile_indicator_touch_v);
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
        this.mTitleView.setTextAppearance(R.style.TextAppearance_QSControl_BigTileTitle);
        this.mStatusView.setTextAppearance(R.style.TextAppearance_QSControl_BigTileSubTitle);
        this.mExpandIndicator.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_big_tile_expand_indicator));
        updateBackground();
        QSTile.State state = this.mQSTileState;
        if (state != null) {
            handleStateChanged(state);
        }
    }

    public void init(QSControlCenterPanel qSControlCenterPanel, String str, int i) {
        this.mQSControlCenterPanel = qSControlCenterPanel;
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
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_1);
            } else if (i == 1) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_inactive_1);
            } else if (i == 0) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_unavailable_1);
            }
        } else if (this.mTag.equals("wifi")) {
            int i2 = this.mState;
            if (i2 == 2) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_2);
            } else if (i2 == 1) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_inactive_2);
            } else if (i2 == 0) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_unavailable_2);
            }
        } else if (this.mTag.equals("bt")) {
            int i3 = this.mState;
            if (i3 == 2) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_3);
            } else if (i3 == 1) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_inactive_3);
            } else if (i3 == 0) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_unavailable_3);
            }
        } else if (this.mTag.equals("flashlight")) {
            int i4 = this.mState;
            if (i4 == 2) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_4);
            } else if (i4 == 1) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_inactive_4);
            } else if (i4 == 0) {
                setSmoothBackground(R.drawable.ic_qs_big_tile_bg_unavailable_4);
            }
        }
    }

    private void setSmoothBackground(int i) {
        Drawable smoothRoundDrawable = Utils.getSmoothRoundDrawable(this.mContext, i);
        if (smoothRoundDrawable != null) {
            setBackground(smoothRoundDrawable);
        }
    }

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mHost = qSControlTileHost;
        this.mTileRecord = new QSPanel.TileRecord();
        this.mQSTile = this.mHost.createTile(this.mTag);
        this.mQSTile.userSwitch(KeyguardUpdateMonitor.getCurrentUser());
        this.mTileRecord.callback = new QSTile.Callback() {
            public void onAnnouncementRequested(CharSequence charSequence) {
            }

            public void onShowEdit(boolean z) {
            }

            public void onStateChanged(QSTile.State state) {
                QSBigTileView.this.onStateChanged(state);
            }

            public void onShowDetail(boolean z) {
                QSBigTileView.this.mQSControlCenterPanel.showDetail(z, QSBigTileView.this.mTileRecord);
            }

            public void onToggleStateChanged(boolean z) {
                QSBigTileView.this.mQSControlCenterPanel.fireToggleStateChanged(z);
            }

            public void onScanStateChanged(boolean z) {
                QSBigTileView.this.mQSControlCenterPanel.fireScanStateChanged(z);
            }
        };
        this.mQSTile.addCallback(this.mTileRecord.callback);
        init(this.mQSTile);
        this.mQSTile.refreshState();
        QSPanel.TileRecord tileRecord = this.mTileRecord;
        tileRecord.tile = this.mQSTile;
        tileRecord.tileView = this;
        tileRecord.expandIndicator = this.mExpandIndicator;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0068  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleSetListening(boolean r7) {
        /*
            r6 = this;
            java.lang.String r0 = r6.mTag
            int r1 = r0.hashCode()
            r2 = -1183073498(0xffffffffb97bbb26, float:-2.4006944E-4)
            r3 = 3
            r4 = 2
            r5 = 1
            if (r1 == r2) goto L_0x003c
            r2 = 3154(0xc52, float:4.42E-42)
            if (r1 == r2) goto L_0x0032
            r2 = 3049826(0x2e8962, float:4.273716E-39)
            if (r1 == r2) goto L_0x0028
            r2 = 3649301(0x37af15, float:5.11376E-39)
            if (r1 == r2) goto L_0x001d
            goto L_0x0046
        L_0x001d:
            java.lang.String r1 = "wifi"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0046
            r0 = r5
            goto L_0x0047
        L_0x0028:
            java.lang.String r1 = "cell"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0046
            r0 = 0
            goto L_0x0047
        L_0x0032:
            java.lang.String r1 = "bt"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0046
            r0 = r4
            goto L_0x0047
        L_0x003c:
            java.lang.String r1 = "flashlight"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x0046
            r0 = r3
            goto L_0x0047
        L_0x0046:
            r0 = -1
        L_0x0047:
            if (r0 == 0) goto L_0x0068
            if (r0 == r5) goto L_0x0060
            if (r0 == r4) goto L_0x0058
            if (r0 == r3) goto L_0x0050
            goto L_0x006f
        L_0x0050:
            com.android.systemui.plugins.qs.QSTile r6 = r6.mQSTile
            com.android.systemui.qs.tiles.FlashlightTile r6 = (com.android.systemui.qs.tiles.FlashlightTile) r6
            r6.handleSetListening(r7)
            goto L_0x006f
        L_0x0058:
            com.android.systemui.plugins.qs.QSTile r6 = r6.mQSTile
            com.android.systemui.qs.tiles.BluetoothTile r6 = (com.android.systemui.qs.tiles.BluetoothTile) r6
            r6.handleSetListening(r7)
            goto L_0x006f
        L_0x0060:
            com.android.systemui.plugins.qs.QSTile r6 = r6.mQSTile
            com.android.systemui.qs.tiles.WifiTile r6 = (com.android.systemui.qs.tiles.WifiTile) r6
            r6.handleSetListening(r7)
            goto L_0x006f
        L_0x0068:
            com.android.systemui.plugins.qs.QSTile r6 = r6.mQSTile
            com.android.systemui.qs.tiles.CellularTile r6 = (com.android.systemui.qs.tiles.CellularTile) r6
            r6.handleSetListening(r7)
        L_0x006f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.QSBigTileView.handleSetListening(boolean):void");
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        CharSequence charSequence;
        int i;
        int i2;
        boolean z;
        int i3 = 0;
        setClickable(state.state != 0);
        ImageView imageView = this.mExpandIndicator;
        if (this.mPanelController.isSuperPowerMode() || !state.dualTarget) {
            i3 = 8;
        }
        imageView.setVisibility(i3);
        this.mStatusIconView.setIcon(state);
        this.mStatusIconView.setContentDescription(state.contentDescription);
        this.mAccessibilityClass = state.expandedAccessibilityClassName;
        if ((state instanceof QSTile.BooleanState) && this.mTileState != (z = ((QSTile.BooleanState) state).value)) {
            this.mTileState = z;
        }
        Log.d("QSBigTileView" + this.mTag, "start state.state:" + state.state + " mState:" + this.mState);
        if (state.withAnimation && !this.mBreathAnimator.isStarted()) {
            this.mBreathAnimator.start();
        } else if (!state.withAnimation) {
            this.mBreathAnimator.cancel();
            this.mStatusIconView.animate().alpha(1.0f).setDuration(300).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
        }
        int i4 = state.state;
        if (i4 == 2) {
            charSequence = this.mActiveString;
        } else {
            charSequence = i4 == 1 ? this.mInActiveString : this.mUnavailableString;
        }
        this.mState = state.state;
        if (this.mTag.equals("wifi") || this.mTag.equals("bt")) {
            Boolean targetEnable = this.mTag.equals("wifi") ? ((WifiTile) this.mQSTile).getTargetEnable() : ((BluetoothTile) this.mQSTile).getTargetEnable();
            boolean isConnected = this.mTag.equals("wifi") ? ((WifiTile) this.mQSTile).isConnected() : ((BluetoothTile) this.mQSTile).isConnected();
            if (targetEnable != null) {
                if (targetEnable.booleanValue() && state.state != 2) {
                    charSequence = this.mOpeningString;
                    this.mState = targetEnable.booleanValue() ? 2 : 1;
                } else if (!targetEnable.booleanValue() && state.state != 1) {
                    charSequence = this.mClosingString;
                    this.mState = targetEnable.booleanValue() ? 2 : 1;
                }
            }
            if (state.state == 2 && isConnected) {
                charSequence = this.mConnectedString;
            }
        }
        int i5 = this.mState;
        if (i5 == 0) {
            int color = getContext().getColor(R.color.qs_control_tile_text_unavailable_color);
            state.label = new SpannableStringBuilder().append(state.label, new ForegroundColorSpan(color), 18);
            charSequence = new SpannableStringBuilder().append(charSequence, new ForegroundColorSpan(color), 18);
        } else {
            TextView textView = this.mTitleView;
            if (i5 == 2) {
                i = this.mContext.getColor(R.color.qs_control_big_tile_title_color);
            } else {
                i = this.mContext.getColor(R.color.qs_control_big_tile_title_color_off);
            }
            textView.setTextColor(i);
            TextView textView2 = this.mStatusView;
            if (this.mState == 2) {
                i2 = this.mContext.getColor(R.color.qs_control_big_tile_sub_title_color);
            } else {
                i2 = this.mContext.getColor(R.color.qs_control_big_tile_sub_title_color_off);
            }
            textView2.setTextColor(i2);
        }
        this.mTitleView.setEnabled(true ^ state.disabledByPolicy);
        this.mTitleView.setText(state.label);
        this.mStatusView.setText(charSequence);
        this.mQSTileState = state;
        updateBackground();
    }

    public void init(final QSTile qSTile) {
        AnonymousClass4 r0 = new View.OnClickListener() {
            public void onClick(View view) {
                qSTile.click();
            }
        };
        AnonymousClass5 r1 = new View.OnClickListener() {
            public void onClick(View view) {
                String str;
                if (QSBigTileView.this.mTag.equals("cell")) {
                    str = "data_expand";
                } else if (QSBigTileView.this.mTag.equals("bt")) {
                    str = "bt_expand";
                } else {
                    str = QSBigTileView.this.mTag.equals("wifi") ? "wifi_expand" : null;
                }
                if (!TextUtils.isEmpty(str)) {
                    ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(str);
                }
                qSTile.secondaryClick();
            }
        };
        AnonymousClass6 r2 = new View.OnLongClickListener() {
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
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    boolean unused = QSBigTileView.this.mClicked = true;
                }
                QSBigTileView.this.mAnimatorController.onTouchEvent(motionEvent);
                if (actionMasked == 1) {
                    view.callOnClick();
                }
                if (actionMasked == 3 || actionMasked == 1) {
                    boolean unused2 = QSBigTileView.this.mClicked = false;
                }
                return true;
            }
        });
    }

    public boolean isClicked() {
        return this.mClicked;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        ITouchStyle iTouchStyle = Folme.useAt(this).touch();
        iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
        iTouchStyle.onMotionEventEx(this, motionEvent, new AnimConfig[0]);
        return super.onTouchEvent(motionEvent);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return super.dispatchTouchEvent(motionEvent);
    }

    private class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                QSBigTileView.this.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }

    private class BigQSTileAnimationController {
        protected int[] location = new int[2];
        private View mBigQSTile;
        private IStateStyle mBigQSTileAnim;
        private AnimConfig mBigQSTileDownAnimConfig;
        private AnimConfig mBigQSTileUpAnimConfig;
        private View mExpandIndicator;
        private IStateStyle mExpandIndicatorAnim;
        private AnimConfig mExpandIndicatorAnimConfig;
        private AnimState mExpandIndicatorClickState;
        private AnimState mExpandIndicatorReleaseState;

        public BigQSTileAnimationController(View view, View view2, float f, float f2) {
            this.mBigQSTile = view;
            this.mExpandIndicator = view2;
            this.mExpandIndicatorAnim = Folme.useAt(view2).state();
            this.mExpandIndicatorClickState = new AnimState("clicked state");
            this.mExpandIndicatorReleaseState = new AnimState("released state");
            this.mExpandIndicatorClickState.add(ViewProperty.ALPHA, f2, new long[0]);
            this.mExpandIndicatorReleaseState.add(ViewProperty.ALPHA, f, new long[0]);
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
            this.mExpandIndicatorAnimConfig = animConfig;
            Folme.clean(this.mBigQSTile);
            this.mBigQSTileAnim = Folme.useAt(this.mBigQSTile).state();
            AnimConfig animConfig2 = new AnimConfig();
            animConfig2.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
            this.mBigQSTileDownAnimConfig = animConfig2;
            AnimConfig animConfig3 = new AnimConfig();
            animConfig3.setEase(EaseManager.getStyle(0, 300.0f, 0.99f, 0.6666f));
            this.mBigQSTileUpAnimConfig = animConfig3;
        }

        public void onTouchEvent(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action != 0) {
                if (action != 1) {
                    if (action != 2) {
                        if (action != 3) {
                            return;
                        }
                    }
                }
                this.mExpandIndicatorAnim.cancel();
                this.mExpandIndicatorAnim.to(this.mExpandIndicatorReleaseState, this.mExpandIndicatorAnimConfig);
                this.mBigQSTileAnim.clean();
                IStateStyle iStateStyle = this.mBigQSTileAnim;
                AnimState animState = new AnimState("qs big tile up");
                animState.add(ViewProperty.ROTATION_X, 0, new long[0]);
                animState.add(ViewProperty.ROTATION_Y, 0, new long[0]);
                animState.add(ViewProperty.TRANSLATION_Z, 0, new long[0]);
                iStateStyle.to(animState, this.mBigQSTileUpAnimConfig);
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
                this.mBigQSTileAnim.clean();
                IStateStyle iStateStyle2 = this.mBigQSTileAnim;
                AnimState animState2 = new AnimState("qs big tile down");
                animState2.add(ViewProperty.ROTATION_X, f5, new long[0]);
                animState2.add(ViewProperty.ROTATION_Y, f6, new long[0]);
                animState2.add(ViewProperty.TRANSLATION_Z, sqrt, new long[0]);
                iStateStyle2.to(animState2, this.mBigQSTileDownAnimConfig);
                return;
            }
            this.mBigQSTileAnim.clean();
            this.mBigQSTile.setRotationX(f5);
            this.mBigQSTile.setRotationY(f6);
            this.mBigQSTile.setTranslationZ(sqrt);
        }
    }
}
