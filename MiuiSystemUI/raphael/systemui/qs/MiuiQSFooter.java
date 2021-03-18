package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.qs.TouchAnimator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiQSFooter.kt */
public final class MiuiQSFooter extends FrameLayout implements QSFooter {
    private View.OnClickListener mExpandClickListener;
    private boolean mExpanded;
    private float mExpansionAmount;
    private TouchAnimator mFooterAnimator;
    private MiuiPageIndicator mPageIndicator;

    @Override // com.android.systemui.qs.QSFooter
    public void disable(int i, int i2, boolean z) {
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setListening(boolean z) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiQSFooter(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        if (context != null) {
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0015R$id.footer_page_indicator);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.footer_page_indicator)");
        this.mPageIndicator = (MiuiPageIndicator) findViewById;
        updateResources();
        setImportantForAccessibility(1);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        updateResources();
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateResources();
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setQSPanel(@Nullable QSPanel qSPanel) {
        if (qSPanel != null) {
            MiuiPageIndicator miuiPageIndicator = this.mPageIndicator;
            if (miuiPageIndicator != null) {
                qSPanel.setFooterPageIndicator(miuiPageIndicator);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mPageIndicator");
                throw null;
            }
        }
    }

    private final void updateResources() {
        updateFooterAnimator();
        setExpansion(this.mExpansionAmount);
    }

    private final void updateFooterAnimator() {
        this.mFooterAnimator = createFooterAnimator();
    }

    private final TouchAnimator createFooterAnimator() {
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        MiuiPageIndicator miuiPageIndicator = this.mPageIndicator;
        if (miuiPageIndicator != null) {
            builder.addFloat(miuiPageIndicator, "alpha", 0.0f, 1.0f);
            builder.setStartDelay(0.9f);
            return builder.build();
        }
        Intrinsics.throwUninitializedPropertyAccessException("mPageIndicator");
        throw null;
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setKeyguardShowing(boolean z) {
        setExpansion(this.mExpansionAmount);
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpandClickListener(@NotNull View.OnClickListener onClickListener) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "onClickListener");
        this.mExpandClickListener = onClickListener;
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
        }
    }

    @Override // com.android.systemui.qs.QSFooter
    public void setExpansion(float f) {
        this.mExpansionAmount = f;
        TouchAnimator touchAnimator = this.mFooterAnimator;
        if (touchAnimator == null) {
            return;
        }
        if (touchAnimator != null) {
            touchAnimator.setPosition(f);
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public void onDetachedFromWindow() {
        setListening(false);
        super.onDetachedFromWindow();
    }

    public boolean performAccessibilityAction(int i, @Nullable Bundle bundle) {
        View.OnClickListener onClickListener;
        if (i != 262144 || (onClickListener = this.mExpandClickListener) == null) {
            return super.performAccessibilityAction(i, bundle);
        }
        if (onClickListener != null) {
            onClickListener.onClick(null);
            return true;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    public void onInitializeAccessibilityNodeInfo(@Nullable AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (accessibilityNodeInfo != null) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
        }
    }
}
