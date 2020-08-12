package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.R;

public class CollapsedStatusBarFragmentControllerHoleImpl extends CollapsedStatusBarFragmentControllerImpl {
    private int mCarrierMaxWidth;
    private int mHolePaddingStartExtra;
    private ViewGroup mNotificationContainer;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private ViewGroup mStatusBarContainer;

    public boolean isClockVisibleByPrompt(boolean z) {
        return true;
    }

    public void init(CollapsedStatusBarFragment collapsedStatusBarFragment) {
        super.init(collapsedStatusBarFragment);
        Resources resources = collapsedStatusBarFragment.getContext().getResources();
        this.mPaddingLeft = resources.getDimensionPixelOffset(R.dimen.statusbar_hole_type_padding_left);
        this.mPaddingRight = resources.getDimensionPixelOffset(R.dimen.statusbar_hole_type_padding_right);
        this.mPaddingTop = resources.getDimensionPixelOffset(R.dimen.notch_hole_type_status_bar_padding_top);
        this.mCarrierMaxWidth = resources.getDimensionPixelOffset(R.dimen.statusbar_carrier_max_width);
        this.mHolePaddingStartExtra = resources.getDimensionPixelOffset(R.dimen.status_bar_hole_padding_start_extra);
    }

    private void updatePadding() {
        ViewGroup viewGroup = this.mStatusBarContainer;
        if (viewGroup != null) {
            viewGroup.setPadding(this.mPaddingLeft, this.mPaddingTop, this.mPaddingRight, viewGroup.getPaddingBottom());
        }
    }

    public void initViews(View view) {
        super.initViews(view);
        this.mStatusBarContainer = (ViewGroup) view.findViewById(R.id.phone_status_bar_contents_container);
        this.mNotificationContainer = (ViewGroup) view.findViewById(R.id.notification_icon_area);
        updatePadding();
    }

    public void updateLeftPartVisibility(boolean z, boolean z2, boolean z3, boolean z4) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mFragment;
        if (collapsedStatusBarFragment != null) {
            int i = 0;
            if (!z4) {
                super.updateLeftPartVisibility(z, z2, z3, false);
                return;
            }
            collapsedStatusBarFragment.clockVisibleAnimate(z3 && z, true);
            ViewGroup viewGroup = this.mNotificationContainer;
            if (!z3) {
                i = this.mCarrierMaxWidth + this.mHolePaddingStartExtra;
            }
            viewGroup.setPaddingRelative(i, this.mNotificationContainer.getPaddingTop(), this.mNotificationContainer.getPaddingEnd(), this.mNotificationContainer.getPaddingBottom());
        }
    }
}
