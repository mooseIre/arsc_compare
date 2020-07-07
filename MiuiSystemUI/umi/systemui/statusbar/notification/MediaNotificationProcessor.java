package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.palette.Palette;
import com.android.systemui.plugins.R;

public class MediaNotificationProcessor {
    private Palette.Filter mBlackWhiteFilter;
    private final ImageGradientColorizer mColorizer;
    private final Context mContext;
    private float[] mFilteredBackgroundHsl;
    private final Context mPackageContext;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ boolean lambda$new$0$MediaNotificationProcessor(int i, float[] fArr) {
        return !isWhiteOrBlack(fArr);
    }

    public MediaNotificationProcessor(Context context, Context context2) {
        this(context, context2, new ImageGradientColorizer());
    }

    @VisibleForTesting
    MediaNotificationProcessor(Context context, Context context2, ImageGradientColorizer imageGradientColorizer) {
        this.mFilteredBackgroundHsl = null;
        this.mBlackWhiteFilter = new Palette.Filter() {
            public final boolean isAllowed(int i, float[] fArr) {
                return MediaNotificationProcessor.this.lambda$new$0$MediaNotificationProcessor(i, fArr);
            }
        };
        this.mContext = context;
        this.mPackageContext = context2;
        this.mColorizer = imageGradientColorizer;
    }

