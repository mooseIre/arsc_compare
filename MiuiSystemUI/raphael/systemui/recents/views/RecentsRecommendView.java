package com.android.systemui.recents.views;

import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.Utilities;

public class RecentsRecommendView extends LinearLayout implements View.OnClickListener {
    private static String ACTION_APP_MANAGER = "miui.intent.action.APP_MANAGER";
    private static String ACTION_GARBAGE_CLEANUP = "miui.intent.action.GARBAGE_CLEANUP";
    private static String ACTION_GARBAGE_DEEPCLEAN = "miui.intent.action.GARBAGE_DEEPCLEAN";
    private static String ACTION_SECURITY_SACN = "miui.intent.action.ANTI_VIRUS";
    private static String DATA_MARKET_REF = "recents";
    private static String EXTRA_ENTER_HOMEPAGE_WAY = "enter_homepage_way";
    private static String EXTRA_ENTER_WAY = "enter_way";
    private static String EXTRA_VALUE_CHANNEL = "recent_task";
    private static String GLOBAL_GAME_PACKAGE_NAME = "com.xiaomi.glgm";
    private static String GLOBAL_MARKET_PACKAGE_NAME = "com.xiaomi.mipicks";
    private static String TAG = "RecentsRecommendView";
    private LinearLayout mFirstItem;
    private LinearLayout mFourthItem;
    private boolean mIsGamesEnable;
    private boolean mIsMarketEnabled;
    private LinearLayout mSecondItem;
    private LinearLayout mThirdItem;

    public RecentsRecommendView(Context context) {
        this(context, (AttributeSet) null);
    }

    public RecentsRecommendView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentsRecommendView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentsRecommendView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIsMarketEnabled = Utilities.isPackageEnabled(this.mContext, GLOBAL_MARKET_PACKAGE_NAME);
        this.mIsGamesEnable = Utilities.isPackageEnabled(this.mContext, GLOBAL_GAME_PACKAGE_NAME);
        this.mFirstItem = initItem(R.id.first_item, R.drawable.recents_icon_garbage_cleanup, R.string.recents_title_garbage_cleanup);
        this.mSecondItem = initItem(R.id.second_item, R.drawable.recents_icon_security_scan, R.string.recents_title_security_scan);
        this.mThirdItem = initItem(R.id.third_item, this.mIsMarketEnabled ? R.drawable.recents_icon_market : R.drawable.recents_icon_garbage_deepclean, this.mIsMarketEnabled ? R.string.recents_title_market : R.string.recents_title_garbage_deepclean);
        this.mFourthItem = initItem(R.id.fourth_item, this.mIsGamesEnable ? R.drawable.recents_icon_game : R.drawable.recents_icon_app_manager, this.mIsGamesEnable ? R.string.recents_title_game : R.string.recents_title_app_manager);
    }

    private LinearLayout initItem(int i, int i2, int i3) {
        LinearLayout linearLayout = (LinearLayout) findViewById(i);
        ((ImageView) linearLayout.findViewById(R.id.item_icon)).setImageDrawable(getResources().getDrawable(i2));
        ((TextView) linearLayout.findViewById(R.id.item_title)).setText(i3);
        linearLayout.setOnClickListener(this);
        return linearLayout;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        RecentsEventBus.getDefault().register(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecentsEventBus.getDefault().unregister(this);
    }

    public final void onBusEvent(PackagesChangedEvent packagesChangedEvent) {
        boolean isPackageEnabled;
        boolean isPackageEnabled2;
        if (GLOBAL_MARKET_PACKAGE_NAME.equals(packagesChangedEvent.packageName) && this.mIsMarketEnabled != (isPackageEnabled2 = Utilities.isPackageEnabled(this.mContext, GLOBAL_MARKET_PACKAGE_NAME))) {
            this.mIsMarketEnabled = isPackageEnabled2;
            this.mThirdItem = initItem(R.id.third_item, this.mIsMarketEnabled ? R.drawable.recents_icon_market : R.drawable.recents_icon_garbage_deepclean, this.mIsMarketEnabled ? R.string.recents_title_market : R.string.recents_title_garbage_deepclean);
        }
        if (GLOBAL_GAME_PACKAGE_NAME.equals(packagesChangedEvent.packageName) && this.mIsGamesEnable != (isPackageEnabled = Utilities.isPackageEnabled(this.mContext, GLOBAL_GAME_PACKAGE_NAME))) {
            this.mIsGamesEnable = isPackageEnabled;
            this.mFourthItem = initItem(R.id.fourth_item, this.mIsGamesEnable ? R.drawable.recents_icon_game : R.drawable.recents_icon_app_manager, this.mIsGamesEnable ? R.string.recents_title_game : R.string.recents_title_app_manager);
        }
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setFlags(268435456);
        switch (view.getId()) {
            case R.id.first_item:
                intent.setAction(ACTION_GARBAGE_CLEANUP);
                intent.putExtra(EXTRA_ENTER_HOMEPAGE_WAY, EXTRA_VALUE_CHANNEL);
                RecentsPushEventHelper.sendClickRecommendCardEvent("cleaner");
                break;
            case R.id.fourth_item:
                if (!this.mIsGamesEnable) {
                    intent.setAction(ACTION_APP_MANAGER);
                    intent.putExtra(EXTRA_ENTER_WAY, EXTRA_VALUE_CHANNEL);
                    RecentsPushEventHelper.sendClickRecommendCardEvent("manager_app");
                    break;
                } else {
                    intent.setComponent(new ComponentName("com.xiaomi.glgm", "com.xiaomi.glgm.home.ui.SplashActivity"));
                    RecentsPushEventHelper.sendClickRecommendCardEvent("game");
                    break;
                }
            case R.id.second_item:
                intent.setAction(ACTION_SECURITY_SACN);
                intent.putExtra(EXTRA_ENTER_HOMEPAGE_WAY, EXTRA_VALUE_CHANNEL);
                RecentsPushEventHelper.sendClickRecommendCardEvent("scan");
                break;
            case R.id.third_item:
                if (!this.mIsMarketEnabled) {
                    intent.setAction(ACTION_GARBAGE_DEEPCLEAN);
                    intent.putExtra(EXTRA_ENTER_HOMEPAGE_WAY, EXTRA_VALUE_CHANNEL);
                    RecentsPushEventHelper.sendClickRecommendCardEvent("deep_clean");
                    break;
                } else {
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse("mimarket://home?ref=" + DATA_MARKET_REF));
                    RecentsPushEventHelper.sendClickRecommendCardEvent("market");
                    break;
                }
        }
        try {
            TaskStackBuilder.create(this.mContext.getApplicationContext()).addNextIntentWithParentStack(intent).startActivities((Bundle) null, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "start activity error.", e);
        }
    }

    public void setAllItemClickable(boolean z) {
        LinearLayout linearLayout = this.mFirstItem;
        if (linearLayout != null) {
            linearLayout.setClickable(z);
        }
        LinearLayout linearLayout2 = this.mSecondItem;
        if (linearLayout2 != null) {
            linearLayout2.setClickable(z);
        }
        LinearLayout linearLayout3 = this.mThirdItem;
        if (linearLayout3 != null) {
            linearLayout3.setClickable(z);
        }
        LinearLayout linearLayout4 = this.mFourthItem;
        if (linearLayout4 != null) {
            linearLayout4.setClickable(z);
        }
    }
}
