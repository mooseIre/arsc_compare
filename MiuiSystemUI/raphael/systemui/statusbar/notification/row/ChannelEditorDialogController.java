package com.android.systemui.statusbar.notification.row;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialog;
import com.android.systemui.statusbar.notification.row.NotificationInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChannelEditorDialogController.kt */
public final class ChannelEditorDialogController {
    private Drawable appIcon;
    private String appName;
    private Boolean appNotificationsCurrentlyEnabled;
    private boolean appNotificationsEnabled = true;
    private Integer appUid;
    private final List<NotificationChannelGroup> channelGroupList = new ArrayList();
    @NotNull
    private final Context context;
    private ChannelEditorDialog dialog;
    private final ChannelEditorDialog.Builder dialogBuilder;
    private final Map<NotificationChannel, Integer> edits = new LinkedHashMap();
    @NotNull
    private final HashMap<String, CharSequence> groupNameLookup = new HashMap<>();
    private final INotificationManager noMan;
    @Nullable
    private OnChannelEditorDialogFinishedListener onFinishListener;
    private NotificationInfo.OnSettingsClickListener onSettingsClickListener;
    private String packageName;
    @NotNull
    private final List<NotificationChannel> paddedChannels = new ArrayList();
    private boolean prepared;
    private final List<NotificationChannel> providedChannels = new ArrayList();
    private final int wmFlags = -2130444288;

    @VisibleForTesting
    public static /* synthetic */ void groupNameLookup$annotations() {
    }

    @VisibleForTesting
    public static /* synthetic */ void paddedChannels$annotations() {
    }

    public ChannelEditorDialogController(@NotNull Context context2, @NotNull INotificationManager iNotificationManager, @NotNull ChannelEditorDialog.Builder builder) {
        Intrinsics.checkParameterIsNotNull(context2, "c");
        Intrinsics.checkParameterIsNotNull(iNotificationManager, "noMan");
        Intrinsics.checkParameterIsNotNull(builder, "dialogBuilder");
        this.noMan = iNotificationManager;
        this.dialogBuilder = builder;
        Context applicationContext = context2.getApplicationContext();
        Intrinsics.checkExpressionValueIsNotNull(applicationContext, "c.applicationContext");
        this.context = applicationContext;
    }

    @Nullable
    public final OnChannelEditorDialogFinishedListener getOnFinishListener() {
        return this.onFinishListener;
    }

    public final void setOnFinishListener(@Nullable OnChannelEditorDialogFinishedListener onChannelEditorDialogFinishedListener) {
        this.onFinishListener = onChannelEditorDialogFinishedListener;
    }

