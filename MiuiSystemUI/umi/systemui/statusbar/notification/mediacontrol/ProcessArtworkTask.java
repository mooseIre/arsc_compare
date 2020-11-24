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
    private int direction;
    private final Palette.Filter mBlackWhiteFilter = $$Lambda$ProcessArtworkTask$5NgxIsban__ilCnG4_czslaOjX0.INSTANCE;
    private ImageGradientColorizer mColorizer;
    private WeakReference<MiuiMediaControlPanel> mPanel;

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
            ((MiuiMediaControlPanel) this.mPanel.get()).setForegroundColor(result.foregroundColor);
            if (((MiuiMediaControlPanel) this.mPanel.get()).getView() != null) {
                ((MiuiMediaControlPanel) this.mPanel.get()).getView().setBackground(result.bitmap, result.backgroundColor);
            }
            if (!isCancelled()) {
                ((MiuiMediaControlPanel) this.mPanel.get()).removeTask(this);
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
            ((MiuiMediaControlPanel) this.mPanel.get()).removeTask(this);
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
        if (i > 22500) {
            double sqrt = Math.sqrt((double) (22500.0f / ((float) i)));
            intrinsicWidth = (int) (((double) intrinsicWidth) * sqrt);
            intrinsicHeight = (int) (sqrt * ((double) intrinsicHeight));
        }
        Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        boolean z = false;
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        Palette.Builder generateArtworkPaletteBuilder = MediaNotificationProcessor.generateArtworkPaletteBuilder(createBitmap);
        Palette.Swatch findBackgroundSwatch = MediaNotificationProcessor.findBackgroundSwatch(generateArtworkPaletteBuilder.generate());
        int computeBackgroundColor = MediaNotificationProcessorExt.computeBackgroundColor(findBackgroundSwatch);
        generateArtworkPaletteBuilder.setRegion((int) (((float) createBitmap.getWidth()) * 0.4f), 0, createBitmap.getWidth(), createBitmap.getHeight());
        if (!MediaNotificationProcessor.isWhiteOrBlack(findBackgroundSwatch.getHsl())) {
            generateArtworkPaletteBuilder.addFilter(new Palette.Filter(findBackgroundSwatch.getHsl()[0]) {
                public final /* synthetic */ float f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean isAllowed(int i, float[] fArr) {
                    return ProcessArtworkTask.lambda$processArtwork$1(this.f$0, i, fArr);
                }
            });
        }
        generateArtworkPaletteBuilder.addFilter(this.mBlackWhiteFilter);
        int selectForegroundColor = MediaNotificationProcessor.selectForegroundColor(computeBackgroundColor, generateArtworkPaletteBuilder.generate());
        ImageGradientColorizer imageGradientColorizer = this.mColorizer;
        if (this.direction == 1) {
            z = true;
        }
        Bitmap colorize = imageGradientColorizer.colorize(drawable, computeBackgroundColor, z);
        Result result = new Result();
        result.bitmap = colorize;
        result.backgroundColor = computeBackgroundColor;
        result.foregroundColor = selectForegroundColor;
        return result;
    }

    static /* synthetic */ boolean lambda$processArtwork$1(float f, int i, float[] fArr) {
        float abs = Math.abs(fArr[0] - f);
        return abs > 10.0f && abs < 350.0f;
    }

    static final class Result {
        int backgroundColor;
        Bitmap bitmap;
        int foregroundColor;

        Result() {
        }
    }
}
