package com.android.systemui.volume;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.VolumePolicy;
import android.os.Bundle;
import com.android.settingslib.applications.InterestingConfigChanges;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeDialogComponent implements VolumeComponent, TunerService.Tunable, VolumeDialogControllerImpl.UserActivityListener {
    private final InterestingConfigChanges mConfigChanges = new InterestingConfigChanges(-1073741308);
    protected final Context mContext;
    private final VolumeDialogControllerImpl mController;
    private VolumeDialog mDialog;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private final VolumeDialog.Callback mVolumeDialogCallback = new VolumeDialog.Callback() {
        /* class com.android.systemui.volume.VolumeDialogComponent.AnonymousClass1 */

        @Override // com.android.systemui.plugins.VolumeDialog.Callback
        public void onZenSettingsClicked() {
            VolumeDialogComponent.this.startSettings(ZenModePanel.ZEN_SETTINGS);
        }

        @Override // com.android.systemui.plugins.VolumeDialog.Callback
        public void onZenPrioritySettingsClicked() {
            VolumeDialogComponent.this.startSettings(ZenModePanel.ZEN_PRIORITY_SETTINGS);
        }
    };
    private VolumePolicy mVolumePolicy = new VolumePolicy(false, false, false, 400);

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public VolumeDialogComponent(Context context, KeyguardViewMediator keyguardViewMediator, VolumeDialogControllerImpl volumeDialogControllerImpl) {
        this.mContext = context;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mController = volumeDialogControllerImpl;
        volumeDialogControllerImpl.setUserActivityListener(this);
        ((PluginDependencyProvider) Dependency.get(PluginDependencyProvider.class)).allowPluginDependency(VolumeDialogController.class);
        ExtensionController.ExtensionBuilder newExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(VolumeDialog.class);
        newExtension.withPlugin(VolumeDialog.class);
        newExtension.withDefault(new Supplier() {
            /* class com.android.systemui.volume.$$Lambda$5eQ6FmuY0CORdNfZebXQAtrsfI4 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return VolumeDialogComponent.this.createDefault();
            }
        });
        newExtension.withCallback(new Consumer() {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogComponent$vZvGMkdhFGTZ9hLE1BnozIW6Wb0 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                VolumeDialogComponent.this.lambda$new$0$VolumeDialogComponent((VolumeDialog) obj);
            }
        });
        newExtension.build();
        applyConfiguration();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_volume_down_silent", "sysui_volume_up_silent", "sysui_do_not_disturb");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$VolumeDialogComponent(VolumeDialog volumeDialog) {
        VolumeDialog volumeDialog2 = this.mDialog;
        if (volumeDialog2 != null) {
            volumeDialog2.destroy();
        }
        this.mDialog = volumeDialog;
        volumeDialog.init(2020, this.mVolumeDialogCallback);
    }

    /* access modifiers changed from: protected */
    public VolumeDialog createDefault() {
        VolumeDialogImpl volumeDialogImpl = new VolumeDialogImpl(this.mContext);
        volumeDialogImpl.setStreamImportant(1, false);
        volumeDialogImpl.setAutomute(true);
        volumeDialogImpl.setSilentMode(false);
        return volumeDialogImpl;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        VolumePolicy volumePolicy = this.mVolumePolicy;
        boolean z = volumePolicy.volumeDownToEnterSilent;
        boolean z2 = volumePolicy.volumeUpToExitSilent;
        boolean z3 = volumePolicy.doNotDisturbWhenSilent;
        if ("sysui_volume_down_silent".equals(str)) {
            z = TunerService.parseIntegerSwitch(str2, false);
        } else if ("sysui_volume_up_silent".equals(str)) {
            z2 = TunerService.parseIntegerSwitch(str2, false);
        } else if ("sysui_do_not_disturb".equals(str)) {
            z3 = TunerService.parseIntegerSwitch(str2, false);
        }
        setVolumePolicy(z, z2, z3, this.mVolumePolicy.vibrateToSilentDebounce);
    }

    private void setVolumePolicy(boolean z, boolean z2, boolean z3, int i) {
        VolumePolicy volumePolicy = new VolumePolicy(z, z2, z3, i);
        this.mVolumePolicy = volumePolicy;
        this.mController.setVolumePolicy(volumePolicy);
    }

    /* access modifiers changed from: package-private */
    public void setEnableDialogs(boolean z, boolean z2) {
        this.mController.setEnableDialogs(z, z2);
    }

    @Override // com.android.systemui.volume.VolumeDialogControllerImpl.UserActivityListener
    public void onUserActivity() {
        this.mKeyguardViewMediator.userActivity();
    }

    private void applyConfiguration() {
        this.mController.setVolumePolicy(this.mVolumePolicy);
        this.mController.showDndTile(true);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mConfigChanges.applyNewConfig(this.mContext.getResources())) {
            this.mController.mCallbacks.onConfigurationChanged();
        }
    }

    @Override // com.android.systemui.volume.VolumeComponent
    public void dismissNow() {
        this.mController.dismiss();
    }

    public void register() {
        this.mController.register();
        DndTile.setCombinedIcon(this.mContext, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSettings(Intent intent) {
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(intent, true, true);
    }
}
