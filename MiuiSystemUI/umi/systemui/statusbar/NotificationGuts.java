package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.R;
import com.android.systemui.Constants;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.util.AutoCleanFloatTransitionListener;
import java.util.Map;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.property.FloatProperty;

public class NotificationGuts extends FrameLayout {
    private static final boolean DEBUG = Constants.DEBUG;
    private int mActualHeight;
    private Drawable mBackground;
    private int mClipBottomAmount;
    private int mClipTopAmount;
    /* access modifiers changed from: private */
    public OnGutsClosedListener mClosedListener;
    /* access modifiers changed from: private */
    public boolean mExposed;
    private Runnable mFalsingCheck;
    private GutsContent mGutsContent;
    private Handler mHandler;
    private OnHeightChangedListener mHeightListener;
    /* access modifiers changed from: private */
    public boolean mIsAnimating;
    /* access modifiers changed from: private */
    public boolean mNeedsFalsingProtection;
    private int mPanelContentMargin;

    public interface GutsContent {
        int getActualHeight();

        View getContentView();

        boolean handleCloseControls(boolean z, boolean z2);

        boolean isLeavebehind();

        void setGutsParent(NotificationGuts notificationGuts);

        boolean willBeRemoved();
    }

    public interface OnGutsClosedListener {
        void onGutsCloseAnimationEnd();

        void onGutsClosed(NotificationGuts notificationGuts);
    }

    public interface OnHeightChangedListener {
        void onHeightChanged(NotificationGuts notificationGuts);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public NotificationGuts(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setWillNotDraw(false);
        this.mHandler = new Handler();
        this.mFalsingCheck = new Runnable() {
            public void run() {
                if (NotificationGuts.this.mNeedsFalsingProtection && NotificationGuts.this.mExposed) {
                    NotificationGuts.this.closeControls(-1, -1, false, false);
                }
            }
        };
        context.obtainStyledAttributes(attributeSet, R.styleable.Theme, 0, 0).recycle();
        this.mPanelContentMargin = context.getResources().getDimensionPixelSize(com.android.systemui.plugins.R.dimen.panel_content_margin);
    }

    public NotificationGuts(Context context) {
        this(context, (AttributeSet) null);
    }

    public void setGutsContent(GutsContent gutsContent) {
        this.mGutsContent = gutsContent;
        removeAllViews();
        addView(this.mGutsContent.getContentView());
    }

    public GutsContent getGutsContent() {
        return this.mGutsContent;
    }

