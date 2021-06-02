package com.android.systemui.controls.ui;

import android.app.ActivityView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.ImageView;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0022R$style;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DetailDialog.kt */
public final class DetailDialog extends Dialog {
    @NotNull
    private ActivityView activityView = new ActivityView(getContext(), (AttributeSet) null, 0, false);
    @NotNull
    private final ControlViewHolder cvh;
    @NotNull
    private final Intent intent;
    @NotNull
    private final ActivityView.StateCallback stateCallback = new DetailDialog$stateCallback$1(this);

    @NotNull
    public final Intent getIntent() {
        return this.intent;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DetailDialog(@NotNull ControlViewHolder controlViewHolder, @NotNull Intent intent2) {
        super(controlViewHolder.getContext(), C0022R$style.Theme_SystemUI_Dialog_Control_DetailPanel);
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(intent2, "intent");
        this.cvh = controlViewHolder;
        this.intent = intent2;
        getWindow().setType(2020);
        setContentView(C0017R$layout.controls_detail_dialog);
        ((ViewGroup) requireViewById(C0015R$id.controls_activity_view)).addView(this.activityView);
        ((ImageView) requireViewById(C0015R$id.control_detail_close)).setOnClickListener(new DetailDialog$$special$$inlined$apply$lambda$1(this));
        ImageView imageView = (ImageView) requireViewById(C0015R$id.control_detail_open_in_app);
        imageView.setOnClickListener(new DetailDialog$$special$$inlined$apply$lambda$2(imageView, this));
        getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener(this) {
            /* class com.android.systemui.controls.ui.DetailDialog.AnonymousClass4 */
            final /* synthetic */ DetailDialog this$0;

            {
                this.this$0 = r1;
            }

            public final WindowInsets onApplyWindowInsets(@NotNull View view, @NotNull WindowInsets windowInsets) {
                Intrinsics.checkParameterIsNotNull(view, "<anonymous parameter 0>");
                Intrinsics.checkParameterIsNotNull(windowInsets, "insets");
                ActivityView activityView = this.this$0.getActivityView();
                activityView.setPadding(activityView.getPaddingLeft(), activityView.getPaddingTop(), activityView.getPaddingRight(), windowInsets.getInsets(WindowInsets.Type.systemBars()).bottom);
                return WindowInsets.CONSUMED;
            }
        });
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0015R$id.control_detail_root);
        int i = Settings.Secure.getInt(this.cvh.getContext().getContentResolver(), "systemui.controls_panel_top_offset", this.cvh.getContext().getResources().getDimensionPixelSize(C0012R$dimen.controls_activity_view_top_offset));
        ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        if (layoutParams != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.topMargin = i;
            viewGroup.setLayoutParams(marginLayoutParams);
            viewGroup.setOnClickListener(new DetailDialog$$special$$inlined$apply$lambda$3(this));
            ViewParent parent = viewGroup.getParent();
            if (parent != null) {
                ((View) parent).setOnClickListener(new DetailDialog$$special$$inlined$apply$lambda$4(this));
                if (ScreenDecorationsUtils.supportsRoundedCornersOnWindows(getContext().getResources())) {
                    Context context = getContext();
                    Intrinsics.checkExpressionValueIsNotNull(context, "context");
                    this.activityView.setCornerRadius((float) context.getResources().getDimensionPixelSize(C0012R$dimen.controls_activity_view_corner_radius));
                    return;
                }
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.view.View");
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
    }

    @NotNull
    public final ActivityView getActivityView() {
        return this.activityView;
    }

    public void show() {
        this.activityView.setCallback(this.stateCallback);
        super.show();
    }

    public void dismiss() {
        if (isShowing()) {
            this.activityView.release();
            super.dismiss();
        }
    }
}
