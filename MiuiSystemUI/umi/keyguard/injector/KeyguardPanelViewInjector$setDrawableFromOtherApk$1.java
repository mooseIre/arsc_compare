package com.android.keyguard.injector;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.keyguard.utils.PackageUtils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$setDrawableFromOtherApk$1 extends AsyncTask<Void, Void, Drawable> {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    KeyguardPanelViewInjector$setDrawableFromOtherApk$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    /* access modifiers changed from: protected */
    @Nullable
    public Drawable doInBackground(@NotNull Void... voidArr) {
        Intrinsics.checkParameterIsNotNull(voidArr, "params");
        if (!KeyguardPanelViewInjector.access$getMKeyguardUpdateMonitor$p(this.this$0).isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser())) {
            return null;
        }
        KeyguardPanelViewInjector keyguardPanelViewInjector = this.this$0;
        keyguardPanelViewInjector.mLeftViewBackgroundImageDrawable = PackageUtils.getDrawableFromPackage(keyguardPanelViewInjector.getMContext(), LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).getPreTransToLeftScreenDrawableResName());
        return PackageUtils.getDrawableFromPackage(this.this$0.getMContext(), LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardPanelViewInjector.access$getMLockScreenMagazineController$p(this.this$0).getPreLeftScreenDrawableResName());
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(@Nullable Drawable drawable) {
        MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.this$0.mLeftView;
        if (miuiKeyguardMoveLeftViewContainer != null) {
            miuiKeyguardMoveLeftViewContainer.setCustomBackground(drawable);
        }
        if (this.this$0.mLeftViewBackgroundImageDrawable != null) {
            ImageView access$getMLeftViewBackgroundView$p = KeyguardPanelViewInjector.access$getMLeftViewBackgroundView$p(this.this$0);
            Drawable drawable2 = this.this$0.mLeftViewBackgroundImageDrawable;
            if (drawable2 != null) {
                access$getMLeftViewBackgroundView$p.setBackgroundDrawable(drawable2);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            KeyguardPanelViewInjector.access$getMLeftViewBackgroundView$p(this.this$0).setBackgroundColor(KeyguardPanelViewInjector.access$getMWallpaperController$p(this.this$0).getWallpaperBlurColor());
        }
    }
}
