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
    /* access modifiers changed from: private */
    public final ActivityStarter activityStarter;
    private final QueryDataUsageHandler bgHandler;
    private TextView dataUsage;
    private final H handler;
    /* access modifiers changed from: private */
    public Intent intent1;
    /* access modifiers changed from: private */
    public Intent intent2;
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
    public final void setDataUsage(DataUsageInfo dataUsageInfo) {
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
        if (this.isAvailable == null || (!Intrinsics.areEqual((Object) Boolean.valueOf(isDataUsageAvailable), (Object) this.isAvailable))) {
            this.isAvailable = Boolean.valueOf(isDataUsageAvailable);
            setVisibility(isDataUsageAvailable ? 0 : 8);
            QSContainerImpl qSContainerImpl = this.qsContainer;
            if (qSContainerImpl != null) {
                qSContainerImpl.updateExpansion(true);
            }
        }
    }

    /* compiled from: QSFooterDataUsage.kt */
    private final class H extends Handler {
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

    /* compiled from: QSFooterDataUsage.kt */
    private final class QueryDataUsageHandler extends Handler {
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

    /* JADX WARNING: type inference failed for: r3v0 */
    /* JADX WARNING: type inference failed for: r3v2, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r3v4 */
    /* JADX WARNING: type inference failed for: r3v6, types: [java.lang.CharSequence] */
    /* JADX WARNING: type inference failed for: r3v12 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0143  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:55:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:57:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void queryDataUsage() {
        /*
            r11 = this;
            java.lang.String r0 = "context"
            java.lang.String r1 = "QSFooterDataUsage"
            boolean r2 = com.android.systemui.statusbar.phone.StatusBar.isBootCompleted()
            if (r2 != 0) goto L_0x000b
            return
        L_0x000b:
            com.android.systemui.qs.QSFooterDataUsage$DataUsageInfo r2 = new com.android.systemui.qs.QSFooterDataUsage$DataUsageInfo
            r2.<init>(r11)
            r3 = 0
            android.content.Context r4 = r11.getContext()     // Catch:{ Exception -> 0x011a }
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r4, r0)     // Catch:{ Exception -> 0x011a }
            android.content.ContentResolver r5 = r4.getContentResolver()     // Catch:{ Exception -> 0x011a }
            android.net.Uri r6 = r11.networkUri     // Catch:{ Exception -> 0x011a }
            r7 = 0
            r8 = 0
            r9 = 0
            r10 = 0
            android.database.Cursor r4 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x011a }
            if (r4 == 0) goto L_0x00f9
            boolean r5 = r4.moveToFirst()     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r6 = 1
            if (r5 != r6) goto L_0x00f9
            java.lang.String r5 = "text1"
            int r5 = r4.getColumnIndex(r5)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r5 = r4.getString(r5)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r7 = "cursor.getString(cursor.…olumnIndex(COLUMN_TEXT1))"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r7)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r7 = "text2"
            int r7 = r4.getColumnIndex(r7)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r7 = r4.getString(r7)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r8 = "cursor.getString(cursor.…olumnIndex(COLUMN_TEXT2))"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r7, r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            boolean r8 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            if (r8 == 0) goto L_0x0062
            boolean r8 = android.text.TextUtils.isEmpty(r7)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            if (r8 == 0) goto L_0x0062
            java.lang.String r11 = "queryDataUsage: cannot find text1, text2."
            android.util.Log.d(r1, r11)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r4.close()
            return
        L_0x0062:
            java.lang.String r8 = "icon"
            int r8 = r4.getColumnIndex(r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r8 = r4.getString(r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r9 = "cursor.getString(cursor.…ColumnIndex(COLUMN_ICON))"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r9)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.net.Uri r8 = android.net.Uri.parse(r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.content.Context r9 = r11.getContext()     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r9, r0)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.content.ContentResolver r0 = r9.getContentResolver()     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r9 = "r"
            android.os.ParcelFileDescriptor r0 = r0.openFileDescriptor(r8, r9)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.os.ParcelFileDescriptor$AutoCloseInputStream r8 = new android.os.ParcelFileDescriptor$AutoCloseInputStream     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r8.<init>(r0)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.graphics.Bitmap r0 = android.graphics.BitmapFactory.decodeStream(r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            if (r0 != 0) goto L_0x009a
            java.lang.String r11 = "queryDataUsage: cannot load icon."
            android.util.Log.d(r1, r11)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r4.close()
            return
        L_0x009a:
            java.lang.String r8 = "action1"
            int r8 = r4.getColumnIndex(r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r8 = r4.getString(r8)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r9 = "cursor.getString(cursor.…umnIndex(COLUMN_ACTION1))"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r9)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r9 = "action2"
            int r9 = r4.getColumnIndex(r9)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r9 = r4.getString(r9)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r10 = "cursor.getString(cursor.…umnIndex(COLUMN_ACTION2))"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r9, r10)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.content.Intent r8 = android.content.Intent.parseUri(r8, r6)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r11.intent1 = r8     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.content.Intent r8 = android.content.Intent.parseUri(r9, r6)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r11.intent2 = r8     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.content.Intent r9 = r11.intent1     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            if (r9 != 0) goto L_0x00d3
            if (r8 != 0) goto L_0x00d3
            java.lang.String r11 = "queryDataUsage: cannot find action1, action2."
            android.util.Log.d(r1, r11)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r4.close()
            return
        L_0x00d3:
            r2.setDataUsageAvailable(r6)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            if (r5 == 0) goto L_0x00e9
            java.lang.String r3 = "&nbsp;"
            kotlin.text.Regex r6 = new kotlin.text.Regex     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r6.<init>((java.lang.String) r3)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            java.lang.String r3 = "&ensp;"
            java.lang.String r3 = r6.replace(r5, r3)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.text.Spanned r3 = android.text.Html.fromHtml(r3)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
        L_0x00e9:
            r2.setText1(r3)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r2.setText2(r7)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r2.setIconImage(r0)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            goto L_0x00fd
        L_0x00f3:
            r11 = move-exception
            r3 = r4
            goto L_0x0147
        L_0x00f6:
            r11 = move-exception
            r3 = r4
            goto L_0x011b
        L_0x00f9:
            r0 = 0
            r2.setDataUsageAvailable(r0)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
        L_0x00fd:
            com.android.systemui.qs.QSFooterDataUsage$H r0 = r11.handler     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r3 = 100000(0x186a0, float:1.4013E-40)
            r0.removeMessages(r3)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            android.os.Message r0 = android.os.Message.obtain()     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r0.what = r3     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r0.obj = r2     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            com.android.systemui.qs.QSFooterDataUsage$H r11 = r11.handler     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            r11.sendMessage(r0)     // Catch:{ Exception -> 0x00f6, all -> 0x00f3 }
            if (r4 == 0) goto L_0x0146
            r4.close()
            goto L_0x0146
        L_0x0118:
            r11 = move-exception
            goto L_0x0147
        L_0x011a:
            r11 = move-exception
        L_0x011b:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x0118 }
            r0.<init>()     // Catch:{ all -> 0x0118 }
            java.lang.Throwable r2 = r11.getCause()     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x0118 }
            r0.append(r2)     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = ", "
            r0.append(r2)     // Catch:{ all -> 0x0118 }
            java.lang.String r2 = r11.getMessage()     // Catch:{ all -> 0x0118 }
            r0.append(r2)     // Catch:{ all -> 0x0118 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x0118 }
            android.util.Log.d(r1, r0)     // Catch:{ all -> 0x0118 }
            r11.printStackTrace()     // Catch:{ all -> 0x0118 }
            if (r3 == 0) goto L_0x0146
            r3.close()
        L_0x0146:
            return
        L_0x0147:
            if (r3 == 0) goto L_0x014c
            r3.close()
        L_0x014c:
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QSFooterDataUsage.queryDataUsage():void");
    }

    /* compiled from: QSFooterDataUsage.kt */
    private final class DataUsageInfo {
        @Nullable
        private Bitmap iconImage;
        private boolean isDataUsageAvailable;
        @Nullable
        private CharSequence text1;
        @Nullable
        private String text2;

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
