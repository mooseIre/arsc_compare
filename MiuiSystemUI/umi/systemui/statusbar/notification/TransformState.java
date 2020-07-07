package com.android.systemui.statusbar.notification;

import android.util.ArraySet;
import android.util.Pools;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;

public class TransformState {
    private static Pools.SimplePool<TransformState> sInstancePool = new Pools.SimplePool<>(40);
    private int[] mOwnPosition = new int[2];
    private float mTransformationEndX = -1.0f;
    private float mTransformationEndY = -1.0f;
    protected View mTransformedView;

    /* access modifiers changed from: protected */
    public boolean sameAs(TransformState transformState) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean transformScale() {
        return false;
    }

    public void initFrom(View view) {
        this.mTransformedView = view;
    }

    public void transformViewFrom(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (!sameAs(transformState)) {
            CrossFadeHelper.fadeIn(this.mTransformedView, f);
        } else if (this.mTransformedView.getVisibility() == 4 || this.mTransformedView.getAlpha() != 1.0f) {
            this.mTransformedView.setAlpha(1.0f);
            this.mTransformedView.setVisibility(0);
        }
        transformViewFullyFrom(transformState, f);
    }

    public void transformViewFullyFrom(TransformState transformState, float f) {
        transformViewFrom(transformState, 17, (ViewTransformationHelper.CustomTransformation) null, f);
    }

    public void transformViewFullyFrom(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewFrom(transformState, 17, customTransformation, f);
    }

    public void transformViewVerticalFrom(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewFrom(transformState, 16, customTransformation, f);
    }

    public void transformViewVerticalFrom(TransformState transformState, float f) {
        transformViewFrom(transformState, 16, (ViewTransformationHelper.CustomTransformation) null, f);
    }

