package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class AutoReinflateContainer extends FrameLayout {
    private int mDensity;
    private final List<InflateListener> mInflateListeners = new ArrayList();
    private final int mLayout;
    private Object mLocaleList;

    public interface InflateListener {
        void onInflated(View view);
    }

    public AutoReinflateContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDensity = context.getResources().getConfiguration().densityDpi;
        this.mLocaleList = SystemUICompat.getLocales(context.getResources().getConfiguration());
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.AutoReinflateContainer);
        if (obtainStyledAttributes.hasValue(0)) {
            this.mLayout = obtainStyledAttributes.getResourceId(0, 0);
            inflateLayout();
            return;
        }
        throw new IllegalArgumentException("AutoReinflateContainer must contain a layout");
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        boolean z;
        super.onConfigurationChanged(configuration);
        int i = configuration.densityDpi;
        if (i != this.mDensity) {
            this.mDensity = i;
            z = true;
        } else {
            z = false;
        }
        Object locales = SystemUICompat.getLocales(configuration);
        if (locales != this.mLocaleList) {
            this.mLocaleList = locales;
            z = true;
        }
        if (z) {
            inflateLayout();
        }
    }

    /* access modifiers changed from: protected */
    public void inflateLayoutImpl() {
        LayoutInflater.from(getContext()).inflate(this.mLayout, this);
    }

    /* access modifiers changed from: protected */
    public void inflateLayout() {
        removeAllViews();
        inflateLayoutImpl();
        int size = this.mInflateListeners.size();
        for (int i = 0; i < size; i++) {
            this.mInflateListeners.get(i).onInflated(getChildAt(0));
        }
    }
}
