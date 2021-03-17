package com.android.systemui.controlcenter.phone.detail;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.qs.tileview.QSBigTileView;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.DataBillClickEvent;
import com.miui.systemui.events.DataUsageClickEvent;
import com.miui.systemui.events.ExpandTileUnfoldEvent;
import com.miui.systemui.events.HealthClickEvent;
import com.miui.systemui.events.ScreenTileClickEvent;
import com.miui.systemui.events.SuperPowerClickEvent;
import com.miui.systemui.util.HapticFeedBackImpl;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class QSControlExpandTileView extends LinearLayout implements ExpandInfoController.Callback {
    private QSBigTileView.BigQSTileAnimationController mAnimatorController;
    private boolean mClicked;
    private ExpandInfoController mExpandInfoController;
    private ImageView mIndicator;
    private ExpandInfoController.Info mInfo;
    private int mLayoutDirection;
    private ControlPanelController mPanelController;
    private QSControlExpandDetail mQsControlExpandDetail;
    private TextView mStatus;
    private ImageView mStatusIcon;
    private TextView mTitle;

    public QSControlExpandTileView(Context context) {
        this(context, null);
    }

    public QSControlExpandTileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        updateBackground();
        this.mStatusIcon = (ImageView) findViewById(C0015R$id.status_icon);
        updateIconMargins();
        this.mIndicator = (ImageView) findViewById(C0015R$id.indicator);
        this.mTitle = (TextView) findViewById(C0015R$id.title);
        this.mStatus = (TextView) findViewById(C0015R$id.status);
        this.mIndicator.setContentDescription(((LinearLayout) this).mContext.getResources().getString(C0021R$string.accessibility_expand_button));
        this.mIndicator.setVisibility(this.mPanelController.isSuperPowerMode() ? 8 : 0);
        this.mExpandInfoController = (ExpandInfoController) Dependency.get(ExpandInfoController.class);
        setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView.AnonymousClass1 */

            public void onClick(View view) {
                QSControlExpandTileView.this.handleClick();
            }
        });
        setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView.AnonymousClass2 */

            public boolean onLongClick(View view) {
                return QSControlExpandTileView.this.handleClick();
            }
        });
        this.mQsControlExpandDetail = new QSControlExpandDetail(((LinearLayout) this).mContext, this, this.mIndicator);
        this.mIndicator.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView.AnonymousClass3 */

            public void onClick(View view) {
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new ExpandTileUnfoldEvent());
                QSControlExpandTileView.this.mQsControlExpandDetail.show();
            }
        });
        this.mAnimatorController = new QSBigTileView.BigQSTileAnimationController(this, this.mIndicator, 0.7f, 1.0f);
        this.mIndicator.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView.AnonymousClass4 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                QSControlExpandTileView.this.mAnimatorController.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == 1) {
                    view.callOnClick();
                }
                if (actionMasked == 0) {
                    QSControlExpandTileView.this.mClicked = true;
                }
                if (actionMasked == 3 || actionMasked == 1) {
                    QSControlExpandTileView.this.mClicked = false;
                }
                return true;
            }
        });
        updateIndicatorTouch();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ExpandInfoController expandInfoController = this.mExpandInfoController;
        if (expandInfoController != null) {
            expandInfoController.addCallback(this);
        }
        QSControlExpandDetail qSControlExpandDetail = this.mQsControlExpandDetail;
        if (qSControlExpandDetail != null) {
            qSControlExpandDetail.addExpandInfoCallback();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ExpandInfoController expandInfoController = this.mExpandInfoController;
        if (expandInfoController != null) {
            expandInfoController.removeCallback(this);
        }
        QSControlExpandDetail qSControlExpandDetail = this.mQsControlExpandDetail;
        if (qSControlExpandDetail != null) {
            qSControlExpandDetail.removeExpandInfoCallback();
        }
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
            /* class com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView.AnonymousClass5 */

            public void run() {
                int dimensionPixelSize = ((LinearLayout) QSControlExpandTileView.this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_indicator_touch_h);
                int dimensionPixelSize2 = ((LinearLayout) QSControlExpandTileView.this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_indicator_touch_v);
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

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        new AnimConfig();
        ITouchStyle iTouchStyle = Folme.useAt(this).touch();
        iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
        iTouchStyle.onMotionEventEx(this, motionEvent, new AnimConfig[0]);
        if (actionMasked == 0 || actionMasked == 1) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).flick();
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override // com.android.systemui.controlcenter.phone.ExpandInfoController.Callback
    public void updateInfo(int i, ExpandInfoController.Info info) {
        updateViews();
    }

    @Override // com.android.systemui.controlcenter.phone.ExpandInfoController.Callback
    public void updateSelectedType(int i) {
        updateViews();
    }

    public void updateResources() {
        int dimensionPixelSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_width);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        if (layoutParams.width != dimensionPixelSize) {
            layoutParams.width = dimensionPixelSize;
            setLayoutParams(layoutParams);
        }
        updateIconMargins();
        this.mQsControlExpandDetail.updateResources();
        updateBackground();
        this.mTitle.setTextAppearance(C0022R$style.TextAppearance_QSControl_ExpandTileTitle);
        this.mStatus.setTextAppearance(C0022R$style.TextAppearance_QSControl_ExpandTileSubTitle);
        this.mIndicator.setImageDrawable(((LinearLayout) this).mContext.getDrawable(C0013R$drawable.qs_big_tile_expand_indicator_dark));
        updateViews();
    }

    private void updateBackground() {
        Drawable smoothRoundDrawable = ControlCenterUtils.getSmoothRoundDrawable(((LinearLayout) this).mContext, C0013R$drawable.ic_qs_big_tile_bg_0);
        if (smoothRoundDrawable != null) {
            setBackground(smoothRoundDrawable);
        }
    }

    private void updateIconMargins() {
        if (this.mStatusIcon != null) {
            float dimensionPixelSize = (float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_icon_size);
            float dimensionPixelSize2 = (float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_expand_tile_icon_size);
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
    /* access modifiers changed from: public */
    private boolean handleClick() {
        Object obj;
        int selectedType = this.mExpandInfoController.getSelectedType();
        if (selectedType == 0) {
            obj = new DataUsageClickEvent();
        } else if (selectedType == 1) {
            obj = new DataBillClickEvent();
        } else if (selectedType == 2) {
            obj = new HealthClickEvent();
        } else if (selectedType == 3) {
            obj = new ScreenTileClickEvent();
        } else {
            obj = selectedType == 16 ? new SuperPowerClickEvent() : null;
        }
        if (obj != null) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(obj);
        }
        ExpandInfoController.Info info = this.mExpandInfoController.getInfosMap().get(Integer.valueOf(this.mExpandInfoController.getSelectedType()));
        if (this.mInfo == null) {
            return false;
        }
        String str = info.action;
        if (!TextUtils.isEmpty(str)) {
            this.mExpandInfoController.startActivity(str);
        } else {
            this.mExpandInfoController.startActivityByUri(info.uri);
        }
        return true;
    }

    public void updateViews() {
        ExpandInfoController.Info info = this.mExpandInfoController.getInfosMap().get(Integer.valueOf(this.mExpandInfoController.getSelectedType()));
        this.mInfo = info;
        if (info != null) {
            this.mTitle.setText(info.title);
            this.mStatusIcon.setImageBitmap(this.mInfo.icon);
            if (!TextUtils.isEmpty(this.mInfo.status)) {
                SpannableString spannableString = new SpannableString(this.mInfo.status + " " + this.mInfo.unit);
                spannableString.setSpan(new TextAppearanceSpan(((LinearLayout) this).mContext, C0022R$style.TextAppearance_QSControl_ExpandTileSubTitle), 0, this.mInfo.status.length() + -1, 18);
                spannableString.setSpan(new TextAppearanceSpan(((LinearLayout) this).mContext, C0022R$style.TextAppearance_QSControl_ExpandTileUnit), this.mInfo.status.length(), spannableString.length(), 18);
                this.mStatus.setText(spannableString);
            }
            StringBuilder sb = new StringBuilder(this.mTitle.getText());
            sb.append(this.mStatus.getText());
            setContentDescription(sb);
            Log.d("QSControlExpandTileView", "updateViews" + this.mInfo.toString());
        }
    }
}