    public void resetFalsingCheck() {
        this.mHandler.removeCallbacks(this.mFalsingCheck);
        if (this.mNeedsFalsingProtection && this.mExposed) {
            this.mHandler.postDelayed(this.mFalsingCheck, 8000);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        draw(canvas, this.mBackground);
    }

    private void draw(Canvas canvas, Drawable drawable) {
        int i = this.mClipTopAmount;
        int i2 = this.mActualHeight - this.mClipBottomAmount;
        if (drawable != null && i < i2) {
            drawable.setBounds(0, i, getWidth(), i2);
            drawable.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        Drawable drawable = this.mContext.getDrawable(com.android.systemui.plugins.R.drawable.notification_guts_bg);
        this.mBackground = drawable;
        if (drawable != null) {
            drawable.setCallback(this);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mBackground;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    private void drawableStateChanged(Drawable drawable) {
        if (drawable != null && drawable.isStateful()) {
            drawable.setState(getDrawableState());
        }
    }

    public void drawableHotspotChanged(float f, float f2) {
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setHotspot(f, f2);
        }
    }

    public void openControls(int i, int i2, boolean z, NotificationMenuRowPlugin.MenuItem menuItem) {
        animateOpen(i, i2, menuItem);
        setExposed(true, z);
    }

    public void closeControls(boolean z, boolean z2, int i, int i2, boolean z3) {
        GutsContent gutsContent = this.mGutsContent;
        if (gutsContent == null) {
            return;
        }
        if (gutsContent.isLeavebehind() && z) {
            closeControls(i, i2, true, z3);
        } else if (!this.mGutsContent.isLeavebehind() && z2) {
            closeControls(i, i2, true, z3);
        }
    }

    public void closeControls(int i, int i2, boolean z, boolean z2) {
        if (getWindowToken() == null) {
            OnGutsClosedListener onGutsClosedListener = this.mClosedListener;
            if (onGutsClosedListener != null) {
                onGutsClosedListener.onGutsClosed(this);
                this.mClosedListener.onGutsCloseAnimationEnd();
                return;
            }
            return;
        }
        GutsContent gutsContent = this.mGutsContent;
        if (gutsContent == null || !gutsContent.handleCloseControls(z, z2)) {
            animateClose(i, i2);
            setExposed(false, this.mNeedsFalsingProtection);
            OnGutsClosedListener onGutsClosedListener2 = this.mClosedListener;
            if (onGutsClosedListener2 != null) {
                onGutsClosedListener2.onGutsClosed(this);
            }
        }
    }

    private void animateOpen(int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        animateRow(true);
        animateGuts(menuItem, true);
        animateContent(true);
        animateMenuIcon(true);
    }

    private void animateClose(int i, int i2) {
        animateRow(false);
        animateGuts((NotificationMenuRowPlugin.MenuItem) null, false);
        animateContent(false);
        animateMenuIcon(false);
    }

    public boolean isAnimating() {
        return this.mIsAnimating;
    }

    private AnimConfig newSpringEase() {
        return newSpringEase(0.85f, 0.3f);
    }

    private AnimConfig newSpringEase(float f, float f2) {
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, f, f2);
        return animConfig;
    }

    private void animateRow(boolean z) {
        final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) getParent();
        float translation = expandableNotificationRow.getTranslation();
        float f = z ? (float) (-(expandableNotificationRow.getWidth() + this.mPanelContentMargin)) : 0.0f;
        if (DEBUG) {
            Log.d("NotificationGuts", "animateRow from=" + translation + ", to=" + f);
        }
        String folmeTarget = getFolmeTarget(z ? "RowOpen" : "RowClose");
        Folme.getValueTarget(folmeTarget).setMinVisibleChange(1.0f, "x");
        Folme.useValue(folmeTarget).setTo("x", Float.valueOf(translation)).addListener(new AutoCleanFloatTransitionListener(folmeTarget) {
            public void onUpdate(Map<String, Float> map) {
                expandableNotificationRow.setTranslation(map.get("x").floatValue());
            }
        }).to("x", Float.valueOf(f));
    }

    private void animateGuts(NotificationMenuRowPlugin.MenuItem menuItem, boolean z) {
        final boolean z2 = z;
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) getParent();
        NotificationMenuRowContainer notificationMenuRowContainer = (NotificationMenuRowContainer) expandableNotificationRow.getMenu().getMenuView();
        int menuIconSize = z2 ? notificationMenuRowContainer.getMenuIconSize() : expandableNotificationRow.getWidth();
        int width = z2 ? expandableNotificationRow.getWidth() : notificationMenuRowContainer.getMenuIconSize();
        int menuIconSize2 = z2 ? notificationMenuRowContainer.getMenuIconSize() : this.mActualHeight;
        int menuIconSize3 = z2 ? this.mActualHeight : notificationMenuRowContainer.getMenuIconSize();
        float f = 0.0f;
        float f2 = z2 ? 0.0f : 1.0f;
        float f3 = z2 ? 1.0f : 0.0f;
        float f4 = 0.7f;
        float f5 = z2 ? 0.7f : 1.0f;
        if (z2) {
            f4 = 1.0f;
        }
        float translationX = z2 ? menuItem.getMenuView().getTranslationX() : 0.0f;
        if (!z2) {
            f = (float) getWidth();
        }
        String folmeTarget = getFolmeTarget(z2 ? "GutsOpen" : "GutsClose");
        float f6 = f;
        float f7 = f4;
        float f8 = f3;
        Folme.getValueTarget(folmeTarget).setMinVisibleChange(1.0f, "width", "height", "x");
        int i = menuIconSize3;
        int i2 = width;
        Folme.getValueTarget(folmeTarget).setMinVisibleChange(0.01f, "alpha", "scale");
        IStateStyle addListener = Folme.useValue(folmeTarget).setTo("width", Integer.valueOf(menuIconSize), "height", Integer.valueOf(menuIconSize2), "alpha", Float.valueOf(f2), "scale", Float.valueOf(f5), "x", Float.valueOf(translationX)).setConfig(newSpringEase(0.85f, 0.4f), new FloatProperty[0]).addListener(new AutoCleanFloatTransitionListener(folmeTarget) {
            public void onBegin(Object obj) {
                boolean unused = NotificationGuts.this.mIsAnimating = true;
            }

            public void onStart() {
                if (z2) {
                    NotificationGuts.this.setVisibility(0);
                }
            }

            public void onUpdate(Map<String, Float> map) {
                int intValue = map.get("width").intValue();
                int intValue2 = map.get("height").intValue();
                float floatValue = map.get("alpha").floatValue();
                float floatValue2 = map.get("scale").floatValue();
                float floatValue3 = map.get("x").floatValue();
                ViewGroup.LayoutParams layoutParams = NotificationGuts.this.getLayoutParams();
                layoutParams.width = intValue;
                layoutParams.height = intValue2;
                NotificationGuts.this.requestLayout();
                NotificationGuts.this.setAlpha(floatValue);
                NotificationGuts.this.setScaleX(floatValue2);
                NotificationGuts.this.setScaleY(floatValue2);
                NotificationGuts.this.setTranslationX(floatValue3);
            }

            public void onEnd() {
                boolean unused = NotificationGuts.this.mIsAnimating = false;
                if (!z2) {
                    NotificationGuts.this.setVisibility(8);
                    if (NotificationGuts.this.mClosedListener != null) {
                        NotificationGuts.this.mClosedListener.onGutsCloseAnimationEnd();
                    }
                }
            }
        });
        Object[] objArr = new Object[3];
        objArr[0] = "width";
        objArr[1] = Integer.valueOf(i2);
        objArr[2] = z2 ? newSpringEase() : newSpringEase(0.99f, 0.15f);
        IStateStyle iStateStyle = addListener.to(objArr);
        Object[] objArr2 = new Object[3];
        objArr2[0] = "height";
        objArr2[1] = Integer.valueOf(i);
        objArr2[2] = z2 ? newSpringEase() : newSpringEase(0.9f, 0.4f);
        IStateStyle iStateStyle2 = iStateStyle.to(objArr2);
        Object[] objArr3 = new Object[3];
        objArr3[0] = "alpha";
        objArr3[1] = Float.valueOf(f8);
        objArr3[2] = z2 ? newSpringEase(0.99f, 0.6f) : newSpringEase(0.99f, 0.15f);
        IStateStyle iStateStyle3 = iStateStyle2.to(objArr3).to("scale", Float.valueOf(f7));
        Object[] objArr4 = new Object[3];
        objArr4[0] = "x";
        objArr4[1] = Float.valueOf(f6);
        objArr4[2] = z2 ? newSpringEase(0.99f, 0.6f) : newSpringEase(0.99f, 0.15f);
        iStateStyle3.to(objArr4);
    }

