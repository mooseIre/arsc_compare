package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.util.Utils;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class QSControlExpandTileView extends LinearLayout implements ExpandInfoController.Callback {
    /* access modifiers changed from: private */
    public BigQSTileAnimationController mAnimatorController;
    /* access modifiers changed from: private */
    public boolean mClicked;
    private ExpandInfoController mExpandInfoController;
    /* access modifiers changed from: private */
    public ImageView mIndicator;
    private ExpandInfoController.Info mInfo;
    private int mLayoutDirection;
    private ControlPanelController mPanelController;
    /* access modifiers changed from: private */
    public QSControlExpandDetail mQsControlExpandDetail;
    private TextView mStatus;
    private ImageView mStatusIcon;
    private TextView mTitle;

    public QSControlExpandTileView(Context context) {
        this(context, (AttributeSet) null);
    }

    public QSControlExpandTileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        updateBackground();
        this.mStatusIcon = (ImageView) findViewById(R.id.status_icon);
        updateIconMargins();
        this.mIndicator = (ImageView) findViewById(R.id.indicator);
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mStatus = (TextView) findViewById(R.id.status);
        this.mIndicator.setContentDescription(this.mContext.getResources().getString(R.string.accessibility_expand_button));
        this.mIndicator.setVisibility(this.mPanelController.isSuperPowerMode() ? 8 : 0);
        ExpandInfoController expandInfoController = (ExpandInfoController) Dependency.get(ExpandInfoController.class);
        this.mExpandInfoController = expandInfoController;
        expandInfoController.addCallback(this);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = QSControlExpandTileView.this.handleClick();
            }
        });
        setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                return QSControlExpandTileView.this.handleClick();
            }
        });
        this.mQsControlExpandDetail = new QSControlExpandDetail(this.mContext, this, this.mIndicator);
        this.mIndicator.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent("expand_tile_unfold");
                QSControlExpandTileView.this.mQsControlExpandDetail.show();
            }
        });
        this.mAnimatorController = new BigQSTileAnimationController(this, this.mIndicator, 0.7f, 1.0f);
        this.mIndicator.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                QSControlExpandTileView.this.mAnimatorController.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == 1) {
                    view.callOnClick();
                }
                if (actionMasked == 0) {
                    boolean unused = QSControlExpandTileView.this.mClicked = true;
                }
                if (actionMasked == 3 || actionMasked == 1) {
                    boolean unused2 = QSControlExpandTileView.this.mClicked = false;
                }
                return true;
            }
        });
        updateIndicatorTouch();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int layoutDirection = getLayoutDirection();
        if (this.mLayoutDirection != layoutDirection) {
            this.mLayoutDirection = layoutDirection;
            updateIndicatorTouch();
        }
    }

    private void updateIndicatorTouch() {
        this.mIndicator.post(new Runnable() {
            public void run() {
                int dimensionPixelSize = QSControlExpandTileView.this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_big_tile_indicator_touch_h);
                int dimensionPixelSize2 = QSControlExpandTileView.this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_big_tile_indicator_touch_v);
                Rect rect = new Rect();
                QSControlExpandTileView.this.mIndicator.getHitRect(rect);
                rect.top -= dimensionPixelSize2;
                rect.bottom += dimensionPixelSize2;
                rect.left -= dimensionPixelSize;
                rect.right += dimensionPixelSize;
                TouchDelegate touchDelegate = new TouchDelegate(rect, QSControlExpandTileView.this.mIndicator);
                if (View.class.isInstance(QSControlExpandTileView.this.mIndicator.getParent())) {
                    ((View) QSControlExpandTileView.this.mIndicator.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    public boolean isClicked() {
        return this.mClicked;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        ITouchStyle iTouchStyle = Folme.useAt(this).touch();
        iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
        iTouchStyle.onMotionEventEx(this, motionEvent, new AnimConfig[0]);
        if (actionMasked == 0 || actionMasked == 1) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).flick();
        }
        return super.onTouchEvent(motionEvent);
    }

    public void updateInfo(int i, ExpandInfoController.Info info) {
        updateViews();
    }

    public void updateSelectedType(int i) {
        updateViews();
    }

    public void updateResources() {
        updateIconMargins();
        this.mQsControlExpandDetail.updateResources();
        updateBackground();
        this.mTitle.setTextAppearance(R.style.TextAppearance_QSControl_ExpandTileTitle);
        this.mStatus.setTextAppearance(R.style.TextAppearance_QSControl_ExpandTileSubTitle);
        this.mIndicator.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_big_tile_expand_indicator_dark));
        updateViews();
    }

    private void updateBackground() {
        Drawable smoothRoundDrawable = Utils.getSmoothRoundDrawable(this.mContext, R.drawable.ic_qs_big_tile_bg_0);
        if (smoothRoundDrawable != null) {
            setBackground(smoothRoundDrawable);
        }
    }

    private void updateIconMargins() {
        if (this.mStatusIcon != null) {
            float dimensionPixelSize = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_big_tile_icon_size);
            float dimensionPixelSize2 = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_expand_tile_icon_size);
            if (dimensionPixelSize2 < dimensionPixelSize) {
                int i = (int) ((dimensionPixelSize - dimensionPixelSize2) / 2.0f);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mStatusIcon.getLayoutParams();
                layoutParams.setMarginStart(i);
                layoutParams.setMarginEnd(i);
                this.mStatusIcon.setLayoutParams(layoutParams);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean handleClick() {
        int selectedType = this.mExpandInfoController.getSelectedType();
        String str = selectedType == 0 ? "data_usage_click" : selectedType == 1 ? "data_bill_click" : selectedType == 2 ? "health_click" : selectedType == 3 ? "screen_time_click" : selectedType == 16 ? "super_power_click" : null;
        if (!TextUtils.isEmpty(str)) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(str);
        }
        ExpandInfoController.Info info = this.mExpandInfoController.getInfosMap().get(Integer.valueOf(this.mExpandInfoController.getSelectedType()));
        if (this.mInfo == null) {
            return false;
        }
        String str2 = info.action;
        if (!TextUtils.isEmpty(str2)) {
            this.mExpandInfoController.startActivity(str2);
        } else {
            this.mExpandInfoController.startActivityByUri(info.uri);
        }
        return true;
    }

    public void updateViews() {
        ExpandInfoController.Info info = this.mExpandInfoController.getInfosMap().get(Integer.valueOf(this.mExpandInfoController.getSelectedType()));
        this.mInfo = info;
        if (info == null) {
            this.mInfo = this.mExpandInfoController.getInfosMap().get(0);
        }
        this.mTitle.setText(this.mInfo.title);
        this.mStatusIcon.setImageBitmap(this.mInfo.icon);
        if (!TextUtils.isEmpty(this.mInfo.status)) {
            SpannableString spannableString = new SpannableString(this.mInfo.status + " " + this.mInfo.unit);
            spannableString.setSpan(new TextAppearanceSpan(this.mContext, R.style.TextAppearance_QSControl_ExpandTileSubTitle), 0, this.mInfo.status.length() + -1, 18);
            spannableString.setSpan(new TextAppearanceSpan(this.mContext, R.style.TextAppearance_QSControl_ExpandTileUnit), this.mInfo.status.length(), spannableString.length(), 18);
            this.mStatus.setText(spannableString);
        }
        Log.d("QSControlExpandTileView", "updateViews" + this.mInfo.toString());
    }

    private static class BigQSTileAnimationController {
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
                this.mBigQSTileAnim.cancel();
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
                this.mBigQSTileAnim.cancel();
                IStateStyle iStateStyle2 = this.mBigQSTileAnim;
                AnimState animState2 = new AnimState("qs big tile down");
                animState2.add(ViewProperty.ROTATION_X, f5, new long[0]);
                animState2.add(ViewProperty.ROTATION_Y, f6, new long[0]);
                animState2.add(ViewProperty.TRANSLATION_Z, sqrt, new long[0]);
                iStateStyle2.to(animState2, this.mBigQSTileDownAnimConfig);
                return;
            }
            this.mBigQSTileAnim.cancel();
            this.mBigQSTile.setRotationX(f5);
            this.mBigQSTile.setRotationY(f6);
            this.mBigQSTile.setTranslationZ(sqrt);
        }
    }
}
