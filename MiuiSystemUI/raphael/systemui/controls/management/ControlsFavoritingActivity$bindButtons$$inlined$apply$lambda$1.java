package com.android.systemui.controls.management;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.android.systemui.C0021R$string;

/* compiled from: ControlsFavoritingActivity.kt */
final class ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$1 implements View.OnClickListener {
    final /* synthetic */ Button $this_apply;
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$1(Button button, ControlsFavoritingActivity controlsFavoritingActivity) {
        this.$this_apply = button;
        this.this$0 = controlsFavoritingActivity;
    }

    public final void onClick(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this.$this_apply.getContext(), ControlsProviderSelectorActivity.class));
        if (ControlsFavoritingActivity.access$getDoneButton$p(this.this$0).isEnabled()) {
            Toast.makeText(this.this$0.getApplicationContext(), C0021R$string.controls_favorite_toast_no_changes, 0).show();
        }
        ControlsFavoritingActivity controlsFavoritingActivity = this.this$0;
        controlsFavoritingActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(controlsFavoritingActivity, new Pair[0]).toBundle());
        ControlsFavoritingActivity.access$animateExitAndFinish(this.this$0);
    }
}
