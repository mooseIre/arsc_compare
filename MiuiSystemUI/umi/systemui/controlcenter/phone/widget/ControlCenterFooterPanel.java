package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterFooterPanel.kt */
public final class ControlCenterFooterPanel extends LinearLayout {
    @NotNull
    private View divider;
    @NotNull
    private ImageView indicator;
    @NotNull
    private QSControlFooter settingsFooter;

    public ControlCenterFooterPanel(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @NotNull
    public final QSControlFooter getSettingsFooter() {
        QSControlFooter qSControlFooter = this.settingsFooter;
        if (qSControlFooter != null) {
            return qSControlFooter;
        }
        Intrinsics.throwUninitializedPropertyAccessException("settingsFooter");
        throw null;
    }

    @NotNull
    public final View getDivider() {
        View view = this.divider;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("divider");
        throw null;
    }

    @NotNull
    public final ImageView getIndicator() {
        ImageView imageView = this.indicator;
        if (imageView != null) {
            return imageView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("indicator");
        throw null;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View requireViewById = requireViewById(C0015R$id.settings_footer);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(R.id.settings_footer)");
        this.settingsFooter = (QSControlFooter) requireViewById;
        View requireViewById2 = requireViewById(C0015R$id.footer_divider);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.footer_divider)");
        this.divider = requireViewById2;
        View requireViewById3 = requireViewById(C0015R$id.qs_expand_indicator);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById(R.id.qs_expand_indicator)");
        this.indicator = (ImageView) requireViewById3;
    }

    public final void updateResources() {
        ImageView imageView = this.indicator;
        if (imageView != null) {
            imageView.setImageDrawable(getContext().getDrawable(C0013R$drawable.qs_control_tiles_indicator));
            QSControlFooter qSControlFooter = this.settingsFooter;
            if (qSControlFooter != null) {
                qSControlFooter.updateResources();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("settingsFooter");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("indicator");
            throw null;
        }
    }

    public final void setListening(boolean z) {
        QSControlFooter qSControlFooter = this.settingsFooter;
        if (qSControlFooter != null) {
            qSControlFooter.setListening(z);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("settingsFooter");
            throw null;
        }
    }
}
