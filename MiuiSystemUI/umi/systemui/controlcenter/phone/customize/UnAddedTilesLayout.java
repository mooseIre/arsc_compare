package com.android.systemui.controlcenter.phone.customize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0022R$style;
import com.android.systemui.controlcenter.utils.Constants;
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
    private float mDownY;
    private View mHeader;
    private boolean mInTop;
    private ImageView mIndicator;
    private float mMarginDelta;
    private float mMarginTopStart;
    private float mMaxMarginTop;
    private float mMinMarginTop;
    private TextView mTitle;
    private VelocityMonitor mVelocityMonitor;

    public UnAddedTilesLayout(Context context) {
        this(context, null);
    }

    public UnAddedTilesLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDownY = -1.0f;
        this.mContext = context;
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        LayoutInflater.from(this.mContext).inflate(C0017R$layout.qs_control_customize_unadded_tiles_layout, this);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mVelocityMonitor = new VelocityMonitor();
        this.mContent = findViewById(C0015R$id.content);
        this.mIndicator = (ImageView) findViewById(C0015R$id.indicator);
        this.mHeader = findViewById(C0015R$id.header);
        this.mTitle = (TextView) findViewById(C0015R$id.others_title);
        IStateStyle useValue = Folme.useValue("setMarginTop");
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(EaseManager.getStyle(-2, 0.8f, 0.4f));
        animConfig.addListeners(new TransitionListener() {
            /* class com.android.systemui.controlcenter.phone.customize.UnAddedTilesLayout.AnonymousClass1 */

            @Override // miuix.animation.listener.TransitionListener
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                UnAddedTilesLayout.this.setMarginTop((int) f);
            }
        });
        this.mAnim = useValue.setConfig(animConfig, new FloatProperty[0]);
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
        this.mIndicator.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.qs_control_tiles_indicator));
        this.mTitle.setTextAppearance(C0022R$style.TextAppearance_QSControl_CustomizeOthersTitle);
    }

    public void setAddedLayout(RecyclerView recyclerView) {
        this.mAddedLayout = recyclerView;
    }

    public void resetMargin() {
        setMarginTop((int) this.mMaxMarginTop);
    }

    public void setMarginTop(int i, int i2) {
        float f = (float) i;
        this.mMinMarginTop = f;
        float f2 = (float) i2;
        this.mMaxMarginTop = f2;
        if (!this.mInTop) {
            f = f2;
        }
        this.mMarginTopStart = f;
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
        if (r0 != 3) goto L_0x00f4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r7) {
        /*
        // Method dump skipped, instructions count: 245
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.customize.UnAddedTilesLayout.onTouchEvent(android.view.MotionEvent):boolean");
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
