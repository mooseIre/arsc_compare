package com.android.systemui.fsgesture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.R;
import java.util.HashMap;

public class DemoIntroduceAct extends FsGestureDemoBaseActiivy {
    TextView backBtn;
    RelativeLayout mIntroContainer;
    TextView nextBtn;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(1024);
        setContentView(R.layout.demo_intro_layout);
        Util.hideSystemBars(getWindow().getDecorView());
        final boolean booleanExtra = getIntent().getBooleanExtra("IS_FROM_PROVISION", false);
        TextView textView = (TextView) findViewById(R.id.btn_back);
        this.backBtn = textView;
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DemoIntroduceAct.this.finish();
            }
        });
        TextView textView2 = (TextView) findViewById(R.id.btn_next);
        this.nextBtn = textView2;
        textView2.setOnClickListener(new View.OnClickListener() {
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
        HashMap hashMap = new HashMap();
        hashMap.put("source", booleanExtra ? "oobe" : "settings");
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).reportFullScreenEventAnonymous("show_gestures_learning_page", hashMap);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.intro_container);
        this.mIntroContainer = relativeLayout;
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle(relativeLayout);
    }
}
