package com.android.keyguard.AwesomeLockScreenImp;

import miui.maml.ScreenElementRoot;
import miui.maml.elements.ScreenElement;
import miui.maml.elements.ScreenElementFactory;
import org.w3c.dom.Element;

public class LockScreenElementFactory extends ScreenElementFactory {
    public ScreenElement createInstance(Element element, ScreenElementRoot screenElementRoot) {
        String tagName = element.getTagName();
        if (tagName.equalsIgnoreCase("Unlocker")) {
            return new UnlockerScreenElement(element, (LockScreenRoot) screenElementRoot);
        }
        if (tagName.equalsIgnoreCase("Wallpaper")) {
            return new WallpaperScreenElement(element, screenElementRoot);
        }
        return LockScreenElementFactory.super.createInstance(element, screenElementRoot);
    }
}
