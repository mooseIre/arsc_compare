package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.util.Log;
import com.android.systemui.shared.recents.IMiuiSystemUiProxy;
import com.android.systemui.stackdivider.Divider;
import java.util.Optional;
import kotlin.jvm.internal.Intrinsics;

public final class MiuiOverviewProxy extends IMiuiSystemUiProxy.Stub {
    private final OverviewProxyService proxyService;

    private final void notifyGestureLineProgress(float f) {
    }

    public MiuiOverviewProxy(OverviewProxyService overviewProxyService) {
        Intrinsics.checkParameterIsNotNull(overviewProxyService, "proxyService");
        this.proxyService = overviewProxyService;
    }

    @Override // com.android.systemui.shared.recents.IMiuiSystemUiProxy
    public void exitSplitScreen() {
        if (verifyCaller("startScreenPinning")) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Optional<Divider> dividerOptional = this.proxyService.getDividerOptional();
                Intrinsics.checkExpressionValueIsNotNull(dividerOptional, "proxyService.dividerOptional");
                if (dividerOptional.isPresent()) {
                    this.proxyService.getHandler().post(new MiuiOverviewProxy$exitSplitScreen$1(this));
                }
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    @Override // com.android.systemui.shared.recents.IMiuiSystemUiProxy
    public Rect getMiddleSplitScreenSecondaryBounds() {
        if (!verifyCaller("getMiddleSplitScreenSecondaryBounds")) {
            return null;
        }
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Optional<Divider> dividerOptional = this.proxyService.getDividerOptional();
            Intrinsics.checkExpressionValueIsNotNull(dividerOptional, "proxyService.dividerOptional");
            if (dividerOptional.isPresent()) {
                Divider divider = this.proxyService.getDividerOptional().get();
                Intrinsics.checkExpressionValueIsNotNull(divider, "proxyService.dividerOptional.get()");
                return divider.getView().getMiddleSplitScreenSecondaryBounds();
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
            return null;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    @Override // com.android.systemui.shared.recents.IMiuiSystemUiProxy
    public void onGestureLineProgress(float f) {
        if (verifyCaller("onGestureLineProgress")) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                this.proxyService.getHandler().post(new MiuiOverviewProxy$onGestureLineProgress$1(this, f));
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    @Override // com.android.systemui.shared.recents.IMiuiSystemUiProxy
    public void onAssistantGestureCompletion() {
        if (verifyCaller("onAssistantGestureCompletion")) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                this.proxyService.getHandler().post(new MiuiOverviewProxy$sam$java_lang_Runnable$0(new MiuiOverviewProxy$onAssistantGestureCompletion$1(this)));
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    private final boolean verifyCaller(String str) {
        int identifier = Binder.getCallingUserHandle().getIdentifier();
        if (identifier == this.proxyService.getCurrentBoundedUserId()) {
            return true;
        }
        Log.w("OverviewProxyService", "Launcher called sysui with invalid user: " + identifier + ", reason: " + str);
        return false;
    }

    private final void notifyCompleteAssistant() {
        this.proxyService.notifyAssistantGestureCompletion(0.0f);
    }
}
