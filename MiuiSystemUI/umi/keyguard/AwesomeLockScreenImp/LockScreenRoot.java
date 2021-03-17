package com.android.keyguard.AwesomeLockScreenImp;

import android.content.Intent;
import android.provider.Settings;
import android.view.MotionEvent;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.charge.MiuiBatteryStatus;
import java.util.Iterator;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.data.BatteryVariableUpdater;
import miui.maml.data.IndexedVariable;
import miui.maml.data.VariableUpdaterManager;
import miui.maml.data.VolumeVariableUpdater;
import miui.maml.elements.ElementGroup;
import miui.maml.elements.ScreenElement;
import miui.maml.util.Utils;
import miui.os.Build;
import org.w3c.dom.Element;

public class LockScreenRoot extends ScreenElementRoot implements ScreenElementRoot.OnExternCommandListener {
    private String curCategory;
    private MiuiBatteryStatus mBatteryInfo;
    private IndexedVariable mBatteryLevel = new IndexedVariable("battery_level", ((ScreenElementRoot) this).mContext.mVariables, true);
    private IndexedVariable mBatteryState = new IndexedVariable("battery_state", ((ScreenElementRoot) this).mContext.mVariables, true);
    private IndexedVariable mBatteryType = new IndexedVariable("battery_type", ((ScreenElementRoot) this).mContext.mVariables, true);
    private float mFrameRateBatteryFull;
    private float mFrameRateBatteryLow;
    private float mFrameRateCharging;
    private boolean mInit;
    private LockscreenCallback mLockscreenCallback;
    private float mNormalFrameRate;

    public interface LockscreenCallback {
        void disableChargeAnimation(boolean z);

        void disableLockScreenFaceUnlockAnim(boolean z);

        void disableLockScreenFod(boolean z);

        void disableLockScreenFodAnim(boolean z);

        int getPasswordMode();

        void haptic(int i);

        boolean isSecure();

        boolean isSoundEnable();

        void pokeWakelock();

        void startLockScreenFaceUnlock();

        void stopLockScreenFaceUnlock();

        boolean unlockVerify(String str, int i);

        void unlocked(Intent intent, int i);
    }

    public LockScreenRoot(ScreenContext screenContext) {
        super(screenContext);
        setOnExternCommandListener(this);
        screenContext.registerObjectFactory("BitmapProvider", new LockscreenBitmapProviderFactory());
        screenContext.registerObjectFactory("ActionCommand", new LockscreenActionCommandFactory());
    }

    public void setLockscreenCallback(LockscreenCallback lockscreenCallback) {
        this.mLockscreenCallback = lockscreenCallback;
    }

    public boolean onTouch(MotionEvent motionEvent) {
        ElementGroup elementGroup = ((ScreenElementRoot) this).mInnerGroup;
        if (elementGroup != null && elementGroup.getElements().size() != 0) {
            return LockScreenRoot.super.onTouch(motionEvent);
        }
        this.mLockscreenCallback.unlocked(null, 0);
        return false;
    }

    public void pokeWakelock() {
        this.mLockscreenCallback.pokeWakelock();
    }

    /* access modifiers changed from: protected */
    public boolean shouldPlaySound() {
        return this.mLockscreenCallback.isSoundEnable();
    }

    public void haptic(int i) {
        this.mLockscreenCallback.haptic(i);
    }

    public void unlocked(Intent intent, int i) {
        this.mLockscreenCallback.unlocked(intent, i);
    }

    public boolean unlockVerify(String str, int i) {
        return this.mLockscreenCallback.unlockVerify(str, i);
    }

    public int getPasswordMode() {
        return this.mLockscreenCallback.getPasswordMode();
    }

    /* access modifiers changed from: protected */
    public void onAddVariableUpdater(VariableUpdaterManager variableUpdaterManager) {
        LockScreenRoot.super.onAddVariableUpdater(variableUpdaterManager);
        variableUpdaterManager.add(new BatteryVariableUpdater(variableUpdaterManager));
        variableUpdaterManager.add(new VolumeVariableUpdater(variableUpdaterManager));
    }

