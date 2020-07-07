package com.android.systemui.fsgesture;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class FsGestureDemoTitleView extends FrameLayout {
    private TextView mSkipView;
    private TextView mSummaryView;
    private TextView mTitleView;

    public FsGestureDemoTitleView(Context context) {
        this(context, (AttributeSet) null);
    }

    public FsGestureDemoTitleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FsGestureDemoTitleView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public FsGestureDemoTitleView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init();
    }

    private void init() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.fs_gesture_title_view, this);
        this.mTitleView = (TextView) inflate.findViewById(R.id.fsgesture_ready_title);
        this.mSummaryView = (TextView) inflate.findViewById(R.id.fsgesture_ready_summary);
        this.mSkipView = (TextView) inflate.findViewById(R.id.fsgesture_skip);
        setElevation((float) getResources().getDimensionPixelSize(R.dimen.gesture_title_view_elevation));
    }

    /* access modifiers changed from: package-private */
    public void setRTLParams() {
        ViewGroup.LayoutParams layoutParams = this.mSkipView.getLayoutParams();
        if (layoutParams instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) layoutParams;
            layoutParams2.removeRule(20);
            layoutParams2.addRule(11);
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.fsgesture_skip_margin_right);
            this.mSkipView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.fsgesture_skip_margin_top), dimensionPixelSize, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareTitleView(int i) {
        int i2;
        setBackground(getResources().getDrawable(R.drawable.fs_gesture_back_bg, (Resources.Theme) null));
        int i3 = R.string.how_to_use_app_quick;
        switch (i) {
            case 0:
                i2 = R.string.fs_gesture_left_back_ready_summary;
                break;
            case 1:
                i2 = R.string.fs_gesture_right_back_ready_summary;
                break;
            case 2:
                i3 = R.string.how_to_back_home;
                i2 = R.string.fs_gesture_back_home_summary;
                break;
            case 3:
                i3 = R.string.how_to_switch_recents;
                i2 = R.string.fs_gesture_switch_recents_summary;
                break;
            case 4:
                i3 = R.string.how_to_use_drawer;
                i2 = R.string.how_to_use_drawer_summary;
                break;
            case 5:
                i2 = R.string.how_to_use_app_quick_summary;
                break;
            case 6:
                i2 = R.string.how_to_use_app_quick_hide_line_summary;
                break;
            default:
                i2 = 0;
                i3 = 0;
                break;
        }
        i3 = R.string.fs_gesture_back_ready_title;
        TextView textView = this.mTitleView;
        if (textView != null && this.mSummaryView != null) {
            textView.setText(i3);
            this.mSummaryView.setText(i2);
            this.mTitleView.setVisibility(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyFinish() {
        setBackground(getResources().getDrawable(R.drawable.fs_gesture_finish_bg, (Resources.Theme) null));
        this.mTitleView.setVisibility(4);
        this.mSummaryView.setTranslationY(this.mSummaryView.getTranslationX() - 15.0f);
        this.mSummaryView.setText(R.string.fs_gesture_finish);
        this.mSkipView.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public void registerSkipEvent(View.OnClickListener onClickListener) {
        TextView textView = this.mSkipView;
        if (textView != null) {
            textView.setOnClickListener(onClickListener);
        }
    }
}
