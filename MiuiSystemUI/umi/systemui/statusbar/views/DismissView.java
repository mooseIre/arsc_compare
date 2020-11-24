package com.android.systemui.statusbar.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0013R$integer;
import com.android.systemui.C0018R$string;
import com.android.systemui.views.CircleAndTickAnimView;

public class DismissView extends CircleAndTickAnimView {
    public DismissView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setDrawables(C0010R$drawable.notifications_clear_all, C0010R$drawable.btn_clear_all);
        setContentDescription(getContext().getString(C0018R$string.accessibility_clear_all));
        updateLayoutParam();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    private void updateLayoutParam() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(C0009R$dimen.notification_clear_all_bottom_margin);
        layoutParams.rightMargin = getResources().getDimensionPixelSize(C0009R$dimen.notification_clear_all_end_margin);
        layoutParams.gravity = getResources().getInteger(C0013R$integer.dismiss_button_layout_gravity);
        setLayoutParams(layoutParams);
    }
}
