package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.ScrollView;
import com.android.systemui.plugins.R;
import java.lang.reflect.Field;
import java.util.ArrayList;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.FloatProperty;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class QSControlScrollView extends ScrollView {
    private static final Field EDGE_GLOW_BOTTOM;
    private static final Field EDGE_GLOW_TOP;
    private static final Field OVERFLING_DISTANCE;
    private static final FloatProperty TRANS_HEIGHT = new FloatProperty<QSControlScrollView>("TransHeight") {
        public void setValue(QSControlScrollView qSControlScrollView, float f) {
            qSControlScrollView.setTransHeight(f);
        }

        public float getValue(QSControlScrollView qSControlScrollView) {
            return qSControlScrollView.getTransHeight();
        }
    };
    private boolean ismTouchScrolledToBottom;
    private IStateStyle mBounceState;
    private Rect mBound = new Rect();
    private float mInitialY;
    private boolean mIsScrolledToBottom = false;
    private boolean mIsScrolledToTop = true;
    private int mOrientation;
    private boolean mOverTrans;
    private QSControlCenterTileLayout mQsControlCenterTileLayout;
    private int mStartSrcollY = 0;
    private boolean mTouchScrolledToTop;
    private float mTransHeight;
    private IStateStyle mTransState;

    public QSControlScrollView(Context context) {
        super(context, (AttributeSet) null);
    }

    public QSControlScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mQsControlCenterTileLayout = (QSControlCenterTileLayout) findViewById(R.id.quick_tile_layout);
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            arrayList.add(getChildAt(i));
        }
        this.mTransState = Folme.useAt((View[]) arrayList.toArray(new View[arrayList.size()])).state();
        this.mBounceState = Folme.useAt(this).state();
        setOverScrollMode(1);
        replaceScrollViewAttribute();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mOrientation = configuration.orientation;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        Rect rect = this.mBound;
        rect.left = 0;
        rect.top = 0;
        rect.right = i;
        rect.bottom = i2;
        setClipBounds(rect);
    }

    /* access modifiers changed from: protected */
    public void onOverScrolled(int i, int i2, boolean z, boolean z2) {
        super.onOverScrolled(i, i2, z, z2);
        if (i2 == 0) {
            this.mIsScrolledToTop = z2;
            this.mIsScrolledToBottom = false;
        } else {
            this.mIsScrolledToTop = false;
            this.mIsScrolledToBottom = z2;
        }
        int scrollRange = getScrollRange();
        if (i2 < 0) {
            this.mOverTrans = true;
            setOverTrans((float) (-i2));
        } else if (i2 > scrollRange) {
            this.mOverTrans = true;
            setOverTrans((float) (scrollRange - i2));
        } else if (this.mOverTrans) {
            this.mOverTrans = false;
            setOverTrans(0.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        if (getScrollY() == 0) {
            this.mIsScrolledToTop = true;
            this.mIsScrolledToBottom = false;
        } else if (((getScrollY() + getHeight()) - getPaddingTop()) - getPaddingBottom() == getChildAt(0).getHeight()) {
            this.mIsScrolledToBottom = true;
            this.mIsScrolledToTop = false;
        } else {
            this.mIsScrolledToTop = false;
            this.mIsScrolledToBottom = false;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return super.dispatchTouchEvent(motionEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mInitialY = motionEvent.getRawY();
            this.mTouchScrolledToTop = this.mIsScrolledToTop;
            this.ismTouchScrolledToBottom = this.mIsScrolledToBottom;
        }
        boolean onInterceptTouchEvent = super.onInterceptTouchEvent(motionEvent);
        Log.d("QSControlScrollView", "onInterceptTouchEvent " + motionEvent.getActionMasked() + "  return " + onInterceptTouchEvent);
        return onInterceptTouchEvent;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        if (r0 != 3) goto L_0x009c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r8) {
        /*
            r7 = this;
            com.android.systemui.miui.controlcenter.QSControlCenterTileLayout r0 = r7.mQsControlCenterTileLayout
            boolean r0 = r0.isCollapsed()
            java.lang.String r1 = "QSControlScrollView"
            r2 = 0
            if (r0 == 0) goto L_0x0011
            java.lang.String r7 = "onTouchEvent collapsed return false"
            android.util.Log.d(r1, r7)
            return r2
        L_0x0011:
            int r0 = r8.getActionMasked()
            if (r0 == 0) goto L_0x008e
            java.lang.String r3 = "bounce"
            r4 = 1
            r5 = 0
            if (r0 == r4) goto L_0x007a
            r6 = 2
            if (r0 == r6) goto L_0x0025
            r4 = 3
            if (r0 == r4) goto L_0x007a
            goto L_0x009c
        L_0x0025:
            boolean r0 = r7.mTouchScrolledToTop
            boolean r6 = r7.mIsScrolledToTop
            if (r0 != r6) goto L_0x0031
            boolean r0 = r7.ismTouchScrolledToBottom
            boolean r6 = r7.mIsScrolledToBottom
            if (r0 == r6) goto L_0x003f
        L_0x0031:
            boolean r0 = r7.mIsScrolledToTop
            r7.mTouchScrolledToTop = r0
            boolean r0 = r7.mIsScrolledToBottom
            r7.ismTouchScrolledToBottom = r0
            float r0 = r8.getRawY()
            r7.mInitialY = r0
        L_0x003f:
            boolean r0 = r7.isScrolledToTop()
            if (r0 == 0) goto L_0x0050
            float r0 = r8.getRawY()
            float r6 = r7.mInitialY
            float r0 = r0 - r6
            int r0 = (r0 > r5 ? 1 : (r0 == r5 ? 0 : -1))
            if (r0 >= 0) goto L_0x0061
        L_0x0050:
            boolean r0 = r7.isScrolledToBottom()
            if (r0 == 0) goto L_0x009c
            float r0 = r8.getRawY()
            float r6 = r7.mInitialY
            float r0 = r0 - r6
            int r0 = (r0 > r5 ? 1 : (r0 == r5 ? 0 : -1))
            if (r0 >= 0) goto L_0x009c
        L_0x0061:
            miuix.animation.controller.AnimState r0 = new miuix.animation.controller.AnimState
            r0.<init>(r3)
            miuix.animation.property.FloatProperty r1 = TRANS_HEIGHT
            float r8 = r8.getRawY()
            float r3 = r7.mInitialY
            float r8 = r8 - r3
            long[] r2 = new long[r2]
            r0.add((miuix.animation.property.FloatProperty) r1, (float) r8, (long[]) r2)
            miuix.animation.IStateStyle r7 = r7.mBounceState
            r7.setTo((java.lang.Object) r0)
            return r4
        L_0x007a:
            miuix.animation.controller.AnimState r0 = new miuix.animation.controller.AnimState
            r0.<init>(r3)
            miuix.animation.property.FloatProperty r3 = TRANS_HEIGHT
            long[] r4 = new long[r2]
            r0.add((miuix.animation.property.FloatProperty) r3, (int) r2, (long[]) r4)
            miuix.animation.IStateStyle r2 = r7.mBounceState
            r2.setTo((java.lang.Object) r0)
            r7.mInitialY = r5
            goto L_0x009c
        L_0x008e:
            float r0 = r8.getRawY()
            r7.mInitialY = r0
            boolean r0 = r7.mIsScrolledToTop
            r7.mTouchScrolledToTop = r0
            boolean r0 = r7.mIsScrolledToBottom
            r7.ismTouchScrolledToBottom = r0
        L_0x009c:
            boolean r7 = super.onTouchEvent(r8)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "QSControlScrollView: onTouchEvent ev = "
            r0.append(r2)
            int r8 = r8.getAction()
            r0.append(r8)
            java.lang.String r8 = ", result = "
            r0.append(r8)
            r0.append(r7)
            java.lang.String r8 = r0.toString()
            android.util.Log.d(r1, r8)
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.QSControlScrollView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void srcollTotratio(float f) {
        if (isScrolledToTop()) {
            this.mStartSrcollY = 0;
            return;
        }
        if (this.mStartSrcollY == 0) {
            this.mStartSrcollY = getScrollY();
        }
        scrollTo(getScrollX(), (int) (((float) this.mStartSrcollY) * f));
        if (f == 0.0f || f == 1.0f) {
            this.mStartSrcollY = 0;
        }
    }

    public boolean isScrolledToBottom() {
        return this.mIsScrolledToBottom;
    }

    public boolean isScrolledToTop() {
        return this.mIsScrolledToTop;
    }

    private void setOverTrans(float f) {
        AnimState animState = new AnimState("control_scroll_trans");
        animState.add(ViewProperty.TRANSLATION_Y, f, new long[0]);
        this.mTransState.cancel(ViewProperty.TRANSLATION_Y);
        this.mTransState.setTo((Object) animState);
    }

    /* access modifiers changed from: private */
    public void setTransHeight(float f) {
        this.mTransHeight = f;
        int height = getHeight();
        if (f == 0.0f) {
            AnimState animState = new AnimState("control_scroll_trans");
            animState.add(ViewProperty.TRANSLATION_Y, 0.0f, new long[0]);
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(EaseManager.getStyle(-2, 0.85f, 0.6f));
            this.mTransState.cancel(ViewProperty.TRANSLATION_Y);
            this.mTransState.to(animState, animConfig);
            updateParentTransHeight(0.0f);
        } else if (f < 0.0f || this.mOrientation == 2) {
            float translationY = Utils.getTranslationY(f, (float) height);
            AnimState animState2 = new AnimState("control_scroll_trans");
            animState2.add(ViewProperty.TRANSLATION_Y, translationY, new long[0]);
            this.mTransState.setTo((Object) animState2);
        } else {
            updateParentTransHeight(f);
        }
    }

    /* access modifiers changed from: private */
    public float getTransHeight() {
        return this.mTransHeight;
    }

    private void updateParentTransHeight(float f) {
        if (getParent() != null && getParent().getParent() != null && (getParent().getParent() instanceof QSControlCenterPanel)) {
            ((QSControlCenterPanel) getParent().getParent()).updateTransHeight(f);
        }
    }

    private int getScrollRange() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
        }
        return 0;
    }

    private void replaceScrollViewAttribute() {
        try {
            OVERFLING_DISTANCE.set(this, Integer.valueOf((int) getContext().getResources().getDimension(R.dimen.qs_control_scrollview_overfling_distance)));
            EDGE_GLOW_TOP.set(this, new EmptyEdgeEffect(getContext()));
            EDGE_GLOW_BOTTOM.set(this, new EmptyEdgeEffect(getContext()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            OVERFLING_DISTANCE = ScrollView.class.getDeclaredField("mOverflingDistance");
            OVERFLING_DISTANCE.setAccessible(true);
            EDGE_GLOW_TOP = ScrollView.class.getDeclaredField("mEdgeGlowTop");
            EDGE_GLOW_TOP.setAccessible(true);
            EDGE_GLOW_BOTTOM = ScrollView.class.getDeclaredField("mEdgeGlowBottom");
            EDGE_GLOW_BOTTOM.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static class EmptyEdgeEffect extends EdgeEffect {
        public boolean draw(Canvas canvas) {
            return false;
        }

        public EmptyEdgeEffect(Context context) {
            super(context);
        }
    }
}