    private void animateContent(boolean z) {
        final View contentView = this.mGutsContent.getContentView();
        float f = 0.0f;
        float f2 = z ? 0.0f : 1.0f;
        if (z) {
            f = 1.0f;
        }
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(16, 0.4f);
        animConfig.setDelay(60);
        String folmeTarget = getFolmeTarget(z ? "ContentOpen" : "ContentClose");
        Folme.getValueTarget(folmeTarget).setMinVisibleChange(0.01f, "alpha");
        IStateStyle addListener = Folme.useValue(folmeTarget).setTo("alpha", Float.valueOf(f2)).addListener(new AutoCleanFloatTransitionListener(folmeTarget) {
            public void onUpdate(Map<String, Float> map) {
                contentView.setAlpha(map.get("alpha").floatValue());
            }
        });
        Object[] objArr = new Object[3];
        objArr[0] = "alpha";
        objArr[1] = Float.valueOf(f);
        if (!z) {
            animConfig = newSpringEase();
        }
        objArr[2] = animConfig;
        addListener.to(objArr);
    }

    private void animateMenuIcon(boolean z) {
        final View menuView = ((ExpandableNotificationRow) getParent()).getMenu().getMenuView();
        float f = 1.0f;
        float f2 = z ? 1.0f : 0.0f;
        if (z) {
            f = 0.0f;
        }
        String folmeTarget = getFolmeTarget(z ? "MenuIconOpen" : "MenuIconClose");
        Folme.getValueTarget(folmeTarget).setMinVisibleChange(0.01f, "alpha");
        Folme.useValue(folmeTarget).setTo("alpha", Float.valueOf(f2)).addListener(new AutoCleanFloatTransitionListener(folmeTarget) {
            public void onUpdate(Map<String, Float> map) {
                menuView.setAlpha(map.get("alpha").floatValue());
            }
        }).to("alpha", Float.valueOf(f));
    }

    public void setActualHeight(int i) {
        if (DEBUG) {
            Log.d("NotificationGuts", "setActualHeight " + i);
        }
        this.mActualHeight = i;
        invalidate();
    }

    public int getIntrinsicHeight() {
        GutsContent gutsContent = this.mGutsContent;
        int height = (gutsContent == null || !this.mExposed) ? getHeight() : gutsContent.getActualHeight();
        if (DEBUG) {
            Log.d("NotificationGuts", "getIntrinsicHeight height=" + height + ", mActualHeight=" + this.mActualHeight);
        }
        return this.mGutsContent instanceof NotificationSnooze ? height : this.mActualHeight;
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        invalidate();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        invalidate();
    }

    public void setClosedListener(OnGutsClosedListener onGutsClosedListener) {
        this.mClosedListener = onGutsClosedListener;
    }

    public void setHeightChangedListener(OnHeightChangedListener onHeightChangedListener) {
        this.mHeightListener = onHeightChangedListener;
    }

    /* access modifiers changed from: protected */
    public void onHeightChanged() {
        OnHeightChangedListener onHeightChangedListener = this.mHeightListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onHeightChanged(this);
        }
    }

    public void setExposed(boolean z, boolean z2) {
        GutsContent gutsContent;
        boolean z3 = this.mExposed;
        this.mExposed = z;
        this.mNeedsFalsingProtection = z2;
        if (!z || !z2) {
            this.mHandler.removeCallbacks(this.mFalsingCheck);
        } else {
            resetFalsingCheck();
        }
        if (z3 != this.mExposed && (gutsContent = this.mGutsContent) != null) {
            View contentView = gutsContent.getContentView();
            contentView.sendAccessibilityEvent(32);
            if (this.mExposed) {
                contentView.requestAccessibilityFocus();
            }
        }
    }

    public boolean willBeRemoved() {
        GutsContent gutsContent = this.mGutsContent;
        if (gutsContent != null) {
            return gutsContent.willBeRemoved();
        }
        return false;
    }

    public boolean isExposed() {
        return this.mExposed;
    }

    private String getFolmeTarget(String str) {
        return str + hashCode();
    }
}
