package com.android.systemui.qs;

import android.content.Intent;
import android.view.View;
import com.android.systemui.Dependency;
import com.miui.systemui.analytics.SystemUIStat;

/* compiled from: QSFooterDataUsage.kt */
final class QSFooterDataUsage$onFinishInflate$1 implements View.OnClickListener {
    final /* synthetic */ QSFooterDataUsage this$0;

    QSFooterDataUsage$onFinishInflate$1(QSFooterDataUsage qSFooterDataUsage) {
        this.this$0 = qSFooterDataUsage;
    }

    public final void onClick(View view) {
        Intent access$getIntent1$p = this.this$0.intent1;
        if (access$getIntent1$p != null) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("data_usage_footer");
            this.this$0.activityStarter.startActivity(access$getIntent1$p, true);
        }
    }
}
