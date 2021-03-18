package com.android.systemui.stackdivider;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0007R$anim;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;

public class ForcedResizableInfoActivity extends Activity implements View.OnTouchListener {
    private final Runnable mFinishRunnable = new Runnable() {
        /* class com.android.systemui.stackdivider.ForcedResizableInfoActivity.AnonymousClass1 */

        public void run() {
            ForcedResizableInfoActivity.this.finish();
        }
    };

    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        String str;
        super.onCreate(bundle);
        setContentView(C0017R$layout.forced_resizable_activity);
        TextView textView = (TextView) findViewById(16908299);
        int intExtra = getIntent().getIntExtra("extra_forced_resizeable_reason", -1);
        if (intExtra == 1) {
            str = getString(C0021R$string.dock_forced_resizable);
        } else if (intExtra == 2) {
            str = getString(C0021R$string.forced_resizable_secondary_display);
        } else {
            throw new IllegalArgumentException("Unexpected forced resizeable reason: " + intExtra);
        }
        textView.setText(str);
        getWindow().setTitle(str);
        getWindow().getDecorView().setOnTouchListener(this);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        getWindow().getDecorView().postDelayed(this.mFinishRunnable, 2500);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        finish();
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        finish();
        return true;
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        finish();
        return true;
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, C0007R$anim.forced_resizable_exit);
    }
}
