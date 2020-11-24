package com.android.systemui.qs;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.tuner.TunerService;
import java.io.File;

public abstract class MiuiHeaderView extends RelativeLayout implements View.OnClickListener, TunerService.Tunable {
    private ActivityStarter mActStarter;
    protected MiuiClock mClock;
    protected MiuiClock mDateView;
    protected int mLastOrientation;
    protected ImageView mShortcut;
    protected int mShortcutDestination;

    public abstract void regionChanged();

    public abstract void themeChanged();

    public MiuiHeaderView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiHeaderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiHeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLastOrientation = 1;
        this.mShortcutDestination = 0;
        this.mActStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        Typeface typeface;
        super.onFinishInflate();
        MiuiClock miuiClock = (MiuiClock) findViewById(C0012R$id.date_time);
        this.mDateView = miuiClock;
        miuiClock.setOnClickListener(this);
        MiuiClock miuiClock2 = (MiuiClock) findViewById(C0012R$id.big_time);
        this.mClock = miuiClock2;
        miuiClock2.setOnClickListener(this);
        if (new File("system/fonts/MitypeVF.ttf").exists()) {
            typeface = Typeface.create("mitype-regular", 0);
        } else {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Mitype2018-50.otf");
        }
        this.mClock.setTypeface(typeface);
        ImageView imageView = (ImageView) findViewById(C0012R$id.notification_shade_shortcut);
        this.mShortcut = imageView;
        imageView.setOnClickListener(this);
        this.mShortcut.setContentDescription(getResources().getString(C0018R$string.accessibility_settings));
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0078 A[Catch:{ Exception -> 0x0083 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClick(android.view.View r7) {
        /*
            r6 = this;
            com.android.systemui.statusbar.policy.MiuiClock r0 = r6.mClock     // Catch:{ Exception -> 0x0083 }
            r1 = 1
            r2 = 0
            java.lang.String r3 = "android.intent.action.MAIN"
            if (r7 == r0) goto L_0x0064
            boolean r0 = miui.os.Build.IS_TABLET     // Catch:{ Exception -> 0x0083 }
            if (r0 != 0) goto L_0x0016
            int r0 = r6.mLastOrientation     // Catch:{ Exception -> 0x0083 }
            r4 = 2
            if (r0 != r4) goto L_0x0016
            com.android.systemui.statusbar.policy.MiuiClock r0 = r6.mDateView     // Catch:{ Exception -> 0x0083 }
            if (r7 != r0) goto L_0x0016
            goto L_0x0064
        L_0x0016:
            com.android.systemui.statusbar.policy.MiuiClock r0 = r6.mDateView     // Catch:{ Exception -> 0x0083 }
            if (r7 != r0) goto L_0x002b
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0083 }
            r2.<init>(r3)     // Catch:{ Exception -> 0x0083 }
            android.content.Context r7 = r6.mContext     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = com.miui.systemui.util.CommonUtil.getCalendarPkg(r7)     // Catch:{ Exception -> 0x0083 }
            r2.setPackage(r7)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "date"
            goto L_0x0070
        L_0x002b:
            android.widget.ImageView r0 = r6.mShortcut     // Catch:{ Exception -> 0x0083 }
            if (r7 != r0) goto L_0x0062
            int r7 = r6.mShortcutDestination     // Catch:{ Exception -> 0x0083 }
            if (r7 != r1) goto L_0x0058
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0083 }
            r2.<init>(r3)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "com.android.settings"
            java.lang.String r0 = "com.android.settings.SubSettings"
            r2.setClassName(r7, r0)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = ":settings:show_fragment"
            java.lang.String r0 = "com.android.settings.NotificationControlCenterSettings"
            r2.putExtra(r7, r0)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = ":settings:show_fragment_title"
            android.content.res.Resources r0 = r6.getResources()     // Catch:{ Exception -> 0x0083 }
            int r3 = com.android.systemui.C0018R$string.notification_control_center     // Catch:{ Exception -> 0x0083 }
            java.lang.String r0 = r0.getString(r3)     // Catch:{ Exception -> 0x0083 }
            r2.putExtra(r7, r0)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "notification-settings"
            goto L_0x0070
        L_0x0058:
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "android.settings.SETTINGS"
            r2.<init>(r7)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "settings"
            goto L_0x0070
        L_0x0062:
            r7 = r2
            goto L_0x0073
        L_0x0064:
            android.content.Intent r2 = new android.content.Intent     // Catch:{ Exception -> 0x0083 }
            r2.<init>(r3)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "com.android.deskclock"
            r2.setPackage(r7)     // Catch:{ Exception -> 0x0083 }
            java.lang.String r7 = "clock"
        L_0x0070:
            r5 = r2
            r2 = r7
            r7 = r5
        L_0x0073:
            android.text.TextUtils.isEmpty(r2)     // Catch:{ Exception -> 0x0083 }
            if (r7 == 0) goto L_0x0087
            r0 = 268435456(0x10000000, float:2.5243549E-29)
            r7.addFlags(r0)     // Catch:{ Exception -> 0x0083 }
            com.android.systemui.plugins.ActivityStarter r6 = r6.mActStarter     // Catch:{ Exception -> 0x0083 }
            r6.startActivity(r7, r1)     // Catch:{ Exception -> 0x0083 }
            goto L_0x0087
        L_0x0083:
            r6 = move-exception
            r6.printStackTrace()
        L_0x0087:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.MiuiHeaderView.onClick(android.view.View):void");
    }
}
