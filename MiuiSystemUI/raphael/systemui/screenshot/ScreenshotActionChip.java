package com.android.systemui.screenshot;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0015R$id;

public class ScreenshotActionChip extends FrameLayout {
    private ImageView mIcon;
    private int mIconColor;
    private TextView mText;

    public ScreenshotActionChip(Context context) {
        this(context, null);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScreenshotActionChip(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mIconColor = context.getColor(C0011R$color.global_screenshot_button_icon);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mIcon = (ImageView) findViewById(C0015R$id.screenshot_action_chip_icon);
        this.mText = (TextView) findViewById(C0015R$id.screenshot_action_chip_text);
    }

    /* access modifiers changed from: package-private */
    public void setIcon(Icon icon, boolean z) {
        if (z) {
            icon.setTint(this.mIconColor);
        }
        this.mIcon.setImageIcon(icon);
    }

    /* access modifiers changed from: package-private */
    public void setText(CharSequence charSequence) {
        this.mText.setText(charSequence);
    }

    /* access modifiers changed from: package-private */
    public void setPendingIntent(PendingIntent pendingIntent, Runnable runnable) {
        setOnClickListener(new View.OnClickListener(pendingIntent, runnable) {
            /* class com.android.systemui.screenshot.$$Lambda$ScreenshotActionChip$ESyR9a8Hwpm3rCY_qaXPGrIojI */
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                ScreenshotActionChip.lambda$setPendingIntent$0(this.f$0, this.f$1, view);
            }
        });
    }

    static /* synthetic */ void lambda$setPendingIntent$0(PendingIntent pendingIntent, Runnable runnable, View view) {
        try {
            pendingIntent.send();
            runnable.run();
        } catch (PendingIntent.CanceledException e) {
            Log.e("ScreenshotActionChip", "Intent cancelled", e);
        }
    }
}
