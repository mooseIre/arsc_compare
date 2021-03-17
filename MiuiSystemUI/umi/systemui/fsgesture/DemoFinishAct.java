package com.android.systemui.fsgesture;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0007R$anim;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;

public class DemoFinishAct extends FsGestureDemoBaseActiivy {
    TextView finishView;
    private boolean isFromPro;
    TextView replayView;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.fs_gesture_demo_final_view);
        Intent intent = getIntent();
        final String stringExtra = intent.getStringExtra("DEMO_TYPE");
        this.isFromPro = intent.getBooleanExtra("IS_FROM_PROVISION", false);
        TextView textView = (TextView) findViewById(C0015R$id.fs_gesture_final_restart);
        this.replayView = textView;
        textView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.fsgesture.DemoFinishAct.AnonymousClass1 */

            public void onClick(View view) {
                Intent intent = new Intent();
                if ("DEMO_TO_HOME".equals(stringExtra) || "DEMO_TO_RECENTTASK".equals(stringExtra)) {
                    intent.setClass(DemoFinishAct.this, HomeDemoAct.class);
                    intent.putExtra("DEMO_TYPE", stringExtra);
                } else if ("DEMO_FULLY_SHOW".equals(stringExtra)) {
                    intent.setClass(DemoFinishAct.this, HomeDemoAct.class);
                    intent.putExtra("DEMO_TYPE", stringExtra);
                    intent.putExtra("FULLY_SHOW_STEP", 1);
                    intent.putExtra("IS_FROM_PROVISION", DemoFinishAct.this.isFromPro);
                } else if ("FSG_BACK_GESTURE".equals(stringExtra)) {
                    intent.setClass(DemoFinishAct.this, FsGestureBackDemoActivity.class);
                    intent.putExtra("DEMO_TYPE", "FSG_BACK_GESTURE");
                }
                DemoFinishAct.this.startActivity(intent);
                DemoFinishAct.this.overridePendingTransition(C0007R$anim.activity_start_enter, C0007R$anim.activity_start_exit);
                DemoFinishAct.this.finish();
            }
        });
        TextView textView2 = (TextView) findViewById(C0015R$id.fs_gesture_final_over);
        this.finishView = textView2;
        textView2.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.fsgesture.DemoFinishAct.AnonymousClass2 */

            public void onClick(View view) {
                if (DemoFinishAct.this.isFromPro) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.provision", "com.android.provision.activities.NavigationModePickerActivity"));
                    intent.putExtra("IS_COMPLETE", true);
                    DemoFinishAct.this.startActivity(intent);
                    DemoFinishAct.this.overridePendingTransition(C0007R$anim.activity_start_enter, C0007R$anim.activity_start_exit);
                }
                DemoFinishAct.this.finish();
            }
        });
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle((RelativeLayout) this.replayView.getParent());
    }
}
