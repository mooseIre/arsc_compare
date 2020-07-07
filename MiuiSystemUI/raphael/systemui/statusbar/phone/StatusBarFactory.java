package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBarTypeController;

public class StatusBarFactory {
    private static StatusBarFactory sFactory = new StatusBarFactory();

    public static StatusBarFactory getInstance() {
        return sFactory;
    }

    private StatusBarFactory() {
    }

    /* access modifiers changed from: package-private */
    public CollapsedStatusBarFragmentController getCollapsedStatusBarFragmentController(StatusBarTypeController.CutoutType cutoutType) {
        if (cutoutType == StatusBarTypeController.CutoutType.DRIP) {
            return new CollapsedStatusBarFragmentControllerDripImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.NONE) {
            return new CollapsedStatusBarFragmentControllerImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.NOTCH) {
            return new CollapsedStatusBarFragmentControllerNotchImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.HOLE) {
            return new CollapsedStatusBarFragmentControllerHoleImpl();
        }
        return new CollapsedStatusBarFragmentControllerNarrowNotchImpl();
    }

    /* access modifiers changed from: package-private */
    public KeyguardStatusBarViewController getKeyguardStatusBarViewController(Context context) {
        StatusBarTypeController.CutoutType cutoutType = ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType();
        if (cutoutType == StatusBarTypeController.CutoutType.DRIP) {
            return new KeyguardStatusBarViewControllerDripImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.NONE) {
            return new KeyguardStatusBarViewControllerImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.HOLE) {
            return new KeyguardStatusBarViewControllerHoleImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.NOTCH) {
            return new KeyguardStatusBarViewControllerNotchImpl();
        }
        return new KeyguardStatusBarViewControllerNarrowNotchImpl();
    }

    public SignalClusterViewController getSignalClusterViewController(Context context) {
        return getSignalClusterViewController(((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType());
    }

    public SignalClusterViewController getSignalClusterViewController(StatusBarTypeController.CutoutType cutoutType) {
        if (cutoutType == StatusBarTypeController.CutoutType.NONE || cutoutType == StatusBarTypeController.CutoutType.HOLE) {
            return new SignalClusterViewControllerImpl();
        }
        if (cutoutType == StatusBarTypeController.CutoutType.DRIP) {
            return new SignalClusterViewControllerDripImpl();
        }
        return new SignalClusterViewControllerNotchImpl();
    }
}
