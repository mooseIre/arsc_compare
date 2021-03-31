package com.android.systemui.statusbar.notification.row;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.PushEvents;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.NotificationMenuRow;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.util.ViewAnimUtils;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.events.ModalDialogExitMode;
import com.miui.systemui.events.ModalDialogSource;
import com.miui.systemui.events.ModalExitMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import miui.app.AlertDialog;

public class MiuiNotificationMenuRow implements NotificationMenuRowPlugin {
    private AlertDialog mAlertDialog;
    private String mAppName;
    private NotificationMenuRowPlugin.MenuItem mAppOpsItem;
    private int mChoiceIndex = 0;
    private Context mContext;
    private NotificationMenuRowPlugin.MenuItem mInfoItem;
    private ViewGroup mMenuContainer;
    private ArrayList<NotificationMenuRowPlugin.MenuItem> mMenuItems = new ArrayList<>();
    private int mMenuMargin;
    private ModalController.OnModalChangeListener mOnModalChangeListener = new ModalController.OnModalChangeListener() {
        /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$s38gBSbWszbfTXByhYvQyjgc58w */

        @Override // com.android.systemui.statusbar.notification.modal.ModalController.OnModalChangeListener
        public final void onChange(boolean z) {
            MiuiNotificationMenuRow.this.lambda$new$0$MiuiNotificationMenuRow(z);
        }
    };
    private ExpandableNotificationRow mParent;
    private ExpandedNotification mSbn;
    private NotificationMenuRowPlugin.MenuItem mSnoozeItem;

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean canBeDismissed() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public int getMenuSnapTarget() {
        return 0;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public Point getRevealAnimationOrigin() {
        return null;
    }

    @Override // com.android.systemui.plugins.Plugin
    public int getVersion() {
        return 0;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean isMenuVisible() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean isSnappedAndOnSameSide() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean isSwipedEnoughToShowMenu() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean isTowardsMenu(float f) {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean isWithinSnapMenuThreshold() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public NotificationMenuRowPlugin.MenuItem menuItemToExposeOnSnap() {
        return null;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onConfigurationChanged() {
    }

    @Override // com.android.systemui.plugins.Plugin
    public void onCreate(Context context, Context context2) {
    }

    @Override // com.android.systemui.plugins.Plugin
    public void onDestroy() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onDismiss() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean onInterceptTouchEvent(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onParentHeightUpdate() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onParentTranslationUpdate(float f) {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onSnapClosed() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onSnapOpen() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onTouchEnd() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onTouchMove(float f) {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onTouchStart() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void resetMenu() {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void setDismissRtl(boolean z) {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void setMenuClickListener(NotificationMenuRowPlugin.OnMenuEventListener onMenuEventListener) {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void setMenuItems(ArrayList<NotificationMenuRowPlugin.MenuItem> arrayList) {
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean shouldShowGutsOnSnapOpen() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean shouldShowMenu() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean shouldSnapBack() {
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public boolean shouldUseDefaultMenuItems() {
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiNotificationMenuRow(boolean z) {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null && !z) {
            alertDialog.dismiss();
        }
    }

    public MiuiNotificationMenuRow(Context context, PeopleNotificationIdentifier peopleNotificationIdentifier) {
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        this.mMenuMargin = context.getResources().getDimensionPixelOffset(C0012R$dimen.miui_notification_modal_menu_margin_left_right);
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public ArrayList<NotificationMenuRowPlugin.MenuItem> getMenuItems(Context context) {
        return this.mMenuItems;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public NotificationMenuRowPlugin.MenuItem getLongpressMenuItem(Context context) {
        return this.mInfoItem;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public NotificationMenuRowPlugin.MenuItem getAppOpsMenuItem(Context context) {
        return this.mAppOpsItem;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public NotificationMenuRowPlugin.MenuItem getSnoozeMenuItem(Context context) {
        return this.mSnoozeItem;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void setAppName(String str) {
        this.mAppName = str;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void createMenu(ViewGroup viewGroup, StatusBarNotification statusBarNotification) {
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) viewGroup;
        this.mParent = expandableNotificationRow;
        this.mSbn = expandableNotificationRow.getEntry().getSbn();
        createMenuViews(true, (statusBarNotification == null || (statusBarNotification.getNotification().flags & 64) == 0) ? false : true);
    }

    private void createMenuViews(boolean z, boolean z2) {
        this.mMenuItems.clear();
        this.mAppOpsItem = createAppOpsItem(this.mContext);
        ExpandedNotification expandedNotification = this.mSbn;
        if (canBlock(this.mContext, expandedNotification)) {
            this.mMenuItems.add(createAllowItem(this.mContext));
        }
        if (canFold(this.mContext, expandedNotification)) {
            this.mMenuItems.add(createFoldItem(this.mContext));
        }
        if (canAggregate(this.mContext, expandedNotification)) {
            this.mMenuItems.add(createAggregateFeedbackItem(this.mContext));
        }
        boolean z3 = BuildConfig.IS_INTERNATIONAL;
        if (!z2 && z3) {
            NotificationMenuRowPlugin.MenuItem createSnoozeItem = createSnoozeItem(this.mContext);
            this.mSnoozeItem = createSnoozeItem;
            this.mMenuItems.add(createSnoozeItem);
        }
        NotificationMenuRowPlugin.MenuItem createInfoItem = createInfoItem(this.mContext);
        this.mInfoItem = createInfoItem;
        this.mMenuItems.add(createInfoItem);
        ViewGroup viewGroup = this.mMenuContainer;
        if (viewGroup != null) {
            viewGroup.removeAllViews();
        } else {
            this.mMenuContainer = new LinearLayout(this.mContext);
        }
        this.mMenuItems.forEach(new Consumer() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$e3yWaPYW7pcVsU2VOj_jiCrATs */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                MiuiNotificationMenuRow.this.lambda$createMenuViews$1$MiuiNotificationMenuRow((NotificationMenuRowPlugin.MenuItem) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createMenuViews$1 */
    public /* synthetic */ void lambda$createMenuViews$1$MiuiNotificationMenuRow(NotificationMenuRowPlugin.MenuItem menuItem) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        int i = this.mMenuMargin;
        layoutParams.leftMargin = i;
        layoutParams.rightMargin = i;
        this.mMenuContainer.addView(menuItem.getMenuView(), layoutParams);
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public View getMenuView() {
        return this.mMenuContainer;
    }

    @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin
    public void onNotificationUpdated(StatusBarNotification statusBarNotification) {
        if (this.mMenuContainer != null) {
            boolean z = true;
            boolean z2 = !isMenuVisible();
            if ((statusBarNotification.getNotification().flags & 64) == 0) {
                z = false;
            }
            createMenuViews(z2, z);
        }
    }

    private NotificationMenuRowPlugin.MenuItem createAllowItem(Context context) {
        MiuiNotificationMenuItem miuiNotificationMenuItem = new MiuiNotificationMenuItem(this, context, C0021R$string.miui_notification_menu_allow, null, C0013R$drawable.miui_notification_menu_ic_allow);
        miuiNotificationMenuItem.setOnClickListener(new View.OnClickListener(context, miuiNotificationMenuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$9dvlRjJ70_U60ELOoscM6qVyYc */
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ MiuiNotificationMenuRow.MiuiNotificationMenuItem f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                MiuiNotificationMenuRow.this.lambda$createAllowItem$2$MiuiNotificationMenuRow(this.f$1, this.f$2, view);
            }
        });
        miuiNotificationMenuItem.setIconBgResId(C0013R$drawable.miui_notification_menu_ic_bg_active);
        return miuiNotificationMenuItem;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createAllowItem$2 */
    public /* synthetic */ void lambda$createAllowItem$2$MiuiNotificationMenuRow(Context context, MiuiNotificationMenuItem miuiNotificationMenuItem, View view) {
        onClickAllowItem(context, miuiNotificationMenuItem);
    }

    private NotificationMenuRowPlugin.MenuItem createInfoItem(Context context) {
        MiuiNotificationMenuItem miuiNotificationMenuItem = new MiuiNotificationMenuItem(this, context, C0021R$string.miui_notification_menu_more_setting, null, C0013R$drawable.miui_notification_menu_ic_more_setting);
        miuiNotificationMenuItem.setOnClickListener(new View.OnClickListener(context, miuiNotificationMenuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$0fC6juA22fUsRGX20G7IW3Y_wlg */
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ MiuiNotificationMenuRow.MiuiNotificationMenuItem f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                MiuiNotificationMenuRow.this.lambda$createInfoItem$3$MiuiNotificationMenuRow(this.f$1, this.f$2, view);
            }
        });
        return miuiNotificationMenuItem;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createInfoItem$3 */
    public /* synthetic */ void lambda$createInfoItem$3$MiuiNotificationMenuRow(Context context, MiuiNotificationMenuItem miuiNotificationMenuItem, View view) {
        onClickInfoItem(context, miuiNotificationMenuItem);
    }

    private NotificationMenuRowPlugin.MenuItem createSnoozeItem(Context context) {
        MiuiNotificationMenuItem miuiNotificationMenuItem = new MiuiNotificationMenuItem(this, context, C0021R$string.miui_notification_menu_snooze, (NotificationSnooze) LayoutInflater.from(context).inflate(C0017R$layout.notification_snooze, (ViewGroup) null, false), C0013R$drawable.miui_notification_menu_ic_snooze);
        miuiNotificationMenuItem.setOnClickListener(new View.OnClickListener(context, miuiNotificationMenuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$0ZT1R7sRKYU0nYykMVHofiCJOGY */
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ MiuiNotificationMenuRow.MiuiNotificationMenuItem f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                MiuiNotificationMenuRow.this.lambda$createSnoozeItem$4$MiuiNotificationMenuRow(this.f$1, this.f$2, view);
            }
        });
        return miuiNotificationMenuItem;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createSnoozeItem$4 */
    public /* synthetic */ void lambda$createSnoozeItem$4$MiuiNotificationMenuRow(Context context, MiuiNotificationMenuItem miuiNotificationMenuItem, View view) {
        onClickSnoozeItem(context, miuiNotificationMenuItem);
    }

    static NotificationMenuRowPlugin.MenuItem createAppOpsItem(Context context) {
        return NotificationMenuRow.createAppOpsItem(context);
    }

    private NotificationMenuRowPlugin.MenuItem createFoldItem(Context context) {
        MiuiNotificationMenuItem miuiNotificationMenuItem = new MiuiNotificationMenuItem(this, context, C0021R$string.miui_notification_menu_fold, null, C0013R$drawable.miui_notification_menu_ic_fold);
        miuiNotificationMenuItem.setOnClickListener(new View.OnClickListener(context, miuiNotificationMenuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$FXCq4Q_oVAyQUWOqVaM8spRfXI */
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ MiuiNotificationMenuRow.MiuiNotificationMenuItem f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                MiuiNotificationMenuRow.this.lambda$createFoldItem$5$MiuiNotificationMenuRow(this.f$1, this.f$2, view);
            }
        });
        return miuiNotificationMenuItem;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createFoldItem$5 */
    public /* synthetic */ void lambda$createFoldItem$5$MiuiNotificationMenuRow(Context context, MiuiNotificationMenuItem miuiNotificationMenuItem, View view) {
        onClickFoldItem(context, miuiNotificationMenuItem);
    }

    private NotificationMenuRowPlugin.MenuItem createAggregateFeedbackItem(Context context) {
        MiuiNotificationMenuItem miuiNotificationMenuItem = new MiuiNotificationMenuItem(this, context, C0021R$string.miui_notification_menu_aggregate_feedback, null, C0013R$drawable.miui_notification_menu_ic_aggregate_feedback);
        miuiNotificationMenuItem.setOnClickListener(new View.OnClickListener(context, miuiNotificationMenuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$6B45gur_tckNfzd3qDaFjVhY08 */
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ MiuiNotificationMenuRow.MiuiNotificationMenuItem f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                MiuiNotificationMenuRow.this.lambda$createAggregateFeedbackItem$6$MiuiNotificationMenuRow(this.f$1, this.f$2, view);
            }
        });
        return miuiNotificationMenuItem;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createAggregateFeedbackItem$6 */
    public /* synthetic */ void lambda$createAggregateFeedbackItem$6$MiuiNotificationMenuRow(Context context, MiuiNotificationMenuItem miuiNotificationMenuItem, View view) {
        onClickFeedbackItem(context, miuiNotificationMenuItem);
    }

    private void onClickAllowItem(Context context, MiuiNotificationMenuItem miuiNotificationMenuItem) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onClickAllowNotification(this.mParent.getEntry());
        showDialog(new AlertDialog.Builder(context, 8).setTitle(C0021R$string.miui_notification_menu_title_not_allow).setMessage(this.mContext.getString(C0021R$string.miui_notification_menu_msg_not_allow, this.mAppName)).setPositiveButton(C0021R$string.confirm, new DialogInterface.OnClickListener(miuiNotificationMenuItem, context) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$1tY6BgfxGMSb4LAvUtuIvcy7rE */
            public final /* synthetic */ MiuiNotificationMenuRow.MiuiNotificationMenuItem f$1;
            public final /* synthetic */ Context f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickAllowItem$7$MiuiNotificationMenuRow(this.f$1, this.f$2, dialogInterface, i);
            }
        }).setNegativeButton(C0021R$string.cancel, new DialogInterface.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$5mTZcaykStGDN4ZIIxrabdDFxjg */

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickAllowItem$8$MiuiNotificationMenuRow(dialogInterface, i);
            }
        }).create());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickAllowItem$7 */
    public /* synthetic */ void lambda$onClickAllowItem$7$MiuiNotificationMenuRow(MiuiNotificationMenuItem miuiNotificationMenuItem, Context context, DialogInterface dialogInterface, int i) {
        miuiNotificationMenuItem.setIconBgResId(C0013R$drawable.miui_notification_menu_ic_bg_inactive);
        saveImportance();
        ((NotificationStat) Dependency.get(NotificationStat.class)).onModalDialogConfirm(this.mParent.getEntry(), ModalDialogSource.DISABLE.name());
        ((ModalController) Dependency.get(ModalController.class)).animExitModal(ModalExitMode.DISABLE.name());
        dialogInterface.dismiss();
        Toast.makeText(context, C0021R$string.miui_notification_menu_setting_success, 0).show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickAllowItem$8 */
    public /* synthetic */ void lambda$onClickAllowItem$8$MiuiNotificationMenuRow(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        ((NotificationStat) Dependency.get(NotificationStat.class)).onModalDialogCancel(this.mParent.getEntry(), ModalDialogSource.DISABLE.name(), ModalDialogExitMode.BUTTON.name());
    }

    private void saveImportance() {
        ExpandedNotification expandedNotification = this.mSbn;
        String targetPackageName = expandedNotification.getTargetPackageName();
        NotificationSettingsHelper.setNotificationsEnabledForPackage(this.mContext, targetPackageName, false);
        Intent intent = new Intent("com.miui.app.ExtraStatusBarManager.action_refresh_notification");
        intent.setPackage("com.android.systemui");
        intent.putExtra("com.miui.app.ExtraStatusBarManager.extra_forbid_notification", true);
        intent.putExtra("app_packageName", targetPackageName);
        String messageId = PushEvents.getMessageId(expandedNotification);
        if (!TextUtils.isEmpty(messageId)) {
            intent.putExtra("messageId", messageId);
        }
        this.mContext.sendBroadcast(intent);
        ((NotificationStat) Dependency.get(NotificationStat.class)).onBlock(this.mParent.getEntry());
    }

    private void onClickFeedbackItem(Context context, NotificationMenuRowPlugin.MenuItem menuItem) {
        showDialog(new AlertDialog.Builder(context, 8).setTitle(C0021R$string.miui_notification_menu_title_aggregate_feedback).setSingleChoiceItems(context.getResources().getStringArray(C0008R$array.miui_notification_aggregate_category), -1, new DialogInterface.OnClickListener(context) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$BH3Ex4FiScCat5bQvxBoEzxIOEo */
            public final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickFeedbackItem$9$MiuiNotificationMenuRow(this.f$1, dialogInterface, i);
            }
        }).setNegativeButton(C0021R$string.cancel, $$Lambda$MiuiNotificationMenuRow$ABN7pkdTwFybOgG1cg8iSJzHS_0.INSTANCE).create());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickFeedbackItem$9 */
    public /* synthetic */ void lambda$onClickFeedbackItem$9$MiuiNotificationMenuRow(Context context, DialogInterface dialogInterface, int i) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onSetConfig(this.mParent.getEntry());
        saveFeedback(context, i == 0 ? "CONTENT" : "AD");
        dialogInterface.dismiss();
        ((ModalController) Dependency.get(ModalController.class)).animExitModal(ModalExitMode.AGGREGATE_FEEDBACK.name());
        Toast.makeText(context, C0021R$string.miui_notification_menu_feedback_success, 0).show();
    }

    private void saveFeedback(Context context, String str) {
        Intent intent = new Intent("com.miui.notification.action.SET_CATEGORY");
        ExpandedNotification expandedNotification = this.mSbn;
        intent.putExtra("key", expandedNotification.getKey());
        intent.putExtra("target_package", expandedNotification.getTargetPackageName());
        intent.putExtra("title", NotificationUtil.resolveTitle(expandedNotification.getNotification()));
        intent.putExtra("text", NotificationUtil.resolveText(expandedNotification.getNotification()));
        intent.putExtra("sub_text", NotificationUtil.resolveSubText(expandedNotification.getNotification()));
        intent.putExtra("category", str);
        intent.setClassName("com.miui.notification", "miui.notification.aggregation.AggregateReceiver");
        context.sendBroadcast(intent);
    }

    private void onClickFoldItem(Context context, NotificationMenuRowPlugin.MenuItem menuItem) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onClickSetUnimportant(this.mParent.getEntry());
        showDialog(new AlertDialog.Builder(context, 8).setTitle(this.mContext.getString(C0021R$string.miui_notification_menu_title_fold)).setMessage(this.mContext.getString(C0021R$string.miui_notification_menu_msg_fold, this.mAppName)).setPositiveButton(C0021R$string.confirm, new DialogInterface.OnClickListener(context) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$iHc7_sC9XNFeRc8xKp9aVUL5Rc */
            public final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickFoldItem$11$MiuiNotificationMenuRow(this.f$1, dialogInterface, i);
            }
        }).setNegativeButton(C0021R$string.cancel, new DialogInterface.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$iTzoAMrc7PHmixdShp8RmUEZnIs */

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickFoldItem$12$MiuiNotificationMenuRow(dialogInterface, i);
            }
        }).create());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickFoldItem$11 */
    public /* synthetic */ void lambda$onClickFoldItem$11$MiuiNotificationMenuRow(Context context, DialogInterface dialogInterface, int i) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onModalDialogConfirm(this.mParent.getEntry(), ModalDialogSource.FOLD.name());
        ((NotificationStat) Dependency.get(NotificationStat.class)).onSetConfig(this.mParent.getEntry());
        saveFold(this.mSbn);
        ((ModalController) Dependency.get(ModalController.class)).animExitModal(ModalExitMode.FOLD.name());
        dialogInterface.dismiss();
        Toast.makeText(context, C0021R$string.miui_notification_menu_setting_success, 0).show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickFoldItem$12 */
    public /* synthetic */ void lambda$onClickFoldItem$12$MiuiNotificationMenuRow(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        ((NotificationStat) Dependency.get(NotificationStat.class)).onModalDialogCancel(this.mParent.getEntry(), ModalDialogSource.FOLD.name(), ModalDialogExitMode.BUTTON.name());
    }

    private void saveFold(ExpandedNotification expandedNotification) {
        String targetPackageName = expandedNotification.getTargetPackageName();
        int currentUserId = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getCurrentUserId();
        NotificationSettingsHelper.setFoldImportance(targetPackageName, -1);
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://statusbar.notification/foldImportance").buildUpon().appendQueryParameter("package", targetPackageName).appendQueryParameter("foldImportance", "-1").build(), null, true, currentUserId);
    }

    private void onClickInfoItem(Context context, NotificationMenuRowPlugin.MenuItem menuItem) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onClickMore(this.mParent.getEntry());
        ExpandedNotification expandedNotification = this.mSbn;
        String packageName = expandedNotification.getPackageName();
        String messageId = PushEvents.getMessageId(expandedNotification);
        PackageManager packageManager = this.mContext.getPackageManager();
        boolean z = false;
        if (NotificationUtil.isHybrid(expandedNotification)) {
            Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES").setPackage(packageName);
            List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent, 0);
            if (queryIntentActivities.size() > 0) {
                ActivityInfo activityInfo = queryIntentActivities.get(0).activityInfo;
                intent.setClassName(activityInfo.packageName, activityInfo.name);
                intent.addFlags(32768);
                intent.addFlags(268435456);
                intent.putExtra("appName", "");
                intent.putExtra("packageName", packageName);
                intent.putExtra("userId", UserHandle.getUserId(expandedNotification.getAppUid()));
                intent.putExtra("messageId", messageId);
                intent.putExtra("notificationId", "");
                intent.putExtra("miui.category", NotificationUtil.getCategory(expandedNotification));
                try {
                    this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    z = true;
                } catch (ActivityNotFoundException e) {
                    Log.e("TAG", "Failed startActivityAsUser() ", e);
                }
            }
        }
        if (!z) {
            NotificationSettingsHelper.startAppNotificationSettings(this.mContext, packageName, this.mAppName, expandedNotification.getAppUid(), messageId);
        }
        ((ModalController) Dependency.get(ModalController.class)).animExitModelCollapsePanels();
    }

    private void onClickSnoozeItem(Context context, NotificationMenuRowPlugin.MenuItem menuItem) {
        ArrayList<NotificationSwipeActionHelper.SnoozeOption> defaultSnoozeOptions = ((NotificationSnooze) menuItem.getGutsView()).getDefaultSnoozeOptions();
        this.mChoiceIndex = 0;
        showDialog(new AlertDialog.Builder(context, 8).setTitle(C0021R$string.miui_notification_menu_snooze_title).setPositiveButton(C0021R$string.confirm, new DialogInterface.OnClickListener(defaultSnoozeOptions) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$oFNGSPvUjkIcoKAUg5aHU0AzM0Y */
            public final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickSnoozeItem$14$MiuiNotificationMenuRow(this.f$1, dialogInterface, i);
            }
        }).setNegativeButton(C0021R$string.cancel, $$Lambda$MiuiNotificationMenuRow$HqidaIYBw0YsGYMT33qct3f_g8.INSTANCE).setSingleChoiceItems((CharSequence[]) defaultSnoozeOptions.stream().map($$Lambda$z3J6tczH2334AIFNVK6MFkhT7v0.INSTANCE).toArray($$Lambda$MiuiNotificationMenuRow$sm4UrPGqndr28nGsxOySExKsjUY.INSTANCE), 0, new DialogInterface.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$xwFpSiUiy41CuHNWEKBGDatmk */

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiNotificationMenuRow.this.lambda$onClickSnoozeItem$16$MiuiNotificationMenuRow(dialogInterface, i);
            }
        }).create());
    }

    static /* synthetic */ CharSequence[] lambda$onClickSnoozeItem$13(int i) {
        return new CharSequence[i];
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickSnoozeItem$14 */
    public /* synthetic */ void lambda$onClickSnoozeItem$14$MiuiNotificationMenuRow(List list, DialogInterface dialogInterface, int i) {
        ModalController modalController = (ModalController) Dependency.get(ModalController.class);
        modalController.getStatusBar().setNotificationSnoozed(this.mSbn, (NotificationSwipeActionHelper.SnoozeOption) list.get(this.mChoiceIndex));
        modalController.animExitModal(ModalExitMode.OTHER.name());
        dialogInterface.dismiss();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onClickSnoozeItem$16 */
    public /* synthetic */ void lambda$onClickSnoozeItem$16$MiuiNotificationMenuRow(DialogInterface dialogInterface, int i) {
        this.mChoiceIndex = i;
    }

    private void showDialog(AlertDialog alertDialog) {
        applyFlags(alertDialog);
        this.mAlertDialog = alertDialog;
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$MiuiNotificationMenuRow$tf2hix3nqO1V2bff9LH5frIbsdk */

            public final void onDismiss(DialogInterface dialogInterface) {
                MiuiNotificationMenuRow.this.lambda$showDialog$17$MiuiNotificationMenuRow(dialogInterface);
            }
        });
        ((ModalController) Dependency.get(ModalController.class)).addOnModalChangeListener(this.mOnModalChangeListener);
        alertDialog.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showDialog$17 */
    public /* synthetic */ void lambda$showDialog$17$MiuiNotificationMenuRow(DialogInterface dialogInterface) {
        ((ModalController) Dependency.get(ModalController.class)).removeOnModalChangeListener(this.mOnModalChangeListener);
        this.mAlertDialog = null;
    }

    private static AlertDialog applyFlags(AlertDialog alertDialog) {
        Window window = alertDialog.getWindow();
        window.setType(2017);
        window.addFlags(655360);
        window.getAttributes().setFitInsetsTypes(window.getAttributes().getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
        return alertDialog;
    }

    public class MiuiNotificationMenuItem extends NotificationMenuRow.NotificationMenuItem {
        private ImageView mIcon;

        public MiuiNotificationMenuItem(MiuiNotificationMenuRow miuiNotificationMenuRow, Context context, int i, NotificationGuts.GutsContent gutsContent, int i2) {
            super(context, "", gutsContent, i2);
            this.mContentDescription = context.getResources().getString(i);
            if (i2 >= 0) {
                View inflate = LayoutInflater.from(context).inflate(C0017R$layout.miui_notification_modal_menu, (ViewGroup) null);
                this.mMenuView = inflate;
                ((TextView) inflate.findViewById(C0015R$id.modal_menu_title)).setText(this.mContentDescription);
                ImageView imageView = (ImageView) this.mMenuView.findViewById(C0015R$id.modal_menu_icon);
                this.mIcon = imageView;
                imageView.setBackgroundResource(C0013R$drawable.miui_notification_menu_ic_bg_inactive);
                this.mIcon.setImageResource(i2);
                this.mIcon.setContentDescription(this.mContentDescription);
                ViewAnimUtils.mouse(this.mIcon);
            }
        }

        public MiuiNotificationMenuItem setIconBgResId(int i) {
            this.mIcon.setBackgroundResource(i);
            return this;
        }

        public MiuiNotificationMenuItem setOnClickListener(View.OnClickListener onClickListener) {
            this.mIcon.setOnClickListener(onClickListener);
            return this;
        }
    }

    private static boolean canBlock(Context context, ExpandedNotification expandedNotification) {
        if (NotificationSettingsHelper.isNonBlockable(context, expandedNotification.getPackageName(), "")) {
            return false;
        }
        return !NotificationUtil.isUidSystem(expandedNotification.getAppUid());
    }

    public static boolean canFold(Context context, ExpandedNotification expandedNotification) {
        return canFoldOrAggregate(context, expandedNotification) && ((SettingsManager) Dependency.get(SettingsManager.class)).getNotifFold();
    }

    public static boolean canAggregate(Context context, ExpandedNotification expandedNotification) {
        return canFoldOrAggregate(context, expandedNotification) && ((SettingsManager) Dependency.get(SettingsManager.class)).getNotifAggregate();
    }

    private static boolean canFoldOrAggregate(Context context, ExpandedNotification expandedNotification) {
        if (expandedNotification == null || !expandedNotification.isClearable() || expandedNotification.getNotification().isGroupSummary() || NotificationUtil.isMediaNotification(expandedNotification) || NotificationUtil.isCustomViewNotification(expandedNotification)) {
            return false;
        }
        return NotificationSettingsHelper.isFoldable(context, expandedNotification.getPackageName());
    }
}
