package com.android.systemui.controlcenter.phone.customize;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.external.CustomTile;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.QuickTilesAddedEvent;
import com.miui.systemui.events.QuickTilesMovedEvent;
import com.miui.systemui.events.QuickTilesRemovedEvent;
import com.miui.systemui.statusbar.phone.MiuiSystemUIDialog;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miui.app.AlertDialog;

public class CCTileAdapter extends RecyclerView.Adapter<Holder> implements TileQueryHelper.TileStateListener {
    private int mAccessibilityFromIndex;
    private final AccessibilityManager mAccessibilityManager;
    private boolean mAccessibilityMoving;
    private boolean mAddedAdpater;
    private List<TileQueryHelper.TileInfo> mAllTiles;
    private final ItemTouchHelper.Callback mCallback = new ItemTouchHelper.Callback() {
        /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass9 */

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return true;
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            int adapterPosition;
            super.onSelectedChanged(viewHolder, i);
            if (i == 2) {
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).hapticFeedback("pickup", false);
            }
            if (i != 2) {
                viewHolder = null;
            }
            if (viewHolder != CCTileAdapter.this.mCurrentDrag && CCTileAdapter.this.mCurrentDrag != null && (adapterPosition = CCTileAdapter.this.mCurrentDrag.getAdapterPosition()) >= 0) {
                TileQueryHelper.TileInfo tileInfo = (TileQueryHelper.TileInfo) CCTileAdapter.this.mTiles.get(adapterPosition);
                CCTileAdapter.this.mCurrentDrag = null;
            }
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            CCTileAdapter cCTileAdapter = CCTileAdapter.this;
            cCTileAdapter.saveSpecs(cCTileAdapter.mHost, false);
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 1 || viewHolder.getItemViewType() == 4) {
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }
            return ItemTouchHelper.Callback.makeMovementFlags(15, 0);
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            int adapterPosition = viewHolder.getAdapterPosition();
            int adapterPosition2 = viewHolder2.getAdapterPosition();
            if (adapterPosition < 0 || adapterPosition2 < 0) {
                return false;
            }
            return CCTileAdapter.this.move((CCTileAdapter) adapterPosition, adapterPosition2, (int) viewHolder2.itemView);
        }
    };
    private final Context mContext;
    private Holder mCurrentDrag;
    private List<String> mCurrentSpecs;
    private RecyclerView.ItemDecoration mDecoration;
    private int mEditIndex;
    private QSTileHost mHost;
    private boolean mIsAccessibility = false;
    private final ItemTouchHelper mItemTouchHelper;
    private AccessibilityManager.AccessibilityStateChangeListener mListener;
    private boolean mNeedsFocus;
    private List<TileQueryHelper.TileInfo> mOtherTiles = new ArrayList();
    private RecyclerView mParent;
    private QSControlCustomizer mQSControlCustomizer;
    private final GridLayoutManager.SpanSizeLookup mSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass8 */

        @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            int itemViewType = CCTileAdapter.this.getItemViewType(i);
            if (itemViewType == 1 || itemViewType == 4) {
                return CCTileAdapter.this.mSpanCount;
            }
            return 1;
        }
    };
    private int mSpanCount;
    private int mTileBottom;
    private int mTileDividerIndex;
    private boolean mTileMoved;
    private int mTileWidth;
    private List<TileQueryHelper.TileInfo> mTiles = new ArrayList();

    public CCTileAdapter(Context context, int i, RecyclerView recyclerView, boolean z) {
        new Handler();
        this.mContext = context;
        this.mSpanCount = i;
        this.mAddedAdpater = z;
        this.mTileWidth = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tile_width);
        this.mTileBottom = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_margin_bottom);
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mItemTouchHelper = new ItemTouchHelper(this.mCallback);
        this.mDecoration = new TileItemDecoration(context);
        this.mParent = recyclerView;
        registerAdapterDataObserver(new CCTileAdapterDataObserver());
        TileAccessibilityStateChangeListener tileAccessibilityStateChangeListener = new TileAccessibilityStateChangeListener(this);
        this.mListener = tileAccessibilityStateChangeListener;
        this.mAccessibilityManager.addAccessibilityStateChangeListener(tileAccessibilityStateChangeListener);
    }

    public void setQsControlCustomizer(QSControlCustomizer qSControlCustomizer) {
        this.mQSControlCustomizer = qSControlCustomizer;
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

    public void saveSpecs(QSTileHost qSTileHost, boolean z) {
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (i < this.mTiles.size() && this.mTiles.get(i) != null) {
            arrayList.add(this.mTiles.get(i).spec);
            i++;
        }
        arrayList.add("edit");
        qSTileHost.changeTiles(this.mCurrentSpecs, arrayList);
        this.mCurrentSpecs = arrayList;
        recalcSpecs(z);
    }

    public void setTileSpecs(List<String> list) {
        if (!list.equals(this.mCurrentSpecs)) {
            this.mCurrentSpecs = list;
            recalcSpecs(true);
        }
    }

    public void removeAccessibilityListener() {
        this.mAccessibilityManager.removeAccessibilityStateChangeListener(this.mListener);
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTilesChanged(List<TileQueryHelper.TileInfo> list, Map<String, QSTile> map) {
        this.mAllTiles = list;
        recalcSpecs(true);
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTileChanged(TileQueryHelper.TileInfo tileInfo) {
        List<TileQueryHelper.TileInfo> list = this.mAddedAdpater ? this.mTiles : this.mOtherTiles;
        for (int i = 0; i < list.size(); i++) {
            TileQueryHelper.TileInfo tileInfo2 = list.get(i);
            if (tileInfo2 != null && TextUtils.equals(tileInfo.spec, tileInfo2.spec)) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recalcSpecs() {
        if (!(this.mCurrentSpecs == null || this.mAllTiles == null)) {
            this.mTiles.clear();
            this.mOtherTiles.clear();
            ArrayList arrayList = new ArrayList(this.mAllTiles);
            for (int i = 0; i < this.mCurrentSpecs.size(); i++) {
                TileQueryHelper.TileInfo andRemoveOther = getAndRemoveOther(this.mCurrentSpecs.get(i), arrayList);
                if (andRemoveOther != null) {
                    this.mTiles.add(andRemoveOther);
                }
            }
            this.mTileDividerIndex = this.mTiles.size();
            this.mOtherTiles.addAll(arrayList);
            this.mEditIndex = this.mTiles.size();
            notifyDataSetChanged();
        }
    }

    private void recalcSpecs(boolean z) {
        if (!(this.mCurrentSpecs == null || this.mAllTiles == null)) {
            ArrayList arrayList = new ArrayList(this.mAllTiles);
            final List<TileQueryHelper.TileInfo> list = this.mTiles;
            final List<TileQueryHelper.TileInfo> list2 = this.mOtherTiles;
            this.mTiles = new ArrayList();
            this.mOtherTiles = new ArrayList();
            for (int i = 0; i < this.mCurrentSpecs.size(); i++) {
                TileQueryHelper.TileInfo andRemoveOther = getAndRemoveOther(this.mCurrentSpecs.get(i), arrayList);
                if (andRemoveOther != null) {
                    this.mTiles.add(andRemoveOther);
                }
            }
            this.mTileDividerIndex = this.mTiles.size();
            this.mOtherTiles.addAll(arrayList);
            this.mEditIndex = this.mTiles.size();
            if (z) {
                DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass1 */

                    @Override // androidx.recyclerview.widget.DiffUtil.Callback
                    public int getOldListSize() {
                        return (CCTileAdapter.this.mAddedAdpater ? list : list2).size();
                    }

                    @Override // androidx.recyclerview.widget.DiffUtil.Callback
                    public int getNewListSize() {
                        boolean z = CCTileAdapter.this.mAddedAdpater;
                        CCTileAdapter cCTileAdapter = CCTileAdapter.this;
                        return (z ? cCTileAdapter.mTiles : cCTileAdapter.mOtherTiles).size();
                    }

                    @Override // androidx.recyclerview.widget.DiffUtil.Callback
                    public boolean areItemsTheSame(int i, int i2) {
                        if (CCTileAdapter.this.mAddedAdpater) {
                            return ((TileQueryHelper.TileInfo) list.get(i)).spec.equals(((TileQueryHelper.TileInfo) CCTileAdapter.this.mTiles.get(i2)).spec);
                        }
                        return ((TileQueryHelper.TileInfo) list2.get(i)).spec.equals(((TileQueryHelper.TileInfo) CCTileAdapter.this.mOtherTiles.get(i2)).spec);
                    }

                    @Override // androidx.recyclerview.widget.DiffUtil.Callback
                    public boolean areContentsTheSame(int i, int i2) {
                        return areItemsTheSame(i, i2);
                    }
                }, true).dispatchUpdatesTo(this);
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

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        if (this.mAddedAdpater && this.mAccessibilityMoving && i == this.mEditIndex - 1) {
            return 2;
        }
        if (!this.mAddedAdpater || i != this.mEditIndex) {
            return 0;
        }
        return 1;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Drawable drawable;
        Context context = viewGroup.getContext();
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(context).inflate(C0017R$layout.qs_control_customize_tile_frame, viewGroup, false);
        ImageView imageView = (ImageView) frameLayout.findViewById(C0015R$id.marker);
        if (this.mAddedAdpater) {
            drawable = this.mContext.getDrawable(C0013R$drawable.ic_qs_control_delete_marker);
        } else {
            drawable = this.mContext.getDrawable(C0013R$drawable.ic_qs_control_add_marker);
        }
        imageView.setImageDrawable(drawable);
        View cCCustomizeTileView = new CCCustomizeTileView(context, new CCQSIconViewImpl(context));
        frameLayout.addView(cCCustomizeTileView, frameLayout.getChildCount());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) cCCustomizeTileView.getLayoutParams();
        layoutParams.gravity = 8388691;
        cCCustomizeTileView.setLayoutParams(layoutParams);
        frameLayout.removeView(imageView);
        frameLayout.addView(imageView);
        frameLayout.setHapticFeedbackEnabled(false);
        final Holder holder = new Holder(this, frameLayout);
        imageView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass2 */

            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition >= 0) {
                    if (CCTileAdapter.this.mAddedAdpater) {
                        CCTileAdapter.this.moveTileItemToOther(adapterPosition);
                    } else {
                        CCTileAdapter.this.addTileItemFromOther(adapterPosition);
                    }
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
                }
            }
        });
        imageView.setImportantForAccessibility(2);
        return holder;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
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
                holder.mTileView.setContentDescription(this.mContext.getString(C0021R$string.accessibility_qs_edit_position_label, Integer.valueOf(i + 1)));
                holder.mTileView.setOnClickListener(new View.OnClickListener() {
                    /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass3 */

                    public void onClick(View view) {
                        CCTileAdapter.this.selectPosition(holder.getAdapterPosition(), view);
                    }
                });
                if (this.mNeedsFocus) {
                    holder.mTileView.requestLayout();
                    holder.mTileView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) {
                        /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass4 */

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
            bindContentDescription(holder, i);
            if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                if (!this.mAccessibilityMoving || i < this.mEditIndex) {
                    z = true;
                }
                holder.mTileView.setClickable(z);
                holder.mTileView.setFocusable(z);
                CCCustomizeTileView cCCustomizeTileView = holder.mTileView;
                if (!z) {
                    i2 = 4;
                }
                cCCustomizeTileView.setImportantForAccessibility(i2);
                if (z) {
                    holder.mTileView.setOnClickListener(new View.OnClickListener() {
                        /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass5 */

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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindContentDescription(Holder holder, int i) {
        TileQueryHelper.TileInfo tileInfo = (this.mAddedAdpater ? this.mTiles : this.mOtherTiles).get(i);
        if (tileInfo != null) {
            holder.mTileView.getIcon().setIsCustomTile(!tileInfo.isSystem);
            if (i > this.mEditIndex) {
                QSTile.State state = tileInfo.state;
                state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_add_tile_label, state.label);
            } else if (this.mAccessibilityMoving) {
                tileInfo.state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_position_label, Integer.valueOf(i + 1));
            } else {
                tileInfo.state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_label, Integer.valueOf(i + 1), tileInfo.state.label);
            }
            tileInfo.state.state = 1;
            holder.mTileView.onStateChanged(tileInfo.state);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canRemoveTiles() {
        return this.mCurrentSpecs.size() > 8;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void selectPosition(int i, View view) {
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
    /* access modifiers changed from: public */
    private void showAccessibilityDialog(final int i, View view) {
        TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
        AlertDialog create = new AlertDialog.Builder(this.mContext, 8).setItems(new CharSequence[]{this.mContext.getString(C0021R$string.accessibility_qs_edit_move_tile, tileInfo.state.label), this.mContext.getString(C0021R$string.accessibility_qs_edit_remove_tile, tileInfo.state.label)}, new DialogInterface.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.customize.CCTileAdapter.AnonymousClass7 */

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    CCTileAdapter.this.startAccessibleDrag(i);
                } else if (CCTileAdapter.this.mAddedAdpater) {
                    CCTileAdapter.this.moveTileItemToOther(i);
                }
            }
        }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        MiuiSystemUIDialog.setShowForAllUsers(create, true);
        MiuiSystemUIDialog.applyFlags(create);
        create.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void moveTileItemToOther(int i) {
        if (this.mTiles.size() > this.mSpanCount * 2) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new QuickTilesRemovedEvent(this.mTiles.get(i).spec));
            this.mQSControlCustomizer.addInTileAdapter(this.mTiles.get(i), false);
            this.mTiles.remove(i);
            notifyItemRemoved(i);
            saveSpecs(this.mHost, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addTileItemFromOther(int i) {
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new QuickTilesAddedEvent(this.mOtherTiles.get(i).spec));
        this.mQSControlCustomizer.addInTileAdapter(this.mOtherTiles.get(i), true);
        this.mOtherTiles.remove(i);
        notifyItemRemoved(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAccessibleDrag(int i) {
        this.mAccessibilityMoving = true;
        this.mNeedsFocus = true;
        this.mAccessibilityFromIndex = i;
        List<TileQueryHelper.TileInfo> list = this.mTiles;
        int i2 = this.mEditIndex;
        this.mEditIndex = i2 + 1;
        list.add(i2, null);
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback(268435461, false);
        notifyDataSetChanged();
    }

    public GridLayoutManager.SpanSizeLookup getSizeLookup() {
        return this.mSizeLookup;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean move(int i, int i2, View view) {
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
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new QuickTilesMovedEvent(this.mTiles.get(i2).spec));
        updateDividerLocations();
        int i4 = this.mEditIndex;
        if (i2 >= i4) {
            MetricsLogger.action(this.mContext, 360, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 361, i);
            str = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_removed, charSequence);
        } else if (i >= i4) {
            MetricsLogger.action(this.mContext, 362, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 363, i2);
            str = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_added, charSequence, Integer.valueOf(i2 + 1));
        } else {
            MetricsLogger.action(this.mContext, 364, strip(this.mTiles.get(i2)));
            MetricsLogger.action(this.mContext, 365, i2);
            str = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_moved, charSequence, Integer.valueOf(i2 + 1));
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
        private CCCustomizeTileView mTileView;

        public Holder(CCTileAdapter cCTileAdapter, View view) {
            super(view);
            if (view instanceof FrameLayout) {
                CCCustomizeTileView cCCustomizeTileView = (CCCustomizeTileView) ((FrameLayout) view).getChildAt(0);
                this.mTileView = cCCustomizeTileView;
                cCCustomizeTileView.setBackground(null);
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
            this.mDrawable = new ColorDrawable(context.getColor(C0011R$color.qs_customize_content_background_color));
        }

        @Override // androidx.recyclerview.widget.RecyclerView.ItemDecoration
        public void onDraw(Canvas canvas, RecyclerView recyclerView) {
            super.onDraw(canvas, recyclerView);
            int findEditViewIndex = findEditViewIndex(recyclerView);
            if (findEditViewIndex >= 0) {
                drawBackgroundAfter(recyclerView, findEditViewIndex, canvas);
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.ItemDecoration
        public void getItemOffsets(Rect rect, int i, RecyclerView recyclerView) {
            super.getItemOffsets(rect, i, recyclerView);
            int measuredWidth = recyclerView.getMeasuredWidth();
            boolean z = recyclerView.getLayoutDirection() == 1;
            float f = ((float) (measuredWidth - (CCTileAdapter.this.mTileWidth * CCTileAdapter.this.mSpanCount))) / ((float) (CCTileAdapter.this.mSpanCount - 1));
            if (i % CCTileAdapter.this.mSpanCount < CCTileAdapter.this.mSpanCount - 1) {
                if (z) {
                    rect.left = Math.round(f);
                } else {
                    rect.right = Math.round(f);
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
            this.mDrawable.setBounds(0, childAt.getTop() + ((ViewGroup.MarginLayoutParams) ((RecyclerView.LayoutParams) childAt.getLayoutParams())).topMargin + Math.round(ViewCompat.getTranslationY(childAt)), width, bottom);
            this.mDrawable.draw(canvas);
        }
    }

    private class CCTileAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        private CCTileAdapterDataObserver() {
        }

        private void rebindContentDescription(int i) {
            Log.d("CCTileAdapter", "mAccessibilityManager.isEnabled()");
            Holder holder = (Holder) CCTileAdapter.this.mParent.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                CCTileAdapter.this.bindContentDescription(holder, i);
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeInserted(int i, int i2) {
            super.onItemRangeInserted(i, i2);
            if (CCTileAdapter.this.mAccessibilityManager.isEnabled() && i2 == 1) {
                rebindContentDescription(i);
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeRemoved(int i, int i2) {
            super.onItemRangeRemoved(i, i2);
            if (!CCTileAdapter.this.mAccessibilityManager.isEnabled() || i2 != 1) {
                return;
            }
            if (CCTileAdapter.this.mAddedAdpater && i + 1 < CCTileAdapter.this.mTiles.size()) {
                while (i < CCTileAdapter.this.mTiles.size()) {
                    rebindContentDescription(i);
                    i++;
                }
            } else if (!CCTileAdapter.this.mAddedAdpater) {
                while (i < CCTileAdapter.this.mOtherTiles.size()) {
                    rebindContentDescription(i);
                    i++;
                }
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeMoved(int i, int i2, int i3) {
            super.onItemRangeMoved(i, i2, i3);
            if (CCTileAdapter.this.mAccessibilityManager.isEnabled() && i3 == 1) {
                rebindContentDescription(i);
                rebindContentDescription(i2);
            }
        }
    }

    private static class TileAccessibilityStateChangeListener implements AccessibilityManager.AccessibilityStateChangeListener {
        private final CCTileAdapter mAdapter;

        public TileAccessibilityStateChangeListener(CCTileAdapter cCTileAdapter) {
            this.mAdapter = cCTileAdapter;
        }

        public void onAccessibilityStateChanged(boolean z) {
            CCTileAdapter cCTileAdapter = this.mAdapter;
            if (cCTileAdapter != null && cCTileAdapter.mIsAccessibility != z) {
                this.mAdapter.mIsAccessibility = z;
                Log.d("CCTileAdapter", "onAccessibilityStateChanged:" + z);
                this.mAdapter.recalcSpecs();
            }
        }
    }
}
