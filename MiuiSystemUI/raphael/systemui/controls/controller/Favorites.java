package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
final class Favorites {
    public static final Favorites INSTANCE = new Favorites();
    private static Map<ComponentName, ? extends List<StructureInfo>> favMap = MapsKt__MapsKt.emptyMap();

    private Favorites() {
    }

    @NotNull
    public final List<StructureInfo> getAllStructures() {
        Map<ComponentName, ? extends List<StructureInfo>> map = favMap;
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<ComponentName, ? extends List<StructureInfo>> value : map.entrySet()) {
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, (List) value.getValue());
        }
        return arrayList;
    }

    @NotNull
    public final List<StructureInfo> getStructuresForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        List<StructureInfo> list = (List) favMap.get(componentName);
        return list != null ? list : CollectionsKt__CollectionsKt.emptyList();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0032, code lost:
        r3 = r0.getControls();
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.util.List<com.android.systemui.controls.controller.ControlInfo> getControlsForStructure(@org.jetbrains.annotations.NotNull com.android.systemui.controls.controller.StructureInfo r4) {
        /*
            r3 = this;
            java.lang.String r0 = "structure"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r4, r0)
            android.content.ComponentName r0 = r4.getComponentName()
            java.util.List r3 = r3.getStructuresForComponent(r0)
            java.util.Iterator r3 = r3.iterator()
        L_0x0011:
            boolean r0 = r3.hasNext()
            if (r0 == 0) goto L_0x002d
            java.lang.Object r0 = r3.next()
            r1 = r0
            com.android.systemui.controls.controller.StructureInfo r1 = (com.android.systemui.controls.controller.StructureInfo) r1
            java.lang.CharSequence r1 = r1.getStructure()
            java.lang.CharSequence r2 = r4.getStructure()
            boolean r1 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r1, (java.lang.Object) r2)
            if (r1 == 0) goto L_0x0011
            goto L_0x002e
        L_0x002d:
            r0 = 0
        L_0x002e:
            com.android.systemui.controls.controller.StructureInfo r0 = (com.android.systemui.controls.controller.StructureInfo) r0
            if (r0 == 0) goto L_0x0039
            java.util.List r3 = r0.getControls()
            if (r3 == 0) goto L_0x0039
            goto L_0x003d
        L_0x0039:
            java.util.List r3 = kotlin.collections.CollectionsKt__CollectionsKt.emptyList()
        L_0x003d:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.controller.Favorites.getControlsForStructure(com.android.systemui.controls.controller.StructureInfo):java.util.List");
    }

    @NotNull
    public final List<ControlInfo> getControlsForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        List<StructureInfo> structuresForComponent = getStructuresForComponent(componentName);
        ArrayList arrayList = new ArrayList();
        for (StructureInfo controls : structuresForComponent) {
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, controls.getControls());
        }
        return arrayList;
    }

    public final void removeStructures(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Map<ComponentName, ? extends List<StructureInfo>> mutableMap = MapsKt__MapsKt.toMutableMap(favMap);
        mutableMap.remove(componentName);
        favMap = mutableMap;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006c, code lost:
        if (r1 != null) goto L_0x0078;
     */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0042 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0043  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean addFavorite(@org.jetbrains.annotations.NotNull android.content.ComponentName r10, @org.jetbrains.annotations.NotNull java.lang.CharSequence r11, @org.jetbrains.annotations.NotNull com.android.systemui.controls.controller.ControlInfo r12) {
        /*
            r9 = this;
            java.lang.String r0 = "componentName"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r10, r0)
            java.lang.String r0 = "structureName"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r11, r0)
            java.lang.String r0 = "controlInfo"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r12, r0)
            java.util.List r0 = r9.getControlsForComponent(r10)
            boolean r1 = r0 instanceof java.util.Collection
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x0021
            boolean r1 = r0.isEmpty()
            if (r1 == 0) goto L_0x0021
        L_0x001f:
            r0 = r3
            goto L_0x0040
        L_0x0021:
            java.util.Iterator r0 = r0.iterator()
        L_0x0025:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x001f
            java.lang.Object r1 = r0.next()
            com.android.systemui.controls.controller.ControlInfo r1 = (com.android.systemui.controls.controller.ControlInfo) r1
            java.lang.String r1 = r1.getControlId()
            java.lang.String r4 = r12.getControlId()
            boolean r1 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r1, (java.lang.Object) r4)
            if (r1 == 0) goto L_0x0025
            r0 = r2
        L_0x0040:
            if (r0 == 0) goto L_0x0043
            return r3
        L_0x0043:
            java.util.Map<android.content.ComponentName, ? extends java.util.List<com.android.systemui.controls.controller.StructureInfo>> r0 = favMap
            java.lang.Object r0 = r0.get(r10)
            java.util.List r0 = (java.util.List) r0
            if (r0 == 0) goto L_0x006f
            java.util.Iterator r0 = r0.iterator()
        L_0x0051:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0069
            java.lang.Object r1 = r0.next()
            r3 = r1
            com.android.systemui.controls.controller.StructureInfo r3 = (com.android.systemui.controls.controller.StructureInfo) r3
            java.lang.CharSequence r3 = r3.getStructure()
            boolean r3 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r3, (java.lang.Object) r11)
            if (r3 == 0) goto L_0x0051
            goto L_0x006a
        L_0x0069:
            r1 = 0
        L_0x006a:
            com.android.systemui.controls.controller.StructureInfo r1 = (com.android.systemui.controls.controller.StructureInfo) r1
            if (r1 == 0) goto L_0x006f
            goto L_0x0078
        L_0x006f:
            com.android.systemui.controls.controller.StructureInfo r1 = new com.android.systemui.controls.controller.StructureInfo
            java.util.List r0 = kotlin.collections.CollectionsKt__CollectionsKt.emptyList()
            r1.<init>(r10, r11, r0)
        L_0x0078:
            r3 = r1
            r4 = 0
            r5 = 0
            java.util.List r10 = r3.getControls()
            java.util.List r6 = kotlin.collections.CollectionsKt___CollectionsKt.plus(r10, r12)
            r7 = 3
            r8 = 0
            com.android.systemui.controls.controller.StructureInfo r10 = com.android.systemui.controls.controller.StructureInfo.copy$default(r3, r4, r5, r6, r7, r8)
            r9.replaceControls(r10)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.controller.Favorites.addFavorite(android.content.ComponentName, java.lang.CharSequence, com.android.systemui.controls.controller.ControlInfo):boolean");
    }

    public final void replaceControls(@NotNull StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "updatedStructure");
        Map<ComponentName, ? extends List<StructureInfo>> mutableMap = MapsKt__MapsKt.toMutableMap(favMap);
        ArrayList arrayList = new ArrayList();
        ComponentName componentName = structureInfo.getComponentName();
        boolean z = false;
        for (StructureInfo structureInfo2 : getStructuresForComponent(componentName)) {
            if (Intrinsics.areEqual((Object) structureInfo2.getStructure(), (Object) structureInfo.getStructure())) {
                z = true;
                structureInfo2 = structureInfo;
            }
            if (!structureInfo2.getControls().isEmpty()) {
                arrayList.add(structureInfo2);
            }
        }
        if (!z && !structureInfo.getControls().isEmpty()) {
            arrayList.add(structureInfo);
        }
        mutableMap.put(componentName, arrayList);
        favMap = mutableMap;
    }

    public final void clear() {
        favMap = MapsKt__MapsKt.emptyMap();
    }

    public final boolean updateControls(@NotNull ComponentName componentName, @NotNull List<Control> list) {
        Pair pair;
        ComponentName componentName2 = componentName;
        List<Control> list2 = list;
        Intrinsics.checkParameterIsNotNull(componentName2, "componentName");
        Intrinsics.checkParameterIsNotNull(list2, "controls");
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt___RangesKt.coerceAtLeast(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(list2, 10)), 16));
        for (T next : list) {
            linkedHashMap.put(((Control) next).getControlId(), next);
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        boolean z = false;
        for (StructureInfo structureInfo : getStructuresForComponent(componentName)) {
            for (ControlInfo controlInfo : structureInfo.getControls()) {
                Control control = (Control) linkedHashMap.get(controlInfo.getControlId());
                if (control != null) {
                    if ((!Intrinsics.areEqual((Object) control.getTitle(), (Object) controlInfo.getControlTitle())) || (!Intrinsics.areEqual((Object) control.getSubtitle(), (Object) controlInfo.getControlSubtitle())) || control.getDeviceType() != controlInfo.getDeviceType()) {
                        CharSequence title = control.getTitle();
                        Intrinsics.checkExpressionValueIsNotNull(title, "updatedControl.title");
                        CharSequence subtitle = control.getSubtitle();
                        Intrinsics.checkExpressionValueIsNotNull(subtitle, "updatedControl.subtitle");
                        controlInfo = ControlInfo.copy$default(controlInfo, (String) null, title, subtitle, control.getDeviceType(), 1, (Object) null);
                        z = true;
                    }
                    Object structure = control.getStructure();
                    if (structure == null) {
                        structure = "";
                    }
                    if (!Intrinsics.areEqual((Object) structureInfo.getStructure(), structure)) {
                        z = true;
                    }
                    pair = new Pair(structure, controlInfo);
                } else {
                    pair = new Pair(structureInfo.getStructure(), controlInfo);
                }
                CharSequence charSequence = (CharSequence) pair.component1();
                ControlInfo controlInfo2 = (ControlInfo) pair.component2();
                Object obj = linkedHashMap2.get(charSequence);
                if (obj == null) {
                    obj = new ArrayList();
                    linkedHashMap2.put(charSequence, obj);
                }
                ((List) obj).add(controlInfo2);
            }
        }
        if (!z) {
            return false;
        }
        ArrayList arrayList = new ArrayList(linkedHashMap2.size());
        for (Map.Entry entry : linkedHashMap2.entrySet()) {
            arrayList.add(new StructureInfo(componentName2, (CharSequence) entry.getKey(), (List) entry.getValue()));
        }
        Map<ComponentName, ? extends List<StructureInfo>> mutableMap = MapsKt__MapsKt.toMutableMap(favMap);
        mutableMap.put(componentName2, arrayList);
        favMap = mutableMap;
        return true;
    }

    public final void load(@NotNull List<StructureInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "structures");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (T next : list) {
            ComponentName componentName = ((StructureInfo) next).getComponentName();
            Object obj = linkedHashMap.get(componentName);
            if (obj == null) {
                obj = new ArrayList();
                linkedHashMap.put(componentName, obj);
            }
            ((List) obj).add(next);
        }
        favMap = linkedHashMap;
    }
}
