package com.android.systemui.volume;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Objects;

public class SegmentedButtons extends LinearLayout {
    private static final Typeface MEDIUM = Typeface.create("sans-serif-medium", 0);
    private static final Typeface REGULAR = Typeface.create("sans-serif", 0);
    private Callback mCallback;
    private final View.OnClickListener mClick = new View.OnClickListener() {
        public void onClick(View view) {
            SegmentedButtons.this.setSelectedValue(view.getTag(), true);
        }
    };
    private final ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    protected final LayoutInflater mInflater;
    protected Object mSelectedValue;

    public interface Callback extends Interaction$Callback {
        void onSelected(Object obj, boolean z);
    }

    public SegmentedButtons(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        setOrientation(0);
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
    }

    public void setSelectedValue(Object obj, boolean z) {
        if (!Objects.equals(obj, this.mSelectedValue)) {
            this.mSelectedValue = obj;
            for (int i = 0; i < getChildCount(); i++) {
                TextView textView = (TextView) getChildAt(i);
                boolean equals = Objects.equals(this.mSelectedValue, textView.getTag());
                textView.setSelected(equals);
                setSelectedStyle(textView, equals);
            }
            fireOnSelected(z);
        }
    }

    /* access modifiers changed from: protected */
    public void setSelectedStyle(TextView textView, boolean z) {
        textView.setTypeface(z ? MEDIUM : REGULAR);
    }

    private void fireOnSelected(boolean z) {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onSelected(this.mSelectedValue, z);
        }
    }
}
