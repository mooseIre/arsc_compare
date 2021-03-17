package com.android.systemui.statusbar.notification.row;

import android.view.accessibility.AccessibilityManager;
import com.android.systemui.plugins.FalsingManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ActivatableNotificationViewController_Factory implements Factory<ActivatableNotificationViewController> {
    private final Provider<AccessibilityManager> accessibilityManagerProvider;
    private final Provider<ExpandableOutlineViewController> expandableOutlineViewControllerProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<ActivatableNotificationView> viewProvider;

    public ActivatableNotificationViewController_Factory(Provider<ActivatableNotificationView> provider, Provider<ExpandableOutlineViewController> provider2, Provider<AccessibilityManager> provider3, Provider<FalsingManager> provider4) {
        this.viewProvider = provider;
        this.expandableOutlineViewControllerProvider = provider2;
        this.accessibilityManagerProvider = provider3;
        this.falsingManagerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public ActivatableNotificationViewController get() {
        return provideInstance(this.viewProvider, this.expandableOutlineViewControllerProvider, this.accessibilityManagerProvider, this.falsingManagerProvider);
    }

    public static ActivatableNotificationViewController provideInstance(Provider<ActivatableNotificationView> provider, Provider<ExpandableOutlineViewController> provider2, Provider<AccessibilityManager> provider3, Provider<FalsingManager> provider4) {
        return new ActivatableNotificationViewController(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static ActivatableNotificationViewController_Factory create(Provider<ActivatableNotificationView> provider, Provider<ExpandableOutlineViewController> provider2, Provider<AccessibilityManager> provider3, Provider<FalsingManager> provider4) {
        return new ActivatableNotificationViewController_Factory(provider, provider2, provider3, provider4);
    }
}
