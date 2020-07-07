package com.android.systemui.miui.volume.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.systemui.miui.volume.widget.ExpandCollapseStateHelper;

public class ExpandCollapseLinearLayout extends LinearLayout implements ExpandCollapseStateHelper.OnExpandStateUpdatedListener {
    private ExpandCollapseStateHelper mStateHelper;

    public void onExpandStateUpdated(boolean z) {
    }

    public ExpandCollapseLinearLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public ExpandCollapseLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ExpandCollapseLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mStateHelper = new ExpandCollapseStateHelper(this, this, attributeSet, i);
    }

    public void updateExpanded(boolean z, boolean z2) {
        this.mStateHelper.updateExpanded(z, z2);
    }

    public boolean isExpanded() {
        return this.mStateHelper.isExpanded();
    }
}
