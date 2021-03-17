package com.android.systemui.statusbar.notification.mediacontrol;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.palette.graphics.Palette;
import com.android.systemui.statusbar.notification.ImageGradientColorizer;
import com.android.systemui.statusbar.notification.MediaNotificationProcessor;
import java.lang.ref.WeakReference;

public class ProcessArtworkTask extends AsyncTask<Drawable, Void, Result> {
    private final int direction;
    private final Palette.Filter mBlackWhiteFilter = $$Lambda$ProcessArtworkTask$5NgxIsban__ilCnG4_czslaOjX0.INSTANCE;
    private final ImageGradientColorizer mColorizer;
    private float[] mFilteredBackgroundHsl = null;
    private final WeakReference<MiuiMediaControlPanel> mPanel;

    public static final class Result {
        int backgroundColor;
        Bitmap bitmap;
        int foregroundColor;
        int primaryTextColor;
        int secondaryTextColor;
    }

    static /* synthetic */ boolean lambda$new$0(int i, float[] fArr) {
        return !MediaNotificationProcessor.isWhiteOrBlack(fArr);
    }

    public ProcessArtworkTask(int i, MiuiMediaControlPanel miuiMediaControlPanel) {
        this.direction = i;
        this.mPanel = new WeakReference<>(miuiMediaControlPanel);
        this.mColorizer = new ImageGradientColorizer();
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Result result) {
        if (this.mPanel.get() != null) {
            this.mPanel.get().setForegroundColors(result);
            if (this.mPanel.get().getView() != null) {
                this.mPanel.get().getView().setBackground(result.bitmap, result.backgroundColor);
            }
            if (!isCancelled()) {
                this.mPanel.get().removeTask(this);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onCancelled(Result result) {
        Bitmap bitmap;
        if (!(result == null || (bitmap = result.bitmap) == null)) {
            bitmap.recycle();
        }
        if (this.mPanel.get() != null) {
            this.mPanel.get().removeTask(this);
        }
    }

    /* access modifiers changed from: protected */
    public Result doInBackground(Drawable... drawableArr) {
        if (drawableArr.length == 0 || drawableArr[0] == null) {
            return null;
        }
        return processArtwork(drawableArr[0]);
    }

    public Result processArtwork(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int i = intrinsicWidth * intrinsicHeight;
        if (i > 62500) {
            double sqrt = Math.sqrt((double) (62500.0f / ((float) i)));
            intrinsicWidth = (int) (((double) intrinsicWidth) * sqrt);
            intrinsicHeight = (int) (sqrt * ((double) intrinsicHeight));
        }
        Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        boolean z = false;
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        Palette.Builder generateArtworkPaletteBuilder = MediaNotificationProcessor.generateArtworkPaletteBuilder(createBitmap);
        int findBackgroundColorAndFilter = findBackgroundColorAndFilter(generateArtworkPaletteBuilder.generate());
        generateArtworkPaletteBuilder.setRegion((int) (((float) createBitmap.getWidth()) * 0.4f), 0, createBitmap.getWidth(), createBitmap.getHeight());
        if (this.mFilteredBackgroundHsl != null) {
            generateArtworkPaletteBuilder.addFilter(new Palette.Filter() {
                /* class com.android.systemui.statusbar.notification.mediacontrol.$$Lambda$ProcessArtworkTask$GicmRQnGsd1PdG1arj3Y8LFqu_4 */

                @Override // androidx.palette.graphics.Palette.Filter
                public final boolean isAllowed(int i, float[] fArr) {
                    return ProcessArtworkTask.this.lambda$processArtwork$1$ProcessArtworkTask(i, fArr);
                }
            });
        }
        generateArtworkPaletteBuilder.addFilter(this.mBlackWhiteFilter);
        int selectForegroundColor = MediaNotificationProcessor.selectForegroundColor(findBackgroundColorAndFilter, generateArtworkPaletteBuilder.generate());
        ImageGradientColorizer imageGradientColorizer = this.mColorizer;
        if (this.direction == 1) {
            z = true;
        }
        Bitmap colorize = imageGradientColorizer.colorize(drawable, findBackgroundColorAndFilter, z);
        Result result = new Result();
        result.bitmap = colorize;
        result.backgroundColor = findBackgroundColorAndFilter;
        result.foregroundColor = selectForegroundColor;
        return MediaNotificationProcessorExt.recalculateColors(result);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processArtwork$1 */
    public /* synthetic */ boolean lambda$processArtwork$1$ProcessArtworkTask(int i, float[] fArr) {
        float abs = Math.abs(fArr[0] - this.mFilteredBackgroundHsl[0]);
        return abs > 10.0f && abs < 350.0f;
    }

    private int findBackgroundColorAndFilter(Palette palette) {
        Palette.Swatch dominantSwatch = palette.getDominantSwatch();
        if (dominantSwatch == null) {
            this.mFilteredBackgroundHsl = null;
            return -1;
        } else if (!MediaNotificationProcessor.isWhiteOrBlack(dominantSwatch.getHsl())) {
            this.mFilteredBackgroundHsl = dominantSwatch.getHsl();
            return dominantSwatch.getRgb();
        } else {
            float f = -1.0f;
            Palette.Swatch swatch = null;
            for (Palette.Swatch swatch2 : palette.getSwatches()) {
                if (swatch2 != dominantSwatch && ((float) swatch2.getPopulation()) > f && !MediaNotificationProcessor.isWhiteOrBlack(swatch2.getHsl())) {
                    f = (float) swatch2.getPopulation();
                    swatch = swatch2;
                }
            }
            if (swatch == null) {
                this.mFilteredBackgroundHsl = null;
                return dominantSwatch.getRgb();
            } else if (((float) dominantSwatch.getPopulation()) / f > 2.5f) {
                this.mFilteredBackgroundHsl = null;
                return dominantSwatch.getRgb();
            } else {
                this.mFilteredBackgroundHsl = swatch.getHsl();
                return swatch.getRgb();
            }
        }
    }
}
