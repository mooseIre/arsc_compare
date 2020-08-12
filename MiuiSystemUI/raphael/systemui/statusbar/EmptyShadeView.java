package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.statusbar.stack.ExpandableViewState;

public class EmptyShadeView extends StackScrollerDecorView {
    /* access modifiers changed from: protected */
    public View findContentView() {
        return this;
    }

    public EmptyShadeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
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
                ((EmptyShadeView) view).performVisibilityAnimation(false);
            }
        }
    }
}
