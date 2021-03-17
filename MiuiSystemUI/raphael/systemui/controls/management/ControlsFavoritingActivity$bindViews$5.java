package com.android.systemui.controls.management;

import androidx.viewpager2.widget.ViewPager2;
import com.android.systemui.controls.TooltipManager;

/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity$bindViews$5 extends ViewPager2.OnPageChangeCallback {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$bindViews$5(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    public void onPageSelected(int i) {
        super.onPageSelected(i);
        TooltipManager access$getMTooltipManager$p = this.this$0.mTooltipManager;
        if (access$getMTooltipManager$p != null) {
            access$getMTooltipManager$p.hide(true);
        }
    }
}
