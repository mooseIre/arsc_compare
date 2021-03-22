package com.android.systemui.statusbar.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ClickableToast.kt */
public final class ClickableToast implements IClickableToast {
    public static final Companion Companion = new Companion(null);
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    @NotNull
    private final Context context;
    private Runnable mCancelRunnable;
    private WindowManager.LayoutParams mParams;
    private Runnable mShowRunnable;
    private View mView = LayoutInflater.from(this.context).inflate(C0017R$layout.clickable_toast, (ViewGroup) null);
    private WindowManager mWindowManager;

    @NotNull
    public static final IClickableToast showToast(@NotNull Context context2) {
        return Companion.showToast(context2);
    }

    public ClickableToast(@NotNull Context context2) {
        Context context3;
        Resources resources;
        Intrinsics.checkParameterIsNotNull(context2, "context");
        this.context = context2;
        this.mWindowManager = (WindowManager) context2.getSystemService("window");
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.mParams = layoutParams;
        if (layoutParams != null) {
            layoutParams.height = -2;
        }
        WindowManager.LayoutParams layoutParams2 = this.mParams;
        if (layoutParams2 != null) {
            layoutParams2.width = -2;
        }
        WindowManager.LayoutParams layoutParams3 = this.mParams;
        if (layoutParams3 != null) {
            layoutParams3.format = -3;
        }
        WindowManager.LayoutParams layoutParams4 = this.mParams;
        if (layoutParams4 != null) {
            layoutParams4.windowAnimations = 16973828;
        }
        WindowManager.LayoutParams layoutParams5 = this.mParams;
        if (layoutParams5 != null) {
            layoutParams5.type = 2038;
        }
        WindowManager.LayoutParams layoutParams6 = this.mParams;
        if (layoutParams6 != null) {
            layoutParams6.flags = 160;
        }
        View view = this.mView;
        Configuration configuration = (view == null || (context3 = view.getContext()) == null || (resources = context3.getResources()) == null) ? null : resources.getConfiguration();
        int integer = this.context.getResources().getInteger(17694915);
        if (configuration != null) {
            int absoluteGravity = Gravity.getAbsoluteGravity(integer, configuration.getLayoutDirection());
            WindowManager.LayoutParams layoutParams7 = this.mParams;
            if (layoutParams7 != null) {
                layoutParams7.gravity = absoluteGravity;
            }
            WindowManager.LayoutParams layoutParams8 = this.mParams;
            if (layoutParams8 != null) {
                layoutParams8.y = this.context.getResources().getDimensionPixelOffset(17105547);
            }
            this.mCancelRunnable = new Runnable(this) {
                /* class com.android.systemui.statusbar.views.ClickableToast.AnonymousClass1 */
                final /* synthetic */ ClickableToast this$0;

                {
                    this.this$0 = r1;
                }

                public final void run() {
                    WindowManager windowManager;
                    if ((!(this.this$0.getContext() instanceof Activity) || !((Activity) this.this$0.getContext()).isFinishing()) && (windowManager = this.this$0.mWindowManager) != null) {
                        windowManager.removeViewImmediate(this.this$0.mView);
                    }
                    this.this$0.mParams = null;
                    this.this$0.mWindowManager = null;
                    this.this$0.mView = null;
                }
            };
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }

    /* compiled from: ClickableToast.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final IClickableToast showToast(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return new ClickableToast(context);
        }
    }

    @Override // com.android.systemui.statusbar.views.IClickableToast
    public void show() {
        if (this.mShowRunnable != null) {
            sHandler.removeCallbacksAndMessages(null);
        }
        ClickableToast$show$1 clickableToast$show$1 = new ClickableToast$show$1(this);
        this.mShowRunnable = clickableToast$show$1;
        Handler handler = sHandler;
        if (clickableToast$show$1 != null) {
            handler.post(clickableToast$show$1);
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    @Override // com.android.systemui.statusbar.views.IClickableToast
    @NotNull
    public IClickableToast setClickListener(@NotNull View.OnClickListener onClickListener) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "listener");
        View view = this.mView;
        TextView textView = view != null ? (TextView) view.findViewById(C0015R$id.click_btn_text) : null;
        if (textView != null) {
            textView.setVisibility(0);
        }
        if (textView != null) {
            textView.setOnClickListener(onClickListener);
        }
        return this;
    }

    @Override // com.android.systemui.statusbar.views.IClickableToast
    @NotNull
    public IClickableToast setText(@NotNull CharSequence charSequence) {
        Intrinsics.checkParameterIsNotNull(charSequence, "text");
        View view = this.mView;
        TextView textView = view != null ? (TextView) view.findViewById(C0015R$id.click_btn_text) : null;
        if (textView != null) {
            textView.setText(charSequence);
        }
        return this;
    }
}
