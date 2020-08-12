package com.android.systemui.statusbar.stack;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import java.util.List;

public class StackScrollState {
    private AmbientState mAmbientState;
    private final ViewGroup mHostView;

    public void removeViewStateForView(View view) {
    }

    public StackScrollState(ViewGroup viewGroup) {
        this.mHostView = viewGroup;
    }

    public ViewGroup getHostView() {
        return this.mHostView;
    }

    public void setAmbientState(AmbientState ambientState) {
        this.mAmbientState = ambientState;
    }

    public void resetViewStates() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostView.getChildAt(i);
            resetViewState(expandableView);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                if (expandableNotificationRow.isSummaryWithChildren() && notificationChildren != null) {
                    for (ExpandableNotificationRow resetViewState : notificationChildren) {
                        resetViewState(resetViewState);
                    }
                }
            }
        }
    }

    private void resetViewState(ExpandableView expandableView) {
        ExpandableViewState viewState = expandableView.getViewState();
        viewState.height = expandableView.getIntrinsicHeight();
        viewState.gone = expandableView.getVisibility() == 8;
        float f = 1.0f;
        viewState.alpha = this.mAmbientState.isPanelTracking() ? expandableView.getAlpha() : 1.0f;
        viewState.shadowAlpha = 1.0f;
        viewState.notGoneIndex = -1;
        viewState.xTranslation = expandableView.getTranslationX();
        viewState.hidden = false;
        viewState.scaleX = needResetScale(expandableView) ? 1.0f : expandableView.getScaleX();
        if (!needResetScale(expandableView)) {
            f = expandableView.getScaleY();
        }
        viewState.scaleY = f;
        viewState.inShelf = false;
        viewState.paddingTop = 0;
        viewState.paddingBottom = 0;
    }

    private boolean needResetScale(ExpandableView expandableView) {
        Object tag = expandableView.getTag(R.id.view_reset_scale_tag);
        return tag != null && ((Boolean) tag).booleanValue();
    }

    public ExpandableViewState getViewStateForView(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getViewState();
        }
        return null;
    }

    public void apply() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) this.mHostView.getChildAt(i);
            ExpandableViewState viewState = expandableView.getViewState();
            if (viewState == null) {
                Log.wtf("StackScrollStateNoSuchChild", "No child state was found when applying this state to the hostView");
            } else if (!viewState.gone) {
                viewState.applyToView(expandableView);
            }
        }
    }
}
