package com.android.systemui.controls.management;

import android.text.TextUtils;
import androidx.viewpager2.widget.ViewPager2;

/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity$setUpPager$$inlined$apply$lambda$1 extends ViewPager2.OnPageChangeCallback {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$setUpPager$$inlined$apply$lambda$1(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
    public void onPageSelected(int i) {
        super.onPageSelected(i);
        CharSequence structureName = ((StructureContainer) this.this$0.listOfStructures.get(i)).getStructureName();
        if (TextUtils.isEmpty(structureName)) {
            structureName = this.this$0.appName;
        }
        ControlsFavoritingActivity.access$getTitleView$p(this.this$0).setText(structureName);
        ControlsFavoritingActivity.access$getTitleView$p(this.this$0).requestFocus();
    }

    @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
    public void onPageScrolled(int i, float f, int i2) {
        super.onPageScrolled(i, f, i2);
        ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0).setLocation(((float) i) + f);
    }
}
