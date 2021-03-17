package com.android.keyguard.fod.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;

public abstract class IQuickOpenItem {
    protected Context mContext;
    private final RectF mRectF;
    private final Region mRegion;

    public abstract Intent getIntent();

    public abstract String getSubTitle();

    public abstract String getTag();

    public abstract String getTitle();

    public abstract View getView();

    public boolean needStartProcess() {
        return false;
    }

    public boolean startActionByService() {
        return false;
    }

    public IQuickOpenItem(RectF rectF, Region region, Context context) {
        this.mContext = context;
        this.mRegion = region;
        this.mRectF = rectF;
    }

    public RectF getRect() {
        return this.mRectF;
    }

    public Region getRegion() {
        return this.mRegion;
    }
}
