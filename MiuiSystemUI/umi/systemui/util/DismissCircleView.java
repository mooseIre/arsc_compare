package com.android.systemui.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.C0011R$dimen;
import com.android.systemui.C0012R$drawable;

public class DismissCircleView extends FrameLayout {
    private final ImageView mIconView = new ImageView(getContext());

    public DismissCircleView(Context context) {
        super(context);
        Resources resources = getResources();
        setBackground(resources.getDrawable(C0012R$drawable.dismiss_circle_background));
        this.mIconView.setImageDrawable(resources.getDrawable(C0012R$drawable.ic_close_white));
        addView(this.mIconView);
        setViewSizes();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setViewSizes();
    }

    private void setViewSizes() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0011R$dimen.dismiss_target_x_size);
        this.mIconView.setLayoutParams(new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize, 17));
    }
}
