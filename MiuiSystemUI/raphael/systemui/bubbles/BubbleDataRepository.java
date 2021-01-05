package com.android.systemui.bubbles;

import android.annotation.SuppressLint;
import android.content.pm.LauncherApps;
import com.android.systemui.bubbles.storage.BubbleEntity;
import com.android.systemui.bubbles.storage.BubblePersistentRepository;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository;
import java.util.ArrayList;
import java.util.List;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;

/* compiled from: BubbleDataRepository.kt */
public final class BubbleDataRepository {
    private final CoroutineScope ioScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getIO());
    private Job job;
    /* access modifiers changed from: private */
    public final LauncherApps launcherApps;
    /* access modifiers changed from: private */
    public final BubblePersistentRepository persistentRepository;
    /* access modifiers changed from: private */
    public final CoroutineScope uiScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getMain());
    /* access modifiers changed from: private */
    public final BubbleVolatileRepository volatileRepository;

    public BubbleDataRepository(@NotNull BubbleVolatileRepository bubbleVolatileRepository, @NotNull BubblePersistentRepository bubblePersistentRepository, @NotNull LauncherApps launcherApps2) {
        Intrinsics.checkParameterIsNotNull(bubbleVolatileRepository, "volatileRepository");
        Intrinsics.checkParameterIsNotNull(bubblePersistentRepository, "persistentRepository");
        Intrinsics.checkParameterIsNotNull(launcherApps2, "launcherApps");
        this.volatileRepository = bubbleVolatileRepository;
        this.persistentRepository = bubblePersistentRepository;
        this.launcherApps = launcherApps2;
    }

    public final void addBubble(int i, @NotNull Bubble bubble) {
        Intrinsics.checkParameterIsNotNull(bubble, "bubble");
        addBubbles(i, CollectionsKt__CollectionsJVMKt.listOf(bubble));
    }

    public final void addBubbles(int i, @NotNull List<? extends Bubble> list) {
        Intrinsics.checkParameterIsNotNull(list, "bubbles");
        List<BubbleEntity> transform = transform(i, list);
        this.volatileRepository.addBubbles(transform);
        if (!transform.isEmpty()) {
            persistToDisk();
        }
    }

    public final void removeBubbles(int i, @NotNull List<? extends Bubble> list) {
        Intrinsics.checkParameterIsNotNull(list, "bubbles");
        List<BubbleEntity> transform = transform(i, list);
        this.volatileRepository.removeBubbles(transform);
        if (!transform.isEmpty()) {
            persistToDisk();
        }
    }

    private final void persistToDisk() {
        this.job = BuildersKt__Builders_commonKt.launch$default(this.ioScope, (CoroutineContext) null, (CoroutineStart) null, new BubbleDataRepository$persistToDisk$1(this, this.job, (Continuation) null), 3, (Object) null);
    }

    @NotNull
    @SuppressLint({"WrongConstant"})
    public final Job loadBubbles(@NotNull Function1<? super List<? extends Bubble>, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "cb");
        return BuildersKt__Builders_commonKt.launch$default(this.ioScope, (CoroutineContext) null, (CoroutineStart) null, new BubbleDataRepository$loadBubbles$1(this, function1, (Continuation) null), 3, (Object) null);
    }

    private final List<BubbleEntity> transform(int i, List<? extends Bubble> list) {
        BubbleEntity bubbleEntity;
        ArrayList arrayList = new ArrayList();
        for (Bubble bubble : list) {
            String packageName = bubble.getPackageName();
            Intrinsics.checkExpressionValueIsNotNull(packageName, "b.packageName");
            String metadataShortcutId = bubble.getMetadataShortcutId();
            if (metadataShortcutId != null) {
                String key = bubble.getKey();
                Intrinsics.checkExpressionValueIsNotNull(key, "b.key");
                bubbleEntity = new BubbleEntity(i, packageName, metadataShortcutId, key, bubble.getRawDesiredHeight(), bubble.getRawDesiredHeightResId(), bubble.getTitle());
            } else {
                bubbleEntity = null;
            }
            if (bubbleEntity != null) {
                arrayList.add(bubbleEntity);
            }
        }
        return arrayList;
    }
}
