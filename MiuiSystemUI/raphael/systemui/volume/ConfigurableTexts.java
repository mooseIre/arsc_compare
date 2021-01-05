package com.android.systemui.volume;

import android.content.Context;
import android.content.res.Resources;
import android.util.ArrayMap;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.volume.Util;

public class ConfigurableTexts {
    private final Context mContext;
    /* access modifiers changed from: private */
    public final ArrayMap<TextView, Integer> mTextLabels = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final ArrayMap<TextView, Integer> mTexts = new ArrayMap<>();
    private final Runnable mUpdateAll = new Runnable() {
        public void run() {
            for (int i = 0; i < ConfigurableTexts.this.mTexts.size(); i++) {
                ConfigurableTexts configurableTexts = ConfigurableTexts.this;
                configurableTexts.setTextSizeH((TextView) configurableTexts.mTexts.keyAt(i), ((Integer) ConfigurableTexts.this.mTexts.valueAt(i)).intValue());
            }
            for (int i2 = 0; i2 < ConfigurableTexts.this.mTextLabels.size(); i2++) {
                ConfigurableTexts configurableTexts2 = ConfigurableTexts.this;
                configurableTexts2.setTextLabelH((TextView) configurableTexts2.mTextLabels.keyAt(i2), ((Integer) ConfigurableTexts.this.mTextLabels.valueAt(i2)).intValue());
            }
        }
    };

    public ConfigurableTexts(Context context) {
        this.mContext = context;
    }

    public int add(TextView textView) {
        return add(textView, -1);
    }

    public int add(final TextView textView, int i) {
        if (textView == null) {
            return 0;
        }
        if (this.mTexts.containsKey(textView)) {
            return this.mTexts.get(textView).intValue();
        }
        Resources resources = this.mContext.getResources();
        float f = resources.getConfiguration().fontScale;
        final int textSize = (int) ((textView.getTextSize() / f) / resources.getDisplayMetrics().density);
        this.mTexts.put(textView, Integer.valueOf(textSize));
        textView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewDetachedFromWindow(View view) {
            }

            public void onViewAttachedToWindow(View view) {
                ConfigurableTexts.this.setTextSizeH(textView, textSize);
            }
        });
        this.mTextLabels.put(textView, Integer.valueOf(i));
        return textSize;
    }

    public void remove(TextView textView) {
        this.mTexts.remove(textView);
        this.mTextLabels.remove(textView);
    }

    public void update() {
        if (!this.mTexts.isEmpty()) {
            this.mTexts.keyAt(0).post(this.mUpdateAll);
        }
    }

    /* access modifiers changed from: private */
    public void setTextSizeH(TextView textView, int i) {
        textView.setTextSize(2, (float) i);
    }

    /* access modifiers changed from: private */
    public void setTextLabelH(TextView textView, int i) {
        if (i >= 0) {
            try {
                Util.setText(textView, this.mContext.getString(i));
            } catch (Resources.NotFoundException unused) {
            }
        }
    }
}
