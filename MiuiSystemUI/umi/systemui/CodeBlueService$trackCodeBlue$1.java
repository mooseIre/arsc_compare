package com.android.systemui;

/* compiled from: CodeBlueService.kt */
final class CodeBlueService$trackCodeBlue$1 implements Runnable {
    final /* synthetic */ CodeBlueService this$0;

    CodeBlueService$trackCodeBlue$1(CodeBlueService codeBlueService) {
        this.this$0 = codeBlueService;
    }

    public final void run() {
        if (CodeBlueConfig.Companion.getTrackCodeBlue(this.this$0.getContext())) {
            CodeBlueService.access$trackCodeBlueEvent(this.this$0);
            CodeBlueConfig.Companion.setTrackCodeBlue(this.this$0.getContext(), false);
        }
    }
}
