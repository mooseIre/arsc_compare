package com.android.systemui.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener implements MediaDataManager.Listener {
    private final Executor backgroundExecutor;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private int currentUserId = this.context.getUserId();
    private ResumeMediaBrowser mediaBrowser;
    private final MediaResumeListener$mediaBrowserCallback$1 mediaBrowserCallback = new MediaResumeListener$mediaBrowserCallback$1(this);
    private MediaDataManager mediaDataManager;
    private final ConcurrentLinkedQueue<ComponentName> resumeComponents = new ConcurrentLinkedQueue<>();
    private final TunerService tunerService;
    private boolean useMediaResumption;
    private final MediaResumeListener$userChangeReceiver$1 userChangeReceiver = new MediaResumeListener$userChangeReceiver$1(this);

    public MediaResumeListener(@NotNull Context context2, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull Executor executor, @NotNull TunerService tunerService2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(tunerService2, "tunerService");
        this.context = context2;
        this.broadcastDispatcher = broadcastDispatcher2;
        this.backgroundExecutor = executor;
        this.tunerService = tunerService2;
        this.useMediaResumption = Utils.useMediaResumption(context2);
        if (this.useMediaResumption) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            BroadcastDispatcher broadcastDispatcher3 = this.broadcastDispatcher;
            MediaResumeListener$userChangeReceiver$1 mediaResumeListener$userChangeReceiver$1 = this.userChangeReceiver;
            UserHandle userHandle = UserHandle.ALL;
            Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
            broadcastDispatcher3.registerReceiver(mediaResumeListener$userChangeReceiver$1, intentFilter, null, userHandle);
            loadSavedComponents();
        }
    }

    public static final /* synthetic */ MediaDataManager access$getMediaDataManager$p(MediaResumeListener mediaResumeListener) {
        MediaDataManager mediaDataManager2 = mediaResumeListener.mediaDataManager;
        if (mediaDataManager2 != null) {
            return mediaDataManager2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
        throw null;
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        MediaDataManager.Listener.DefaultImpls.onMediaDataRemoved(this, str);
    }

    public final void setManager(@NotNull MediaDataManager mediaDataManager2) {
        Intrinsics.checkParameterIsNotNull(mediaDataManager2, "manager");
        this.mediaDataManager = mediaDataManager2;
        this.tunerService.addTunable(new MediaResumeListener$setManager$1(this), "qs_media_resumption");
    }

    public final boolean isResumptionEnabled() {
        return this.useMediaResumption;
    }

    /* access modifiers changed from: private */
    public final void loadSavedComponents() {
        List<String> split;
        boolean z;
        this.resumeComponents.clear();
        List<String> list = null;
        String string = this.context.getSharedPreferences("media_control_prefs", 0).getString("browser_components_" + this.currentUserId, null);
        if (string != null && (split = new Regex(":").split(string, 0)) != null) {
            if (!split.isEmpty()) {
                ListIterator<String> listIterator = split.listIterator(split.size());
                while (true) {
                    if (!listIterator.hasPrevious()) {
                        break;
                    }
                    if (listIterator.previous().length() == 0) {
                        z = true;
                        continue;
                    } else {
                        z = false;
                        continue;
                    }
                    if (!z) {
                        list = CollectionsKt___CollectionsKt.take(split, listIterator.nextIndex() + 1);
                        break;
                    }
                }
            }
            list = CollectionsKt__CollectionsKt.emptyList();
        }
        if (list != null) {
            for (String str : list) {
                List list2 = StringsKt__StringsKt.split$default(str, new String[]{"/"}, false, 0, 6, null);
                this.resumeComponents.add(new ComponentName((String) list2.get(0), (String) list2.get(1)));
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("loaded resume components ");
        Object[] array = this.resumeComponents.toArray();
        Intrinsics.checkExpressionValueIsNotNull(array, "resumeComponents.toArray()");
        String arrays = Arrays.toString(array);
        Intrinsics.checkExpressionValueIsNotNull(arrays, "java.util.Arrays.toString(this)");
        sb.append(arrays);
        Log.d("MediaResumeListener", sb.toString());
    }

    /* access modifiers changed from: private */
    public final void loadMediaResumptionControls() {
        if (this.useMediaResumption) {
            Iterator<T> it = this.resumeComponents.iterator();
            while (it.hasNext()) {
                new ResumeMediaBrowser(this.context, this.mediaBrowserCallback, it.next()).findRecentMedia();
            }
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        ArrayList arrayList;
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        if (this.useMediaResumption) {
            ResumeMediaBrowser resumeMediaBrowser = this.mediaBrowser;
            if (resumeMediaBrowser != null) {
                resumeMediaBrowser.disconnect();
            }
            if (mediaData.getResumeAction() == null && !mediaData.getHasCheckedForResume()) {
                Log.d("MediaResumeListener", "Checking for service component for " + mediaData.getPackageName());
                List<ResolveInfo> queryIntentServices = this.context.getPackageManager().queryIntentServices(new Intent("android.media.browse.MediaBrowserService"), 0);
                if (queryIntentServices != null) {
                    arrayList = new ArrayList();
                    for (T t : queryIntentServices) {
                        if (Intrinsics.areEqual(((ResolveInfo) t).serviceInfo.packageName, mediaData.getPackageName())) {
                            arrayList.add(t);
                        }
                    }
                } else {
                    arrayList = null;
                }
                if (arrayList == null || arrayList.size() <= 0) {
                    MediaDataManager mediaDataManager2 = this.mediaDataManager;
                    if (mediaDataManager2 != null) {
                        mediaDataManager2.setResumeAction(str, null);
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
                        throw null;
                    }
                } else {
                    this.backgroundExecutor.execute(new MediaResumeListener$onMediaDataLoaded$1(this, str, arrayList));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final void tryUpdateResumptionList(String str, ComponentName componentName) {
        Log.d("MediaResumeListener", "Testing if we can connect to " + componentName);
        ResumeMediaBrowser resumeMediaBrowser = this.mediaBrowser;
        if (resumeMediaBrowser != null) {
            resumeMediaBrowser.disconnect();
        }
        ResumeMediaBrowser resumeMediaBrowser2 = new ResumeMediaBrowser(this.context, new MediaResumeListener$tryUpdateResumptionList$1(this, componentName, str), componentName);
        this.mediaBrowser = resumeMediaBrowser2;
        if (resumeMediaBrowser2 != null) {
            resumeMediaBrowser2.testConnection();
        }
    }

    /* access modifiers changed from: private */
    public final void updateResumptionList(ComponentName componentName) {
        this.resumeComponents.remove(componentName);
        this.resumeComponents.add(componentName);
        if (this.resumeComponents.size() > 5) {
            this.resumeComponents.remove();
        }
        StringBuilder sb = new StringBuilder();
        Iterator<T> it = this.resumeComponents.iterator();
        while (it.hasNext()) {
            sb.append(it.next().flattenToString());
            sb.append(":");
        }
        SharedPreferences.Editor edit = this.context.getSharedPreferences("media_control_prefs", 0).edit();
        edit.putString("browser_components_" + this.currentUserId, sb.toString()).apply();
    }

    /* access modifiers changed from: private */
    public final Runnable getResumeAction(ComponentName componentName) {
        return new MediaResumeListener$getResumeAction$1(this, componentName);
    }
}
