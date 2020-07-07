package com.android.systemui.recents.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.MutableBoolean;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RecentsEventBus extends BroadcastReceiver {
    public static boolean DEBUG_TRACE_ALL = true;
    private static final Comparator<EventHandler> EVENT_HANDLER_COMPARATOR = new Comparator<EventHandler>() {
        public int compare(EventHandler eventHandler, EventHandler eventHandler2) {
            int i = eventHandler.priority;
            int i2 = eventHandler2.priority;
            if (i != i2) {
                return i2 - i;
            }
            return Long.compare(eventHandler2.subscriber.registrationTime, eventHandler.subscriber.registrationTime);
        }
    };
    private static volatile RecentsEventBus sDefaultBus;
    private static final Object sLock = new Object();
    private int mCallCount;
    private long mCallDurationMicros;
    private HashMap<Class<? extends Event>, ArrayList<EventHandler>> mEventTypeMap = new HashMap<>();
    private Handler mHandler;
    private HashMap<String, Class<? extends InterprocessEvent>> mInterprocessEventNameMap = new HashMap<>();
    private HashMap<Class<? extends Object>, ArrayList<EventHandlerMethod>> mSubscriberTypeMap = new HashMap<>();
    private ArrayList<Subscriber> mSubscribers = new ArrayList<>();

    public static class InterprocessEvent extends Event {
    }

    public static class Event implements Cloneable {
        boolean cancelled;
        boolean requiresPost;
        boolean trace = false;

        public String description() {
            return null;
        }

        /* access modifiers changed from: package-private */
        public void onPostDispatch() {
        }

        /* access modifiers changed from: package-private */
        public void onPreDispatch() {
        }

        protected Event() {
        }

        /* access modifiers changed from: protected */
        public Object clone() throws CloneNotSupportedException {
            Event event = (Event) super.clone();
            event.cancelled = false;
            return event;
        }
    }

    public static class AnimatedEvent extends Event {
        private final ReferenceCountedTrigger mTrigger = new ReferenceCountedTrigger();

        protected AnimatedEvent() {
        }

        public ReferenceCountedTrigger getAnimationTrigger() {
            return this.mTrigger;
        }

        public void addPostAnimationCallback(Runnable runnable) {
            this.mTrigger.addLastDecrementRunnable(runnable);
        }

        /* access modifiers changed from: package-private */
        public void onPreDispatch() {
            this.mTrigger.increment();
        }

        /* access modifiers changed from: package-private */
        public void onPostDispatch() {
            this.mTrigger.decrement();
        }

        /* access modifiers changed from: protected */
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }

    public static class ReusableEvent extends Event {
        private int mDispatchCount;

        protected ReusableEvent() {
        }

        /* access modifiers changed from: package-private */
        public void onPostDispatch() {
            super.onPostDispatch();
            this.mDispatchCount++;
        }

        /* access modifiers changed from: protected */
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }

    private RecentsEventBus(Looper looper) {
        this.mHandler = new Handler(looper);
    }

    public static RecentsEventBus getDefault() {
        if (sDefaultBus == null) {
            synchronized (sLock) {
                if (sDefaultBus == null) {
                    if (DEBUG_TRACE_ALL) {
                        logWithPid("New EventBus");
                    }
                    sDefaultBus = new RecentsEventBus(Looper.getMainLooper());
                }
            }
        }
        return sDefaultBus;
    }

    public void register(Object obj) {
        registerSubscriber(obj, 1, (MutableBoolean) null);
    }

    public void register(Object obj, int i) {
        registerSubscriber(obj, i, (MutableBoolean) null);
    }

    public void unregister(Object obj) {
        ArrayList arrayList;
        if (DEBUG_TRACE_ALL) {
            logWithPid("unregister()");
        }
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            throw new RuntimeException("Can not unregister() a subscriber from a non-main thread.");
        } else if (findRegisteredSubscriber(obj, true) && (arrayList = this.mSubscriberTypeMap.get(obj.getClass())) != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ArrayList arrayList2 = this.mEventTypeMap.get(((EventHandlerMethod) it.next()).eventType);
                for (int size = arrayList2.size() - 1; size >= 0; size--) {
                    if (((EventHandler) arrayList2.get(size)).subscriber.getReference() == obj) {
                        arrayList2.remove(size);
                    }
                }
            }
        }
    }

    public void send(Event event) {
        String str;
        if (Thread.currentThread().getId() == this.mHandler.getLooper().getThread().getId()) {
            if (DEBUG_TRACE_ALL) {
                StringBuilder sb = new StringBuilder();
                sb.append("send(");
                sb.append(event.getClass().getSimpleName());
                if (event.description() != null) {
                    str = "[" + event.description() + "]";
                } else {
                    str = "";
                }
                sb.append(str);
                sb.append(")");
                logWithPid(sb.toString());
            }
            event.requiresPost = false;
            event.cancelled = false;
            queueEvent(event);
            return;
        }
        throw new RuntimeException("Can not send() a message from a non-main thread.");
    }

    public void post(Event event) {
        String str;
        if (DEBUG_TRACE_ALL) {
            StringBuilder sb = new StringBuilder();
            sb.append("post(");
            sb.append(event.getClass().getSimpleName());
            if (event.description() != null) {
                str = "[" + event.description() + "]";
            } else {
                str = "";
            }
            sb.append(str);
            sb.append(")");
            logWithPid(sb.toString());
        }
        event.requiresPost = true;
        event.cancelled = false;
        queueEvent(event);
    }

    public void sendOntoMainThread(Event event) {
        if (Thread.currentThread().getId() != this.mHandler.getLooper().getThread().getId()) {
            post(event);
        } else {
            send(event);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (DEBUG_TRACE_ALL) {
            logWithPid("onReceive(" + intent.getAction() + ", user " + UserHandle.myUserId() + ")");
        }
        Bundle bundleExtra = intent.getBundleExtra("interprocess_event_bundle");
        try {
            send((Event) this.mInterprocessEventNameMap.get(intent.getAction()).getConstructor(new Class[]{Bundle.class}).newInstance(new Object[]{bundleExtra}));
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.e("EventBus", "Failed to create InterprocessEvent", e.getCause());
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.println(dumpInternal(str));
    }

    public String dumpInternal(String str) {
        String str2 = str + "  ";
        String str3 = str2 + "  ";
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("Registered class types:");
        sb.append("\n");
        ArrayList arrayList = new ArrayList(this.mSubscriberTypeMap.keySet());
        Collections.sort(arrayList, new Comparator<Class<?>>(this) {
            public int compare(Class<?> cls, Class<?> cls2) {
                return cls.getSimpleName().compareTo(cls2.getSimpleName());
            }
        });
        for (int i = 0; i < arrayList.size(); i++) {
            sb.append(str2);
            sb.append(((Class) arrayList.get(i)).getSimpleName());
            sb.append("\n");
        }
        sb.append(str);
        sb.append("Event map:");
        sb.append("\n");
        ArrayList arrayList2 = new ArrayList(this.mEventTypeMap.keySet());
        Collections.sort(arrayList2, new Comparator<Class<?>>(this) {
            public int compare(Class<?> cls, Class<?> cls2) {
                return cls.getSimpleName().compareTo(cls2.getSimpleName());
            }
        });
        for (int i2 = 0; i2 < arrayList2.size(); i2++) {
            Class cls = (Class) arrayList2.get(i2);
            sb.append(str2);
            sb.append(cls.getSimpleName());
            sb.append(" -> ");
            sb.append("\n");
            Iterator it = this.mEventTypeMap.get(cls).iterator();
            while (it.hasNext()) {
                EventHandler eventHandler = (EventHandler) it.next();
                Object reference = eventHandler.subscriber.getReference();
                if (reference != null) {
                    String hexString = Integer.toHexString(System.identityHashCode(reference));
                    sb.append(str3);
                    sb.append(reference.getClass().getSimpleName());
                    sb.append(" [0x" + hexString + ", #" + eventHandler.priority + "]");
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    /* JADX WARNING: type inference failed for: r4v3 */
    /* JADX WARNING: type inference failed for: r4v4, types: [boolean] */
    /* JADX WARNING: type inference failed for: r4v20 */
    private void registerSubscriber(Object obj, int i, MutableBoolean mutableBoolean) {
        Method[] methodArr;
        boolean z;
        Object obj2 = obj;
        int i2 = i;
        MutableBoolean mutableBoolean2 = mutableBoolean;
        if (Thread.currentThread().getId() == this.mHandler.getLooper().getThread().getId()) {
            ? r4 = 0;
            if (!findRegisteredSubscriber(obj2, false)) {
                long j = 0;
                if (DEBUG_TRACE_ALL) {
                    j = SystemClock.currentTimeMicro();
                    logWithPid("registerSubscriber(" + obj.getClass().getSimpleName() + ")");
                }
                Subscriber subscriber = new Subscriber(obj2, SystemClock.uptimeMillis());
                Class<?> cls = obj.getClass();
                ArrayList arrayList = this.mSubscriberTypeMap.get(cls);
                if (arrayList != null) {
                    if (DEBUG_TRACE_ALL) {
                        logWithPid("Subscriber class type already registered");
                    }
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        EventHandlerMethod eventHandlerMethod = (EventHandlerMethod) it.next();
                        ArrayList arrayList2 = this.mEventTypeMap.get(eventHandlerMethod.eventType);
                        arrayList2.add(new EventHandler(subscriber, eventHandlerMethod, i2));
                        sortEventHandlersByPriority(arrayList2);
                    }
                    this.mSubscribers.add(subscriber);
                    return;
                }
                if (DEBUG_TRACE_ALL) {
                    logWithPid("Subscriber class type requires registration");
                }
                ArrayList arrayList3 = new ArrayList();
                this.mSubscriberTypeMap.put(cls, arrayList3);
                this.mSubscribers.add(subscriber);
                MutableBoolean mutableBoolean3 = new MutableBoolean(false);
                Method[] methods = cls.getMethods();
                int length = methods.length;
                int i3 = 0;
                while (i3 < length) {
                    Method method = methods[i3];
                    Class[] parameterTypes = method.getParameterTypes();
                    mutableBoolean3.value = r4;
                    if (isValidEventBusHandlerMethod(method, parameterTypes, mutableBoolean3)) {
                        Class cls2 = parameterTypes[r4];
                        ArrayList arrayList4 = this.mEventTypeMap.get(cls2);
                        if (arrayList4 == null) {
                            arrayList4 = new ArrayList();
                            this.mEventTypeMap.put(cls2, arrayList4);
                        }
                        if (mutableBoolean3.value) {
                            methodArr = methods;
                            try {
                                cls2.getConstructor(new Class[]{Bundle.class});
                                this.mInterprocessEventNameMap.put(cls2.getName(), cls2);
                                if (mutableBoolean2 != null) {
                                    mutableBoolean2.value = true;
                                }
                            } catch (NoSuchMethodException unused) {
                                throw new RuntimeException("Expected InterprocessEvent to have a Bundle constructor");
                            }
                        } else {
                            methodArr = methods;
                        }
                        EventHandlerMethod eventHandlerMethod2 = new EventHandlerMethod(method, cls2);
                        arrayList4.add(new EventHandler(subscriber, eventHandlerMethod2, i2));
                        arrayList3.add(eventHandlerMethod2);
                        sortEventHandlersByPriority(arrayList4);
                        if (DEBUG_TRACE_ALL) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("  * Method: ");
                            sb.append(method.getName());
                            sb.append(" event: ");
                            z = false;
                            sb.append(parameterTypes[0].getSimpleName());
                            sb.append(" interprocess? ");
                            sb.append(mutableBoolean3.value);
                            logWithPid(sb.toString());
                        } else {
                            z = false;
                        }
                    } else {
                        methodArr = methods;
                        z = r4;
                    }
                    i3++;
                    Object obj3 = obj;
                    methods = methodArr;
                    r4 = z;
                }
                if (DEBUG_TRACE_ALL) {
                    logWithPid("Registered " + obj.getClass().getSimpleName() + " in " + (SystemClock.currentTimeMicro() - j) + " microseconds");
                    return;
                }
                return;
            }
            return;
        }
        throw new RuntimeException("Can not register() a subscriber from a non-main thread.");
    }

    private void queueEvent(final Event event) {
        ArrayList arrayList = this.mEventTypeMap.get(event.getClass());
        if (arrayList != null) {
            event.onPreDispatch();
            ArrayList arrayList2 = (ArrayList) arrayList.clone();
            int size = arrayList2.size();
            boolean z = false;
            for (int i = 0; i < size; i++) {
                final EventHandler eventHandler = (EventHandler) arrayList2.get(i);
                if (eventHandler.subscriber.getReference() != null) {
                    if (event.requiresPost) {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                RecentsEventBus.this.processEvent(eventHandler, event);
                            }
                        });
                        z = true;
                    } else {
                        processEvent(eventHandler, event);
                    }
                }
            }
            if (z) {
                this.mHandler.post(new Runnable(this) {
                    public void run() {
                        event.onPostDispatch();
                    }
                });
            } else {
                event.onPostDispatch();
            }
        }
    }

    /* access modifiers changed from: private */
    public void processEvent(EventHandler eventHandler, Event event) {
        if (!event.cancelled) {
            try {
                if (event.trace || DEBUG_TRACE_ALL) {
                    logWithPid(" -> " + eventHandler.toString());
                }
                Object reference = eventHandler.subscriber.getReference();
                if (reference != null) {
                    long j = 0;
                    if (DEBUG_TRACE_ALL) {
                        j = SystemClock.currentTimeMicro();
                    }
                    eventHandler.method.invoke(reference, event);
                    if (DEBUG_TRACE_ALL) {
                        long currentTimeMicro = SystemClock.currentTimeMicro() - j;
                        this.mCallDurationMicros += currentTimeMicro;
                        this.mCallCount++;
                        logWithPid(eventHandler.method.toString() + " duration: " + currentTimeMicro + " microseconds, avg: " + (this.mCallDurationMicros / ((long) this.mCallCount)));
                        return;
                    }
                    return;
                }
                Log.e("EventBus", "Failed to deliver event to null subscriber");
            } catch (IllegalAccessException e) {
                Log.e("EventBus", "Failed to invoke method", e.getCause());
            } catch (InvocationTargetException e2) {
                throw new RuntimeException(e2.getCause());
            }
        } else if (event.trace || DEBUG_TRACE_ALL) {
            logWithPid("Event dispatch cancelled");
        }
    }

    private boolean findRegisteredSubscriber(Object obj, boolean z) {
        for (int size = this.mSubscribers.size() - 1; size >= 0; size--) {
            if (this.mSubscribers.get(size).getReference() == obj) {
                if (z) {
                    this.mSubscribers.remove(size);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isValidEventBusHandlerMethod(Method method, Class<?>[] clsArr, MutableBoolean mutableBoolean) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || !Modifier.isFinal(modifiers) || !method.getReturnType().equals(Void.TYPE) || clsArr.length != 1) {
            if (DEBUG_TRACE_ALL) {
                if (!Modifier.isPublic(modifiers)) {
                    logWithPid("  Expected method to be public: " + method.getName());
                } else if (!Modifier.isFinal(modifiers)) {
                    logWithPid("  Expected method to be final: " + method.getName());
                } else if (!method.getReturnType().equals(Void.TYPE)) {
                    logWithPid("  Expected method to return null: " + method.getName());
                }
            }
        } else if (InterprocessEvent.class.isAssignableFrom(clsArr[0]) && method.getName().startsWith("onInterprocessBusEvent")) {
            mutableBoolean.value = true;
            return true;
        } else if (Event.class.isAssignableFrom(clsArr[0]) && method.getName().startsWith("onBusEvent")) {
            mutableBoolean.value = false;
            return true;
        } else if (DEBUG_TRACE_ALL) {
            if (!Event.class.isAssignableFrom(clsArr[0])) {
                logWithPid("  Expected method take an Event-based parameter: " + method.getName());
            } else if (!method.getName().startsWith("onInterprocessBusEvent") && !method.getName().startsWith("onBusEvent")) {
                logWithPid("  Expected method start with method prefix: " + method.getName());
            }
        }
        return false;
    }

    private void sortEventHandlersByPriority(List<EventHandler> list) {
        Collections.sort(list, EVENT_HANDLER_COMPARATOR);
    }

    private static void logWithPid(String str) {
        Log.d("EventBus", "[" + Process.myPid() + ", u" + UserHandle.myUserId() + "] " + str);
    }
}
