package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.views.CircleAndTickAnimView;

public class DismissView extends CircleAndTickAnimView {
    private int mExtraMarginBottom;

    public DismissView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setDrawables(R.drawable.notifications_clear_all, R.drawable.btn_clear_all);
        setContentDescription(getContext().getString(R.string.accessibility_clear_all));
        updateLayoutParam();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        WindowInsets onApplyWindowInsets = super.onApplyWindowInsets(windowInsets);
        this.mExtraMarginBottom = onApplyWindowInsets.getStableInsetBottom();
        updateLayoutParam();
        return onApplyWindowInsets;
    }

    private void updateLayoutParam() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.notification_clear_all_bottom_margin) + this.mExtraMarginBottom;
        layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.notification_clear_all_end_margin);
        layoutParams.gravity = getResources().getInteger(R.integer.dismiss_button_layout_gravity);
        setLayoutParams(layoutParams);
    }
}
