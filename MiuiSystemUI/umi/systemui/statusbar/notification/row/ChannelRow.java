package com.android.systemui.statusbar.notification.row;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.C0012R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChannelEditorListView.kt */
public final class ChannelRow extends LinearLayout {
    @Nullable
    private NotificationChannel channel;
    private TextView channelDescription;
    private TextView channelName;
    @NotNull
    public ChannelEditorDialogController controller;
    private final int highlightColor = Utils.getColorAttrDefaultColor(getContext(), 16843820);

    /* renamed from: switch  reason: not valid java name */
    private Switch f1switch;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChannelRow(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    public static final /* synthetic */ Switch access$getSwitch$p(ChannelRow channelRow) {
        Switch switchR = channelRow.f1switch;
        if (switchR != null) {
            return switchR;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
    }

    @NotNull
    public final ChannelEditorDialogController getController() {
        ChannelEditorDialogController channelEditorDialogController = this.controller;
        if (channelEditorDialogController != null) {
            return channelEditorDialogController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controller");
        throw null;
    }

    public final void setController(@NotNull ChannelEditorDialogController channelEditorDialogController) {
        Intrinsics.checkParameterIsNotNull(channelEditorDialogController, "<set-?>");
        this.controller = channelEditorDialogController;
    }

    @Nullable
    public final NotificationChannel getChannel() {
        return this.channel;
    }

    public final void setChannel(@Nullable NotificationChannel notificationChannel) {
        this.channel = notificationChannel;
        updateImportance();
        updateViews();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0012R$id.channel_name);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.channel_name)");
        this.channelName = (TextView) findViewById;
        View findViewById2 = findViewById(C0012R$id.channel_description);
        Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.channel_description)");
        this.channelDescription = (TextView) findViewById2;
        View findViewById3 = findViewById(C0012R$id.toggle);
        Intrinsics.checkExpressionValueIsNotNull(findViewById3, "findViewById(R.id.toggle)");
        Switch switchR = (Switch) findViewById3;
        this.f1switch = switchR;
        if (switchR != null) {
            switchR.setOnCheckedChangeListener(new ChannelRow$onFinishInflate$1(this));
            setOnClickListener(new ChannelRow$onFinishInflate$2(this));
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
    }

    public final void playHighlight() {
        ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{0, Integer.valueOf(this.highlightColor)});
        Intrinsics.checkExpressionValueIsNotNull(ofObject, "fadeInLoop");
        ofObject.setDuration(200);
        ofObject.addUpdateListener(new ChannelRow$playHighlight$1(this));
        ofObject.setRepeatMode(2);
        ofObject.setRepeatCount(5);
        ofObject.start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0075  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateViews() {
        /*
            r6 = this;
            android.app.NotificationChannel r0 = r6.channel
            if (r0 == 0) goto L_0x0085
            android.widget.TextView r1 = r6.channelName
            r2 = 0
            if (r1 == 0) goto L_0x007f
            java.lang.CharSequence r3 = r0.getName()
            if (r3 == 0) goto L_0x0010
            goto L_0x0012
        L_0x0010:
            java.lang.String r3 = ""
        L_0x0012:
            r1.setText(r3)
            java.lang.String r1 = r0.getGroup()
            java.lang.String r3 = "channelDescription"
            if (r1 == 0) goto L_0x0037
            android.widget.TextView r4 = r6.channelDescription
            if (r4 == 0) goto L_0x0033
            com.android.systemui.statusbar.notification.row.ChannelEditorDialogController r5 = r6.controller
            if (r5 == 0) goto L_0x002d
            java.lang.CharSequence r1 = r5.groupNameForId(r1)
            r4.setText(r1)
            goto L_0x0037
        L_0x002d:
            java.lang.String r6 = "controller"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
            throw r2
        L_0x0033:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x0037:
            java.lang.String r1 = r0.getGroup()
            r4 = 0
            if (r1 == 0) goto L_0x005d
            android.widget.TextView r1 = r6.channelDescription
            if (r1 == 0) goto L_0x0059
            java.lang.CharSequence r1 = r1.getText()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x004d
            goto L_0x005d
        L_0x004d:
            android.widget.TextView r1 = r6.channelDescription
            if (r1 == 0) goto L_0x0055
            r1.setVisibility(r4)
            goto L_0x0066
        L_0x0055:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x0059:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x005d:
            android.widget.TextView r1 = r6.channelDescription
            if (r1 == 0) goto L_0x007b
            r3 = 8
            r1.setVisibility(r3)
        L_0x0066:
            android.widget.Switch r6 = r6.f1switch
            if (r6 == 0) goto L_0x0075
            int r0 = r0.getImportance()
            if (r0 == 0) goto L_0x0071
            r4 = 1
        L_0x0071:
            r6.setChecked(r4)
            return
        L_0x0075:
            java.lang.String r6 = "switch"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
            throw r2
        L_0x007b:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x007f:
            java.lang.String r6 = "channelName"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
            throw r2
        L_0x0085:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.ChannelRow.updateViews():void");
    }

    private final void updateImportance() {
        NotificationChannel notificationChannel = this.channel;
        if ((notificationChannel != null ? notificationChannel.getImportance() : 0) == -1000) {
        }
    }
}
