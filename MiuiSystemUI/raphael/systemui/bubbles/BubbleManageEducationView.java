package com.android.systemui.bubbles;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;

public class BubbleManageEducationView extends LinearLayout {
    private TextView mDescTextView;
    private View mManageView;
    private TextView mTitleTextView;

    public BubbleManageEducationView(Context context) {
        this(context, null);
    }

    public BubbleManageEducationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleManageEducationView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BubbleManageEducationView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mManageView = findViewById(C0015R$id.manage_education_view);
        this.mTitleTextView = (TextView) findViewById(C0015R$id.user_education_title);
        this.mDescTextView = (TextView) findViewById(C0015R$id.user_education_description);
        TypedArray obtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{16843829, 16842809});
        int color = obtainStyledAttributes.getColor(0, -16777216);
        int color2 = obtainStyledAttributes.getColor(1, -1);
        obtainStyledAttributes.recycle();
        int ensureTextContrast = ContrastColorUtil.ensureTextContrast(color2, color, true);
        this.mTitleTextView.setTextColor(ensureTextContrast);
        this.mDescTextView.setTextColor(ensureTextContrast);
    }

    public void setManageViewPosition(int i, int i2) {
        this.mManageView.setTranslationX((float) i);
        this.mManageView.setTranslationY((float) i2);
    }

    public int getManageViewHeight() {
        return this.mManageView.getHeight();
    }

    public void setLayoutDirection(int i) {
        super.setLayoutDirection(i);
        if (getResources().getConfiguration().getLayoutDirection() == 1) {
            this.mManageView.setBackgroundResource(C0013R$drawable.bubble_stack_user_education_bg_rtl);
            this.mTitleTextView.setGravity(5);
            this.mDescTextView.setGravity(5);
            return;
        }
        this.mManageView.setBackgroundResource(C0013R$drawable.bubble_stack_user_education_bg);
        this.mTitleTextView.setGravity(3);
        this.mDescTextView.setGravity(3);
    }
}
