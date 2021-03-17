package com.android.systemui.controls.management;

import android.service.controls.Control;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.management.ControlsModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.markers.KMutableMap;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AllModel.kt */
public final class AllModel implements ControlsModel {
    private final List<ControlStatus> controls;
    private final ControlsModel.ControlsModelCallback controlsModelCallback;
    @NotNull
    private final List<ElementWrapper> elements;
    private final CharSequence emptyZoneString;
    private final List<String> favoriteIds;
    private boolean modified;
    @Nullable
    private final Void moveHelper;

    public AllModel(@NotNull List<ControlStatus> list, @NotNull List<String> list2, @NotNull CharSequence charSequence, @NotNull ControlsModel.ControlsModelCallback controlsModelCallback2) {
        Intrinsics.checkParameterIsNotNull(list, "controls");
        Intrinsics.checkParameterIsNotNull(list2, "initialFavoriteIds");
        Intrinsics.checkParameterIsNotNull(charSequence, "emptyZoneString");
        Intrinsics.checkParameterIsNotNull(controlsModelCallback2, "controlsModelCallback");
        this.controls = list;
        this.emptyZoneString = charSequence;
        this.controlsModelCallback = controlsModelCallback2;
        HashSet hashSet = new HashSet();
        for (ControlStatus control : list) {
            hashSet.add(control.getControl().getControlId());
        }
        ArrayList arrayList = new ArrayList();
        for (T next : list2) {
            if (hashSet.contains((String) next)) {
                arrayList.add(next);
            }
        }
        this.favoriteIds = CollectionsKt___CollectionsKt.toMutableList(arrayList);
        this.elements = createWrappers(this.controls);
    }

    @Nullable
    public Void getMoveHelper() {
        return this.moveHelper;
    }

    @NotNull
    public List<ControlInfo> getFavorites() {
        ControlInfo controlInfo;
        T t;
        List<String> list = this.favoriteIds;
        ArrayList arrayList = new ArrayList();
        for (String str : list) {
            Iterator<T> it = this.controls.iterator();
            while (true) {
                controlInfo = null;
                if (!it.hasNext()) {
                    t = null;
                    break;
                }
                t = it.next();
                if (Intrinsics.areEqual((Object) ((ControlStatus) t).getControl().getControlId(), (Object) str)) {
                    break;
                }
            }
            ControlStatus controlStatus = (ControlStatus) t;
            Control control = controlStatus != null ? controlStatus.getControl() : null;
            if (control != null) {
                controlInfo = ControlInfo.Companion.fromControl(control);
            }
            if (controlInfo != null) {
                arrayList.add(controlInfo);
            }
        }
        return arrayList;
    }

    @NotNull
    public List<ElementWrapper> getElements() {
        return this.elements;
    }

    public void changeFavoriteStatus(@NotNull String str, boolean z) {
        T t;
        boolean z2;
        ControlStatus controlStatus;
        boolean z3;
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Iterator<T> it = getElements().iterator();
        while (true) {
            if (!it.hasNext()) {
                t = null;
                break;
            }
            t = it.next();
            ElementWrapper elementWrapper = (ElementWrapper) t;
            if (!(elementWrapper instanceof ControlStatusWrapper) || !Intrinsics.areEqual((Object) ((ControlStatusWrapper) elementWrapper).getControlStatus().getControl().getControlId(), (Object) str)) {
                z3 = false;
                continue;
            } else {
                z3 = true;
                continue;
            }
            if (z3) {
                break;
            }
        }
        ControlStatusWrapper controlStatusWrapper = (ControlStatusWrapper) t;
        if (controlStatusWrapper == null || (controlStatus = controlStatusWrapper.getControlStatus()) == null || z != controlStatus.getFavorite()) {
            if (z) {
                z2 = this.favoriteIds.add(str);
            } else {
                z2 = this.favoriteIds.remove(str);
            }
            if (z2 && !this.modified) {
                this.modified = true;
                this.controlsModelCallback.onFirstChange();
            }
            if (controlStatusWrapper != null) {
                controlStatusWrapper.getControlStatus().setFavorite(z);
            }
        }
    }

