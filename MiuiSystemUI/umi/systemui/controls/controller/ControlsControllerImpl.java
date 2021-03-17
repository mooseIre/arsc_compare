package com.android.systemui.controls.controller;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.controls.Control;
import android.service.controls.actions.ControlAction;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl implements Dumpable, ControlsController {
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    private static final Uri URI = Settings.Secure.getUriFor("controls_enabled");
    @NotNull
    private AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper;
    /* access modifiers changed from: private */
    public boolean available = Companion.isAvailable(getCurrentUserId(), getContentResolver());
    /* access modifiers changed from: private */
    public final ControlsBindingController bindingController;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    /* access modifiers changed from: private */
    public UserHandle currentUser = UserHandle.of(ActivityManager.getCurrentUser());
    /* access modifiers changed from: private */
    public final DelayableExecutor executor;
    private final ControlsControllerImpl$listingCallback$1 listingCallback;
    private final ControlsListingController listingController;
    /* access modifiers changed from: private */
    public final ControlsFavoritePersistenceWrapper persistenceWrapper;
    @NotNull
    private final BroadcastReceiver restoreFinishedReceiver;
    /* access modifiers changed from: private */
    public final List<Consumer<Boolean>> seedingCallbacks = new ArrayList();
    /* access modifiers changed from: private */
    public boolean seedingInProgress;
    @NotNull
    private final ContentObserver settingObserver;
    private final ControlsUiController uiController;
    /* access modifiers changed from: private */
    public boolean userChanging = true;
    /* access modifiers changed from: private */
    public UserStructure userStructure;
    private final ControlsControllerImpl$userSwitchReceiver$1 userSwitchReceiver;

    @VisibleForTesting
    public static /* synthetic */ void auxiliaryPersistenceWrapper$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void restoreFinishedReceiver$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void settingObserver$annotations() {
    }

    public ControlsControllerImpl(@NotNull Context context2, @NotNull DelayableExecutor delayableExecutor, @NotNull ControlsUiController controlsUiController, @NotNull ControlsBindingController controlsBindingController, @NotNull ControlsListingController controlsListingController, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull Optional<ControlsFavoritePersistenceWrapper> optional, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
        Intrinsics.checkParameterIsNotNull(controlsUiController, "uiController");
        Intrinsics.checkParameterIsNotNull(controlsBindingController, "bindingController");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "listingController");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(optional, "optionalWrapper");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.context = context2;
        this.executor = delayableExecutor;
        this.uiController = controlsUiController;
        this.bindingController = controlsBindingController;
        this.listingController = controlsListingController;
        this.broadcastDispatcher = broadcastDispatcher2;
        Context context3 = this.context;
        UserHandle userHandle = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "currentUser");
        this.userStructure = new UserStructure(context3, userHandle);
        ControlsFavoritePersistenceWrapper orElseGet = optional.orElseGet(new Supplier<ControlsFavoritePersistenceWrapper>(this) {
            final /* synthetic */ ControlsControllerImpl this$0;

            {
                this.this$0 = r1;
            }

            @NotNull
            public final ControlsFavoritePersistenceWrapper get() {
                File file = this.this$0.userStructure.getFile();
                Intrinsics.checkExpressionValueIsNotNull(file, "userStructure.file");
                return new ControlsFavoritePersistenceWrapper(file, this.this$0.executor, new BackupManager(this.this$0.userStructure.getUserContext()));
            }
        });
        Intrinsics.checkExpressionValueIsNotNull(orElseGet, "optionalWrapper.orElseGe…)\n            )\n        }");
        this.persistenceWrapper = orElseGet;
        File auxiliaryFile = this.userStructure.getAuxiliaryFile();
        Intrinsics.checkExpressionValueIsNotNull(auxiliaryFile, "userStructure.auxiliaryFile");
        this.auxiliaryPersistenceWrapper = new AuxiliaryPersistenceWrapper(auxiliaryFile, this.executor);
        this.userSwitchReceiver = new ControlsControllerImpl$userSwitchReceiver$1(this);
        this.restoreFinishedReceiver = new ControlsControllerImpl$restoreFinishedReceiver$1(this);
        this.settingObserver = new ControlsControllerImpl$settingObserver$1(this, (Handler) null);
        this.listingCallback = new ControlsControllerImpl$listingCallback$1(this);
        String name = ControlsControllerImpl.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
        resetFavorites(getAvailable());
        this.userChanging = false;
        BroadcastDispatcher broadcastDispatcher3 = this.broadcastDispatcher;
        ControlsControllerImpl$userSwitchReceiver$1 controlsControllerImpl$userSwitchReceiver$1 = this.userSwitchReceiver;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.USER_SWITCHED");
        DelayableExecutor delayableExecutor2 = this.executor;
        UserHandle userHandle2 = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle2, "UserHandle.ALL");
        broadcastDispatcher3.registerReceiver(controlsControllerImpl$userSwitchReceiver$1, intentFilter, delayableExecutor2, userHandle2);
        this.context.registerReceiver(this.restoreFinishedReceiver, new IntentFilter("com.android.systemui.backup.RESTORE_FINISHED"), "com.android.systemui.permission.SELF", (Handler) null);
        getContentResolver().registerContentObserver(URI, false, this.settingObserver, -1);
        this.listingController.addCallback(this.listingCallback);
    }

    /* compiled from: ControlsControllerImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* access modifiers changed from: private */
        public final boolean isAvailable(int i, ContentResolver contentResolver) {
            return Settings.Secure.getIntForUser(contentResolver, "controls_enabled", 1, i) != 0;
        }
    }

    public int getCurrentUserId() {
        UserHandle userHandle = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "currentUser");
        return userHandle.getIdentifier();
    }

    /* access modifiers changed from: private */
    public final ContentResolver getContentResolver() {
        ContentResolver contentResolver = this.context.getContentResolver();
        Intrinsics.checkExpressionValueIsNotNull(contentResolver, "context.contentResolver");
        return contentResolver;
    }

    public boolean getAvailable() {
        return this.available;
    }

    @NotNull
    public final AuxiliaryPersistenceWrapper getAuxiliaryPersistenceWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        return this.auxiliaryPersistenceWrapper;
    }

    /* access modifiers changed from: private */
    public final void setValuesForUser(UserHandle userHandle) {
        Log.d("ControlsControllerImpl", "Changing to user: " + userHandle);
        this.currentUser = userHandle;
        Context context2 = this.context;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "currentUser");
        UserStructure userStructure2 = new UserStructure(context2, userHandle);
        this.userStructure = userStructure2;
        ControlsFavoritePersistenceWrapper controlsFavoritePersistenceWrapper = this.persistenceWrapper;
        File file = userStructure2.getFile();
        Intrinsics.checkExpressionValueIsNotNull(file, "userStructure.file");
        controlsFavoritePersistenceWrapper.changeFileAndBackupManager(file, new BackupManager(this.userStructure.getUserContext()));
        AuxiliaryPersistenceWrapper auxiliaryPersistenceWrapper2 = this.auxiliaryPersistenceWrapper;
        File auxiliaryFile = this.userStructure.getAuxiliaryFile();
        Intrinsics.checkExpressionValueIsNotNull(auxiliaryFile, "userStructure.auxiliaryFile");
        auxiliaryPersistenceWrapper2.changeFile(auxiliaryFile);
        this.available = Companion.isAvailable(userHandle.getIdentifier(), getContentResolver());
        resetFavorites(getAvailable());
        this.bindingController.changeUser(userHandle);
        this.listingController.changeUser(userHandle);
        this.userChanging = false;
    }

    /* access modifiers changed from: private */
    public final void resetFavorites(boolean z) {
        Favorites.INSTANCE.clear();
        if (z) {
            Favorites.INSTANCE.load(this.persistenceWrapper.readFavorites());
        }
    }

    private final boolean confirmAvailability() {
        if (this.userChanging) {
            Log.w("ControlsControllerImpl", "Controls not available while user is changing");
            return false;
        } else if (getAvailable()) {
            return true;
        } else {
            Log.d("ControlsControllerImpl", "Controls not available");
            return false;
        }
    }

    public void loadForComponent(@NotNull ComponentName componentName, @NotNull Consumer<ControlsController.LoadData> consumer, @NotNull Consumer<Runnable> consumer2) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(consumer, "dataCallback");
        Intrinsics.checkParameterIsNotNull(consumer2, "cancelWrapper");
        if (!confirmAvailability()) {
            if (this.userChanging) {
                this.executor.executeDelayed(new ControlsControllerImpl$loadForComponent$1(this, componentName, consumer, consumer2), 500, TimeUnit.MILLISECONDS);
            }
            consumer.accept(ControlsControllerKt.createLoadDataObject(CollectionsKt__CollectionsKt.emptyList(), CollectionsKt__CollectionsKt.emptyList(), true));
        }
        consumer2.accept(this.bindingController.bindAndLoad(componentName, new ControlsControllerImpl$loadForComponent$2(this, componentName, consumer)));
    }

    public boolean addSeedingFavoritesCallback(@NotNull Consumer<Boolean> consumer) {
        Intrinsics.checkParameterIsNotNull(consumer, "callback");
        if (!this.seedingInProgress) {
            return false;
        }
        this.executor.execute(new ControlsControllerImpl$addSeedingFavoritesCallback$1(this, consumer));
        return true;
    }

    public void seedFavoritesForComponents(@NotNull List<ComponentName> list, @NotNull Consumer<SeedResponse> consumer) {
        Intrinsics.checkParameterIsNotNull(list, "componentNames");
        Intrinsics.checkParameterIsNotNull(consumer, "callback");
        if (!this.seedingInProgress && !list.isEmpty()) {
            if (confirmAvailability()) {
                this.seedingInProgress = true;
                startSeeding(list, consumer, false);
            } else if (this.userChanging) {
                this.executor.executeDelayed(new ControlsControllerImpl$seedFavoritesForComponents$1(this, list, consumer), 500, TimeUnit.MILLISECONDS);
            } else {
                for (ComponentName packageName : list) {
                    String packageName2 = packageName.getPackageName();
                    Intrinsics.checkExpressionValueIsNotNull(packageName2, "it.packageName");
                    consumer.accept(new SeedResponse(packageName2, false));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final void startSeeding(List<ComponentName> list, Consumer<SeedResponse> consumer, boolean z) {
        if (list.isEmpty()) {
            endSeedingCall(!z);
            return;
        }
        ComponentName componentName = list.get(0);
        Log.d("ControlsControllerImpl", "Beginning request to seed favorites for: " + componentName);
        this.bindingController.bindAndLoadSuggested(componentName, new ControlsControllerImpl$startSeeding$1(this, componentName, consumer, CollectionsKt___CollectionsKt.drop(list, 1), z));
    }

    private final void endSeedingCall(boolean z) {
        this.seedingInProgress = false;
        for (Consumer accept : this.seedingCallbacks) {
            accept.accept(Boolean.valueOf(z));
        }
        this.seedingCallbacks.clear();
    }

    static /* synthetic */ ControlStatus createRemovedStatus$default(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, ControlInfo controlInfo, CharSequence charSequence, boolean z, int i, Object obj) {
        if ((i & 8) != 0) {
            z = true;
        }
        return controlsControllerImpl.createRemovedStatus(componentName, controlInfo, charSequence, z);
    }

    /* access modifiers changed from: private */
    public final ControlStatus createRemovedStatus(ComponentName componentName, ControlInfo controlInfo, CharSequence charSequence, boolean z) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(componentName.getPackageName());
        Control build = new Control.StatelessBuilder(controlInfo.getControlId(), PendingIntent.getActivity(this.context, componentName.hashCode(), intent, 0)).setTitle(controlInfo.getControlTitle()).setSubtitle(controlInfo.getControlSubtitle()).setStructure(charSequence).setDeviceType(controlInfo.getDeviceType()).build();
        Intrinsics.checkExpressionValueIsNotNull(build, "control");
        return new ControlStatus(build, componentName, true, z);
    }

    public void subscribeToFavorites(@NotNull StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "structureInfo");
        if (confirmAvailability()) {
            this.bindingController.subscribe(structureInfo);
        }
    }

    public void unsubscribe() {
        if (confirmAvailability()) {
            this.bindingController.unsubscribe();
        }
    }

    public void addFavorite(@NotNull ComponentName componentName, @NotNull CharSequence charSequence, @NotNull ControlInfo controlInfo) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(charSequence, "structureName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        if (confirmAvailability()) {
            this.executor.execute(new ControlsControllerImpl$addFavorite$1(this, componentName, charSequence, controlInfo));
        }
    }

    public void replaceFavoritesForStructure(@NotNull StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "structureInfo");
        if (confirmAvailability()) {
            this.executor.execute(new ControlsControllerImpl$replaceFavoritesForStructure$1(this, structureInfo));
        }
    }

    public void refreshStatus(@NotNull ComponentName componentName, @NotNull Control control) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(control, "control");
        if (!confirmAvailability()) {
            Log.d("ControlsControllerImpl", "Controls not available");
            return;
        }
        if (control.getStatus() == 1) {
            this.executor.execute(new ControlsControllerImpl$refreshStatus$1(this, componentName, control));
        }
        this.uiController.onRefreshState(componentName, CollectionsKt__CollectionsJVMKt.listOf(control));
    }

    public void onActionResponse(@NotNull ComponentName componentName, @NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        if (confirmAvailability()) {
            this.uiController.onActionResponse(componentName, str, i);
        }
    }

    public void action(@NotNull ComponentName componentName, @NotNull ControlInfo controlInfo, @NotNull ControlAction controlAction) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        Intrinsics.checkParameterIsNotNull(controlAction, "action");
        if (confirmAvailability()) {
            this.bindingController.action(componentName, controlInfo, controlAction);
        }
    }

    @NotNull
    public List<StructureInfo> getFavorites() {
        return Favorites.INSTANCE.getAllStructures();
    }

    public int countFavoritesForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        return Favorites.INSTANCE.getControlsForComponent(componentName).size();
    }

    @NotNull
    public List<StructureInfo> getFavoritesForComponent(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        return Favorites.INSTANCE.getStructuresForComponent(componentName);
    }

    @NotNull
    public List<ControlInfo> getFavoritesForStructure(@NotNull ComponentName componentName, @NotNull CharSequence charSequence) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(charSequence, "structureName");
        return Favorites.INSTANCE.getControlsForStructure(new StructureInfo(componentName, charSequence, CollectionsKt__CollectionsKt.emptyList()));
    }

    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("ControlsController state:");
        printWriter.println("  Available: " + getAvailable());
        printWriter.println("  Changing users: " + this.userChanging);
        StringBuilder sb = new StringBuilder();
        sb.append("  Current user: ");
        UserHandle userHandle = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "currentUser");
        sb.append(userHandle.getIdentifier());
        printWriter.println(sb.toString());
        printWriter.println("  Favorites:");
        for (StructureInfo structureInfo : Favorites.INSTANCE.getAllStructures()) {
            printWriter.println("    " + structureInfo);
            for (ControlInfo controlInfo : structureInfo.getControls()) {
                printWriter.println("      " + controlInfo);
            }
        }
        printWriter.println(this.bindingController.toString());
    }

    /* access modifiers changed from: private */
    public final Set<String> findRemoved(Set<String> set, List<Control> list) {
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (Control controlId : list) {
            arrayList.add(controlId.getControlId());
        }
        return SetsKt___SetsKt.minus(set, arrayList);
    }
}
