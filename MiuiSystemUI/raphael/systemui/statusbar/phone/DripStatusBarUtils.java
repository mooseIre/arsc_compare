package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.plugins.R;

public class DripStatusBarUtils {
    static void updateContainerWidth(View view, boolean z, boolean z2, int i) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = getContainerWidth(view, z, z2, i);
        view.setLayoutParams(layoutParams);
    }

    private static int getContainerWidth(View view, boolean z, boolean z2, int i) {
        int i2;
        Resources resources = view.getContext().getResources();
        int i3 = z ? R.dimen.statusbar_padding_start : R.dimen.statusbar_padding_end;
        if (!z2 || z) {
            i2 = getSize(resources, i3);
        } else {
            i2 = 0;
        }
        return ((getSpaceWidthInPixel(view.getContext()) - getSize(resources, R.dimen.round_cornor_padding)) - i2) + i;
    }

    static int getSize(Resources resources, int i) {
        return resources.getDimensionPixelSize(i);
    }

    private static int getSpaceWidthInPixel(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayInfo displayInfo = new DisplayInfo();
        defaultDisplay.getDisplayInfo(displayInfo);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        return (displayMetrics.widthPixels - DisplayCutoutCompat.getCutoutWidth(displayInfo, context)) / 2;
    }

    public static void updateContainerEndMargin(ViewGroup viewGroup, int i) {
        int dimensionPixelSize = viewGroup.getContext().getResources().getDimensionPixelSize(R.dimen.round_cornor_padding) - i;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams();
        marginLayoutParams.setMarginEnd(dimensionPixelSize);
        viewGroup.setLayoutParams(marginLayoutParams);
    }
}
