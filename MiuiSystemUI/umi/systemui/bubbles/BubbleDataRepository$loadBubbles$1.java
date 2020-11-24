package com.android.systemui.bubbles;

import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.UserHandle;
import com.android.systemui.bubbles.storage.BubbleEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DebugMetadata(c = "com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1", f = "BubbleDataRepository.kt", l = {}, m = "invokeSuspend")
/* compiled from: BubbleDataRepository.kt */
final class BubbleDataRepository$loadBubbles$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function1 $cb;
    int label;
    private CoroutineScope p$;
    final /* synthetic */ BubbleDataRepository this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    BubbleDataRepository$loadBubbles$1(BubbleDataRepository bubbleDataRepository, Function1 function1, Continuation continuation) {
        super(2, continuation);
        this.this$0 = bubbleDataRepository;
        this.$cb = function1;
    }

    @NotNull
    public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        BubbleDataRepository$loadBubbles$1 bubbleDataRepository$loadBubbles$1 = new BubbleDataRepository$loadBubbles$1(this.this$0, this.$cb, continuation);
        bubbleDataRepository$loadBubbles$1.p$ = (CoroutineScope) obj;
        return bubbleDataRepository$loadBubbles$1;
    }

    public final Object invoke(Object obj, Object obj2) {
        return ((BubbleDataRepository$loadBubbles$1) create(obj, (Continuation) obj2)).invokeSuspend(Unit.INSTANCE);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object obj2;
        Object unused = IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label == 0) {
            ResultKt.throwOnFailure(obj);
            List<BubbleEntity> readFromDisk = this.this$0.persistentRepository.readFromDisk();
            this.this$0.volatileRepository.addBubbles(readFromDisk);
            ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(readFromDisk, 10));
            for (BubbleEntity bubbleEntity : readFromDisk) {
                arrayList.add(new ShortcutKey(bubbleEntity.getUserId(), bubbleEntity.getPackageName()));
            }
            Set<ShortcutKey> set = CollectionsKt___CollectionsKt.toSet(arrayList);
            ArrayList arrayList2 = new ArrayList();
            for (ShortcutKey shortcutKey : set) {
                List<ShortcutInfo> shortcuts = this.this$0.launcherApps.getShortcuts(new LauncherApps.ShortcutQuery().setPackage(shortcutKey.getPkg()).setQueryFlags(1041), UserHandle.of(shortcutKey.getUserId()));
                if (shortcuts == null) {
                    shortcuts = CollectionsKt__CollectionsKt.emptyList();
                }
                boolean unused2 = CollectionsKt__MutableCollectionsKt.addAll(arrayList2, shortcuts);
            }
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (Object next : arrayList2) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) next;
                Intrinsics.checkExpressionValueIsNotNull(shortcutInfo, "it");
                int userId = shortcutInfo.getUserId();
                String str = shortcutInfo.getPackage();
                Intrinsics.checkExpressionValueIsNotNull(str, "it.`package`");
                ShortcutKey shortcutKey2 = new ShortcutKey(userId, str);
                Object obj3 = linkedHashMap.get(shortcutKey2);
                if (obj3 == null) {
                    obj3 = new ArrayList();
                    linkedHashMap.put(shortcutKey2, obj3);
                }
                ((List) obj3).add(next);
            }
            final ArrayList arrayList3 = new ArrayList();
            Iterator<T> it = readFromDisk.iterator();
            while (true) {
                Bubble bubble = null;
                if (it.hasNext()) {
                    BubbleEntity bubbleEntity2 = (BubbleEntity) it.next();
                    List list = (List) linkedHashMap.get(new ShortcutKey(bubbleEntity2.getUserId(), bubbleEntity2.getPackageName()));
                    if (list != null) {
                        Iterator it2 = list.iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                obj2 = null;
                                break;
                            }
                            obj2 = it2.next();
                            ShortcutInfo shortcutInfo2 = (ShortcutInfo) obj2;
                            String shortcutId = bubbleEntity2.getShortcutId();
                            Intrinsics.checkExpressionValueIsNotNull(shortcutInfo2, "shortcutInfo");
                            if (Boxing.boxBoolean(Intrinsics.areEqual((Object) shortcutId, (Object) shortcutInfo2.getId())).booleanValue()) {
                                break;
                            }
                        }
                        ShortcutInfo shortcutInfo3 = (ShortcutInfo) obj2;
                        if (shortcutInfo3 != null) {
                            bubble = new Bubble(bubbleEntity2.getKey(), shortcutInfo3, bubbleEntity2.getDesiredHeight(), bubbleEntity2.getDesiredHeightResId(), bubbleEntity2.getTitle());
                        }
                    }
                    if (bubble != null) {
                        arrayList3.add(bubble);
                    }
                } else {
                    Job unused3 = BuildersKt__Builders_commonKt.launch$default(this.this$0.uiScope, (CoroutineContext) null, (CoroutineStart) null, new AnonymousClass1(this, (Continuation) null), 3, (Object) null);
                    return Unit.INSTANCE;
                }
            }
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }

    @DebugMetadata(c = "com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1$1", f = "BubbleDataRepository.kt", l = {}, m = "invokeSuspend")
    /* renamed from: com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1$1  reason: invalid class name */
    /* compiled from: BubbleDataRepository.kt */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        int label;
        private CoroutineScope p$;
        final /* synthetic */ BubbleDataRepository$loadBubbles$1 this$0;

        {
            this.this$0 = r1;
        }

        @NotNull
        public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
            Intrinsics.checkParameterIsNotNull(continuation, "completion");
            AnonymousClass1 r0 = new AnonymousClass1(this.this$0, arrayList3, continuation);
            r0.p$ = (CoroutineScope) obj;
            return r0;
        }

        public final Object invoke(Object obj, Object obj2) {
            return ((AnonymousClass1) create(obj, (Continuation) obj2)).invokeSuspend(Unit.INSTANCE);
        }

        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            Object unused = IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label == 0) {
                ResultKt.throwOnFailure(obj);
                this.this$0.$cb.invoke(arrayList3);
                return Unit.INSTANCE;
            }
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }
}
