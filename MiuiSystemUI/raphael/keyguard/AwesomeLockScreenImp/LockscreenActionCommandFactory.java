package com.android.keyguard.AwesomeLockScreenImp;

import miui.maml.ActionCommand;
import miui.maml.ObjectFactory;
import miui.maml.elements.ScreenElement;
import org.w3c.dom.Element;

public class LockscreenActionCommandFactory extends ObjectFactory.ActionCommandFactory {
    /* access modifiers changed from: protected */
    public ActionCommand doCreate(ScreenElement screenElement, Element element) {
        if (element.getNodeName().equals("UnlockVerifyPasswordCommand")) {
            return new UnlockVerifyPasswordCommand(screenElement, element);
        }
        return null;
    }
}