    @NotNull
    public final List<NotificationChannel> getPaddedChannels$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        return this.paddedChannels;
    }

    public final void prepareDialogForApp(@NotNull String str, @NotNull String str2, int i, @NotNull Set<NotificationChannel> set, @NotNull Drawable drawable, @Nullable NotificationInfo.OnSettingsClickListener onSettingsClickListener2) {
        Intrinsics.checkParameterIsNotNull(str, "appName");
        Intrinsics.checkParameterIsNotNull(str2, "packageName");
        Intrinsics.checkParameterIsNotNull(set, "channels");
        Intrinsics.checkParameterIsNotNull(drawable, "appIcon");
        this.appName = str;
        this.packageName = str2;
        this.appUid = Integer.valueOf(i);
        this.appIcon = drawable;
        boolean checkAreAppNotificationsOn = checkAreAppNotificationsOn();
        this.appNotificationsEnabled = checkAreAppNotificationsOn;
        this.onSettingsClickListener = onSettingsClickListener2;
        this.appNotificationsCurrentlyEnabled = Boolean.valueOf(checkAreAppNotificationsOn);
        this.channelGroupList.clear();
        this.channelGroupList.addAll(fetchNotificationChannelGroups());
        buildGroupNameLookup();
        this.providedChannels.clear();
        this.providedChannels.addAll(set);
        padToFourChannels(set);
        initDialog();
        this.prepared = true;
    }

    private final void buildGroupNameLookup() {
        for (T t : this.channelGroupList) {
            if (t.getId() != null) {
                HashMap<String, CharSequence> hashMap = this.groupNameLookup;
                String id = t.getId();
                Intrinsics.checkExpressionValueIsNotNull(id, "group.id");
                CharSequence name = t.getName();
                Intrinsics.checkExpressionValueIsNotNull(name, "group.name");
                hashMap.put(id, name);
            }
        }
    }

    private final void padToFourChannels(Set<NotificationChannel> set) {
        this.paddedChannels.clear();
        boolean unused = CollectionsKt__MutableCollectionsKt.addAll(this.paddedChannels, SequencesKt___SequencesKt.take(CollectionsKt___CollectionsKt.asSequence(set), 4));
        boolean unused2 = CollectionsKt__MutableCollectionsKt.addAll(this.paddedChannels, SequencesKt___SequencesKt.take(SequencesKt___SequencesKt.distinct(SequencesKt___SequencesKt.filterNot(getDisplayableChannels(CollectionsKt___CollectionsKt.asSequence(this.channelGroupList)), new ChannelEditorDialogController$padToFourChannels$1(this))), 4 - this.paddedChannels.size()));
        if (this.paddedChannels.size() == 1 && Intrinsics.areEqual("miscellaneous", this.paddedChannels.get(0).getId())) {
            this.paddedChannels.clear();
        }
    }

    private final Sequence<NotificationChannel> getDisplayableChannels(Sequence<NotificationChannelGroup> sequence) {
        return SequencesKt___SequencesKt.sortedWith(SequencesKt___SequencesKt.flatMap(sequence, ChannelEditorDialogController$getDisplayableChannels$channels$1.INSTANCE), new ChannelEditorDialogController$getDisplayableChannels$$inlined$compareBy$1());
    }

    public final void show() {
        if (this.prepared) {
            ChannelEditorDialog channelEditorDialog = this.dialog;
            if (channelEditorDialog != null) {
                channelEditorDialog.show();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("dialog");
                throw null;
            }
        } else {
            throw new IllegalStateException("Must call prepareDialogForApp() before calling show()");
        }
    }

    public final void close() {
        done();
    }

    /* access modifiers changed from: private */
    public final void done() {
        resetState();
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog != null) {
            channelEditorDialog.dismiss();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
    }

    private final void resetState() {
        this.appIcon = null;
        this.appUid = null;
        this.packageName = null;
        this.appName = null;
        this.appNotificationsCurrentlyEnabled = null;
        this.edits.clear();
        this.paddedChannels.clear();
        this.providedChannels.clear();
        this.groupNameLookup.clear();
    }

    @NotNull
    public final CharSequence groupNameForId(@Nullable String str) {
        CharSequence charSequence = this.groupNameLookup.get(str);
        return charSequence != null ? charSequence : "";
    }

    public final void proposeEditForChannel(@NotNull NotificationChannel notificationChannel, int i) {
        Intrinsics.checkParameterIsNotNull(notificationChannel, "channel");
        if (notificationChannel.getImportance() == i) {
            this.edits.remove(notificationChannel);
        } else {
            this.edits.put(notificationChannel, Integer.valueOf(i));
        }
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog != null) {
            channelEditorDialog.updateDoneButtonText(hasChanges());
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
    }

    public final void proposeSetAppNotificationsEnabled(boolean z) {
        this.appNotificationsEnabled = z;
        ChannelEditorDialog channelEditorDialog = this.dialog;
        if (channelEditorDialog != null) {
            channelEditorDialog.updateDoneButtonText(hasChanges());
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
    }

    public final boolean areAppNotificationsEnabled() {
        return this.appNotificationsEnabled;
    }

    private final boolean hasChanges() {
        return (this.edits.isEmpty() ^ true) || (Intrinsics.areEqual(Boolean.valueOf(this.appNotificationsEnabled), this.appNotificationsCurrentlyEnabled) ^ true);
    }

    private final List<NotificationChannelGroup> fetchNotificationChannelGroups() {
        try {
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            List<NotificationChannelGroup> list = null;
            if (str != null) {
                Integer num = this.appUid;
                if (num != null) {
                    ParceledListSlice notificationChannelGroupsForPackage = iNotificationManager.getNotificationChannelGroupsForPackage(str, num.intValue(), false);
                    Intrinsics.checkExpressionValueIsNotNull(notificationChannelGroupsForPackage, "noMan.getNotificationCha…eName!!, appUid!!, false)");
                    List<NotificationChannelGroup> list2 = notificationChannelGroupsForPackage.getList();
                    if (list2 instanceof List) {
                        list = list2;
                    }
                    if (list != null) {
                        return list;
                    }
                    return CollectionsKt__CollectionsKt.emptyList();
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Error fetching channel groups", e);
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }

    private final boolean checkAreAppNotificationsOn() {
        try {
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str != null) {
                Integer num = this.appUid;
                if (num != null) {
                    return iNotificationManager.areNotificationsEnabledForPackage(str, num.intValue());
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Error calling NoMan", e);
            return false;
        }
    }

    private final void applyAppNotificationsOn(boolean z) {
        try {
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str != null) {
                Integer num = this.appUid;
                if (num != null) {
                    iNotificationManager.setNotificationsEnabledForPackage(str, num.intValue(), z);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Error calling NoMan", e);
        }
    }

    private final void setChannelImportance(NotificationChannel notificationChannel, int i) {
        try {
            notificationChannel.setImportance(i);
            INotificationManager iNotificationManager = this.noMan;
            String str = this.packageName;
            if (str != null) {
                Integer num = this.appUid;
                if (num != null) {
                    iNotificationManager.updateNotificationChannelForPackage(str, num.intValue(), notificationChannel);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } catch (Exception e) {
            Log.e("ChannelDialogController", "Unable to update notification importance", e);
        }
    }

    @VisibleForTesting
    public final void apply() {
        for (Map.Entry<NotificationChannel, Integer> entry : this.edits.entrySet()) {
            NotificationChannel key = entry.getKey();
            int intValue = entry.getValue().intValue();
            if (key.getImportance() != intValue) {
                setChannelImportance(key, intValue);
            }
        }
        if (!Intrinsics.areEqual(Boolean.valueOf(this.appNotificationsEnabled), this.appNotificationsCurrentlyEnabled)) {
            applyAppNotificationsOn(this.appNotificationsEnabled);
        }
    }

    @VisibleForTesting
    public final void launchSettings(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "sender");
        NotificationInfo.OnSettingsClickListener onSettingsClickListener2 = this.onSettingsClickListener;
        if (onSettingsClickListener2 != null) {
            Integer num = this.appUid;
            if (num != null) {
                onSettingsClickListener2.onClick(view, null, num.intValue());
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
    }

    private final void initDialog() {
        this.dialogBuilder.setContext(this.context);
        ChannelEditorDialog build = this.dialogBuilder.build();
        this.dialog = build;
        if (build != null) {
            Window window = build.getWindow();
            if (window != null) {
                window.requestFeature(1);
            }
            ChannelEditorDialog channelEditorDialog = this.dialog;
            if (channelEditorDialog != null) {
                channelEditorDialog.setTitle(" ");
                ChannelEditorDialog channelEditorDialog2 = this.dialog;
                if (channelEditorDialog2 != null) {
                    channelEditorDialog2.setContentView(C0017R$layout.notif_half_shelf);
                    channelEditorDialog2.setCanceledOnTouchOutside(true);
                    channelEditorDialog2.setOnDismissListener(new ChannelEditorDialogController$initDialog$$inlined$apply$lambda$1(this));
                    ChannelEditorListView channelEditorListView = (ChannelEditorListView) channelEditorDialog2.findViewById(C0015R$id.half_shelf_container);
                    if (channelEditorListView != null) {
                        channelEditorListView.setController(this);
                        channelEditorListView.setAppIcon(this.appIcon);
                        channelEditorListView.setAppName(this.appName);
                        channelEditorListView.setChannels(this.paddedChannels);
                    }
                    channelEditorDialog2.setOnShowListener(new ChannelEditorDialogController$initDialog$$inlined$apply$lambda$2(channelEditorListView, this));
                    TextView textView = (TextView) channelEditorDialog2.findViewById(C0015R$id.done_button);
                    if (textView != null) {
                        textView.setOnClickListener(new ChannelEditorDialogController$initDialog$$inlined$apply$lambda$3(this));
                    }
                    TextView textView2 = (TextView) channelEditorDialog2.findViewById(C0015R$id.see_more_button);
                    if (textView2 != null) {
                        textView2.setOnClickListener(new ChannelEditorDialogController$initDialog$$inlined$apply$lambda$4(this));
                    }
                    Window window2 = channelEditorDialog2.getWindow();
                    if (window2 != null) {
                        window2.setBackgroundDrawable(new ColorDrawable(0));
                        window2.addFlags(this.wmFlags);
                        window2.setType(2017);
                        window2.setWindowAnimations(16973910);
                        WindowManager.LayoutParams attributes = window2.getAttributes();
                        attributes.format = -3;
                        attributes.setTitle(ChannelEditorDialogController.class.getSimpleName());
                        attributes.gravity = 81;
                        WindowManager.LayoutParams attributes2 = window2.getAttributes();
                        Intrinsics.checkExpressionValueIsNotNull(attributes2, "attributes");
                        attributes.setFitInsetsTypes(attributes2.getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
                        attributes.width = -1;
                        attributes.height = -2;
                        window2.setAttributes(attributes);
                        return;
                    }
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("dialog");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dialog");
        throw null;
    }
}
