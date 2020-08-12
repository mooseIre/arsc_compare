package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class KeyguardStatusBarViewControllerHoleImpl extends KeyguardStatusBarViewControllerImpl {
    private ConfigurationController.ConfigurationListener mConfigurationListener;
    /* access modifiers changed from: private */
    public int mHolePaddingStartExtra;
    /* access modifiers changed from: private */
    public int mLayoutDirection;
    private ViewGroup mLeftPartContainer;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private ViewGroup mPromptContainer;
    private int mRoundConrnerPadding;
    private ViewGroup mSystemIconsContainer;

    public void init(KeyguardStatusBarView keyguardStatusBarView) {
        super.init(keyguardStatusBarView);
        final Resources resources = keyguardStatusBarView.getContext().getResources();
        this.mPaddingLeft = resources.getDimensionPixelOffset(R.dimen.statusbar_hole_type_padding_left);
        this.mPaddingRight = resources.getDimensionPixelOffset(R.dimen.statusbar_hole_type_padding_right);
        this.mPaddingTop = resources.getDimensionPixelOffset(R.dimen.notch_hole_type_status_bar_padding_top);
        this.mHolePaddingStartExtra = resources.getDimensionPixelOffset(R.dimen.status_bar_hole_padding_start_extra);
        this.mRoundConrnerPadding = resources.getDimensionPixelOffset(R.dimen.round_cornor_padding);
        this.mLeftPartContainer = (ViewGroup) keyguardStatusBarView.findViewById(R.id.left_part_super_container);
        this.mSystemIconsContainer = (ViewGroup) keyguardStatusBarView.findViewById(R.id.system_icons_padding_container);
        this.mPromptContainer = (ViewGroup) keyguardStatusBarView.findViewById(R.id.notch_prompt_content_container);
        onLayoutDirectionUpdate(keyguardStatusBarView.getContext().getResources().getConfiguration().getLayoutDirection());
        this.mConfigurationListener = new ConfigurationController.ConfigurationListener() {
            public void onDensityOrFontScaleChanged() {
            }

            public void onConfigChanged(Configuration configuration) {
                if (configuration.getLayoutDirection() != KeyguardStatusBarViewControllerHoleImpl.this.mLayoutDirection) {
                    int unused = KeyguardStatusBarViewControllerHoleImpl.this.mHolePaddingStartExtra = resources.getDimensionPixelOffset(R.dimen.status_bar_hole_padding_start_extra);
                    KeyguardStatusBarViewControllerHoleImpl.this.onLayoutDirectionUpdate(configuration.getLayoutDirection());
                }
            }
        };
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
    }

    /* access modifiers changed from: private */
    public void onLayoutDirectionUpdate(int i) {
        this.mLayoutDirection = i;
        boolean z = i == 0;
        int i2 = z ? this.mPaddingLeft : this.mPaddingRight;
        int i3 = z ? this.mPaddingRight : this.mPaddingLeft;
        ViewGroup viewGroup = this.mLeftPartContainer;
        if (viewGroup != null) {
            viewGroup.setPaddingRelative(i2, this.mPaddingTop, viewGroup.getPaddingEnd(), this.mLeftPartContainer.getPaddingBottom());
        }
        ViewGroup viewGroup2 = this.mSystemIconsContainer;
        if (viewGroup2 != null) {
            viewGroup2.setPaddingRelative(viewGroup2.getPaddingStart(), this.mPaddingTop, i3, this.mSystemIconsContainer.getPaddingBottom());
        }
        ViewGroup viewGroup3 = this.mPromptContainer;
        if (viewGroup3 != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewGroup3.getLayoutParams();
            layoutParams.setMarginStart(z ? this.mRoundConrnerPadding : this.mHolePaddingStartExtra);
            this.mPromptContainer.setLayoutParams(layoutParams);
        }
    }

    public void destroy() {
        super.destroy();
        if (this.mConfigurationListener != null) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this.mConfigurationListener);
        }
    }
}
