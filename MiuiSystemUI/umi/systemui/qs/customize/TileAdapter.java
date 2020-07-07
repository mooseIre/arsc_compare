package com.android.systemui.qs.customize;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.QSIconViewImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miui.app.AlertDialog;

public class TileAdapter extends RecyclerView.Adapter<Holder> implements TileQueryHelper.TileStateListener {
    private int mAccessibilityFromIndex;
    private final AccessibilityManager mAccessibilityManager;
    /* access modifiers changed from: private */
    public boolean mAccessibilityMoving;
    private List<TileQueryHelper.TileInfo> mAllTiles;
    private int mBottomDividerPadding;
    private final ItemTouchHelper.Callback mCallback = new ItemTouchHelper.Callback() {
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
            if (viewHolder != TileAdapter.this.mCurrentDrag) {
                if (TileAdapter.this.mCurrentDrag != null && (adapterPosition = TileAdapter.this.mCurrentDrag.getAdapterPosition()) >= 0) {
                    TileAdapter.this.mCurrentDrag.mTileView.setShowAppLabel(adapterPosition > TileAdapter.this.mEditIndex && !((TileQueryHelper.TileInfo) TileAdapter.this.mTiles.get(adapterPosition)).isSystem);
                    Holder unused = TileAdapter.this.mCurrentDrag = null;
                }
                if (viewHolder != null) {
                    Holder unused2 = TileAdapter.this.mCurrentDrag = (Holder) viewHolder;
                    TileAdapter.this.mCurrentDrag.startDrag();
                }
            }
        }

        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (viewHolder != null) {
                ((Holder) viewHolder).stopDrag();
            }
            TileAdapter tileAdapter = TileAdapter.this;
            tileAdapter.saveSpecs(tileAdapter.mHost);
        }

        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            if (TileAdapter.this.canRemoveTiles() || viewHolder.getAdapterPosition() >= TileAdapter.this.mEditIndex) {
                if (viewHolder2.getAdapterPosition() <= TileAdapter.this.mEditIndex + 1) {
                    return true;
                }
                return false;
            } else if (viewHolder2.getAdapterPosition() < TileAdapter.this.mEditIndex) {
                return true;
            } else {
                return false;
            }
        }

        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 1 || viewHolder.getItemViewType() == 4) {
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }
            return ItemTouchHelper.Callback.makeMovementFlags(15, 0);
        }

        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return TileAdapter.this.move(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition(), viewHolder2.itemView);
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public Holder mCurrentDrag;
    private List<String> mCurrentSpecs;
    private RecyclerView.ItemDecoration mDecoration;
    /* access modifiers changed from: private */
    public int mEditIndex;
    /* access modifiers changed from: private */
    public QSTileHost mHost;
    private final ItemTouchHelper mItemTouchHelper;
    /* access modifiers changed from: private */
    public Map<String, QSTile> mLiveTiles;
    private boolean mNeedsFocus;
    private List<TileQueryHelper.TileInfo> mOtherTiles;
    private RecyclerView mParent;
    private final GridLayoutManager.SpanSizeLookup mSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        public int getSpanSize(int i) {
            int itemViewType = TileAdapter.this.getItemViewType(i);
            if (itemViewType == 1 || itemViewType == 4) {
                return TileAdapter.this.mSpanCount;
            }
            return 1;
        }
    };
    /* access modifiers changed from: private */
    public int mSpanCount;
    /* access modifiers changed from: private */
    public int mTileDividerIndex;
    private boolean mTileMoved;
    /* access modifiers changed from: private */
    public final List<TileQueryHelper.TileInfo> mTiles = new ArrayList();
    private int mTopDividerPadding;

    public TileAdapter(Context context, int i, RecyclerView recyclerView) {
        new Handler();
        this.mContext = context;
        this.mSpanCount = i;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mItemTouchHelper = new ItemTouchHelper(this.mCallback);
        this.mDecoration = new TileItemDecoration(context);
        Resources resources = context.getResources();
        this.mTopDividerPadding = -resources.getDimensionPixelSize(R.dimen.qs_customize_content_padding_horizontal);
        this.mBottomDividerPadding = resources.getDimensionPixelSize(R.dimen.qs_customize_divider_padding_horizontal);
        this.mParent = recyclerView;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public ItemTouchHelper getItemTouchHelper() {
        return this.mItemTouchHelper;
    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return this.mDecoration;
    }

    public void saveSpecs(QSTileHost qSTileHost) {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (i < this.mTiles.size() && this.mTiles.get(i) != null) {
            arrayList.add(this.mTiles.get(i).spec);
            i++;
        }
        arrayList.add("edit");
        qSTileHost.changeTiles(this.mCurrentSpecs, arrayList);
        this.mCurrentSpecs = arrayList;
    }

    public void resetTileSpecs(QSTileHost qSTileHost, List<String> list) {
        qSTileHost.changeTiles(this.mCurrentSpecs, list);
        setTileSpecs(list);
    }

    public void setTileSpecs(List<String> list) {
        if (!list.equals(this.mCurrentSpecs)) {
            this.mCurrentSpecs = list;
            recalcSpecs();
        }
    }

    public void onTilesChanged(List<TileQueryHelper.TileInfo> list, Map<String, QSTile> map) {
        this.mLiveTiles = map;
        this.mAllTiles = list;
        recalcSpecs();
    }

    public void onTileChanged(TileQueryHelper.TileInfo tileInfo) {
        int i = 0;
        while (i < this.mTiles.size()) {
            TileQueryHelper.TileInfo tileInfo2 = this.mTiles.get(i);
            if (tileInfo2 == null || !TextUtils.equals(tileInfo.spec, tileInfo2.spec)) {
                i++;
            } else {
                handleUpdateStateForPosition(i, tileInfo.state);
                return;
            }
        }
    }

    private void handleUpdateStateForPosition(int i, QSTile.State state) {
        this.mTiles.get(i).state = state;
        Holder holder = (Holder) this.mParent.findViewHolderForAdapterPosition(i);
        if (holder != null) {
            holder.mTileView.getIcon().setAnimationEnabled(true);
            holder.mTileView.handleStateChanged(state);
            holder.mTileView.getIcon().setAnimationEnabled(false);
        }
    }

    private void recalcSpecs() {
        if (this.mCurrentSpecs != null && this.mAllTiles != null) {
            this.mOtherTiles = new ArrayList(this.mAllTiles);
            this.mTiles.clear();
            int i = 0;
            for (int i2 = 0; i2 < this.mCurrentSpecs.size(); i2++) {
                TileQueryHelper.TileInfo andRemoveOther = getAndRemoveOther(this.mCurrentSpecs.get(i2));
                if (andRemoveOther != null) {
                    this.mTiles.add(andRemoveOther);
                }
            }
            this.mTiles.add((Object) null);
            while (i < this.mOtherTiles.size()) {
                TileQueryHelper.TileInfo tileInfo = this.mOtherTiles.get(i);
                if (tileInfo.isSystem) {
                    this.mOtherTiles.remove(i);
                    this.mTiles.add(tileInfo);
                    i--;
                }
                i++;
            }
            this.mTileDividerIndex = this.mTiles.size();
            this.mTiles.add((Object) null);
            this.mTiles.addAll(this.mOtherTiles);
            updateDividerLocations();
            notifyDataSetChanged();
        }
    }

    private TileQueryHelper.TileInfo getAndRemoveOther(String str) {
        for (int i = 0; i < this.mOtherTiles.size(); i++) {
            if (this.mOtherTiles.get(i).spec.equals(str)) {
                return this.mOtherTiles.remove(i);
            }
        }
        return null;
    }

    public int getItemViewType(int i) {
        if (this.mAccessibilityMoving && i == this.mEditIndex - 1) {
            return 2;
        }
        if (i == this.mTileDividerIndex) {
            return 4;
        }
        if (i == this.mEditIndex) {
            return 1;
        }
        return 0;
    }

    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater from = LayoutInflater.from(context);
        if (i == 4 || i == 1) {
            View inflate = from.inflate(R.layout.qs_customize_tile_divider, viewGroup, false);
            if (i == 4) {
                int i2 = this.mBottomDividerPadding;
                inflate.setPadding(i2, 0, i2, 0);
            } else {
                int i3 = this.mTopDividerPadding;
                inflate.setPadding(i3, 0, i3, 0);
            }
            return new Holder(this, inflate);
        }
        FrameLayout frameLayout = (FrameLayout) from.inflate(R.layout.qs_customize_tile_frame, viewGroup, false);
        frameLayout.addView(new CustomizeTileView(context, new QSIconViewImpl(context)));
        return new Holder(this, frameLayout);
    }

    public int getItemCount() {
        return this.mTiles.size();
    }

    public boolean onFailedToRecycleView(Holder holder) {
        holder.clearDrag();
        return true;
    }

    public void onBindViewHolder(final Holder holder, int i) {
        int i2 = 4;
        boolean z = false;
        if (holder.getItemViewType() == 4) {
            View view = holder.itemView;
            if (this.mTileDividerIndex < this.mTiles.size() - 1) {
                i2 = 0;
            }
            view.setVisibility(i2);
        } else if (holder.getItemViewType() != 1) {
            if (holder.getItemViewType() == 2) {
                holder.mTileView.setClickable(true);
                holder.mTileView.setFocusable(true);
                holder.mTileView.setFocusableInTouchMode(true);
                holder.mTileView.setVisibility(0);
                holder.mTileView.setImportantForAccessibility(1);
                holder.mTileView.setContentDescription(this.mContext.getString(R.string.accessibility_qs_edit_position_label, new Object[]{Integer.valueOf(i + 1)}));
                holder.mTileView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        TileAdapter.this.selectPosition(holder.getAdapterPosition(), view);
                    }
                });
                if (this.mNeedsFocus) {
                    holder.mTileView.requestLayout();
                    holder.mTileView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) {
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
            TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
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
                holder.mTileView.onStateChanged(tileInfo.state);
                holder.mTileView.setShowAppLabel(i > this.mEditIndex && !tileInfo.isSystem);
                bindOnClickListeners(tileInfo, holder);
                if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                    if (!this.mAccessibilityMoving || i < this.mEditIndex) {
                        z = true;
                    }
                    holder.mTileView.setClickable(z);
                    holder.mTileView.setFocusable(z);
                    CustomizeTileView access$100 = holder.mTileView;
                    if (z) {
                        i2 = 1;
                    }
                    access$100.setImportantForAccessibility(i2);
                    if (z) {
                        holder.mTileView.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {
                                int adapterPosition = holder.getAdapterPosition();
                                if (TileAdapter.this.mAccessibilityMoving) {
                                    TileAdapter.this.selectPosition(adapterPosition, view);
                                } else if (adapterPosition >= TileAdapter.this.mEditIndex || !TileAdapter.this.canRemoveTiles()) {
                                    TileAdapter.this.startAccessibleDrag(adapterPosition);
                                } else {
                                    TileAdapter.this.showAccessibilityDialog(adapterPosition, view);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void bindOnClickListeners(final TileQueryHelper.TileInfo tileInfo, Holder holder) {
        holder.mTileView.init(new View.OnClickListener() {
            public void onClick(View view) {
                QSTile qSTile;
                if (TileAdapter.this.mLiveTiles == null || !TileAdapter.this.mLiveTiles.containsKey(tileInfo.spec)) {
                    qSTile = TileAdapter.this.mHost.getTile(tileInfo.spec);
                } else {
                    qSTile = (QSTile) TileAdapter.this.mLiveTiles.get(tileInfo.spec);
                }
                if (qSTile != null) {
                    qSTile.click(true);
                } else if (TileAdapter.isCustomTile(tileInfo)) {
                    Util.showSystemOverlayToast(view.getContext(), (int) R.string.quick_settings_toast_drag_to_enable_custom_tile, 0);
                }
            }
        }, (View.OnClickListener) null, (View.OnLongClickListener) null);
    }

    /* access modifiers changed from: private */
    public boolean canRemoveTiles() {
        return this.mCurrentSpecs.size() > this.mHost.getMinTiles();
    }

    /* access modifiers changed from: private */
    public void selectPosition(int i, View view) {
        this.mAccessibilityMoving = false;
        List<TileQueryHelper.TileInfo> list = this.mTiles;
        int i2 = this.mEditIndex;
        this.mEditIndex = i2 - 1;
        list.remove(i2);
        notifyItemRemoved(this.mEditIndex - 1);
        if (i == this.mEditIndex) {
            i--;
        }
        move(this.mAccessibilityFromIndex, i, view);
        notifyDataSetChanged();
    }

    /* access modifiers changed from: private */
    public void showAccessibilityDialog(final int i, final View view) {
        final TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
        CharSequence[] charSequenceArr = {this.mContext.getString(R.string.accessibility_qs_edit_move_tile, new Object[]{tileInfo.state.label}), this.mContext.getString(R.string.accessibility_qs_edit_remove_tile, new Object[]{tileInfo.state.label})};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setItems(charSequenceArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    TileAdapter.this.startAccessibleDrag(i);
                    return;
                }
                TileAdapter tileAdapter = TileAdapter.this;
                boolean unused = tileAdapter.move(i, tileInfo.isSystem ? tileAdapter.mEditIndex : tileAdapter.mTileDividerIndex, view);
                TileAdapter tileAdapter2 = TileAdapter.this;
                tileAdapter2.notifyItemChanged(tileAdapter2.mTileDividerIndex);
                TileAdapter.this.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
        AlertDialog create = builder.create();
        SystemUIDialog.setShowForAllUsers(create, true);
        SystemUIDialog.applyFlags(create);
        create.show();
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
        saveSpecs(this.mHost);
        return true;
    }

    private void updateDividerLocations() {
        this.mEditIndex = -1;
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

    /* access modifiers changed from: private */
    public static boolean isCustomTile(TileQueryHelper.TileInfo tileInfo) {
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
        public CustomizeTileView mTileView;

        public Holder(TileAdapter tileAdapter, View view) {
            super(view);
            if (view instanceof FrameLayout) {
                CustomizeTileView customizeTileView = (CustomizeTileView) ((FrameLayout) view).getChildAt(0);
                this.mTileView = customizeTileView;
                customizeTileView.setBackground((Drawable) null);
                this.mTileView.getIcon().setAnimationEnabled(false);
            }
        }

        public void clearDrag() {
            this.itemView.clearAnimation();
            this.mTileView.findViewById(R.id.tile_label).clearAnimation();
            this.mTileView.findViewById(R.id.tile_label).setAlpha(1.0f);
            this.mTileView.getAppLabel().clearAnimation();
            this.mTileView.getAppLabel().setAlpha(0.6f);
        }

        public void startDrag() {
            this.itemView.animate().setDuration(100).scaleX(1.2f).scaleY(1.2f);
            this.mTileView.findViewById(R.id.tile_label).animate().setDuration(100).alpha(0.0f);
            this.mTileView.getAppLabel().animate().setDuration(100).alpha(0.0f);
        }

        public void stopDrag() {
            this.itemView.animate().setDuration(100).scaleX(1.0f).scaleY(1.0f);
            this.mTileView.findViewById(R.id.tile_label).animate().setDuration(100).alpha(1.0f);
            this.mTileView.getAppLabel().animate().setDuration(100).alpha(0.6f);
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

        private int findEditViewIndex(RecyclerView recyclerView) {
            if ((recyclerView.getLayoutManager() instanceof LinearLayoutManager) && ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > TileAdapter.this.mEditIndex) {
                return 0;
            }
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (recyclerView.getChildViewHolder(recyclerView.getChildAt(i)).getAdapterPosition() == TileAdapter.this.mEditIndex) {
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
