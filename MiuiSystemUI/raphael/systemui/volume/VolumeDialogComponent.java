package com.android.systemui.volume;

import android.content.Context;
import android.content.res.Configuration;
import android.media.VolumePolicy;
import android.os.Bundle;
import android.os.Handler;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.SystemUI;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.miui.volume.MiuiVolumeDialogImpl;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VolumeDialogComponent implements VolumeComponent, TunerService.Tunable, VolumeDialogControllerImpl.UserActivityListener {
    private final Context mContext;
    private final VolumeDialogControllerImpl mController;
    private VolumeDialog mDialog;
    private final ExtensionController.Extension mExtension;
    private final SystemUI mSysui;
    private final VolumeDialog.Callback mVolumeDialogCallback = new VolumeDialog.Callback() {
        public void onZenPrioritySettingsClicked() {
        }

        public void onZenSettingsClicked() {
        }
    };
    private VolumePolicy mVolumePolicy = new VolumePolicy(true, true, false, 400);

    public void dispatchDemoCommand(String str, Bundle bundle) {
    }

    public void onConfigurationChanged(Configuration configuration) {
    }

    public VolumeDialogComponent(SystemUI systemUI, Context context, Handler handler) {
        this.mSysui = systemUI;
        this.mContext = context;
        this.mController = (VolumeDialogControllerImpl) Dependency.get(VolumeDialogController.class);
        this.mController.setUserActivityListener(this);
        ((PluginDependencyProvider) Dependency.get(PluginDependencyProvider.class)).allowPluginDependency(VolumeDialogController.class);
        ExtensionController.ExtensionBuilder<VolumeDialog> newExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(VolumeDialog.class);
        newExtension.withPlugin(VolumeDialog.class);
        newExtension.withDefault(new Supplier() {
            public final Object get() {
                return VolumeDialogComponent.this.lambda$new$0$VolumeDialogComponent();
            }
        });
        newExtension.withCallback(new Consumer() {
            public final void accept(Object obj) {
                VolumeDialogComponent.this.lambda$new$1$VolumeDialogComponent((VolumeDialog) obj);
            }
        });
        this.mExtension = newExtension.build();
        applyConfiguration();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_volume_down_silent", "sysui_volume_up_silent", "sysui_do_not_disturb");
    }

    public /* synthetic */ void lambda$new$1$VolumeDialogComponent(VolumeDialog volumeDialog) {
        VolumeDialog volumeDialog2 = this.mDialog;
        if (volumeDialog2 != null) {
            volumeDialog2.destroy();
        }
        this.mDialog = volumeDialog;
        this.mDialog.init(2020, this.mVolumeDialogCallback);
    }

    /* access modifiers changed from: private */
    /* renamed from: createDefault */
    public VolumeDialog lambda$new$0$VolumeDialogComponent() {
        MiuiVolumeDialogImpl miuiVolumeDialogImpl = new MiuiVolumeDialogImpl(this.mContext);
        miuiVolumeDialogImpl.setStreamImportant(4, true);
        miuiVolumeDialogImpl.setStreamImportant(1, false);
        miuiVolumeDialogImpl.setAutomute(true);
        miuiVolumeDialogImpl.setSilentMode(false);
        return miuiVolumeDialogImpl;
    }

    public void onTuningChanged(String str, String str2) {
        boolean z = false;
        boolean z2 = true;
        if ("sysui_volume_down_silent".equals(str)) {
            if (str2 != null && Integer.parseInt(str2) == 0) {
                z2 = false;
            }
            VolumePolicy volumePolicy = this.mVolumePolicy;
            setVolumePolicy(z2, volumePolicy.volumeUpToExitSilent, volumePolicy.doNotDisturbWhenSilent, volumePolicy.vibrateToSilentDebounce);
        } else if ("sysui_volume_up_silent".equals(str)) {
            if (str2 != null && Integer.parseInt(str2) == 0) {
                z2 = false;
            }
            VolumePolicy volumePolicy2 = this.mVolumePolicy;
            setVolumePolicy(volumePolicy2.volumeDownToEnterSilent, z2, volumePolicy2.doNotDisturbWhenSilent, volumePolicy2.vibrateToSilentDebounce);
        } else if ("sysui_do_not_disturb".equals(str)) {
            if (!(str2 == null || Integer.parseInt(str2) == 0)) {
                z = true;
            }
            VolumePolicy volumePolicy3 = this.mVolumePolicy;
            setVolumePolicy(volumePolicy3.volumeDownToEnterSilent, volumePolicy3.volumeUpToExitSilent, z, volumePolicy3.vibrateToSilentDebounce);
        }
    }

    private void setVolumePolicy(boolean z, boolean z2, boolean z3, int i) {
        this.mVolumePolicy = new VolumePolicy(z, z2, z3, i);
        this.mController.setVolumePolicy(this.mVolumePolicy);
    }

    public void onUserActivity() {
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) this.mSysui.getComponent(KeyguardViewMediator.class);
        if (keyguardViewMediator != null) {
            keyguardViewMediator.userActivity();
        }
    }

    private void applyConfiguration() {
        this.mController.setVolumePolicy(this.mVolumePolicy);
        this.mController.showDndTile(true);
    }

    public void dismissNow() {
        this.mController.dismiss();
    }

    public void register() {
        this.mController.register();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        VolumeDialog volumeDialog = this.mDialog;
        if (volumeDialog instanceof Dumpable) {
            ((Dumpable) volumeDialog).dump(fileDescriptor, printWriter, strArr);
        }
    }
}
