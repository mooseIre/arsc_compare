package com.android.systemui.recents.events;

/* compiled from: RecentsEventBus */
class EventHandler {
    EventHandlerMethod method;
    int priority;
    Subscriber subscriber;

    EventHandler(Subscriber subscriber2, EventHandlerMethod eventHandlerMethod, int i) {
        this.subscriber = subscriber2;
        this.method = eventHandlerMethod;
        this.priority = i;
    }

    public String toString() {
        return this.subscriber.toString(this.priority) + " " + this.method.toString();
    }
}
