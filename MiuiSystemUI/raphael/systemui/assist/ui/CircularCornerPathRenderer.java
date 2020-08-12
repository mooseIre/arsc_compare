package com.android.systemui.assist.ui;

import android.graphics.Path;
import com.android.systemui.assist.ui.CornerPathRenderer;

public final class CircularCornerPathRenderer extends CornerPathRenderer {
    private final int mCornerRadiusBottom;
    private final int mCornerRadiusTop;
    private final int mHeight;
    private final Path mPath = new Path();
    private final int mWidth;

    public CircularCornerPathRenderer(int i, int i2, int i3, int i4) {
        this.mCornerRadiusBottom = i;
        this.mCornerRadiusTop = i2;
        this.mHeight = i4;
        this.mWidth = i3;
    }

    /* renamed from: com.android.systemui.assist.ui.CircularCornerPathRenderer$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner = new int[CornerPathRenderer.Corner.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(10:0|1|2|3|4|5|6|7|8|10) */
        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        static {
            /*
                com.android.systemui.assist.ui.CornerPathRenderer$Corner[] r0 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner = r0
                int[] r0 = $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.BOTTOM_LEFT     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x001f }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.BOTTOM_RIGHT     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x002a }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.TOP_RIGHT     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.TOP_LEFT     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.assist.ui.CircularCornerPathRenderer.AnonymousClass1.<clinit>():void");
        }
    }

    public Path getCornerPath(CornerPathRenderer.Corner corner) {
        this.mPath.reset();
        int i = AnonymousClass1.$SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner[corner.ordinal()];
        if (i == 1) {
            this.mPath.moveTo(0.0f, (float) (this.mHeight - this.mCornerRadiusBottom));
            Path path = this.mPath;
            int i2 = this.mHeight;
            int i3 = this.mCornerRadiusBottom;
            path.arcTo(0.0f, (float) (i2 - (i3 * 2)), (float) (i3 * 2), (float) i2, 180.0f, -90.0f, true);
        } else if (i == 2) {
            this.mPath.moveTo((float) (this.mWidth - this.mCornerRadiusBottom), (float) this.mHeight);
            Path path2 = this.mPath;
            int i4 = this.mWidth;
            int i5 = this.mCornerRadiusBottom;
            int i6 = this.mHeight;
            path2.arcTo((float) (i4 - (i5 * 2)), (float) (i6 - (i5 * 2)), (float) i4, (float) i6, 90.0f, -90.0f, true);
        } else if (i == 3) {
            this.mPath.moveTo((float) this.mWidth, (float) this.mCornerRadiusTop);
            Path path3 = this.mPath;
            int i7 = this.mWidth;
            int i8 = this.mCornerRadiusTop;
            path3.arcTo((float) (i7 - (i8 * 2)), 0.0f, (float) i7, (float) (i8 * 2), 0.0f, -90.0f, true);
        } else if (i == 4) {
            this.mPath.moveTo((float) this.mCornerRadiusTop, 0.0f);
            Path path4 = this.mPath;
            int i9 = this.mCornerRadiusTop;
            path4.arcTo(0.0f, 0.0f, (float) (i9 * 2), (float) (i9 * 2), 270.0f, -90.0f, true);
        }
        return this.mPath;
    }
}
