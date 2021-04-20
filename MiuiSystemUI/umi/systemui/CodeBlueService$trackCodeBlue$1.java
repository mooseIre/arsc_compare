package com.android.systemui;

/* access modifiers changed from: package-private */
/* compiled from: CodeBlueService.kt */
public final class CodeBlueService$trackCodeBlue$1 implements Runnable {
    final /* synthetic */ CodeBlueService this$0;

    CodeBlueService$trackCodeBlue$1(CodeBlueService codeBlueService) {
        this.this$0 = codeBlueService;
    }

    public final void run() {
        if (CodeBlueConfig.Companion.getTrackCodeBlue(this.this$0.getContext())) {
            this.this$0.trackCodeBlueEvent();
            CodeBlueConfig.Companion.setTrackCodeBlue(this.this$0.getContext(), false);
        }
    }
}
