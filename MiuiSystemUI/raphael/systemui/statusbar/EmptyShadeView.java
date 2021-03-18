package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;

public class EmptyShadeView extends StackScrollerDecorView {
    private TextView mEmptyText;
    private int mText = C0021R$string.empty_shade_text;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findSecondaryView() {
        return null;
    }

    public EmptyShadeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mEmptyText.setText(this.mText);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findContentView() {
        return findViewById(C0015R$id.no_notifications);
    }

    public void setText(int i) {
        this.mText = i;
        this.mEmptyText.setText(i);
    }

    public int getTextResource() {
        return this.mText;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEmptyText = (TextView) findContentView();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public ExpandableViewState createExpandableViewState() {
        return new EmptyShadeViewState();
    }

    public class EmptyShadeViewState extends ExpandableViewState {
        public EmptyShadeViewState() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState, com.android.systemui.statusbar.notification.stack.ExpandableViewState
        public void applyToView(View view) {
            super.applyToView(view);
            if (view instanceof EmptyShadeView) {
                EmptyShadeView emptyShadeView = (EmptyShadeView) view;
                boolean z = true;
                if (!(((float) this.clipTopAmount) <= ((float) EmptyShadeView.this.mEmptyText.getPaddingTop()) * 0.6f) || !emptyShadeView.isVisible()) {
                    z = false;
                }
                emptyShadeView.setContentVisible(z);
            }
        }
    }
}