    private final List<ElementWrapper> createWrappers(List<ControlStatus> list) {
        OrderedMap orderedMap = new OrderedMap(new ArrayMap());
        for (T next : list) {
            Object zone = ((ControlStatus) next).getControl().getZone();
            if (zone == null) {
                zone = "";
            }
            Object obj = orderedMap.get(zone);
            if (obj == null) {
                obj = new ArrayList();
                orderedMap.put(zone, obj);
            }
            ((List) obj).add(next);
        }
        ArrayList arrayList = new ArrayList();
        Sequence sequence = null;
        for (CharSequence charSequence : orderedMap.getOrderedKeys()) {
            Object value = MapsKt__MapsKt.getValue(orderedMap, charSequence);
            Intrinsics.checkExpressionValueIsNotNull(value, "map.getValue(zoneName)");
            Sequence map = SequencesKt___SequencesKt.map(CollectionsKt___CollectionsKt.asSequence((Iterable) value), AllModel$createWrappers$values$1.INSTANCE);
            if (TextUtils.isEmpty(charSequence)) {
                sequence = map;
            } else {
                Intrinsics.checkExpressionValueIsNotNull(charSequence, "zoneName");
                arrayList.add(new ZoneNameWrapper(charSequence));
                boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, map);
            }
        }
        if (sequence != null) {
            if (orderedMap.size() != 1) {
                arrayList.add(new ZoneNameWrapper(this.emptyZoneString));
            }
            boolean unused2 = CollectionsKt__MutableCollectionsKt.addAll(arrayList, sequence);
        }
        return arrayList;
    }

    /* compiled from: AllModel.kt */
    private static final class OrderedMap<K, V> implements Map<K, V>, KMutableMap {
        private final Map<K, V> map;
        @NotNull
        private final List<K> orderedKeys = new ArrayList();

        public boolean containsKey(Object obj) {
            return this.map.containsKey(obj);
        }

        public boolean containsValue(Object obj) {
            return this.map.containsValue(obj);
        }

        @Nullable
        public V get(Object obj) {
            return this.map.get(obj);
        }

        @NotNull
        public Set<Map.Entry<K, V>> getEntries() {
            return this.map.entrySet();
        }

        @NotNull
        public Set<K> getKeys() {
            return this.map.keySet();
        }

        public int getSize() {
            return this.map.size();
        }

        @NotNull
        public Collection<V> getValues() {
            return this.map.values();
        }

        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        public void putAll(@NotNull Map<? extends K, ? extends V> map2) {
            Intrinsics.checkParameterIsNotNull(map2, "from");
            this.map.putAll(map2);
        }

        public OrderedMap(@NotNull Map<K, V> map2) {
            Intrinsics.checkParameterIsNotNull(map2, "map");
            this.map = map2;
        }

        public final /* bridge */ Set<Map.Entry<K, V>> entrySet() {
            return getEntries();
        }

        public final /* bridge */ Set<K> keySet() {
            return getKeys();
        }

        public final /* bridge */ int size() {
            return getSize();
        }

        public final /* bridge */ Collection<V> values() {
            return getValues();
        }

        @NotNull
        public final List<K> getOrderedKeys() {
            return this.orderedKeys;
        }

        @Nullable
        public V put(K k, V v) {
            if (!this.map.containsKey(k)) {
                this.orderedKeys.add(k);
            }
            return this.map.put(k, v);
        }

        public void clear() {
            this.orderedKeys.clear();
            this.map.clear();
        }

        @Nullable
        public V remove(Object obj) {
            V remove = this.map.remove(obj);
            if (remove != null) {
                this.orderedKeys.remove(obj);
            }
            return remove;
        }
    }
}
