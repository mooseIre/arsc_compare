package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;

public class FooterView extends StackScrollerDecorView {
    private final int mClearAllTopPadding;
    private FooterViewButton mDismissButton;
    private FooterViewButton mManageButton;
    private boolean mShowHistory;

    public FooterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClearAllTopPadding = context.getResources().getDimensionPixelSize(C0012R$dimen.clear_all_padding_top);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findContentView() {
        return findViewById(C0015R$id.content);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findSecondaryView() {
        return findViewById(C0015R$id.dismiss_text);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDismissButton = (FooterViewButton) findSecondaryView();
        this.mManageButton = (FooterViewButton) findViewById(C0015R$id.manage_text);
    }

    public void setTextColor(int i) {
        this.mManageButton.setTextColor(i);
        this.mDismissButton.setTextColor(i);
    }

    public void setManageButtonClickListener(View.OnClickListener onClickListener) {
        this.mManageButton.setOnClickListener(onClickListener);
    }

    public void setDismissButtonClickListener(View.OnClickListener onClickListener) {
        this.mDismissButton.setOnClickListener(onClickListener);
    }

    public boolean isOnEmptySpace(float f, float f2) {
        return f < this.mContent.getX() || f > this.mContent.getX() + ((float) this.mContent.getWidth()) || f2 < this.mContent.getY() || f2 > this.mContent.getY() + ((float) this.mContent.getHeight());
    }

    public void showHistory(boolean z) {
        this.mShowHistory = z;
        if (z) {
            this.mManageButton.setText(C0021R$string.manage_notifications_history_text);
            this.mManageButton.setContentDescription(((FrameLayout) this).mContext.getString(C0021R$string.manage_notifications_history_text));
            return;
        }
        this.mManageButton.setText(C0021R$string.manage_notifications_text);
        this.mManageButton.setContentDescription(((FrameLayout) this).mContext.getString(C0021R$string.manage_notifications_text));
    }

    public boolean isHistoryShown() {
        return this.mShowHistory;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mDismissButton.setText(C0021R$string.clear_all_notifications_text);
        this.mDismissButton.setContentDescription(((FrameLayout) this).mContext.getString(C0021R$string.accessibility_clear_all));
        showHistory(this.mShowHistory);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public ExpandableViewState createExpandableViewState() {
        return new FooterViewState();
    }

    public class FooterViewState extends ExpandableViewState {
        public FooterViewState() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState, com.android.systemui.statusbar.notification.stack.ExpandableViewState
        public void applyToView(View view) {
            super.applyToView(view);
            if (view instanceof FooterView) {
                FooterView footerView = (FooterView) view;
                boolean z = true;
                if (!(this.clipTopAmount < FooterView.this.mClearAllTopPadding) || !footerView.isVisible()) {
                    z = false;
                }
                footerView.setContentVisible(z);
            }
        }
    }
}
