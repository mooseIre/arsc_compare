package com.android.systemui.statusbar.notification;

import android.util.Pools;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.widget.MessagingImageMessage;
import com.android.internal.widget.MessagingPropertyAnimator;
import com.android.internal.widget.ViewClippingUtil;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class TransformState {
    private static ViewClippingUtil.ClippingParameters CLIPPING_PARAMETERS = new ViewClippingUtil.ClippingParameters() {
        public boolean shouldFinish(View view) {
            if (view instanceof ExpandableNotificationRow) {
                return !((ExpandableNotificationRow) view).isChildInGroup();
            }
            return false;
        }

        public void onClippingStateChanged(View view, boolean z) {
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                if (z) {
                    expandableNotificationRow.setClipToActualHeight(true);
                } else if (expandableNotificationRow.isChildInGroup()) {
                    expandableNotificationRow.setClipToActualHeight(false);
                }
            }
        }
    };
    private static final int TRANSFORMATION_START_SCLALE_X = C0015R$id.transformation_start_scale_x_tag;
    private static final int TRANSFORMATION_START_SCLALE_Y = C0015R$id.transformation_start_scale_y_tag;
    private static final int TRANSFORMATION_START_X = C0015R$id.transformation_start_x_tag;
    private static final int TRANSFORMATION_START_Y = C0015R$id.transformation_start_y_tag;
    private static Pools.SimplePool<TransformState> sInstancePool = new Pools.SimplePool<>(40);
    protected Interpolator mDefaultInterpolator = Interpolators.FAST_OUT_SLOW_IN;
    private int[] mOwnPosition = new int[2];
    private boolean mSameAsAny;
    protected TransformInfo mTransformInfo;
    private float mTransformationEndX = -1.0f;
    private float mTransformationEndY = -1.0f;
    protected View mTransformedView;

    public interface TransformInfo {
        boolean isAnimating();
    }

    public void initFrom(View view, TransformInfo transformInfo) {
        this.mTransformedView = view;
        this.mTransformInfo = transformInfo;
    }

    public void transformViewFrom(TransformState transformState, float f) {
        this.mTransformedView.animate().cancel();
        if (sameAs(transformState)) {
            ensureVisible();
        } else {
            CrossFadeHelper.fadeIn(this.mTransformedView, f, true);
        }
        transformViewFullyFrom(transformState, f);
    }

    public void ensureVisible() {
        if (this.mTransformedView.getVisibility() == 4 || this.mTransformedView.getAlpha() != 1.0f) {
            this.mTransformedView.setAlpha(1.0f);
            this.mTransformedView.setVisibility(0);
        }
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

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00e9  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00ee  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00f3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void transformViewFrom(com.android.systemui.statusbar.notification.TransformState r20, int r21, com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation r22, float r23) {
        /*
            r19 = this;
            r0 = r19
            r1 = r22
            r2 = r23
            android.view.View r3 = r0.mTransformedView
            r4 = r21 & 1
            r5 = 0
            r6 = 1
            if (r4 == 0) goto L_0x0010
            r4 = r6
            goto L_0x0011
        L_0x0010:
            r4 = r5
        L_0x0011:
            r7 = 16
            r8 = r21 & 16
            if (r8 == 0) goto L_0x0019
            r8 = r6
            goto L_0x001a
        L_0x0019:
            r8 = r5
        L_0x001a:
            int r9 = r19.getViewHeight()
            int r10 = r20.getViewHeight()
            if (r10 == r9) goto L_0x002a
            if (r10 == 0) goto L_0x002a
            if (r9 == 0) goto L_0x002a
            r11 = r6
            goto L_0x002b
        L_0x002a:
            r11 = r5
        L_0x002b:
            int r12 = r19.getViewWidth()
            int r13 = r20.getViewWidth()
            if (r13 == r12) goto L_0x003b
            if (r13 == 0) goto L_0x003b
            if (r12 == 0) goto L_0x003b
            r14 = r6
            goto L_0x003c
        L_0x003b:
            r14 = r5
        L_0x003c:
            boolean r15 = r19.transformScale(r20)
            if (r15 == 0) goto L_0x0048
            if (r11 != 0) goto L_0x0046
            if (r14 == 0) goto L_0x0048
        L_0x0046:
            r15 = r6
            goto L_0x0049
        L_0x0048:
            r15 = r5
        L_0x0049:
            r7 = 0
            int r16 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
            r7 = -1082130432(0xffffffffbf800000, float:-1.0)
            if (r16 == 0) goto L_0x007c
            if (r4 == 0) goto L_0x005a
            float r17 = r19.getTransformationStartX()
            int r17 = (r17 > r7 ? 1 : (r17 == r7 ? 0 : -1))
            if (r17 == 0) goto L_0x007c
        L_0x005a:
            if (r8 == 0) goto L_0x0064
            float r17 = r19.getTransformationStartY()
            int r17 = (r17 > r7 ? 1 : (r17 == r7 ? 0 : -1))
            if (r17 == 0) goto L_0x007c
        L_0x0064:
            if (r15 == 0) goto L_0x0070
            float r17 = r19.getTransformationStartScaleX()
            int r17 = (r17 > r7 ? 1 : (r17 == r7 ? 0 : -1))
            if (r17 != 0) goto L_0x0070
            if (r14 != 0) goto L_0x007c
        L_0x0070:
            if (r15 == 0) goto L_0x00fc
            float r17 = r19.getTransformationStartScaleY()
            int r17 = (r17 > r7 ? 1 : (r17 == r7 ? 0 : -1))
            if (r17 != 0) goto L_0x00fc
            if (r11 == 0) goto L_0x00fc
        L_0x007c:
            if (r16 == 0) goto L_0x0083
            int[] r16 = r20.getLaidOutLocationOnScreen()
            goto L_0x0087
        L_0x0083:
            int[] r16 = r20.getLocationOnScreen()
        L_0x0087:
            int[] r17 = r19.getLaidOutLocationOnScreen()
            r7 = r20
            if (r1 == 0) goto L_0x0099
            boolean r18 = r1.initTransformation(r0, r7)
            if (r18 != 0) goto L_0x0096
            goto L_0x0099
        L_0x0096:
            r5 = -1082130432(0xffffffffbf800000, float:-1.0)
            goto L_0x00e7
        L_0x0099:
            if (r4 == 0) goto L_0x00a5
            r18 = r16[r5]
            r5 = r17[r5]
            int r5 = r18 - r5
            float r5 = (float) r5
            r0.setTransformationStartX(r5)
        L_0x00a5:
            if (r8 == 0) goto L_0x00b1
            r5 = r16[r6]
            r16 = r17[r6]
            int r5 = r5 - r16
            float r5 = (float) r5
            r0.setTransformationStartY(r5)
        L_0x00b1:
            android.view.View r5 = r20.getTransformedView()
            if (r15 == 0) goto L_0x00c9
            if (r14 == 0) goto L_0x00c9
            float r7 = (float) r13
            float r13 = r5.getScaleX()
            float r7 = r7 * r13
            float r12 = (float) r12
            float r7 = r7 / r12
            r0.setTransformationStartScaleX(r7)
            r7 = 0
            r3.setPivotX(r7)
            goto L_0x00ce
        L_0x00c9:
            r7 = -1082130432(0xffffffffbf800000, float:-1.0)
            r0.setTransformationStartScaleX(r7)
        L_0x00ce:
            if (r15 == 0) goto L_0x00e2
            if (r11 == 0) goto L_0x00e2
            float r7 = (float) r10
            float r5 = r5.getScaleY()
            float r7 = r7 * r5
            float r5 = (float) r9
            float r7 = r7 / r5
            r0.setTransformationStartScaleY(r7)
            r5 = 0
            r3.setPivotY(r5)
            goto L_0x0096
        L_0x00e2:
            r5 = -1082130432(0xffffffffbf800000, float:-1.0)
            r0.setTransformationStartScaleY(r5)
        L_0x00e7:
            if (r4 != 0) goto L_0x00ec
            r0.setTransformationStartX(r5)
        L_0x00ec:
            if (r8 != 0) goto L_0x00f1
            r0.setTransformationStartY(r5)
        L_0x00f1:
            if (r15 != 0) goto L_0x00f9
            r0.setTransformationStartScaleX(r5)
            r0.setTransformationStartScaleY(r5)
        L_0x00f9:
            r0.setClippingDeactivated(r3, r6)
        L_0x00fc:
            android.view.animation.Interpolator r5 = r0.mDefaultInterpolator
            float r5 = r5.getInterpolation(r2)
            if (r4 == 0) goto L_0x011e
            if (r1 == 0) goto L_0x0111
            android.view.animation.Interpolator r4 = r1.getCustomInterpolator(r6, r6)
            if (r4 == 0) goto L_0x0111
            float r4 = r4.getInterpolation(r2)
            goto L_0x0112
        L_0x0111:
            r4 = r5
        L_0x0112:
            float r7 = r19.getTransformationStartX()
            r9 = 0
            float r4 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r7, r9, r4)
            r3.setTranslationX(r4)
        L_0x011e:
            if (r8 == 0) goto L_0x013c
            if (r1 == 0) goto L_0x012f
            r4 = 16
            android.view.animation.Interpolator r1 = r1.getCustomInterpolator(r4, r6)
            if (r1 == 0) goto L_0x012f
            float r1 = r1.getInterpolation(r2)
            goto L_0x0130
        L_0x012f:
            r1 = r5
        L_0x0130:
            float r2 = r19.getTransformationStartY()
            r4 = 0
            float r1 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r2, r4, r1)
            r3.setTranslationY(r1)
        L_0x013c:
            if (r15 == 0) goto L_0x0160
            float r1 = r19.getTransformationStartScaleX()
            r2 = -1082130432(0xffffffffbf800000, float:-1.0)
            int r4 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            r6 = 1065353216(0x3f800000, float:1.0)
            if (r4 == 0) goto L_0x0151
            float r1 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r1, r6, r5)
            r3.setScaleX(r1)
        L_0x0151:
            float r0 = r19.getTransformationStartScaleY()
            int r1 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r1 == 0) goto L_0x0160
            float r0 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r0, r6, r5)
            r3.setScaleY(r0)
        L_0x0160:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.TransformState.transformViewFrom(com.android.systemui.statusbar.notification.TransformState, int, com.android.systemui.statusbar.ViewTransformationHelper$CustomTransformation, float):void");
    }

    /* access modifiers changed from: protected */
    public int getViewWidth() {
        return this.mTransformedView.getWidth();
    }

    /* access modifiers changed from: protected */
    public int getViewHeight() {
        return this.mTransformedView.getHeight();
    }

    /* access modifiers changed from: protected */
    public boolean transformScale(TransformState transformState) {
        return sameAs(transformState);
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
        boolean transformScale = transformScale(transformState);
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
            transformState.getTransformedView();
            if (!transformScale || transformState.getViewWidth() == getViewWidth()) {
                setTransformationStartScaleX(-1.0f);
            } else {
                setTransformationStartScaleX(view.getScaleX());
                view.setPivotX(0.0f);
            }
            if (!transformScale || transformState.getViewHeight() == getViewHeight()) {
                setTransformationStartScaleY(-1.0f);
            } else {
                setTransformationStartScaleY(view.getScaleY());
                view.setPivotY(0.0f);
            }
            setClippingDeactivated(view, true);
        }
        float interpolation = this.mDefaultInterpolator.getInterpolation(f4);
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
            transformState.getTransformedView();
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                view.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, ((float) transformState.getViewWidth()) / ((float) getViewWidth()), interpolation));
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                view.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, ((float) transformState.getViewHeight()) / ((float) getViewHeight()), interpolation));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setClippingDeactivated(View view, boolean z) {
        ViewClippingUtil.setClippingDeactivated(view, z, CLIPPING_PARAMETERS);
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
        int[] iArr3 = this.mOwnPosition;
        iArr3[1] = iArr3[1] - (MessagingPropertyAnimator.getTop(this.mTransformedView) - MessagingPropertyAnimator.getLayoutTop(this.mTransformedView));
        return this.mOwnPosition;
    }

    /* access modifiers changed from: protected */
    public boolean sameAs(TransformState transformState) {
        return this.mSameAsAny;
    }

    public void appear(float f, TransformableView transformableView) {
        if (f == 0.0f) {
            prepareFadeIn();
        }
        CrossFadeHelper.fadeIn(this.mTransformedView, f, true);
    }

    public void disappear(float f, TransformableView transformableView) {
        CrossFadeHelper.fadeOut(this.mTransformedView, f);
    }

    public static TransformState createFrom(View view, TransformInfo transformInfo) {
        if (view instanceof TextView) {
            TextViewTransformState obtain = TextViewTransformState.obtain();
            obtain.initFrom(view, transformInfo);
            return obtain;
        } else if (view.getId() == 16908724) {
            ActionListTransformState obtain2 = ActionListTransformState.obtain();
            obtain2.initFrom(view, transformInfo);
            return obtain2;
        } else if (view.getId() == 16909252) {
            MessagingLayoutTransformState obtain3 = MessagingLayoutTransformState.obtain();
            obtain3.initFrom(view, transformInfo);
            return obtain3;
        } else if (view instanceof MessagingImageMessage) {
            MessagingImageTransformState obtain4 = MessagingImageTransformState.obtain();
            obtain4.initFrom(view, transformInfo);
            return obtain4;
        } else if (view instanceof ImageView) {
            ImageTransformState obtain5 = ImageTransformState.obtain();
            obtain5.initFrom(view, transformInfo);
            if (view.getId() == 16909368) {
                obtain5.setIsSameAsAnyView(true);
            }
            return obtain5;
        } else if (view instanceof ProgressBar) {
            ProgressTransformState obtain6 = ProgressTransformState.obtain();
            obtain6.initFrom(view, transformInfo);
            return obtain6;
        } else {
            TransformState obtain7 = obtain();
            obtain7.initFrom(view, transformInfo);
            return obtain7;
        }
    }

    public void setIsSameAsAnyView(boolean z) {
        this.mSameAsAny = z;
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
        Object tag = this.mTransformedView.getTag(TRANSFORMATION_START_X);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartY() {
        Object tag = this.mTransformedView.getTag(TRANSFORMATION_START_Y);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleX() {
        Object tag = this.mTransformedView.getTag(TRANSFORMATION_START_SCLALE_X);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleY() {
        Object tag = this.mTransformedView.getTag(TRANSFORMATION_START_SCLALE_Y);
        if (tag == null) {
            return -1.0f;
        }
        return ((Float) tag).floatValue();
    }

    public void setTransformationStartX(float f) {
        this.mTransformedView.setTag(TRANSFORMATION_START_X, Float.valueOf(f));
    }

    public void setTransformationStartY(float f) {
        this.mTransformedView.setTag(TRANSFORMATION_START_Y, Float.valueOf(f));
    }

    private void setTransformationStartScaleX(float f) {
        this.mTransformedView.setTag(TRANSFORMATION_START_SCLALE_X, Float.valueOf(f));
    }

    private void setTransformationStartScaleY(float f) {
        this.mTransformedView.setTag(TRANSFORMATION_START_SCLALE_Y, Float.valueOf(f));
    }

    /* access modifiers changed from: protected */
    public void reset() {
        this.mTransformedView = null;
        this.mTransformInfo = null;
        this.mSameAsAny = false;
        this.mTransformationEndX = -1.0f;
        this.mTransformationEndY = -1.0f;
        this.mDefaultInterpolator = Interpolators.FAST_OUT_SLOW_IN;
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
        int i = TRANSFORMATION_START_X;
        Float valueOf = Float.valueOf(-1.0f);
        view.setTag(i, valueOf);
        this.mTransformedView.setTag(TRANSFORMATION_START_Y, valueOf);
        this.mTransformedView.setTag(TRANSFORMATION_START_SCLALE_X, valueOf);
        this.mTransformedView.setTag(TRANSFORMATION_START_SCLALE_Y, valueOf);
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

    public void setDefaultInterpolator(Interpolator interpolator) {
        this.mDefaultInterpolator = interpolator;
    }
}
