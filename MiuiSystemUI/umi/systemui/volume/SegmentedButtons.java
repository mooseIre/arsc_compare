package com.android.systemui.volume;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class SegmentedButtons extends LinearLayout {
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", 0);
    private static final Typeface REGULAR = Typeface.create("sans-serif", 0);
    private final Context mContext;

    public SegmentedButtons(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        LayoutInflater.from(context);
        setOrientation(0);
        new ConfigurableTexts(this.mContext);
    }
}
