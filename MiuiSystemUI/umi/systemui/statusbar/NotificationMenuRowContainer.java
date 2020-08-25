package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import com.android.systemui.util.AutoCleanFloatTransitionListener;
import java.util.Map;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;

public class NotificationMenuRowContainer extends FrameLayout {
    private static final boolean DEBUG = Constants.DEBUG;
    private float mAlpha;
    private Rect mClipBounds;
    private int mMenuIconMargin;
    private int mMenuIconSize;
    private int mMenuIconSpace;
    private boolean mResetMenu;
    private float mTranslation;

    public NotificationMenuRowContainer(Context context) {
        this(context, (AttributeSet) null);
    }

    public NotificationMenuRowContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMenuIconSize = -1;
        this.mMenuIconSpace = -1;
        this.mMenuIconMargin = -1;
        this.mClipBounds = new Rect();
        new AnimConfig().setEase(-2, 0.9f, 0.2f);
        new AnimConfig().setEase(-2, 0.6f, 0.3f);
        init(context);
        setLayoutDirection(0);
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        this.mMenuIconSize = resources.getDimensionPixelSize(R.dimen.notification_menu_icon_size);
        this.mMenuIconSpace = resources.getDimensionPixelOffset(R.dimen.notification_menu_icon_lr_space);
        this.mMenuIconMargin = resources.getDimensionPixelOffset(R.dimen.notification_menu_icon_lr_margin);
    }

    public void addMenuView(View view) {
        addView(view);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int i = this.mMenuIconSize;
        layoutParams.width = i;
        layoutParams.height = i;
        view.setLayoutParams(layoutParams);
    }

    public void resetState() {
        if (DEBUG) {
            Log.d("MenuRowContainer", "resetState");
        }
        this.mResetMenu = true;
        resetMenuLocation();
        setMenuAlpha(0.0f);
    }

    public void resetMenuLocation() {
        if (DEBUG) {
            Log.d("MenuRowContainer", "resetMenuLocation");
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setX((float) getChildTranslationX(i));
        }
    }

    private int getChildTranslationX(int i) {
        return ((getWidth() - (this.mMenuIconSize * (i + 1))) - this.mMenuIconMargin) - (this.mMenuIconSpace * i);
    }

    public void onTranslationUpdate(float f) {
        if (DEBUG) {
            Log.d("MenuRowContainer", "onTranslationUpdate translation=" + f);
        }
        if (this.mResetMenu || f == this.mTranslation) {
            this.mResetMenu = false;
            return;
        }
        this.mTranslation = f;
        updateClipBounds();
        float f2 = 0.0f;
        if (f < 0.0f) {
            f2 = 1.0f;
        }
        setMenuAlpha(f2);
        animateIconTranslation();
    }

    private void animateIconTranslation() {
        if (!isFolmeAnimating()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                animateIconTranslation(i);
            }
        }
    }

    private boolean isFolmeAnimating() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).getTag(R.id.folme_animating_tag) != null) {
                return true;
            }
        }
        return false;
    }

    private void animateIconTranslation(int i) {
        float offsetX = getOffsetX(i);
        float range = getRange(i);
        float min = Math.min(offsetX / range, 1.0f);
        if (DEBUG) {
            Log.d("MenuRowContainer", String.format("animateIconTranslation childIndex=%d offset=%.1f range=%.1f per=%.1f", new Object[]{Integer.valueOf(i), Float.valueOf(offsetX), Float.valueOf(range), Float.valueOf(min)}));
        }
        float f = (0.19999999f * min) + 0.7f;
        float f2 = min * 0.80999994f;
        float movingX = getMovingX(i);
        if (DEBUG) {
            Log.d("MenuRowContainer", String.format("            toScale=%.1f toAlpha=%.1f toX=%.1f", new Object[]{Float.valueOf(f), Float.valueOf(f2), Float.valueOf(movingX)}));
        }
        animateIconTranslation2(i, f, f2, movingX);
    }

    private void animateIconTranslation2(int i, float f, float f2, float f3) {
        View childAt = getChildAt(i);
        childAt.setScaleX(f);
        childAt.setScaleY(f);
        childAt.setAlpha(f2);
        childAt.setTranslationX(f3);
    }

    public void handleShowMenu() {
        if (DEBUG) {
            Log.d("MenuRowContainer", "handleShowMenu");
        }
        this.mResetMenu = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            animateIconReset(i, getChildAt(i));
        }
    }

    private void animateIconReset(int i, View view) {
        float childTranslationX = (float) getChildTranslationX(i);
        if (DEBUG) {
            Log.d("MenuRowContainer", String.format("animateIconReset childIndex=%d scale=%.1f alpha=%.1f x=%.1f toX=%.1f", new Object[]{Integer.valueOf(i), Float.valueOf(view.getScaleX()), Float.valueOf(view.getAlpha()), Float.valueOf(view.getTranslationX()), Float.valueOf(childTranslationX)}));
        }
        final String str = "IconReset-" + i;
        Folme.getValueTarget(str).setMinVisibleChange(0.01f, "scale", "alpha");
        Folme.getValueTarget(str).setMinVisibleChange(1.0f, "x");
        final View view2 = view;
        Folme.useValue(str).setTo("scale", Float.valueOf(view.getScaleX()), "alpha", Float.valueOf(view.getAlpha()), "x", Float.valueOf(view.getTranslationX())).addListener(new AutoCleanFloatTransitionListener(str) {
            public void onStart() {
                view2.setTag(R.id.folme_animating_tag, str);
            }

            public void onUpdate(Map<String, Float> map) {
                float floatValue = getFloatValue("scale", view2.getScaleX());
                float floatValue2 = getFloatValue("alpha", view2.getAlpha());
                float floatValue3 = getFloatValue("x", view2.getTranslationX());
                view2.setScaleX(floatValue);
                view2.setScaleY(floatValue);
                view2.setAlpha(floatValue2);
                view2.setTranslationX(floatValue3);
            }

            public void onEnd() {
                view2.setTag(R.id.folme_animating_tag, (Object) null);
            }
        }).to("scale", Float.valueOf(1.0f)).to("alpha", Float.valueOf(1.0f)).to("x", Float.valueOf(childTranslationX));
    }

    private float getOffsetX(int i) {
        boolean z = true;
        if (getChildCount() == 1) {
            return -this.mTranslation;
        }
        int i2 = 0;
        if (i != 0) {
            z = false;
        }
        if (!z) {
            i2 = getWidth() - getChildTranslationX(i - 1);
        }
        return Math.min((float) (getWidth() - getStartMovingX(i)), Math.max((-this.mTranslation) - ((float) i2), 0.0f));
    }

    private float getRange(int i) {
        int i2;
        boolean z = true;
        int i3 = 0;
        if (getChildCount() == 1) {
            i2 = getWidth() - getStartMovingX(0);
        } else {
            int width = getWidth() - getStartMovingX(i - 1);
            int width2 = getWidth() - getStartMovingX(i);
            if (i != 0) {
                z = false;
            }
            int max = Math.max(width2 - width, 0);
            if (!z) {
                i3 = this.mMenuIconSpace;
            }
            i2 = max + i3;
        }
        return (float) i2;
    }

    private int getStartMovingX(int i) {
        if (i < 0) {
            return getWidth();
        }
        int childCount = getChildCount();
        boolean z = false;
        if (childCount == 1) {
            return getChildTranslationX(0) - this.mMenuIconMargin;
        }
        if (i == childCount - 1) {
            z = true;
        }
        return getChildTranslationX(i) - (z ? this.mMenuIconMargin : this.mMenuIconSpace);
    }

    private float getMovingX(int i) {
        return ((float) getChildTranslationX(i)) - (Math.max((-this.mTranslation) - ((float) getActualWidth()), 0.0f) / 2.0f);
    }

    private void setMenuAlpha(float f) {
        this.mAlpha = f;
        setVisibility(f == 0.0f ? 4 : 0);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setAlpha(this.mAlpha);
        }
    }

    private void updateClipBounds() {
        this.mClipBounds.set(this.mTranslation < 0.0f ? (int) (((float) getWidth()) + this.mTranslation) : getWidth(), 0, getWidth(), getHeight());
        setClipBounds(this.mClipBounds);
    }

    public float getMenuAlpha() {
        return this.mAlpha;
    }

    public int getMenuIconSize() {
        return this.mMenuIconSize;
    }

    public int getActualWidth() {
        int childCount = getChildCount();
        return (this.mMenuIconSize * childCount) + (this.mMenuIconMargin * 2) + Math.max(0, this.mMenuIconSpace * (childCount - 1));
    }
}
