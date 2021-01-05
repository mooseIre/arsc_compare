package com.android.systemui.qs;

import android.content.Intent;
import android.view.View;
import com.android.systemui.Dependency;
import com.miui.systemui.analytics.SystemUIStat;

/* compiled from: QSFooterDataUsage.kt */
final class QSFooterDataUsage$onFinishInflate$2 implements View.OnClickListener {
    final /* synthetic */ QSFooterDataUsage this$0;

    QSFooterDataUsage$onFinishInflate$2(QSFooterDataUsage qSFooterDataUsage) {
        this.this$0 = qSFooterDataUsage;
    }

    public final void onClick(View view) {
        Intent access$getIntent2$p = this.this$0.intent2;
        if (access$getIntent2$p != null) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("data_usage_purchase");
            this.this$0.activityStarter.startActivity(access$getIntent2$p, true);
        }
    }
}
