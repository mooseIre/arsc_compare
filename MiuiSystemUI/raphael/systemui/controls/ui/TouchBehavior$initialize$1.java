package com.android.systemui.controls.ui;

import android.service.controls.templates.StatelessTemplate;
import android.view.View;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: TouchBehavior.kt */
final class TouchBehavior$initialize$1 implements View.OnClickListener {
    final /* synthetic */ ControlViewHolder $cvh;
    final /* synthetic */ TouchBehavior this$0;

    TouchBehavior$initialize$1(TouchBehavior touchBehavior, ControlViewHolder controlViewHolder) {
        this.this$0 = touchBehavior;
        this.$cvh = controlViewHolder;
    }

    public final void onClick(View view) {
        ControlActionCoordinator controlActionCoordinator = this.$cvh.getControlActionCoordinator();
        ControlViewHolder controlViewHolder = this.$cvh;
        String templateId = this.this$0.getTemplate().getTemplateId();
        Intrinsics.checkExpressionValueIsNotNull(templateId, "template.getTemplateId()");
        controlActionCoordinator.touch(controlViewHolder, templateId, this.this$0.getControl());
        if (this.this$0.getTemplate() instanceof StatelessTemplate) {
            this.this$0.statelessTouch = true;
            ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(this.$cvh, this.this$0.getEnabled(), this.this$0.lastColorOffset, false, 4, null);
            this.$cvh.getUiExecutor().executeDelayed(new Runnable(this) {
                /* class com.android.systemui.controls.ui.TouchBehavior$initialize$1.AnonymousClass1 */
                final /* synthetic */ TouchBehavior$initialize$1 this$0;

                {
                    this.this$0 = r1;
                }

                public final void run() {
                    this.this$0.this$0.statelessTouch = false;
                    TouchBehavior$initialize$1 touchBehavior$initialize$1 = this.this$0;
                    ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(touchBehavior$initialize$1.$cvh, touchBehavior$initialize$1.this$0.getEnabled(), this.this$0.this$0.lastColorOffset, false, 4, null);
                }
            }, 3000);
        }
    }
}
