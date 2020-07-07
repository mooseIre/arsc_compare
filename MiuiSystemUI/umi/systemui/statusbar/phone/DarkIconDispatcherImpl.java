package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.widget.ImageView;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.miui.systemui.annotation.Inject;

public class DarkIconDispatcherImpl implements DarkIconDispatcher {
    private float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    /* access modifiers changed from: private */
    public int mIconTint;
    private int mLightModeIconColorSingleTone;
    private final ArrayMap<Object, DarkIconDispatcher.DarkReceiver> mReceivers = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final Rect mTintArea = new Rect();
    private final LightBarTransitionsController mTransitionsController;
    private boolean mUseTint;

    public DarkIconDispatcherImpl(@Inject Context context) {
        this.mUseTint = context.getResources().getBoolean(R.bool.use_status_bar_tint);
        this.mDarkModeIconColorSingleTone = context.getColor(R.color.dark_mode_icon_color_single_tone);
        int color = context.getColor(R.color.light_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = color;
        this.mIconTint = color;
        LightBarTransitionsController lightBarTransitionsController = new LightBarTransitionsController(context, new LightBarTransitionsController.DarkIntensityApplier() {
            public void applyDarkIntensity(float f) {
                DarkIconDispatcherImpl.this.setIconTintInternal(f);
            }
        });
        this.mTransitionsController = lightBarTransitionsController;
        lightBarTransitionsController.setUseTint(this.mUseTint);
    }

    public LightBarTransitionsController getTransitionsController() {
        return this.mTransitionsController;
    }

    public void addDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.put(darkReceiver, darkReceiver);
        darkReceiver.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
    }

    public void addDarkReceiver(final ImageView imageView) {
        AnonymousClass2 r0 = new DarkIconDispatcher.DarkReceiver() {
            public void onDarkChanged(Rect rect, float f, int i) {
                imageView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(DarkIconDispatcherImpl.this.mTintArea, imageView, DarkIconDispatcherImpl.this.mIconTint)));
            }
        };
        this.mReceivers.put(imageView, r0);
        r0.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
    }

    public void removeDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.remove(darkReceiver);
    }

    public void removeDarkReceiver(ImageView imageView) {
        this.mReceivers.remove(imageView);
    }

    public void applyDark(Object obj) {
        DarkIconDispatcher.DarkReceiver darkReceiver = this.mReceivers.get(obj);
        if (darkReceiver != null) {
            darkReceiver.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
        }
    }

    public void updateResource(Context context) {
        this.mDarkModeIconColorSingleTone = context.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = context.getColor(R.color.light_mode_icon_color_single_tone);
        boolean z = context.getResources().getBoolean(R.bool.use_status_bar_tint);
        this.mUseTint = z;
        this.mTransitionsController.setUseTint(z);
        setIconTintInternal(this.mDarkIntensity);
    }

    public boolean useTint() {
        return this.mUseTint;
    }

    public void setIconsDarkArea(Rect rect) {
        if (rect != null || !this.mTintArea.isEmpty()) {
            if (rect == null) {
                this.mTintArea.setEmpty();
            } else {
                this.mTintArea.set(rect);
            }
            applyIconTint();
        }
    }

    /* access modifiers changed from: private */
    public void setIconTintInternal(float f) {
        this.mDarkIntensity = f;
        this.mIconTint = ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mLightModeIconColorSingleTone), Integer.valueOf(this.mDarkModeIconColorSingleTone))).intValue();
        applyIconTint();
    }

    private void applyIconTint() {
        for (int i = 0; i < this.mReceivers.size(); i++) {
            this.mReceivers.valueAt(i).onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint);
        }
    }

    public int getLightTintColor() {
        return this.mLightModeIconColorSingleTone;
    }
}
