package com.android.systemui.miui.controlcenter.customize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import miuix.animation.utils.EaseManager;
import miuix.animation.utils.VelocityMonitor;

public class UnAddedTilesLayout extends FrameLayout {
    private static final boolean DEBUG = Constants.DEBUG;
    private View mAddedLayout;
    private IStateStyle mAnim;
    private View mContent;
    private Context mContext;
    private float mDownX;
    private float mDownY;
    private View mHeader;
    private boolean mInTop;
    private ImageView mIndicator;
    private float mMarginDelta;
    private int mMarginThreshold;
    private float mMarginTopStart;
    private float mMaxMarginTop;
    private float mMinMarginTop;
    private TextView mTitle;
    private VelocityMonitor mVelocityMonitor;

    public UnAddedTilesLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public UnAddedTilesLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDownX = -1.0f;
        this.mDownY = -1.0f;
        this.mContext = context;
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        LayoutInflater.from(this.mContext).inflate(R.layout.qs_control_customize_unadded_tiles_layout, this);
        this.mMarginThreshold = 20;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mVelocityMonitor = new VelocityMonitor();
        this.mContent = findViewById(R.id.content);
        this.mIndicator = (ImageView) findViewById(R.id.indicator);
        this.mHeader = findViewById(R.id.header);
        this.mTitle = (TextView) findViewById(R.id.others_title);
        IStateStyle useValue = Folme.useValue("setMarginTop");
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(EaseManager.getStyle(-2, 0.8f, 0.4f));
        animConfig.addListeners(new TransitionListener() {
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                UnAddedTilesLayout.this.setMarginTop((int) f);
            }
        });
        useValue.setConfig(animConfig, new FloatProperty[0]);
        this.mAnim = useValue;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mAddedLayout.getPaddingBottom() != getHeight()) {
            updateHeaderLayoutMargin();
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        View view = this.mContent;
        if (view != null) {
            int paddingTop = view.getPaddingTop();
            this.mContent.setPadding(this.mContent.getPaddingLeft(), paddingTop, this.mContent.getPaddingRight(), windowInsets.getStableInsetBottom());
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    public void init() {
        this.mAnim.to(Float.valueOf(this.mMaxMarginTop), new AnimConfig[0]);
    }

    public void updateResources() {
        this.mIndicator.setImageDrawable(this.mContext.getDrawable(R.drawable.qs_control_tiles_indicator));
        this.mTitle.setTextAppearance(R.style.TextAppearance_QSControl_CustomizeOthersTitle);
    }

    public void setAddedLayout(RecyclerView recyclerView) {
        this.mAddedLayout = recyclerView;
    }

    public void resetMargin() {
        setMarginTop((int) this.mMaxMarginTop);
    }

    public void setMarginTop(int i, int i2) {
        this.mMinMarginTop = (float) i;
        this.mMaxMarginTop = (float) i2;
        this.mMarginTopStart = this.mInTop ? this.mMinMarginTop : this.mMaxMarginTop;
        updateMarginTop(0.0f);
    }

    private void updateMarginTop(float f) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.topMargin = (int) (this.mMarginTopStart + f);
        setLayoutParams(layoutParams);
        requestLayout();
        updateHeaderLayoutMargin();
    }

    private void updateHeaderLayoutMargin() {
        View view = this.mAddedLayout;
        if (view != null) {
            view.setPadding(view.getPaddingLeft(), this.mAddedLayout.getPaddingTop(), this.mAddedLayout.getPaddingRight(), getHeight());
            this.mAddedLayout.requestLayout();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002d, code lost:
        if (r0 != 3) goto L_0x00fb;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r7) {
        /*
            r6 = this;
            boolean r0 = DEBUG
            if (r0 == 0) goto L_0x001e
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onTouchEvent: action = "
            r0.append(r1)
            int r1 = r7.getActionMasked()
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "UnAddedTilesLayout"
            android.util.Log.i(r1, r0)
        L_0x001e:
            int r0 = r7.getActionMasked()
            r1 = 1
            if (r0 == 0) goto L_0x00c3
            r2 = 0
            r3 = 0
            if (r0 == r1) goto L_0x0081
            r4 = 2
            if (r0 == r4) goto L_0x0031
            r7 = 3
            if (r0 == r7) goto L_0x0081
            goto L_0x00fb
        L_0x0031:
            float r0 = r6.mDownY
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 <= 0) goto L_0x00fb
            float r7 = r7.getRawY()
            float r0 = r6.mDownY
            float r7 = r7 - r0
            r6.mMarginDelta = r7
            float r7 = r6.mMarginTopStart
            float r0 = r6.mMarginDelta
            float r7 = r7 + r0
            float r0 = r6.mMaxMarginTop
            int r5 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r5 <= 0) goto L_0x0054
            float r7 = r7 - r0
            float r5 = r6.mMinMarginTop
            float r7 = com.android.systemui.miui.controlcenter.Utils.afterFriction(r7, r5)
            float r7 = r7 + r0
            goto L_0x0062
        L_0x0054:
            float r0 = r6.mMinMarginTop
            int r5 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r5 >= 0) goto L_0x0062
            float r7 = r0 - r7
            float r7 = com.android.systemui.miui.controlcenter.Utils.afterFriction(r7, r0)
            float r7 = r0 - r7
        L_0x0062:
            miuix.animation.IStateStyle r0 = r6.mAnim
            java.lang.Float r5 = java.lang.Float.valueOf(r7)
            r0.setTo((java.lang.Object) r5)
            int r7 = (int) r7
            r6.setMarginTop(r7)
            miuix.animation.utils.VelocityMonitor r7 = r6.mVelocityMonitor
            float[] r0 = new float[r4]
            r0[r3] = r2
            float r2 = r6.mMarginTopStart
            float r6 = r6.mMarginDelta
            float r2 = r2 + r6
            r0[r1] = r2
            r7.update((float[]) r0)
            goto L_0x00fb
        L_0x0081:
            miuix.animation.utils.VelocityMonitor r7 = r6.mVelocityMonitor
            float r7 = r7.getVelocity(r1)
            r0 = 1148846080(0x447a0000, float:1000.0)
            int r4 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r4 <= 0) goto L_0x0090
            r6.mInTop = r3
            goto L_0x00ae
        L_0x0090:
            float r7 = r7 + r0
            int r7 = (r7 > r2 ? 1 : (r7 == r2 ? 0 : -1))
            if (r7 >= 0) goto L_0x0098
            r6.mInTop = r1
            goto L_0x00ae
        L_0x0098:
            int r7 = r6.getMarginTop()
            float r7 = (float) r7
            float r0 = r6.mMinMarginTop
            float r2 = r6.mMaxMarginTop
            float r0 = r0 + r2
            r2 = 1073741824(0x40000000, float:2.0)
            float r0 = r0 / r2
            int r7 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r7 > 0) goto L_0x00ab
            r7 = r1
            goto L_0x00ac
        L_0x00ab:
            r7 = r3
        L_0x00ac:
            r6.mInTop = r7
        L_0x00ae:
            boolean r7 = r6.mInTop
            if (r7 == 0) goto L_0x00b5
            float r7 = r6.mMinMarginTop
            goto L_0x00b7
        L_0x00b5:
            float r7 = r6.mMaxMarginTop
        L_0x00b7:
            miuix.animation.IStateStyle r6 = r6.mAnim
            java.lang.Float r7 = java.lang.Float.valueOf(r7)
            miuix.animation.base.AnimConfig[] r0 = new miuix.animation.base.AnimConfig[r3]
            r6.to(r7, r0)
            goto L_0x00fb
        L_0x00c3:
            r0 = -1082130432(0xffffffffbf800000, float:-1.0)
            r6.mDownX = r0
            r6.mDownY = r0
            android.graphics.Rect r0 = new android.graphics.Rect
            r0.<init>()
            miuix.animation.utils.VelocityMonitor r2 = r6.mVelocityMonitor
            r2.clear()
            android.view.View r2 = r6.mHeader
            r2.getBoundsOnScreen(r0)
            float r2 = r7.getRawX()
            int r2 = (int) r2
            float r3 = r7.getRawY()
            int r3 = (int) r3
            boolean r0 = r0.contains(r2, r3)
            if (r0 == 0) goto L_0x00fb
            float r0 = r7.getRawX()
            r6.mDownX = r0
            float r7 = r7.getRawY()
            r6.mDownY = r7
            int r7 = r6.getMarginTop()
            float r7 = (float) r7
            r6.mMarginTopStart = r7
        L_0x00fb:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.customize.UnAddedTilesLayout.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public int getMarginTop() {
        return ((FrameLayout.LayoutParams) getLayoutParams()).topMargin;
    }

    public void setMarginTop(int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.topMargin = i;
        setLayoutParams(layoutParams);
        requestLayout();
        updateHeaderLayoutMargin();
        this.mAddedLayout.setBottom(getTop());
        this.mAddedLayout.requestLayout();
    }
}
