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
        this.mDataUsage = (TextView) findViewById(R.id.data_usage);
        this.mDataUsage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (QSFooterDataUsage.this.mIntent1 != null) {
                    ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("data_usage_footer");
                    ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(QSFooterDataUsage.this.mIntent1, true);
                }
            }
        });
        this.mPurchase = (TextView) findViewById(R.id.data_purchase);
        this.mPurchase.setOnClickListener(new View.OnClickListener() {
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
    /* JADX WARNING: type inference failed for: r2v1, types: [android.database.Cursor] */
    /* JADX WARNING: type inference failed for: r2v5 */
    /* JADX WARNING: type inference failed for: r2v6, types: [java.lang.CharSequence] */
    /* JADX WARNING: type inference failed for: r2v10 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:64:? A[RETURN, SYNTHETIC] */
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
            android.content.Context r3 = r10.mContext     // Catch:{ Exception -> 0x00f8 }
            android.content.ContentResolver r4 = r3.getContentResolver()     // Catch:{ Exception -> 0x00f8 }
            android.net.Uri r5 = r10.mNetworkUri     // Catch:{ Exception -> 0x00f8 }
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r3 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x00f8 }
            if (r3 == 0) goto L_0x00d1
            boolean r4 = r3.moveToFirst()     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r4 == 0) goto L_0x00d1
            java.lang.String r4 = "text1"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r4 = r3.getString(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r5 = "text2"
            int r5 = r3.getColumnIndex(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r5 = r3.getString(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            boolean r6 = android.text.TextUtils.isEmpty(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r6 == 0) goto L_0x0052
            boolean r6 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r6 == 0) goto L_0x0052
            java.lang.String r10 = "queryDataUsage: cannot find text1, text2."
            android.util.Log.d(r0, r10)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r3 == 0) goto L_0x0051
            r3.close()
        L_0x0051:
            return
        L_0x0052:
            java.lang.String r6 = "icon"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.net.Uri r6 = android.net.Uri.parse(r6)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.content.Context r7 = r10.mContext     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.content.ContentResolver r7 = r7.getContentResolver()     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r8 = "r"
            android.os.ParcelFileDescriptor r6 = r7.openFileDescriptor(r6, r8)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.os.ParcelFileDescriptor$AutoCloseInputStream r7 = new android.os.ParcelFileDescriptor$AutoCloseInputStream     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r7.<init>(r6)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.graphics.Bitmap r6 = android.graphics.BitmapFactory.decodeStream(r7)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r6 != 0) goto L_0x0082
            java.lang.String r10 = "queryDataUsage: cannot load icon."
            android.util.Log.d(r0, r10)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r3 == 0) goto L_0x0081
            r3.close()
        L_0x0081:
            return
        L_0x0082:
            java.lang.String r7 = "action1"
            int r7 = r3.getColumnIndex(r7)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r7 = r3.getString(r7)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r8 = "action2"
            int r8 = r3.getColumnIndex(r8)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            java.lang.String r8 = r3.getString(r8)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r9 = 1
            android.content.Intent r7 = android.content.Intent.parseUri(r7, r9)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r10.mIntent1 = r7     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.content.Intent r7 = android.content.Intent.parseUri(r8, r9)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r10.mIntent2 = r7     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.content.Intent r7 = r10.mIntent1     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r7 != 0) goto L_0x00b6
            android.content.Intent r7 = r10.mIntent2     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r7 != 0) goto L_0x00b6
            java.lang.String r10 = "queryDataUsage: cannot find action1, action2."
            android.util.Log.d(r0, r10)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r3 == 0) goto L_0x00b5
            r3.close()
        L_0x00b5:
            return
        L_0x00b6:
            r1.setDataUsageAvailable(r9)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r4 == 0) goto L_0x00c7
            java.lang.String r2 = "&nbsp;"
            java.lang.String r7 = "&ensp;"
            java.lang.String r2 = r4.replaceAll(r2, r7)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.text.Spanned r2 = android.text.Html.fromHtml(r2)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
        L_0x00c7:
            r1.setText1(r2)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r1.setText2(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r1.setIconImage(r6)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            goto L_0x00d5
        L_0x00d1:
            r2 = 0
            r1.setDataUsageAvailable(r2)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
        L_0x00d5:
            android.os.Handler r2 = r10.mHandler     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r4 = 100000(0x186a0, float:1.4013E-40)
            r2.removeMessages(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.os.Message r2 = android.os.Message.obtain()     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r2.what = r4     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r2.obj = r1     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            android.os.Handler r10 = r10.mHandler     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            r10.sendMessage(r2)     // Catch:{ Exception -> 0x00f2, all -> 0x00f0 }
            if (r3 == 0) goto L_0x0120
            r3.close()
            goto L_0x0120
        L_0x00f0:
            r10 = move-exception
            goto L_0x0121
        L_0x00f2:
            r10 = move-exception
            r2 = r3
            goto L_0x00f9
        L_0x00f5:
            r10 = move-exception
            r3 = r2
            goto L_0x0121
        L_0x00f8:
            r10 = move-exception
        L_0x00f9:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00f5 }
            r1.<init>()     // Catch:{ all -> 0x00f5 }
            java.lang.Throwable r3 = r10.getCause()     // Catch:{ all -> 0x00f5 }
            r1.append(r3)     // Catch:{ all -> 0x00f5 }
            java.lang.String r3 = ", "
            r1.append(r3)     // Catch:{ all -> 0x00f5 }
            java.lang.String r3 = r10.getMessage()     // Catch:{ all -> 0x00f5 }
            r1.append(r3)     // Catch:{ all -> 0x00f5 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00f5 }
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x00f5 }
            r10.printStackTrace()     // Catch:{ all -> 0x00f5 }
            if (r2 == 0) goto L_0x0120
            r2.close()
        L_0x0120:
            return
        L_0x0121:
            if (r3 == 0) goto L_0x0126
            r3.close()
        L_0x0126:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QSFooterDataUsage.queryDataUsage():void");
    }
}
