package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ChannelEditorListView.kt */
public final class AppControlView extends LinearLayout {
    @NotNull
    public TextView channelName;
    @NotNull
    public ImageView iconView;
    @NotNull

    /* renamed from: switch  reason: not valid java name */
    public Switch f0switch;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AppControlView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @NotNull
    public final ImageView getIconView() {
        ImageView imageView = this.iconView;
        if (imageView != null) {
            return imageView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("iconView");
        throw null;
    }

    @NotNull
    public final TextView getChannelName() {
        TextView textView = this.channelName;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("channelName");
        throw null;
    }

    @NotNull
    public final Switch getSwitch() {
        Switch r0 = this.f0switch;
        if (r0 != null) {
            return r0;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        View findViewById = findViewById(C0015R$id.icon);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.icon)");
        this.iconView = (ImageView) findViewById;
        View findViewById2 = findViewById(C0015R$id.app_name);
        Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.app_name)");
        this.channelName = (TextView) findViewById2;
        View findViewById3 = findViewById(C0015R$id.toggle);
        Intrinsics.checkExpressionValueIsNotNull(findViewById3, "findViewById(R.id.toggle)");
        this.f0switch = (Switch) findViewById3;
        setOnClickListener(new AppControlView$onFinishInflate$1(this));
    }
}
