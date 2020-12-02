package com.android.systemui.statusbar.notification.row;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ChannelEditorDialogController.kt */
public final class ChannelEditorDialog extends Dialog {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChannelEditorDialog(@NotNull Context context) {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
    }

    public final void updateDoneButtonText(boolean z) {
        int i;
        TextView textView = (TextView) findViewById(C0015R$id.done_button);
        if (textView != null) {
            if (z) {
                i = C0021R$string.inline_ok_button;
            } else {
                i = C0021R$string.inline_done_button;
            }
            textView.setText(i);
        }
    }

    /* compiled from: ChannelEditorDialogController.kt */
    public static final class Builder {
        private Context context;

        @NotNull
        public final Builder setContext(@NotNull Context context2) {
            Intrinsics.checkParameterIsNotNull(context2, "context");
            this.context = context2;
            return this;
        }

        @NotNull
        public final ChannelEditorDialog build() {
            Context context2 = this.context;
            if (context2 != null) {
                return new ChannelEditorDialog(context2);
            }
            Intrinsics.throwUninitializedPropertyAccessException("context");
            throw null;
        }
    }
}
