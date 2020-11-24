package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0018R$string;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;

public class EmptyShadeView extends StackScrollerDecorView {
    /* access modifiers changed from: private */
    public TextView mEmptyText;
    private int mText = C0018R$string.empty_shade_text;

    /* access modifiers changed from: protected */
    public View findSecondaryView() {
        return null;
    }

    public EmptyShadeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mEmptyText.setText(this.mText);
    }

    /* access modifiers changed from: protected */
    public View findContentView() {
        return findViewById(C0012R$id.no_notifications);
    }

    public void setText(int i) {
        this.mText = i;
        this.mEmptyText.setText(i);
    }

    public int getTextResource() {
        return this.mText;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEmptyText = (TextView) findContentView();
    }

    public ExpandableViewState createExpandableViewState() {
        return new EmptyShadeViewState();
    }

    public class EmptyShadeViewState extends ExpandableViewState {
        public EmptyShadeViewState() {
        }

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
