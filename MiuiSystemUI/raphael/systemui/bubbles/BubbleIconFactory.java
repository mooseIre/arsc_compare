package com.android.systemui.bubbles;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import com.android.launcher3.icons.BaseIconFactory;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.icons.R$dimen;
import com.android.launcher3.icons.ShadowGenerator;
import com.android.settingslib.R$color;
import com.android.systemui.C0012R$dimen;

public class BubbleIconFactory extends BaseIconFactory {
    protected BubbleIconFactory(Context context) {
        super(context, context.getResources().getConfiguration().densityDpi, context.getResources().getDimensionPixelSize(C0012R$dimen.individual_bubble_size));
    }

    /* access modifiers changed from: package-private */
    public int getBadgeSize() {
        return this.mContext.getResources().getDimensionPixelSize(R$dimen.profile_badge_size);
    }

    /* access modifiers changed from: package-private */
    public Drawable getBubbleDrawable(Context context, ShortcutInfo shortcutInfo, Icon icon) {
        if (shortcutInfo != null) {
            return ((LauncherApps) context.getSystemService("launcherapps")).getShortcutIconDrawable(shortcutInfo, context.getResources().getConfiguration().densityDpi);
        }
        if (icon == null) {
            return null;
        }
        if (icon.getType() == 4 || icon.getType() == 6) {
            context.grantUriPermission(context.getPackageName(), icon.getUri(), 1);
        }
        return icon.loadDrawable(context);
    }

    /* access modifiers changed from: package-private */
    public BitmapInfo getBadgeBitmap(Drawable drawable, boolean z) {
        Bitmap createIconBitmap = createIconBitmap(drawable, 1.0f, getBadgeSize());
        ShadowGenerator shadowGenerator = new ShadowGenerator(getBadgeSize());
        if (!z) {
            Canvas canvas = new Canvas();
            canvas.setBitmap(createIconBitmap);
            shadowGenerator.recreateIcon(Bitmap.createBitmap(createIconBitmap), canvas);
            return createIconBitmap(createIconBitmap);
        }
        float dimensionPixelSize = (float) this.mContext.getResources().getDimensionPixelSize(17105236);
        int color = this.mContext.getResources().getColor(R$color.important_conversation, null);
        Bitmap createBitmap = Bitmap.createBitmap(createIconBitmap.getWidth(), createIconBitmap.getHeight(), createIconBitmap.getConfig());
        Canvas canvas2 = new Canvas(createBitmap);
        int i = (int) dimensionPixelSize;
        canvas2.drawBitmap(createIconBitmap, (Rect) null, new Rect(i, i, canvas2.getHeight() - i, canvas2.getWidth() - i), (Paint) null);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dimensionPixelSize);
        canvas2.drawCircle((float) (canvas2.getWidth() / 2), (float) (canvas2.getHeight() / 2), ((float) (canvas2.getWidth() / 2)) - dimensionPixelSize, paint);
        shadowGenerator.recreateIcon(Bitmap.createBitmap(createBitmap), canvas2);
        return createIconBitmap(createBitmap);
    }

    /* access modifiers changed from: package-private */
    public BitmapInfo getBubbleBitmap(Drawable drawable, BitmapInfo bitmapInfo) {
        BitmapInfo createBadgedIconBitmap = createBadgedIconBitmap(drawable, null, true);
        badgeWithDrawable(createBadgedIconBitmap.icon, new BitmapDrawable(this.mContext.getResources(), bitmapInfo.icon));
        return createBadgedIconBitmap;
    }
}
