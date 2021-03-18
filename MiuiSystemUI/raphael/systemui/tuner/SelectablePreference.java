package com.android.systemui.tuner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import androidx.preference.CheckBoxPreference;
import com.android.systemui.C0017R$layout;
import com.android.systemui.statusbar.ScalingDrawableWrapper;

public class SelectablePreference extends CheckBoxPreference {
    private final int mSize;

    @Override // androidx.preference.Preference
    public String toString() {
        return "";
    }

    public SelectablePreference(Context context) {
        super(context);
        setWidgetLayoutResource(C0017R$layout.preference_widget_radiobutton);
        setSelectable(true);
        this.mSize = (int) TypedValue.applyDimension(1, 32.0f, context.getResources().getDisplayMetrics());
    }

    @Override // androidx.preference.Preference
    public void setIcon(Drawable drawable) {
        super.setIcon(new ScalingDrawableWrapper(drawable, ((float) this.mSize) / ((float) drawable.getIntrinsicWidth())));
    }
}
