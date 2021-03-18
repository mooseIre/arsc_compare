package com.android.systemui.qs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.plugins.ActivityStarter;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressLint({"ViewConstructor"})
/* compiled from: QSFooterDataUsage.kt */
public final class QSFooterDataUsage extends FrameLayout {
    private final ActivityStarter activityStarter;
    private final QueryDataUsageHandler bgHandler;
    private TextView dataUsage;
    private final H handler;
    private Intent intent1;
    private Intent intent2;
    private Boolean isAvailable;
    private final Uri networkUri = Uri.parse("content://com.miui.networkassistant.provider/datausage_noti_status");
    private ImageView pieImage;
    private TextView purchase;
    private QSContainerImpl qsContainer;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public QSFooterDataUsage(@NotNull Context context, @Nullable AttributeSet attributeSet, @NotNull ActivityStarter activityStarter2, @NotNull Looper looper, @NotNull Looper looper2) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(activityStarter2, "activityStarter");
        Intrinsics.checkParameterIsNotNull(looper, "uiLooper");
        Intrinsics.checkParameterIsNotNull(looper2, "bgLooper");
        this.activityStarter = activityStarter2;
        this.handler = new H(this, looper);
        this.bgHandler = new QueryDataUsageHandler(this, looper2);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0015R$id.pie);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.pie)");
        this.pieImage = (ImageView) findViewById;
        View findViewById2 = findViewById(C0015R$id.data_usage);
        Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.data_usage)");
        TextView textView = (TextView) findViewById2;
        this.dataUsage = textView;
        if (textView != null) {
            textView.setOnClickListener(new QSFooterDataUsage$onFinishInflate$1(this));
            View findViewById3 = findViewById(C0015R$id.data_purchase);
            if (findViewById3 != null) {
                TextView textView2 = (TextView) findViewById3;
                this.purchase = textView2;
                if (textView2 != null) {
                    textView2.setOnClickListener(new QSFooterDataUsage$onFinishInflate$2(this));
                    TextView textView3 = this.purchase;
                    if (textView3 != null) {
                        textView3.setVisibility(0);
                        updateDataUsageInfo();
                        return;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("purchase");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("purchase");
                throw null;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.widget.TextView");
        }
        Intrinsics.throwUninitializedPropertyAccessException("dataUsage");
        throw null;
    }

    public final void setQSContainer(@Nullable QSContainerImpl qSContainerImpl) {
        this.qsContainer = qSContainerImpl;
    }

    public final void updateDataUsageInfo() {
        this.bgHandler.removeMessages(100);
        this.bgHandler.sendEmptyMessage(100);
    }

    public final boolean isAvailable() {
        Boolean bool = this.isAvailable;
        if (bool == null) {
            return false;
        }
        if (bool != null) {
            return bool.booleanValue();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void setDataUsage(DataUsageInfo dataUsageInfo) {
        boolean isDataUsageAvailable = dataUsageInfo.isDataUsageAvailable();
        if (isDataUsageAvailable) {
            ImageView imageView = this.pieImage;
            if (imageView != null) {
                imageView.setImageBitmap(dataUsageInfo.getIconImage());
                TextView textView = this.dataUsage;
                if (textView != null) {
                    textView.setText(dataUsageInfo.getText1());
                    TextView textView2 = this.purchase;
                    if (textView2 != null) {
                        textView2.setText(dataUsageInfo.getText2());
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("purchase");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("dataUsage");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("pieImage");
                throw null;
            }
        }
        if (this.isAvailable == null || (!Intrinsics.areEqual(Boolean.valueOf(isDataUsageAvailable), this.isAvailable))) {
            this.isAvailable = Boolean.valueOf(isDataUsageAvailable);
            setVisibility(isDataUsageAvailable ? 0 : 8);
            QSContainerImpl qSContainerImpl = this.qsContainer;
            if (qSContainerImpl != null) {
                qSContainerImpl.updateExpansion(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: QSFooterDataUsage.kt */
    public final class H extends Handler {
        final /* synthetic */ QSFooterDataUsage this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public H(@NotNull QSFooterDataUsage qSFooterDataUsage, Looper looper) {
            super(looper);
            Intrinsics.checkParameterIsNotNull(looper, "looper");
            this.this$0 = qSFooterDataUsage;
        }

        public void handleMessage(@NotNull Message message) {
            Intrinsics.checkParameterIsNotNull(message, "msg");
            if (message.what == 100000) {
                QSFooterDataUsage qSFooterDataUsage = this.this$0;
                Object obj = message.obj;
                if (obj != null) {
                    qSFooterDataUsage.setDataUsage((DataUsageInfo) obj);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.qs.QSFooterDataUsage.DataUsageInfo");
            }
        }
    }

    /* access modifiers changed from: private */
    /* compiled from: QSFooterDataUsage.kt */
    public final class QueryDataUsageHandler extends Handler {
        final /* synthetic */ QSFooterDataUsage this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public QueryDataUsageHandler(@NotNull QSFooterDataUsage qSFooterDataUsage, Looper looper) {
            super(looper);
            Intrinsics.checkParameterIsNotNull(looper, "looper");
            this.this$0 = qSFooterDataUsage;
        }

        public void handleMessage(@NotNull Message message) {
            Intrinsics.checkParameterIsNotNull(message, "msg");
            if (message.what == 100) {
                this.this$0.queryDataUsage();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0143  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:48:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void queryDataUsage() {
        /*
        // Method dump skipped, instructions count: 333
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QSFooterDataUsage.queryDataUsage():void");
    }

    /* access modifiers changed from: private */
    /* compiled from: QSFooterDataUsage.kt */
    public final class DataUsageInfo {
        @Nullable
        private Bitmap iconImage;
        private boolean isDataUsageAvailable;
        @Nullable
        private CharSequence text1;
        @Nullable
        private String text2;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        public DataUsageInfo(QSFooterDataUsage qSFooterDataUsage) {
        }

        @Nullable
        public final String getText2() {
            return this.text2;
        }

        public final void setText2(@Nullable String str) {
            this.text2 = str;
        }

        @Nullable
        public final CharSequence getText1() {
            return this.text1;
        }

        public final void setText1(@Nullable CharSequence charSequence) {
            this.text1 = charSequence;
        }

        @Nullable
        public final Bitmap getIconImage() {
            return this.iconImage;
        }

        public final void setIconImage(@Nullable Bitmap bitmap) {
            this.iconImage = bitmap;
        }

        public final boolean isDataUsageAvailable() {
            return this.isDataUsageAvailable;
        }

        public final void setDataUsageAvailable(boolean z) {
            this.isDataUsageAvailable = z;
        }
    }
}
