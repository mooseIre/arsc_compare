package com.android.systemui.controlcenter.phone.customize;

import android.content.Context;
import com.android.systemui.C0008R$array;
import com.android.systemui.controlcenter.utils.Constants;
import com.android.systemui.qs.customize.TileQueryHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

public class CCTileQueryHelper extends TileQueryHelper {
    protected List<String> mControlIndependentTiles;

    public CCTileQueryHelper(Context context, Executor executor, Executor executor2) {
        super(context, executor, executor2);
        ArrayList arrayList = new ArrayList();
        this.mControlIndependentTiles = arrayList;
        if (Constants.IS_INTERNATIONAL) {
            arrayList.addAll(Arrays.asList(context.getResources().getStringArray(C0008R$array.qs_control_independent_tiles_global)));
        } else {
            arrayList.addAll(Arrays.asList(context.getResources().getStringArray(C0008R$array.qs_control_independent_tiles)));
        }
        for (String str : this.mControlIndependentTiles) {
            String str2 = this.mTilesStock;
            this.mTilesStock = str2.replace(str + ",", "");
        }
    }

    public void filterBigTile(ArrayList<String> arrayList) {
        super.filterBigTile(arrayList);
        if (arrayList != null) {
            for (String remove : this.mControlIndependentTiles) {
                arrayList.remove(remove);
            }
        }
    }
}
