package com.android.keyguard.negative;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.ConsumerIrManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.LocaleList;
import android.os.UserHandle;
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
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.miui.systemui.util.CommonExtensionsKt;
import com.miui.systemui.util.CommonUtil;
import miui.os.Build;

public class MiuiKeyguardMoveLeftControlCenterView extends MiuiKeyguardMoveLeftBaseView implements FlashlightController.FlashlightListener {
    public static final Uri KEYGUARD_SMART_HOME = Uri.parse("content://com.xiaomi.smarthome.ext_cp");
    private LinearLayout mAllFourLinearLayout;
    private ConsumerIrManager mConsumerIrManager = null;
    private Context mContext;
    private FlashlightController mFlashlightController = ((FlashlightController) Dependency.get(FlashlightController.class));
    private float mFontScale;
    private int mFourOrThreeItemTopMargin;
    private boolean mHasIrEmitter;
    private boolean mIsForceDisableMagazine = false;
    private int mItemNums = 0;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDeviceProvisioned() {
            super.onDeviceProvisioned();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            MiuiKeyguardMoveLeftControlCenterView.this.initLeftView();
        }
    };
    View.OnClickListener mListener = new View.OnClickListener() {
        /* class com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView.AnonymousClass3 */

        public void onClick(View view) {
            int id = view.getId();
            if (id == C0015R$id.keyguard_smarthome_info) {
                if (PackageUtils.isAppInstalledForUser(MiuiKeyguardMoveLeftControlCenterView.this.mContext, "com.xiaomi.smarthome", KeyguardUpdateMonitor.getCurrentUser())) {
                    try {
                        MiuiKeyguardMoveLeftControlCenterView.this.mStatusBar.startActivity(PackageUtils.getSmartHomeMainIntent(), true);
                    } catch (Exception e) {
                        Log.e("MiuiKeyguardMoveLeftBaseView", "StatusBar.startActivity fail " + e.getCause());
                    }
                } else {
                    MiuiKeyguardMoveLeftControlCenterView.this.startAppStoreToDownload(C0015R$id.keyguard_smarthome_info);
                }
            } else if (id == C0015R$id.keyguard_remote_controller_info) {
                if (PackageUtils.isAppInstalledForUser(MiuiKeyguardMoveLeftControlCenterView.this.mContext, "com.duokan.phone.remotecontroller", KeyguardUpdateMonitor.getCurrentUser())) {
                    try {
                        Intent launchIntentForPackage = MiuiKeyguardMoveLeftControlCenterView.this.mContext.getPackageManager().getLaunchIntentForPackage("com.duokan.phone.remotecontroller");
                        launchIntentForPackage.addFlags(268435456);
                        MiuiKeyguardMoveLeftControlCenterView.this.mStatusBar.startActivity(launchIntentForPackage, true);
                    } catch (Exception e2) {
                        Log.e("MiuiKeyguardMoveLeftBaseView", "StatusBar.startActivity fail " + e2.getCause());
                    }
                } else {
                    MiuiKeyguardMoveLeftControlCenterView.this.startAppStoreToDownload(C0015R$id.keyguard_remote_controller_info);
                }
            } else if (id == C0015R$id.keyguard_mi_wallet_info) {
                MiuiKeyguardMoveLeftControlCenterView.this.startToTSMClientActivity();
            } else if (id == C0015R$id.keyguard_electric_torch) {
                MiuiKeyguardMoveLeftControlCenterView.this.mTorchLightImageView.setSelected(!MiuiKeyguardMoveLeftControlCenterView.this.mFlashlightController.isEnabled());
                CommonUtil.toggleTorch();
                if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                    view.performHapticFeedback(268435458);
                }
            } else if (id != C0015R$id.keyguard_lock_screen_magazine_info) {
            } else {
                if (PackageUtils.isAppInstalledForUser(MiuiKeyguardMoveLeftControlCenterView.this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardUpdateMonitor.getCurrentUser())) {
                    Log.d("miui_keyguard", "left view goto lock screen wall paper");
                    MiuiKeyguardMoveLeftControlCenterView.this.setPreviewButtonClicked();
                    if (CommonExtensionsKt.checkFastDoubleClick(view, 500)) {
                        LockScreenMagazineUtils.gotoMagazine(MiuiKeyguardMoveLeftControlCenterView.this.mContext, "leftLockScreen");
                        return;
                    }
                    return;
                }
                MiuiKeyguardMoveLeftControlCenterView.this.startAppStoreToDownload(C0015R$id.keyguard_lock_screen_magazine_info);
            }
        }
    };
    private Object mLocaleList;
    private LinearLayout mLockScreenMagazineLinearLayout;
    private LinearLayout mMiWalletLinearLayout;
    private LinearLayout mRemoteCenterLinearLayout;
    private LinearLayout mSmartHomeLinearLayout;
    private boolean mSupportTSMClient;
    private ImageView mTorchLightImageView;
    private int mTwoOrOneItemLeftMargin;
    private int mTwoOrOneItemRightMargin;
    private int mTwoOrOneItemTopMargin;

    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
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
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSmartHomeLinearLayout = (LinearLayout) findViewById(C0015R$id.keyguard_smarthome_info);
        this.mRemoteCenterLinearLayout = (LinearLayout) findViewById(C0015R$id.keyguard_remote_controller_info);
        this.mMiWalletLinearLayout = (LinearLayout) findViewById(C0015R$id.keyguard_mi_wallet_info);
        this.mLockScreenMagazineLinearLayout = (LinearLayout) findViewById(C0015R$id.keyguard_lock_screen_magazine_info);
        this.mTorchLightImageView = (ImageView) findViewById(C0015R$id.keyguard_electric_torch);
        this.mAllFourLinearLayout = (LinearLayout) findViewById(C0015R$id.keyguard_move_left);
        this.mSmartHomeLinearLayout.setOnClickListener(this.mListener);
        this.mRemoteCenterLinearLayout.setOnClickListener(this.mListener);
        this.mMiWalletLinearLayout.setOnClickListener(this.mListener);
        this.mLockScreenMagazineLinearLayout.setOnClickListener(this.mListener);
        this.mTorchLightImageView.setOnClickListener(this.mListener);
        this.mTwoOrOneItemTopMargin = getResources().getDimensionPixelSize(C0012R$dimen.keyguard_move_left_layout_top_margint_twoorone);
        this.mTwoOrOneItemLeftMargin = getResources().getDimensionPixelSize(C0012R$dimen.keyguard_move_left_layout_left_margint_twoorone);
        this.mTwoOrOneItemRightMargin = getResources().getDimensionPixelSize(C0012R$dimen.keyguard_move_left_layout_right_margint_twoorone);
        this.mFourOrThreeItemTopMargin = getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_move_left_layout_top_margint_fourorthree);
        this.mIsForceDisableMagazine = getResources().getBoolean(C0010R$bool.config_disableLockScreenMagazine);
        initKeyguardLeftItemInfos();
        initLeftView();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mFlashlightController.addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mKeyguardUpdateMonitorCallback);
        this.mFlashlightController.removeCallback(this);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        StatusBar statusBar;
        if (motionEvent.getAction() == 0 && (statusBar = this.mStatusBar) != null) {
            statusBar.userActivity();
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private void initKeyguardLeftItemInfos() {
        initKeyguardLeftItemInfo(C0015R$id.keyguard_smarthome_info, C0013R$drawable.keyguard_left_view_smarthome, C0021R$string.keyguard_left_smarthome);
        initKeyguardLeftItemInfo(C0015R$id.keyguard_remote_controller_info, C0013R$drawable.keyguard_left_view_remotecontroller, C0021R$string.keyguard_left_remotecentral);
        initKeyguardLeftItemInfo(C0015R$id.keyguard_mi_wallet_info, C0013R$drawable.keyguard_left_view_bankcard, C0021R$string.keyguard_left_mi_wallet);
        initKeyguardLeftItemInfo(C0015R$id.keyguard_lock_screen_magazine_info, C0013R$drawable.keyguard_left_view_magazine, C0021R$string.keyguard_left_view_lock_wallpaper);
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void initLeftView() {
        this.mTorchLightImageView.setSelected(this.mFlashlightController.isEnabled());
        new AsyncTask<Void, Void, Boolean>() {
            /* class com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView.AnonymousClass2 */

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                if (MiuiKeyguardMoveLeftControlCenterView.this.mConsumerIrManager == null) {
                    MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView = MiuiKeyguardMoveLeftControlCenterView.this;
                    miuiKeyguardMoveLeftControlCenterView.mConsumerIrManager = (ConsumerIrManager) miuiKeyguardMoveLeftControlCenterView.mContext.getSystemService("consumer_ir");
                }
                if (MiuiKeyguardMoveLeftControlCenterView.this.mConsumerIrManager != null) {
                    MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView2 = MiuiKeyguardMoveLeftControlCenterView.this;
                    miuiKeyguardMoveLeftControlCenterView2.mHasIrEmitter = miuiKeyguardMoveLeftControlCenterView2.mConsumerIrManager.hasIrEmitter();
                }
                MiuiKeyguardMoveLeftControlCenterView miuiKeyguardMoveLeftControlCenterView3 = MiuiKeyguardMoveLeftControlCenterView.this;
                miuiKeyguardMoveLeftControlCenterView3.mSupportTSMClient = PackageUtils.supportTSMClient(miuiKeyguardMoveLeftControlCenterView3.mContext);
                return Boolean.TRUE;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                MiuiKeyguardMoveLeftControlCenterView.this.mItemNums = 0;
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
    /* access modifiers changed from: public */
    private void updateItemVisibility(boolean z, View view) {
        Log.d("MiuiKeyguardMoveLeftBaseView", "show:" + z + " item:" + view);
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
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        LocaleList locales = configuration.getLocales();
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
        ((ImageView) findViewById.findViewById(C0015R$id.keyguard_left_list_item_img)).setBackgroundResource(i2);
        TextView textView = (TextView) findViewById.findViewById(C0015R$id.keyguard_left_list_item_name);
        textView.setText(i3);
        updateItemInfoTextSize(textView);
    }

    private void updateItemInfoTextSize(TextView textView) {
        Resources resources = getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.keyguard_move_left_litem_textview_name_size);
        resources.getDimensionPixelSize(C0012R$dimen.keyguard_move_left_litem_textview_num_size);
        textView.setTextSize(0, (float) dimensionPixelSize);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAppStoreToDownload(int i) {
        String str = "";
        try {
            if (i == C0015R$id.keyguard_smarthome_info) {
                str = "com.xiaomi.smarthome";
            } else if (i == C0015R$id.keyguard_remote_controller_info) {
                str = "com.duokan.phone.remotecontroller";
            } else if (i == C0015R$id.keyguard_lock_screen_magazine_info) {
                str = LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME;
            }
            this.mStatusBar.startActivity(PackageUtils.getMarketDownloadIntent(str), true);
        } catch (Exception e) {
            Log.e("MiuiKeyguardMoveLeftBaseView", "startAppStoreToDownload", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startToTSMClientActivity() {
        try {
            this.mContext.startActivityAsUser(PackageUtils.getTSMClientIntent(), UserHandle.CURRENT);
        } catch (Exception unused) {
        }
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public boolean isSupportRightMove() {
        return MiuiKeyguardUtils.isRegionSupportMiHome(this.mContext) || this.mHasIrEmitter || this.mSupportTSMClient || supportLockScreenMagazine();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPreviewButtonClicked() {
        PreferenceManager.getDefaultSharedPreferences(this.mContext).edit().putBoolean("prfe_key_preview_button_clicked", true).commit();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean supportLockScreenMagazine() {
        if (this.mIsForceDisableMagazine) {
            return false;
        }
        if (!Build.IS_INTERNATIONAL_BUILD || MiuiKeyguardUtils.isIndianRegion()) {
            return true;
        }
        return false;
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void setCustomBackground(Drawable drawable) {
        setBackground(drawable);
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightChanged(boolean z) {
        this.mTorchLightImageView.setSelected(z);
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightError() {
        this.mTorchLightImageView.setSelected(false);
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightAvailabilityChanged(boolean z) {
        this.mTorchLightImageView.setSelected(false);
    }
}
