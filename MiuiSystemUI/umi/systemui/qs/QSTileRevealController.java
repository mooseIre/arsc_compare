package com.android.systemui.qs;

import android.content.Context;
import android.os.Handler;
import android.util.ArraySet;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileRevealController;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class QSTileRevealController {
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private final PagedTileLayout mPagedTileLayout;
    private final QSPanel mQSPanel;
    private final Runnable mRevealQsTiles = new Runnable() {
        /* class com.android.systemui.qs.QSTileRevealController.AnonymousClass1 */

        public void run() {
            QSTileRevealController.this.mPagedTileLayout.startTileReveal(QSTileRevealController.this.mTilesToReveal, new Runnable() {
                /* class com.android.systemui.qs.$$Lambda$QSTileRevealController$1$gTMt7UW3YL6K0ko8X3nSQ3r95I */

                public final void run() {
                    QSTileRevealController.AnonymousClass1.this.lambda$run$0$QSTileRevealController$1();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$run$0 */
        public /* synthetic */ void lambda$run$0$QSTileRevealController$1() {
            if (QSTileRevealController.this.mQSPanel.isExpanded()) {
                QSTileRevealController qSTileRevealController = QSTileRevealController.this;
                qSTileRevealController.addTileSpecsToRevealed(qSTileRevealController.mTilesToReveal);
                QSTileRevealController.this.mTilesToReveal.clear();
            }
        }
    };
    private final ArraySet<String> mTilesToReveal = new ArraySet<>();

    QSTileRevealController(Context context, QSPanel qSPanel, PagedTileLayout pagedTileLayout) {
        this.mContext = context;
        this.mQSPanel = qSPanel;
        this.mPagedTileLayout = pagedTileLayout;
    }

    public void setExpansion(float f) {
        if (f == 1.0f) {
            this.mHandler.postDelayed(this.mRevealQsTiles, 500);
        } else {
            this.mHandler.removeCallbacks(this.mRevealQsTiles);
        }
    }

    public void updateRevealedTiles(Collection<QSTile> collection) {
        ArraySet<String> arraySet = new ArraySet<>();
        for (QSTile qSTile : collection) {
            arraySet.add(qSTile.getTileSpec());
        }
        Set<String> stringSet = Prefs.getStringSet(this.mContext, "QsTileSpecsRevealed", Collections.EMPTY_SET);
        if (stringSet.isEmpty() || this.mQSPanel.isShowingCustomize()) {
            addTileSpecsToRevealed(arraySet);
            return;
        }
        arraySet.removeAll(stringSet);
        this.mTilesToReveal.addAll((ArraySet<? extends String>) arraySet);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addTileSpecsToRevealed(ArraySet<String> arraySet) {
        ArraySet arraySet2 = new ArraySet(Prefs.getStringSet(this.mContext, "QsTileSpecsRevealed", Collections.EMPTY_SET));
        arraySet2.addAll((ArraySet) arraySet);
        Prefs.putStringSet(this.mContext, "QsTileSpecsRevealed", arraySet2);
    }
}
