package com.android.keyguard.negative;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.hardware.ConsumerIrManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.preference.PreferenceManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.SystemUICompat;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBar;
import miui.os.Build;

public class MiuiKeyguardMoveLeftControlCenterView extends MiuiKeyguardMoveLeftBaseView {
    public static final Uri KEYGUARD_SMART_HOME = Uri.parse("content://com.xiaomi.smarthome.ext_cp");
    /* access modifiers changed from: private */
    public LinearLayout mAllFourLinearLayout;
    /* access modifiers changed from: private */
    public ConsumerIrManager mConsumerIrManager = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private float mFontScale;
    /* access modifiers changed from: private */
    public int mFourOrThreeItemTopMargin;
    /* access modifiers changed from: private */
    public boolean mHasIrEmitter;
    /* access modifiers changed from: private */
    public int mItemNums = 0;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onDeviceProvisioned() {
            super.onDeviceProvisioned();
        }

        public void onUserSwitchComplete(int i) {
            MiuiKeyguardMoveLeftControlCenterView.this.initLeftView();
        }
    };
    View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.keyguard_electric_torch /*2131362333*/:
                    boolean z = false;
                    if (Settings.Global.getInt(MiuiKeyguardMoveLeftControlCenterView.this.mContext.getContentResolver(), "torch_state", 0) != 0) {
                        z = true;
                    }
                    MiuiKeyguardMoveLeftControlCenterView.this.mContext.sendBroadcast(PackageUtils.getToggleTorchIntent(!z));
                    MiuiKeyguardMoveLeftControlCenterView.this.mTorchLightImageView.setSelected(!z);
                    if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                        view.performHapticFeedback(268435458);
                        return;
                    }
                    return;
                case R.id.keyguard_lock_screen_magazine_info /*2131362344*/:
                    if (PackageUtils.isAppInstalledForUser(MiuiKeyguardMoveLeftControlCenterView.this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardUpdateMonitor.getCurrentUser())) {
                        Log.d("miui_keyguard", "left view goto lock screen wall paper");
                        MiuiKeyguardMoveLeftControlCenterView.this.setPreviewButtonClicked();
                        LockScreenMagazineUtils.gotoLockScreenMagazine(MiuiKeyguardMoveLeftControlCenterView.this.mContext, "leftLockScreen");
                        return;
                    }
                    MiuiKeyguardMoveLeftControlCenterView.this.startAppStoreToDownload(R.id.keyguard_lock_screen_magazine_info);
                    return;
                case R.id.keyguard_mi_wallet_info /*2131362346*/:
                    MiuiKeyguardMoveLeftControlCenterView.this.startToTSMClientActivity();
                    return;
                case R.id.keyguard_remote_controller_info /*2131362351*/:
                    if (PackageUtils.isAppInstalledForUser(MiuiKeyguardMoveLeftControlCenterView.this.mContext, "com.duokan.phone.remotecontroller", KeyguardUpdateMonitor.getCurrentUser())) {
                        Intent launchIntentForPackage = MiuiKeyguardMoveLeftControlCenterView.this.mContext.getPackageManager().getLaunchIntentForPackage("com.duokan.phone.remotecontroller");
                        launchIntentForPackage.addFlags(268435456);
                        MiuiKeyguardMoveLeftControlCenterView.this.mStatusBar.startActivity(launchIntentForPackage, true);
                        return;
                    }
                    MiuiKeyguardMoveLeftControlCenterView.this.startAppStoreToDownload(R.id.keyguard_remote_controller_info);
                    return;
                case R.id.keyguard_smarthome_info /*2131362358*/:
                    if (PackageUtils.isAppInstalledForUser(MiuiKeyguardMoveLeftControlCenterView.this.mContext, "com.xiaomi.smarthome", KeyguardUpdateMonitor.getCurrentUser())) {
                        try {
                            MiuiKeyguardMoveLeftControlCenterView.this.mStatusBar.startActivity(PackageUtils.getSmartHomeMainIntent(), true);
                            return;
                        } catch (Exception unused) {
                        }
                    } else {
                        MiuiKeyguardMoveLeftControlCenterView.this.startAppStoreToDownload(R.id.keyguard_smarthome_info);
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private Object mLocaleList;
    /* access modifiers changed from: private */
    public LinearLayout mLockScreenMagazineLinearLayout;
    /* access modifiers changed from: private */
    public LinearLayout mMiWalletLinearLayout;
    private NotificationPanelView mPanel;
    /* access modifiers changed from: private */
    public LinearLayout mRemoteCenterLinearLayout;
    /* access modifiers changed from: private */
    public LinearLayout mSmartHomeLinearLayout;
    /* access modifiers changed from: private */
    public boolean mSupportTSMClient;
    /* access modifiers changed from: private */
    public ImageView mTorchLightImageView;
    private ContentObserver mTorchStateReceiver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            boolean z2 = false;
            if (Settings.Global.getInt(MiuiKeyguardMoveLeftControlCenterView.this.mContext.getContentResolver(), "torch_state", 0) != 0) {
                z2 = true;
            }
            MiuiKeyguardMoveLeftControlCenterView.this.mTorchLightImageView.setSelected(z2);
        }
    };
    /* access modifiers changed from: private */
    public int mTwoOrOneItemLeftMargin;
    /* access modifiers changed from: private */
    public int mTwoOrOneItemRightMargin;
    /* access modifiers changed from: private */
    public int mTwoOrOneItemTopMargin;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public boolean hasBackgroundImageDrawable() {
        return false;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void uploadData() {
    }

    static {
        Uri.parse("content://com.xiaomi.mitv.phone.remotecontroller.provider.LockScreenProvider");
        Uri.parse("content://com.miui.tsmclient.provider.public");
    }

    public MiuiKeyguardMoveLeftControlCenterView(Context context) {
        super(context);
        this.mContext = context;
    }

    public MiuiKeyguardMoveLeftControlCenterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
    }

    public void setPanel(NotificationPanelView notificationPanelView) {
        this.mPanel = notificationPanelView;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mSmartHomeLinearLayout = (LinearLayout) findViewById(R.id.keyguard_smarthome_info);
        this.mRemoteCenterLinearLayout = (LinearLayout) findViewById(R.id.keyguard_remote_controller_info);
        this.mMiWalletLinearLayout = (LinearLayout) findViewById(R.id.keyguard_mi_wallet_info);
        this.mLockScreenMagazineLinearLayout = (LinearLayout) findViewById(R.id.keyguard_lock_screen_magazine_info);
        this.mTorchLightImageView = (ImageView) findViewById(R.id.keyguard_electric_torch);
        this.mAllFourLinearLayout = (LinearLayout) findViewById(R.id.keyguard_move_left);
        this.mSmartHomeLinearLayout.setOnClickListener(this.mListener);
        this.mRemoteCenterLinearLayout.setOnClickListener(this.mListener);
        this.mMiWalletLinearLayout.setOnClickListener(this.mListener);
        this.mLockScreenMagazineLinearLayout.setOnClickListener(this.mListener);
        this.mTorchLightImageView.setOnClickListener(this.mListener);
        this.mTwoOrOneItemTopMargin = getResources().getDimensionPixelSize(R.dimen.keyguard_move_left_layout_top_margint_twoorone);
        this.mTwoOrOneItemLeftMargin = getResources().getDimensionPixelSize(R.dimen.keyguard_move_left_layout_left_margint_twoorone);
        this.mTwoOrOneItemRightMargin = getResources().getDimensionPixelSize(R.dimen.keyguard_move_left_layout_right_margint_twoorone);
        this.mFourOrThreeItemTopMargin = getResources().getDimensionPixelOffset(R.dimen.keyguard_move_left_layout_top_margint_fourorthree);
        initKeyguardLeftItemInfos();
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("torch_state"), false, this.mTorchStateReceiver);
        this.mTorchStateReceiver.onChange(false);
        initLeftView();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        StatusBar statusBar;
        if (motionEvent.getAction() == 0 && (statusBar = this.mStatusBar) != null) {
            statusBar.userActivity();
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private void initKeyguardLeftItemInfos() {
        initKeyguardLeftItemInfo(R.id.keyguard_smarthome_info, R.drawable.keyguard_left_view_smarthome, R.string.keyguard_left_smarthome);
        initKeyguardLeftItemInfo(R.id.keyguard_remote_controller_info, R.drawable.keyguard_left_view_remotecontroller, R.string.keyguard_left_remotecentral);
        initKeyguardLeftItemInfo(R.id.keyguard_mi_wallet_info, R.drawable.keyguard_left_view_bankcard, R.string.keyguard_left_mi_wallet);
        initKeyguardLeftItemInfo(R.id.keyguard_lock_screen_magazine_info, R.drawable.keyguard_left_view_magazine, R.string.keyguard_left_view_lock_wallpaper);
    }

    public void initLeftView() {
        new AsyncTask<Void, Void, Boolean>() {
            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                if (MiuiKeyguardMoveLeftControlCenterView.this.mConsumerIrManager == null) {
                    MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView = MiuiKeyguardMoveLeftControlCenterView.this;
                    ConsumerIrManager unused = miuiKeyguardMoveLeftControlCenterView.mConsumerIrManager = (ConsumerIrManager) miuiKeyguardMoveLeftControlCenterView.mContext.getSystemService("consumer_ir");
                }
                if (MiuiKeyguardMoveLeftControlCenterView.this.mConsumerIrManager != null) {
                    MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView2 = MiuiKeyguardMoveLeftControlCenterView.this;
                    boolean unused2 = miuiKeyguardMoveLeftControlCenterView2.mHasIrEmitter = miuiKeyguardMoveLeftControlCenterView2.mConsumerIrManager.hasIrEmitter();
                }
                MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView3 = MiuiKeyguardMoveLeftControlCenterView.this;
                boolean unused3 = miuiKeyguardMoveLeftControlCenterView3.mSupportTSMClient = PackageUtils.supportTSMClient(miuiKeyguardMoveLeftControlCenterView3.mContext);
                return Boolean.TRUE;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                int unused = MiuiKeyguardMoveLeftControlCenterView.this.mItemNums = 0;
                MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView = MiuiKeyguardMoveLeftControlCenterView.this;
                miuiKeyguardMoveLeftControlCenterView.updateItemVisibility(miuiKeyguardMoveLeftControlCenterView.mHasIrEmitter, MiuiKeyguardMoveLeftControlCenterView.this.mRemoteCenterLinearLayout);
                MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView2 = MiuiKeyguardMoveLeftControlCenterView.this;
                miuiKeyguardMoveLeftControlCenterView2.updateItemVisibility(miuiKeyguardMoveLeftControlCenterView2.mSupportTSMClient, MiuiKeyguardMoveLeftControlCenterView.this.mMiWalletLinearLayout);
                MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView3 = MiuiKeyguardMoveLeftControlCenterView.this;
                miuiKeyguardMoveLeftControlCenterView3.updateItemVisibility(MiuiKeyguardUtils.isRegionSupportMiHome(miuiKeyguardMoveLeftControlCenterView3.mContext), MiuiKeyguardMoveLeftControlCenterView.this.mSmartHomeLinearLayout);
                MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView4 = MiuiKeyguardMoveLeftControlCenterView.this;
                miuiKeyguardMoveLeftControlCenterView4.updateItemVisibility(miuiKeyguardMoveLeftControlCenterView4.supportLockScreenMagazine(), MiuiKeyguardMoveLeftControlCenterView.this.mLockScreenMagazineLinearLayout);
                if (MiuiKeyguardMoveLeftControlCenterView.this.mAllFourLinearLayout != null) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) MiuiKeyguardMoveLeftControlCenterView.this.mAllFourLinearLayout.getLayoutParams();
                    layoutParams.setMargins(MiuiKeyguardMoveLeftControlCenterView.this.mTwoOrOneItemLeftMargin, MiuiKeyguardMoveLeftControlCenterView.this.mItemNums <= 2 ? MiuiKeyguardMoveLeftControlCenterView.this.mTwoOrOneItemTopMargin : MiuiKeyguardMoveLeftControlCenterView.this.mFourOrThreeItemTopMargin, MiuiKeyguardMoveLeftControlCenterView.this.mTwoOrOneItemRightMargin, 0);
                    MiuiKeyguardMoveLeftControlCenterView.this.mAllFourLinearLayout.setLayoutParams(layoutParams);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* access modifiers changed from: private */
    public void updateItemVisibility(boolean z, View view) {
        if (view == null) {
            return;
        }
        if (z) {
            view.setVisibility(0);
            this.mItemNums++;
            return;
        }
        view.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Object locales = SystemUICompat.getLocales(configuration);
        float f = configuration.fontScale;
        if (this.mLocaleList != locales) {
            initKeyguardLeftItemInfos();
            this.mLocaleList = locales;
        }
        if (this.mFontScale != f) {
            initKeyguardLeftItemInfos();
            this.mFontScale = f;
        }
    }

    private void initKeyguardLeftItemInfo(int i, int i2, int i3) {
        View findViewById = findViewById(i);
        ((ImageView) findViewById.findViewById(R.id.keyguard_left_list_item_img)).setBackgroundResource(i2);
        TextView textView = (TextView) findViewById.findViewById(R.id.keyguard_left_list_item_name);
        textView.setText(i3);
        updateItemInfoTextSize(textView);
    }

    private void updateItemInfoTextSize(TextView textView) {
        Resources resources = getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.keyguard_move_left_litem_textview_name_size);
        resources.getDimensionPixelSize(R.dimen.keyguard_move_left_litem_textview_num_size);
        textView.setTextSize(0, (float) dimensionPixelSize);
    }

    /* access modifiers changed from: private */
    public void startAppStoreToDownload(int i) {
        String str = "";
        if (i == R.id.keyguard_smarthome_info) {
            str = "com.xiaomi.smarthome";
        } else if (i == R.id.keyguard_remote_controller_info) {
            str = "com.duokan.phone.remotecontroller";
        } else if (i == R.id.keyguard_lock_screen_magazine_info) {
            try {
                str = LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME;
            } catch (Exception e) {
                Log.e("MiuiKeyguardMoveLeftBaseView", "startAppStoreToDownload", e);
                return;
            }
        }
        this.mStatusBar.startActivity(PackageUtils.getMarketDownloadIntent(str), true);
    }

    /* access modifiers changed from: private */
    public void startToTSMClientActivity() {
        try {
            this.mContext.startActivityAsUser(PackageUtils.getTSMClientIntent(), UserHandle.CURRENT);
        } catch (Exception unused) {
        }
    }

    public boolean isSupportRightMove() {
        return MiuiKeyguardUtils.isRegionSupportMiHome(this.mContext) || this.mHasIrEmitter || this.mSupportTSMClient || supportLockScreenMagazine();
    }

    /* access modifiers changed from: private */
    public void setPreviewButtonClicked() {
        PreferenceManager.getDefaultSharedPreferences(this.mContext).edit().putBoolean("prfe_key_preview_button_clicked", true).commit();
    }

    /* access modifiers changed from: private */
    public boolean supportLockScreenMagazine() {
        return !Build.IS_INTERNATIONAL_BUILD || MiuiKeyguardUtils.isIndianRegion(this.mContext);
    }

    public void setCustomBackground() {
        setBackgroundDrawable((Drawable) null);
        this.mPanel.getLeftViewBg().setBackgroundColor(this.mUpdateMonitor.getWallpaperBlurColor());
    }
}