    public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
        int i;
        String str;
        if (!this.mInit) {
            this.mBatteryInfo = miuiBatteryStatus;
            return;
        }
        this.mBatteryLevel.set((double) miuiBatteryStatus.getLevel());
        this.mBatteryType.set((double) miuiBatteryStatus.plugged);
        Utils.putVariableNumber("ChargeWireState", ((ScreenElementRoot) this).mContext.mVariables, (double) miuiBatteryStatus.wireState);
        Utils.putVariableNumber("ChargeSpeed", ((ScreenElementRoot) this).mContext.mVariables, (double) miuiBatteryStatus.chargeSpeed);
        if (!miuiBatteryStatus.isPluggedIn() && !miuiBatteryStatus.isBatteryLow()) {
            ((ScreenElementRoot) this).mFrameRate = this.mNormalFrameRate;
            str = "Normal";
            i = 0;
        } else if (!miuiBatteryStatus.isPluggedIn()) {
            i = 2;
            ((ScreenElementRoot) this).mFrameRate = this.mFrameRateBatteryLow;
            str = "BatteryLow";
        } else if (miuiBatteryStatus.getLevel() >= 100) {
            i = 3;
            ((ScreenElementRoot) this).mFrameRate = this.mFrameRateBatteryFull;
            str = "BatteryFull";
        } else {
            ((ScreenElementRoot) this).mFrameRate = this.mFrameRateCharging;
            i = 1;
            str = "Charging";
        }
        if (str != this.curCategory) {
            requestFramerate(((ScreenElementRoot) this).mFrameRate);
            requestUpdate();
            this.mBatteryState.set((double) i);
            showCategory("BatteryFull", false);
            showCategory("Charging", false);
            showCategory("BatteryLow", false);
            showCategory("Normal", false);
            showCategory(str, true);
            this.curCategory = str;
        }
    }

    public void init() {
        boolean z = Settings.System.getIntForUser(((ScreenElementRoot) this).mContext.mContext.getContentResolver(), "pref_key_enable_notification_body", 1, KeyguardUpdateMonitor.getCurrentUser()) == 1 && !this.mLockscreenCallback.isSecure();
        double d = 1.0d;
        Utils.putVariableNumber("sms_body_preview", ((ScreenElementRoot) this).mContext.mVariables, z ? 1.0d : 0.0d);
        this.mInit = true;
        if (!z) {
            ((ScreenElementRoot) this).mVariableBinderManager.acceptVisitor(new BlockedColumnsSetter("content://sms/inbox", "body"));
        }
        putRawAttr("__is_secure", String.valueOf(this.mLockscreenCallback.isSecure()));
        if (!Build.IS_CU_CUSTOMIZATION) {
            if (Build.IS_CM_CUSTOMIZATION) {
                d = 2.0d;
            } else {
                d = Build.IS_CT_CUSTOMIZATION ? 3.0d : 0.0d;
            }
        }
        Utils.putVariableNumber("operator_customization", ((ScreenElementRoot) this).mContext.mVariables, d);
        LockScreenRoot.super.init();
        MiuiBatteryStatus miuiBatteryStatus = this.mBatteryInfo;
        if (miuiBatteryStatus != null) {
            onRefreshBatteryInfo(miuiBatteryStatus);
            this.mBatteryInfo = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean onLoad(Element element) {
        if (!LockScreenRoot.super.onLoad(element)) {
            return false;
        }
        float attrAsFloat = Utils.getAttrAsFloat(element, "frameRate", ((ScreenElementRoot) this).DEFAULT_FRAME_RATE);
        this.mNormalFrameRate = attrAsFloat;
        this.mFrameRateCharging = Utils.getAttrAsFloat(element, "frameRateCharging", attrAsFloat);
        this.mFrameRateBatteryLow = Utils.getAttrAsFloat(element, "frameRateBatteryLow", this.mNormalFrameRate);
        this.mFrameRateBatteryFull = Utils.getAttrAsFloat(element, "frameRateBatteryFull", this.mNormalFrameRate);
        setClearCanvas(!"false".equalsIgnoreCase(element.getAttribute("clearCanvas")));
        BuiltinVariableBinders.fill(((ScreenElementRoot) this).mVariableBinderManager);
        ((ScreenElementRoot) this).mFrameRate = this.mNormalFrameRate;
        return true;
    }

    public void finish() {
        LockScreenRoot.super.finish();
        this.curCategory = null;
        this.mInit = false;
        this.mBatteryInfo = null;
    }

    public void startUnlockMoving(UnlockerScreenElement unlockerScreenElement) {
        startUnlockMoving(((ScreenElementRoot) this).mInnerGroup, unlockerScreenElement);
    }

    public void endUnlockMoving(UnlockerScreenElement unlockerScreenElement) {
        endUnlockMoving(((ScreenElementRoot) this).mInnerGroup, unlockerScreenElement);
    }

    private void startUnlockMoving(ElementGroup elementGroup, UnlockerScreenElement unlockerScreenElement) {
        if (elementGroup != null) {
            Iterator it = elementGroup.getElements().iterator();
            while (it.hasNext()) {
                UnlockerScreenElement unlockerScreenElement2 = (ScreenElement) it.next();
                if (unlockerScreenElement2 instanceof UnlockerScreenElement) {
                    unlockerScreenElement2.startUnlockMoving(unlockerScreenElement);
                } else if (unlockerScreenElement2 instanceof ElementGroup) {
                    startUnlockMoving((ElementGroup) unlockerScreenElement2, unlockerScreenElement);
                }
            }
        }
    }

    private void endUnlockMoving(ElementGroup elementGroup, UnlockerScreenElement unlockerScreenElement) {
        if (elementGroup != null) {
            Iterator it = elementGroup.getElements().iterator();
            while (it.hasNext()) {
                UnlockerScreenElement unlockerScreenElement2 = (ScreenElement) it.next();
                if (unlockerScreenElement2 instanceof UnlockerScreenElement) {
                    unlockerScreenElement2.endUnlockMoving(unlockerScreenElement);
                } else if (unlockerScreenElement2 instanceof ElementGroup) {
                    endUnlockMoving((ElementGroup) unlockerScreenElement2, unlockerScreenElement);
                }
            }
        }
    }

    public void onUIInteractive(ScreenElement screenElement, String str) {
        this.mLockscreenCallback.pokeWakelock();
    }

    public void onCommand(String str, Double d, String str2) {
        boolean z = false;
        if ("unlock".equals(str)) {
            unlocked(null, 0);
        } else if ("pokewakelock".equals(str)) {
            pokeWakelock();
        } else if ("disableFod".equals(str)) {
            if (d != null && d.intValue() == 1) {
                z = true;
            }
            this.mLockscreenCallback.disableLockScreenFod(z);
        } else if ("disableFodAnim".equals(str)) {
            if (d != null && d.intValue() == 1) {
                z = true;
            }
            this.mLockscreenCallback.disableLockScreenFodAnim(z);
        } else if ("disableChargeAnim".equals(str)) {
            if (d != null && d.intValue() == 1) {
                z = true;
            }
            this.mLockscreenCallback.disableChargeAnimation(z);
        } else if ("startFaceUnlock".equals(str)) {
            this.mLockscreenCallback.startLockScreenFaceUnlock();
        } else if ("stopFaceUnlock".equals(str)) {
            this.mLockscreenCallback.stopLockScreenFaceUnlock();
        } else if ("disableKeyguardFaceUnlockAnim".equals(str)) {
            if (d != null && d.intValue() == 1) {
                z = true;
            }
            this.mLockscreenCallback.disableLockScreenFaceUnlockAnim(z);
        }
    }
}
