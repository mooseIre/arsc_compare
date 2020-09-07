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
import android.widget.LinearLayout;
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
    private float mInitialX;
    private float mInitialY;
    private boolean mIsScrolledToBottom = false;
    private boolean mIsScrolledToTop = true;
    private boolean mMoved = false;
    private int mOrientation;
    private boolean mOverTrans;
    private QSControlCenterPanel mQSControlCenterPanel;
    private QSControlCenterTileLayout mQsControlCenterTileLayout;
    private LinearLayout mSmartControlsView;
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
        this.mSmartControlsView = (LinearLayout) findViewById(R.id.ll_smart_controls);
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

    public void setQSControlCenterPanel(QSControlCenterPanel qSControlCenterPanel) {
        this.mQSControlCenterPanel = qSControlCenterPanel;
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
            this.mInitialX = motionEvent.getRawX();
            this.mTouchScrolledToTop = this.mIsScrolledToTop;
            this.ismTouchScrolledToBottom = this.mIsScrolledToBottom;
        }
        if (motionEvent.getActionMasked() == 2) {
            boolean z = Math.abs(motionEvent.getRawY() - this.mInitialY) > Math.abs(motionEvent.getRawX() - this.mInitialX);
            if (!this.mIsScrolledToBottom && !this.mIsScrolledToTop && this.mSmartControlsView.getChildCount() > 0 && z) {
                return true;
            }
        }
        boolean onInterceptTouchEvent = super.onInterceptTouchEvent(motionEvent);
        Log.d("QSControlScrollView", "onInterceptTouchEvent " + motionEvent.getActionMasked() + "  return " + onInterceptTouchEvent);
        return onInterceptTouchEvent;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        if (r0 != 3) goto L_0x00b9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r9) {
        /*
            r8 = this;
            com.android.systemui.miui.controlcenter.QSControlCenterTileLayout r0 = r8.mQsControlCenterTileLayout
            boolean r0 = r0.isCollapsed()
            java.lang.String r1 = "QSControlScrollView"
            r2 = 0
            if (r0 == 0) goto L_0x0019
            android.widget.LinearLayout r0 = r8.mSmartControlsView
            int r0 = r0.getChildCount()
            if (r0 != 0) goto L_0x0019
            java.lang.String r8 = "onTouchEvent collapsed return false"
            android.util.Log.d(r1, r8)
            return r2
        L_0x0019:
            int r0 = r9.getActionMasked()
            if (r0 == 0) goto L_0x00a9
            java.lang.String r3 = "bounce"
            r4 = 0
            r5 = 1
            if (r0 == r5) goto L_0x0084
            r6 = 2
            if (r0 == r6) goto L_0x002d
            r5 = 3
            if (r0 == r5) goto L_0x0095
            goto L_0x00b9
        L_0x002d:
            r8.mMoved = r5
            boolean r0 = r8.mTouchScrolledToTop
            boolean r6 = r8.mIsScrolledToTop
            if (r0 != r6) goto L_0x003b
            boolean r0 = r8.ismTouchScrolledToBottom
            boolean r6 = r8.mIsScrolledToBottom
            if (r0 == r6) goto L_0x0049
        L_0x003b:
            boolean r0 = r8.mIsScrolledToTop
            r8.mTouchScrolledToTop = r0
            boolean r0 = r8.mIsScrolledToBottom
            r8.ismTouchScrolledToBottom = r0
            float r0 = r9.getRawY()
            r8.mInitialY = r0
        L_0x0049:
            boolean r0 = r8.isScrolledToTop()
            if (r0 == 0) goto L_0x005a
            float r0 = r9.getRawY()
            float r6 = r8.mInitialY
            float r0 = r0 - r6
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 >= 0) goto L_0x006b
        L_0x005a:
            boolean r0 = r8.isScrolledToBottom()
            if (r0 == 0) goto L_0x00b9
            float r0 = r9.getRawY()
            float r6 = r8.mInitialY
            float r0 = r0 - r6
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 >= 0) goto L_0x00b9
        L_0x006b:
            miuix.animation.controller.AnimState r0 = new miuix.animation.controller.AnimState
            r0.<init>(r3)
            miuix.animation.property.FloatProperty r1 = TRANS_HEIGHT
            float r9 = r9.getRawY()
            float r3 = r8.mInitialY
            float r9 = r9 - r3
            long[] r2 = new long[r2]
            r0.add((miuix.animation.property.FloatProperty) r1, (float) r9, (long[]) r2)
            miuix.animation.IStateStyle r8 = r8.mBounceState
            r8.setTo((java.lang.Object) r0)
            return r5
        L_0x0084:
            com.android.systemui.miui.controlcenter.QSControlCenterPanel r0 = r8.mQSControlCenterPanel
            float r5 = r9.getRawX()
            int r5 = (int) r5
            float r6 = r9.getRawY()
            int r6 = (int) r6
            boolean r7 = r8.mMoved
            r0.performCollapseByClick(r5, r6, r7)
        L_0x0095:
            miuix.animation.controller.AnimState r0 = new miuix.animation.controller.AnimState
            r0.<init>(r3)
            miuix.animation.property.FloatProperty r3 = TRANS_HEIGHT
            long[] r5 = new long[r2]
            r0.add((miuix.animation.property.FloatProperty) r3, (int) r2, (long[]) r5)
            miuix.animation.IStateStyle r2 = r8.mBounceState
            r2.setTo((java.lang.Object) r0)
            r8.mInitialY = r4
            goto L_0x00b9
        L_0x00a9:
            r8.mMoved = r2
            float r0 = r9.getRawY()
            r8.mInitialY = r0
            boolean r0 = r8.mIsScrolledToTop
            r8.mTouchScrolledToTop = r0
            boolean r0 = r8.mIsScrolledToBottom
            r8.ismTouchScrolledToBottom = r0
        L_0x00b9:
            boolean r8 = super.onTouchEvent(r9)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "QSControlScrollView: onTouchEvent ev = "
            r0.append(r2)
            int r9 = r9.getAction()
            r0.append(r9)
            java.lang.String r9 = ", result = "
            r0.append(r9)
            r0.append(r8)
            java.lang.String r9 = r0.toString()
            android.util.Log.d(r1, r9)
            return r8
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
            Field declaredField = ScrollView.class.getDeclaredField("mOverflingDistance");
            OVERFLING_DISTANCE = declaredField;
            declaredField.setAccessible(true);
            Field declaredField2 = ScrollView.class.getDeclaredField("mEdgeGlowTop");
            EDGE_GLOW_TOP = declaredField2;
            declaredField2.setAccessible(true);
            Field declaredField3 = ScrollView.class.getDeclaredField("mEdgeGlowBottom");
            EDGE_GLOW_BOTTOM = declaredField3;
            declaredField3.setAccessible(true);
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
