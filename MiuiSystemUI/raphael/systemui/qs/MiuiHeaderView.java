package com.android.systemui.qs;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
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
        this(context, null);
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
        MiuiClock miuiClock = (MiuiClock) findViewById(C0015R$id.date_time);
        this.mDateView = miuiClock;
        miuiClock.setOnClickListener(this);
        MiuiClock miuiClock2 = (MiuiClock) findViewById(C0015R$id.big_time);
        this.mClock = miuiClock2;
        miuiClock2.setOnClickListener(this);
        if (new File("system/fonts/MitypeVF.ttf").exists()) {
            typeface = Typeface.create("mitype-regular", 0);
        } else {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Mitype2018-50.otf");
        }
        this.mClock.setTypeface(typeface);
        ImageView imageView = (ImageView) findViewById(C0015R$id.notification_shade_shortcut);
        this.mShortcut = imageView;
        imageView.setOnClickListener(this);
        this.mShortcut.setContentDescription(getResources().getString(C0021R$string.accessibility_settings));
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0079  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClick(android.view.View r7) {
        /*
        // Method dump skipped, instructions count: 150
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.MiuiHeaderView.onClick(android.view.View):void");
    }
}
