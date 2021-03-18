package com.android.systemui.fsgesture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;

public class DemoIntroduceAct extends FsGestureDemoBaseActiivy {
    TextView backBtn;
    RelativeLayout mIntroContainer;
    TextView nextBtn;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.demo_intro_layout);
        final boolean booleanExtra = getIntent().getBooleanExtra("IS_FROM_PROVISION", false);
        TextView textView = (TextView) findViewById(C0015R$id.btn_back);
        this.backBtn = textView;
        textView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.fsgesture.DemoIntroduceAct.AnonymousClass1 */

            public void onClick(View view) {
                DemoIntroduceAct.this.finish();
            }
        });
        TextView textView2 = (TextView) findViewById(C0015R$id.btn_next);
        this.nextBtn = textView2;
        textView2.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.fsgesture.DemoIntroduceAct.AnonymousClass2 */

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(DemoIntroduceAct.this, HomeDemoAct.class);
                intent.putExtra("DEMO_TYPE", "DEMO_FULLY_SHOW");
                intent.putExtra("FULLY_SHOW_STEP", 1);
                intent.putExtra("IS_FROM_PROVISION", booleanExtra);
                DemoIntroduceAct.this.startActivity(intent);
                DemoIntroduceAct.this.finish();
            }
        });
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(C0015R$id.intro_container);
        this.mIntroContainer = relativeLayout;
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle(relativeLayout);
    }
}