    public void processNotification(Notification notification, Notification.Builder builder) {
        int i;
        Icon largeIcon = notification.getLargeIcon();
        if (largeIcon != null) {
            boolean z = true;
            builder.setRebuildStyledRemoteViews(true);
            Drawable loadDrawable = largeIcon.loadDrawable(this.mPackageContext);
            if (notification.isColorizedMedia()) {
                int intrinsicWidth = loadDrawable.getIntrinsicWidth();
                int intrinsicHeight = loadDrawable.getIntrinsicHeight();
                int i2 = intrinsicWidth * intrinsicHeight;
                if (i2 > 22500) {
                    double sqrt = Math.sqrt((double) (22500.0f / ((float) i2)));
                    intrinsicWidth = (int) (((double) intrinsicWidth) * sqrt);
                    intrinsicHeight = (int) (sqrt * ((double) intrinsicHeight));
                }
                Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                loadDrawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
                loadDrawable.draw(canvas);
                Palette.Builder resizeBitmapArea = Palette.from(createBitmap).setRegion(0, 0, createBitmap.getWidth() / 2, createBitmap.getHeight()).clearFilters().resizeBitmapArea(22500);
                i = findBackgroundColorAndFilter(resizeBitmapArea.generate());
                resizeBitmapArea.setRegion((int) (((float) createBitmap.getWidth()) * 0.4f), 0, createBitmap.getWidth(), createBitmap.getHeight());
                if (this.mFilteredBackgroundHsl != null) {
                    resizeBitmapArea.addFilter(new Palette.Filter() {
                        public final boolean isAllowed(int i, float[] fArr) {
                            return MediaNotificationProcessor.this.lambda$processNotification$1$MediaNotificationProcessor(i, fArr);
                        }
                    });
                }
                resizeBitmapArea.addFilter(this.mBlackWhiteFilter);
                builder.setColorPalette(i, selectForegroundColor(i, resizeBitmapArea.generate()));
            } else {
                i = this.mContext.getColor(R.color.notification_material_background_color);
            }
            ImageGradientColorizer imageGradientColorizer = this.mColorizer;
            if (this.mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
                z = false;
            }
            builder.setLargeIcon(Icon.createWithBitmap(imageGradientColorizer.colorize(loadDrawable, i, z)));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$processNotification$1 */
    public /* synthetic */ boolean lambda$processNotification$1$MediaNotificationProcessor(int i, float[] fArr) {
        float abs = Math.abs(fArr[0] - this.mFilteredBackgroundHsl[0]);
        return abs > 10.0f && abs < 350.0f;
    }

    private int selectForegroundColor(int i, Palette palette) {
        if (CompatibilityColorUtil.isColorLight(i)) {
            return selectForegroundColorForSwatches(palette.getDarkVibrantSwatch(), palette.getVibrantSwatch(), palette.getDarkMutedSwatch(), palette.getMutedSwatch(), palette.getDominantSwatch(), -16777216);
        }
        return selectForegroundColorForSwatches(palette.getLightVibrantSwatch(), palette.getVibrantSwatch(), palette.getLightMutedSwatch(), palette.getMutedSwatch(), palette.getDominantSwatch(), -1);
    }

    private int selectForegroundColorForSwatches(Palette.Swatch swatch, Palette.Swatch swatch2, Palette.Swatch swatch3, Palette.Swatch swatch4, Palette.Swatch swatch5, int i) {
        Palette.Swatch selectVibrantCandidate = selectVibrantCandidate(swatch, swatch2);
        if (selectVibrantCandidate == null) {
            selectVibrantCandidate = selectMutedCandidate(swatch4, swatch3);
        }
        if (selectVibrantCandidate == null) {
            return hasEnoughPopulation(swatch5) ? swatch5.getRgb() : i;
        }
        if (swatch5 == selectVibrantCandidate) {
            return selectVibrantCandidate.getRgb();
        }
        if (((float) selectVibrantCandidate.getPopulation()) / ((float) swatch5.getPopulation()) >= 0.01f || swatch5.getHsl()[1] <= 0.19f) {
            return selectVibrantCandidate.getRgb();
        }
        return swatch5.getRgb();
    }

    private Palette.Swatch selectMutedCandidate(Palette.Swatch swatch, Palette.Swatch swatch2) {
        boolean hasEnoughPopulation = hasEnoughPopulation(swatch);
        boolean hasEnoughPopulation2 = hasEnoughPopulation(swatch2);
        if (hasEnoughPopulation && hasEnoughPopulation2) {
            return swatch.getHsl()[1] * (((float) swatch.getPopulation()) / ((float) swatch2.getPopulation())) > swatch2.getHsl()[1] ? swatch : swatch2;
        } else if (hasEnoughPopulation) {
            return swatch;
        } else {
            if (hasEnoughPopulation2) {
                return swatch2;
            }
            return null;
        }
    }

    private Palette.Swatch selectVibrantCandidate(Palette.Swatch swatch, Palette.Swatch swatch2) {
        boolean hasEnoughPopulation = hasEnoughPopulation(swatch);
        boolean hasEnoughPopulation2 = hasEnoughPopulation(swatch2);
        if (hasEnoughPopulation && hasEnoughPopulation2) {
            return ((float) swatch.getPopulation()) / ((float) swatch2.getPopulation()) < 1.0f ? swatch2 : swatch;
        } else if (hasEnoughPopulation) {
            return swatch;
        } else {
            if (hasEnoughPopulation2) {
                return swatch2;
            }
            return null;
        }
    }

    private boolean hasEnoughPopulation(Palette.Swatch swatch) {
        return swatch != null && ((double) (((float) swatch.getPopulation()) / 22500.0f)) > 0.002d;
    }

    private int findBackgroundColorAndFilter(Palette palette) {
        Palette.Swatch dominantSwatch = palette.getDominantSwatch();
        if (dominantSwatch == null) {
            this.mFilteredBackgroundHsl = null;
            return -1;
        } else if (!isWhiteOrBlack(dominantSwatch.getHsl())) {
            this.mFilteredBackgroundHsl = dominantSwatch.getHsl();
            return dominantSwatch.getRgb();
        } else {
            float f = -1.0f;
            Palette.Swatch swatch = null;
            for (Palette.Swatch swatch2 : palette.getSwatches()) {
                if (swatch2 != dominantSwatch && ((float) swatch2.getPopulation()) > f && !isWhiteOrBlack(swatch2.getHsl())) {
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

    private boolean isWhiteOrBlack(float[] fArr) {
        return isBlack(fArr) || isWhite(fArr);
    }

    private boolean isBlack(float[] fArr) {
        return fArr[2] <= 0.08f;
    }

    private boolean isWhite(float[] fArr) {
        return fArr[2] >= 0.9f;
    }
}
