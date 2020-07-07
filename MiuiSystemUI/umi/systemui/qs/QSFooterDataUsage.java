package com.android.systemui.qs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;

public class QSFooterDataUsage extends FrameLayout {
    private boolean mAvailable = false;
    private Handler mBgHandler;
    private TextView mDataUsage;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 100000) {
                QSFooterDataUsage.this.setDataUsage((DataUsageInfo) message.obj);
            }
        }
    };
    /* access modifiers changed from: private */
    public Intent mIntent1;
    /* access modifiers changed from: private */
    public Intent mIntent2;
    private Uri mNetworkUri;
    private ImageView mPieImage;
    private TextView mPurchase;
    private QSContainerImpl mQSContainer;

    public QSFooterDataUsage(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initNetworkAssistantProviderUri();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPieImage = (ImageView) findViewById(R.id.pie);
        TextView textView = (TextView) findViewById(R.id.data_usage);
        this.mDataUsage = textView;
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (QSFooterDataUsage.this.mIntent1 != null) {
                    ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("data_usage_footer");
                    ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(QSFooterDataUsage.this.mIntent1, true);
                }
            }
        });
        TextView textView2 = (TextView) findViewById(R.id.data_purchase);
        this.mPurchase = textView2;
        textView2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (QSFooterDataUsage.this.mIntent2 != null) {
                    ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("data_usage_purchase");
                    ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(QSFooterDataUsage.this.mIntent2, true);
                }
            }
        });
        this.mPurchase.setVisibility(0);
        this.mBgHandler = new QueryDataUsageHandler((Looper) Dependency.get(Dependency.BG_LOOPER));
        updateDataUsageInfo();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void updateDataUsageInfo() {
        Handler handler = this.mBgHandler;
        if (handler != null) {
            handler.removeMessages(100);
            this.mBgHandler.sendEmptyMessage(100);
        }
    }

    public void setQSContainer(QSContainerImpl qSContainerImpl) {
        this.mQSContainer = qSContainerImpl;
    }

    /* access modifiers changed from: private */
    public void setDataUsage(DataUsageInfo dataUsageInfo) {
        boolean isDataUsageAvailable = dataUsageInfo.isDataUsageAvailable();
        if (isDataUsageAvailable) {
            this.mPieImage.setImageBitmap(dataUsageInfo.getIconImage());
            this.mDataUsage.setText(dataUsageInfo.getText1());
            this.mPurchase.setText(dataUsageInfo.getText2());
        }
        if (isDataUsageAvailable != this.mAvailable) {
            this.mAvailable = isDataUsageAvailable;
            QSContainerImpl qSContainerImpl = this.mQSContainer;
            if (qSContainerImpl != null) {
                qSContainerImpl.updateFooter();
            }
        }
    }

    private final class QueryDataUsageHandler extends Handler {
        QueryDataUsageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 100) {
                QSFooterDataUsage.this.queryDataUsage();
            }
        }
    }

    protected class DataUsageInfo {
        private boolean mDataUsageAvailable;
        private Bitmap mIconImage;
        private CharSequence mText1;
        private String mText2;

        protected DataUsageInfo() {
        }

        public String getText2() {
            return this.mText2;
        }

        public void setText2(String str) {
            this.mText2 = str;
        }

        public CharSequence getText1() {
            return this.mText1;
        }

        public void setText1(CharSequence charSequence) {
            this.mText1 = charSequence;
        }

        public Bitmap getIconImage() {
            return this.mIconImage;
        }

        public void setIconImage(Bitmap bitmap) {
            this.mIconImage = bitmap;
        }

        public boolean isDataUsageAvailable() {
            return this.mDataUsageAvailable;
        }

        public void setDataUsageAvailable(boolean z) {
            this.mDataUsageAvailable = z;
        }
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    private void initNetworkAssistantProviderUri() {
        this.mNetworkUri = Uri.parse("content://com.miui.networkassistant.provider/datausage_noti_status");
    }

    /* JADX WARNING: type inference failed for: r2v0 */
    /* JADX WARNING: type inference failed for: r2v2, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r2v6 */
    /* JADX WARNING: type inference failed for: r2v8, types: [java.lang.CharSequence] */
    /* JADX WARNING: type inference failed for: r2v13 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0121  */
    /* JADX WARNING: Removed duplicated region for block: B:61:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:63:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void queryDataUsage() {
        /*
            r10 = this;
            java.lang.String r0 = "QSFooterDataUsage"
            boolean r1 = com.android.systemui.statusbar.phone.StatusBar.sBootCompleted
            if (r1 != 0) goto L_0x0007
            return
        L_0x0007:
            com.android.systemui.qs.QSFooterDataUsage$DataUsageInfo r1 = new com.android.systemui.qs.QSFooterDataUsage$DataUsageInfo
            r1.<init>()
            r2 = 0
            android.content.Context r3 = r10.mContext     // Catch:{ Exception -> 0x00f6 }
            android.content.ContentResolver r4 = r3.getContentResolver()     // Catch:{ Exception -> 0x00f6 }
            android.net.Uri r5 = r10.mNetworkUri     // Catch:{ Exception -> 0x00f6 }
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r3 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x00f6 }
            if (r3 == 0) goto L_0x00cf
            boolean r4 = r3.moveToFirst()     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r4 == 0) goto L_0x00cf
            java.lang.String r4 = "text1"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r4 = r3.getString(r4)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r5 = "text2"
            int r5 = r3.getColumnIndex(r5)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r5 = r3.getString(r5)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            boolean r6 = android.text.TextUtils.isEmpty(r4)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r6 == 0) goto L_0x0052
            boolean r6 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r6 == 0) goto L_0x0052
            java.lang.String r10 = "queryDataUsage: cannot find text1, text2."
            android.util.Log.d(r0, r10)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r3 == 0) goto L_0x0051
            r3.close()
        L_0x0051:
            return
        L_0x0052:
            java.lang.String r6 = "icon"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.net.Uri r6 = android.net.Uri.parse(r6)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.content.Context r7 = r10.mContext     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.content.ContentResolver r7 = r7.getContentResolver()     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r8 = "r"
            android.os.ParcelFileDescriptor r6 = r7.openFileDescriptor(r6, r8)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.os.ParcelFileDescriptor$AutoCloseInputStream r7 = new android.os.ParcelFileDescriptor$AutoCloseInputStream     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r7.<init>(r6)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.graphics.Bitmap r6 = android.graphics.BitmapFactory.decodeStream(r7)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r6 != 0) goto L_0x0082
            java.lang.String r10 = "queryDataUsage: cannot load icon."
            android.util.Log.d(r0, r10)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r3 == 0) goto L_0x0081
            r3.close()
        L_0x0081:
            return
        L_0x0082:
            java.lang.String r7 = "action1"
            int r7 = r3.getColumnIndex(r7)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r7 = r3.getString(r7)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r8 = "action2"
            int r8 = r3.getColumnIndex(r8)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            java.lang.String r8 = r3.getString(r8)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r9 = 1
            android.content.Intent r7 = android.content.Intent.parseUri(r7, r9)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r10.mIntent1 = r7     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.content.Intent r7 = android.content.Intent.parseUri(r8, r9)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r10.mIntent2 = r7     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.content.Intent r8 = r10.mIntent1     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r8 != 0) goto L_0x00b4
            if (r7 != 0) goto L_0x00b4
            java.lang.String r10 = "queryDataUsage: cannot find action1, action2."
            android.util.Log.d(r0, r10)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r3 == 0) goto L_0x00b3
            r3.close()
        L_0x00b3:
            return
        L_0x00b4:
            r1.setDataUsageAvailable(r9)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r4 == 0) goto L_0x00c5
            java.lang.String r2 = "&nbsp;"
            java.lang.String r7 = "&ensp;"
            java.lang.String r2 = r4.replaceAll(r2, r7)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.text.Spanned r2 = android.text.Html.fromHtml(r2)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
        L_0x00c5:
            r1.setText1(r2)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r1.setText2(r5)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r1.setIconImage(r6)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            goto L_0x00d3
        L_0x00cf:
            r2 = 0
            r1.setDataUsageAvailable(r2)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
        L_0x00d3:
            android.os.Handler r2 = r10.mHandler     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r4 = 100000(0x186a0, float:1.4013E-40)
            r2.removeMessages(r4)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.os.Message r2 = android.os.Message.obtain()     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r2.what = r4     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r2.obj = r1     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            android.os.Handler r10 = r10.mHandler     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            r10.sendMessage(r2)     // Catch:{ Exception -> 0x00f1, all -> 0x00ee }
            if (r3 == 0) goto L_0x011e
            r3.close()
            goto L_0x011e
        L_0x00ee:
            r10 = move-exception
            r2 = r3
            goto L_0x011f
        L_0x00f1:
            r10 = move-exception
            r2 = r3
            goto L_0x00f7
        L_0x00f4:
            r10 = move-exception
            goto L_0x011f
        L_0x00f6:
            r10 = move-exception
        L_0x00f7:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00f4 }
            r1.<init>()     // Catch:{ all -> 0x00f4 }
            java.lang.Throwable r3 = r10.getCause()     // Catch:{ all -> 0x00f4 }
            r1.append(r3)     // Catch:{ all -> 0x00f4 }
            java.lang.String r3 = ", "
            r1.append(r3)     // Catch:{ all -> 0x00f4 }
            java.lang.String r3 = r10.getMessage()     // Catch:{ all -> 0x00f4 }
            r1.append(r3)     // Catch:{ all -> 0x00f4 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00f4 }
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x00f4 }
            r10.printStackTrace()     // Catch:{ all -> 0x00f4 }
            if (r2 == 0) goto L_0x011e
            r2.close()
        L_0x011e:
            return
        L_0x011f:
            if (r2 == 0) goto L_0x0124
            r2.close()
        L_0x0124:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QSFooterDataUsage.queryDataUsage():void");
    }
}
