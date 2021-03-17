package com.android.systemui.statusbar.notification;

import android.view.LayoutInflater;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import com.android.systemui.statusbar.policy.ConfigurationController;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiNotificationSectionsManager.kt */
public final class MiuiNotificationSectionsManager$configurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ MiuiNotificationSectionsManager this$0;

    MiuiNotificationSectionsManager$configurationListener$1(MiuiNotificationSectionsManager miuiNotificationSectionsManager) {
        this.this$0 = miuiNotificationSectionsManager;
    }

    public void onUiModeChanged() {
        ZenModeView zenModeView = this.this$0.getZenModeView();
        if (zenModeView != null) {
            zenModeView.reInflate();
        }
    }

    public void onLocaleListChanged() {
        MiuiNotificationSectionsManager miuiNotificationSectionsManager = this.this$0;
        LayoutInflater from = LayoutInflater.from(miuiNotificationSectionsManager.getParent().getContext());
        Intrinsics.checkExpressionValueIsNotNull(from, "LayoutInflater.from(parent.context)");
        miuiNotificationSectionsManager.reinflateZenModeView(from);
    }
}
