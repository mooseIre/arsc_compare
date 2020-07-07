package com.android.systemui.miui.controlcenter.customize;

import android.content.Context;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.customize.TileQueryHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CCTileQueryHelper extends TileQueryHelper {
    protected List<String> mControlIndependentTiles = new ArrayList();

    public CCTileQueryHelper(Context context, TileQueryHelper.TileStateListener tileStateListener) {
        super(context, tileStateListener);
        if (Constants.IS_INTERNATIONAL) {
            this.mControlIndependentTiles.addAll(Arrays.asList(context.getResources().getStringArray(R.array.qs_control_independent_tiles_global)));
        } else {
            this.mControlIndependentTiles.addAll(Arrays.asList(context.getResources().getStringArray(R.array.qs_control_independent_tiles)));
        }
        for (String str : this.mControlIndependentTiles) {
            String str2 = this.mTilesStock;
            this.mTilesStock = str2.replace(str + ",", "");
        }
    }
}
