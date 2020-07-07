package com.android.systemui.recents.events;

import com.android.systemui.recents.events.RecentsEventBus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* compiled from: RecentsEventBus */
class EventHandlerMethod {
    Class<? extends RecentsEventBus.Event> eventType;
    private Method mMethod;

    EventHandlerMethod(Method method, Class<? extends RecentsEventBus.Event> cls) {
        this.mMethod = method;
        this.mMethod.setAccessible(true);
        this.eventType = cls;
    }

    public void invoke(Object obj, RecentsEventBus.Event event) throws InvocationTargetException, IllegalAccessException {
        this.mMethod.invoke(obj, new Object[]{event});
    }

    public String toString() {
        return this.mMethod.getName() + "(" + this.eventType.getSimpleName() + ")";
    }
}
