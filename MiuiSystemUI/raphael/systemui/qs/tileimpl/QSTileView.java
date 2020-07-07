package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.FontUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import java.util.Objects;

public class QSTileView extends QSTileBaseView {
    private View mDivider;
    private View mExpandIndicator;
    private View mExpandSpace;
    protected TextView mLabel;
    private ViewGroup mLabelContainer;
    private ImageView mPadLock;
    protected TextView mSecondLine;
    private int mState;

    public QSTileView(Context context, QSIconView qSIconView) {
        this(context, qSIconView, false);
    }

    public QSTileView(Context context, QSIconView qSIconView, boolean z) {
        super(context, qSIconView, z);
        setClipChildren(false);
        setClipToPadding(false);
        setClickable(true);
        setId(View.generateViewId());
        createLabel();
        setOrientation(1);
        setGravity(17);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontUtils.updateFontSize(this.mLabel, R.dimen.qs_tile_label_text_size);
        FontUtils.updateFontSize(this.mSecondLine, R.dimen.qs_tile_app_label_text_size);
    }

    public int getDetailY() {
        return getTop() + this.mLabelContainer.getTop() + (this.mLabelContainer.getHeight() / 2);
    }

    /* access modifiers changed from: protected */
    public void createLabel() {
        this.mLabelContainer = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.qs_tile_label, this, false);
        this.mLabelContainer.setClipChildren(false);
        this.mLabelContainer.setClipToPadding(false);
        this.mLabel = (TextView) this.mLabelContainer.findViewById(R.id.tile_label);
        this.mLabel.setSelected(true);
        this.mPadLock = (ImageView) this.mLabelContainer.findViewById(R.id.restricted_padlock);
        this.mDivider = this.mLabelContainer.findViewById(R.id.underline);
        this.mExpandIndicator = this.mLabelContainer.findViewById(R.id.expand_indicator);
        this.mExpandSpace = this.mLabelContainer.findViewById(R.id.expand_space);
        this.mSecondLine = (TextView) this.mLabelContainer.findViewById(R.id.app_label);
        addView(this.mLabelContainer);
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
        if (!Objects.equals(this.mLabel.getText(), state.label) || this.mState != state.state) {
            if (state.state == 0) {
                state.label = new SpannableStringBuilder().append(state.label, new ForegroundColorSpan(getContext().getColor(R.color.qs_tile_label_text_color_secondary)), 18);
            }
            this.mState = state.state;
            this.mLabel.setText(state.label);
        }
        int i = 0;
        if (!Objects.equals(this.mSecondLine.getText(), state.secondaryLabel)) {
            this.mSecondLine.setText(state.secondaryLabel);
            this.mSecondLine.setVisibility(TextUtils.isEmpty(state.secondaryLabel) ? 8 : 0);
        }
        boolean z = state.dualTarget;
        this.mExpandIndicator.setVisibility(z ? 0 : 8);
        this.mExpandSpace.setVisibility(z ? 0 : 8);
        this.mLabelContainer.setImportantForAccessibility(z ? 1 : 2);
        this.mLabelContainer.setContentDescription(z ? state.dualLabelContentDescription : null);
        if (z != this.mLabelContainer.isClickable()) {
            this.mLabelContainer.setClickable(z);
            this.mLabelContainer.setLongClickable(z);
        }
        this.mLabel.setEnabled(!state.disabledByPolicy);
        ImageView imageView = this.mPadLock;
        if (!state.disabledByPolicy) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        super.init(onClickListener, onClickListener2, onLongClickListener);
        this.mLabelContainer.setOnClickListener(onClickListener2);
        this.mLabelContainer.setOnLongClickListener(onLongClickListener);
        this.mLabelContainer.setClickable(false);
        this.mLabelContainer.setLongClickable(false);
    }
}
