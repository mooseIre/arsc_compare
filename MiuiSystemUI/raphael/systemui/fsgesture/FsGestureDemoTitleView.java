package com.android.systemui.fsgesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;

public class FsGestureDemoTitleView extends FrameLayout {
    private TextView mSkipView;
    private TextView mSummaryView;
    private TextView mTitleView;

    public FsGestureDemoTitleView(Context context) {
        this(context, null);
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
        View inflate = LayoutInflater.from(getContext()).inflate(C0017R$layout.fs_gesture_title_view, this);
        this.mTitleView = (TextView) inflate.findViewById(C0015R$id.fsgesture_ready_title);
        this.mSummaryView = (TextView) inflate.findViewById(C0015R$id.fsgesture_ready_summary);
        this.mSkipView = (TextView) inflate.findViewById(C0015R$id.fsgesture_skip);
        setElevation((float) getResources().getDimensionPixelSize(C0012R$dimen.gesture_title_view_elevation));
    }

    /* access modifiers changed from: package-private */
    public void setRTLParams() {
        ViewGroup.LayoutParams layoutParams = this.mSkipView.getLayoutParams();
        if (layoutParams instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) layoutParams;
            layoutParams2.removeRule(20);
            layoutParams2.addRule(11);
            int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.fsgesture_skip_margin_right);
            this.mSkipView.setPadding(0, getResources().getDimensionPixelSize(C0012R$dimen.fsgesture_skip_margin_top), dimensionPixelSize, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareTitleView(int i) {
        int i2;
        int i3;
        setBackground(getResources().getDrawable(C0013R$drawable.fs_gesture_back_bg, null));
        switch (i) {
            case 0:
                i2 = C0021R$string.fs_gesture_back_ready_title;
                i3 = C0021R$string.fs_gesture_left_back_ready_summary;
                break;
            case 1:
                i2 = C0021R$string.fs_gesture_back_ready_title;
                i3 = C0021R$string.fs_gesture_right_back_ready_summary;
                break;
            case 2:
                i2 = C0021R$string.how_to_back_home;
                i3 = C0021R$string.fs_gesture_back_home_summary;
                break;
            case 3:
                i2 = C0021R$string.how_to_switch_recents;
                i3 = C0021R$string.fs_gesture_switch_recents_summary;
                break;
            case 4:
                i2 = C0021R$string.how_to_use_drawer;
                i3 = C0021R$string.how_to_use_drawer_summary;
                break;
            case 5:
                i2 = C0021R$string.how_to_use_app_quick;
                i3 = C0021R$string.how_to_use_app_quick_summary;
                break;
            case 6:
                i2 = C0021R$string.how_to_use_app_quick;
                i3 = C0021R$string.how_to_use_app_quick_hide_line_summary;
                break;
            default:
                i2 = 0;
                i3 = 0;
                break;
        }
        TextView textView = this.mTitleView;
        if (textView != null && this.mSummaryView != null) {
            textView.setText(i2);
            this.mSummaryView.setText(i3);
            this.mTitleView.setVisibility(0);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyFinish() {
        setBackground(getResources().getDrawable(C0013R$drawable.fs_gesture_finish_bg, null));
        this.mTitleView.setVisibility(4);
        this.mSummaryView.setTranslationY(this.mSummaryView.getTranslationX() - 15.0f);
        this.mSummaryView.setText(C0021R$string.fs_gesture_finish);
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
