package com.android.systemui.miui.controlcenter.customize;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.miui.controlcenter.QSControlTileHost;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSIconViewImpl;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miui.app.AlertDialog;

public class CCTileAdapter extends RecyclerView.Adapter<Holder> implements TileQueryHelper.TileStateListener {
    private int mAccessibilityFromIndex;
    private final AccessibilityManager mAccessibilityManager;
    /* access modifiers changed from: private */
    public boolean mAccessibilityMoving;
    /* access modifiers changed from: private */
    public boolean mAddedAdpater;
    private List<TileQueryHelper.TileInfo> mAllTiles;
    private final ItemTouchHelper.Callback mCallback = new ItemTouchHelper.Callback() {
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return true;
        }

        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        public boolean isLongPressDragEnabled() {
            return true;
        }

        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        }

        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            int adapterPosition;
            super.onSelectedChanged(viewHolder, i);
            if (i != 2) {
                viewHolder = null;
            }
            if (viewHolder != CCTileAdapter.this.mCurrentDrag && CCTileAdapter.this.mCurrentDrag != null && (adapterPosition = CCTileAdapter.this.mCurrentDrag.getAdapterPosition()) >= 0) {
                TileQueryHelper.TileInfo tileInfo = (TileQueryHelper.TileInfo) CCTileAdapter.this.mTiles.get(adapterPosition);
                Holder unused = CCTileAdapter.this.mCurrentDrag = null;
            }
        }

        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            CCTileAdapter cCTileAdapter = CCTileAdapter.this;
            cCTileAdapter.saveSpecs(cCTileAdapter.mHost, false);
        }

        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 1 || viewHolder.getItemViewType() == 4) {
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }
            return ItemTouchHelper.Callback.makeMovementFlags(15, 0);
        }

        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            int adapterPosition = viewHolder.getAdapterPosition();
            int adapterPosition2 = viewHolder2.getAdapterPosition();
            if (adapterPosition < 0 || adapterPosition2 < 0) {
                return false;
            }
            return CCTileAdapter.this.move(adapterPosition, adapterPosition2, viewHolder2.itemView);
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public Holder mCurrentDrag;
    private List<String> mCurrentSpecs;
    private RecyclerView.ItemDecoration mDecoration;
    /* access modifiers changed from: private */
    public int mEditIndex;
    private final Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public QSControlTileHost mHost;
    private final ItemTouchHelper mItemTouchHelper;
    private Map<String, QSTile> mLiveTiles;
    private boolean mNeedsFocus;
    private final List<TileQueryHelper.TileInfo> mOtherTiles = new ArrayList();
    private RecyclerView mParent;
    private QSControlCustomizer mQSControlCustomizer;
    private final GridLayoutManager.SpanSizeLookup mSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        public int getSpanSize(int i) {
            int itemViewType = CCTileAdapter.this.getItemViewType(i);
            if (itemViewType == 1 || itemViewType == 4) {
                return CCTileAdapter.this.mSpanCount;
            }
            return 1;
        }
    };
    /* access modifiers changed from: private */
    public int mSpanCount;
    /* access modifiers changed from: private */
    public int mTileBottom;
    private int mTileDividerIndex;
    private boolean mTileMoved;
    /* access modifiers changed from: private */
    public int mTileWidth;
    /* access modifiers changed from: private */
    public final List<TileQueryHelper.TileInfo> mTiles = new ArrayList();

    public CCTileAdapter(Context context, int i, RecyclerView recyclerView, boolean z) {
        this.mContext = context;
        this.mSpanCount = i;
        this.mAddedAdpater = z;
        this.mTileWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tile_width);
        this.mTileBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_margin_bottom);
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mItemTouchHelper = new ItemTouchHelper(this.mCallback);
        this.mDecoration = new TileItemDecoration(context);
        this.mParent = recyclerView;
    }

    public void setQsControlCustomizer(QSControlCustomizer qSControlCustomizer) {
        this.mQSControlCustomizer = qSControlCustomizer;
    }

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mHost = qSControlTileHost;
    }

    public ItemTouchHelper getItemTouchHelper() {
        return this.mItemTouchHelper;
    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return this.mDecoration;
    }

    public void saveSpecs(QSControlTileHost qSControlTileHost, boolean z) {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (i < this.mTiles.size() && this.mTiles.get(i) != null) {
            arrayList.add(this.mTiles.get(i).spec);
            i++;
        }
        arrayList.add("edit");
        qSControlTileHost.changeTiles(this.mCurrentSpecs, arrayList);
        this.mCurrentSpecs = arrayList;
        recalcSpecs(z);
    }

    public void setTileSpecs(List<String> list) {
        if (!list.equals(this.mCurrentSpecs)) {
            this.mCurrentSpecs = list;
            recalcSpecs(true);
        }
    }

    public void onTilesChanged(List<TileQueryHelper.TileInfo> list, Map<String, QSTile> map) {
        this.mLiveTiles = map;
        this.mAllTiles = list;
        recalcSpecs(true);
    }

    public void onTileChanged(TileQueryHelper.TileInfo tileInfo) {
        List<TileQueryHelper.TileInfo> list = this.mAddedAdpater ? this.mTiles : this.mOtherTiles;
        int i = 0;
        while (i < list.size()) {
            TileQueryHelper.TileInfo tileInfo2 = list.get(i);
            if (tileInfo2 == null || !TextUtils.equals(tileInfo.spec, tileInfo2.spec)) {
                i++;
            } else {
                handleUpdateStateForPosition(i, tileInfo.state);
                return;
            }
        }
    }

    private void handleUpdateStateForPosition(int i, QSTile.State state) {
        if (this.mAddedAdpater && i >= this.mTiles.size()) {
            return;
        }
        if (this.mAddedAdpater || i < this.mOtherTiles.size()) {
            (this.mAddedAdpater ? this.mTiles : this.mOtherTiles).get(i).state = state;
            Holder holder = (Holder) this.mParent.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                state.state = 1;
                holder.mTileView.getIcon().setAnimationEnabled(true);
                holder.mTileView.handleStateChanged(state);
                holder.mTileView.getIcon().setAnimationEnabled(false);
            }
        }
    }

    private void recalcSpecs(boolean z) {
        List<TileQueryHelper.TileInfo> list;
        if (this.mCurrentSpecs != null && (list = this.mAllTiles) != null) {
            ArrayList arrayList = new ArrayList(list);
            this.mTiles.clear();
            this.mOtherTiles.clear();
            int i = 0;
            for (int i2 = 0; i2 < this.mCurrentSpecs.size(); i2++) {
                TileQueryHelper.TileInfo andRemoveOther = getAndRemoveOther(this.mCurrentSpecs.get(i2), arrayList);
                if (andRemoveOther != null) {
                    this.mTiles.add(andRemoveOther);
                }
            }
            while (i < this.mOtherTiles.size()) {
                TileQueryHelper.TileInfo tileInfo = (TileQueryHelper.TileInfo) arrayList.get(i);
                if (tileInfo.isSystem) {
                    arrayList.remove(i);
                    this.mOtherTiles.add(tileInfo);
                    i--;
                }
                i++;
            }
            this.mTileDividerIndex = this.mTiles.size();
            this.mOtherTiles.addAll(arrayList);
            this.mEditIndex = this.mTiles.size();
            if (z) {
                notifyDataSetChanged();
            }
        }
    }

    private TileQueryHelper.TileInfo getAndRemoveOther(String str, List<TileQueryHelper.TileInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).spec.equals(str)) {
                return list.remove(i);
            }
        }
        return null;
    }

    public int getItemViewType(int i) {
        if (this.mAddedAdpater && this.mAccessibilityMoving && i == this.mEditIndex - 1) {
            return 2;
        }
        if (!this.mAddedAdpater || i != this.mEditIndex) {
            return 0;
        }
        return 1;
    }

    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Drawable drawable;
        Context context = viewGroup.getContext();
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.qs_control_customize_tile_frame, viewGroup, false);
        ImageView imageView = (ImageView) frameLayout.findViewById(R.id.marker);
        if (this.mAddedAdpater) {
            drawable = this.mContext.getDrawable(R.drawable.ic_qs_control_delete_marker);
        } else {
            drawable = this.mContext.getDrawable(R.drawable.ic_qs_control_add_marker);
        }
        imageView.setImageDrawable(drawable);
        CCCustomizeTileView cCCustomizeTileView = new CCCustomizeTileView(context, new CCQSIconViewImpl(context));
        frameLayout.addView(cCCustomizeTileView, frameLayout.getChildCount());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cCCustomizeTileView.getLayoutParams();
        layoutParams.gravity = 8388691;
        cCCustomizeTileView.setLayoutParams(layoutParams);
        frameLayout.removeView(imageView);
        frameLayout.addView(imageView);
        final Holder holder = new Holder(frameLayout);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition >= 0) {
                    if (CCTileAdapter.this.mAddedAdpater) {
                        CCTileAdapter.this.moveTileItemToOther(adapterPosition);
                    } else {
                        CCTileAdapter.this.addTileItemFromOther(adapterPosition);
                    }
                }
            }
        });
        imageView.setImportantForAccessibility(2);
        return holder;
    }

    public int getItemCount() {
        if (this.mAddedAdpater) {
            return this.mTiles.size();
        }
        return this.mOtherTiles.size();
    }

    public boolean onFailedToRecycleView(Holder holder) {
        if (!this.mAddedAdpater) {
            return true;
        }
        holder.clearDrag();
        return true;
    }

    public void onBindViewHolder(final Holder holder, int i) {
        int i2 = 1;
        if (holder.getItemViewType() != 1) {
            boolean z = false;
            if (holder.getItemViewType() == 2) {
                holder.mTileView.setClickable(true);
                holder.mTileView.setFocusable(true);
                holder.mTileView.setFocusableInTouchMode(true);
                holder.mTileView.setVisibility(0);
                holder.mTileView.setImportantForAccessibility(1);
                holder.mTileView.setContentDescription(this.mContext.getString(R.string.accessibility_qs_edit_position_label, new Object[]{Integer.valueOf(i + 1)}));
                holder.mTileView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        CCTileAdapter.this.selectPosition(holder.getAdapterPosition(), view);
                    }
                });
                if (this.mNeedsFocus) {
                    holder.mTileView.requestLayout();
                    holder.mTileView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                            holder.mTileView.removeOnLayoutChangeListener(this);
                            holder.mTileView.requestFocus();
                        }
                    });
                    this.mNeedsFocus = false;
                    return;
                }
                return;
            }
            TileQueryHelper.TileInfo tileInfo = (this.mAddedAdpater ? this.mTiles : this.mOtherTiles).get(i);
            if (tileInfo != null) {
                holder.mTileView.getIcon().setIsCustomTile(!tileInfo.isSystem);
                if (i > this.mEditIndex) {
                    QSTile.State state = tileInfo.state;
                    state.contentDescription = this.mContext.getString(R.string.accessibility_qs_edit_add_tile_label, new Object[]{state.label});
                } else if (this.mAccessibilityMoving) {
                    tileInfo.state.contentDescription = this.mContext.getString(R.string.accessibility_qs_edit_position_label, new Object[]{Integer.valueOf(i + 1)});
                } else {
                    tileInfo.state.contentDescription = this.mContext.getString(R.string.accessibility_qs_edit_tile_label, new Object[]{Integer.valueOf(i + 1), tileInfo.state.label});
                }
                tileInfo.state.state = 1;
                holder.mTileView.onStateChanged(tileInfo.state);
                if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                    if (!this.mAccessibilityMoving || i < this.mEditIndex) {
                        z = true;
                    }
                    holder.mTileView.setClickable(z);
                    holder.mTileView.setFocusable(z);
                    CCCustomizeTileView access$100 = holder.mTileView;
                    if (!z) {
                        i2 = 4;
                    }
                    access$100.setImportantForAccessibility(i2);
                    if (z) {
                        holder.mTileView.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                int adapterPosition = holder.getAdapterPosition();
                                if (adapterPosition >= 0) {
                                    if (CCTileAdapter.this.mAccessibilityMoving) {
                                        CCTileAdapter.this.selectPosition(adapterPosition, view);
                                    } else if (!CCTileAdapter.this.mAddedAdpater) {
                                        CCTileAdapter.this.addTileItemFromOther(adapterPosition);
                                    } else if (adapterPosition >= CCTileAdapter.this.mEditIndex || !CCTileAdapter.this.canRemoveTiles()) {
                                        CCTileAdapter.this.startAccessibleDrag(adapterPosition);
                                    } else {
                                        CCTileAdapter.this.showAccessibilityDialog(adapterPosition, view);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean canRemoveTiles() {
        return this.mCurrentSpecs.size() > 8;
    }

    /* access modifiers changed from: private */
    public void selectPosition(int i, View view) {
        if (i >= 0) {
            this.mAccessibilityMoving = false;
            List<TileQueryHelper.TileInfo> list = this.mTiles;
            int i2 = this.mEditIndex - 1;
            this.mEditIndex = i2;
            list.remove(i2);
            notifyItemRemoved(this.mEditIndex);
            if (i == this.mEditIndex) {
                i--;
            }
            move(this.mAccessibilityFromIndex, i, view);
            notifyDataSetChanged();
        }
    }

    public void addTileItem(TileQueryHelper.TileInfo tileInfo) {
        if (this.mAddedAdpater) {
            this.mTiles.add(tileInfo);
            this.mOtherTiles.remove(tileInfo);
            updateDividerLocations();
            notifyItemInserted(this.mTiles.size() - 1);
            saveSpecs(this.mHost, false);
            return;
        }
        this.mOtherTiles.add(tileInfo);
        this.mTiles.remove(tileInfo);
        updateDividerLocations();
        notifyItemInserted(this.mOtherTiles.size() - 1);
    }

    /* access modifiers changed from: private */
    public void showAccessibilityDialog(final int i, View view) {
        TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
        CharSequence[] charSequenceArr = {this.mContext.getString(R.string.accessibility_qs_edit_move_tile, new Object[]{tileInfo.state.label}), this.mContext.getString(R.string.accessibility_qs_edit_remove_tile, new Object[]{tileInfo.state.label})};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setItems(charSequenceArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    CCTileAdapter.this.startAccessibleDrag(i);
                } else if (CCTileAdapter.this.mAddedAdpater) {
                    CCTileAdapter.this.moveTileItemToOther(i);
                }
            }
        });
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
        AlertDialog create = builder.create();
        SystemUIDialog.setShowForAllUsers(create, true);
        SystemUIDialog.applyFlags(create);
        create.show();
    }

    /* access modifiers changed from: private */
    public void moveTileItemToOther(int i) {
        if (this.mTiles.size() > this.mSpanCount * 2) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterQuickTileEvent("quick_tiles_removed", this.mTiles.get(i).spec);
            this.mQSControlCustomizer.addInTileAdapter(this.mTiles.get(i), false);
            this.mTiles.remove(i);
            notifyItemRemoved(i);
            saveSpecs(this.mHost, false);
        }
    }

    /* access modifiers changed from: private */
    public void addTileItemFromOther(int i) {
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterQuickTileEvent("quick_tiles_added", this.mOtherTiles.get(i).spec);
        this.mQSControlCustomizer.addInTileAdapter(this.mOtherTiles.get(i), true);
        this.mOtherTiles.remove(i);
        notifyItemRemoved(i);
    }

    /* access modifiers changed from: private */
    public void startAccessibleDrag(int i) {
        this.mAccessibilityMoving = true;
        this.mNeedsFocus = true;
        this.mAccessibilityFromIndex = i;
        List<TileQueryHelper.TileInfo> list = this.mTiles;
        int i2 = this.mEditIndex;
        this.mEditIndex = i2 + 1;
        list.add(i2, (Object) null);
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback(268435461, false);
        notifyDataSetChanged();
    }

    public GridLayoutManager.SpanSizeLookup getSizeLookup() {
        return this.mSizeLookup;
    }

    /* access modifiers changed from: private */
    public boolean move(int i, int i2, View view) {
        String str;
        if (i2 == i) {
            return true;
        }
        int i3 = this.mEditIndex;
        if (i > i3 && i2 > i3) {
            return false;
        }
        CharSequence charSequence = this.mTiles.get(i).state.label;
        move(i, i2, this.mTiles);
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterQuickTileEvent("quick_tiles_moved", this.mTiles.get(i2).spec);
        updateDividerLocations();
        int i4 = this.mEditIndex;
        if (i2 >= i4) {
            MetricsLogger.action(this.mContext, 360, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 361, i);
            str = this.mContext.getString(R.string.accessibility_qs_edit_tile_removed, new Object[]{charSequence});
        } else if (i >= i4) {
            MetricsLogger.action(this.mContext, 362, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 363, i2);
            str = this.mContext.getString(R.string.accessibility_qs_edit_tile_added, new Object[]{charSequence, Integer.valueOf(i2 + 1)});
        } else {
            MetricsLogger.action(this.mContext, 364, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 365, i2);
            str = this.mContext.getString(R.string.accessibility_qs_edit_tile_moved, new Object[]{charSequence, Integer.valueOf(i2 + 1)});
        }
        view.announceForAccessibility(str);
        saveSpecs(this.mHost, false);
        return true;
    }

    private void updateDividerLocations() {
        this.mEditIndex = this.mTiles.size();
        this.mTileDividerIndex = this.mTiles.size();
        for (int i = 0; i < this.mTiles.size(); i++) {
            if (this.mTiles.get(i) == null) {
                if (this.mEditIndex == -1) {
                    this.mEditIndex = i;
                } else {
                    this.mTileDividerIndex = i;
                }
            }
        }
        int size = this.mTiles.size() - 1;
        int i2 = this.mTileDividerIndex;
        if (size == i2) {
            notifyItemChanged(i2);
        }
    }

    private static String strip(TileQueryHelper.TileInfo tileInfo) {
        String str = tileInfo.spec;
        return isCustomTile(tileInfo) ? CustomTile.getComponentFromSpec(str).getPackageName() : str;
    }

    private static boolean isCustomTile(TileQueryHelper.TileInfo tileInfo) {
        return tileInfo.spec.startsWith("custom(");
    }

    private <T> void move(int i, int i2, List<T> list) {
        this.mTileMoved = true;
        list.add(i2, list.remove(i));
        notifyItemMoved(i, i2);
    }

    public boolean isTileMoved() {
        return this.mTileMoved;
    }

    public void resetTileMoved() {
        this.mTileMoved = false;
    }

    public class Holder extends RecyclerView.ViewHolder {
        /* access modifiers changed from: private */
        public CCCustomizeTileView mTileView;

        public Holder(View view) {
            super(view);
            if (view instanceof FrameLayout) {
                this.mTileView = (CCCustomizeTileView) ((FrameLayout) view).getChildAt(0);
                this.mTileView.setBackground((Drawable) null);
                this.mTileView.getIcon().setAnimationEnabled(false);
            }
        }

        public void clearDrag() {
            this.itemView.clearAnimation();
            this.mTileView.getLabel().clearAnimation();
            this.mTileView.getLabel().setAlpha(1.0f);
        }
    }

    public void setSpanCount(int i) {
        this.mSpanCount = i;
    }

    private class TileItemDecoration extends RecyclerView.ItemDecoration {
        private final ColorDrawable mDrawable;

        private TileItemDecoration(Context context) {
            this.mDrawable = new ColorDrawable(context.getColor(R.color.qs_customize_content_background_color));
        }

        public void onDraw(Canvas canvas, RecyclerView recyclerView) {
            super.onDraw(canvas, recyclerView);
            int findEditViewIndex = findEditViewIndex(recyclerView);
            if (findEditViewIndex >= 0) {
                drawBackgroundAfter(recyclerView, findEditViewIndex, canvas);
            }
        }

        public void getItemOffsets(Rect rect, int i, RecyclerView recyclerView) {
            super.getItemOffsets(rect, i, recyclerView);
            int measuredWidth = recyclerView.getMeasuredWidth();
            boolean z = recyclerView.getLayoutDirection() == 1;
            float access$1500 = ((float) (measuredWidth - (CCTileAdapter.this.mTileWidth * CCTileAdapter.this.mSpanCount))) / ((float) (CCTileAdapter.this.mSpanCount - 1));
            if (i % CCTileAdapter.this.mSpanCount < CCTileAdapter.this.mSpanCount - 1) {
                if (z) {
                    rect.left = Math.round(access$1500);
                } else {
                    rect.right = Math.round(access$1500);
                }
            }
            rect.bottom = CCTileAdapter.this.mTileBottom;
        }

        private int findEditViewIndex(RecyclerView recyclerView) {
            if ((recyclerView.getLayoutManager() instanceof LinearLayoutManager) && ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > CCTileAdapter.this.mEditIndex) {
                return 0;
            }
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (recyclerView.getChildViewHolder(recyclerView.getChildAt(i)).getAdapterPosition() == CCTileAdapter.this.mEditIndex) {
                    return i;
                }
            }
            return -1;
        }

        private void drawBackgroundAfter(RecyclerView recyclerView, int i, Canvas canvas) {
            View childAt = recyclerView.getChildAt(i);
            int width = recyclerView.getWidth();
            int bottom = recyclerView.getBottom();
            this.mDrawable.setBounds(0, childAt.getTop() + ((RecyclerView.LayoutParams) childAt.getLayoutParams()).topMargin + Math.round(ViewCompat.getTranslationY(childAt)), width, bottom);
            this.mDrawable.draw(canvas);
        }
    }
}
