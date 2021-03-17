package com.android.systemui.assist.ui;

import android.content.Context;
import android.graphics.Path;
import com.android.systemui.assist.ui.CornerPathRenderer;

public final class CircularCornerPathRenderer extends CornerPathRenderer {
    private final int mCornerRadiusBottom;
    private final int mCornerRadiusTop;
    private final int mHeight;
    private final Path mPath = new Path();
    private final int mWidth;

    public CircularCornerPathRenderer(Context context) {
        this.mCornerRadiusBottom = DisplayUtils.getCornerRadiusBottom(context);
        this.mCornerRadiusTop = DisplayUtils.getCornerRadiusTop(context);
        this.mHeight = DisplayUtils.getHeight(context);
        this.mWidth = DisplayUtils.getWidth(context);
    }

    /* renamed from: com.android.systemui.assist.ui.CircularCornerPathRenderer$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.android.systemui.assist.ui.CornerPathRenderer$Corner[] r0 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.android.systemui.assist.ui.CircularCornerPathRenderer.AnonymousClass1.$SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner = r0
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.BOTTOM_LEFT     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.systemui.assist.ui.CircularCornerPathRenderer.AnonymousClass1.$SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.BOTTOM_RIGHT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = com.android.systemui.assist.ui.CircularCornerPathRenderer.AnonymousClass1.$SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.TOP_RIGHT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = com.android.systemui.assist.ui.CircularCornerPathRenderer.AnonymousClass1.$SwitchMap$com$android$systemui$assist$ui$CornerPathRenderer$Corner     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.android.systemui.assist.ui.CornerPathRenderer$Corner r1 = com.android.systemui.assist.ui.CornerPathRenderer.Corner.TOP_LEFT     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.assist.ui.CircularCornerPathRenderer.AnonymousClass1.<clinit>():void");
        }
    }

    @Override // com.android.systemui.assist.ui.CornerPathRenderer
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