    private void transformViewFrom(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        Interpolator customInterpolator;
        Interpolator customInterpolator2;
        int[] iArr;
        View view = this.mTransformedView;
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 16) != 0;
        boolean transformScale = transformScale();
        float f2 = 0.0f;
        int i2 = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        if (i2 == 0 || ((z && getTransformationStartX() == -1.0f) || ((z2 && getTransformationStartY() == -1.0f) || ((transformScale && getTransformationStartScaleX() == -1.0f) || (transformScale && getTransformationStartScaleY() == -1.0f))))) {
            if (i2 != 0) {
                iArr = transformState.getLaidOutLocationOnScreen();
            } else {
                iArr = transformState.getLocationOnScreen();
            }
            int[] laidOutLocationOnScreen = getLaidOutLocationOnScreen();
            if (customTransformation == null || !customTransformation.initTransformation(this, transformState)) {
                if (z) {
                    setTransformationStartX((float) (iArr[0] - laidOutLocationOnScreen[0]));
                }
                if (z2) {
                    setTransformationStartY((float) (iArr[1] - laidOutLocationOnScreen[1]));
                }
                View transformedView = transformState.getTransformedView();
                if (!transformScale || transformedView.getWidth() == view.getWidth() || view.getWidth() == 0) {
                    setTransformationStartScaleX(-1.0f);
                } else {
                    setTransformationStartScaleX((((float) transformedView.getWidth()) * transformedView.getScaleX()) / ((float) view.getWidth()));
                    view.setPivotX(0.0f);
                }
                if (!transformScale || transformedView.getHeight() == view.getHeight() || view.getHeight() == 0) {
                    setTransformationStartScaleY(-1.0f);
                } else {
                    setTransformationStartScaleY((((float) transformedView.getHeight()) * transformedView.getScaleY()) / ((float) view.getHeight()));
                    view.setPivotY(0.0f);
                }
            }
            if (!z) {
                setTransformationStartX(-1.0f);
            }
            if (!z2) {
                setTransformationStartY(-1.0f);
            }
            if (!transformScale) {
                setTransformationStartScaleX(-1.0f);
                setTransformationStartScaleY(-1.0f);
            }
            setClippingDeactivated(view, true);
        }
        float interpolation = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(f);
        if (z) {
            view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), 0.0f, (customTransformation == null || (customInterpolator2 = customTransformation.getCustomInterpolator(1, true)) == null) ? interpolation : customInterpolator2.getInterpolation(f)));
        }
        if (z2) {
            view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), 0.0f, (customTransformation == null || (customInterpolator = customTransformation.getCustomInterpolator(16, true)) == null) ? interpolation : customInterpolator.getInterpolation(f)));
        }
        if (transformScale) {
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                float interpolate = NotificationUtils.interpolate(transformationStartScaleX, 1.0f, interpolation);
                if (!Float.isFinite(interpolate)) {
                    interpolate = 0.0f;
                }
                view.setScaleX(interpolate);
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                float interpolate2 = NotificationUtils.interpolate(transformationStartScaleY, 1.0f, interpolation);
                if (Float.isFinite(interpolate2)) {
                    f2 = interpolate2;
                }
                view.setScaleY(f2);
            }
        }
    }

    public boolean transformViewTo(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (!sameAs(transformState)) {
            CrossFadeHelper.fadeOut(this.mTransformedView, f);
            transformViewFullyTo(transformState, f);
            return true;
        } else if (this.mTransformedView.getVisibility() != 0) {
            return false;
        } else {
            this.mTransformedView.setAlpha(0.0f);
            this.mTransformedView.setVisibility(4);
            return false;
        }
    }

    public void transformViewFullyTo(TransformState transformState, float f) {
        transformViewTo(transformState, 17, (ViewTransformationHelper.CustomTransformation) null, f);
    }

    public void transformViewFullyTo(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewTo(transformState, 17, customTransformation, f);
    }

    public void transformViewVerticalTo(TransformState transformState, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        transformViewTo(transformState, 16, customTransformation, f);
    }

    public void transformViewVerticalTo(TransformState transformState, float f) {
        transformViewTo(transformState, 16, (ViewTransformationHelper.CustomTransformation) null, f);
    }

    private void transformViewTo(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        float f2;
        float f3;
        TransformState transformState2 = transformState;
        ViewTransformationHelper.CustomTransformation customTransformation2 = customTransformation;
        float f4 = f;
        View view = this.mTransformedView;
        boolean z = (i & 1) != 0;
        boolean z2 = (i & 16) != 0;
        boolean transformScale = transformScale();
        if (f4 == 0.0f) {
            if (z) {
                float transformationStartX = getTransformationStartX();
                if (transformationStartX == -1.0f) {
                    transformationStartX = view.getTranslationX();
                }
                setTransformationStartX(transformationStartX);
            }
            if (z2) {
                float transformationStartY = getTransformationStartY();
                if (transformationStartY == -1.0f) {
                    transformationStartY = view.getTranslationY();
                }
                setTransformationStartY(transformationStartY);
            }
            View transformedView = transformState.getTransformedView();
            if (!transformScale || transformedView.getWidth() == view.getWidth()) {
                setTransformationStartScaleX(-1.0f);
            } else {
                setTransformationStartScaleX(view.getScaleX());
                view.setPivotX(0.0f);
            }
            if (!transformScale || transformedView.getHeight() == view.getHeight()) {
                setTransformationStartScaleY(-1.0f);
            } else {
                setTransformationStartScaleY(view.getScaleY());
                view.setPivotY(0.0f);
            }
            setClippingDeactivated(view, true);
        }
        float interpolation = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(f4);
        int[] laidOutLocationOnScreen = transformState.getLaidOutLocationOnScreen();
        int[] laidOutLocationOnScreen2 = getLaidOutLocationOnScreen();
        if (z) {
            float f5 = (float) (laidOutLocationOnScreen[0] - laidOutLocationOnScreen2[0]);
            if (customTransformation2 != null) {
                if (customTransformation2.customTransformTarget(this, transformState2)) {
                    f5 = this.mTransformationEndX;
                }
                Interpolator customInterpolator = customTransformation2.getCustomInterpolator(1, false);
                if (customInterpolator != null) {
                    f3 = customInterpolator.getInterpolation(f4);
                    view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), f5, f3));
                }
            }
            f3 = interpolation;
            view.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), f5, f3));
        }
        if (z2) {
            float f6 = (float) (laidOutLocationOnScreen[1] - laidOutLocationOnScreen2[1]);
            if (customTransformation2 != null) {
                if (customTransformation2.customTransformTarget(this, transformState2)) {
                    f6 = this.mTransformationEndY;
                }
                Interpolator customInterpolator2 = customTransformation2.getCustomInterpolator(16, false);
                if (customInterpolator2 != null) {
                    f2 = customInterpolator2.getInterpolation(f4);
                    view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), f6, f2));
                }
            }
            f2 = interpolation;
            view.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), f6, f2));
        }
        if (transformScale) {
            View transformedView2 = transformState.getTransformedView();
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                float interpolate = NotificationUtils.interpolate(transformationStartScaleX, ((float) transformedView2.getWidth()) / ((float) view.getWidth()), interpolation);
                if (!Float.isFinite(interpolate)) {
                    interpolate = 0.0f;
                }
                view.setScaleX(interpolate);
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                float interpolate2 = NotificationUtils.interpolate(transformationStartScaleY, ((float) transformedView2.getHeight()) / ((float) view.getHeight()), interpolation);
                view.setScaleY(!Float.isFinite(interpolate2) ? 0.0f : interpolate2);
            }
        }
    }

    public static void setClippingDeactivated(View view, boolean z) {
        if (view.getParent() instanceof ViewGroup) {
            ViewParent parent = view.getParent();
            while (true) {
                ViewGroup viewGroup = (ViewGroup) parent;
                ArraySet arraySet = (ArraySet) viewGroup.getTag(R.id.clip_children_set_tag);
                if (arraySet == null) {
                    arraySet = new ArraySet();
                    viewGroup.setTag(R.id.clip_children_set_tag, arraySet);
                }
                Boolean bool = (Boolean) viewGroup.getTag(R.id.clip_children_tag);
                if (bool == null) {
                    bool = Boolean.valueOf(viewGroup.getClipChildren());
                    viewGroup.setTag(R.id.clip_children_tag, bool);
                }
                Boolean bool2 = (Boolean) viewGroup.getTag(R.id.clip_to_padding_tag);
                if (bool2 == null) {
                    bool2 = Boolean.valueOf(viewGroup.getClipToPadding());
                    viewGroup.setTag(R.id.clip_to_padding_tag, bool2);
                }
                ExpandableNotificationRow expandableNotificationRow = viewGroup instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) viewGroup : null;
                if (!z) {
                    arraySet.remove(view);
                    if (arraySet.isEmpty()) {
                        viewGroup.setClipChildren(bool.booleanValue());
                        viewGroup.setClipToPadding(bool2.booleanValue());
                        viewGroup.setTag(R.id.clip_children_set_tag, (Object) null);
                        if (expandableNotificationRow != null) {
                            expandableNotificationRow.setClipToActualHeight(true);
                        }
                    }
                } else {
                    arraySet.add(view);
                    viewGroup.setClipChildren(false);
                    viewGroup.setClipToPadding(false);
                    if (expandableNotificationRow != null && expandableNotificationRow.isChildInGroup()) {
                        expandableNotificationRow.setClipToActualHeight(false);
                    }
                }
                if (expandableNotificationRow == null || expandableNotificationRow.isChildInGroup()) {
                    parent = viewGroup.getParent();
                    if (!(parent instanceof ViewGroup)) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    public int[] getLaidOutLocationOnScreen() {
        int[] locationOnScreen = getLocationOnScreen();
        locationOnScreen[0] = (int) (((float) locationOnScreen[0]) - this.mTransformedView.getTranslationX());
        locationOnScreen[1] = (int) (((float) locationOnScreen[1]) - this.mTransformedView.getTranslationY());
        return locationOnScreen;
    }

    public int[] getLocationOnScreen() {
        this.mTransformedView.getLocationOnScreen(this.mOwnPosition);
        int[] iArr = this.mOwnPosition;
        iArr[0] = (int) (((float) iArr[0]) - ((1.0f - this.mTransformedView.getScaleX()) * this.mTransformedView.getPivotX()));
        int[] iArr2 = this.mOwnPosition;
        iArr2[1] = (int) (((float) iArr2[1]) - ((1.0f - this.mTransformedView.getScaleY()) * this.mTransformedView.getPivotY()));
        return this.mOwnPosition;
    }

    public void appear(float f, TransformableView transformableView) {
        if (f == 0.0f) {
            prepareFadeIn();
        }
        CrossFadeHelper.fadeIn(this.mTransformedView, f);
    }

    public void disappear(float f, TransformableView transformableView) {
        CrossFadeHelper.fadeOut(this.mTransformedView, f);
    }

    public static TransformState createFrom(View view) {
        if (view instanceof TextView) {
            TextViewTransformState obtain = TextViewTransformState.obtain();
            obtain.initFrom(view);
            return obtain;
        } else if (view.getId() == 16908724) {
            ActionListTransformState obtain2 = ActionListTransformState.obtain();
            obtain2.initFrom(view);
            return obtain2;
        } else if (view instanceof ImageView) {
            ImageTransformState obtain3 = ImageTransformState.obtain();
            obtain3.initFrom(view);
            return obtain3;
        } else if (view instanceof ProgressBar) {
            ProgressTransformState obtain4 = ProgressTransformState.obtain();
            obtain4.initFrom(view);
            return obtain4;
        } else {
            TransformState obtain5 = obtain();
            obtain5.initFrom(view);
            return obtain5;
        }
    }

    public void recycle() {
        reset();
        if (getClass() == TransformState.class) {
            sInstancePool.release(this);
        }
    }

    public void setTransformationEndY(float f) {
        this.mTransformationEndY = f;
    }

    public float getTransformationStartX() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_x_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartY() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_y_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleX() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_scale_x_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleY() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_scale_y_tag);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public void setTransformationStartX(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_x_tag, Float.valueOf(f));
    }

    public void setTransformationStartY(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_y_tag, Float.valueOf(f));
    }

    private void setTransformationStartScaleX(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_scale_x_tag, Float.valueOf(f));
    }

    private void setTransformationStartScaleY(float f) {
        this.mTransformedView.setTag(R.id.transformation_start_scale_y_tag, Float.valueOf(f));
    }

    /* access modifiers changed from: protected */
    public void reset() {
        this.mTransformedView = null;
        this.mTransformationEndX = -1.0f;
        this.mTransformationEndY = -1.0f;
    }

    public void setVisible(boolean z, boolean z2) {
        if (z2 || this.mTransformedView.getVisibility() != 8) {
            if (this.mTransformedView.getVisibility() != 8) {
                this.mTransformedView.setVisibility(z ? 0 : 4);
            }
            this.mTransformedView.animate().cancel();
            this.mTransformedView.setAlpha(z ? 1.0f : 0.0f);
            resetTransformedView();
        }
    }

    public void prepareFadeIn() {
        resetTransformedView();
    }

    /* access modifiers changed from: protected */
    public void resetTransformedView() {
        this.mTransformedView.setTranslationX(0.0f);
        this.mTransformedView.setTranslationY(0.0f);
        this.mTransformedView.setScaleX(1.0f);
        this.mTransformedView.setScaleY(1.0f);
        setClippingDeactivated(this.mTransformedView, false);
        abortTransformation();
    }

    public void abortTransformation() {
        View view = this.mTransformedView;
        Float valueOf = Float.valueOf(-1.0f);
        view.setTag(R.id.transformation_start_x_tag, valueOf);
        this.mTransformedView.setTag(R.id.transformation_start_y_tag, valueOf);
        this.mTransformedView.setTag(R.id.transformation_start_scale_x_tag, valueOf);
        this.mTransformedView.setTag(R.id.transformation_start_scale_y_tag, valueOf);
    }

    public static TransformState obtain() {
        TransformState transformState = (TransformState) sInstancePool.acquire();
        if (transformState != null) {
            return transformState;
        }
        return new TransformState();
    }

    public View getTransformedView() {
        return this.mTransformedView;
    }
}
