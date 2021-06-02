package com.android.systemui.bubbles;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class BubbleOverflowActivity extends Activity {
    private BubbleOverflowAdapter mAdapter;
    private BubbleController mBubbleController;
    private LinearLayout mEmptyState;
    private ImageView mEmptyStateImage;
    private TextView mEmptyStateSubtitle;
    private TextView mEmptyStateTitle;
    private List<Bubble> mOverflowBubbles = new ArrayList();
    private RecyclerView mRecyclerView;

    /* access modifiers changed from: private */
    public class NoScrollGridLayoutManager extends GridLayoutManager {
        NoScrollGridLayoutManager(Context context, int i) {
            super(context, i);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.LayoutManager, androidx.recyclerview.widget.LinearLayoutManager
        public boolean canScrollVertically() {
            if (BubbleOverflowActivity.this.mBubbleController.inLandscape()) {
                return super.canScrollVertically();
            }
            return false;
        }

        @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
        public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
            int itemCount = state.getItemCount();
            int columnCountForAccessibility = super.getColumnCountForAccessibility(recycler, state);
            return itemCount < columnCountForAccessibility ? itemCount : columnCountForAccessibility;
        }
    }

    public BubbleOverflowActivity(BubbleController bubbleController) {
        this.mBubbleController = bubbleController;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.bubble_overflow_activity);
        this.mRecyclerView = (RecyclerView) findViewById(C0015R$id.bubble_overflow_recycler);
        this.mEmptyState = (LinearLayout) findViewById(C0015R$id.bubble_overflow_empty_state);
        this.mEmptyStateTitle = (TextView) findViewById(C0015R$id.bubble_overflow_empty_title);
        this.mEmptyStateSubtitle = (TextView) findViewById(C0015R$id.bubble_overflow_empty_subtitle);
        this.mEmptyStateImage = (ImageView) findViewById(C0015R$id.bubble_overflow_empty_state_image);
        updateDimensions();
        onDataChanged(this.mBubbleController.getOverflowBubbles());
        this.mBubbleController.setOverflowCallback(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleOverflowActivity$bBXw1pgL9xyN0c4JMlrR5U428HM */

            public final void run() {
                BubbleOverflowActivity.this.lambda$onCreate$0$BubbleOverflowActivity();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$0 */
    public /* synthetic */ void lambda$onCreate$0$BubbleOverflowActivity() {
        onDataChanged(this.mBubbleController.getOverflowBubbles());
    }

    /* access modifiers changed from: package-private */
    public void updateDimensions() {
        Resources resources = getResources();
        int integer = resources.getInteger(C0016R$integer.bubbles_overflow_columns);
        this.mRecyclerView.setLayoutManager(new NoScrollGridLayoutManager(getApplicationContext(), integer));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int dimensionPixelSize = (displayMetrics.widthPixels - (resources.getDimensionPixelSize(C0012R$dimen.bubble_overflow_padding) * 2)) / integer;
        int dimensionPixelSize2 = (resources.getDimensionPixelSize(C0012R$dimen.bubble_overflow_height) - resources.getDimensionPixelSize(C0012R$dimen.bubble_overflow_padding)) / ((int) Math.ceil(((double) resources.getInteger(C0016R$integer.bubbles_max_overflow)) / ((double) integer)));
        Context applicationContext = getApplicationContext();
        List<Bubble> list = this.mOverflowBubbles;
        BubbleController bubbleController = this.mBubbleController;
        Objects.requireNonNull(bubbleController);
        BubbleOverflowAdapter bubbleOverflowAdapter = new BubbleOverflowAdapter(applicationContext, list, new Consumer() {
            /* class com.android.systemui.bubbles.$$Lambda$HcbZA8v8RHJPrNTsZB0H54PCimo */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                BubbleController.this.promoteBubbleFromOverflow((Bubble) obj);
            }
        }, dimensionPixelSize, dimensionPixelSize2);
        this.mAdapter = bubbleOverflowAdapter;
        this.mRecyclerView.setAdapter(bubbleOverflowAdapter);
    }

    /* access modifiers changed from: package-private */
    public void updateTheme() {
        Drawable drawable;
        int i;
        Resources resources = getResources();
        boolean z = (resources.getConfiguration().uiMode & 48) == 32;
        ImageView imageView = this.mEmptyStateImage;
        if (z) {
            drawable = resources.getDrawable(C0013R$drawable.ic_empty_bubble_overflow_dark);
        } else {
            drawable = resources.getDrawable(C0013R$drawable.ic_empty_bubble_overflow_light);
        }
        imageView.setImageDrawable(drawable);
        View findViewById = findViewById(16908290);
        if (z) {
            i = resources.getColor(C0011R$color.bubbles_dark);
        } else {
            i = resources.getColor(C0011R$color.bubbles_light);
        }
        findViewById.setBackgroundColor(i);
        TypedArray obtainStyledAttributes = getApplicationContext().obtainStyledAttributes(new int[]{16844002, 16842808});
        int i2 = -16777216;
        int color = obtainStyledAttributes.getColor(0, z ? -16777216 : -1);
        if (z) {
            i2 = -1;
        }
        int ensureTextContrast = ContrastColorUtil.ensureTextContrast(obtainStyledAttributes.getColor(1, i2), color, z);
        obtainStyledAttributes.recycle();
        this.mEmptyStateTitle.setTextColor(ensureTextContrast);
        this.mEmptyStateSubtitle.setTextColor(ensureTextContrast);
    }

    /* access modifiers changed from: package-private */
    public void onDataChanged(List<Bubble> list) {
        this.mOverflowBubbles.clear();
        this.mOverflowBubbles.addAll(list);
        this.mAdapter.notifyDataSetChanged();
        if (this.mOverflowBubbles.isEmpty()) {
            this.mEmptyState.setVisibility(0);
        } else {
            this.mEmptyState.setVisibility(8);
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onRestart() {
        super.onRestart();
    }

    public void onResume() {
        super.onResume();
        updateDimensions();
        updateTheme();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
